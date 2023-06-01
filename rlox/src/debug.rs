use crate::chunk::{Chunk, OpCode};
use crate::chunk::OpCode::{OpConstant, OpReturn, OpNegate};

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

    let code = chunk.code[offset as usize];
    match OpCode::try_from(code) {
        Ok(OpConstant) => constant_instruction("OP_CONSTANT", chunk, offset),
        Ok(OpReturn) => simple_instruction("OP_RETURN", offset),
        Ok(OpNegate) => simple_instruction("OP_NEGATE", offset),
        Ok(OpCode::OpAdd) => simple_instruction("OP_ADD", offset),
        Ok(OpCode::OpSubtract) => simple_instruction("OP_SUBTRACT", offset),
        Ok(OpCode::OpMultiply) => simple_instruction("OP_MULTIPLY", offset),
        Ok(OpCode::OpDivide) => simple_instruction("OP_DIVIDE", offset),
        Err(_) => {
            println!("Unknown opcode {}", code);
            offset + 1
        }
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