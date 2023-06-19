use std::fmt::{Display, Formatter};
use std::rc::Rc;
use crate::vm::closure::Closure;
use crate::vm::function::Function;
use crate::vm::native_function::NativeFunction;
use crate::vm::vm::StackValue;

#[derive(Debug, Clone)]
pub enum Value {
    Nil,
    Number(f64),
    Bool(bool),
    StringValue(String),
    FunctionValue(Rc<Function>),
    NativeFunctionValue(Rc<NativeFunction>),
    ClosureValue(Rc<Closure>),
}

impl PartialEq<Self> for Value {
    fn eq(&self, other: &Self) -> bool {
        match (self, other) {
            (Value::Nil, Value::Nil) => true,
            (Value::Number(n1), Value::Number(n2)) => n1 == n2,
            (Value::Bool(b1), Value::Bool(b2)) => b1 == b2,
            (Value::StringValue(s1), Value::StringValue(s2)) => s1 == s2,
            (Value::FunctionValue(f1), Value::FunctionValue(f2)) => Rc::ptr_eq(f1, f2),
            (Value::NativeFunctionValue(f1), Value::NativeFunctionValue(f2)) => Rc::ptr_eq(f1, f2),
            (Value::ClosureValue(c1), Value::ClosureValue(c2)) => Rc::ptr_eq(c1, c2),
            _ => false,
        }
    }
}

impl Eq for Value {}

impl Display for Value {
    fn fmt(&self, f: &mut Formatter<'_>) -> std::fmt::Result {
        match self {
            Value::Nil => write!(f, "nil"),
            Value::Number(n) => write!(f, "{}", n),
            Value::Bool(b) => write!(f, "{}", b),
            Value::StringValue(s) => write!(f, "{}", s),
            Value::FunctionValue(fun) => write!(f, "{}", fun),
            Value::NativeFunctionValue(fun) => write!(f, "{}", fun),
            Value::ClosureValue(closure) => write!(f, "{}", closure),
        }
    }
}

#[derive(Debug)]
pub struct ValueArray {
    pub values: Vec<Value>,
}

impl ValueArray {
    pub fn new() -> ValueArray {
        ValueArray {
            values: Vec::new(),
        }
    }
    pub fn write_value(&mut self, value: Value) -> usize {
        // if self.values.capacity() < self.values.len() + 1 {
        //     let new_capacity = grow_capacity(self.values.capacity());
        //     self.values.resize(new_capacity, Value::Nil);
        // }
        self.values.push(value);
        self.values.len() - 1
    }

    pub fn read_value(&self, index: usize) -> Option<&Value> {
        self.values.get(index)
    }
}

pub fn print_value(value: &StackValue) {
    print!("{:4}", value);
}