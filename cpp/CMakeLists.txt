cmake_minimum_required(VERSION 3.23)
project(CraftingInterpreters)

set(CMAKE_CXX_STANDARD 20)

find_package(ICU REQUIRED COMPONENTS uc dt in io)
add_subdirectory(library)
add_subdirectory(interpreter)