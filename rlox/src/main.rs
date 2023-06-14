use std::env::args;
use std::fs::File;
use std::io::{Read, stdout, Write};
use std::io::stdin;
use std::process::exit;
use crate::compile::compile;
use crate::vm::vm::{InterpretError, InterpretResult, VM};


mod chunk;
mod vm;
mod compiler;
mod compile;

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
        stdout().flush();
        // read a line from stdin
        let mut line = String::new();
        loop {
            match stdin().read_line(&mut line) {
                Ok(size) => {
                    if size == 1 {
                        interpret(line);
                        break;
                    }
                }
                Err(_) => {
                    println!("Error");
                    return;
                }
            }
        }
    }
}

fn interpret(source: String) -> InterpretResult {
    if let Ok(chunk) = compile(source) {
        let mut vm = VM::new(chunk);
        vm.run()
    } else {
        Err(InterpretError::CompileError)
    }
}

fn run_file(path: &str) {
    let mut file = File::open(path).expect("File not found");
    let mut contents = String::new();
    file.read_to_string(&mut contents).expect("Error reading file");
    // interpret(contents);
}
