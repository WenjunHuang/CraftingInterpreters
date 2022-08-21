
set(SOURCES
    src/token_type.h
    src/token.h
    src/value.h
    src/scanner.cc
    src/scanner.h
    src/lox.h
    src/lox.cc
    src/lox_parser.cc
    src/lox_parser.h
    src/expression.h
    src/statement.h
    src/parse_error.h
    src/util.h)
set(LIBS ICU::uc ICU::dt ICU::in ICU::io range-v3 range-v3-meta range-v3::meta range-v3-concepts fmt::fmt)
add_library(library STATIC ${SOURCES})
target_include_directories(library PUBLIC ${CMAKE_CURRENT_LIST_DIR}/src)
target_link_libraries(library PUBLIC ${LIBS})

add_executable(library_test ${SOURCES} test/main.cc test/scanner_spec.cc)
target_include_directories(library_test PUBLIC ${CMAKE_CURRENT_LIST_DIR}/src)
target_link_libraries(library_test PRIVATE
                      ${LIBS}
                      GTest::gmock GTest::gtest GTest::gmock_main GTest::gtest_main)