//
// Created by rick on 8/18/2022.
//

#pragma once

#include <unicode/unistr.h>
#include <memory>
#include <optional>
#include <unordered_map>
#include "token.h"
#include "value.h"

namespace std {
template <>
class hash<icu::UnicodeString> {
 public:
  size_t operator()(const icu::UnicodeString& str) const {
    return (size_t)str.hashCode();
  }
};
}  // namespace std

namespace lox {
class Environment {
 public:
  explicit Environment(
      std::optional<std::shared_ptr<Environment>> enclosing = std::nullopt);

  void define(const icu::UnicodeString& name, const Value& value);
  [[nodiscard]] const Value& get(const Token& name) const;
  void assign(const Token& name, const Value& value);

 private:
  std::optional<std::shared_ptr<Environment>> enclosing;
  std::unordered_map<icu::UnicodeString, Value> values;
};

extern std::shared_ptr<Environment> GLOBAL;
}  // namespace lox
