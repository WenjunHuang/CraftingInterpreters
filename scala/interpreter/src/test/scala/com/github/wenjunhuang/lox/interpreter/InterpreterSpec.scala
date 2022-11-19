package com.github.wenjunhuang.lox.interpreter

import com.github.wenjunhuang.lox.{Lox, Parser, Scanner, TestUtils}
import org.scalatest.Inside
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers

import java.io.{ByteArrayOutputStream, File, PrintStream}
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
    Using(Source.fromFile(sourceName)) { source =>
      Using(Source.fromFile(astFileName)) { ast =>
        val buffer      = ByteArrayOutputStream()
        val interpreter = Interpreter(PrintStream(buffer))
        runSource(interpreter, source.getLines().mkString("\n"))
        buffer.toString should be(ast.getLines().mkString("\n").trim)
      }.get
    }.get
  }
end InterpreterSpec
