//
// Created by rick on 8/17/2022.
//

#pragma once

#include <unicode/unistr.h>
#include <optional>
#include <variant>

namespace lox {
using NumericValue = double;
using StringValue = icu::UnicodeString;
using BooleanValue = bool;
constexpr std::monostate NoValue{};

using Value =
    std::variant<std::monostate, NumericValue, StringValue, BooleanValue>;

}  // namespace lox