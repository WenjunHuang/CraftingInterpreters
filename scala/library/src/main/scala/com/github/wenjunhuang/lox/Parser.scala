package com.github.wenjunhuang.lox
import com.github.wenjunhuang.lox.Value.NoValue

import scala.collection.mutable
import scala.util.control.Breaks.*

class Parser(private val tokens: Vector[Token]):
  import TokenType.*
  import Value.*

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
      else if matching(TokenType.FUN) then Some(function(FunKind.Function))
      else Some(statement())
    catch
      case _: ParseError =>
        synchronize()
        None

  private def function(kind: FunKind): Statement =
    val name = consume(TokenType.IDENTIFIER, s"Expect $kind name.")
    consume(TokenType.LEFT_PAREN, s"Expect '(' after $kind name.")

    val parameters = mutable.Buffer[Token]()
    if !check(TokenType.RIGHT_PAREN) then
      while !check(TokenType.RIGHT_PAREN) do
        if parameters.size >= 255 then error(peek, "Can't have more than 255 parameters.")
        parameters += consume(TokenType.IDENTIFIER, s"Expect parameter name.")
        if !check(TokenType.RIGHT_PAREN) then consume(TokenType.COMMA, s"Expect ',' after parameter name.")

    consume(TokenType.RIGHT_PAREN, s"Expect ')' after parameters.")
    consume(TokenType.LEFT_BRACE, s"Expect '{' before $kind body.")
    val body = block()

    Statement.Func(name, parameters.toVector, Statement.Block(body))

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
    else if matching(TokenType.IF) then ifStatement()
    else if matching(TokenType.RETURN) then returnStatement()
    else if matching(TokenType.WHILE) then whileStatement()
    else if matching(TokenType.FOR) then forStatement()
    else if matching(TokenType.RETURN) then returnStatement()
    else if matching(TokenType.LEFT_BRACE) then Statement.Block(block())
    else expressionStatement()

  private def returnStatement(): Statement =
    inline def keyword = previous
    inline def endWithSemicolon = !check(TokenType.SEMICOLON)

    val value = if endWithSemicolon then Some(expression()) else None
    consume(TokenType.SEMICOLON, "Expect ';' after return value.")
    Statement.Return(keyword, value)

  private def forStatement(): Statement =
    consume(TokenType.LEFT_PAREN, "Expect '(' after 'for'.")
    val initializer =
      if matching(TokenType.SEMICOLON) then None
      else if matching(TokenType.VAR) then Some(varDeclaration())
      else Some(expressionStatement())

    val condition =
      if !check(TokenType.SEMICOLON) then Some(expression())
      else None

    consume(TokenType.SEMICOLON, "Expect ';' after loop condition.")
    val increment =
      if !check(TokenType.RIGHT_PAREN) then Some(expression())
      else None

    consume(TokenType.RIGHT_PAREN, "Expect ')' after for clauses.")

    var body = statement()
    body = increment match
      case Some(inc) => Statement.Block(Vector(body, Statement.Expr(inc)))
      case None      => body

    val whileCond = condition match
      case Some(cond) => cond
      case None       => Expression.Literal(Value.BooleanValue(true))

    body = Statement.While(whileCond, body)

    body = initializer match
      case Some(init) => Statement.Block(Vector(init, body))
      case None       => body

    body

  private def whileStatement(): Statement =
    consume(TokenType.LEFT_PAREN, "Expect '(' after 'while'.")
    val condition = expression()
    consume(TokenType.RIGHT_PAREN, "Expect ')' after condition.")
    val body = statement()
    Statement.While(condition, body)

  private def ifStatement(): Statement =
    consume(TokenType.LEFT_PAREN, "Expect '(' after 'if'.")
    val condition = expression()
    consume(TokenType.RIGHT_PAREN, "Expect ')' after if condition.")

    val thenBranch = statement()
    val elseBranch =
      if matching(TokenType.ELSE) then Some(statement())
      else None

    Statement.If(condition, thenBranch, elseBranch)

  private def block(): Vector[Statement] =
    val statements = mutable.Buffer[Statement]()
    while !check(TokenType.RIGHT_BRACE) && !isAtEnd do
      declaration() match
        case Some(statement) => statements += statement
        case None            =>
    consume(TokenType.RIGHT_BRACE, "Expect '}' after block.")
    statements.toVector
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
    val expr = or()
    if matching(TokenType.EQUAL) then
      expr match
        case Expression.Variable(name) =>
          val value = assignment()
          Expression.Assign(name, value)
        case _ =>
          error(previous, "Invalid assignment target.")
          expr
    else expr

  private def or(): Expression =
    LazyList
      .continually(())
      .takeWhile(_ => matching(TokenType.OR))
      .foldLeft(and()) { (accum, _) =>
        val operator = previous
        val right = and()
        Expression.Logical(accum, operator, right)
      }
  private def and(): Expression =
    var expr = equality()
    while matching(TokenType.AND) do
      val operator = previous
      val right = equality()
      expr = Expression.Logical(expr, operator, right)
    expr

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
    else call()

  private def call(): Expression =
    val expr = primary()
    LazyList
      .continually(())
      .takeWhile(_ => matching(TokenType.LEFT_PAREN))
      .foldLeft(expr) { (accum, _) =>
        val arguments = mutable.Buffer[Expression]()

        if !check(TokenType.RIGHT_PAREN) then
          while !check(TokenType.RIGHT_PAREN) do
            arguments += expression()
            if !check(TokenType.RIGHT_PAREN) then consume(TokenType.COMMA, "Expect ',' after value.")

        val paren = consume(TokenType.RIGHT_PAREN, "Expect ')' after arguments.")

        if arguments.length >= 255 then error(peek, "Can't have move than 255 arguments.")

        Expression.Call(accum, paren, arguments.toVector)
      }

  private def primary(): Expression =
    if matching(FALSE) then Expression.Literal(BooleanValue(false))
    else if matching(TRUE) then Expression.Literal(BooleanValue(true))
    else if matching(NIL) then Expression.Literal(NoValue)
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

  private def synchronize(): Unit =
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

object Parser:
  def apply(tokens: Vector[Token]) = new Parser(tokens)
