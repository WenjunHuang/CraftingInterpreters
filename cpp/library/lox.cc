//
// Created by rick on 8/17/2022.
//
#include "lox.h"
#include <unicode/msgfmt.h>
#include <unicode/unistr.h>
#include <unicode/ustream.h>
#include <iostream>

using namespace icu;

void lox::report(int line,
                 const icu::UnicodeString& where,
                 const icu::UnicodeString& message) {
  using namespace icu;
  UnicodeString result;
  UErrorCode success = U_ZERO_ERROR;
  Formattable args[] = {Formattable(line), Formattable(where),
                        Formattable(message)};
  std::cout << MessageFormat::format("[line {0}] {1}: {2}", args, sizeof(args),
                                     result, success)
            << std::endl;
}

void lox::error(const lox::Token& token, const icu::UnicodeString& message) {
  if (token.tokenType == TokenType::EndOfFile)
    report(token.line, "", message);
  else {
    UnicodeString result;
    UErrorCode success = U_ZERO_ERROR;
    Formattable args[] = {Formattable(token.lexeme)};
    report(
        token.line,
        MessageFormat::format(" at '{0}'", args, sizeof(args), result, success),
        message);
  }
}
