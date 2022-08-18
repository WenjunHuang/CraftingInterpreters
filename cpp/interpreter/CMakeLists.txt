
add_executable(interpreter main.cc environment.cc environment.h interpreter.cc interpreter.h)
target_link_libraries(interpreter PUBLIC library ICU::uc ICU::dt ICU::in ICU::io
                      range-v3 range-v3-meta range-v3::meta range-v3-concepts)
