package com.github.wenjunhuang.lox.ideaplugin
import com.intellij.openapi.editor.colors.TextAttributesKey
import com.intellij.openapi.fileTypes.SyntaxHighlighter
import com.intellij.openapi.options.colors.{AttributesDescriptor, ColorDescriptor, ColorSettingsPage}

import java.util
import javax.swing.Icon
import scala.jdk.CollectionConverters.*

class LoxColorSettingsPage extends ColorSettingsPage:
  import LoxColorSettingsPage.*

  override def getIcon: Icon = Assets.FILE

  override def getHighlighter: SyntaxHighlighter = LoxSyntaxHighlighter()

  override def getDemoText: String = """var a = 1;
                                       | var b = "This is some string";
                                       | // This is a for loop
                                       | for (var c = 1;  c < 10; c = c + 1) {
                                       |  print "This is round" + c;
                                       | }
                                       | /**
                                       |  This is a while loop
                                       | */
                                       | while (a < 10) {
                                       | print "This is round" + a;
                                       | a = a + 1;
                                       | }
                                       | fun greeting(name){
                                       | }
                                       |""".stripMargin

  override def getAdditionalHighlightingTagToDescriptorMap: util.Map[String, TextAttributesKey] = Map.empty.asJava

  override def getAttributeDescriptors: Array[AttributesDescriptor] = ATTRIBUTES

  override def getColorDescriptors: Array[ColorDescriptor] = ColorDescriptor.EMPTY_ARRAY

  override def getDisplayName: String = "Lox"
end LoxColorSettingsPage

object LoxColorSettingsPage:
  val ATTRIBUTES: Array[AttributesDescriptor] = Array(
    new AttributesDescriptor("Keyword", LoxSyntaxHighlighter.KEY),
    new AttributesDescriptor("Number", LoxSyntaxHighlighter.NUMBER_VALUE),
    new AttributesDescriptor("String", LoxSyntaxHighlighter.STRING_VALUE),
    new AttributesDescriptor("Line Comment", LoxSyntaxHighlighter.LINE_COMMENT),
    new AttributesDescriptor("Block Comment", LoxSyntaxHighlighter.BLOCK_COMMENT)
  )
end LoxColorSettingsPage
