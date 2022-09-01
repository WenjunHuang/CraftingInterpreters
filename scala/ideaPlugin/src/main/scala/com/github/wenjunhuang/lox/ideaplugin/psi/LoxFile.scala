package com.github.wenjunhuang.lox.ideaplugin.psi

import com.github.wenjunhuang.lox.ideaplugin.{Lox, LoxFileType}
import com.intellij.extapi.psi.PsiFileBase
import com.intellij.openapi.fileTypes.FileType
import com.intellij.psi.FileViewProvider

class LoxFile(viewProvider: FileViewProvider) extends PsiFileBase(viewProvider, Lox):
  override def getFileType: FileType = LoxFileType
end LoxFile
