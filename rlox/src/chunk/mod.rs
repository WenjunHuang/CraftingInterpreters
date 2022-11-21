use crate::memory::grow_capacity;
use crate::value::{Value, ValueArray};

#[repr(u8)]
pub enum OpCode {
    OpConstant,
    OpReturn,
}

pub struct Chunk {
    pub code: Vec<u8>,
    pub lines: Vec<i32>,
    pub constants: ValueArray,
    pub capacity: u32,
    pub count: u32,
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

        self.code[self.count as usize] = byte;
        self.lines[self.count as usize] = line;
        self.count += 1;
    }

    pub fn add_constant(&mut self, value: Value) -> u32 {
        self.constants.write_value(value);
        self.constants.count - 1
    }
}