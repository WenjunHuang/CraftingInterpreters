use std::cell::{Cell, RefCell};
use std::collections::HashMap;
use std::fmt::Write;
use std::mem;
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

type ParseFn = fn(&mut Parser, bool) -> ParserResult;

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

#[derive(Eq, PartialEq)]
enum UpValue {
    LocalVariable(u8),
    UpValue(u8),
}

pub struct Compiler {
    enclosing: Option<Box<Compiler>>,
    pub function: Function,
    locals: Vec<Local>,
    up_values: Vec<UpValue>,
    scope_depth: i32,
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
            up_values: Vec::with_capacity(u8::MAX as usize),
            scope_depth: 0,
        };
        let mut local = Local::new();
        local.depth = 0;
        local.index = 0;
        if function_type == FunctionType::METHOD || function_type == FunctionType::INITIALIZER {
            local.name = "this".to_string();
        }
        compiler.locals.push(local);
        return compiler;
    }
    fn add_upvalue(&mut self, up_value: UpValue) -> u8 {
        self.up_values.push(up_value);
        self.function.upvalue_count = self.up_values.len();
        (self.function.upvalue_count - 1) as u8
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
            infix: Some(Parser::dot),
            precedence: Precedence::Call,
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
            prefix: Some(Parser::super_),
            infix:None,
            precedence: Precedence::None,
        });
        m.insert(TokenType::This,ParseRule{
            prefix: Some(Parser::this),
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

pub struct ClassCompiler {
    pub enclosing: Option<Box<ClassCompiler>>,
    pub has_superclass: bool,
}

impl ClassCompiler {
    pub fn new() -> ClassCompiler {
        ClassCompiler {
            enclosing: None,
            has_superclass: false,
        }
    }
}

struct ParserError {
    pub had_error: bool,
    panic_mode: bool,
}

type ParserResult = Result<(), String>;

pub struct Parser {
    current: Option<Token>,
    previous: Option<Token>,
    scanner: Scanner,
    pub compiler: Box<Compiler>,
    pub class_compiler: Option<Box<ClassCompiler>>,
    error: RefCell<ParserError>,
}

impl Parser {
    pub(crate) fn current_function(self) -> Function {
        self.compiler.function
    }
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
            class_compiler: None,
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

    pub fn consume(&mut self, token_type: TokenType, message: &str) -> ParserResult {
        match self.current {
            Some(ref t) if t.token_type == token_type => {
                self.advance();
                return Ok(());
            }
            _ => {}
        }

        Err(self.error_at_current(message))
    }

    fn super_(&mut self, _: bool) -> ParserResult {
        match self.class_compiler.as_ref() {
            None => {
                return Err(self.error_at_current("Can't use 'super' outside of a class."));
            }
            Some(cc) if !cc.has_superclass => {
                return Err(self.error_at_current("Can't use 'super' in a class with no super class."));
            }
            _ => {}
        }

        self.consume(TokenType::Dot, "Expect '.' after 'super'.")?;
        self.consume(TokenType::Identifier, "Expect superclass method name.")?;

        let name = self.make_string_constant(self.get_token_name(self.get_previous()?).to_string())?;

        self.named_variable("this", false)?;
        if self.match_token(TokenType::LeftParen) {
            let arg_count = self.argument_list()?;
            self.named_variable("super", false)?;
            self.emit_opcode(OpCode::OpSuperInvoke);
            self.emit_byte(name);
            self.emit_byte(arg_count);
        } else {
            self.named_variable("super", false)?;
            self.emit_opcode(OpCode::OpGetSuper);
            self.emit_byte(name);
        }
        Ok(())
    }

    fn this(&mut self, _: bool) -> ParserResult {
        if self.class_compiler.is_none() {
            return Err(self.error_at_current("Can't use 'this' outside of a class."));
        }
        self.variable(false)?;
        Ok(())
    }

    fn literal(&mut self, _: bool) -> ParserResult {
        let token = self.previous.as_ref().unwrap();
        match token.token_type {
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
        Ok(())
    }

    fn argument_list(&mut self) -> Result<u8, String> {
        let mut arg_count = 0;
        if !self.check(TokenType::RightParen) {
            loop {
                self.expression()?;
                if arg_count == 255 {
                    return Err(self.error_at_current("Can't have more than 255 arguments."));
                }
                arg_count += 1;
                if !self.match_token(TokenType::Comma) {
                    break;
                }
            }
        }
        self.consume(TokenType::RightParen, "Expect ')' after arguments.")?;
        Ok(arg_count)
    }

    pub fn dot(&mut self, can_assign: bool) -> ParserResult {
        self.consume(TokenType::Identifier, "Expect property name after '.'.")?;
        let name = self.make_string_constant(self.get_token_name(self.get_previous()?).to_string())?;
        if can_assign && self.match_token(TokenType::Equal) {
            self.expression()?;
            self.emit_opcode(OpCode::OpSetProperty);
            self.emit_byte(name);
        } else if self.match_token(TokenType::LeftParen) {
            let arg_count = self.argument_list()?;
            self.emit_opcode(OpCode::OpInvoke);
            self.emit_byte(name);
            self.emit_byte(arg_count);
        } else {
            self.emit_opcode(OpCode::OpGetProperty);
            self.emit_byte(name);
        }
        Ok(())
    }

    pub fn call(&mut self, can_assign: bool) -> ParserResult {
        let arg_count = self.argument_list()?;
        self.emit_opcode(OpCode::OpCall);
        self.emit_byte(arg_count);
        Ok(())
    }

    pub fn variable(&mut self, can_assign: bool) -> ParserResult {
        let name = self.get_token_name(self.previous.as_ref().unwrap()).to_string();
        self.named_variable(&name, can_assign)
    }

    pub fn and(&mut self, _can_assign: bool) -> ParserResult {
        let end_jump = self.emit_jump(OpCode::OpJumpIfFalse);
        self.emit_opcode(OpCode::OpPop);
        self.parse_precedence(Precedence::And)?;
        self.patch_jump(end_jump);
        Ok(())
    }

    pub fn or(&mut self, _can_assign: bool) -> ParserResult {
        let else_jump = self.emit_jump(OpCode::OpJumpIfFalse);
        let end_jump = self.emit_jump(OpCode::OpJump);
        self.patch_jump(else_jump);
        self.emit_opcode(OpCode::OpPop);
        self.parse_precedence(Precedence::Or)?;
        self.patch_jump(end_jump);
        Ok(())
    }


    fn named_variable(&mut self, name: &str, can_assign: bool) -> ParserResult {
        let (arg, get_op, set_op) = match Self::resolve_local(&mut self.compiler, name)? {
            Some(local) => {
                (local.index as u8, OpCode::OpGetLocal, OpCode::OpSetLocal)
            }
            None => {
                if let Some(upvalue) = self.resolve_upvalue(name)? {
                    (upvalue, OpCode::OpGetUpValue, OpCode::OpSetUpValue)
                } else {
                    (self.make_string_constant(name.to_string())?, OpCode::OpGetGlobal, OpCode::OpSetGlobal)
                }
            }
        };

        if can_assign && self.match_token(TokenType::Equal) {
            self.expression()?;
            self.emit_opcode(set_op);
            self.emit_byte(arg);
        } else {
            self.emit_opcode(get_op);
            self.emit_byte(arg);
        }
        Ok(())
    }

    fn resolve_upvalue(&mut self, name: &str) -> Result<Option<u8>, String> {
        Self::resolve_compiler_upvalue(&mut self.compiler, name)
    }

    fn resolve_compiler_upvalue(compiler: &mut Compiler, name: &str) -> Result<Option<u8>, String> {
        match compiler.enclosing {
            None => Ok(None),
            Some(ref mut enclosing) => {
                match Self::resolve_local(enclosing, name)? {
                    Some(local) => {
                        let index = local.index as u8;
                        Self::add_upvalue(compiler, UpValue::LocalVariable(index)).map(|x| Some(x))
                    }
                    None => {
                        match Self::resolve_compiler_upvalue(enclosing, name)? {
                            Some(idx) => Self::add_upvalue(compiler, UpValue::UpValue(idx)).map(|x| Some(x)),
                            None => Ok(None)
                        }
                    }
                }
            }
        }
    }

    fn add_upvalue(compiler: &mut Compiler, up_value: UpValue) -> Result<u8, String> {
        match compiler.up_values.iter().enumerate().find(|(index, item)| up_value == **item) {
            Some((idx, _)) => Ok(idx as u8),
            None =>
                if compiler.up_values.len() == u8::MAX as usize {
                    Err("Too many closure variables in functions.".to_string())
                } else {
                    Ok(compiler.add_upvalue(up_value))
                }
        }
    }

    fn resolve_local<'a>(compiler: &'a mut Compiler, name: &str) -> Result<Option<&'a mut Local>, String> {
        for local in compiler.locals.iter_mut().rev() {
            if local.name == name {
                if local.depth == -1 {
                    return Err("Cannot read local variable in its own initializer.".to_string());
                }
                return Ok(Some(local));
            }
        }
        return Ok(None);
    }

    fn error_at_current(&mut self, message: &str) -> String {
        let token: Token = self.current.as_ref().unwrap().clone();
        self.error_at(&token, message)
    }

    fn error(&self, message: &str) -> String {
        let token = self.previous.as_ref().unwrap();
        self.error_at(&token, message)
    }

    fn error_at(&self, token: &Token, message: &str) -> String {
        let mut msg = String::new();
        writeln!(msg, "[line {}] Error", token.line).unwrap();
        writeln!(msg, "[line {}] Error", token.line).unwrap();
        if token.token_type == TokenType::Eof {
            writeln!(msg, " at end").unwrap();
        } else if token.token_type == TokenType::Error {} else {
            writeln!(msg, " at '{}'", &self.scanner.source[(token.start as usize)..(token.start + token.length) as usize]).unwrap();
        }
        writeln!(msg, ": {}", message).unwrap();
        msg
    }

    fn emit_return(&mut self) {
        if self.compiler.function.function_type == FunctionType::INITIALIZER {
            self.emit_opcode(OpCode::OpGetLocal);
            self.emit_byte(0);
        } else {
            self.emit_opcode(OpCode::OpNil);
        }
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

    fn emit_constant(&mut self, value: Value) -> ParserResult {
        self.emit_opcode(OpCode::OpConstant);
        let b = self.make_constant(value)?;
        let line = self.previous_line();
        self.chunk().write_chunk(b, line);
        Ok(())
    }

    fn previous_line(&self) -> i32 {
        self.previous.as_ref().unwrap().line
    }

    pub fn expression(&mut self) -> ParserResult {
        self.parse_precedence(Precedence::Assignment)
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

    pub fn string(&mut self, _: bool) -> ParserResult {
        let token = self.previous.as_ref().unwrap();
        let mut value = self.scanner.source[token.start as usize..token.start as usize + token.length as usize].to_string();
        value.remove(value.len() - 1);
        value.remove(0);
        self.emit_constant(Value::StringValue(value))?;
        Ok(())
    }

    pub fn number(&mut self, _: bool) -> ParserResult {
        let token = self.previous.as_ref().unwrap();
        let value = self.scanner.source[token.start as usize..token.start as usize + token.length as usize]
            .parse::<f64>()
            .unwrap();
        self.emit_constant(Value::Number(value))?;
        Ok(())
    }

    fn grouping(&mut self, _: bool) -> ParserResult {
        self.expression()?;
        self.consume(TokenType::RightParen, "Expect ')' after expression.")?;
        Ok(())
    }

    fn unary(&mut self, _: bool) -> ParserResult {
        let token = self.previous.as_ref().unwrap();
        let operator_type = token.token_type;
        self.expression()?;

        match operator_type {
            TokenType::Minus => self.emit_opcode(OpCode::OpNegate),
            TokenType::Bang => self.emit_opcode(OpCode::OpNot),
            _ => {}
        }
        Ok(())
    }

    fn binary(&mut self, _: bool) -> ParserResult {
        let token = self.previous.as_ref().unwrap();
        let operator_type = token.token_type;
        let rule = RULES.get(&operator_type).unwrap();
        let precedence: u8 = rule.precedence.into();
        self.parse_precedence(Precedence::try_from(precedence + 1).unwrap())?;
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
        Ok(())
    }

    fn parse_precedence(&mut self, precedence: Precedence) -> ParserResult {
        self.advance();
        if let Some(prefix_fn) = self.get_prev_prefix_fn() {
            let can_assign = precedence <= Precedence::Assignment;
            prefix_fn(self, can_assign)?;

            while let Some(_) = self.get_current_rule().filter(|rule| precedence <= rule.precedence) {
                self.advance();
                if let Some(infix) = self.get_prev_infix_fn() {
                    infix(self, can_assign)?;
                }
            }
            Ok(())
        } else {
            Err(self.error_at_current("Expect expression."))
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

    fn get_prev_prefix_fn(&self) -> Option<&'static ParseFn> {
        self.get_prev_rule().and_then(|rule| rule.prefix.as_ref())
    }
    fn get_prev_infix_fn(&self) -> Option<&'static ParseFn> {
        self.get_prev_rule().and_then(|rule| rule.infix.as_ref())
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

    fn end_enclosing(&mut self) -> Result<Compiler, String> {
        let line = self.current.unwrap().line;
        self.emit_return();
        match self.compiler.enclosing.take() {
            Some(enclosing) => {
                let compiler = replace(&mut self.compiler, enclosing);
                Ok(*compiler)
            }
            _ => {
                Err(format!("Compiler is not enclosed. line: {}", line))
            }
        }
    }

    pub fn end_compiler(&mut self) {
        self.emit_return();
    }

    fn print_statement(&mut self) -> ParserResult {
        self.expression()?;
        self.consume(TokenType::Semicolon, "Expect ';' after value.")?;
        self.emit_opcode(OpCode::OpPrint);
        Ok(())
    }

    pub fn statement(&mut self) -> ParserResult {
        if self.match_token(TokenType::Print) {
            self.print_statement()?;
        } else if self.match_token(TokenType::LeftBrace) {
            self.begin_scope();
            self.block()?;
            self.end_scope();
        } else if self.match_token(TokenType::If) {
            self.if_statement()?;
        } else if self.match_token(TokenType::Return) {
            self.return_statement()?;
        } else if self.match_token(TokenType::While) {
            self.while_statement()?;
        } else if self.match_token(TokenType::For) {
            self.for_statement()?;
        } else {
            self.expression_statement()?;
        }
        Ok(())
    }

    fn return_statement(&mut self) -> ParserResult {
        if self.compiler.function.function_type == FunctionType::SCRIPT {
            return Err(self.error("Can't return from top-level code."));
        }

        if self.match_token(TokenType::Semicolon) {
            self.emit_return();
        } else {
            if self.compiler.function.function_type == FunctionType::INITIALIZER {
                return Err(self.error("Can't return a value from an initializer."));
            }

            self.expression()?;
            self.consume(TokenType::Semicolon, "Expect ';' after return value.")?;
            self.emit_opcode(OpCode::OpReturn);
        }
        Ok(())
    }

    fn for_statement(&mut self) -> ParserResult {
        self.begin_scope();
        self.consume(TokenType::LeftParen, "Expect '(' after 'for'.")?;
        if self.match_token(TokenType::Semicolon) {
            // no initializer
        } else if self.match_token(TokenType::Var) {
            self.var_declaration()?;
        } else {
            self.expression_statement()?;
        }

        let mut loop_start = self.chunk().count;
        let mut exit_jump = None;
        if !self.match_token(TokenType::Semicolon) {
            self.expression()?;
            self.consume(TokenType::Semicolon, "Expect ';' after loop condition.")?;

            // Jump out of the loop if the condition is false.
            exit_jump = Some(self.emit_jump(OpCode::OpJumpIfFalse));
            self.emit_opcode(OpCode::OpPop);
        }

        if !self.match_token(TokenType::RightParen) {
            let body_jump = self.emit_jump(OpCode::OpJump);
            let increment_start = self.chunk().count;
            self.expression()?;
            self.emit_opcode(OpCode::OpPop);
            self.consume(TokenType::RightParen, "Expect ')' after for clauses.")?;

            self.emit_loop(loop_start);
            loop_start = increment_start;
            self.patch_jump(body_jump);
        }

        self.statement()?;

        self.emit_loop(loop_start);

        if let Some(exit_jump) = exit_jump {
            self.patch_jump(exit_jump);
            self.emit_opcode(OpCode::OpPop);
        }

        self.end_scope();
        Ok(())
    }

    fn while_statement(&mut self) -> ParserResult {
        let loop_start = self.chunk().count;
        self.consume(TokenType::LeftParen, "Expect '(' after 'while'.")?;
        self.expression()?;
        self.consume(TokenType::RightParen, "Expect ')' after condition.")?;

        let exit_jump = self.emit_jump(OpCode::OpJumpIfFalse);
        self.emit_opcode(OpCode::OpPop);
        self.statement()?;
        self.emit_loop(loop_start);

        self.patch_jump(exit_jump);
        self.emit_opcode(OpCode::OpPop);
        Ok(())
    }

    fn emit_loop(&mut self, loop_start: usize) {
        self.emit_opcode(OpCode::OpLoop);
        let offset = self.chunk().count - loop_start + 2;
        self.emit_byte((offset >> 8) as u8);
        self.emit_byte((offset & 0xff) as u8);
    }

    fn if_statement(&mut self) -> ParserResult {
        self.consume(TokenType::LeftParen, "Expect '(' after 'if'.")?;
        self.expression()?;
        self.consume(TokenType::RightParen, "Expect ')' after condition.")?;

        let then_jump = self.emit_jump(OpCode::OpJumpIfFalse);
        self.emit_opcode(OpCode::OpPop);
        self.statement()?;
        let else_jump = self.emit_jump(OpCode::OpJump);

        self.patch_jump(then_jump);
        self.emit_opcode(OpCode::OpPop);

        if self.match_token(TokenType::Else) {
            self.statement()?;
        }
        self.patch_jump(else_jump);
        Ok(())
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

    fn block(&mut self) -> ParserResult {
        while !self.check(TokenType::RightBrace) && !self.check(TokenType::Eof) {
            self.declaration()?;
        }
        self.consume(TokenType::RightBrace, "Expect '}' after block.")?;
        Ok(())
    }

    fn expression_statement(&mut self) -> ParserResult {
        self.expression()?;
        self.consume(TokenType::Semicolon, "Expect ';' after expression.")?;
        self.emit_opcode(OpCode::OpPop);
        Ok(())
    }

    fn make_constant(&mut self, value: Value) -> Result<u8, String> {
        let constant = self.chunk().add_constant(value);
        if constant > (u8::MAX as usize) {
            Err(self.error("Too many constants in one chunk()."))
        } else {
            Ok(constant as u8)
        }
    }

    fn make_string_constant(&mut self, name: String) -> Result<u8, String> {
        self.make_constant(Value::StringValue(name))
    }

    fn parse_variable(&mut self, error_message: &str) -> Result<Variable, String> {
        self.consume(TokenType::Identifier, error_message)?;
        self.declare_variable()?;
        if self.compiler.scope_depth > 0 {
            return Ok(Variable::LocalVariable((self.compiler.locals.len() - 1) as u8));
        }
        let token_name = self.get_token_name(self.get_previous()?).to_string();
        return Ok(Variable::GlobalVariable(self.make_string_constant(token_name)?));
    }

    fn declare_variable(&mut self) -> ParserResult {
        if self.compiler.scope_depth == 0 {
            return Ok(());
        }

        let token = self.get_previous()?;
        let token_name = self.get_token_name(token);
        for local in self.compiler.locals.iter().rev() {
            if local.depth != -1 && local.depth < self.compiler.scope_depth {
                break;
            }

            if token_name == local.name {
                return Err(self.error("Already variable with this name in this scope."));
            }
        }
        self.add_local(token_name.to_string())?;
        Ok(())
    }

    fn copy_string(&self, start: usize, length: usize) -> String {
        self.scanner.source.get(start..(start + length)).unwrap().to_string()
    }

    fn add_local(&mut self, name: String) -> Result<u8, String> {
        if self.compiler.locals.len() == u8::MAX as usize {
            return Err(self.error("Too many local variables in function."));
        }

        let mut local = Local::new();
        local.name = name;
        local.depth = -1;
        local.index = self.compiler.locals.len() as i32;
        self.compiler.locals.push(local);
        return Ok((self.compiler.locals.len() - 1) as u8);
    }

    fn define_variable(&mut self, variable: Variable) {
        match variable {
            Variable::GlobalVariable(global) => {
                self.emit_opcode(OpCode::OpDefineGlobal);
                self.emit_byte(global);
            }
            Variable::LocalVariable(_) => {
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

    fn var_declaration(&mut self) -> ParserResult {
        let variable = self.parse_variable("Expect variable name.")?;
        if self.match_token(TokenType::Equal) {
            self.expression()?;
        } else {
            self.emit_opcode(OpCode::OpNil);
        }

        self.consume(TokenType::Semicolon, "Expect ';' after variable declaration.")?;
        self.define_variable(variable);
        Ok(())
    }

    pub fn declaration(&mut self) -> ParserResult {
        let result =
            if self.match_token(TokenType::Class) {
                self.class_declaration()
            } else if self.match_token(TokenType::Fun) {
                self.fun_declaration()
            } else if self.match_token(TokenType::Var) {
                self.var_declaration()
            } else {
                self.statement()
            };

        result
    }

    fn class_declaration(&mut self) -> ParserResult {
        self.consume(TokenType::Identifier, "Expect class name.")?;
        let class_name = self.get_token_name(self.get_previous()?).to_string();
        let name_constant = self.make_string_constant(class_name.clone())?;
        self.declare_variable()?;

        self.emit_opcode(OpCode::OpClass);
        self.emit_byte(name_constant);
        self.define_variable(Variable::GlobalVariable(name_constant));

        let mut class_compiler = ClassCompiler::new();
        class_compiler.enclosing = self.class_compiler.take();
        self.class_compiler = Some(Box::new(class_compiler));

        if self.match_token(TokenType::Less) {
            self.consume(TokenType::Identifier, "Expect superclass name.")?;
            self.variable(false)?;
            if class_name == self.get_token_name(self.get_previous()?) {
                return Err(self.error("A class can't inherit from itself."));
            }

            self.begin_scope();
            self.add_local("super".to_string())?;
            self.define_variable(Variable::LocalVariable(0));

            self.named_variable(&class_name, false)?;
            self.emit_opcode(OpCode::OpInherit);
            if let Some(class_compiler) = self.class_compiler.as_mut() {
                class_compiler.has_superclass = true;
            }
        }

        self.named_variable(&class_name, false)?;

        self.consume(TokenType::LeftBrace, "Expect '{' before class body.")?;
        while !self.check(TokenType::RightBrace) && !self.check(TokenType::Eof) {
            self.method()?;
        }
        self.consume(TokenType::RightBrace, "Expect '}' after class body.")?;

        self.emit_opcode(OpCode::OpPop);
        if self.class_compiler.as_ref().map_or_else(|| false, |c| c.has_superclass) {
            self.end_scope();
        }
        let enclosing = self.class_compiler.take().and_then(|c| c.enclosing);
        self.class_compiler = enclosing;
        Ok(())
    }

    fn method(&mut self) -> ParserResult {
        self.consume(TokenType::Identifier, "Expect method name.")?;
        let method_name = self.get_token_name(self.get_previous()?).to_string();
        let function_type = if method_name == "init" {
            FunctionType::INITIALIZER
        } else {
            FunctionType::METHOD
        };
        let constant = self.make_string_constant(method_name)?;
        self.function(function_type)?;
        self.emit_opcode(OpCode::OpMethod);
        self.emit_byte(constant);

        Ok(())
    }

    fn fun_declaration(&mut self) -> ParserResult {
        let variable = self.parse_variable("Expect function name.")?;
        self.mark_initialized();
        self.function(FunctionType::FUNCTION)?;
        self.define_variable(variable);
        Ok(())
    }

    fn function(&mut self, function_type: FunctionType) -> ParserResult {
        self.begin_enclosing(function_type);
        self.begin_scope();


        self.consume(TokenType::LeftParen, "Expect '(' after function name.")?;
        if !self.check(TokenType::RightParen) {
            loop {
                self.compiler.function.arity += 1;
                if self.compiler.function.arity > 255 {
                    self.error_at_current("Can't have more than 255 parameters.");
                }
                let constant = self.parse_variable("Expect parameter name.")?;
                self.define_variable(constant);
                if !self.match_token(TokenType::Comma) {
                    break;
                }
            }
        }
        self.consume(TokenType::RightParen, "Expect ')' after parameters.")?;
        self.consume(TokenType::LeftBrace, "Expect '{' before function body.")?;

        self.block()?;
        let mut compiler = self.end_enclosing()?;
        let upvalues = mem::take(&mut compiler.up_values);
        let const_num = self.make_constant(Value::FunctionValue(Rc::new(compiler.function)))?;

        self.emit_opcode(OpCode::OpClosure);
        self.emit_byte(const_num);
        for upvalue in upvalues {
            match upvalue {
                UpValue::LocalVariable(idx) => {
                    self.emit_byte(1);
                    self.emit_byte(idx);
                }
                UpValue::UpValue(idx) => {
                    self.emit_byte(0);
                    self.emit_byte(idx);
                }
            }
        }

        Ok(())
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

    fn get_previous(&self) -> Result<&Token, String> {
        match self.previous {
            Some(ref token) => Ok(token),
            None => Err("No previous token".to_string())
        }
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

