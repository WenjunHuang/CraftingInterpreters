use std::cell::RefCell;
use std::fmt::{Display, Formatter};
use std::rc::Rc;
use crate::vm::function::Function;
use crate::vm::up_value::UpValue;

#[derive(Debug)]
pub struct Closure {
    pub function: Rc<Function>,
    pub upvalues: Vec<UpValue>,
}

impl Closure {
    pub fn new(function: Rc<Function>) -> Closure {
        let upvalue_count = function.upvalue_count;
        Closure {
            function,
            upvalues: Vec::with_capacity(upvalue_count),
        }
    }
}

impl Display for Closure {
    fn fmt(&self, f: &mut Formatter<'_>) -> std::fmt::Result {
        write!(f, "{}", self.function)
    }
}