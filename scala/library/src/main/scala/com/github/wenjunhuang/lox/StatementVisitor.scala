package com.github.wenjunhuang.lox

trait StatementVisitor:
  def visitVarStatement(statement: Statement.Var): Unit
  def visitExpressionStatement(statement: Statement.Expr): Unit
  def visitPrintStatement(statement: Statement.Print): Unit
  def visitBlockStatement(statement: Statement.Block): Unit
  def visitIfStatement(statement: Statement.If): Unit

  def visitWhileStatement(statement: Statement.While): Unit
end StatementVisitor
