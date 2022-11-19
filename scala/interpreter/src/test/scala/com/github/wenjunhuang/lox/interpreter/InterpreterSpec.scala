package com.github.wenjunhuang.lox.interpreter

import com.github.wenjunhuang.lox.{Lox, Parser, Scanner, TestUtils}
import org.apache.commons.io.{Charsets, FileUtils}
import org.apache.commons.lang3.StringUtils
import org.scalatest.Inside
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers

import java.io.{ByteArrayOutputStream, File, PrintStream}
import java.nio.charset.StandardCharsets
import scala.io.Source
import scala.util.Using

val TestDataPath = File(TestUtils.BaseTestDataPath, "interpreter")

class InterpreterSpec extends AnyFlatSpec with Matchers with Inside:

  "A Interpreter" should "run an empty program" in {
    doTest("printVariable1")
  }

  it should "call super class method" in {
    doTest("inheritance1")
  }

  it should "call correct super class method" in {
    doTest("super1")
  }


  private def runSource(interpreter: Interpreter, source: String, resolve: Boolean = true) =
    val tokens     = Scanner(source).scanTokens()
    val expression = Parser(tokens).parse()
    expression match
      case Right(stmts) =>
        if resolve then
          val resolver = Resolver(interpreter)
          resolver.startResolve(stmts)
        if !Lox.hadError then
          interpreter.interpret(stmts)
      case Left(error)  => fail(error)

  private def doTest(testFileName: String) = {
    val sourceName  = File(TestDataPath, s"$testFileName.lox")
    val astFileName = File(TestDataPath, s"$testFileName.txt")
    val source      = FileUtils.readFileToString(sourceName, StandardCharsets.UTF_8)
    val result      =
      StringUtils.replace(FileUtils.readFileToString(astFileName, StandardCharsets.UTF_8), System.lineSeparator(), "\n")

    val buffer      = ByteArrayOutputStream()
    val interpreter = Interpreter(PrintStream(buffer))
    runSource(interpreter, source)
    StringUtils.replace(buffer.toString, System.lineSeparator(), "\n") should be(result)
  }
end InterpreterSpec
