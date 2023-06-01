pub struct Scanner<'a> {
    start: i32,
    current: i32,
    line: i32,
    source: &'a str,
}

pub enum TokenType {
    LeftParam,
    RightParam,
    LeftBrace,
    RightBrace,
    Comma,
    Dot,
    Minus,
    Plus,
    Semicolon,
    Slash,
    Star,
    Bang,
    BangEqual,
    Equal,
    EqualEqual,
    Greater,
    GreaterEqual,
    Less,
    LessEqual,
    // Literals
    Identifier,
    String,
    Number,
    // keywords
    And,
    Class,
    Else,
    False,
    For,
    Fun,
    If,
    Nil,
    Or,
    Print,
    Return,
    Super,
    This,
    True,
    Var,
    While,
    Error,
    Eof,
}

pub struct Token<'a> {
    pub token_type: TokenType,
    pub start: i32,
    pub length: i32,
    pub line: i32,
    pub source: &'a str,
}

impl<'a> Scanner<'a> {
    pub fn new(source: &str) -> Scanner {
        Scanner {
            start: 0,
            current: 0,
            line: 1,
            source,
        }
    }


    pub fn scan_token(&mut self) -> Token {
        self.skip_whitespace();
        self.start = self.current;
        if self.is_at_end() {
            return self.make_token(TokenType::Eof);
        }

        let c = self.advance();
        return match c {
            '(' => self.make_token(TokenType::LeftParam),
            ')' => self.make_token(TokenType::RightParam),
            '{' => self.make_token(TokenType::LeftBrace),
            '}' => self.make_token(TokenType::RightBrace),
            ';' => self.make_token(TokenType::Semicolon),
            ',' => self.make_token(TokenType::Comma),
            '.' => self.make_token(TokenType::Dot),
            '-' => self.make_token(TokenType::Minus),
            '+' => self.make_token(TokenType::Plus),
            '/' => self.make_token(TokenType::Slash),
            '*' => self.make_token(TokenType::Star),
            '!' => {
                if self.match_char('=') {
                    self.make_token(TokenType::BangEqual)
                } else {
                    self.make_token(TokenType::Bang)
                }
            }
            '=' => {
                if self.match_char('=') {
                    self.make_token(TokenType::EqualEqual)
                } else {
                    self.make_token(TokenType::Equal)
                }
            }
            '<' => {
                if self.match_char('=') {
                    self.make_token(TokenType::LessEqual)
                } else {
                    self.make_token(TokenType::Less)
                }
            }
            '>' => {
                if self.match_char('=') {
                    self.make_token(TokenType::GreaterEqual)
                } else {
                    self.make_token(TokenType::Greater)
                }
            }
            '"' => { self.string() }
            n if n.is_digit(10) => { self.number() }
            a if self.is_alpha(a) => { self.identifier() }
            _ => self.error_token("Unexpected character.")
        };
    }

    fn identifier(&mut self) -> Token {
        while let Some(c) = self.peek() {
            if self.is_alpha(c) || c.is_digit(10) {
                self.advance();
            } else {
                break;
            }
        }
        return self.make_token(TokenType::Identifier);
    }

    fn is_alpha(&self, c: char) -> bool {
        c.is_ascii_alphabetic() || c == '_'
    }

    fn number(&mut self) -> Token {
        while let Some(c) = self.peek() {
            if c.is_digit(10) {
                self.advance();
            } else {
                break;
            }
        }
        if let Some('.') = self.peek() {
            if let Some(c) = self.peek_next() {
                if c.is_digit(10) {
                    self.advance();
                    while let Some(c) = self.peek() {
                        if c.is_digit(10) {
                            self.advance();
                        } else { break; }
                    }
                }
            }
        }
        return self.make_token(TokenType::Number);
    }

    fn string(&mut self) -> Token {
        while self.peek() != Some('"') && !self.is_at_end() {
            if self.peek() == Some('\n') {
                self.line += 1;
            }
            self.advance();
        }
        if self.is_at_end() {
            return self.error_token("Unterminated string.");
        }
        self.advance();
        return self.make_token(TokenType::String);
    }

    fn skip_whitespace(&mut self) {
        loop {
            let c = self.peek();
            match c {
                Some(' ' | '\r' | '\t') => { self.advance(); }
                Some('\n') => {
                    self.line += 1;
                    self.advance();
                }
                Some('/') => {
                    if Some('/') == self.peek_next() {
                        // A comment goes until the end of the line.
                        while self.peek() != Some('\n') && !self.is_at_end() {
                            self.advance();
                        }
                    }
                }
                _ => { return; }
            }
        }
    }

    fn peek_next(&self) -> Option<char> {
        if self.is_at_end() {
            return None;
        }
        return self.source.chars().nth((self.current + 1) as usize);
    }

    fn peek(&self) -> Option<char> {
        if self.is_at_end() {
            return None;
        }
        return self.source.chars().nth(self.current as usize);
    }

    fn is_at_end(&self) -> bool {
        self.current >= self.source.len() as i32
    }

    fn advance(&mut self) -> char {
        self.current += 1;
        return self.source.chars().nth((self.current - 1) as usize).unwrap();
    }

    fn match_char(&mut self, c: char) -> bool {
        if self.is_at_end() {
            return false;
        }
        match self.source.chars().nth(self.current as usize) {
            Some(ch) => {
                if ch != c {
                    return false;
                }
            }
            _ => { return false; }
        }
        self.current += 1;
        return true;
    }

    fn make_token(&self, token_type: TokenType) -> Token {
        Token {
            token_type,
            start: self.start,
            length: self.current - self.start,
            line: self.line,
            source: &self.source[self.start as usize..self.current as usize],
        }
    }

    fn error_token(&self, message: &'static str) -> Token {
        Token {
            token_type: TokenType::Error,
            start: 0,
            length: message.len() as i32,
            line: self.line,
            source: message,
        }
    }
}