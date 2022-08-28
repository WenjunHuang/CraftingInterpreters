package com.github.wenjunhuang.lox
import java.io.{BufferedReader, InputStreamReader}
import java.nio.file.{Files, Paths}
import scala.collection.mutable
import scala.io.StdIn
import scala.util.Using

object Lox:
  var hasRuntimeError = false
  var hadError = false

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
