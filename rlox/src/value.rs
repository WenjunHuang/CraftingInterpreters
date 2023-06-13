use std::fmt::{Display, Formatter};
use std::rc::Rc;
use crate::function::Function;
use crate::memory::grow_capacity;
use crate::native_function::NativeFunction;

#[derive(Clone, Debug)]
pub enum Value {
    Nil,
    Number(f64),
    Bool(bool),
    StringValue(Rc<String>),
    FunctionValue(Rc<Function>),
    NativeFunctionValue(Rc<NativeFunction>),
}

impl PartialEq<Self> for Value {
    fn eq(&self, other: &Self) -> bool {
        match (self, other) {
            (Value::Nil, Value::Nil) => true,
            (Value::Number(n1), Value::Number(n2)) => n1 == n2,
            (Value::Bool(b1), Value::Bool(b2)) => b1 == b2,
            (Value::StringValue(s1), Value::StringValue(s2)) => s1 == s2,
            (Value::FunctionValue(f1), Value::FunctionValue(f2)) => Rc::ptr_eq(f1, f2),
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
        }
    }
}

#[derive(Debug)]
pub struct ValueArray {
    pub values: Vec<Value>,
    pub capacity: usize,
    pub count: usize,
}

impl ValueArray {
    pub fn new() -> ValueArray {
        ValueArray {
            values: Vec::new(),
            capacity: 0,
            count: 0,
        }
    }
    pub fn write_value(&mut self, value: Value) {
        if self.capacity < self.count + 1 {
            let old_capacity = self.capacity;
            self.capacity = grow_capacity(old_capacity);
            self.values.resize(self.capacity as usize, Value::Nil);
        }

        self.values[self.count as usize] = value;
        self.count += 1;
    }

    pub fn read_value(&self, index: usize) -> Option<Value> {
        return self.values.get(index).cloned();
    }
}

pub fn print_value(value: &Value) {
    print!("{:4}", value);
}