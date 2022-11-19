package com.github.wenjunhuang.lox.ideaplugin.formatter

import com.intellij.formatting.templateLanguages.BlockWithParent
import com.intellij.formatting.{Alignment, Block, Spacing, Wrap}
import com.intellij.lang.ASTNode
import com.intellij.psi.formatter.common.AbstractBlock

import java.util

class LoxBlock(node: ASTNode, wrap: Wrap, alignment: Alignment) extends AbstractBlock(node, wrap, alignment)
    with BlockWithParent:
  override def buildChildren(): util.List[Block] = ???

  override def getSpacing(child1: Block, child2: Block): Spacing = ???

  override def isLeaf: Boolean = ???

  override def getParent: BlockWithParent = ???

  override def setParent(newParent: BlockWithParent): Unit = ???
end LoxBlock
