package com.github.wenjunhuang.lox
import scala.collection.mutable
import scala.util.control.Breaks.*

class Parser(val tokens: Vector[Token]):
  import TokenType.*
  var current = 0

  def parse(): Either[ParseError, Vector[Statement]] =
    try
      val statements = mutable.Buffer[Statement]()
      while !isAtEnd do
        declaration() match
          case Some(statement) => statements += statement
          case None            =>
      Right(statements.toVector)
    catch case error: ParseError => Left(error)

  private def declaration(): Option[Statement] =
    try
      if matching(TokenType.VAR) then Some(varDeclaration())
      else Some(statement())
    catch
      case error: ParseError =>
        synchronize()
        None

  private def varDeclaration(): Statement =
    val name = consume(TokenType.IDENTIFIER, "Expect variable name.")
    if matching(TokenType.EQUAL) then
      val initializer = expression()
      consume(TokenType.SEMICOLON, "Expect ';' after variable declaration.")
      Statement.Var(name, Some(initializer))
    else
      consume(TokenType.SEMICOLON, "Expect '=' after variable name.")
      Statement.Var(name, None)

  private def statement(): Statement =
    if matching(TokenType.PRINT) then printStatement()
    else expressionStatement()

  private def expressionStatement(): Statement =
    val expr = expression()
    consume(TokenType.SEMICOLON, "Expect ';' after expression.")
    Statement.Expr(expr)

  private def printStatement(): Statement =
    val expr = expression()
    consume(TokenType.SEMICOLON, "Expect ';' after value.")
    Statement.Print(expr)

  private def expression(): Expression = assignment()

  private def assignment(): Expression =
    val expr = equality()
    if matching(TokenType.EQUAL) then
      expr match
        case Expression.Variable(name) =>
          val value = assignment()
          Expression.Assign(name, value)
        case _ =>
          error(previous, "Invalid assignment target.")
          expr
    else expr

  private def equality(): Expression =
    var expr = comparison()
    while matching(TokenType.BANG_EQUAL, TokenType.EQUAL_EQUAL) do
      val operator = previous
      val right = comparison()
      expr = Expression.Binary(expr, operator, right)
    expr

  private def comparison(): Expression =
    var expr = term()
    while matching(GREATER, GREATER_EQUAL, LESS, LESS_EQUAL) do
      val operator = previous
      val right = term()
      expr = Expression.Binary(expr, operator, right)
    expr

  private def term(): Expression =
    var expr = factor()
    while matching(PLUS, MINUS) do
      val operator = previous
      val right = factor()
      expr = Expression.Binary(expr, operator, right)
    expr

  private def factor(): Expression =
    var expr = unary()
    while matching(SLASH, STAR) do
      val operator = previous
      val right = unary()
      expr = Expression.Binary(expr, operator, right)
    expr

  private def unary(): Expression =
    if matching(BANG, MINUS) then
      val operator = previous
      val right = unary()
      Expression.Unary(operator, right)
    else primary()

  private def primary(): Expression =
    if matching(FALSE) then Expression.Literal(Some(false))
    else if matching(TRUE) then Expression.Literal(Some(true))
    else if matching(NIL) then Expression.Literal(None)
    else if matching(IDENTIFIER) then Expression.Variable(previous)
    else if matching(NUMBER, STRING) then Expression.Literal(previous.literal)
    else if matching(LEFT_PAREN) then
      val expr = expression()
      consume(RIGHT_PAREN, "Expect ')' after expression.")
      Expression.Grouping(expr)
    else throw error(peek, "Expect expression.")

  private def consume(tokenType: TokenType, message: String): Token =
    if !check(tokenType) then throw error(peek, message)
    else advance()

  private def error(token: Token, message: String): ParseError =
    Lox.error(token, message)
    new ParseError()

  private def synchronize() =
    advance()
    breakable {
      while !isAtEnd do
        if previous.tt == SEMICOLON then break
        peek.tt match
          case CLASS | FOR | FUN | IF | PRINT | RETURN | VAR | WHILE => break
          case _                                                     =>
        advance()
    }

  private def matching(types: TokenType*): Boolean =
    if types.exists(check) then
      advance()
      true
    else false

  private def check(tokenType: TokenType): Boolean =
    if isAtEnd then false
    else peek.tt == tokenType

  private def advance(): Token =
    if !isAtEnd then current += 1
    previous

  private def isAtEnd: Boolean =
    peek.tt == TokenType.EOF

  private def peek: Token = tokens(current)

  private def previous: Token = tokens(current - 1)

end Parser
