use num_enum::TryFromPrimitiveError;
use crate::chunk::{Chunk, OpCode};
use crate::chunk::OpCode::{OpConstant, OpReturn, OpNegate};

pub fn disassemble_chunk(chunk: &Chunk, name: &str) {
    println!("== {} ==", name);
    let mut offset: usize = 0;
    while offset < chunk.count {
        offset = disassemble_instruction(chunk, offset);
    }
}

pub fn disassemble_instruction(chunk: &Chunk, offset: usize) -> usize {
    print!("{:04} ", offset);
    if offset > 0 && chunk.lines[offset] == chunk.lines[offset - 1] {
        print!("   | ");
    } else {
        print!("{:4} ", chunk.lines[offset]);
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
        Ok(OpCode::OpNil) => simple_instruction("OP_NIL", offset),
        Ok(OpCode::OpTrue) => simple_instruction("OP_TRUE", offset),
        Ok(OpCode::OpFalse) => simple_instruction("OP_FALSE", offset),
        Ok(OpCode::OpNot) => simple_instruction("OP_NOT", offset),
        Ok(OpCode::OpEqual) => simple_instruction("OP_EQUAL", offset),
        Ok(OpCode::OpGreater) => simple_instruction("OP_GREATER", offset),
        Ok(OpCode::OpLess) => simple_instruction("OP_LESS", offset),
        Ok(OpCode::OpPrint) => simple_instruction("OP_PRINT", offset),
        Ok(OpCode::OpPop) => simple_instruction("OP_POP", offset),
        Ok(OpCode::OpDefineGlobal) => constant_instruction("OP_DEFINE_GLOBAL", chunk, offset),
        Ok(OpCode::OpGetGlobal) => constant_instruction("OP_GET_GLOBAL", chunk, offset),
        Ok(OpCode::OpSetGlobal) => constant_instruction("OP_SET_GLOBAL", chunk, offset),
        Ok(OpCode::OpGetLocal) => byte_instruction("OP_GET_LOCAL", chunk, offset),
        Ok(OpCode::OpSetLocal) => byte_instruction("OP_SET_LOCAL", chunk, offset),
        Ok(OpCode::OpJumpIfFalse) => jump_instruction("OP_JUMP_IF_ELSE", 1, chunk, offset),
        Ok(OpCode::OpJump) => jump_instruction("OP_JUMP", 1, chunk, offset),
        Ok(OpCode::OpLoop) => jump_instruction("OP_LOOP", -1, chunk, offset),
        Ok(OpCode::OpCall) => byte_instruction("OP_CALL", chunk, offset),
        Ok(OpCode::OpClosure) => {
            let mut offset = offset + 1;
            let constant = chunk.code[offset];
            println!("{:16} {:4} {}", "OP_CLOSURE", constant, chunk.constants.values[constant as usize]);
            offset += 1;

            offset
        }
        Err(_) => {
            println!("Unknown opcode {}", code);
            offset + 1
        }
        Ok(OpCode::OpGetUpvalue) => {0}
        Ok(OpCode::OpSetUpvalue) => {0}
    }
}

fn jump_instruction(name: &str, sign: i32, chunk: &Chunk, offset: usize) -> usize {
    let jump = ((chunk.code[(offset + 1) as usize] as i32) << 8) | chunk.code[(offset + 2) as usize] as i32;
    println!("{:16} {:4} -> {}", name, offset, offset as i32 + 3 + sign * jump);
    offset + 3
}

fn byte_instruction(name: &str, chunk: &Chunk, offset: usize) -> usize {
    let slot = chunk.code[(offset + 1) as usize];
    println!("{:16} {:4} ", name, slot);
    offset + 2
}

fn simple_instruction(name: &str, offset: usize) -> usize {
    println!("{}", name);
    offset + 1
}

fn constant_instruction(name: &str, chunk: &Chunk, offset: usize) -> usize {
    let constant = chunk.code[(offset + 1) as usize];
    println!("{:16} {:4} '{}'", name, constant, chunk.constants.values[constant as usize]);
    offset + 2
}