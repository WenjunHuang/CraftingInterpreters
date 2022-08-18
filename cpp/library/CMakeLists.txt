
add_library(library STATIC
            token_type.h
            token.h value.h
            scanner.cc
            scanner.h
            lox.h lox.cc
            lox_parser.cc
            lox_parser.h
            expression.h
            statement.h
            parse_error.h
            util.h)
target_include_directories(library PUBLIC ${CMAKE_CURRENT_LIST_DIR})
target_link_libraries(library PUBLIC ICU::uc ICU::dt ICU::in ICU::io)
