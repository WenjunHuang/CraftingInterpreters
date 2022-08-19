package com.github.wenjunhuang.lox.interpreter

import com.github.wenjunhuang.lox.*

class Interpreter extends ExprVisitor with StatementVisitor:
  import TokenType.*
  import Value.*

  private var environment = Environment.Global

  Environment.Global.define(
    "clock",
    Value.CallableValue(0,
                        params =>
                          val result = System.currentTimeMillis() / 1000.0
                          NumericValue(result)
    )
  )
  def interpret(statements: Seq[Statement]): Unit =
    try for stm <- statements do execute(stm)
    catch case error: RuntimeError => Lox.runtimeError(error)

  override def visitLiteral(expr: Expression.Literal): Value = expr.value

  override def visitUnary(expr: Expression.Unary): Value =
    val right = evaluate(expr.right)
    expr.operator.tt match
      case TokenType.MINUS =>
        checkNumberOperand(expr.operator, right, it => NumericValue(-it))
      case TokenType.BANG =>
        BooleanValue(!isTruthy(right))
      case _ =>
        NoValue

  override def visitBinaryExpr(expr: Expression.Binary): Value =
    val left = evaluate(expr.left)
    val right = evaluate(expr.right)
    expr.operator.tt match
      case TokenType.MINUS =>
        checkNumberOperands(expr.operator, left, right, (a, b) => NumericValue(a - b))
      case TokenType.PLUS =>
        (left, right) match
          case (NumericValue(l), NumericValue(r)) => NumericValue(l + r)
          case (StringValue(l), StringValue(r))   => StringValue(l + r)
          case _                                  => NoValue
      case TokenType.SLASH =>
        checkNumberOperands(expr.operator, left, right, (a, b) => NumericValue(a / b))
      case TokenType.STAR =>
        checkNumberOperands(expr.operator, left, right, (a, b) => NumericValue(a * b))
      case GREATER =>
        checkNumberOperands(expr.operator, left, right, (a, b) => BooleanValue(a > b))
      case GREATER_EQUAL =>
        checkNumberOperands(expr.operator, left, right, (a, b) => BooleanValue(a >= b))
      case LESS =>
        checkNumberOperands(expr.operator, left, right, (a, b) => BooleanValue(a < b))
      case LESS_EQUAL =>
        checkNumberOperands(expr.operator, left, right, (a, b) => BooleanValue(a <= b))
      case BANG_EQUAL =>
        BooleanValue(left != right)
      case EQUAL_EQUAL =>
        BooleanValue(left == right)
      case _ =>
        NoValue

  override def visitGroupExpr(expr: Expression.Grouping): Value =
    evaluate(expr.expression)

  private def evaluate(expr: Expression) = expr.accept(this)

  private def isTruthy(value: Value): Boolean =
    value match
      case BooleanValue(v) => v
      case NoValue         => false
      case _               => true

  private def checkNumberOperand[T](operator: Token, operand: Value, func: Double => T): T =
    operand match
      case NumericValue(v: Double) => func(v)
      case _                       => throw new RuntimeError(operator, "Operand must be number.")

  private def checkNumberOperands[T](operator: Token, left: Value, right: Value, func: (Double, Double) => T): T =
    (left, right) match
      case (NumericValue(l), NumericValue(r)) => func(l, r)
      case _                                  => throw new RuntimeError(operator, "Operands must be numbers.")

  override def visitExpressionStatement(statement: Statement.Expr): Unit =
    evaluate(statement.expression)
    ()

  override def visitPrintStatement(statement: Statement.Print): Unit =
    val value = evaluate(statement.expression)
    println(value)
    ()

  override def visitVariable(expr: Expression.Variable): Value =
    environment.get(expr.name)

  override def visitVarStatement(statement: Statement.Var): Unit =
    val value = statement.initializer match
      case Some(initializer) =>
        evaluate(initializer)
      case None => NoValue

    environment.define(statement.name.lexeme, value)

  override def visitAssignment(expr: Expression.Assign): Value =
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

  override def visitIfStatement(statement: Statement.If): Unit =
    if isTruthy(evaluate(statement.condition)) then execute(statement.thenBranch)
    else
      statement.elseBranch match
        case Some(stmt) => execute(stmt)
        case None       =>

  private def execute(statement: Statement): Unit = statement.accept(this)

  override def visitLogical(expr: Expression.Logical): Value =
    val left = evaluate(expr.left)
    expr.operator.tt match
      case TokenType.OR =>
        if isTruthy(left) then left else evaluate(expr.right)
      case TokenType.AND =>
        if isTruthy(left) then evaluate(expr.right) else left
      case _ =>
        Lox.runtimeError(new RuntimeError(expr.operator, "Invalid logical operator."))
        NoValue

  override def visitWhileStatement(statement: Statement.While): Unit =
    while isTruthy(evaluate(statement.condition)) do execute(statement.body)

  override def visitCall(expr: Expression.Call): Value =
    val callee = evaluate(expr.callee)

    callee match
      case Value.CallableValue(_, body) =>
        val arguments = expr.arguments.map(evaluate)
        body(arguments)
      case _ =>
        Lox.runtimeError(new RuntimeError(expr.paren, "Can only call functions and classes."))
        NoValue

  override def visitFunctionStatement(statement: Statement.Func): Unit =
    val currentEnvironment = environment
    currentEnvironment.define(
      statement.name.lexeme,
      Value.CallableValue(
        statement.params.size,
        { params =>
          val funEnvironment = Environment(currentEnvironment)
          statement.params.zip(params).foreach { case (param, value) => funEnvironment.define(param.lexeme, value) }
          executeBlock(statement.body.statements, funEnvironment)
          NoValue
        }
      )
    )
end Interpreter
