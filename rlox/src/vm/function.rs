use std::cell::RefCell;
use std::fmt::{Display, Formatter};
use crate::chunk::Chunk;

#[derive(Copy, Clone, Debug, Eq, PartialEq)]
pub enum FunctionType {
    SCRIPT,
    FUNCTION,
}

#[derive(Debug)]
pub struct Function {
    pub arity: usize,
    pub chunk: Chunk,
    pub name: String,
    pub function_type: FunctionType,
}

impl Function {
    pub fn new(arity: usize, chunk: Chunk, function_type: FunctionType) -> Function {
        Function {
            arity,
            chunk,
            name: "".to_string(),
            function_type,
        }
    }
}

impl Display for Function {
    fn fmt(&self, f: &mut Formatter<'_>) -> std::fmt::Result {
        if self.name.is_empty() {
            write!(f, "<script>")
        } else {
            write!(f, "<fn {}>", self.name)
        }
    }
}