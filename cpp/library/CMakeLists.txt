
add_library(library
            token_type.h
            token.h value.h
            scanner.cc
            scanner.h
            lox.h lox.cc parser.cc parser.h expression.h statement.h parse_error.h)
target_link_libraries(library PUBLIC ICU::dt ICU::in ICU::io)
