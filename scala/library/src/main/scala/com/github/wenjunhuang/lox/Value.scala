package com.github.wenjunhuang.lox

import scala.collection.mutable

enum Value {
  case NumericValue(value: Double)
  case StringValue(value: String)
  case BooleanValue(value: Boolean)
  case FunctionValue(arity: Int, body: Vector[Value] => Value)
  case InstanceValue(`class`: Value.ClassValue, fields: mutable.Map[String, Value])
  case MethodValue(arity: Int, body: (Value.InstanceValue, Vector[Value]) => Value)
  case InitializerValue(arity: Int, body: (Value.InstanceValue, Vector[Value]) => Value.InstanceValue)
  case ClassValue(name: String,
                  superClass: Option[Value.ClassValue],
                  initializers: Vector[Value.InitializerValue],
                  methods: Map[String, Value.MethodValue]
  )
  case NoValue

  override def toString: String = this match {
      case NumericValue(v)           => v.toString
      case StringValue(v)            => v
      case BooleanValue(v)           => v.toString
      case FunctionValue(arity, _)   => s"<function arity:$arity>"
      case ClassValue(name, _, _, _) => s"<class $name>"
      case InstanceValue(k, _)       => s"<instance of ${k.name}>"
      case InitializerValue(_, _)    => "<initializer>"
      case MethodValue(arity, _)     => s"<method arity: $arity>"
      case NoValue                   => "nil"
    }

}
