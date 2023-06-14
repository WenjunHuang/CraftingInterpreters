use crate::chunk::Chunk;
use crate::compiler::parser::Parser;
use crate::compiler::scanner::{Scanner, TokenType};
use crate::vm::debug::disassemble_chunk;
use crate::vm::function::Function;

pub fn compile(source: String) -> Result<Function, ()> {
    let scanner = Scanner::new(source);
    let chunk = Chunk::new();
    let mut parser = Parser::new(scanner, chunk);
    parser.advance();
    while !parser.match_token(TokenType::Eof) {
        parser.declaration();
    }
    parser.consume(TokenType::Eof, "Expect end of expression.");
    parser.end_compiler();
    let had_error = parser.had_error();
    let function = parser.current_function();


    return if !had_error {
        if cfg!(feature = "DEBUG_PRINT_CODE") {
            println!("Disassembling chunk:");
            disassemble_chunk(&function.chunk, if function.name.is_empty() { "<script>" } else { &function.name });
        }
        Ok(function)
    } else {
        Err(())
    };
}
