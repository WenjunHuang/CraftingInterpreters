//
// Created by rick on 8/18/2022.
//

#pragma once
#include <memory>
#include <vector>
#include "environment.h"
#include "statement.h"

namespace lox {
class Interpreter {
 public:
  void interpret(const std::vector<Statement>& statements);
};

}  // namespace lox