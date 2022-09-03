package com.github.wenjunhuang.lox.ideaplugin.execution

import com.github.wenjunhuang.lox.ideaplugin.Assets
import com.intellij.execution.configurations.{ConfigurationFactory, ConfigurationType}

import javax.swing.Icon

class LoxRunConfigurationType extends ConfigurationType:
  import LoxRunConfigurationType.*

  override def getDisplayName: String = "Lox"

  override def getConfigurationTypeDescription: String = "Lox run configuration type"

  override def getIcon: Icon = Assets.FILE

  override def getId: String = ID

  override def getConfigurationFactories: Array[ConfigurationFactory] = Array(LoxRunConfigurationFactory(this))
end LoxRunConfigurationType

object LoxRunConfigurationType:
  val ID = "LoxRunConfigurationType"
end LoxRunConfigurationType


