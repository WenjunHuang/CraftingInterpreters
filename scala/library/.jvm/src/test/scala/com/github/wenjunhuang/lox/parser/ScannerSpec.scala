package com.github.wenjunhuang.lox.parser

import com.github.wenjunhuang.lox.{Scanner, Token, TokenType, Value}
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers

class ScannerSpec extends AnyFlatSpec with Matchers:
  "A Scanner" should "insert eof token at the end" in {
    val source  = ""
    val scanner = new Scanner(source)
    val tokens  = scanner.scanTokens()
    tokens.size should be(1)
    tokens(0) should be(Token(TokenType.EOF, "", Value.NoValue, 1))
  }

  it should "scan number" in {
    val source  = "123 8123.123"
    val scanner = new Scanner(source)
    val tokens  = scanner.scanTokens()
    tokens.size should be(3)
    tokens(0) should be(Token(TokenType.NUMBER, "123", Value.NumericValue(123.0), 1))
    tokens(1) should be(Token(TokenType.NUMBER, "8123.123", Value.NumericValue(8123.123), 1))
  }

  it should "scan key words" in {
    val tokenSource = List(
      "and"    -> TokenType.AND,
      "class"  -> TokenType.CLASS,
      "init"   -> TokenType.INIT,
      "else"   -> TokenType.ELSE,
      "false"  -> TokenType.FALSE,
      "for"    -> TokenType.FOR,
      "fun"    -> TokenType.FUN,
      "if"     -> TokenType.IF,
      "nil"    -> TokenType.NIL,
      "or"     -> TokenType.OR,
      "print"  -> TokenType.PRINT,
      "return" -> TokenType.RETURN,
      "super"  -> TokenType.SUPER,
      "this"   -> TokenType.THIS,
      "true"   -> TokenType.TRUE,
      "var"    -> TokenType.VAR,
      "while"  -> TokenType.WHILE,
      "+"      -> TokenType.PLUS,
      "-"      -> TokenType.MINUS,
      "*"      -> TokenType.STAR,
      "/"      -> TokenType.SLASH,
      ">"      -> TokenType.GREATER,
      "<"      -> TokenType.LESS,
      ">="     -> TokenType.GREATER_EQUAL,
      "<="     -> TokenType.LESS_EQUAL,
      "="      -> TokenType.EQUAL,
      "=="     -> TokenType.EQUAL_EQUAL,
      "!"      -> TokenType.BANG,
      "!="     -> TokenType.BANG_EQUAL,
      "("      -> TokenType.LEFT_PAREN,
      ")"      -> TokenType.RIGHT_PAREN,
      ","      -> TokenType.COMMA,
      "{"      -> TokenType.LEFT_BRACE,
      "}"      -> TokenType.RIGHT_BRACE
    )

    val scanner = new Scanner(tokenSource.map(_._1).mkString(" "))
    val tokens  = scanner.scanTokens()
    tokens.size should be(tokenSource.size + 1)

    tokenSource.zipWithIndex.foreach { case ((source, tokenType), index) =>
      tokens(index) should be(Token(tokenType, source, Value.NoValue, 1))
    }
  }

  it should "scan function call" in {
    val source  = """
                   |myFunction(1, 2, 3);
                   |otherFunction(a,b);
                   |""".stripMargin
    val scanner = new Scanner(source)
    val tokens  = scanner.scanTokens()
    tokens.size should be(17)
  }

  it should "scan return statement" in {
    val source  = """
                   |return 10;
                   |""".stripMargin
    val scanner = new Scanner(source)
    val tokens  = scanner.scanTokens()
    tokens.size should be(4)
  }

end ScannerSpec
