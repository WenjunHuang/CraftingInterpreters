package com.github.wenjunhuang.lox

case class Token(tt: TokenType, lexeme: String, literal: Value, line: Int):
  override def toString: String = lexeme
end Token

object Token {
  def dumbToken(lexeme: String): Token = Token(TokenType.Unknown, lexeme, Value.NoValue, 0)
}
