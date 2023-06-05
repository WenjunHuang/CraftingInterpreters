use crate::chunk::Chunk;
use crate::compiler::parser::Parser;
use crate::compiler::scanner::{Scanner, TokenType};

pub fn compile(source: String) -> Result<Chunk,()> {
    let scanner = Scanner::new(source);
    let chunk = Chunk::new();
    let mut parser = Parser::new(scanner, chunk);
    parser.advance();
    while(!parser.match_token(TokenType::Eof)) {
        parser.declaration();
    }
    parser.consume(TokenType::Eof, "Expect end of expression.");
    parser.end_compiler();


    return if !parser.had_error {
        Ok(parser.chunk)
    } else {
        Err(())
    }
}
