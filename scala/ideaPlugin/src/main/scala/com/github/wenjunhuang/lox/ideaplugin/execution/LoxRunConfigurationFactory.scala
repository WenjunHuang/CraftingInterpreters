package com.github.wenjunhuang.lox.ideaplugin.execution

import com.intellij.execution.RunManager
import com.intellij.execution.configurations.{ConfigurationFactory, RunConfiguration}
import com.intellij.openapi.components.BaseState
import com.intellij.openapi.project.Project

class LoxRunConfigurationFactory(configType: LoxRunConfigurationType) extends ConfigurationFactory(configType):
  override def createTemplateConfiguration(project: Project): RunConfiguration =
    LoxRunConfiguration(project, this, "Lox")

  override def getId: String = LoxRunConfigurationType.ID

  override def getOptionsClass: Class[? <: BaseState] = classOf[LoxRunConfigurationOptions]

end LoxRunConfigurationFactory
