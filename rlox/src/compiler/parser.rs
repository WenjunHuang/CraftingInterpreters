use std::collections::HashMap;
use std::rc::Rc;

use lazy_static::lazy_static;
use num_enum::{IntoPrimitive, TryFromPrimitive};

use crate::chunk::{Chunk, OpCode};
use crate::compiler::scanner::{Scanner, Token, TokenType};
use crate::value::Value;

#[repr(u8)]
#[derive(Copy, Clone, Eq, Ord, PartialOrd, PartialEq, IntoPrimitive, TryFromPrimitive)]
enum Precedence {
    None,
    // =
    Assignment,
    // or
    Or,
    // and
    And,
    // == !=
    Equality,
    // < > <= >=
    Comparison,
    // + -
    Term,
    // * /
    Factor,
    // ! -
    Unary,
    // . ()
    Call,
    Primary,
}

type ParseFn = fn(&mut Parser);

struct ParseRule {
    prefix: Option<ParseFn>,
    infix: Option<ParseFn>,
    precedence: Precedence,
}

// this is a map of token types to parse rules
lazy_static! {
    static ref RULES: HashMap<TokenType,ParseRule> = {
        let mut m = HashMap::new();
        m.insert(TokenType::LeftParen, ParseRule {
            prefix: Some(Parser::grouping),
            infix: None,
            precedence: Precedence::None,
        });
        m.insert(TokenType::RightParen,ParseRule{
            prefix: None,
            infix: None,
            precedence: Precedence::None,
        });
        m.insert(TokenType::LeftBrace,ParseRule{
            prefix: Some(Parser::grouping),
            infix: None,
            precedence: Precedence::None,
        });
        m.insert(TokenType::RightBrace,ParseRule{
            prefix: None,
            infix: None,
            precedence: Precedence::None,
        });
        m.insert(TokenType::Comma,ParseRule{
            prefix: None,
            infix: None,
            precedence: Precedence::None,
        });
        m.insert(TokenType::Dot,ParseRule{
            prefix: None,
            infix: None,
            precedence: Precedence::None,
        });
        m.insert(TokenType::Minus,ParseRule{
            prefix: Some(Parser::unary),
            infix: Some(Parser::binary),
            precedence: Precedence::Term,
        });
        m.insert(TokenType::Plus,ParseRule{
            prefix: None,
            infix: Some(Parser::binary),
            precedence: Precedence::Term,
        });
        m.insert(TokenType::Semicolon,ParseRule{
            prefix: None,
            infix:None,
            precedence: Precedence::None,
        });
        m.insert(TokenType::Slash,ParseRule{
            prefix: None,
            infix:Some(Parser::binary),
            precedence: Precedence::Factor,
        });
        m.insert(TokenType::Star,ParseRule{
            prefix: None,
            infix:Some(Parser::binary),
            precedence: Precedence::Factor,
        });

        m.insert(TokenType::Bang,ParseRule{
            prefix: Some(Parser::unary),
            infix:None,
            precedence: Precedence::None,
        });

        m.insert(TokenType::BangEqual,ParseRule{
            prefix: None,
            infix:Some(Parser::binary),
            precedence: Precedence::Equality,
        });

        m.insert(TokenType::Equal,ParseRule{
            prefix: None,
            infix:None,
            precedence: Precedence::None,
        });
        m.insert(TokenType::EqualEqual,ParseRule{
            prefix: None,
            infix:Some(Parser::binary),
            precedence: Precedence::Equality,
        });

        m.insert(TokenType::Greater,ParseRule{
            prefix: None,
            infix:Some(Parser::binary),
            precedence: Precedence::Comparison,
        });

        m.insert(TokenType::GreaterEqual,ParseRule{
            prefix: None,
            infix:Some(Parser::binary),
            precedence: Precedence::Comparison,
        });

        m.insert(TokenType::Less,ParseRule{
            prefix: None,
            infix:Some(Parser::binary),
            precedence: Precedence::Comparison,
        });
                m.insert(TokenType::LessEqual,ParseRule{
            prefix: None,
            infix:Some(Parser::binary),
            precedence: Precedence::Comparison,
        });
                m.insert(TokenType::Identifier,ParseRule{
            prefix: Some(Parser::variable),
            infix:None,
            precedence: Precedence::None,
        });
                m.insert(TokenType::String,ParseRule{
            prefix: Some(Parser::string),
            infix:None,
            precedence: Precedence::None,
        });
                m.insert(TokenType::Number,ParseRule{
            prefix: Some(Parser::number),
            infix:None,
            precedence: Precedence::None,
        });
                m.insert(TokenType::And,ParseRule{
            prefix: None,
            infix:None,
            precedence: Precedence::None,
        });
                m.insert(TokenType::Class,ParseRule{
            prefix: None,
            infix:None,
            precedence: Precedence::None,
        });
                m.insert(TokenType::Else,ParseRule{
            prefix: None,
            infix:None,
            precedence: Precedence::None,
        });
                m.insert(TokenType::False,ParseRule{
            prefix: Some(Parser::literal),
            infix:None,
            precedence: Precedence::None,
        });
                m.insert(TokenType::For,ParseRule{
            prefix: None,
            infix:None,
            precedence: Precedence::None,
        });
                m.insert(TokenType::Fun,ParseRule{
            prefix: None,
            infix:None,
            precedence: Precedence::None,
        });
                m.insert(TokenType::If,ParseRule{
            prefix: None,
            infix:None,
            precedence: Precedence::None,
        });
                m.insert(TokenType::Nil,ParseRule{
            prefix: Some(Parser::literal),
            infix:None,
            precedence: Precedence::None,
        });
                m.insert(TokenType::Or,ParseRule{
            prefix: None,
            infix:None,
            precedence: Precedence::None,
        });
                m.insert(TokenType::Print,ParseRule{
            prefix: None,
            infix:None,
            precedence: Precedence::None,
        });
                m.insert(TokenType::Return,ParseRule{
            prefix: None,
            infix:None,
            precedence: Precedence::None,
        });
                m.insert(TokenType::Super,ParseRule{
            prefix: None,
            infix:None,
            precedence: Precedence::None,
        });
                m.insert(TokenType::This,ParseRule{
            prefix: None,
            infix:None,
            precedence: Precedence::None,
        });
                m.insert(TokenType::True,ParseRule{
            prefix: Some(Parser::literal),
            infix:None,
            precedence: Precedence::None,
        });
                m.insert(TokenType::Var,ParseRule{
            prefix: None,
            infix:None,
            precedence: Precedence::None,
        });
                m.insert(TokenType::While,ParseRule{
            prefix: None,
            infix:None,
            precedence: Precedence::None,
        });
                m.insert(TokenType::Error,ParseRule{
            prefix: None,
            infix:None,
            precedence: Precedence::None,
        });
                m.insert(TokenType::Eof,ParseRule{
            prefix: None,
            infix:None,
            precedence: Precedence::None,
        });
        m
    };
}

pub struct Parser {
    current: Option<Token>,
    previous: Option<Token>,
    scanner: Scanner,
    pub chunk: Chunk,
    pub had_error: bool,
    panic_mode: bool,
}

impl Parser {}

impl Parser {}

impl Parser {
    pub fn new(scanner: Scanner, chunk: Chunk) -> Parser {
        Parser {
            current: None,
            previous: None,
            scanner,
            chunk,
            had_error: false,
            panic_mode: false,
        }
    }

    pub fn advance(&mut self) {
        // move the current token to the previous token
        self.previous = self.current.take();

        loop {
            let token = self.scanner.scan_token();
            self.current.replace(token);
            match self.current {
                Some(ref tt) if tt.token_type != TokenType::Error => break,
                _ => {
                    self.error_at_current("");
                }
            }
        }
    }

    pub fn consume(&mut self, token_type: TokenType, message: &str) {
        match self.current {
            Some(ref t) if t.token_type == token_type => {
                self.advance();
                return;
            }
            _ => {}
        }

        self.error_at_current(message);
    }

    fn literal(&mut self) {
        match self.previous {
            Some(ref t) => {
                match t.token_type {
                    TokenType::False => {
                        self.emit_opcode(OpCode::OpFalse);
                    }
                    TokenType::True => {
                        self.emit_opcode(OpCode::OpTrue);
                    }
                    TokenType::Nil => {
                        self.emit_opcode(OpCode::OpNil);
                    }
                    _ => {}
                }
            }
            _ => {}
        }
    }

    pub fn variable(&mut self) {
        self.named_variable(self.previous.as_ref().unwrap().clone());
    }

    fn named_variable(&mut self, name: Token) {
        if self.match_token(TokenType::Equal) {
            self.expression();
            self.emit_opcode(OpCode::OpSetGlobal);
        } else {
            let arg = self.identifier_constant(name);
            self.emit_byte(arg);
            self.emit_opcode(OpCode::OpGetGlobal);
        }
    }

    fn error_at_current(&mut self, message: &str) {
        let token: Token = self.current.as_ref().unwrap().clone();
        self.error_at(&token, message);
    }

    fn error(&mut self, message: &str) {
        let token = self.previous.as_ref().unwrap().clone();
        self.error_at(&token, message);
    }

    fn error_at(&mut self, token: &Token, message: &str) {
        eprintln!("[line {}] Error", token.line);
        if token.token_type == TokenType::Eof {
            eprint!(" at end");
        } else if token.token_type == TokenType::Error {} else {
            eprint!(" at '{}'", &self.scanner.source[(token.start as usize)..(token.start + token.length) as usize]);
        }
        eprintln!(": {}", message);
        self.had_error = true;
    }

    fn emit_opcode(&mut self, opcode: OpCode) {
        self.chunk.write_opcode(opcode, self.previous_line());
    }

    fn emit_byte(&mut self, byte: u8) {
        self.chunk.write_chunk(byte, self.previous_line());
    }

    fn emit_constant(&mut self, value: Value) {
        self.emit_opcode(OpCode::OpConstant);
        let b = self.make_constant(value);
        self.chunk.write_chunk(b, self.previous_line());
    }

    fn previous_line(&self) -> i32 {
        self.previous.as_ref().unwrap().line
    }

    pub fn expression(&mut self) {
        self.parse_precedence(Precedence::Assignment);
    }

    fn check(&self, token_type: TokenType) -> bool {
        if let Some(ref token) = self.current {
            if token.token_type == token_type {
                return true;
            }
        }
        return false;
    }

    pub fn match_token(&mut self, token_type: TokenType) -> bool {
        return if self.check(token_type) {
            self.advance();
            true
        } else {
            false
        };
    }

    pub fn string(&mut self) {
        if let Some(ref token) = self.previous {
            let mut value = self.scanner.source[token.start as usize..token.start as usize + token.length as usize].to_string();
            value.remove(value.len() - 1);
            value.remove(0);
            self.emit_constant(Value::StringValue(Rc::new(value)));
        }
    }

    pub fn number(&mut self) {
        if let Some(ref token) = self.previous {
            let value = self.scanner.source[token.start as usize..token.start as usize + token.length as usize]
                .parse::<f64>()
                .unwrap();
            self.emit_constant(Value::Number(value));
        }
    }

    fn grouping(&mut self) {
        self.expression();
        self.consume(TokenType::RightParen, "Expect ')' after expression.");
    }

    fn unary(&mut self) {
        if let Some(ref token) = self.previous {
            let operator_type = token.token_type;
            self.expression();

            match operator_type {
                TokenType::Minus => self.emit_opcode(OpCode::OpNegate),
                TokenType::Bang => self.emit_opcode(OpCode::OpNot),
                _ => {}
            }
        }
    }

    fn binary(&mut self) {
        if let Some(ref token) = self.previous {
            let operator_type = token.token_type;
            if let Some(rule) = RULES.get(&operator_type) {
                let precedence: u8 = rule.precedence.into();
                self.parse_precedence(Precedence::try_from(precedence + 1).unwrap());
                match operator_type {
                    TokenType::BangEqual => {
                        self.emit_opcode(OpCode::OpEqual);
                        self.emit_opcode(OpCode::OpNot);
                    }
                    TokenType::EqualEqual => self.emit_opcode(OpCode::OpEqual),
                    TokenType::Greater => self.emit_opcode(OpCode::OpGreater),
                    TokenType::GreaterEqual => {
                        self.emit_opcode(OpCode::OpLess);
                        self.emit_opcode(OpCode::OpNot);
                    }
                    TokenType::Less => self.emit_opcode(OpCode::OpLess),
                    TokenType::LessEqual => {
                        self.emit_opcode(OpCode::OpGreater);
                        self.emit_opcode(OpCode::OpNot);
                    }
                    TokenType::Plus => self.emit_opcode(OpCode::OpAdd),
                    TokenType::Minus => self.emit_opcode(OpCode::OpSubtract),
                    TokenType::Star => self.emit_opcode(OpCode::OpMultiply),
                    TokenType::Slash => self.emit_opcode(OpCode::OpDivide),
                    _ => {}
                }
            }
        }
    }

    fn parse_precedence(&mut self, precedence: Precedence) {
        self.advance();
        if let Some(prefix) = self.get_prev_rule().and_then(|rule| rule.prefix.as_ref()) {
            prefix(self);

            while let Some(_) = self.get_current_rule().filter(|rule| precedence <= rule.precedence) {
                self.advance();
                if let Some(infix) = self.get_prev_rule().and_then(|rule| rule.infix.as_ref()) {
                    infix(self);
                }
            }
        } else {
            self.error_at_current("Expect expression.");
        }
    }

    fn get_rule(token_type: TokenType) -> Option<&'static ParseRule> {
        let rule = RULES.get(&token_type);
        return rule;
    }

    fn get_current_rule(&self) -> Option<&'static ParseRule> {
        if let Some(ref token) = self.current {
            Self::get_rule(token.token_type)
        } else {
            None
        }
    }

    fn get_prev_rule(&self) -> Option<&'static ParseRule> {
        if let Some(ref token) = self.previous {
            let r = Self::get_rule(token.token_type);
            return r;
        } else {
            None
        }
    }

    fn make_constant(&mut self, value: Value) -> u8 {
        let constant = self.chunk.add_constant(value);
        if constant > (u8::MAX as u32) {
            self.error("Too many constants in one chunk.");
            return 0;
        }
        return constant as u8;
    }

    pub fn end_compiler(&mut self) {
        self.chunk.write_opcode(OpCode::OpReturn, self.current.unwrap().line);
    }

    fn print_statement(&mut self) {
        self.expression();
        self.consume(TokenType::Semicolon, "Expect ';' after value.");
        self.emit_opcode(OpCode::OpPrint);
    }
    pub fn statement(&mut self) {
        if self.match_token(TokenType::Print) {
            self.print_statement();
        } else {
            self.expression_statement();
        }
    }

    fn expression_statement(&mut self) {
        self.expression();
        self.consume(TokenType::Semicolon, "Expect ';' after expression.");
        self.emit_opcode(OpCode::OpPop);
    }

    fn identifier_constant(&mut self, name: Token) -> u8 {
        let mut identifier = self.scanner.source[name.start as usize..name.start as usize + name.length as usize].to_string();
        identifier.remove(0);
        identifier.remove(identifier.len() - 1);
        return self.make_constant(Value::StringValue(Rc::new(identifier)));
    }

    fn parse_variable(&mut self, error_message: &str) -> u8 {
        self.consume(TokenType::Identifier, error_message);
        return self.identifier_constant(self.previous.unwrap());
    }

    fn define_variable(&mut self, global: u8) {
        self.emit_opcode(OpCode::OpDefineGlobal);
        self.emit_byte(global);
    }

    fn var_declaration(&mut self) {
        let global = self.parse_variable("Expect variable name.");
        if self.match_token(TokenType::Equal) {
            self.expression();
        } else {
            self.emit_opcode(OpCode::OpNil);
        }

        self.consume(TokenType::Semicolon, "Expect ';' after variable declaration.");
        self.define_variable(global);
    }

    pub fn declaration(&mut self) {
        if self.match_token(TokenType::Var) {
            self.var_declaration();
        } else {
            self.statement();
        }

        if self.panic_mode {
            self.synchronize();
        }
    }

    fn synchronize(&mut self) {
        self.panic_mode = false;
        loop {
            match self.current {
                Some(ref token) if token.token_type != TokenType::Eof => {
                    if let Some(ref pre) = self.previous {
                        if pre.token_type == TokenType::Semicolon {
                            return;
                        }
                    }

                    match token.token_type {
                        TokenType::Class
                        | TokenType::Fun
                        | TokenType::Var
                        | TokenType::For
                        | TokenType::If
                        | TokenType::While
                        | TokenType::Print
                        | TokenType::Return => { return; }
                        _ => {}
                    }
                }
                _ => {}
            }
            self.advance();
        }
    }
}

