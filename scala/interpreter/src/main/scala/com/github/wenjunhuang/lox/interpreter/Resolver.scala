package com.github.wenjunhuang.lox.interpreter

import com.github.wenjunhuang.lox.*

import scala.collection.mutable

class Resolver(interpreter: Interpreter) extends ExprVisitor with StatementVisitor:
  override def visitLiteral(expr: Expression.Literal): Value = Value.NoValue

  override def visitUnary(expr: Expression.Unary): Value =
    resolve(expr.right)
    Value.NoValue

  override def visitAssignment(expr: Expression.Assign): Value =
    resolve(expr.value)
    resolveLocal(expr, expr.name)
    Value.NoValue

  override def visitBinaryExpr(expr: Expression.Binary): Value =
    resolve(expr.left)
    resolve(expr.right)
    Value.NoValue

  override def visitGroupExpr(expr: Expression.Grouping): Value =
    resolve(expr.expression)
    Value.NoValue

  override def visitVariable(expr: Expression.Variable): Value =
    if scopes.nonEmpty && !scopes.top(expr.name.lexeme) then
      Lox.error(expr.name, "Cannot read local variable in its own initializer.")

    resolveLocal(expr, expr.name)
    Value.NoValue

  override def visitLogical(expr: Expression.Logical): Value =
    resolve(expr.left)
    resolve(expr.right)
    Value.NoValue

  override def visitCall(expr: Expression.Call): Value =
    resolve(expr.callee)
    expr.arguments.foreach(resolve)
    Value.NoValue

  override def visitVarStatement(statement: Statement.Var): Unit =
    declare(statement.name)
    if statement.initializer.isDefined then
      resolve(statement.initializer.get)
    define(statement.name)

  override def visitExpressionStatement(statement: Statement.Expr): Unit =
    resolve(statement.expression)

  override def visitPrintStatement(statement: Statement.Print): Unit =
    resolve(statement.expression)

  override def visitBlockStatement(statement: Statement.Block): Unit =
    beginScope()
    resolve(statement.statements)
    endScope()

  override def visitIfStatement(statement: Statement.If): Unit =
    resolve(statement.condition)
    resolve(statement.thenBranch)
    if statement.elseBranch.isDefined then
      resolve(statement.elseBranch.get)

  override def visitWhileStatement(statement: Statement.While): Unit =
    resolve(statement.condition)
    resolve(statement.body)

  override def visitFunctionStatement(statement: Statement.Func): Unit =
    declare(statement.name)
    define(statement.name)
    resolveFunction(statement)

  override def visitReturnStatement(statement: Statement.Return): Unit =
    if statement.expression.isDefined then
      resolve(statement.expression.get)

  def resolve(statements: Seq[Statement]): Unit =
    statements.foreach(resolve)

  private def resolve(statement: Statement): Unit = statement.accept(this)

  private def resolve(expr: Expression): Unit = expr.accept(this)

  private def beginScope(): Unit =
    scopes.push(mutable.Map.empty)

  private def endScope(): Unit =
    scopes.pop()

  private def declare(token: Token): Unit =
    if scopes.isEmpty then return
    val scope = scopes.top
    scope(token.lexeme) = false

  private def define(name: Token): Unit =
    if scopes.isEmpty then return
    scopes.top(name.lexeme) = true

  private def resolveLocal(expression: Expression, token: Token): Unit =
    scopes.reverseIterator
      .drop(1)
      .zipWithIndex
      .collectFirst { case (scope, index) if scope.contains(token.lexeme) => (scope, index) }
      .foreach { case (scope, index) => interpreter.resolve(expression,index) }

  private def resolveFunction(func: Statement.Func) =
    beginScope()
    func.params.foreach { param =>
      declare(param)
      define(param)
    }
    resolve(func.body)
    endScope()

  private val scopes = mutable.Stack[mutable.Map[String, Boolean]]()
end Resolver
