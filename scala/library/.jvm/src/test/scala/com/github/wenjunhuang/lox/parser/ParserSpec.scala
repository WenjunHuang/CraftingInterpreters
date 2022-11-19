package com.github.wenjunhuang.lox.parser

import com.github.wenjunhuang.lox.*
import com.github.wenjunhuang.lox.Value.{NoValue, NumericValue}
import org.scalatest.EitherValues.*
import org.scalatest.Inside
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers

import java.io.{File, FileReader}
import scala.io.Source
import scala.util.Using

val TestDataPath = File(TestUtils.BaseTestDataPath, "parser")

class ParserSpec extends AnyFlatSpec with Matchers with Inside:
  private def doTest(testFileName: String) = {
    val sourceName  = File(TestDataPath, s"$testFileName.lox")
    val astFileName = File(TestDataPath, s"$testFileName.txt")
    Using(Source.fromFile(sourceName)) { source =>
      Using(Source.fromFile(astFileName)) { ast =>
        val tokens = Scanner(source.mkString).scanTokens()
        inside(Parser(tokens).parse()) {
          case Right(statements) =>
            statements.map(_.toString).mkString("\n") should be(ast.getLines().mkString("\n"))
        }
      }.get
    }.get
  }

  "A Parser" should "parse an empty program" in {
    val tokens = Vector(Token(TokenType.EOF, "", Value.NoValue, 1))
    val parser = Parser(tokens)
    val result = parser.parse()

    result.value.length should be(0)
  }

  it should "parse an empty function" in {
    doTest("function1")
  }

  it should "parse simple variable declarations" in {
    doTest("variable1")
  }

  it should "parse class definition" in {
    doTest("class1")
  }

  it should "parse a zero parameter function declaration" in {
    val tokens = Vector(
      Token(TokenType.FUN, "fun", Value.NoValue, 1),
      Token(TokenType.IDENTIFIER, "f", Value.NoValue, 1),
      Token(TokenType.LEFT_PAREN, "(", Value.NoValue, 1),
      Token(TokenType.RIGHT_PAREN, ")", Value.NoValue, 1),
      Token(TokenType.LEFT_BRACE, "{", Value.NoValue, 1),
      Token(TokenType.RIGHT_BRACE, "}", Value.NoValue, 1),
      Token(TokenType.EOF, "", Value.NoValue, 1)
    )
    val parser = Parser(tokens)
    val result = parser.parse()
    result.value.length should be(1)
    result.value(0) should be(
      Statement.Func(
        Token(TokenType.IDENTIFIER, "f", Value.NoValue, 1),
        Vector.empty,
        Statement.Block(Vector.empty)
      )
    )
  }

  it should "parse a function declaration with multiple parameters" in {
    val tokens = Vector(
      Token(TokenType.FUN, "fun", Value.NoValue, 1),
      Token(TokenType.IDENTIFIER, "f", Value.NoValue, 1),
      Token(TokenType.LEFT_PAREN, "(", Value.NoValue, 1),
      Token(TokenType.IDENTIFIER, "a", Value.NoValue, 1),
      Token(TokenType.COMMA, ",", Value.NoValue, 1),
      Token(TokenType.IDENTIFIER, "b", Value.NoValue, 1),
      Token(TokenType.COMMA, ",", Value.NoValue, 1),
      Token(TokenType.IDENTIFIER, "c", Value.NoValue, 1),
      Token(TokenType.RIGHT_PAREN, ")", Value.NoValue, 1),
      Token(TokenType.LEFT_BRACE, "{", Value.NoValue, 1),
      Token(TokenType.IDENTIFIER, "a", Value.NoValue, 1),
      Token(TokenType.PLUS, "+", Value.NoValue, 1),
      Token(TokenType.IDENTIFIER, "b", Value.NoValue, 1),
      Token(TokenType.MINUS, "-", Value.NoValue, 1),
      Token(TokenType.IDENTIFIER, "c", Value.NoValue, 1),
      Token(TokenType.SEMICOLON, ";", Value.NoValue, 1),
      Token(TokenType.RIGHT_BRACE, "}", Value.NoValue, 1),
      Token(TokenType.EOF, "", Value.NoValue, 1)
    )
    val parser = Parser(tokens)
    val result = parser.parse()
    result.value.length should be(1)
    result.value(0) should be(
      Statement.Func(
        Token(TokenType.IDENTIFIER, "f", Value.NoValue, 1),
        Vector(
          Token(TokenType.IDENTIFIER, "a", Value.NoValue, 1),
          Token(TokenType.IDENTIFIER, "b", Value.NoValue, 1),
          Token(TokenType.IDENTIFIER, "c", Value.NoValue, 1)
        ),
        Statement.Block(
          Vector(
            Statement.Expr(
              Expression.Binary(
                Expression.Binary(
                  Expression.Variable(Token(TokenType.IDENTIFIER, "a", Value.NoValue, 1)),
                  Token(TokenType.PLUS, "+", Value.NoValue, 1),
                  Expression.Variable(Token(TokenType.IDENTIFIER, "b", Value.NoValue, 1))
                ),
                Token(TokenType.MINUS, "-", Value.NoValue, 1),
                Expression.Variable(Token(TokenType.IDENTIFIER, "c", Value.NoValue, 1))
              )
            )
          )
        )
      )
    )
  }

  it should "parse a return statement" in {
    import Value.*
    val tokens = Vector(
      Token(TokenType.RETURN, "return", NoValue, 1),
      Token(TokenType.NUMBER, "1", NumericValue(1.0), 1),
      Token(TokenType.SEMICOLON, ";", NoValue, 1),
      Token(TokenType.EOF, "", NoValue, 1)
    )
    val parser = Parser(tokens)
    val result = parser.parse()
    result.value.size should be(1)
    result.value.head should matchPattern {
      case Statement.Return(_, Some(Expression.Literal(NumericValue(1.0)))) =>
    }
  }

  it should "parse a class definition" in {
    import Value.*
    val tokens = Vector(
      Token(TokenType.CLASS, "class", NoValue, 1),
      Token(TokenType.IDENTIFIER, "ClassName", NoValue, 1),
      Token(TokenType.LEFT_BRACE, "{", NoValue, 1),
      Token(TokenType.INIT, "init", NoValue, 1),
      Token(TokenType.LEFT_PAREN, "(", NoValue, 1),
      Token(TokenType.RIGHT_PAREN, ")", NoValue, 1),
      Token(TokenType.LEFT_BRACE, "{", NoValue, 1),
      Token(TokenType.RIGHT_BRACE, "}", NoValue, 1),
      Token(TokenType.IDENTIFIER, "memberFunc", NoValue, 1),
      Token(TokenType.LEFT_PAREN, "(", NoValue, 1),
      Token(TokenType.RIGHT_PAREN, ")", NoValue, 1),
      Token(TokenType.LEFT_BRACE, "{", NoValue, 1),
      Token(TokenType.RIGHT_BRACE, "}", NoValue, 1),
      Token(TokenType.RIGHT_BRACE, "}", NoValue, 1),
      Token(TokenType.EOF, "", NoValue, 1)
    )

    val parser = Parser(tokens)
    val result = parser.parse()
    result.value.size should be(1)
    inside(result.value.head) {
      case Statement.Class(_, _, initializers, methods) =>
        initializers.length should be(1)
        methods.length should be(1)
        methods.head should matchPattern {
          case Statement.Func(Token(TokenType.IDENTIFIER, "memberFunc", NoValue, 1),
                              Vector(),
                              Statement.Block(Vector())
              ) =>
        }
    }
  }

end ParserSpec
