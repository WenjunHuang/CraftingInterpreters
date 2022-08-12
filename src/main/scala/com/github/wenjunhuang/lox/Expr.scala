package com.github.wenjunhuang.lox

enum Expr:
  case Literal(value: Any)
  case Unary(operator: Token, right: Expr)
  case Binary(left: Expr, operator: Token, right: Expr)
  case Grouping(expression:Expr)
