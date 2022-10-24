package com.github.wenjunhuang.lox

enum Statement:
  case Expr(expression: Expression)
  case Print(expression: Expression)
  case Return(keyword: Token, expression: Option[Expression])
  case If(condition: Expression, thenBranch: Statement, elseBranch: Option[Statement])
  case Var(name: Token, initializer: Option[Expression])
  case While(condition: Expression, body: Statement)
  case Block(statements: Vector[Statement])
  case Func(name: Token, params: Vector[Token], body: Statement.Block)
  case Class(name: Token,
             superClass: Option[Expression.Variable],
             initializers: Vector[Statement.Func],
             methods: Vector[Statement.Func]
  )

  def accept(visitor: StatementVisitor): Unit = this match
    case v: Var           => visitor.visitVarStatement(v)
    case expr: Expr       => visitor.visitExpressionStatement(expr)
    case print: Print     => visitor.visitPrintStatement(print)
    case block: Block     => visitor.visitBlockStatement(block)
    case ifStmt: If       => visitor.visitIfStatement(ifStmt)
    case whileStmt: While => visitor.visitWhileStatement(whileStmt)
    case func: Func       => visitor.visitFunctionStatement(func)
    case rtn: Return      => visitor.visitReturnStatement(rtn)
    case c: Class         => visitor.visitClassStatement(c)

end Statement
