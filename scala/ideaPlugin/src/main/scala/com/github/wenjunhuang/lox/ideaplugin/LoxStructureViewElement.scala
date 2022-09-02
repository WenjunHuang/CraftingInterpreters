package com.github.wenjunhuang.lox.ideaplugin

import com.github.wenjunhuang.lox.ideaplugin.psi.impl.DeclarationImpl
import com.github.wenjunhuang.lox.ideaplugin.psi.{Declaration, LoxFile}
import com.intellij.ide.projectView.PresentationData
import com.intellij.ide.structureView.StructureViewTreeElement
import com.intellij.ide.util.treeView.smartTree.{SortableTreeElement, TreeElement}
import com.intellij.navigation.ItemPresentation
import com.intellij.psi.NavigatablePsiElement
import com.intellij.psi.util.PsiTreeUtil

import scala.jdk.CollectionConverters.*

class LoxStructureViewElement(myElement: NavigatablePsiElement)
    extends StructureViewTreeElement
    with SortableTreeElement:
  override def getValue: AnyRef = myElement

  override def getAlphaSortKey: String =
    myElement.getName match
      case name => name
      case null => ""

  override def getPresentation: ItemPresentation =
    myElement.getPresentation match
      case presentation => presentation
      case null         => new PresentationData()

  override def getChildren: Array[TreeElement] =
    myElement match
      case file: LoxFile =>
        PsiTreeUtil
          .getChildrenOfTypeAsList(file, classOf[Declaration])
          .asScala
          .map { declaration => LoxStructureViewElement(declaration.asInstanceOf[DeclarationImpl]) }
          .toArray
      case _ => Array.empty

  override def navigate(requestFocus: Boolean): Unit = ???

  override def canNavigate: Boolean = ???

  override def canNavigateToSource: Boolean = ???
end LoxStructureViewElement
