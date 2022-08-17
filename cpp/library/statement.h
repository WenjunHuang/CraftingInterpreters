//
// Created by rick on 8/17/2022.
//

#pragma once
#include <memory>
#include <optional>
#include <variant>
#include <vector>
#include "expression.h"

namespace lox {
class Expr;
class Print;
class If;
class Var;
class While;
class Block;
using Statement = std::variant<Expr, Print, If, Var, While, Block>;

using StatementPtr = std::unique_ptr<Statement>;

struct Expr {
  ExpressionPtr expr;
};

struct Print {
  ExpressionPtr expr;
};

struct If {
  ExpressionPtr condition;
  StatementPtr thenBranch;
  std::optional<StatementPtr> elseBranch;
};

struct Var {
  Token name;
  std::optional<ExpressionPtr> initializer;
};

struct While {
  ExpressionPtr condition;
  StatementPtr body;
};

struct Block {
  std::vector<StatementPtr> statements;
};

}  // namespace lox
