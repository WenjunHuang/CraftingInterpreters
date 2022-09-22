package com.github.wenjunhuang.lox.ideaplugin

import com.github.wenjunhuang.lox.ideaplugin.psi.PrimaryExpr
import com.intellij.lang.annotation.{AnnotationHolder, Annotator, HighlightSeverity}
import com.intellij.psi.PsiElement
import org.apache.commons.lang3.StringUtils

class LoxAnnotator extends Annotator:
  override def annotate(element: PsiElement, holder: AnnotationHolder): Unit =
    element match
      case pe: PrimaryExpr =>
        if StringUtils.contains(pe.getText, "fuck") then
          holder.newAnnotation(HighlightSeverity.ERROR, "h is not allowed")
            .range(pe)
            .create()
      case _               =>
end LoxAnnotator
