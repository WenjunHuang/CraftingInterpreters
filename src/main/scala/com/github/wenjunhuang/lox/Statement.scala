package com.github.wenjunhuang.lox

enum Statement:
  case Expr(expression: Expression)
  case Print(expression: Expression)
  case Var(name: Token, initializer: Option[Expression])
  case Block(statements: Vector[Statement])

  def accept[T](visitor: StatementVisitor[T]): T = this match
    case v: Var       => visitor.visitVarStatement(v)
    case expr: Expr   => visitor.visitExpressionStatement(expr)
    case print: Print => visitor.visitPrintStatement(print)
    case block: Block => visitor.visitBlockStatement(block)

end Statement
