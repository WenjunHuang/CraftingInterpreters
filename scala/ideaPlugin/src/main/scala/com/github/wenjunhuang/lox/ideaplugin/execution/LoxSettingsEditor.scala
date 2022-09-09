package com.github.wenjunhuang.lox.ideaplugin.execution

import com.github.wenjunhuang.lox.ideaplugin.LoxFileType
import com.github.wenjunhuang.lox.ideaplugin.ui.PropertyNames.*
import com.github.wenjunhuang.lox.macros.PojoBuilder.*
import com.github.wenjunhuang.lox.macros.SwingOps.*
import com.intellij.openapi.application.{Application, ApplicationManager}
import com.intellij.openapi.fileChooser.{FileChooser, FileChooserDescriptor, FileChooserDescriptorFactory}
import com.intellij.openapi.options.SettingsEditor
import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.{LabeledComponent, TextFieldWithBrowseButton}
import com.intellij.uiDesigner.core.{GridConstraints, GridLayoutManager, Spacer}

import java.awt.event.{ActionEvent, ActionListener}
import javax.swing.{JComponent, JLabel, JPanel}

class LoxSettingsEditor(project: Project) extends SettingsEditor[LoxRunConfiguration]:
  import LoxSettingsEditor.*

  private lazy val scriptName: LabeledComponent[TextFieldWithBrowseButton] =
    pojoBuilder[LabeledComponent[TextFieldWithBrowseButton]] {
      text      := "Script Name2"
      component := new TextFieldWithBrowseButton():
        addBrowseFolderListener("Lox Source File",
                                "Please choose a lox source file",
                                project,
                                FileChooserDescriptorFactory.createSingleFileDescriptor(LoxFileType)
        )

    }

  private lazy val panel = pojoBuilder[JPanel] {
    layout := GridLayoutManager(2, 1)
    add(
      scriptName,
      pojoBuilder[GridConstraints] {
        row             := 0
        column          := 0
        anchor          := GridConstraints.ANCHOR_WEST
        fill            := GridConstraints.FILL_HORIZONTAL
        vSizePolicy     := GridConstraints.SIZEPOLICY_CAN_GROW | GridConstraints.SIZEPOLICY_CAN_SHRINK
        hSizePolicy     := GridConstraints.SIZEPOLICY_FIXED
        useParentLayout := false
      }
    )

    add(
      Spacer(),
      pojoBuilder[GridConstraints] {
        row             := 1
        column          := 0
        anchor          := GridConstraints.ANCHOR_CENTER
        fill            := GridConstraints.FILL_VERTICAL | GridConstraints.FILL_HORIZONTAL
        vSizePolicy     := GridConstraints.SIZEPOLICY_WANT_GROW
        hSizePolicy     := GridConstraints.SIZEPOLICY_CAN_GROW | GridConstraints.SIZEPOLICY_CAN_SHRINK
        useParentLayout := false
      }
    )
  }

  override def resetEditorFrom(s: LoxRunConfiguration): Unit = scriptName.getComponent.setText(s.getScriptName)

  override def applyEditorTo(s: LoxRunConfiguration): Unit = s.setScriptName(scriptName.getComponent.getText)

  override def createEditor(): JComponent = panel

end LoxSettingsEditor

object LoxSettingsEditor:

end LoxSettingsEditor
