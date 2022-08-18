//
// Created by rick on 8/17/2022.
//

#include "scanner.h"
#include "lox.h"

#include <unicode/numberformatter.h>
#include <map>
#include <utility>

using namespace icu;
namespace lox {
static std::map<UnicodeString, TokenType> kKeywords{
    {
        "and",
        TokenType::AND,
    },
    {"class", TokenType::CLASS},
    {"else", TokenType::ELSE},
    {"false", TokenType::FALSE},
    {"for", TokenType::FOR},
    {"fun", TokenType::FUN},
    {"if", TokenType::IF},
    {"nil", TokenType::NIL},
    {"or", TokenType::OR},
    {"print", TokenType::PRINT},
    {"return", TokenType::RETURN},
    {"super", TokenType::SUPER},
    {"this", TokenType::THIS},
    {"true", TokenType::TRUE},
    {"var", TokenType::VAR},
    {"while", TokenType::WHILE}};
struct ScannerInner {
  UnicodeString source;
  const int32_t sourceLength;
  int current = 0;
  int start = 0;
  int line = 1;
  std::vector<Token> tokens;
  NumberFormat* numberFormatter = nullptr;

  explicit ScannerInner(UnicodeString source)
      : source(std::move(source)), sourceLength{this->source.length()} {
    UErrorCode status = U_ZERO_ERROR;
    numberFormatter = NumberFormat::createInstance(Locale::getDefault(),
                                                   UNUM_DECIMAL, status);
  }
  ~ScannerInner() {
    if (numberFormatter)
      delete numberFormatter;
  }

  bool isAtEnd() { return current >= sourceLength; }

  void scanTokens() {
    while (!isAtEnd()) {
      start = current;
      scanToken();
    }

    tokens.emplace_back(TokenType::EndOfFile, "", NoValue, line);
  }

  char16_t advance() {
    current += 1;
    return source[current - 1];
  }

  char16_t peek() {
    if (isAtEnd())
      return u'\u0000';
    else
      return source[current];
  }

  bool matchChar(char16_t expected) {
    if (isAtEnd())
      return false;
    if (source[current] != expected)
      return false;
    current += 1;
    return true;
  }

  void scanToken() {
    auto c = advance();
    switch (c) {
      case u'(':
        addToken(TokenType::LEFT_PAREN);
        break;
      case u')':
        addToken(TokenType::RIGHT_PAREN);
        break;
      case u'{':
        addToken(TokenType::LEFT_BRACE);
        break;
      case u'}':
        addToken(TokenType::RIGHT_BRACE);
        break;
      case u',':
        addToken(TokenType::COMMA);
        break;
      case u'.':
        addToken(TokenType::DOT);
        break;
      case u'-':
        addToken(TokenType::MINUS);
        break;
      case u'+':
        addToken(TokenType::PLUS);
        break;
      case u';':
        addToken(TokenType::SEMICOLON);
        break;
      case u'*':
        addToken(TokenType::STAR);
        break;
      case u'!':
        addToken(matchChar('=') ? TokenType::BANG_EQUAL : TokenType::BANG);
        break;
      case u'=':
        addToken(matchChar('=') ? TokenType::EQUAL_EQUAL : TokenType::EQUAL);
        break;
      case u'<':
        addToken(matchChar('=') ? TokenType::LESS_EQUAL : TokenType::LESS);
        break;
      case u'>':
        addToken(matchChar('=') ? TokenType::GREATER_EQUAL
                                : TokenType::GREATER);
        break;
      case u'/':
        if (matchChar(u'/')) {
          while (peek() != u'\n' && !isAtEnd()) {
            advance();
          }
        } else if (matchChar(u'*')) {
          while (!isAtEnd()) {
            if (matchChar(u'*')) {
              if (matchChar(u'/')) {
                break;
              }
            } else {
              advance();
            }
          }
        }
        break;
      case u' ':
      case u'\r':
      case u'\t':
        break;
      case u'\n':
        line += 1;
        break;
      case u'"':
        scanString();
        break;
      default:
        if (isDigit(c)) {
          scanNumber();
        } else if (isAlpha(c)) {
          scanIdentifier();
        } else {
          error(line, "Unexpected character.");
        }
        break;
    }
  }

  void scanNumber() {
    while (isDigit(peek())) {
      advance();
    }

    // Look for a fractional part.
    if (peek() == u'.' && isDigit(peekNext())) {
      // Consume the '.'
      advance();
      while (isDigit(peek()))
        advance();
    }

    UnicodeString text;
    source.extractBetween(start, current, text);
    Formattable result;
    UErrorCode status = U_ZERO_ERROR;
    numberFormatter->parse(text, result, status);
    if (result.getType() == Formattable::kDouble)
      addToken(TokenType::NUMBER, result.getDouble());
    else if (result.getType() == Formattable::kLong)
      addToken(TokenType::NUMBER, (double)result.getLong());
  }

  void scanIdentifier() {
    while (isAlphaNumeric(peek()))
      advance();
    UnicodeString text;
    source.extractBetween(start, current, text);
    auto it = kKeywords.find(text);
    if (it != kKeywords.end()) {
      addToken(it->second);
    } else {
      addToken(TokenType::IDENTIFIER);
    }
  }

  char16_t peekNext() {
    if (current + 1 >= source.length())
      return u'\u0000';
    else
      return source[current + 1];
  }

  bool isAlpha(char16_t c) {
    return (c >= u'a' && c <= u'z') || (c >= u'A' && c <= u'Z') || c == u'_';
  }

  bool isDigit(char16_t c) { return (c >= u'0' && c <= u'9'); }

  bool isAlphaNumeric(char16_t c) {
    return isAlpha(c) || isDigit(c) || c == u'_';
  }

  void scanString() {
    while (peek() != u'"' && !isAtEnd()) {
      if (peek() == u'\n') {
        line += 1;
      }
      advance();
    }

    if (isAtEnd())
      error(line, "Unterminated string.");
    else {
      advance();
      UnicodeString text;
      source.extractBetween(start + 1, current - 1, text);
      addToken(TokenType::STRING, text);
    }
  }

  void addToken(TokenType type, Value literal = {}) {
    UnicodeString text;
    source.extractBetween(start, current, text);
    tokens.emplace_back(type, std::move(text), std::move(literal), line);
  }
};

Scanner::Scanner(UnicodeString source)
    : inner{std::make_unique<ScannerInner>(std::move(source))} {}

std::vector<Token> Scanner::scanTokens() {
  inner->scanTokens();
  return std::move(inner->tokens);
}
Scanner::~Scanner() = default;

}  // namespace lox