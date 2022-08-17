cmake_minimum_required(VERSION 3.23)
project(CraftingInterpreters)

set(CMAKE_CXX_STANDARD 20)
#if (MSVC)
#  add_compile_options("/std:c++latest" "/await")
#endif ()
find_package(ICU REQUIRED COMPONENTS uc dt in io)
add_subdirectory(library)

add_executable(main main.cpp)
target_link_libraries(main PUBLIC library)