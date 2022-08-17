//
// Created by rick on 8/17/2022.
//

#include "parser.h"
#include <unicode/unistr.h>
#include "lox.h"
#include "token_type.h"

using namespace icu;
template <class... Ts>
struct overloaded : Ts... {
  using Ts::operator()...;
};
template <class... Ts>
overloaded(Ts...) -> overloaded<Ts...>;

namespace lox {
struct ParserInner {
  std::vector<Token> tokens;
  int current = 0;

  std::vector<Statement> parse() {
    std::vector<Statement> statements{};
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

  std::optional<Statement> declaration() {
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

  Statement varDeclaration() {
    auto name = consume(TokenType::IDENTIFIER, "Expect variable name.");
    if (matching(TokenType::EQUAL)) {
      auto initializer = expression();
      consume(TokenType::SEMICOLON, "Expect ';' after variable declaration.");
      return Var{name, std::make_unique<Expression>(std::move(initializer))};
    } else {
      consume(TokenType::SEMICOLON, "Expect '=' after variable name.");
      return Var{name, std::nullopt};
    }
  }

  Expression expression() { return assignment(); }

  Expression logicalOr() {
    auto expr = logicalAnd();
    while (matching(TokenType::OR)) {
      auto& opt = previous();
      auto right = logicalAnd();
      expr =
          Expression(Logical{std::make_unique<Expression>(std::move(expr)), opt,
                             std::make_unique<Expression>(std::move(right))});
    }
    return expr;
  }

  Expression logicalAnd() {
    auto expr = equality();
    while (matching(TokenType::AND)) {
      auto& opt = previous();
      auto right = equality();
      expr = Logical{std::make_unique<Expression>(std::move(expr)), opt,
                     std::make_unique<Expression>(std::move(right))};
    }
    return expr;
  }

  Expression equality() {
    auto expr = comparison();
    while (matching(TokenType::BANG_EQUAL, TokenType::EQUAL_EQUAL)) {
      auto& opt = previous();
      auto right = comparison();
      expr = Binary{std::make_unique<Expression>(std::move(expr)), opt,
                    std::make_unique<Expression>(std::move(right))};
    }
    return expr;
  }

  Expression comparison() {
    auto expr = term();
    while (matching(TokenType::GREATER, TokenType::GREATER_EQUAL,
                    TokenType::LESS, TokenType::LESS_EQUAL)) {
      auto& opt = previous();
      auto right = term();
      expr = Binary{std::make_unique<Expression>(std::move(expr)), opt,
                    std::make_unique<Expression>(std::move(right))};
    }
    return expr;
  }

  Expression term() {
    auto expr = factory();
    while (matching(TokenType::PLUS, TokenType::MINUS)) {
      auto& opt = previous();
      auto right = factory();
      expr = Binary{std::make_unique<Expression>(std::move(expr)), opt,
                    std::make_unique<Expression>(std::move(right))};
    }
    return expr;
  }

  Expression factory() {
    auto expr = unary();
    while (matching(TokenType::SLASH, TokenType::STAR)) {
      auto& opt = previous();
      auto right = unary();
      expr = Binary{std::make_unique<Expression>(std::move(expr)), opt,
                    std::make_unique<Expression>(std::move(right))};
    }
    return expr;
  }

  Expression unary() {
    if (matching(TokenType::BANG, TokenType::MINUS)) {
      auto& opt = previous();
      auto right = unary();
      return Unary{opt, std::make_unique<Expression>(std::move(right))};
    } else {
      return primary();
    }
  }
  Expression primary() {
    if (matching(TokenType::FALSE))
      return Literal{false};
    else if (matching(TokenType::TRUE))
      return Literal{true};
    else if (matching(TokenType::NIL))
      return Literal{std::nullopt};
    else if (matching(TokenType::IDENTIFIER))
      return Variable{previous()};
    else if (matching(TokenType::NUMBER, TokenType::STRING))
      return Literal{previous().literal};
    else if (matching(TokenType::LEFT_PAREN)) {
      auto expr = expression();
      consume(TokenType::RIGHT_PAREN, "Expect ')' after expression.");
      return Grouping{std::make_unique<Expression>(std::move(expr))};
    } else
      throw error(peek(), "Expect expression.");
  }

  Expression assignment() {
    auto expr = logicalOr();
    if (matching(TokenType::EQUAL)) {
      return std::visit(
          overloaded{[this](auto&& arg) -> Expression {
                       lox::error(previous(), "Invalid assignment target");
                       return std::move(arg);
                     },
                     [this](Variable&& arg) -> Expression {
                       auto value = assignment();
                       return Assign{
                           std::move(arg.name),
                           std::make_unique<Expression>(assignment())};
                     }},
          std::move(expr));
    } else {
      return expr;
    }
  }

  Statement statement() {
    if (matching(TokenType::PRINT))
      return printStatement();
    else if (matching(TokenType::IF))
      return ifStatement();
    else if (matching(TokenType::LEFT_BRACE))
      return Block{block()};
    else
      return expressionStatement();
  }

  Statement expressionStatement() {
    auto expr = expression();
    consume(TokenType::SEMICOLON, "Expect ';' after expression.");
    return Expr{std::move(expr)};
  }

  Statement printStatement() {
    auto expr = expression();
    consume(TokenType::SEMICOLON, "Expect ';' after value.");
    return Print{std::move(expr)};
  }

  Statement ifStatement() {
    consume(TokenType::LEFT_PAREN, "Expect '(' after 'if'.");
    auto condition = expression();
    consume(TokenType::RIGHT_PAREN, "Expect ')' after if condition");

    auto thenBranch = statement();
    auto elseBranch = std::make_optional<StatementPtr>();
    if (matching(TokenType::ELSE))
      elseBranch.emplace(std::make_unique<Statement>(statement()));
    return If{std::move(condition),
              std::make_unique<Statement>(std::move(thenBranch)),
              std::move(elseBranch)};
  }

  std::vector<Statement> block() {
    std::vector<Statement> statements;
    while (!check(TokenType::RIGHT_BRACE) && !isAtEnd()) {
      auto stmt = declaration();
      if (stmt.has_value())
        statements.push_back(std::exchange(stmt, std::nullopt).value());
    }
    consume(TokenType::RIGHT_BRACE, "Expect '}' after block.");
    return statements;
  }

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

std::vector<Statement> Parser::parse() {
  return inner->parse();
}

}  // namespace lox
