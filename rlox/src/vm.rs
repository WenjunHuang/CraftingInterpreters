use std::collections::VecDeque;

use crate::chunk::{Chunk, OpCode};
use crate::debug::{disassemble_chunk, disassemble_instruction};
use crate::value::{print_value, Value};

pub struct VM {
    chunk: Chunk,
    ip: u32,
    stack: VecDeque<Value>,
}

pub enum InterpretResult {
    Ok,
    CompileError,
    RuntimeError,
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

    fn push_value(&mut self, value: Value) {
        self.stack.push_back(value);
    }

    fn pop_value(&mut self) -> Option<Value> {
        self.stack.pop_back()
    }

    pub fn run(&mut self) -> InterpretResult {
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


            let code = self.read_byte();
            match OpCode::try_from(code) {
                Ok(OpCode::OpConstant) => {
                    let idx = self.read_byte();
                    let constant = self.chunk.constants.values[idx as usize];
                    self.push_value(constant);
                }
                Ok(OpCode::OpReturn) => {
                    if let Some(v) = self.pop_value() {
                        print_value(&v);
                        println!();
                        return InterpretResult::Ok;
                    }
                }
                Ok(OpCode::OpNegate) => {
                    if let Some(v) = self.pop_value() {
                        self.push_value(-v);
                    }
                }
                Err(_) => {
                    println!("Unknown opcode {}", code);
                    return InterpretResult::RuntimeError;
                }
                Ok(OpCode::OpAdd) => self.binary_op(OpCode::OpAdd),
                Ok(OpCode::OpSubtract) => self.binary_op(OpCode::OpSubtract),
                Ok(OpCode::OpMultiply) => self.binary_op(OpCode::OpMultiply),
                Ok(OpCode::OpDivide) => self.binary_op(OpCode::OpDivide),
            }
        }
    }

    pub fn interpret(chunk: Chunk) -> InterpretResult {
        let mut vm = VM::new(chunk);
        vm.run()
    }

    fn binary_op(&mut self, op: OpCode) {
        if let (Some(b), Some(a)) = (self.pop_value(), self.pop_value()) {
            match op {
                OpCode::OpAdd => self.push_value(a + b),
                OpCode::OpSubtract => self.push_value(a - b),
                OpCode::OpMultiply => self.push_value(a * b),
                OpCode::OpDivide => self.push_value(a / b),
                _ => {}
            }
        }
    }

    // fn interpret(source: &str) -> InterpretResult {
    //     compile(source);
    //     return InterpretResult::Ok;
    // }
}