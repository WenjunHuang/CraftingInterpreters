//
// Created by rick on 8/18/2022.
//

#pragma once
#include <unicode/unistr.h>
#include <exception>

namespace lox {
struct RuntimeError : public std::exception {
  int line;
  icu::UnicodeString message;
  RuntimeError(int line, icu::UnicodeString message)
      : line{line}, message{std::move(message)} {}
};
}  // namespace lox