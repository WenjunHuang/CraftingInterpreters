use std::cell::RefCell;
use std::collections::HashMap;
use std::fmt::{Display, Formatter};
use std::rc::Rc;
use crate::vm::value::Value;

#[derive(Debug)]
pub struct Class {
    name: String,
}

impl Class {
    pub fn new(name: String) -> Class {
        Class {
            name
        }
    }
}

impl Display for Class {
    fn fmt(&self, f: &mut Formatter<'_>) -> std::fmt::Result {
        write!(f, "<class {}>", self.name)
    }
}

#[derive(Debug)]
pub struct Instance {
    pub class: Rc<Class>,
    pub fields: RefCell<HashMap<String, Value>>,
}

impl Display for Instance{
    fn fmt(&self, f: &mut Formatter<'_>) -> std::fmt::Result {
       write!(f,"<instance {}>",self.class)
    }
}

impl Instance {
    pub fn new(class: Rc<Class>) -> Instance {
        Instance {
            class,
            fields: RefCell::new(HashMap::new()),
        }
    }
}