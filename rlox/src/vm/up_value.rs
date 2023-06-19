use std::cell::RefCell;
use std::rc::Rc;
use crate::vm::value::Value;

#[derive(Debug, Clone)]
pub struct UpValue {
    pub value: Rc<RefCell<Value>>,
    pub is_closed: RefCell<bool>,
}