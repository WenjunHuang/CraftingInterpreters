//
// Created by rick on 8/18/2022.
//

#include "interpreter.h"
#include <unicode/ustream.h>
#include <algorithm>
#include <functional>
#include <iostream>
#include <variant>
#include "lox.h"
#include "runtime_error.h"
#include "util.h"
namespace {
using namespace lox;

template <typename T>
T checkNumberOperands(const Token& operatorToken,
                      const Value& left,
                      const Value& right,
                      T (*func)(double, double)) {
  auto lv = std::get_if<NumericValue>(&left);
  auto rv = std::get_if<NumericValue>(&right);
  if (!lv && !rv)
    throw RuntimeError(operatorToken.line, "Operands must be numbers.");

  return func(*lv, *rv);
}

template <typename T>
T checkNumberOperand(const Token& operatorToken,
                     const Value& value,
                     T (*func)(double)) {
  auto v = std::get_if<NumericValue>(&value);
  if (!v)
    throw RuntimeError(operatorToken.line, "Operand must be a number.");
  return func(*v);
}

bool isTruthy(const Value& value) {
  return std::visit(
      [](const auto& v) -> bool {
        using T = std::decay_t<decltype(v)>;
        if constexpr (std::is_same_v<T, std::monostate>) {
          return false;
        } else if constexpr (std::is_same_v<T, BooleanValue>) {
          return v;
        } else {
          return true;
        }
      },
      value);
}

struct ExpressionVisitor {
  std::shared_ptr<Environment> environment = GLOBAL;

  Value visitExpr(const Literal& literal) { return literal.value; }

  Value visitExpr(const Logical& logical) {
    auto left = evaluate(*logical.left);
    if (logical.opt.tokenType == TokenType::OR) {
      if (isTruthy(left))
        return left;
      else
        return evaluate(*logical.right);
    } else if (logical.opt.tokenType == TokenType::AND) {
      if (!isTruthy(left))
        return left;
      else
        return evaluate(*logical.right);
    }
    runtimeError({logical.opt.line, "Invalid logical operator."});
    return NoValue;
  }

  Value visitExpr(const Unary& unary) {
    auto right = evaluate(*unary.right);
    if (unary.opt.tokenType == TokenType::MINUS) {
      return checkNumberOperand<NumericValue>(unary.opt, right,
                                              [](auto v) { return -v; });
    } else if (unary.opt.tokenType == TokenType::BANG) {
      return !isTruthy(right);
    }
    return right;
  }

  Value visitExpr(const Grouping& grouping) {
    return evaluate(*grouping.expression);
  }

  Value visitExpr(const Variable& variable) {
    return environment->get(variable.name);
  }

  Value visitExpr(const Assign& assign) {
    auto value = evaluate(*assign.value);
    environment->assign(assign.name, value);
    return value;
  }

  Value visitExpr(const Binary& binary) {
    auto left = evaluate(*binary.left.get());
    auto right = evaluate(*binary.right.get());

    switch (binary.opt.tokenType) {
      case TokenType::MINUS: {
        auto v = checkNumberOperands<double>(
            binary.opt, left, right,
            [](double left, double right) { return left - right; });
        return v;
      }
      case TokenType::PLUS: {
        auto ln = std::get_if<NumericValue>(&left);
        auto rn = std::get_if<NumericValue>(&right);
        if (ln && rn)
          return *ln + *rn;
        else {
          auto ls = std::get_if<StringValue>(&left);
          auto rs = std::get_if<StringValue>(&right);
          if (ls && rs)
            return *ls + *rs;
          else
            return NoValue;
        }
      }
      case TokenType::SLASH:
        return checkNumberOperands<NumericValue>(
            binary.opt, left, right,
            [](NumericValue left, NumericValue right) { return left / right; });
      case TokenType::STAR:
        return checkNumberOperands<NumericValue>(
            binary.opt, left, right,
            [](NumericValue left, NumericValue right) { return left * right; });
      case TokenType::GREATER:
        return checkNumberOperands<BooleanValue>(
            binary.opt, left, right,
            [](double left, double right) { return left > right; });
      case TokenType::GREATER_EQUAL:
        return checkNumberOperands<BooleanValue>(
            binary.opt, left, right,
            [](double left, double right) { return left >= right; });
      case TokenType::LESS:
        return checkNumberOperands<BooleanValue>(
            binary.opt, left, right,
            [](double left, double right) { return left < right; });
      case TokenType::LESS_EQUAL:
        return checkNumberOperands<BooleanValue>(
            binary.opt, left, right,
            [](double left, double right) { return left <= right; });
      case TokenType::BANG_EQUAL:
        return left != right;
      case TokenType::EQUAL_EQUAL:
        return left == right;
      default:
        return NoValue;
    }
  }

  Value evaluate(const Expression& expression) {
    return std::visit([this](auto&& v) -> Value { return visitExpr(v); },
                      expression);
  }

  void visitStmt(const Expr& expr) { evaluate(expr.expr); }
  void visitStmt(const Print& print) {
    auto value = evaluate(print.expr);
    std::visit(
        overloaded{
            [](const NumericValue& v) { std::cout << v << std::endl; },
            [](const StringValue& v) { std::cout << v << std::endl; },
            [](const BooleanValue& v) {
              std::cout << std::boolalpha << v << std::endl;
            },
            [](const std::monostate&) { std::cout << "nil" << std::endl; }},
        value);
  }

  void visitStmt(const If& ifStmt) {
    auto condition = evaluate(ifStmt.condition);
    if (isTruthy(condition)) {
      evaluate(*ifStmt.thenBranch);
    } else if (ifStmt.elseBranch) {
      evaluate(*ifStmt.elseBranch.value());
    }
  }

  void visitStmt(const Var& var) {
    auto value = var.initializer ? evaluate(*var.initializer) : NoValue;
    environment->define(var.name.lexeme, value);
  }

  void visitStmt(const While& stmt) {}
  void visitStmt(const Block& stmt) {
    auto previous = environment;
    try {
      environment = std::make_shared<Environment>(environment);
      for (const auto& statement : stmt.statements) {
        evaluate(statement);
      }
    } catch (...) {
      environment = previous;
      throw;
    }
  }
  void evaluate(const Statement& statement) {
    std::visit([this](auto&& s) { visitStmt(s); }, statement);
  }
} visitor;
}  // namespace

namespace lox {
void Interpreter::interpret(const std::vector<Statement>& statements) {
  try {
    std::for_each(
        statements.begin(), statements.end(),
        [this](const Statement& statement) { visitor.evaluate(statement); });
  } catch (RuntimeError& error) {
    runtimeError(error);
  }
}

}  // namespace lox