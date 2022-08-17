package com.github.wenjunhuang.lox.interpreter

import com.github.wenjunhuang.lox.*

import scala.collection.mutable
import scala.io.StdIn

object Program:
  def main(args: Array[String]) =
    runPrompt()

  //    run("""
  //          |print "one"
  //          |print true;
  //          |print 2 + 1;
  //          |""".stripMargin)
  def runPrompt() =
    val interpreter = new Interpreter()
    val buffer = mutable.Buffer[String]()
    LazyList
      .continually(StdIn.readLine("> "))
      .takeWhile(_ != null)
      .foreach(run(interpreter, buffer))

  private def run(interpreter: Interpreter, buffer: mutable.Buffer[String])(source: String): Unit =
    val realSource = if source == "{" then
      buffer.append(source)
      None
    else if source == "}" then
      buffer.append(source)
      val result = buffer.mkString("\n")
      buffer.clear()
      Some(result)
    else if buffer.nonEmpty then
      buffer.append(source)
      None
    else Some(source)

    realSource match
      case Some(source) =>
        val tokens = Scanner(source).scanTokens()
        val expression = Parser(tokens).parse()
        expression match
          case Right(expr) => interpreter.interpret(expr)
          case Left(error) => error.printStackTrace()
        end match
      case None =>
  end run
