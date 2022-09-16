package com.github.wenjunhuang.lox

import com.github.wenjunhuang.lox.Value.CallableValue

import scala.collection.mutable

enum Value:
  case NumericValue(value: Double)
  case StringValue(value: String)
  case BooleanValue(value: Boolean)
  case FunctionValue(arity: Int, body: Vector[Value] => Value)
  case ClassValue(name: String, methods: Map[String, CallableValue])
  case InstanceValue(`class`: Value.ClassValue, fields: mutable.Map[String, Value])
  case NoValue

  override def toString: String =
    this match
      case NumericValue(v)         => v.toString
      case StringValue(v)          => v
      case BooleanValue(v)         => v.toString
      case FunctionValue(arity, _) => s"<function arity:$arity>"
      case ClassValue(name)        => s"<class $name>"
      case NoValue                 => "nil"
