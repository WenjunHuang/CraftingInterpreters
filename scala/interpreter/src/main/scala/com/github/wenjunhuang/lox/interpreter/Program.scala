package com.github.wenjunhuang.lox.interpreter

import com.github.wenjunhuang.lox.*
import org.jline.reader.{LineReader, LineReaderBuilder}

import java.io.{File, PrintStream}
import scala.collection.mutable
import scala.io.StdIn
import scala.util.{Try, Using}

object Program:
  def main(args: Array[String]): Unit =
    val source      = """
                   |class DenoshireCream {
                   |serveOn(){
                   |return "Scones";
                   |}
                   |}
                   |var cream = DenoshireCream();
                   |print cream;
                   |""".stripMargin
    Lox.output = Console.out
    val interpreter = new Interpreter(Console.out)
    runSource(interpreter, source,resolve = false)

  def runFile(file: File, output: PrintStream): Try[Unit] =
    Lox.output = output
    Using(io.Source.fromFile(file)) { source =>
      val interpreter = new Interpreter(output)
      runSource(interpreter, source.mkString)
    }

  def runPrompt() =
    val interpreter = new Interpreter(Console.out)
    val buffer      = mutable.Buffer[String]()
    LazyList
      .continually(StdIn.readLine("> "))
      .takeWhile(_ != null)
      .foreach(run(interpreter, buffer))

  private def runSource(interpreter: Interpreter, source: String,resolve:Boolean = true) =
    val tokens     = Scanner(source).scanTokens()
    val expression = Parser(tokens).parse()
    expression match
      case Right(expr) =>
        if resolve then
          val resolver = Resolver(interpreter)
          resolver.resolve(expr)
        if !Lox.hadError then
          interpreter.interpret(expr)
      case Left(error) => error.printStackTrace()

  private def run(interpreter: Interpreter, buffer: mutable.Buffer[String])(source: String): Unit =
    val realSource = if source.trim.endsWith("{") then
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
        val tokens     = Scanner(source).scanTokens()
        val expression = Parser(tokens).parse()
        expression match
          case Right(expr) => interpreter.interpret(expr)
          case Left(error) => error.printStackTrace()
        end match
      case None         =>
  end run
