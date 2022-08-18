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
  icu::UnicodeString buffer;

  lox::Interpreter interpreter;

  while (true) {
    std::cout << "> " << std::flush;
    std::getline(std::cin, line);

    std::optional<icu::UnicodeString> source{};

    if (!line.empty()) {
      icu::UnicodeString sourceLine{line.c_str()};
      auto& trimmed = sourceLine.trim();
      if (trimmed.endsWith("{")) {
        if (buffer.isEmpty()) {
          buffer = std::move(sourceLine);
        } else {
          buffer.append(sourceLine);
        }
      } else if (trimmed.endsWith("}")) {
        buffer.append(sourceLine);
        source.emplace(buffer);
        buffer.remove();
      } else {
        if (!buffer.isEmpty()) {
          buffer.append(sourceLine);
        } else {
          source.emplace(std::move(sourceLine));
        }
      }
    }

    if (source.has_value()) {
      auto tokens = lox::Scanner(std::exchange(source, std::nullopt).value())
                        .scanTokens();
      auto stmts = lox::Parser(tokens).parse();
      interpreter.interpret(stmts);
    }
  }
}