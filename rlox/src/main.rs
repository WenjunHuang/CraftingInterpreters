use std::env::args;
use std::fs::File;
use std::io::Read;
use std::io::stdin;
use std::process::exit;

use crate::chunk::OpCode::*;

mod chunk;
mod common;
mod memory;
mod debug;
mod value;
mod vm;
mod compiler;

fn main() {
    // parser command line arguments
    let args: Vec<String> = args().collect();
    if args.len() == 1 {
        repl();
    } else if args.len() == 2 {
        // run_file(&args[1]);
    } else {
        println!("Usage: rlox [script]");
        exit(64);
    }
}

fn repl() {
    loop {
        print!("> ");
        // read a line from stdin
        let mut line = String::new();
        match stdin().read_line(&mut line) {
            Ok(_) => {
                // interpret(line);
            }
            Err(_) => {
                println!("Error");
                return;
            }
        }
    }
}

fn run_file(path: &str) {
    let mut file = File::open(path).expect("File not found");
    let mut contents = String::new();
    file.read_to_string(&mut contents).expect("Error reading file");
    // interpret(contents);
}
