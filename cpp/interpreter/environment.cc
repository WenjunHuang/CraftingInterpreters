//
// Created by rick on 8/18/2022.
//

#include "environment.h"
#include <unicode/unistr.h>
#include "runtime_error.h"

namespace lox {
std::shared_ptr<Environment> GLOBAL = std::make_shared<Environment>();

Environment::Environment(
    std::optional<std::shared_ptr<lox::Environment>> enclosing)
    : enclosing{std::move(enclosing)} {}

void Environment::define(const icu::UnicodeString& name, const Value& value) {
  values.emplace(name, value);
}

const Value& Environment::get(const Token& name) const {
  auto it = values.find(name.lexeme);
  if (it != values.end()) {
    return it->second;
  }

  if (enclosing.has_value()) {
    return enclosing.value()->get(name);
  }
  throw RuntimeError(name.line, "Undefined variable '" + name.lexeme + "'.");
}

void Environment::assign(const Token& name, const Value& value) {
  if (values.find(name.lexeme) != values.end()) {
    values[name.lexeme] = value;
    return;
  }
  if (enclosing.has_value()) {
    enclosing.value()->assign(name, value);
    return;
  }
  throw RuntimeError(name.line, "Undefined variable '" + name.lexeme + "'.");
}
}  // namespace lox