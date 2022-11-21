use std::collections::VecDeque;
use crate::chunk::{Chunk, OpCode};
use crate::debug::{disassemble_chunk, disassemble_instruction};
use crate::value::{print_value, Value};
use crate::vm::InterpretResult::InterpretOk;

pub struct VM {
    chunk: Chunk,
    ip: u32,
    stack: VecDeque<Value>,
}

pub enum InterpretResult {
    InterpretOk,
    InterpretCompileError,
    InterpretRuntimeError,
}

impl VM {
    pub fn new(chunk: Chunk) -> VM {
        VM {
            chunk,
            stack: VecDeque::new(),
            ip: 0,
        }
    }

    fn read_byte(self: &mut Self) -> u8 {
        let byte = self.chunk.code[self.ip as usize];
        self.ip += 1;
        byte
    }

    fn push_value(self: &mut Self, value: Value) {
        self.stack.push_back(value);
    }

    fn pop_value(self: &mut Self) -> Option<Value> {
        self.stack.pop_back()
    }

    pub fn run(self: &mut Self) -> InterpretResult {
        loop {
            if cfg!(feature = "DEBUG_TRACE_EXECUTION") {
                print!("          ");
                for slot in &self.stack {
                    print!("[ ");
                    print_value(slot);
                    print!(" ]");
                }
                println!();
                disassemble_instruction(&self.chunk, self.ip);
            }


            let instruction = self.read_byte();
            if instruction == OpCode::OpReturn as u8 {
                match self.pop_value() {
                    Some(ref v) => {
                        print_value(v);
                        println!();
                        return InterpretOk;
                    }
                    None => {}
                }
                return InterpretOk;
            } else if instruction == OpCode::OpConstant as u8 {
                let idx = self.read_byte();
                let constant = self.chunk.constants.values[idx as usize];
                self.push_value(constant);
            } else if instruction == OpCode::OpNegate as u8 {
                match self.pop_value() {
                    Some(v) => self.push_value(-v),
                    _ => {}
                }
            } else if instruction == OpCode::OpAdd as u8 {
                let b = self.pop_value();
                let a = self.pop_value();
                match (a, b) {
                    (Some(a), Some(b)) => self.push_value(a + b),
                    _ => {}
                }
            } else if instruction == OpCode::OpSubstract as u8 {
                let b = self.pop_value();
                let a = self.pop_value();
                match (a, b) {
                    (Some(a), Some(b)) => self.push_value(a - b),
                    _ => {}
                }
            } else if instruction == OpCode::OpMultiply as u8 {
                let b = self.pop_value();
                let a = self.pop_value();
                match (a, b) {
                    (Some(a), Some(b)) => self.push_value(a * b),
                    _ => {}
                }
            } else if instruction == OpCode::OpDivide as u8 {
                let b = self.pop_value();
                let a = self.pop_value();
                match (a, b) {
                    (Some(a), Some(b)) => self.push_value(a / b),
                    _ => {}
                }
            } else {
                println!("Unknown opcode {}", instruction);
            }
        }
    }

    pub fn interpret(chunk: Chunk) -> InterpretResult {
        let mut vm = VM::new(chunk);
        vm.run()
    }
}