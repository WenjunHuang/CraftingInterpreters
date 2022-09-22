package com.github.wenjunhuang.lox.ideaplugin

import com.intellij.lang.CodeDocumentationAwareCommenter
import com.intellij.psi.{PsiComment, PsiElement}
import com.intellij.psi.tree.IElementType

class LoxCommenter extends CodeDocumentationAwareCommenter:
  override def getLineCommentTokenType: IElementType = LoxTypes.LINE_COMMENT

  override def getBlockCommentTokenType: IElementType = LoxTypes.BLOCK_COMMENT

  override def getDocumentationCommentTokenType: IElementType = null

  override def getDocumentationCommentPrefix: String = null

  override def getDocumentationCommentLinePrefix: String = null

  override def getDocumentationCommentSuffix: String = null

  override def isDocumentationComment(element: PsiComment): Boolean = false

  override def getLineCommentPrefix: String = "//"

  override def getBlockCommentPrefix: String = "/*"

  override def getBlockCommentSuffix: String = "*/"

  override def getCommentedBlockCommentPrefix: String = null

  override def getCommentedBlockCommentSuffix: String = null
end LoxCommenter



