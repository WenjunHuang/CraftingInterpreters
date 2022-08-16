package com.github.wenjunhuang.lox
import com.github.wenjunhuang.lox.Expression.*
import com.github.wenjunhuang.lox.TokenType.*

trait ExprVisitor[+A]:
  def visitLiteral(expr: Literal): A
  def visitUnary(expr: Unary): A
  def visitAssignment(expr: Assign): A
  def visitBinaryExpr(expr: Binary): A
  def visitGroupExpr(expr: Grouping): A
  def visitVariable(expr: Variable): A
end ExprVisitor

