pub struct Scanner {
    start: i32,
    current: i32,
    line: i32,
    pub source: String,
}

#[derive(PartialEq, Eq, Hash, Copy, Clone)]
pub enum TokenType {
    LeftParen,
    RightParen,
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

#[derive(Copy, Clone)]
pub struct Token {
    pub token_type: TokenType,
    pub start: i32,
    pub length: i32,
    pub line: i32,
}

impl Scanner {
    pub fn new(source: String) -> Scanner {
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
            '(' => self.make_token(TokenType::LeftParen),
            ')' => self.make_token(TokenType::RightParen),
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
        return self.make_token(self.identifier_type());
    }

    fn identifier_type(&self) -> TokenType {
        return
            if let Some(c) = self.source.chars().nth(self.start as usize) {
                match c {
                    'a' => self.check_keyword(1, 2, "nd", TokenType::And),
                    'c' => self.check_keyword(1, 4, "lass", TokenType::Class),
                    'e' => self.check_keyword(1, 3, "lse", TokenType::Else),
                    'f' => {
                        if self.current - self.start > 1 {
                            match self.source.chars().nth((self.start + 1) as usize) {
                                Some('a') => self.check_keyword(2, 3, "lse", TokenType::False),
                                Some('o') => self.check_keyword(2, 1, "r", TokenType::For),
                                Some('u') => self.check_keyword(2, 1, "n", TokenType::Fun),
                                _ => TokenType::Identifier
                            }
                        } else {
                            TokenType::Identifier
                        }
                    }
                    'i' => self.check_keyword(1, 1, "f", TokenType::If),
                    'n' => self.check_keyword(1, 2, "il", TokenType::Nil),
                    'o' => self.check_keyword(1, 1, "r", TokenType::Or),
                    'p' => self.check_keyword(1, 4, "rint", TokenType::Print),
                    'r' => self.check_keyword(1, 5, "eturn", TokenType::Return),
                    's' => self.check_keyword(1, 4, "uper", TokenType::Super),
                    't' => {
                        if self.current - self.start > 1 {
                            match self.source.chars().nth((self.start + 1) as usize) {
                                Some('h') => self.check_keyword(2, 2, "is", TokenType::This),
                                Some('r') => self.check_keyword(2, 2, "ue", TokenType::True),
                                _ => TokenType::Identifier
                            }
                        } else {
                            TokenType::Identifier
                        }
                    }
                    'v' => self.check_keyword(1, 2, "ar", TokenType::Var),
                    'w' => self.check_keyword(1, 4, "hile", TokenType::While),
                    _ => TokenType::Identifier
                }
            } else {
                TokenType::Identifier
            };
    }

    fn check_keyword(&self, start: i32, length: i32, rest: &str, token_type: TokenType) -> TokenType {
        if self.current - self.start == start + length {
            let mut idx = 0;
            while idx < length {
                if self.source.chars().nth((self.start + start + idx) as usize) == rest.chars().nth(idx as usize) {
                    idx += 1;
                    continue;
                } else {
                    return TokenType::Identifier;
                }
            }
            return token_type;
        }
        return TokenType::Identifier;
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
        }
    }

    fn error_token(&self, message: &str) -> Token {
        Token {
            token_type: TokenType::Error,
            start: 0,
            length: message.len() as i32,
            line: self.line,
        }
    }
}