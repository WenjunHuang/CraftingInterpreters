package com.github.wenjunhuang.lox.ideaplugin

import com.intellij.psi.tree.IElementType

class LoxTokenType(debugName: String) extends IElementType(debugName, Lox):
  override def toString: String = s"LoxTokenType:$debugName"
end LoxTokenType
