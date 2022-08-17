//
// Created by rick on 8/17/2022.
//

#include "parser.h"
#include <unicode/unistr.h>
#include "lox.h"
#include "token_type.h"

using namespace icu;

namespace lox {
struct ParserInner {
  std::vector<Token> tokens;
  int current = 0;

  std::vector<StatementPtr> parse() {
    std::vector<StatementPtr> statements{};
    while (!isAtEnd()) {
      auto stmt = declaration();
      if (stmt.has_value())
        statements.push_back(std::exchange(stmt, std::nullopt).value());
    }
    return statements;
  }

  bool isAtEnd() { return peek().tokenType == TokenType::EndOfFile; }

  const Token& peek() { return tokens[current]; }

  template <typename T, typename... TT>
  bool matching(T type, TT... types) {
    if (check(type)) {
      advance();
      return true;
    } else {
      if constexpr (sizeof...(TT) == 0)
        return false;
      else {
        return matching(types...);
      }
    }
  }

  bool check(TokenType tokenType) {
    if (isAtEnd())
      return false;
    return peek().tokenType == tokenType;
  }

  const Token& advance() {
    if (!isAtEnd())
      current += 1;
    return previous();
  }

  const Token& previous() { return tokens[current - 1]; }

  std::optional<StatementPtr> declaration() {
    try {
      if (matching(TokenType::VAR)) {
        return varDeclaration();
      } else
        return statement();
    } catch (ParseError& e) {
      // synchronize()
      return std::nullopt;
    }
  }

  StatementPtr varDeclaration() {
    auto name = consume(TokenType::IDENTIFIER, "Expect variable name.");
    if (matching(TokenType::EQUAL)) {
      auto initializer = expression();
      consume(TokenType::SEMICOLON, "Expect ';' after variable declaration.");
      return std::make_unique<Statement>(Var{name, std::move(initializer)});
    }

    return nullptr;
  }

  ExpressionPtr expression() { return nullptr; }

  StatementPtr statement() { return nullptr; }

  const Token& consume(TokenType tokenType, const UnicodeString& message) {
    if (!check(tokenType))
      throw error(peek(), message);
    else
      return advance();
  }

  ParseError error(const Token& token, const UnicodeString& message) {
    lox::error(token, message);
    return {};
  }
};

Parser::Parser(std::vector<Token> tokens)
    : inner{std::make_unique<ParserInner>(std::move(tokens))} {}

std::vector<StatementPtr> Parser::parse() {
  return inner->parse();
}

}  // namespace lox
