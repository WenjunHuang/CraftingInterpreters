//
// Created by rick on 8/17/2022.
//

#pragma once

#include <unicode/unistr.h>
#include <memory>
#include <vector>
#include "token.h"

namespace lox {
class ScannerInner;

class Scanner {
 public:
  explicit Scanner(icu::UnicodeString source);
  ~Scanner();

  std::vector<Token> scanTokens();

 private:
  std::unique_ptr<ScannerInner> inner;
};
}  // namespace lox