//
// Created by rick on 8/17/2022.
//

#pragma once
#include <memory>
#include "token.h"
#include "value.h"

namespace lox {
class Logical;
class Unary;
class Assign;
class Binary;
class Grouping;
class Variable;

struct Literal {
  Value value;
};

using Expression = std::variant<std::nullopt_t,
                                Literal,
                                Logical,
                                Unary,
                                Assign,
                                Binary,
                                Grouping,
                                Variable>;

using ExpressionPtr = std::unique_ptr<Expression>;

struct Unary {
  Token opt;
  ExpressionPtr right;
};

struct Assign {
  Token name;
  ExpressionPtr value;
};

struct Logical {
  ExpressionPtr left;
  Token opt;
  ExpressionPtr right;
};

struct Binary {
  ExpressionPtr left;
  Token opt;
  ExpressionPtr right;
};

struct Grouping {
  ExpressionPtr expression;
};

struct Variable {
  Token name;
};
}  // namespace lox