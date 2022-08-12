package com.github.wenjunhuang.lox
import scala.collection.mutable

class Scanner(val source: String):
  import TokenType.*

  val tokens: mutable.Buffer[Token] = mutable.Buffer[Token]()
  // The start field points to the first character in the lexeme being scanned
  var start = 0
  // The current field points at the character currently being considered.
  var current = 0
  // The line field tracks what source line current is on so we can produce tokens that know their location.
  var line = 1

  def scanTokens(): List[Token] =
    while (!isAtEnd) {
      start = current
      scanToken()
    }
    tokens.toList

  private def scanToken() =
    val c = advance()
    c match
      case '(' => addToken(LEFT_PARAN)
      case ')' => addToken(RIGHT_PARAN)
      case '{' => addToken(LEFT_BRACE)
      case '}' => addToken(RIGHT_BRACE)
      case ',' => addToken(COMMA)
      case '.' => addToken(DOT)
      case '-' => addToken(MINUS)
      case '+' => addToken(PLUS)
      case ';' => addToken(SEMICOLON)
      case '*' => addToken(STAR)
      case '!' =>
        addToken(if matchChar('=') then BANG_EQUAL else BANG)
      case '=' =>
        addToken(if matchChar('=') then EQUAL_EQUAL else EQUAL)
      case '<' =>
        addToken(if matchChar('=') then LESS_EQUAL else LESS)
      case '>' =>
        addToken(if matchChar('=') then GREATER_EQUAL else GREATER)
      case '/' =>
        if matchChar('/') then while (peek() != '\n' && !isAtEnd) advance()
        else if matchChar('*') then
          var break = false
          while (!isAtEnd && !break)
            if matchChar('*') then if matchChar('/') then break = true
            if !break then advance()
        else addToken(SLASH)
      case ' ' | '\r' | '\t' =>
      case '\n' =>
        line += 1
      case '"'             => string()
      case c if isDigit(c) => number()
      case c if isAlpha(c) => identifier()
      case _               => error(line, "Unexpected character.")

  private def isAlphaNumeric(c: Char): Boolean =
    isAlpha(c) || isDigit(c)

  private def isAlpha(c: Char): Boolean = (c >= 'a' && c <= 'z') || (c >= 'A' && c <= 'Z') || c == '_'

  private def isDigit(c: Char): Boolean = c >= '0' && c <= '9'

  private def identifier(): Unit =
    while (isAlphaNumeric(peek())) advance()

    val text = source.substring(start, current)
    Scanner.keywords.get(text) match
      case Some(tokenType) => addToken(tokenType)
      case None            => addToken(IDENTIFIER)

  private def number() =
    while (isDigit(peek())) advance()

    // Look for a fractional part.
    if peek() == '.' && isDigit(peekNext()) then
      // Consume the '.'
      advance()
      while (isDigit(peek())) advance()

    addToken(NUMBER, source.substring(start, current).toDoubleOption)

  private def peekNext(): Char =
    if current + 1 >= source.length then '\u0000'
    else source.charAt(current + 1)

  private def string(): Unit =
    while (peek() != '"' && !isAtEnd)
      if peek() == '\n' then line += 1
      advance()

    if isAtEnd then error(line, "Unterminated string.")
    else
      advance() // The closing "
      // Trim the surrounding quotes
      val value = source.substring(start + 1, current - 1)
      addToken(TokenType.STRING, Some(value))

  private def peek(): Char =
    if isAtEnd then '\u0000'
    else source.charAt(current)

  private def advance(): Char =
    current += 1
    source.charAt(current - 1)

  private def matchChar(expected: Char): Boolean =
    if isAtEnd then false
    else if (source.charAt(current) != expected) false
    else
      current += 1; true

  private def addToken(tokenType: TokenType, literal: Option[Any] = None) =
    val text = source.substring(start, current)
    tokens.append(Token(tokenType, text, literal, line))

  private def isAtEnd: Boolean =
    current >= source.length

object Scanner:
  import TokenType.*

  val keywords: Map[String, TokenType] = Map(
    "and" -> AND,
    "class" -> CLASS,
    "else" -> ELSE,
    "false" -> FALSE,
    "for" -> FOR,
    "fun" -> FUN,
    "if" -> IF,
    "nil" -> NIL,
    "or" -> OR,
    "print" -> PRINT,
    "return" -> RETURN,
    "super" -> SUPER,
    "this" -> THIS,
    "true" -> TRUE,
    "var" -> VAR,
    "while" -> WHILE
  )
