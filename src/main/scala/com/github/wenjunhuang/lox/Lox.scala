package com.github.wenjunhuang.lox
import java.io.{BufferedReader, InputStreamReader}
import java.nio.file.{Files, Paths}
import scala.io.StdIn
import scala.util.Using

object Lox:
  var hasRuntimeError = false
  var hadError = false
  def main(args: Array[String]) =
    runPrompt()
//    run("""
//          |print "one"
//          |print true;
//          |print 2 + 1;
//          |""".stripMargin)

  private def runFile(path: String) =
    val bytes = Files.readAllBytes(Paths.get(path))
  end runFile

  def runPrompt() =
    val interpreter = new Interpreter()
    LazyList
      .continually(StdIn.readLine("> "))
      .takeWhile(_ != null)
      .foreach(run(interpreter))

  private def run(interpreter: Interpreter)(source: String) =
    val scanner = new Scanner(source)
    val tokens = scanner.scanTokens()
    val parser = new Parser(tokens)
    val expression = parser.parse()
    expression match
      case Right(expr) => interpreter.interpret(expr)
      case Left(error) => error.printStackTrace()
    end match
  end run

  def error(token: Token, message: String): Unit =
    if token.tt == TokenType.EOF then report(token.line, "", message)
    else report(token.line, s" at '${token.lexeme}'", message)

  def error(line: Int, message: String): Unit =
    report(line, "", message)

  def runtimeError(error: RuntimeError) =
    println(s"${error.getMessage}\n[line ${error.token.line}]")
    hasRuntimeError = true
  end runtimeError
  private def report(line: Int, where: String, message: String) =
    println(s"[line $line] Error $where: $message")
    hadError = true
  end report
end Lox
