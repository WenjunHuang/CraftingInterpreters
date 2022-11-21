use crate::chunk::Chunk;
use crate::chunk::OpCode::{OpAdd, OpConstant, OpDivide, OpMultiply, OpNegate, OpReturn, OpSubstract};

pub fn disassemble_chunk(chunk: &Chunk, name: &str) {
    println!("== {} ==", name);
    let mut offset = 0;
    while offset < chunk.count {
        offset = disassemble_instruction(chunk, offset);
    }
}

pub fn disassemble_instruction(chunk: &Chunk, offset: u32) -> u32 {
    print!("{:04} ", offset);
    if offset > 0 && chunk.lines[offset as usize] == chunk.lines[(offset - 1) as usize] {
        print!("   | ");
    } else {
        print!("{:4} ", chunk.lines[offset as usize]);
    }
    let instruction = chunk.code[offset as usize];
    if instruction == OpReturn as u8 {
        simple_instruction("OP_RETURN", offset)
    } else if instruction == OpConstant as u8 {
        constant_instruction("OP_CONSTANT", chunk, offset)
    } else if instruction == OpNegate as u8 {
        simple_instruction("OP_NEGATE", offset)
    } else if instruction == OpAdd as u8 {
        simple_instruction("OP_ADD", offset)
    } else if instruction == OpSubstract as u8 {
        simple_instruction("OP_SUBSTRACT", offset)
    } else if instruction == OpMultiply as u8 {
        simple_instruction("OP_MULTIPLY", offset)
    } else if instruction == OpDivide as u8 {
        simple_instruction("OP_DIVIDE", offset)
    } else {
        println!("Unknown opcode {}", instruction);
        offset + 1
    }
}

fn simple_instruction(name: &str, offset: u32) -> u32 {
    println!("{}", name);
    offset + 1
}

fn constant_instruction(name: &str, chunk: &Chunk, offset: u32) -> u32 {
    let constant = chunk.code[(offset + 1) as usize];
    println!("{:16} {:4} '{}'", name, constant, chunk.constants.values[constant as usize]);
    offset + 2
}