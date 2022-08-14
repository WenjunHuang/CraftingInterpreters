package com.github.wenjunhuang.lox

enum Expression:
  case Literal(value: Option[Any])
  case Unary(operator: Token, right: Expression)
  case Assign(name: Token, value: Expression)
  case Binary(left: Expression, operator: Token, right: Expression)
  case Grouping(expression: Expression)
  case Variable(name: Token)

  def accept[A](visitor: ExprVisitor[A]): A = this match
    case l: Literal  => visitor.visitLiteral(l)
    case u: Unary    => visitor.visitUnary(u)
    case a: Assign => visitor.visitAssignment(a)
    case b: Binary   => visitor.visitBinaryExpr(b)
    case g: Grouping => visitor.visitGroupExpr(g)
    case v: Variable => visitor.visitVariable(v)
