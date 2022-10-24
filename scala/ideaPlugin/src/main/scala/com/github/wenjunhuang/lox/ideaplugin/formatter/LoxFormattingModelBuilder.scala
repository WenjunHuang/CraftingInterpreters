package com.github.wenjunhuang.lox.ideaplugin.formatter

import com.github.wenjunhuang.lox.ideaplugin.{Lox, LoxTypes}
import com.intellij.formatting.{FormattingContext, FormattingModel, FormattingModelBuilder, SpacingBuilder}
import com.intellij.psi.codeStyle.CodeStyleSettings

class LoxFormattingModelBuilder extends FormattingModelBuilder:
  override def createModel(formattingContext: FormattingContext): FormattingModel = super.createModel(formattingContext)
end LoxFormattingModelBuilder

object LoxFormattingModelBuilder:
  def createSpaceBuilder(settings:CodeStyleSettings):SpacingBuilder = ???
//    SpacingBuilder(settings,Lox)
//      .around(LoxTypes.COMMA)
//      .spaceIf(settings.getCommonSettings(Lox.getID))
//      .before(LoxTypes.SEMICOLON)



end LoxFormattingModelBuilder
