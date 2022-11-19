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
  case Get(obj: Expression, name: Token)
  case Set(obj: Expression, name: Token, value: Expression)
  case This(keyword: Token)
  case Super(keyword: Token, method: Token)

  def accept(visitor: ExprVisitor): Value =
    this match
      case l: Literal  => visitor.visitLiteral(l)
      case l: Logical  => visitor.visitLogical(l)
      case u: Unary    => visitor.visitUnary(u)
      case a: Assign   => visitor.visitAssignment(a)
      case b: Binary   => visitor.visitBinaryExpr(b)
      case g: Grouping => visitor.visitGroupExpr(g)
      case v: Variable => visitor.visitVariable(v)
      case c: Call     => visitor.visitCall(c)
      case get: Get    => visitor.visitGet(get)
      case set: Set    => visitor.visitSet(set)
      case t: This     => visitor.visitThis(t)
      case s: Super    => visitor.visitSuper(s)

end Expression

object Expression:
  given Conversion[Value, Expression.Literal] with
    override def apply(x: Value): Expression.Literal = Expression.Literal(x)
end Expression
