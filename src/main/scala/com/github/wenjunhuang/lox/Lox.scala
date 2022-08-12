package com.github.wenjunhuang.lox
import java.io.{BufferedReader, InputStreamReader}
import java.nio.file.{Files, Paths}
import scala.io.StdIn
import scala.util.Using

@main
def lox() =
  runPrompt()

private def runFile(path: String) =
  val bytes = Files.readAllBytes(Paths.get(path))

private def runPrompt() =
  LazyList
    .continually(StdIn.readLine("> "))
    .takeWhile(_ != null)
    .foreach(run)

private def run(source: String) =
  val scanner = new Scanner(source)
  val tokens = scanner.scanTokens()
  tokens.foreach(println)

private var hadError = false

private[lox] def error(line: Int, message: String) =
  report(line, "", message)

private def report(line: Int, where: String, message: String) =
  println(s"[line $line] Error $where: $message")
  hadError = true
