package com.github.wenjunhuang.lox
import scala.collection.mutable
class Environment private (val enclosing: Option[Environment] = None):
  private val values                           = mutable.Map[String, Value]()

  def define(name: String, value: Value): Unit =
    values(name) = value

  def get(name: Token): Value = values.get(name.lexeme) match
    case Some(value) => value
    case None        =>
      enclosing match
        case Some(e) => e.get(name)
        case None    => throw new RuntimeError(name, s"Undefined variable '${name.lexeme}'.")

  def get(name: String): Value = values.get(name) match
    case Some(value) => value
    case None        =>
      enclosing match
        case Some(e) => e.get(name)
        case None    => throw new Exception(s"Undefined variable '${name}'.")

  def assign(name: Token, value: Value): Unit =
    values.updateWith(name.lexeme)(_.map(_ => value)) match
      case None =>
        enclosing match
          case Some(e) => e.assign(name, value)
          case None    => throw new RuntimeError(name, s"Undefined variable '${name}'.")
      case _    =>

  def assignAt(depth: Int, name: Token, value: Value): Unit =
    if depth == 0 then values(name.lexeme) = value
    else enclosing.foreach(_.assignAt(depth - 1, name, value))

  def getAt(name: Token, depth: Int): Option[Value] =
    if depth == 0 then values.get(name.lexeme)
    else enclosing.flatMap(_.getAt(name, depth - 1))
  def getAt(name: String, depth: Int): Option[Value] =
    if depth == 0 then values.get(name)
    else enclosing.flatMap(_.getAt(name, depth - 1))

end Environment

object Environment:
  val Global                        = new Environment()
  def apply(enclosing: Environment) = new Environment(Some(enclosing))
end Environment
