use std::cell::{Cell, RefCell};
use std::collections::HashMap;
use std::env::var;
use std::mem::replace;
use std::rc::Rc;

use lazy_static::lazy_static;
use num_enum::{IntoPrimitive, TryFromPrimitive};

use crate::chunk::{Chunk, OpCode};
use crate::compiler::scanner::{Scanner, Token, TokenType};
use crate::vm::function::{Function, FunctionType};
use crate::vm::value::Value;

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

type ParseFn = fn(&mut Parser, bool);

struct ParseRule {
    prefix: Option<ParseFn>,
    infix: Option<ParseFn>,
    precedence: Precedence,
}

enum Variable {
    GlobalVariable(u8),
    LocalVariable(u8),
}

#[derive(Clone)]
struct Local {
    name: String,
    index: i32,
    depth: i32,
}

impl Local {
    fn new() -> Local {
        Local {
            name: "".to_string(),
            index: -1,
            depth: -1,
        }
    }
}

struct UpValue {
    index: u8,
    is_local: bool,
}

pub struct Compiler {
    enclosing: Option<Box<Compiler>>,
    pub function: Function,
    locals: Vec<Local>,
    upvalues: Vec<UpValue>,
    scope_depth: i32,
}

impl Compiler {
    pub(crate) fn add_upvalue(&mut self, index: u8, is_local: bool) -> usize {
        self.upvalues.push(UpValue {
            index,
            is_local,
        });
        return self.upvalues.len() - 1;
    }
}

impl Compiler {
    fn new(chunk: Chunk,
           enclosing: Option<Box<Compiler>>,
           function_type: FunctionType) -> Compiler {
        let mut compiler = Compiler {
            enclosing,
            function: Function::new(0, chunk, function_type),
            // allocate u8::max Locals to locals
            locals: Vec::with_capacity(u8::MAX as usize),
            upvalues: Vec::with_capacity(u8::MAX as usize),
            scope_depth: 0,
        };
        let mut local = Local::new();
        local.depth = 0;
        local.index = 0;
        compiler.locals.push(local);
        return compiler;
    }
}

// this is a map of token types to parse rules
lazy_static! {
    static ref RULES: HashMap<TokenType,ParseRule> = {
        let mut m = HashMap::new();
        m.insert(TokenType::LeftParen, ParseRule {
            prefix: Some(Parser::grouping),
            infix: Some(Parser::call),
            precedence: Precedence::Call,
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
            infix: Some(Parser::and),
            precedence: Precedence::And,
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
            infix: Some(Parser::or),
            precedence: Precedence::Or,
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

struct ParserError {
    pub had_error: bool,
    panic_mode: bool,
}

pub struct Parser {
    current: Option<Token>,
    previous: Option<Token>,
    scanner: Scanner,
    pub compiler: Box<Compiler>,
    error: RefCell<ParserError>,
}

impl Parser {
    pub(crate) fn current_function(self) -> Function {
        self.compiler.function
    }
}

impl Parser {
    pub(crate) fn had_error(&self) -> bool {
        self.error.borrow().had_error
    }
}

impl Parser {
    pub fn new(scanner: Scanner, chunk: Chunk) -> Parser {
        Parser {
            current: None,
            previous: None,
            compiler: Box::new(Compiler::new(chunk, None, FunctionType::SCRIPT)),
            scanner,
            error: RefCell::new(ParserError {
                had_error: false,
                panic_mode: false,
            }),
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

    fn literal(&mut self, _: bool) {
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

    fn argument_list(&mut self) -> u8 {
        let mut arg_count = 0;
        if !self.check(TokenType::RightParen) {
            loop {
                self.expression();
                if arg_count == 255 {
                    self.error_at_current("Can't have more than 255 arguments.");
                }
                arg_count += 1;
                if !self.match_token(TokenType::Comma) {
                    break;
                }
            }
        }
        self.consume(TokenType::RightParen, "Expect ')' after arguments.");
        arg_count
    }

    pub fn call(&mut self, can_assign: bool) {
        let arg_count = self.argument_list();
        self.emit_opcode(OpCode::OpCall);
        self.emit_byte(arg_count);
    }

    pub fn variable(&mut self, can_assign: bool) {
        let name = self.get_token_name(self.previous.as_ref().unwrap()).to_string();
        self.named_variable(&name, can_assign);
    }

    pub fn and(&mut self, _can_assign: bool) {
        let end_jump = self.emit_jump(OpCode::OpJumpIfFalse);
        self.emit_opcode(OpCode::OpPop);
        self.parse_precedence(Precedence::And);
        self.patch_jump(end_jump);
    }

    pub fn or(&mut self, _can_assign: bool) {
        let else_jump = self.emit_jump(OpCode::OpJumpIfFalse);
        let end_jump = self.emit_jump(OpCode::OpJump);
        self.patch_jump(else_jump);
        self.emit_opcode(OpCode::OpPop);
        self.parse_precedence(Precedence::Or);
        self.patch_jump(end_jump);
    }


    fn named_variable(&mut self, name: &str, can_assign: bool) -> Result<(), String> {
        let (arg, get_op, set_op) = match Self::resolve_local(&self.compiler, name)? {
            Some(local) => {
                (local.index as u8, OpCode::OpGetLocal, OpCode::OpSetLocal)
            }
            None => {
                if let Some(upvalue) = self.resolve_upvalue(name)? {
                    (upvalue as u8, OpCode::OpGetUpvalue, OpCode::OpSetUpvalue)
                } else {
                    (self.identifier_constant(name.to_string()), OpCode::OpGetGlobal, OpCode::OpSetGlobal)
                }
            }
        };

        if can_assign && self.match_token(TokenType::Equal) {
            self.expression();
            self.emit_opcode(set_op);
            self.emit_byte(arg);
        } else {
            self.emit_opcode(get_op);
            self.emit_byte(arg);
        }
        Ok(())
    }

    fn resolve_upvalue(&mut self, name: &str) -> Result<Option<u8>, String> {
        match self.compiler.enclosing {
            None => Ok(None),
            Some(ref mut enclosing) => {
                match Self::resolve_local(enclosing, name)? {
                    Some(local) => {
                        let index = local.index as u8;
                        Self::add_upvalue(&mut self.compiler, index, true).map(|x| Some(x))
                    }
                    None => {
                        Self::resolve_compiler_upvalue(enclosing, name)
                    }
                }
            }
        }
    }

    fn resolve_compiler_upvalue(compiler: &mut Compiler, name: &str) -> Result<Option<u8>, String> {
        match Self::resolve_local(compiler, name)? {
            Some(local) => {
                let index = local.index as u8;
                Self::add_upvalue(compiler, index, true).map(|x| Some(x))
            }
            None => {
                match compiler.enclosing {
                    None => Ok(None),
                    Some(ref mut enclosing) =>
                        Self::resolve_compiler_upvalue(enclosing, name)
                }
            }
        }
    }

    fn add_upvalue(compiler: &mut Compiler, index: u8, is_local: bool) -> Result<u8, String> {
        match compiler.upvalues.iter().rev().find(|upvalue| upvalue.index == index && upvalue.is_local == is_local) {
            Some(upvalue) => Ok(upvalue.index),
            None =>
                if compiler.upvalues.len() == u8::MAX as usize {
                    Err("Too many closure variables in functions.".to_string())
                } else {
                    Ok(compiler.add_upvalue(index, is_local) as u8)
                }
        }
    }

    fn resolve_local<'a>(compiler: &'a Compiler, name: &str) -> Result<Option<&'a Local>, String> {
        for local in compiler.locals.iter().rev() {
            if local.name == name {
                if local.depth == -1 {
                    return Err("Cannot read local variable in its own initializer.".to_string());
                }
                return Ok(Some(local));
            }
        }
        return Ok(None);
    }

    fn error_at_current(&mut self, message: &str) {
        let token: Token = self.current.as_ref().unwrap().clone();
        self.error_at(&token, message);
    }

    fn error(&self, message: &str) {
        let token = self.previous.as_ref().unwrap();
        self.error_at(&token, message);
    }

    fn error_at(&self, token: &Token, message: &str) {
        eprintln!("[line {}] Error", token.line);
        if token.token_type == TokenType::Eof {
            eprint!(" at end");
        } else if token.token_type == TokenType::Error {} else {
            eprint!(" at '{}'", &self.scanner.source[(token.start as usize)..(token.start + token.length) as usize]);
        }
        eprintln!(": {}", message);
        self.error.borrow_mut().had_error = true;
    }

    fn emit_return(&mut self) {
        self.emit_opcode(OpCode::OpNil);
        self.emit_opcode(OpCode::OpReturn);
    }

    fn emit_opcode(&mut self, opcode: OpCode) {
        let line = self.previous_line();
        self.chunk().write_opcode(opcode, line);
    }

    fn emit_byte(&mut self, byte: u8) {
        let line = self.previous_line();
        self.chunk().write_chunk(byte, line);
    }

    fn emit_constant(&mut self, value: Value) {
        self.emit_opcode(OpCode::OpConstant);
        let b = self.make_constant(value);
        let line = self.previous_line();
        self.chunk().write_chunk(b, line);
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

    pub fn string(&mut self, _: bool) {
        if let Some(ref token) = self.previous {
            let mut value = self.scanner.source[token.start as usize..token.start as usize + token.length as usize].to_string();
            value.remove(value.len() - 1);
            value.remove(0);
            self.emit_constant(Value::StringValue(value));
        }
    }

    pub fn number(&mut self, _: bool) {
        if let Some(ref token) = self.previous {
            let value = self.scanner.source[token.start as usize..token.start as usize + token.length as usize]
                .parse::<f64>()
                .unwrap();
            self.emit_constant(Value::Number(value));
        }
    }

    fn grouping(&mut self, _: bool) {
        self.expression();
        self.consume(TokenType::RightParen, "Expect ')' after expression.");
    }

    fn unary(&mut self, _: bool) {
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

    fn binary(&mut self, _: bool) {
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
            let can_assign = precedence <= Precedence::Assignment;
            prefix(self, can_assign);

            while let Some(_) = self.get_current_rule().filter(|rule| precedence <= rule.precedence) {
                self.advance();
                if let Some(infix) = self.get_prev_rule().and_then(|rule| rule.infix.as_ref()) {
                    infix(self, can_assign);
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
        let constant = self.chunk().add_constant(value);
        if constant > (u8::MAX as usize) {
            self.error("Too many constants in one chunk().");
            return 0;
        }
        return constant as u8;
    }

    fn begin_enclosing(&mut self, function_type: FunctionType) {
        let enclosing = replace(&mut self.compiler, Box::new(Compiler::new(Chunk::new(),
                                                                           None,
                                                                           function_type)));
        self.compiler.enclosing = Some(enclosing);
        if function_type != FunctionType::SCRIPT {
            let token = self.previous.as_ref().unwrap();
            self.compiler.function.name = self.copy_string(token.start as usize,
                                                           token.length as usize);
        }
    }

    fn end_enclosing(&mut self) -> Function {
        let line = self.current.unwrap().line;
        self.emit_return();
        match self.compiler.enclosing.take() {
            Some(enclosing) => {
                let compiler = replace(&mut self.compiler, enclosing);
                compiler.function
            }
            _ => {
                panic!("Compiler is not enclosed.");
            }
        }
    }

    pub fn end_compiler(&mut self) {
        self.emit_return();
    }

    fn print_statement(&mut self) {
        self.expression();
        self.consume(TokenType::Semicolon, "Expect ';' after value.");
        self.emit_opcode(OpCode::OpPrint);
    }

    pub fn statement(&mut self) {
        if self.match_token(TokenType::Print) {
            self.print_statement();
        } else if self.match_token(TokenType::LeftBrace) {
            self.begin_scope();
            self.block();
            self.end_scope();
        } else if self.match_token(TokenType::If) {
            self.if_statement();
        } else if self.match_token(TokenType::Return) {
            self.return_statement();
        } else if self.match_token(TokenType::While) {
            self.while_statement();
        } else if self.match_token(TokenType::For) {
            self.for_statement();
        } else {
            self.expression_statement();
        }
    }

    fn return_statement(&mut self) {
        if self.compiler.function.function_type == FunctionType::SCRIPT {
            self.error("Can't return from top-level code.");
        }

        if self.match_token(TokenType::Semicolon) {
            self.emit_return();
        } else {
            self.expression();
            self.consume(TokenType::Semicolon, "Expect ';' after return value.");
            self.emit_opcode(OpCode::OpReturn);
        }
    }

    fn for_statement(&mut self) {
        self.begin_scope();
        self.consume(TokenType::LeftParen, "Expect '(' after 'for'.");
        if self.match_token(TokenType::Semicolon) {
            // no initializer
        } else if self.match_token(TokenType::Var) {
            self.var_declaration();
        } else {
            self.expression_statement();
        }

        let mut loop_start = self.chunk().count;
        let mut exit_jump = None;
        if !self.match_token(TokenType::Semicolon) {
            self.expression();
            self.consume(TokenType::Semicolon, "Expect ';' after loop condition.");

            // Jump out of the loop if the condition is false.
            exit_jump = Some(self.emit_jump(OpCode::OpJumpIfFalse));
            self.emit_opcode(OpCode::OpPop);
        }

        if !self.match_token(TokenType::RightParen) {
            let body_jump = self.emit_jump(OpCode::OpJump);
            let increment_start = self.chunk().count;
            self.expression();
            self.emit_opcode(OpCode::OpPop);
            self.consume(TokenType::RightParen, "Expect ')' after for clauses.");

            self.emit_loop(loop_start);
            loop_start = increment_start;
            self.patch_jump(body_jump);
        }

        self.statement();

        self.emit_loop(loop_start);

        if let Some(exit_jump) = exit_jump {
            self.patch_jump(exit_jump);
            self.emit_opcode(OpCode::OpPop);
        }

        self.end_scope();
    }

    fn while_statement(&mut self) {
        let loop_start = self.chunk().count;
        self.consume(TokenType::LeftParen, "Expect '(' after 'while'.");
        self.expression();
        self.consume(TokenType::RightParen, "Expect ')' after condition.");

        let exit_jump = self.emit_jump(OpCode::OpJumpIfFalse);
        self.emit_opcode(OpCode::OpPop);
        self.statement();
        self.emit_loop(loop_start);

        self.patch_jump(exit_jump);
        self.emit_opcode(OpCode::OpPop);
    }

    fn emit_loop(&mut self, loop_start: usize) {
        self.emit_opcode(OpCode::OpLoop);
        let offset = self.chunk().count - loop_start + 2;
        self.emit_byte((offset >> 8) as u8);
        self.emit_byte((offset & 0xff) as u8);
    }

    fn if_statement(&mut self) {
        self.consume(TokenType::LeftParen, "Expect '(' after 'if'.");
        self.expression();
        self.consume(TokenType::RightParen, "Expect ')' after condition.");

        let then_jump = self.emit_jump(OpCode::OpJumpIfFalse);
        self.emit_opcode(OpCode::OpPop);
        self.statement();
        let else_jump = self.emit_jump(OpCode::OpJump);

        self.patch_jump(then_jump);
        self.emit_opcode(OpCode::OpPop);

        if self.match_token(TokenType::Else) {
            self.statement();
        }
        self.patch_jump(else_jump);
    }

    fn emit_jump(&mut self, instruction: OpCode) -> i32 {
        self.emit_opcode(instruction);
        self.emit_byte(0xff);
        self.emit_byte(0xff);
        return (self.chunk().count - 2) as i32;
    }

    fn patch_jump(&mut self, offset: i32) {
        let jump = (self.chunk().count as i32) - offset - 2;
        if jump > (u16::MAX as i32) {
            self.error("Too much code to jump over.");
        }
        self.chunk().code[offset as usize] = ((jump >> 8) & 0xff) as u8;
        self.chunk().code[(offset + 1) as usize] = (jump & 0xff) as u8;
    }

    fn begin_scope(&mut self) {
        self.compiler.scope_depth += 1;
    }

    fn end_scope(&mut self) {
        self.compiler.scope_depth -= 1;
        let scope_depth = self.compiler.scope_depth;

        while let Some(last) = self.compiler.locals.last() {
            if last.depth > scope_depth {
                self.emit_opcode(OpCode::OpPop);
                self.compiler.locals.pop();
            } else {
                break;
            }
        }
    }

    fn block(&mut self) {
        while !self.check(TokenType::RightBrace) && !self.check(TokenType::Eof) {
            self.declaration();
        }
        self.consume(TokenType::RightBrace, "Expect '}' after block.");
    }

    fn expression_statement(&mut self) {
        self.expression();
        self.consume(TokenType::Semicolon, "Expect ';' after expression.");
        self.emit_opcode(OpCode::OpPop);
    }

    fn identifier_constant(&mut self, name: String) -> u8 {
        return self.make_constant(Value::StringValue(name));
    }

    fn parse_variable(&mut self, error_message: &str) -> Variable {
        self.consume(TokenType::Identifier, error_message);
        self.declare_variable();
        if self.compiler.scope_depth > 0 {
            return Variable::LocalVariable((self.compiler.locals.len() - 1) as u8);
        }
        return Variable::GlobalVariable(self.identifier_constant(self.get_token_name(self.previous.as_ref().unwrap()).to_string()));
    }

    fn declare_variable(&mut self) {
        if self.compiler.scope_depth == 0 {
            return;
        }

        if let Some(token) = self.previous {
            let token_name = self.scanner.source.get(token.start as usize..(token.start + token.length) as usize).unwrap();
            for local in self.compiler.locals.iter().rev() {
                if local.depth != -1 && local.depth < self.compiler.scope_depth {
                    break;
                }

                if token_name == local.name {
                    self.error("Already variable with this name in this scope.");
                }
            }
            self.add_local(token_name.to_string());
        }
    }

    fn copy_string(&self, start: usize, length: usize) -> String {
        self.scanner.source.get(start..(start + length)).unwrap().to_string()
    }

    fn add_local(&mut self, name: String) {
        if self.compiler.locals.len() == u8::MAX as usize {
            self.error("Too many local variables in function.");
            return;
        }

        let mut local = Local::new();
        local.name = name;
        local.depth = -1;
        local.index = self.compiler.locals.len() as i32;
        self.compiler.locals.push(local);
    }

    fn define_variable(&mut self, variable: Variable) {
        match variable {
            Variable::GlobalVariable(global) => {
                self.emit_opcode(OpCode::OpDefineGlobal);
                self.emit_byte(global);
            }
            Variable::LocalVariable(local) => {
                self.mark_initialized();
            }
        }
    }

    fn mark_initialized(&mut self) {
        if self.compiler.scope_depth == 0 {
            return;
        }
        if let Some(last) = self.compiler.locals.last_mut() {
            last.depth = self.compiler.scope_depth;
        }
    }

    fn var_declaration(&mut self) {
        let variable = self.parse_variable("Expect variable name.");
        if self.match_token(TokenType::Equal) {
            self.expression();
        } else {
            self.emit_opcode(OpCode::OpNil);
        }

        self.consume(TokenType::Semicolon, "Expect ';' after variable declaration.");
        self.define_variable(variable);
    }

    pub fn declaration(&mut self) {
        if self.match_token(TokenType::Fun) {
            self.fun_declaration();
        } else if self.match_token(TokenType::Var) {
            self.var_declaration();
        } else {
            self.statement();
        }

        if self.error.borrow().panic_mode {
            self.synchronize();
        }
    }

    fn fun_declaration(&mut self) {
        let variable = self.parse_variable("Expect function name.");
        self.mark_initialized();
        self.function(FunctionType::FUNCTION);
        self.define_variable(variable);
    }

    fn function(&mut self, function_type: FunctionType) {
        self.begin_enclosing(function_type);
        self.begin_scope();


        self.consume(TokenType::LeftParen, "Expect '(' after function name.");
        if !self.check(TokenType::RightParen) {
            loop {
                self.compiler.function.arity += 1;
                if self.compiler.function.arity > 255 {
                    self.error_at_current("Can't have more than 255 parameters.");
                }
                let constant = self.parse_variable("Expect parameter name.");
                self.define_variable(constant);
                if !self.match_token(TokenType::Comma) {
                    break;
                }
            }
        }
        self.consume(TokenType::RightParen, "Expect ')' after parameters.");
        self.consume(TokenType::LeftBrace, "Expect '{' before function body.");

        self.block();
        let function = self.end_enclosing();
        let const_num = self.make_constant(Value::FunctionValue(Rc::new(function)));
        self.emit_opcode(OpCode::OpClosure);
        self.emit_byte(const_num);
    }

    fn synchronize(&mut self) {
        self.error.borrow_mut().panic_mode = false;
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

    fn chunk(&mut self) -> &mut Chunk {
        &mut self.compiler.function.chunk
    }

    fn get_token_name(&self, token: &Token) -> &str {
        self.scanner.source.get(token.start as usize..(token.start + token.length) as usize).unwrap()
    }
}

fn identifiers_equal(source: &str, a: &Token, b: &Token) -> bool {
    if a.length != b.length {
        return false;
    }

    let a_name = source.get(a.start as usize..(a.start + a.length) as usize);
    let b_name = source.get(b.start as usize..(b.start + b.length) as usize);
    match (a_name, b_name) {
        (Some(a_name), Some(b_name)) => {
            return a_name == b_name;
        }
        _ => {}
    }
    return false;
}

