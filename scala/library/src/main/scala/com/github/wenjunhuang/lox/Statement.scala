package com.github.wenjunhuang.lox

enum Statement:
  case Expr(expression: Expression)
  case Print(expression: Expression)
  case If(condition: Expression, thenBranch: Statement, elseBranch: Option[Statement])
  case Var(name: Token, initializer: Option[Expression])
  case While(condition: Expression, body: Statement)
  case Block(statements: Vector[Statement])

  def accept(visitor: StatementVisitor) = this match
    case v: Var           => visitor.visitVarStatement(v)
    case expr: Expr       => visitor.visitExpressionStatement(expr)
    case print: Print     => visitor.visitPrintStatement(print)
    case block: Block     => visitor.visitBlockStatement(block)
    case ifStmt: If       => visitor.visitIfStatement(ifStmt)
    case whileStmt: While => visitor.visitWhileStatement(whileStmt)

end Statement
