package com.github.wenjunhuang.lox.ideaplugin

import com.intellij.lexer.Lexer
import com.intellij.openapi.editor.DefaultLanguageHighlighterColors
import com.intellij.openapi.editor.colors.TextAttributesKey
import com.intellij.openapi.fileTypes.SyntaxHighlighterBase
import com.intellij.psi.tree.IElementType

class LoxSyntaxHighlighter extends SyntaxHighlighterBase:
  import LoxSyntaxHighlighter.*

  override def getHighlightingLexer: Lexer = LoxLexerAdapter()

  override def getTokenHighlights(tokenType: IElementType): Array[TextAttributesKey] =
    tokenType match
    case LoxTypes.IDENTIFIER => IDENTIFIER_KEYS
    case LoxTypes.STRING     => STRING_KEYS
    case LoxTypes.NUMBER     => NUMBER_KEYS
    case LoxTypes.IF | LoxTypes.FUN | LoxTypes.FOR | LoxTypes.WHILE | LoxTypes.PRINT | LoxTypes.RETURN |
         LoxTypes.PRINT | LoxTypes.VAR =>
      KEYWORD_KEYS
    case _ => EMPTY_KEYS
end LoxSyntaxHighlighter

object LoxSyntaxHighlighter:
  import TextAttributesKey.*
  val LINE_COMMENT: TextAttributesKey =
    createTextAttributesKey("LOX_LINECOMMENT", DefaultLanguageHighlighterColors.LINE_COMMENT)
  val BLOCK_COMMENT: TextAttributesKey =
    createTextAttributesKey("LOX_BLOCKCOMMENT", DefaultLanguageHighlighterColors.BLOCK_COMMENT)
  val STRING_VALUE: TextAttributesKey =
    createTextAttributesKey("LOX_STRING_VALUE", DefaultLanguageHighlighterColors.STRING)
  val NUMBER_VALUE = createTextAttributesKey("LOX_NUMBER_VALUE", DefaultLanguageHighlighterColors.NUMBER)
  val FUN_DECL = createTextAttributesKey("LOX_FUNCTION", DefaultLanguageHighlighterColors.FUNCTION_DECLARATION)
  val IDENTIFIER = createTextAttributesKey("LOX_IDENTIFIER", DefaultLanguageHighlighterColors.IDENTIFIER)
  val KEY = createTextAttributesKey("LOX_KEY", DefaultLanguageHighlighterColors.KEYWORD)

  val COMMENT_KEYS = Array(LINE_COMMENT, BLOCK_COMMENT)
  val IDENTIFIER_KEYS = Array(IDENTIFIER)
  val KEYWORD_KEYS = Array(KEY)
  val STRING_KEYS = Array(STRING_VALUE)
  val NUMBER_KEYS = Array(NUMBER_VALUE)
  val EMPTY_KEYS = Array.empty[TextAttributesKey]

end LoxSyntaxHighlighter
