package com.github.wenjunhuang.lox.ideaplugin

import com.intellij.ide.structureView.{StructureViewBuilder, StructureViewModel, TreeBasedStructureViewBuilder}
import com.intellij.lang.PsiStructureViewFactory
import com.intellij.openapi.editor.Editor
import com.intellij.psi.PsiFile

class LoxStructureViewFactory extends  PsiStructureViewFactory:
  override def getStructureViewBuilder(psiFile: PsiFile): StructureViewBuilder =
    new TreeBasedStructureViewBuilder:
      override def createStructureViewModel(editor: Editor): StructureViewModel = LoxStructureViewModel(editor,psiFile)

end LoxStructureViewFactory
