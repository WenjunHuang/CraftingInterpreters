use std::fmt::{Display, Formatter};

use num_enum::{IntoPrimitive, TryFromPrimitive};

use crate::chunk::OpCode::{OpAdd, OpConstant, OpDivide, OpMultiply, OpNegate, OpReturn, OpSubtract};
use crate::vm::memory::grow_capacity;
use crate::vm::value::{Value, ValueArray};

#[repr(u8)]
#[derive(Copy, Clone, TryFromPrimitive, IntoPrimitive)]
pub enum OpCode {
    OpConstant,
    OpNil,
    OpTrue,
    OpFalse,
    OpReturn,
    OpNegate,
    OpAdd,
    OpSubtract,
    OpMultiply,
    OpDivide,
    OpNot,
    OpEqual,
    OpGreater,
    OpLess,
    OpPrint,
    OpPop,
    OpDefineGlobal,
    OpGetGlobal,
    OpSetGlobal,
    OpGetLocal,
    OpSetLocal,
    OpJumpIfFalse,
    OpJump,
    OpLoop,
    OpCall,
    OpClosure,
    OpGetUpValue,
    OpSetUpValue,
    OpCloseUpValue,
}

#[derive(Debug)]
pub struct Chunk {
    pub code: Vec<u8>,
    pub lines: Vec<i32>,
    pub constants: ValueArray,
    pub capacity: usize,
    pub count: usize,
}

impl Chunk {
    pub fn new() -> Chunk {
        Chunk {
            code: Vec::new(),
            lines: Vec::new(),
            constants: ValueArray::new(),
            capacity: 0,
            count: 0,
        }
    }

    pub fn write_opcode(&mut self, op: OpCode, line: i32) {
        self.write_chunk(op as u8, line)
    }

    pub fn write_chunk(&mut self, byte: u8, line: i32) {
        if self.capacity < self.count + 1 {
            let old_capacity = self.capacity;
            self.capacity = grow_capacity(old_capacity);
            self.code.resize(self.capacity as usize, 0);
            self.lines.resize(self.capacity as usize, 0);
        }

        self.code[self.count] = byte;
        self.lines[self.count] = line;
        self.count += 1;
    }

    pub fn add_constant(&mut self, value: Value) -> usize {
        self.constants.write_value(value)
    }

    pub fn get_constant(&self, index: usize) -> Option<&Value> {
        self.constants.values.get(index)
    }
}