package com.github.wenjunhuang.lox.ideaplugin
import com.intellij.lang.{BracePair, PairedBraceMatcher}
import com.intellij.psi.PsiFile
import com.intellij.psi.tree.IElementType

class LoxBraceMatcher extends PairedBraceMatcher:
  import LoxBraceMatcher.*
  override def getPairs: Array[BracePair] = BracePairs

  override def isPairedBracesAllowedBeforeType(lbraceType: IElementType, contextType: IElementType): Boolean = true

  override def getCodeConstructStart(file: PsiFile, openingBraceOffset: Int): Int = openingBraceOffset
end LoxBraceMatcher

object LoxBraceMatcher:
  val BracePairs = Array(
    BracePair(LoxTypes.LEFT_BRACE, LoxTypes.RIGHT_BRACE, true),
    BracePair(LoxTypes.LEFT_PAREN, LoxTypes.RIGHT_PAREN, false)
  )

end LoxBraceMatcher
