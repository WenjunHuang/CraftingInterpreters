package com.github.wenjunhuang.lox

enum Value:
  case NumericValue(value: Double)
  case StringValue(value: String)
  case BooleanValue(value: Boolean)
  case CallableValue(arguments: Vector[Expression], body: Statement)
  case NoValue
