package com.github.wenjunhuang.lox.ideaplugin
import com.intellij.formatting.{FormattingContext, FormattingModel, FormattingModelBuilder}
import com.intellij.psi.formatter.DocumentBasedFormattingModel

class LoxFormattingModelBuilder extends FormattingModelBuilder:
  override def createModel(formattingContext: FormattingContext): FormattingModel =
    val psiFile = formattingContext.getContainingFile
    val settings = formattingContext.getCodeStyleSettings
    val rootNode = formattingContext.getNode

    DocumentBasedFormattingModel()

end LoxFormattingModelBuilder

