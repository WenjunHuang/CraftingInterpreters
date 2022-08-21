//
// Created by rick on 8/19/2022.
//

#include <gtest/gtest.h>
#include <range/v3/all.hpp>
#include <range/v3/view/zip.hpp>
#include <vector>
#include "scanner.h"

using namespace lox;
using namespace ranges;

TEST(ScannerTest, ScanEmptySource) {
  Scanner scanner("");
  auto tokens = scanner.scanTokens();
  ASSERT_EQ(tokens.size(), 1);
}

TEST(ScannerTest, ScanKeywords) {
  std::vector<std::pair<std::string, TokenType>> keywords = {
      {"(", TokenType::LEFT_PAREN},
      {")", TokenType::RIGHT_PAREN},
      {"{", TokenType::LEFT_BRACE},
      {"}", TokenType::RIGHT_BRACE},
      {",", TokenType::COMMA},
      {".", TokenType::DOT},
      {"-", TokenType::MINUS},
      {"+", TokenType::PLUS},
      {";", TokenType::SEMICOLON},
      {"/", TokenType::SLASH},
      {"*", TokenType::STAR},
      {"!", TokenType::BANG},
      {"!=", TokenType::BANG_EQUAL},
      {"=", TokenType::EQUAL},
      {"==", TokenType::EQUAL_EQUAL},
      {">", TokenType::GREATER},
      {">=", TokenType::GREATER_EQUAL},
      {"<", TokenType::LESS},
      {"<=", TokenType::LESS_EQUAL},
      {"abcd", TokenType::IDENTIFIER},
      {"\"a string\"", TokenType::STRING},
      {"123.123", TokenType::NUMBER},
      {"and", TokenType::AND},
      {"class", TokenType::CLASS},
      {"else", TokenType::ELSE},
      {"false", TokenType::FALSE},
      {"fun", TokenType::FUN},
      {"for", TokenType::FOR},
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

  auto sources = keywords | views::keys | views::join(' ') | to<std::string>();
  auto tokenTypes = keywords | views::values | to<std::vector<TokenType>>();

  auto scanner = Scanner(sources.c_str());
  auto tokens = scanner.scanTokens();
  auto&& last = tokens[tokens.size() - 1];

  ASSERT_EQ(tokens.size(), keywords.size() + 1);
  ASSERT_EQ(last.tokenType, TokenType::EndOfFile);

  for (auto&& kv : views::zip(tokens, tokenTypes)) {
    auto&& [token, tokenType] = kv;
    ASSERT_EQ(tokenType, token.tokenType);
  }
}
