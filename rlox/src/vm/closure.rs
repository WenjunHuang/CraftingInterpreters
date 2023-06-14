use std::fmt::{Display, Formatter};
use std::rc::Rc;
use crate::vm::function::Function;

#[derive(Debug)]
pub struct Closure {
    pub function: Rc<Function>,
}

impl Closure {
    pub fn new(function: Rc<Function>) -> Closure {
        Closure {
            function
        }
    }
}

impl Display for Closure {
    fn fmt(&self, f: &mut Formatter<'_>) -> std::fmt::Result {
        write!(f,"{}", self.function)
    }
}