use std::cell::RefCell;
use std::collections::{HashMap, VecDeque};
use std::fmt::{Display, Formatter, Write};
use std::mem;
use std::ops::Deref;
use std::rc::Rc;
use std::time::{SystemTime, UNIX_EPOCH};


use crate::chunk::{Chunk, OpCode};
use crate::vm::class::{BoundMethod, Class, Instance};
use crate::vm::closure::Closure;
use crate::vm::debug::disassemble_instruction;

use crate::vm::function::Function;
use crate::vm::native_function::{NativeFn, NativeFunction};
use crate::vm::up_value::UpValue;
use crate::vm::value::{print_value, Value};
use crate::vm::value::Value::{Bool, BoundMethodValue, ClassValue, ClosureValue, FunctionValue, InstanceValue, NativeFunctionValue, Number, StringValue};
use crate::vm::vm::InterpretError::RuntimeError;
use crate::vm::vm::StackValue::RawValue;

const FRAMES_MAX: usize = 64;
const STACK_MAX: usize = FRAMES_MAX * u8::MAX as usize;

#[derive(Debug, Clone, Eq, PartialEq)]
pub enum StackValue {
    RawValue(Value),
    UpValue(Rc<RefCell<Value>>),
}

impl StackValue {
    pub fn to_value(self) -> Value {
        match self {
            RawValue(value) => value,
            StackValue::UpValue(value) => value.borrow().clone(),
        }
    }
}

impl Display for StackValue {
    fn fmt(&self, f: &mut Formatter<'_>) -> std::fmt::Result {
        match self {
            RawValue(value) => write!(f, "{}", value),
            StackValue::UpValue(value) => write!(f, "{}", value.borrow()),
        }
    }
}

pub struct CallFrame {
    closure: Rc<Closure>,
    ip: usize,
    stack_base: usize,
}

pub struct VM {
    frames: Vec<CallFrame>,
    stack: Vec<StackValue>,
    globals: HashMap<String, Value>,
}

pub enum InterpretError {
    CompileError(String),
    RuntimeError(String),
}

pub type InterpretResult = Result<(), InterpretError>;

impl VM {
    pub fn new(function: Function) -> VM {
        let mut vm = VM {
            frames: Vec::with_capacity(FRAMES_MAX),
            stack: Vec::with_capacity(STACK_MAX),
            globals: HashMap::new(),
        };
        let rc = Rc::new(Closure::new(Rc::new(function)));
        vm.define_native("clock", VM::clock_native);
        vm.push_value(RawValue(ClosureValue(rc.clone())));
        vm.call(rc, 0);
        return vm;
    }

    fn current_frame(&mut self) -> &mut CallFrame {
        if let Some(cur) = self.frames.last_mut() {
            cur
        } else {
            panic!("No current frame");
        }
    }

    fn current_chunk(&mut self) -> &Chunk {
        &self.current_frame().closure.function.chunk
    }

    fn read_byte(&mut self) -> u8 {
        let frame = self.current_frame();
        let byte = frame.closure.function.chunk.code[frame.ip];
        frame.ip += 1;
        byte
    }

    fn read_short(&mut self) -> u16 {
        let frame = self.current_frame();
        let byte1 = frame.closure.function.chunk.code[frame.ip];
        frame.ip += 1;
        let byte2 = frame.closure.function.chunk.code[frame.ip];
        frame.ip += 1;

        ((byte1 as u16) << 8) | byte2 as u16
    }

    fn push_value(&mut self, value: StackValue) {
        self.stack.push(value);
    }

    fn pop_value(&mut self) -> Result<StackValue, InterpretError> {
        self.stack.pop().ok_or(RuntimeError("Stack underflow".to_string()))
    }

    fn peek_value_mut(&mut self, index: usize) -> Result<&mut StackValue, InterpretError> {
        let size = self.stack.len();
        return if size == 0 || size <= index {
            Err(RuntimeError("Stack underflow".to_string()))
        } else {
            self.stack.get_mut(size - index - 1).ok_or(RuntimeError("Stack underflow".to_string()))
        };
    }
    fn peek_value(&self, index: usize) -> Result<&StackValue, InterpretError> {
        let size = self.stack.len();
        return if size == 0 || size <= index {
            Err(RuntimeError("Stack underflow".to_string()))
        } else {
            self.stack.get(size - index - 1).ok_or(RuntimeError("Stack underflow".to_string()))
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
                disassemble_instruction(&frame.closure.function.chunk, frame.ip);
            }


            let code = self.read_byte();
            match OpCode::try_from(code) {
                Ok(OpCode::OpConstant) => {
                    let constant = self.read_constant().clone();
                    self.push_value(RawValue(constant));
                }
                Ok(OpCode::OpReturn) => {
                    let result = self.pop_value()?;
                    let stack_base = self.current_frame().stack_base;
                    self.frames.pop();
                    if self.frames.len() == 0 {
                        // Exit interpreter
                        self.pop_value()?;
                        return Ok(());
                    }

                    // Pop all values from the stack that were pushed by the caller
                    while self.stack.len() > stack_base {
                        self.pop_value()?;
                    }
                    self.push_value(result);
                }
                Ok(OpCode::OpNegate) => {
                    let v = self.pop_value()?;
                    match v {
                        RawValue(Number(n)) => {
                            self.push_value(RawValue(Number(-n)));
                        }
                        StackValue::UpValue(value) => {
                            let mut v = value.borrow_mut();
                            match *v {
                                Number(n) => {
                                    *v = Number(-n);
                                }
                                _ => {
                                    return Err(RuntimeError(self.runtime_error("Operand must be a number.")));
                                }
                            }
                        }
                        _ => {
                            return Err(RuntimeError(self.runtime_error("Operand must be a number.")));
                        }
                    }
                }
                Err(_) => {
                    return Err(RuntimeError(format!("Unknown opcode {}", code)));
                }
                Ok(OpCode::OpAdd) => {
                    self.binary_op(OpCode::OpAdd)?;
                }
                Ok(OpCode::OpSubtract) => self.binary_op(OpCode::OpSubtract)?,
                Ok(OpCode::OpMultiply) => self.binary_op(OpCode::OpMultiply)?,
                Ok(OpCode::OpDivide) => self.binary_op(OpCode::OpDivide)?,
                Ok(OpCode::OpNil) => self.push_value(RawValue(Value::Nil)),
                Ok(OpCode::OpTrue) => self.push_value(RawValue(Bool(true))),
                Ok(OpCode::OpFalse) => self.push_value(RawValue(Bool(false))),
                Ok(OpCode::OpNot) => {
                    let value = self.pop_value()?;
                    match value {
                        RawValue(Bool(v)) => self.push_value(RawValue(Bool(!v))),
                        RawValue(Value::Nil) => self.push_value(RawValue(Bool(true))),
                        StackValue::UpValue(upvalue) => {
                            let mut v = upvalue.borrow_mut();
                            match *v {
                                Bool(b) => {
                                    *v = Bool(!b);
                                }
                                Value::Nil => {
                                    *v = Bool(true);
                                }
                                _ => {
                                    return Err(RuntimeError(self.runtime_error("Operand must be a boolean.")));
                                }
                            }
                        }
                        _ => {
                            return Err(RuntimeError(self.runtime_error("Operand must be a boolean.")));
                        }
                    }
                }
                Ok(OpCode::OpEqual) => {
                    let b = self.pop_value()?;
                    let a = self.pop_value()?;
                    self.push_value(RawValue(Bool(a == b)));
                }
                Ok(OpCode::OpGreater) => {
                    self.binary_op(OpCode::OpGreater)?;
                }
                Ok(OpCode::OpLess) => {
                    self.binary_op(OpCode::OpLess)?;
                }
                Ok(OpCode::OpPrint) => {
                    let v = self.pop_value()?;
                    print_value(&v);
                    println!();
                }
                Ok(OpCode::OpPop) => {
                    self.pop_value()?;
                }
                Ok(OpCode::OpDefineGlobal) => {
                    let name = self.read_string_constant()?;
                    let value = self.pop_value()?;
                    match value {
                        RawValue(v) => {
                            self.globals.insert(name.to_string(), v);
                        }
                        StackValue::UpValue(upvalue) => {
                            let v = upvalue.borrow();
                            self.globals.insert(name.to_string(), v.clone());
                        }
                    }
                }
                Ok(OpCode::OpGetGlobal) => {
                    let name = self.read_string_constant()?;
                    match self.globals.get(&name) {
                        Some(v) => {
                            self.push_value(RawValue(v.clone()));
                        }
                        None => {
                            return Err(RuntimeError(self.runtime_error(&format!("Undefined variable '{}'.", name))));
                        }
                    };
                }
                Ok(OpCode::OpSetGlobal) => {
                    let name = self.read_string_constant()?;
                    if self.globals.contains_key(&name) {
                        let value = self.peek_value(0)?;
                        match value {
                            RawValue(v) => {
                                self.globals.insert(name.to_string(), v.clone());
                            }
                            StackValue::UpValue(upvalue) => {
                                let v = upvalue.borrow().clone();
                                self.globals.insert(name.to_string(), v);
                            }
                        }
                    } else {
                        return Err(RuntimeError(self.runtime_error(&format!("Undefined variable '{}'.", name))));
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
                    self.stack[frame_base + slot as usize] = self.peek_value(0)?.clone();
                }
                Ok(OpCode::OpJumpIfFalse) => {
                    let offset = self.read_short();
                    let value = self.peek_value(0)?;
                    if self.is_falsey(value) {
                        self.current_frame().ip += offset as usize;
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
                    let callee = self.peek_value(arg_count as usize)?.clone();
                    match callee {
                        RawValue(NativeFunctionValue(function)) => {
                            let mut args = Vec::with_capacity(arg_count as usize);
                            for _ in 0..arg_count {
                                let value = self.pop_value()?;
                                args.push(value);
                            }
                            let result = function.call(&args);
                            self.pop_value()?; // pop native_function from stack
                            self.push_value(RawValue(result));
                        }
                        RawValue(ClosureValue(closure)) => {
                            self.call(closure, arg_count as usize);
                        }
                        RawValue(ClassValue(class)) => {
                            let slot = self.peek_value_mut(arg_count as usize)?;
                            *slot = RawValue(InstanceValue(Rc::new(Instance::new(class.clone()))));
                            match class.methods.borrow().get("init") {
                                Some(method) => {
                                    self.call(method.clone(), arg_count as usize);
                                }
                                None if arg_count != 0 => {
                                    return Err(RuntimeError(self.runtime_error("Expected 0 arguments but got 1.")));
                                }
                                _ => {}
                            }
                        }
                        RawValue(BoundMethodValue(bound)) => {
                            let st = self.peek_value_mut(arg_count as usize)?;
                            *st = RawValue(InstanceValue(bound.receiver.clone()));
                            self.call(bound.method.clone(), arg_count as usize)?;
                        }
                        StackValue::UpValue(upvalue) => {
                            let v = upvalue.borrow();
                            match *v {
                                ClosureValue(ref closure) => {
                                    let f1 = closure.clone();
                                    self.call(f1, arg_count as usize)?;
                                }
                                BoundMethodValue(ref bound) => {
                                    let st = self.peek_value_mut(arg_count as usize)?;
                                    *st = RawValue(InstanceValue(bound.receiver.clone()));
                                    self.call(bound.method.clone(), arg_count as usize)?;
                                }
                                _ => {
                                    return Err(RuntimeError(self.runtime_error("Can only call functions and closures.")));
                                }
                            }
                        }
                        _ => {
                            return Err(RuntimeError(self.runtime_error("Can only call native functions and closures.")));
                        }
                    }
                }
                Ok(OpCode::OpClosure) => {
                    match self.read_constant() {
                        FunctionValue(function) => {
                            let mut closure = Closure::new(function.clone());
                            for _ in 0..function.upvalue_count {
                                let is_local = self.read_byte() == 1;
                                let index = self.read_byte();
                                if is_local {
                                    closure.upvalues.push(self.capture_upvalue(index as usize)?);
                                } else {
                                    closure.upvalues.push(self.current_frame().closure.upvalues[index as usize].clone());
                                }
                            }
                            self.push_value(RawValue(ClosureValue(Rc::new(closure))));
                        }
                        _ => {
                            return Err(RuntimeError(self.runtime_error("Expect a function value.")));
                        }
                    }
                }
                Ok(OpCode::OpGetUpValue) => {
                    let slot = self.read_byte();
                    let value = if let Some(upvalue) = self.current_frame().closure.upvalues.get(slot as usize) {
                        Ok(upvalue.value.borrow().clone())
                    } else {
                        Err(RuntimeError(self.runtime_error("Expect a function value.")))
                    };
                    self.push_value(RawValue(value?));
                }
                Ok(OpCode::OpSetUpValue) => {
                    let slot = self.read_byte();
                    let peek = self.peek_value(0)?.clone();
                    if let Some(upvalue) = self.current_frame().closure.upvalues.get(slot as usize) {
                        match peek {
                            RawValue(value) => {
                                upvalue.value.replace(value);
                            }
                            StackValue::UpValue(value) => {
                                upvalue.value.replace(value.borrow().clone());
                            }
                        }
                    } else {
                        return Err(RuntimeError(self.runtime_error("Undefined upvalue.")));
                    }
                }
                Ok(OpCode::OpClass) => {
                    let name = self.read_string_constant()?;
                    let class = Class::new(name);
                    self.push_value(RawValue(ClassValue(Rc::new(class))));
                }
                Ok(OpCode::OpGetProperty) => {
                    let value = self.pop_value()?;
                    match value {
                        RawValue(InstanceValue(instance)) => {
                            let property_value = self.get_instance_property(&instance)?;
                            self.push_value(property_value);
                        }
                        StackValue::UpValue(upvalue) => {
                            let v = upvalue.borrow();
                            match *v {
                                InstanceValue(ref instance) => {
                                    let property_value = self.get_instance_property(instance)?;
                                    self.push_value(property_value);
                                }
                                _ => {
                                    return Err(RuntimeError(self.runtime_error("Only instances have properties.")));
                                }
                            }
                        }
                        _ => {
                            return Err(RuntimeError(self.runtime_error("Only instances have properties.")));
                        }
                    }
                }
                Ok(OpCode::OpSetProperty) => {
                    let name = self.read_string_constant()?;
                    let value = self.pop_value()?;
                    let instance = self.pop_value()?;
                    match instance {
                        RawValue(InstanceValue(instance)) => {
                            let value = value.to_value();
                            instance.fields
                                .borrow_mut()
                                .insert(name, value.clone());
                            self.push_value(RawValue(value));
                        }
                        StackValue::UpValue(upvalue) => {
                            let v = upvalue.borrow();
                            match *v {
                                InstanceValue(ref instance) => {
                                    let value = value.to_value();
                                    instance.fields
                                        .borrow_mut()
                                        .insert(name, value.clone());
                                    self.push_value(RawValue(value));
                                }
                                _ => {
                                    return Err(RuntimeError(self.runtime_error("Only instances have fields.")));
                                }
                            }
                        }
                        _ => {
                            return Err(RuntimeError(self.runtime_error("Only instances have fields.")));
                        }
                    }
                }
                Ok(OpCode::OpMethod) => {
                    let method_name = self.read_string_constant()?;
                    self.define_method(&method_name)?;
                }
                Ok(OpCode::OpInvoke) => {
                    let method_name = self.read_string_constant()?;
                    let arg_count = self.read_byte();
                    self.invoke(&method_name, arg_count as usize)?;
                }
                Ok(OpCode::OpInherit) => {
                    let subclass = self.pop_value()?;
                    let superclass = self.pop_value()?;
                    if let RawValue(ClassValue(subclass)) = subclass {
                        if let RawValue(ClassValue(superclass)) = superclass {
                            subclass.methods.borrow_mut().extend(superclass.methods.borrow().clone());
                            self.push_value(RawValue(ClassValue(subclass)));
                        } else {
                            return Err(RuntimeError(self.runtime_error("Superclass must be a class.")));
                        }
                    } else {
                        return Err(RuntimeError(self.runtime_error("Subclass must be a class.")));
                    }
                }
                Ok(OpCode::OpGetSuper) => {
                    let name = self.read_string_constant()?;
                    let superclass = self.pop_value().and_then(|value| {
                        match value {
                            RawValue(ClassValue(superclass)) => {
                                Ok(superclass)
                            }
                            _ => {
                                Err(RuntimeError(self.runtime_error("Superclass must be a class.")))
                            }
                        }
                    })?;
                    let instance = self.pop_value().and_then(|value| {
                        match value {
                            RawValue(InstanceValue(instance)) => {
                                Ok(instance)
                            }
                            _ => {
                                Err(RuntimeError(self.runtime_error("Expect an instance.")))
                            }
                        }
                    })?;
                    let superclass_methods = superclass.methods.borrow();
                    if let Some(method) = superclass_methods.get(&name) {
                        let bound = BoundMethod::new(instance.clone(), method.clone());
                        self.push_value(RawValue(BoundMethodValue(Rc::new(bound))));
                    } else {
                        return Err(RuntimeError(self.runtime_error("Undefined super class property.")));
                    }
                }
                Ok(OpCode::OpSuperInvoke) => {
                    let method = self.read_string_constant()?;
                    let arg_count = self.read_byte();
                    let super_class = self.pop_value().and_then(|value| {
                        match value {
                            RawValue(ClassValue(class)) => Ok(class),
                            _ => {
                                Err(RuntimeError(self.runtime_error("Expect a class")))
                            }
                        }
                    })?;
                    self.invoke_from_class(super_class, &method, arg_count as usize)?;
                }
            }
        }
    }

    fn invoke_property(&mut self, name: &str, inst: Rc<Instance>, arg_count: usize) -> Result<(), InterpretError> {
        let field = inst.fields.borrow().get(name)
            .map(|field| field.clone());
        if let Some(ClosureValue(closure)) = field {
            self.call(closure, arg_count)
        } else {
            self.invoke_from_class(inst.class.clone(), name, arg_count)
        }
    }

    fn invoke_from_class(&mut self, class: Rc<Class>, name: &str, arg_count: usize) -> Result<(), InterpretError> {
        let method = class.methods
            .borrow_mut()
            .get(name)
            .map(|method| method.clone())
            .ok_or_else(|| RuntimeError(self.runtime_error("Undefined property.")))?;
        self.call(method, arg_count)
    }

    fn invoke(&mut self, name: &str, arg_count: usize) -> Result<(), InterpretError> {
        // let value = self.peek_value(arg_count)?;
        match self.peek_value(arg_count)? {
            RawValue(value) => {
                match value.clone() {
                    InstanceValue(inst) => {
                        self.invoke_property(name, inst, arg_count)
                    }
                    _ => {
                        return Err(RuntimeError(self.runtime_error("Expect a function value.")));
                    }
                }
            }
            StackValue::UpValue(upvalue) => {
                let upvalue = upvalue.clone();
                let v = upvalue.borrow();
                match *v {
                    InstanceValue(ref inst) => {
                        self.invoke_property(name, inst.clone(), arg_count)
                    }
                    _ => {
                        return Err(RuntimeError(self.runtime_error("Expect a function value.")));
                    }
                }
            }
        }
    }

    fn define_method(&mut self, method_name: &str) -> Result<(), InterpretError> {
        let value = self.pop_value()?;
        let class = match self.peek_value(0)? {
            RawValue(ClassValue(class)) => {
                class.clone()
            }
            StackValue::UpValue(upvalue) => {
                let v = upvalue.borrow();
                match *v {
                    ClassValue(ref class) => {
                        class.clone()
                    }
                    _ => {
                        return Err(RuntimeError(self.runtime_error("Only instances have fields.")));
                    }
                }
            }
            _ => {
                return Err(RuntimeError(self.runtime_error("Only instances have fields.")));
            }
        };
        match value {
            RawValue(ClosureValue(closure)) => {
                class.methods.borrow_mut().insert(method_name.to_string(), closure);
            }
            StackValue::UpValue(upvalue) => {
                let v = upvalue.borrow();
                match *v {
                    ClosureValue(ref closure) => {
                        class.methods.borrow_mut().insert(method_name.to_string(), closure.clone());
                    }
                    _ => {
                        return Err(RuntimeError(self.runtime_error("Value is not a closure.")));
                    }
                }
            }
            _ => {
                return Err(RuntimeError(self.runtime_error("Value is not a closure.")));
            }
        }

        Ok(())
    }

    fn get_instance_property(&mut self, instance: &Rc<Instance>) -> Result<StackValue, InterpretError> {
        let name = self.read_string_constant()?;
        return if let Some(value) = instance.fields.borrow().get(&name) {
            Ok(RawValue(value.clone()))
        } else if let Some(method) = instance.class.methods.borrow().get(&name) {
            let bound = BoundMethod::new(instance.clone(), method.clone());
            Ok(RawValue(BoundMethodValue(Rc::new(bound))))
        } else {
            Err(RuntimeError(self.runtime_error(format!("Undefined property {}.", name).as_str())))
        };
    }

    fn capture_upvalue(&mut self, stack_index: usize) -> Result<UpValue, InterpretError> {
        let real_stack_index = self.current_frame().stack_base + stack_index;
        if let Some(stack_value) = self.stack.get_mut(real_stack_index) {
            match stack_value {
                StackValue::UpValue(upvalue) => {
                    return Ok(UpValue {
                        value: upvalue.clone(),
                        is_closed: RefCell::new(false),
                    });
                }
                r @ RawValue(_) => {
                    match mem::replace(r, RawValue(Value::Nil)) {
                        RawValue(value) => {
                            let upvalue = Rc::new(RefCell::new(value));
                            self.stack[real_stack_index] = StackValue::UpValue(upvalue.clone());
                            return Ok(UpValue {
                                value: upvalue.clone(),
                                is_closed: RefCell::new(false),
                            });
                        }
                        _ => {}
                    }
                }
            }
        }
        Err(RuntimeError(self.runtime_error("Expect a up value")))
    }

    fn define_native(&mut self, name: &str, function: NativeFn) {
        self.globals.insert(name.to_string(), NativeFunctionValue(Rc::new(NativeFunction::new(function))));
    }

    fn call(&mut self, closure: Rc<Closure>, arg_count: usize) -> Result<(), InterpretError> {
        if arg_count != closure.function.arity {
            return Err(RuntimeError(self.runtime_error(&format!("Expected {} arguments but got {}.", closure.function.arity, arg_count))));
        }
        if self.frames.len() == FRAMES_MAX {
            return Err(RuntimeError(self.runtime_error("Stack overflow.")));
        }
        self.frames.push(CallFrame {
            closure,
            ip: 0,
            stack_base: self.stack.len() - arg_count - 1,
        });
        Ok(())
    }

    fn read_constant(&mut self) -> &Value {
        let slot = self.read_byte();
        self.current_frame().closure.function.chunk.constants.values.get(slot as usize).as_ref().unwrap()
    }

    fn is_falsey(&self, value: &StackValue) -> bool {
        match value {
            RawValue(Value::Nil) => true,
            RawValue(Bool(v)) => !v,
            _ => false,
        }
    }

    fn read_string_constant(&mut self) -> Result<String, InterpretError> {
        let b = self.read_byte();
        return if let Some(StringValue(name)) = self.current_chunk().constants.read_value(b as usize) {
            Ok(name.clone())
        } else {
            Err(RuntimeError(self.runtime_error(&format!("Undefined constant for '{}'.", b))))
        };
    }

    fn binary_op(&mut self, op: OpCode) -> Result<(), InterpretError> {
        match (self.pop_value()?, self.pop_value()?) {
            (RawValue(Number(b)), RawValue(Number(a))) => {
                self.number_binary_op(op, b, a);
            }
            (RawValue(StringValue(b)), RawValue(StringValue(a))) => {
                match op {
                    OpCode::OpAdd => self.push_value(RawValue(StringValue(format!("{}{}", a, b)))),
                    _ => {
                        return Err(RuntimeError(self.runtime_error("Unsupported string operation.")));
                    }
                }
            }
            (StackValue::UpValue(b), RawValue(Number(a))) => {
                match b.borrow().deref() {
                    Number(b_val) => self.number_binary_op(op, *b_val, a),
                    _ => {
                        return Err(RuntimeError(self.runtime_error("Unsupported up value operation.")));
                    }
                }
            }
            (RawValue(Number(b)), StackValue::UpValue(a)) => {
                match a.borrow().deref() {
                    Number(a_val) => self.number_binary_op(op, b, *a_val),
                    _ => {
                        return Err(RuntimeError(self.runtime_error("Unsupported up value operation.")));
                    }
                }
            }
            (StackValue::UpValue(b), StackValue::UpValue(a)) => {
                match (b.borrow().deref(), a.borrow().deref()) {
                    (Number(b_val), Number(a_val)) => self.number_binary_op(op, *b_val, *a_val),
                    _ => {
                        return Err(RuntimeError(self.runtime_error("Unsupported up value operation.")));
                    }
                }
            }
            _ => {
                return Err(RuntimeError(self.runtime_error("Unsupported operation.")));
            }
        }
        Ok(())
    }

    fn number_binary_op(&mut self, op: OpCode, b: f64, a: f64) {
        match op {
            OpCode::OpAdd => self.push_value(RawValue(Number(a + b))),
            OpCode::OpSubtract => self.push_value(RawValue(Number(a - b))),
            OpCode::OpMultiply => self.push_value(RawValue(Number(a * b))),
            OpCode::OpDivide => self.push_value(RawValue(Number(a / b))),
            OpCode::OpGreater => self.push_value(RawValue(Bool(a > b))),
            OpCode::OpLess => self.push_value(RawValue(Bool(a < b))),
            _ => {}
        }
    }

    fn runtime_error(&self, message: &str) -> String {
        let mut result = String::new();
        for frame in self.frames.iter().rev() {
            let function = &frame.closure.function;
            let instruction = frame.ip - 1;
            writeln!(result, "[line {} in {}] {}", function.chunk.lines[instruction],
                     if function.name.is_empty() {
                         "script".to_string()
                     } else {
                         format!("{}()", function.name)
                     }, message).unwrap();
        }
        return result;
    }
}

impl VM {
    fn clock_native(args: &[StackValue]) -> Value {
        // get system time as long
        let now = SystemTime::now().duration_since(UNIX_EPOCH).unwrap().as_secs();
        return Number(now as f64);
    }
}