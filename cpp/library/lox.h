//
// Created by rick on 8/17/2022.
//

#pragma once

#include <unicode/unistr.h>
#include "token.h"
#include "parse_error.h"
#include "runtime_error.h"

namespace lox {

extern bool hasRuntimeError;
extern bool hadError;

void report(int line,
            const icu::UnicodeString& where,
            const icu::UnicodeString& message);

inline void error(int line, const icu::UnicodeString& message) {
  report(line, "", message);
}

void error(const Token& token,const icu::UnicodeString& message);

void runtimeError(const RuntimeError& error);
}  // namespace lox
