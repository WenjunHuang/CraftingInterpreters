use std::collections::{HashMap, VecDeque};
use std::fmt::format;
use std::rc::Rc;
use std::time::{SystemTime, UNIX_EPOCH};

use num_enum::TryFromPrimitiveError;

use crate::chunk::{Chunk, OpCode};
use crate::compiler::scanner::Scanner;
use crate::debug::{disassemble_chunk, disassemble_instruction};
use crate::function::Function;
use crate::native_function::{NativeFn, NativeFunction};
use crate::value::{print_value, Value};
use crate::value::Value::{FunctionValue, NativeFunctionValue, Number, StringValue};
use crate::vm::InterpretError::RuntimeError;

const FRAMES_MAX: usize = 64;
const STACK_MAX: usize = FRAMES_MAX * u8::MAX as usize;

pub struct CallFrame {
    function: Rc<Function>,
    ip: usize,
    stack_base: usize,
}

pub struct VM {
    frames: VecDeque<CallFrame>,
    stack: VecDeque<Value>,
    globals: HashMap<String, Value>,
}

pub enum InterpretError {
    CompileError,
    RuntimeError,
}

pub type InterpretResult = Result<(), InterpretError>;

impl VM {
    pub fn new(function: Function) -> VM {
        let mut vm = VM {
            frames: VecDeque::with_capacity(FRAMES_MAX),
            stack: VecDeque::with_capacity(STACK_MAX),
            globals: HashMap::new(),
        };
        let rc = Rc::new(function);
        vm.stack.push_back(FunctionValue(rc.clone()));
        vm.define_native("clock", VM::clock_native);
        vm.call(rc, 0);
        return vm;
    }

    fn current_frame(&mut self) -> &mut CallFrame {
        if let Some(cur) = self.frames.back_mut() {
            cur
        } else {
            panic!("No current frame");
        }
    }

    fn current_chunk(&mut self) -> &Chunk {
        &self.current_frame().function.chunk
    }

    fn read_byte(&mut self) -> u8 {
        let frame = self.current_frame();
        let byte = frame.function.chunk.code[frame.ip];
        frame.ip += 1;
        byte
    }

    fn read_short(&mut self) -> u16 {
        let frame = self.current_frame();
        let byte1 = frame.function.chunk.code[frame.ip];
        frame.ip += 1;
        let byte2 = frame.function.chunk.code[frame.ip];
        frame.ip += 1;

        ((byte1 as u16) << 8) | byte2 as u16
    }

    fn push_value(&mut self, value: Value) {
        self.stack.push_back(value);
    }

    fn pop_value(&mut self) -> Option<Value> {
        self.stack.pop_back()
    }

    fn peek_value(&self, index: usize) -> Option<&Value> {
        let size = self.stack.len();
        return if size == 0 {
            None
        } else if size <= index {
            None
        } else {
            self.stack.get(size - index - 1)
        };
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
                let frame = self.current_frame();
                disassemble_instruction(&frame.function.chunk, frame.ip);
            }


            let code = self.read_byte();
            match OpCode::try_from(code) {
                Ok(OpCode::OpConstant) => {
                    let constant = self.read_constant();
                    self.push_value(constant);
                }
                Ok(OpCode::OpReturn) => {
                    let result = self.pop_value().expect("No return value on stack");
                    let stack_base = self.current_frame().stack_base;
                    self.frames.pop_back();
                    if self.frames.len() == 0 {
                        // Exit interpreter
                        self.pop_value();
                        return Ok(());
                    }

                    // Pop all values from the stack that were pushed by the caller
                    while self.stack.len() > stack_base {
                        self.pop_value();
                    }
                    self.push_value(result);
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
                    match self.globals.get(name.as_ref()) {
                        Some(v) => {
                            self.push_value(v.clone());
                        }
                        None => {
                            self.runtime_error(&format!("Undefined variable '{}'.", name));
                            return Err(RuntimeError);
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
                Ok(OpCode::OpGetLocal) => {
                    let slot = self.read_byte();
                    let frame_base = self.current_frame().stack_base;
                    let value = self.stack[frame_base + slot as usize].clone();
                    self.push_value(value);
                }
                Ok(OpCode::OpSetLocal) => {
                    let slot = self.read_byte();
                    let frame_base = self.current_frame().stack_base;
                    self.stack[frame_base + slot as usize] = self.peek_value(0).unwrap().clone();
                }
                Ok(OpCode::OpJumpIfFalse) => {
                    let offset = self.read_short();
                    if let Some(value) = self.peek_value(0) {
                        if self.is_falsey(value) {
                            self.current_frame().ip += offset as usize;
                        }
                    }
                }
                Ok(OpCode::OpJump) => {
                    let offset = self.read_short();
                    self.current_frame().ip += offset as usize;
                }
                Ok(OpCode::OpLoop) => {
                    let offset = self.read_short();
                    self.current_frame().ip -= offset as usize;
                }
                Ok(OpCode::OpCall) => {
                    let arg_count = self.read_byte();
                    if let Some(callee) = self.peek_value(arg_count as usize) {
                        match callee {
                            FunctionValue(function) => {
                                if !self.call(function.clone(), arg_count as usize) {
                                    return Err(RuntimeError);
                                }
                            }
                            NativeFunctionValue(function) => {
                                let mut args = Vec::with_capacity(arg_count as usize);
                                let native_fn = function.clone();
                                for _ in 0..arg_count {
                                    let value = self.pop_value().expect("Expect a value on the stack.");
                                    args.push(value);
                                }
                                let result = native_fn.call(&args);
                                self.pop_value();
                                self.push_value(result);
                            }
                            _ => {
                                self.runtime_error("Can only call functions and classes.");
                                return Err(RuntimeError);
                            }
                        }
                    }
                }
            }
        }
    }

    fn define_native(&mut self, name: &str, function: NativeFn) {
        self.globals.insert(name.to_string(), Value::NativeFunctionValue(Rc::new(NativeFunction::new(function))));
    }

    fn call(&mut self, function: Rc<Function>, arg_count: usize) -> bool {
        if arg_count != function.arity {
            self.runtime_error(&format!("Expected {} arguments but got {}.", function.arity, arg_count));
            return false;
        }
        if self.frames.len() == FRAMES_MAX {
            self.runtime_error("Stack overflow.");
            return false;
        }
        self.frames.push_back(CallFrame {
            function: function.clone(),
            ip: 0,
            stack_base: self.stack.len() - arg_count - 1,
        });
        true
    }

    fn read_constant(&mut self) -> Value {
        let slot = self.read_byte();
        self.current_frame().function.chunk.constants.values.get(slot as usize).unwrap().clone()
    }

    fn is_falsey(&self, value: &Value) -> bool {
        match value {
            Value::Nil => true,
            Value::Bool(v) => !v,
            _ => false,
        }
    }

    fn read_string(&mut self) -> Result<Rc<String>, InterpretError> {
        let b = self.read_byte();
        return if let Some(StringValue(name)) = self.current_chunk().constants.read_value(b as usize) {
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


    fn runtime_error(&mut self, message: &str) {
        for frame in self.frames.iter().rev() {
            let function = &frame.function;
            let instruction = frame.ip - 1;
            eprintln!("[line {}] in {}", function.chunk.lines[instruction],
                      if function.name.is_empty() {
                          "script".to_string()
                      } else {
                          format!("{}()", function.name)
                      })
        }
        self.stack.clear()
    }
}

impl VM {
    fn clock_native(args: &[Value]) -> Value {
        // get system time as long
        let now = SystemTime::now().duration_since(UNIX_EPOCH).unwrap().as_secs();
        return Value::Number(now as f64);
    }
}