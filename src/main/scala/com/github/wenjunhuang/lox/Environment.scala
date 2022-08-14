package com.github.wenjunhuang.lox
import scala.collection.mutable
class Environment:
  private val values = mutable.Map[String, Option[Any]]()

  def define(name: String, value: Option[Any]): Unit = values(name) = value

  def get(name: Token): Option[Any] = values.get(name.lexeme) match
    case Some(value) => value
    case None        => throw new RuntimeError(name, s"Undefined variable '${name.lexeme}'.'")
end Environment
