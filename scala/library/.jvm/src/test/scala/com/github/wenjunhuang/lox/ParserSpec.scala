package com.github.wenjunhuang.lox
import org.scalatest.EitherValues.*
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers

class ParserSpec extends AnyFlatSpec with Matchers:
  "A Parser" should "parse an empty program" in {
    val tokens = Vector(Token(TokenType.EOF, "", Value.NoValue, 1))
    val parser = Parser(tokens)
    val result = parser.parse()

    result.value.length should be(0)
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
  }

end ParserSpec
