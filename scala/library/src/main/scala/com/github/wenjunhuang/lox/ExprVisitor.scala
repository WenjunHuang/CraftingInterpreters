package com.github.wenjunhuang.lox
import com.github.wenjunhuang.lox.Expression.*
import com.github.wenjunhuang.lox.TokenType.*

trait ExprVisitor:
  def visitLiteral(expr: Literal): Value
  def visitUnary(expr: Unary): Value
  def visitAssignment(expr: Assign): Value
  def visitBinaryExpr(expr: Binary): Value
  def visitGroupExpr(expr: Grouping): Value
  def visitVariable(expr: Variable): Value
  def visitLogical(expr: Logical): Value
  def visitGet(expr: Get): Value
  def visitCall(expr: Call): Value
end ExprVisitor
