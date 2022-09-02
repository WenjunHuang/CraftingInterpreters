package com.github.wenjunhuang.lox.ideaplugin

import com.intellij.ide.structureView.{StructureViewModel, StructureViewModelBase, StructureViewTreeElement}
import com.intellij.openapi.editor.Editor
import com.intellij.psi.PsiFile

class LoxStructureViewModel(editor: Editor, psiFile: PsiFile)
    extends StructureViewModelBase(psiFile, editor, LoxStructureViewElement(psiFile))
    with StructureViewModel.ElementInfoProvider:
  override def isAlwaysShowsPlus(element: StructureViewTreeElement): Boolean = false

  override def isAlwaysLeaf(element: StructureViewTreeElement): Boolean = false
end LoxStructureViewModel
