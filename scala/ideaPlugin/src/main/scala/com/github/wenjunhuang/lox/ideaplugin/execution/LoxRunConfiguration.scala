package com.github.wenjunhuang.lox.ideaplugin.execution

import com.github.wenjunhuang.lox.interpreter.Program
import com.intellij.execution.configuration.EmptyRunProfileState
import com.intellij.execution.configurations.*
import com.intellij.execution.impl.ConsoleViewImpl
import com.intellij.execution.process.{ProcessAdapter, ProcessEvent, ProcessHandler, ProcessOutputType}
import com.intellij.execution.runners.{ExecutionEnvironment, ProgramRunner}
import com.intellij.execution.ui.ConsoleViewContentType
import com.intellij.execution.{DefaultExecutionResult, ExecutionResult, Executor}
import com.intellij.openapi.options.SettingsEditor
import com.intellij.openapi.project.Project
import com.intellij.openapi.util.Disposer

import java.io.{File,ByteArrayOutputStream, OutputStream, PrintStream}
import java.nio.charset.StandardCharsets
import scala.util.Using

class LoxRunConfiguration(project: Project, factory: ConfigurationFactory, name: String)
    extends RunConfigurationBase[LocatableRunConfigurationOptions](project, factory, name):
  self =>

  override def getOptions: LoxRunConfigurationOptions = super.getOptions.asInstanceOf[LoxRunConfigurationOptions]

  def getScriptName: String = getOptions.getScriptName

  def setScriptName(name: String): Unit = getOptions.setScriptName(name)

  override def getConfigurationEditor: SettingsEditor[LoxRunConfiguration] = LoxSettingsEditor(project)

  override def getState(executor: Executor, environment: ExecutionEnvironment): RunProfileState =
    (executor: Executor, runner: ProgramRunner[_]) =>
      val consoleView = ConsoleViewImpl(project,true)
      val processHandler = new ProcessHandler() {
        override def destroyProcessImpl(): Unit =
          notifyProcessTerminated(0)

        override def detachProcessImpl(): Unit =
          notifyProcessDetached()

        override def detachIsDefault(): Boolean = true

        override def getProcessInput: OutputStream = null
      }
      processHandler.addProcessListener(new ProcessAdapter() {
        override def startNotified(event: ProcessEvent): Unit =
          Using.resource(new ByteArrayOutputStream()) { baos =>
            Program.runFile(File(self.getScriptName),new PrintStream(baos))
            event.getProcessHandler.notifyTextAvailable(baos.toString(StandardCharsets.UTF_8), ProcessOutputType.STDOUT)
            event.getProcessHandler.destroyProcess()
          }
      },consoleView)
      consoleView.attachToProcess(processHandler)
      DefaultExecutionResult(consoleView,processHandler)

  override def clone(): RunConfiguration = super.clone()

end LoxRunConfiguration
