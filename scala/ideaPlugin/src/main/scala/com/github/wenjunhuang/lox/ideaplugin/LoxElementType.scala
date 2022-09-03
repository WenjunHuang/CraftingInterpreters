package com.github.wenjunhuang.lox.ideaplugin

import com.intellij.psi.tree.IElementType

class LoxElementType(debugName: String) extends IElementType(debugName, Lox):
  override def toString: String = s"LoxElementType:$debugName"
end LoxElementType

