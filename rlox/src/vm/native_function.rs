use std::fmt::{Debug, Display, Formatter};
use crate::vm::value::Value;

pub type NativeFn = fn(&[Value]) -> Value;

pub struct NativeFunction {
    function: NativeFn,
}

impl Debug for NativeFunction{
    fn fmt(&self, f: &mut Formatter<'_>) -> std::fmt::Result {
        write!(f, "<native fn>")
    }
}

impl Display for NativeFunction{
    fn fmt(&self, f: &mut Formatter<'_>) -> std::fmt::Result {
        write!(f, "<native fn>")
    }
}

impl NativeFunction {
    pub fn new(function: NativeFn) -> NativeFunction {
        NativeFunction {
            function
        }
    }
    pub fn call(&self, arguments: &[Value]) -> Value {
        (self.function)(arguments)
    }
}