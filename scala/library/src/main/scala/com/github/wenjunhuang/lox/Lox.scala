package com.github.wenjunhuang.lox
import java.io.{BufferedReader, InputStreamReader, PrintStream}
import java.nio.file.{Files, Paths}
import scala.collection.mutable
import scala.io.StdIn
import scala.util.Using

object Lox:
  private var hasRuntimeError = false
  var hadError                = false
  var output: PrintStream     = System.out

  def error(token: Token, message: String): Unit =
    if token.tt == TokenType.EOF then report(token.line, "", message)
    else report(token.line, s" at '${token.lexeme}'", message)

  def error(line: Int, message: String): Unit =
    report(line, "", message)

  def runtimeError(error: RuntimeError): Unit =
    output.println(s"${error.getMessage}\n[line ${error.token.line}]")
    hasRuntimeError = true
  end runtimeError

  private def report(line: Int, where: String, message: String): Unit =
    output.println(s"[line $line] Error $where: $message")
    hadError = true
  end report
end Lox
