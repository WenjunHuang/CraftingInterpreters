package com.github.wenjunhuang.lox

trait StatementVisitor[+T]:
  def visitVarStatement(statement:Statement.Var): T
  def visitExpressionStatement(statement: Statement.Expr): T
  def visitPrintStatement(statement: Statement.Print): T
  def visitBlockStatement(statement:Statement.Block): T
end StatementVisitor
