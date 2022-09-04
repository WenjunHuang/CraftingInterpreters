package com.github.wenjunhuang.lox.ideaplugin.execution

import com.intellij.openapi.options.SettingsEditor
import com.intellij.openapi.ui.{LabeledComponent, TextFieldWithBrowseButton}
import com.intellij.uiDesigner.core.{GridConstraints, GridLayoutManager, Spacer}

import javax.swing.{JComponent, JLabel, JPanel}
import scala.swing.GridPanel
import com.github.wenjunhuang.lox.ideaplugin.ui.all.*

class LoxSettingsEditor extends SettingsEditor[LoxRunConfiguration]:
  import LoxSettingsEditor.*

  //  private lazy val scriptName = new LabeledComponent[TextFieldWithBrowseButton]():
  //    setText("Script Name")
  //    setComponent(TextFieldWithBrowseButton())
  private lazy val scriptName: LabeledComponent[TextFieldWithBrowseButton] = labeledComponent {
    setText("Script Name")
    setComponent(TextFieldWithBrowseButton())
//    text := "Script Name"
// component := textFieldWithBrowseButton()
  }

  private lazy val panel = new JPanel(GridLayoutManager(2, 1)):
    private val c1 = GridConstraints()
    c1.setRow(0)
    c1.setColumn(0)
    c1.setFill(GridConstraints.FILL_HORIZONTAL)
    c1.setVSizePolicy(3)
    c1.setUseParentLayout(false)

    add(scriptName, c1)

    private val c2 = GridConstraints()
    c2.setRow(1)
    c2.setFill(GridConstraints.FILL_HORIZONTAL | GridConstraints.FILL_VERTICAL)
    c2.setVSizePolicy(6)
    c2.setUseParentLayout(false)
    c2.setColumn(0)
    add(Spacer(), c2)

  override def resetEditorFrom(s: LoxRunConfiguration): Unit = scriptName.getComponent.setText(s.getScriptName)

  override def applyEditorTo(s: LoxRunConfiguration): Unit =
    s.setScriptName(scriptName.getComponent.getText)

  override def createEditor(): JComponent = panel

end LoxSettingsEditor

object LoxSettingsEditor:

end LoxSettingsEditor
