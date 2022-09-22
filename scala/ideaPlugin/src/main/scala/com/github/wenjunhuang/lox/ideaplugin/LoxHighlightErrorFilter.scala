package com.github.wenjunhuang.lox.ideaplugin

import com.intellij.codeInsight.highlighting.HighlightErrorFilter
import com.intellij.psi.PsiErrorElement

class LoxHighlightErrorFilter extends HighlightErrorFilter:
  override def shouldHighlightErrorElement(element: PsiErrorElement): Boolean = false
end LoxHighlightErrorFilter

