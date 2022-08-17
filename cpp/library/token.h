//
// Created by rick on 8/17/2022.
//

#pragma once

#include <unicode/unistr.h>
#include "token_type.h"
#include "value.h"

namespace lox {
struct Token {
  TokenType tokenType;
  icu::UnicodeString lexeme;
  Value literal;
  int line;
};
}  // namespace lox