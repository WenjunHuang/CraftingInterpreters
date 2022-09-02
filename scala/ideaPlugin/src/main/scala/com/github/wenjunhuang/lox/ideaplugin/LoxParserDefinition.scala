package com.github.wenjunhuang.lox.ideaplugin

import com.github.wenjunhuang.lox.ideaplugin.psi.LoxFile
import com.intellij.lang.{ASTNode, ParserDefinition, PsiParser}
import com.intellij.lexer.Lexer
import com.intellij.openapi.project.Project
import com.intellij.psi.tree.{IFileElementType, TokenSet}
import com.intellij.psi.{FileViewProvider, PsiElement, PsiFile, TokenType}

class LoxParserDefinition extends ParserDefinition:
  import LoxParserDefinition.*

  override def createLexer(project: Project): Lexer = LoxLexerAdapter()

  override def createParser(project: Project): PsiParser = LoxParser()

  override def getWhitespaceTokens: TokenSet = WHITE_SPACES

  override def getFileNodeType: IFileElementType = FILE

  override def getCommentTokens: TokenSet = COMMENTS

  override def getStringLiteralElements: TokenSet = TokenSet.EMPTY

  override def createElement(node: ASTNode): PsiElement =
    LoxElementTypes.Factory.createElement(node)

  override def createFile(viewProvider: FileViewProvider): PsiFile =
    LoxFile(viewProvider)

end LoxParserDefinition

object LoxParserDefinition:
  val FILE: IFileElementType = new IFileElementType(Lox)
  val WHITE_SPACES: TokenSet = TokenSet.create(TokenType.WHITE_SPACE)
  val COMMENTS: TokenSet = TokenSet.create(LoxElementTypes.LINE_COMMENT, LoxElementTypes.BLOCK_COMMENT)

end LoxParserDefinition
