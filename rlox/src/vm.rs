use std::collections::{HashMap, VecDeque};
use std::rc::Rc;

use num_enum::TryFromPrimitiveError;

use crate::chunk::{Chunk, OpCode};
use crate::compiler::scanner::Scanner;
use crate::debug::{disassemble_chunk, disassemble_instruction};
use crate::value::{print_value, Value};
use crate::value::Value::{Number, StringValue};
use crate::vm::InterpretError::RuntimeError;

pub struct VM {
    chunk: Chunk,
    ip: u32,
    stack: VecDeque<Value>,
    globals: HashMap<String, Value>,
}

pub enum InterpretError {
    CompileError,
    RuntimeError,
}

type InterpretResult = Result<(), InterpretError>;

impl VM {
    pub fn new(chunk: Chunk) -> VM {
        VM {
            chunk,
            stack: VecDeque::new(),
            ip: 0,
            globals: HashMap::new(),
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
                    let constant = self.chunk.constants.values[idx as usize].clone();
                    self.push_value(constant);
                }
                Ok(OpCode::OpReturn) => {
                    // Exit interpreter
                    return Ok(());
                }
                Ok(OpCode::OpNegate) => {
                    if let Some(Number(v)) = self.pop_value() {
                        self.push_value(Number(-v));
                    } else {
                        self.runtime_error("Operand must be a number.");
                        return Err(RuntimeError);
                    }
                }
                Err(_) => {
                    println!("Unknown opcode {}", code);
                    return Err(RuntimeError);
                }
                Ok(OpCode::OpAdd) => {
                    self.binary_op(OpCode::OpAdd)
                }
                Ok(OpCode::OpSubtract) => self.binary_op(OpCode::OpSubtract),
                Ok(OpCode::OpMultiply) => self.binary_op(OpCode::OpMultiply),
                Ok(OpCode::OpDivide) => self.binary_op(OpCode::OpDivide),
                Ok(OpCode::OpNil) => self.push_value(Value::Nil),
                Ok(OpCode::OpTrue) => self.push_value(Value::Bool(true)),
                Ok(OpCode::OpFalse) => self.push_value(Value::Bool(false)),
                Ok(OpCode::OpNot) => {
                    match self.pop_value() {
                        Some(Value::Bool(v)) => self.push_value(Value::Bool(!v)),
                        Some(Value::Nil) => self.push_value(Value::Bool(true)),
                        _ => {
                            self.runtime_error("Operand must be a boolean.");
                            return Err(RuntimeError);
                        }
                    }
                }
                Ok(OpCode::OpEqual) => {
                    if let (Some(b), Some(a)) = (self.pop_value(), self.pop_value()) {
                        self.push_value(Value::Bool(a == b));
                    }
                }
                Ok(OpCode::OpGreater) => {
                    self.binary_op(OpCode::OpGreater);
                }
                Ok(OpCode::OpLess) => {
                    self.binary_op(OpCode::OpLess);
                }
                Ok(OpCode::OpPrint) => {
                    if let Some(v) = self.pop_value() {
                        print_value(&v);
                        println!();
                    }
                }
                Ok(OpCode::OpPop) => {
                    self.pop_value();
                }
                Ok(OpCode::OpDefineGlobal) => {
                    let name = self.read_string()?;
                    let value = self.pop_value().unwrap();
                    self.globals.insert(name.to_string(), value);
                }
                Ok(OpCode::OpGetGlobal) => {
                    let name = self.read_string()?;
                    return match self.globals.get(name.as_ref()) {
                        Some(v) => {
                            self.push_value(v.clone());
                            Ok(())
                        }
                        None => {
                            self.runtime_error(&format!("Undefined variable '{}'.", name));
                            Err(RuntimeError)
                        }
                    };
                }
                Ok(OpCode::OpSetGlobal) => {
                    let name = self.read_string()?;
                    if self.globals.contains_key(name.as_ref()) {
                        let value = self.pop_value().unwrap();
                        self.globals.insert(name.to_string(), value);
                    } else {
                        self.runtime_error(&format!("Undefined variable '{}'.", name));
                        return Err(RuntimeError);
                    }
                }
            }
        }
    }

    fn read_string(&mut self) -> Result<Rc<String>, InterpretError> {
        let b = self.read_byte();
        return if let Some(StringValue(name)) = self.chunk.constants.read_value(b as usize) {
            Ok(name)
        } else {
            self.runtime_error(&format!("Undefined constant for '{}'.", b));
            Err(RuntimeError)
        };
    }

    fn binary_op(&mut self, op: OpCode) {
        match (self.pop_value(), self.pop_value()) {
            (Some(Number(b)), Some(Number(a))) => {
                match op {
                    OpCode::OpAdd => self.push_value(Number(a + b)),
                    OpCode::OpSubtract => self.push_value(Number(a - b)),
                    OpCode::OpMultiply => self.push_value(Number(a * b)),
                    OpCode::OpDivide => self.push_value(Number(a / b)),
                    OpCode::OpGreater => self.push_value(Value::Bool(a > b)),
                    OpCode::OpLess => self.push_value(Value::Bool(a < b)),
                    _ => {}
                }
            }
            (Some(StringValue(b)), Some(StringValue(a))) => {
                match op {
                    OpCode::OpAdd => self.push_value(StringValue(Rc::new(format!("{}{}", a, b)))),
                    _ => {}
                }
            }
            _ => {}
        }
    }

    fn peek(&self, index: usize) -> Option<&Value> {
        let size = self.stack.len();
        return if size == 0 {
            None
        } else if size <= index {
            None
        } else {
            self.stack.get(size - index - 1)
        };
    }

    // fn interpret(source: &str) -> InterpretResult {
    //     compile(source);
    //     return InterpretResult::Ok;
    // }
    fn runtime_error(&mut self, message: &str) {
        eprintln!("[line {}] in script", self.chunk.lines[self.ip as usize]);
        self.stack.clear()
    }
}