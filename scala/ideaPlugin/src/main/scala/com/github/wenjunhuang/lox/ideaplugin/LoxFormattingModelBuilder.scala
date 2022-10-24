package com.github.wenjunhuang.lox.ideaplugin
import com.intellij.formatting.{FormattingContext, FormattingModel, FormattingModelBuilder}

class LoxFormattingModelBuilder extends FormattingModelBuilder:
  override def createModel(formattingContext: FormattingContext): FormattingModel =
    val psiFile = formattingContext.getContainingFile
    val settings = formattingContext.getCodeStyleSettings
//    val rootnode = formattingcontext.getnode
//    val context =
    ???

end LoxFormattingModelBuilder

