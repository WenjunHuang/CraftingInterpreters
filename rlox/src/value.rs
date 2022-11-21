use crate::memory::grow_capacity;

pub type Value = f64;

pub struct ValueArray {
    pub values: Vec<Value>,
    pub capacity: u32,
    pub count: u32,
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
            self.values.resize(self.capacity as usize, 0.0);
        }

        self.values[self.count as usize] = value;
        self.count += 1;
    }
}

pub fn print_value(value: &Value) {
    print!("{:4}", value);
}