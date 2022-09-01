package com.github.wenjunhuang.lox.ideaplugin

import com.intellij.openapi.fileTypes.LanguageFileType

import javax.swing.Icon

object LoxFileType extends LanguageFileType(Lox):
  override def getName: String = "Lox File"

  override def getDescription: String = "Lox language file"

  override def getDefaultExtension: String = "lox"

  override def getIcon: Icon = Assets.FILE
end LoxFileType
