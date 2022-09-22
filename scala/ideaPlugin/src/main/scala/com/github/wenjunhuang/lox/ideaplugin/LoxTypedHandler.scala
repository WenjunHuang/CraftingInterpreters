package com.github.wenjunhuang.lox.ideaplugin

import com.intellij.codeInsight.editorActions.TypedHandlerDelegate
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.project.Project
import com.intellij.psi.PsiFile

class LoxTypedHandler extends TypedHandlerDelegate:
  override def charTyped(c: Char, project: Project, editor: Editor, file: PsiFile): TypedHandlerDelegate.Result = super.charTyped(c, project, editor, file)
end LoxTypedHandler

