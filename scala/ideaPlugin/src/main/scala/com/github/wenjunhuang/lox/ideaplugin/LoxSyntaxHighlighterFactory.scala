package com.github.wenjunhuang.lox.ideaplugin

import com.intellij.openapi.fileTypes.{SyntaxHighlighter, SyntaxHighlighterFactory}
import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.VirtualFile

class LoxSyntaxHighlighterFactory extends SyntaxHighlighterFactory:
  override def getSyntaxHighlighter(project: Project, virtualFile: VirtualFile): SyntaxHighlighter =
    LoxSyntaxHighlighter()
end LoxSyntaxHighlighterFactory
