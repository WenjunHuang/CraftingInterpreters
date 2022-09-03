package com.github.wenjunhuang.lox.ideaplugin.execution

import com.intellij.execution.Executor
import com.intellij.execution.configurations.{
  ConfigurationFactory,
  LocatableRunConfigurationOptions,
  RunConfiguration,
  RunConfigurationBase,
  RunConfigurationOptions,
  RunProfileState
}

import com.intellij.execution.configuration.EmptyRunProfileState
import com.intellij.execution.runners.ExecutionEnvironment
import com.intellij.openapi.options.SettingsEditor
import com.intellij.openapi.project.Project

class LoxRunConfiguration(project: Project, factory: ConfigurationFactory, name: String)
    extends RunConfigurationBase[LocatableRunConfigurationOptions](project, factory, name):

  override def getOptions: LoxRunConfigurationOptions = super.getOptions.asInstanceOf[LoxRunConfigurationOptions]

  def getScriptName: String = getOptions.getScriptName

  def setScriptName(name: String): Unit = getOptions.setScriptName(name)

  override def getConfigurationEditor: SettingsEditor[LoxRunConfiguration] = LoxSettingsEditor()

  override def getState(executor: Executor, environment: ExecutionEnvironment): RunProfileState = EmptyRunProfileState.INSTANCE

  override def clone(): RunConfiguration = super.clone()

end LoxRunConfiguration
