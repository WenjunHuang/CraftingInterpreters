package com.github.wenjunhuang.lox
import scala.collection.mutable
class Environment private (val enclosing: Option[Environment] = None):
  private val values = mutable.Map[String, Value]()
  def define(name: String, value: Value): Unit =
    values(name) = value

  def get(name: Token): Value = values.get(name.lexeme) match
    case Some(value) => value
    case None =>
      enclosing match
        case Some(e) => e.get(name)
        case None    => throw new RuntimeError(name, s"Undefined variable '${name.lexeme}'.")

  def assign(name: Token, value: Value): Unit =
    values.updateWith(name.lexeme)(_.map(_ => value)) match
      case None =>
        enclosing match
          case Some(e) => e.assign(name, value)
          case None    => throw new RuntimeError(name, s"Undefined variable '${name}'.")
      case _ =>

end Environment

object Environment:
  val Global = new Environment()
  def apply(enclosing: Environment) = new Environment(Some(enclosing))
end Environment
