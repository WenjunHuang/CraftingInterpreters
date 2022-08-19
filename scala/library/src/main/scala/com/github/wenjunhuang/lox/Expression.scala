package com.github.wenjunhuang.lox

enum Expression:
  case Literal(value: Value)
  case Logical(left: Expression, operator: Token, right: Expression)
  case Unary(operator: Token, right: Expression)
  case Assign(name: Token, value: Expression)
  case Binary(left: Expression, operator: Token, right: Expression)
  case Call(callee: Expression, paren: Token, arguments: Vector[Expression])
  case Grouping(expression: Expression)
  case Variable(name: Token)

  def accept(visitor: ExprVisitor): Value =
    this match
      case l: Literal  => visitor.visitLiteral(l)
      case l: Logical  => visitor.visitLogical(l)
      case u: Unary    => visitor.visitUnary(u)
      case a: Assign   => visitor.visitAssignment(a)
      case b: Binary   => visitor.visitBinaryExpr(b)
      case g: Grouping => visitor.visitGroupExpr(g)
      case v: Variable => visitor.visitVariable(v)
    end match
