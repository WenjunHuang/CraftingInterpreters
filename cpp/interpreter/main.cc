//
// Created by rick on 8/18/2022.
//

#include <unicode/ustream.h>
#include <iostream>
#include "interpreter.h"
#include "lox_parser.h"
#include "scanner.h"

int main() {
  std::string line;
  lox::Interpreter interpreter;

  while (true) {
    std::cout << "> " << std::flush;
    std::getline(std::cin, line);
    if (!line.empty()) {
      auto tokens = lox::Scanner(line.c_str()).scanTokens();
      auto stmts = lox::Parser(tokens).parse();
      interpreter.interpret(stmts);
    }
  }
}