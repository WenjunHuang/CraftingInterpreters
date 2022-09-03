package com.github.wenjunhuang.lox.ideaplugin.execution

import com.intellij.execution.configurations.RunConfigurationOptions
import com.intellij.openapi.components.StoredProperty

class LoxRunConfigurationOptions extends RunConfigurationOptions:
  private val scriptName: StoredProperty[String] = string("")
    .provideDelegate(this, "scriptName")

  def getScriptName: String = scriptName.getValue(this)
  def setScriptName(name: String): Unit = scriptName.setValue(this, name)

end LoxRunConfigurationOptions
