package com.github.wenjunhuang.lox.interpreter

import com.github.wenjunhuang.lox.*
import com.github.wenjunhuang.lox.interpreter.Resolver.Scope

import scala.collection.mutable
import scala.util.Using
import scala.util.Using.Releasable

class Resolver(interpreter: Interpreter) extends ExprVisitor with StatementVisitor:
  import Resolver.*

  override def visitLiteral(expr: Expression.Literal): Value = Value.NoValue

  override def visitSet(expr: Expression.Set): Value =
    resolve(expr.value)
    resolve(expr.obj)
    Value.NoValue

  override def visitGet(expr: Expression.Get): Value =
    resolve(expr.obj)
    Value.NoValue

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
    if scopes.nonEmpty && scopes.top.get(expr.name.lexeme).contains(false) then
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

  override def visitClassStatement(statement: Statement.Class): Unit =
    declare(statement.name)
    define(statement.name)
    statement
      .superClass
      .filter(_.name.lexeme == statement.name.lexeme)
      .foreach { it =>
        Lox.error(it.name, "A class cannot inherit from itself.")
      }

    statement.superClass.foreach(resolve)

    Using(beginScope()) { scope =>
      scope.put("this", true)
      statement.initializers.foreach(resolveFunction(_, FunctionType.Initializer))
      statement.methods.foreach(resolveFunction(_, FunctionType.Method))
    }

  override def visitFunctionStatement(statement: Statement.Func): Unit =
    declare(statement.name)
    define(statement.name)
    resolveFunction(statement, FunctionType.Function)

  override def visitReturnStatement(statement: Statement.Return): Unit =
    if currentFunction == FunctionType.None then
      Lox.error(statement.keyword, "Cannot return from top-level code.")
    else if currentFunction == FunctionType.Initializer then
      if statement.expression.isDefined then
        Lox.error(statement.keyword, "Cannot return a value from an initializer.")

    if statement.expression.isDefined then
      resolve(statement.expression.get)

  def startResolve(statements: Seq[Statement]): Unit    =
    Using(beginScope()) { _ =>
      resolve(statements)
    }
  private def resolve(statements: Seq[Statement]): Unit =
    statements.foreach(resolve)

  private def resolve(statement: Statement): Unit = statement.accept(this)

  private def resolve(expr: Expression): Unit = expr.accept(this)

  private def beginScope(): Scope =
    val scope = new mutable.HashMap[String, Boolean] with AutoCloseable:
      override def close(): Unit = scopes.pop()

    scopes.push(scope)
    scope

  private def endScope(): Unit =
    scopes.pop()

  private def declare(token: Token): Unit =
    if scopes.isEmpty then return
    val scope = scopes.top
    if scope.contains(token.lexeme) then
      Lox.error(token, "Already a variable with this name in this scope.")
    scope(token.lexeme) = false

  private def define(name: Token): Unit =
    if scopes.isEmpty then return
    scopes.top(name.lexeme) = true

  private def resolveLocal(expression: Expression, token: Token): Unit =
    scopes
      .zipWithIndex
      .collectFirst { case (scope, index) if scope.contains(token.lexeme) => (scope, index) }
      .foreach { case (scope, index) => interpreter.resolve(expression, index) }

  private def resolveFunction(func: Statement.Func, functionType: FunctionType) =
    Using(switchFunctionType(functionType)) { _ =>
      Using(beginScope()) { _ =>
        func.params.foreach { param =>
          declare(param)
          define(param)
        }
        resolve(func.body)
      }
    }

  private def switchFunctionType(functionType: FunctionType): AutoCloseable =
    val enclosingFunction = currentFunction
    currentFunction = functionType
    () => currentFunction = enclosingFunction

  override def visitThis(expr: Expression.This): Value =
    resolveLocal(expr, expr.keyword)
    Value.NoValue

  private val scopes          = mutable.Stack[mutable.Map[String, Boolean]]()
  private var currentFunction = FunctionType.None

end Resolver
object Resolver:
  type Scope = mutable.Map[String, Boolean] with AutoCloseable
end Resolver
