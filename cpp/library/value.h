//
// Created by rick on 8/17/2022.
//

#pragma once

#include <optional>
#include <unicode/unistr.h>
#include <variant>

namespace lox {
using NumericValue = double;
using StringValue = icu::UnicodeString;
using BooleanValue = bool;
using NoValue = std::nullopt_t;

using Value = std::variant<NoValue, NumericValue, StringValue, BooleanValue>;

}  // namespace lox