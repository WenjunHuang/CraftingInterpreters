package com.github.wenjunhuang.lox.interpreter

import com.github.wenjunhuang.lox.*
import org.jline.reader.{LineReader, LineReaderBuilder}

import scala.collection.mutable
import scala.io.StdIn
object Program:
  def main(args: Array[String]): Unit =
//    val source = """
//                   |fun sayHi(first, last){
//                   |print "Hi, " + first + " " + last + "!";
//                   |}
//                   |sayHi("Dear","Reader");
//                   |""".stripMargin
//    val source = """
//                   |fun fib(n){
//                   |if (n <= 1) return n;
//                   |return fib(n - 1) + fib(n - 2);
//                   |}
//                   |for (var i = 0 ;i< 20;i = i+1){
//                   |print fib(i);
//                   |}
//                   |""".stripMargin
    val source = """
                   |fun makeCounter(){
                   |var i = 0;
                   |fun count(){
                   |i = i + 1;
                   |print i;
                   |}
                   |return count;
                   |}
                   |
                   |var counter = makeCounter();
                   |counter();
                   |counter();
                   |
                   |""".stripMargin
    val interpreter = new Interpreter()
    runSource(interpreter, source)

//    runPrompt()

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

  private def runSource(interpreter: Interpreter, source: String) =
    val tokens = Scanner(source).scanTokens()
    val expression = Parser(tokens).parse()
    expression match
      case Right(expr) => interpreter.interpret(expr)
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
        val tokens = Scanner(source).scanTokens()
        val expression = Parser(tokens).parse()
        expression match
          case Right(expr) => interpreter.interpret(expr)
          case Left(error) => error.printStackTrace()
        end match
      case None =>
  end run
