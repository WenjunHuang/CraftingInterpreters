use crate::chunk::Chunk;
use crate::chunk::OpCode::{OpConstant, OpReturn};
use crate::debug::disassemble_chunk;
use crate::vm::VM;

mod chunk;
mod common;
mod memory;
mod debug;
mod value;
mod vm;

fn main() {
    let mut chunk = Chunk::new();
    let constant = chunk.add_constant(1.2);
    chunk.write_opcode(OpConstant, 123);
    chunk.write_chunk(constant as u8, 123);
    chunk.write_opcode(OpReturn, 123);

    let mut vm = VM::new(chunk);
    vm.run();
    // disassemble_chunk(&chunk, "test chunk");
}
