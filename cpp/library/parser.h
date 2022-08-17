//
// Created by rick on 8/17/2022.
//

#pragma once
#include <memory>
#include <vector>
#include "statement.h"
#include "token.h"

namespace lox {
class ParserInner;

class Parser {
 public:
  explicit Parser(std::vector<Token> tokens);
  std::vector<StatementPtr> parse();

 private:
  std::unique_ptr<ParserInner> inner;
};

}  // namespace lox