package com.github.wenjunhuang.lox

case class Token(tt: TokenType, lexeme: String, literal: Value, line: Int)
