package com.github.wenjunhuang.lox

import com.github.wenjunhuang.lox.Expression.{Binary, Grouping, Literal, Unary}
import com.github.wenjunhuang.lox.TokenType.*

class Interpreter extends ExprVisitor[Option[Any]] with StatementVisitor[Unit]:
  private var environment = Environment.Global

  def interpret(statements: Seq[Statement]): Unit =
    try for stm <- statements do execute(stm)
    catch case error: RuntimeError => Lox.runtimeError(error)

  override def visitLiteral(expr: Literal): Option[Any] = expr.value

  override def visitUnary(expr: Unary): Option[Any] =
    val right = evaluate(expr.right)
    expr.operator.tt match
      case TokenType.MINUS =>
        checkNumberOperand(expr.operator, right, it => Option(-it))
      case TokenType.BANG =>
        Some(!isTruthy(right))
      case _ =>
        None

  override def visitBinaryExpr(expr: Binary): Option[Any] =
    val left = evaluate(expr.left)
    val right = evaluate(expr.right)
    expr.operator.tt match
      case TokenType.MINUS =>
        checkNumberOperands(expr.operator, left, right, (a, b) => Option(a - b))
      case TokenType.PLUS =>
        (left, right) match
          case (Some(l: Double), Some(r: Double)) => Some(l + r)
          case (Some(l: String), Some(r: String)) => Some(l + r)
          case _                                  => None
      case TokenType.SLASH =>
        checkNumberOperands(expr.operator, left, right, (a, b) => Option(a / b))
      case TokenType.STAR =>
        checkNumberOperands(expr.operator, left, right, (a, b) => Option(a * b))
      case GREATER =>
        checkNumberOperands(expr.operator, left, right, (a, b) => Option(a > b))
      case GREATER_EQUAL =>
        checkNumberOperands(expr.operator, left, right, (a, b) => Option(a >= b))
      case LESS =>
        checkNumberOperands(expr.operator, left, right, (a, b) => Option(a < b))
      case LESS_EQUAL =>
        checkNumberOperands(expr.operator, left, right, (a, b) => Option(a <= b))
      case BANG_EQUAL =>
        (left, right) match
          case (Some(l), Some(r)) => Some(!l.equals(r))
          case (None, None)       => Some(false)
          case _                  => Some(true)
      case EQUAL_EQUAL =>
        (left, right) match
          case (Some(l), Some(r)) => Some(l.equals(r))
          case (None, None)       => Some(true)
          case _                  => Some(false)
      case _ =>
        None

  override def visitGroupExpr(expr: Grouping): Option[Any] =
    evaluate(expr.expression)

  private def evaluate(expr: Expression): Option[Any] = expr.accept(this)

  private def isTruthy(value: Option[Any]): Boolean =
    value match
      case Some(v: Boolean) => v
      case None             => false
      case _                => true

  private def checkNumberOperand[T](operator: Token, operand: Option[Any], func: (Double) => T): T =
    operand match
      case Some(v: Double) => func(v)
      case _               => throw new RuntimeError(operator, "Operand must be number.")

  private def checkNumberOperands[T](operator: Token,
                                     left: Option[Any],
                                     right: Option[Any],
                                     func: (Double, Double) => T
  ): T =
    (left, right) match
      case (Some(l: Double), Some(r: Double)) => func(l, r)
      case _                                  => throw new RuntimeError(operator, "Operands must be numbers.")

  private def execute(statement: Statement): Unit = statement.accept(this)

  override def visitExpressionStatement(statement: Statement.Expr): Unit =
    evaluate(statement.expression)
    ()

  override def visitPrintStatement(statement: Statement.Print): Unit =
    val value = evaluate(statement.expression)
    println(value)
    ()

  override def visitVariable(expr: Expression.Variable): Option[Any] =
    environment.get(expr.name)

  override def visitVarStatement(statement: Statement.Var): Unit =
    val value = statement.initializer match
      case Some(initializer) =>
        evaluate(initializer)
      case None => None

    environment.define(statement.name.lexeme, value)

  override def visitAssignment(expr: Expression.Assign): Option[Any] =
    val value = evaluate(expr.value)
    environment.assign(expr.name, value)
    value

  override def visitBlockStatement(statement: Statement.Block): Unit =
    executeBlock(statement.statements, Environment(environment))

  def executeBlock(statements: Vector[Statement], environment: Environment): Unit =
    val previous = this.environment
    try
      this.environment = environment
      for statement <- statements do execute(statement)
    finally this.environment = previous

end Interpreter
