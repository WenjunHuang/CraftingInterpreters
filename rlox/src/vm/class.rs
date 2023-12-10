use std::cell::RefCell;
use std::collections::HashMap;
use std::fmt::{Display, Formatter};
use std::rc::Rc;
use crate::vm::closure::Closure;
use crate::vm::value::Value;

#[derive(Debug)]
pub struct Class {
    pub name: String,
    pub methods: RefCell<HashMap<String, Rc<Closure>>>,
}

impl Class {
    pub fn new(name: String) -> Class {
        Class {
            name,
            methods: RefCell::new(HashMap::new()),
        }
    }
}

impl Display for Class {
    fn fmt(&self, f: &mut Formatter<'_>) -> std::fmt::Result {
        write!(f, "<class {}>", self.name)
    }
}

#[derive(Debug, Clone)]
pub struct Instance {
    pub class: Rc<Class>,
    pub fields: Rc<RefCell<HashMap<String, Value>>>,
}

impl PartialEq<Self> for Instance {
    fn eq(&self, other: &Self) -> bool {
        Rc::ptr_eq(&self.class, &other.class) && Rc::ptr_eq(&self.fields ,&other.fields)
    }
}

impl Eq for Instance {}

impl Display for Instance {
    fn fmt(&self, f: &mut Formatter<'_>) -> std::fmt::Result {
        write!(f, "<instance {}>", self.class)
    }
}

impl Instance {
    pub fn new(class: Rc<Class>) -> Instance {
        Instance {
            class,
            fields: Rc::new(RefCell::new(HashMap::new())),
        }
    }
}

#[derive(Debug)]
pub struct BoundMethod {
    pub receiver: Rc<Instance>,
    pub method: Rc<Closure>,
}

impl BoundMethod {
    pub fn new(receiver: Rc<Instance>, method: Rc<Closure>) -> BoundMethod {
        BoundMethod {
            receiver,
            method,
        }
    }
}

impl Display for BoundMethod {
    fn fmt(&self, f: &mut Formatter<'_>) -> std::fmt::Result {
        write!(f, "<bound method {}>", self.method)
    }
}