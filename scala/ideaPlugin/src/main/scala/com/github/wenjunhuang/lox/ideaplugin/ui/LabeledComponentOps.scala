package com.github.wenjunhuang.lox.ideaplugin.ui

import com.intellij.openapi.ui.LabeledComponent

import javax.swing.JComponent

trait LabeledComponentOps:
  def labeledComponent[T <: JComponent](init: LabeledComponent[T] ?=> Unit): LabeledComponent[T] =
    given l: LabeledComponent[T] = LabeledComponent[T]()
    init
    l

  def setComponent[T <: JComponent](component: T)(using l: LabeledComponent[T]): Unit =
    l.setComponent(component)

  def setText[T <: JComponent](text: String)(using l: LabeledComponent[T]): Unit =
    l.setText(text)

end LabeledComponentOps

object LabeledComponentOps:
end LabeledComponentOps

