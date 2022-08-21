cmake_minimum_required(VERSION 3.23)
project(CraftingInterpreters)

set(CMAKE_CXX_STANDARD 20)

find_package(ICU REQUIRED COMPONENTS uc dt in io)
find_package(range-v3 CONFIG REQUIRED)
find_package(fmt CONFIG REQUIRED)
find_package(GTest CONFIG REQUIRED)

add_subdirectory(library)
add_subdirectory(interpreter)