pub fn grow_capacity(capacity: u32) -> u32 {
    if capacity < 8 {
        return 8;
    }
    return capacity * 2;
}