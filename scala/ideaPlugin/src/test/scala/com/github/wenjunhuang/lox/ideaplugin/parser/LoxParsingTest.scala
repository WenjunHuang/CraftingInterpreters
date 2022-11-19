package com.github.wenjunhuang.lox.ideaplugin.parser

import com.github.wenjunhuang.lox.ideaplugin.LoxParserDefinition
import com.github.wenjunhuang.lox.ideaplugin.utils.TestUtils.BaseDataPath
import com.intellij.testFramework.ParsingTestCase

class LoxParsingTest extends ParsingTestCase("parser", "lox", new LoxParserDefinition()):
  override def getTestDataPath: String                            = BaseDataPath
  override def getTestName(lowercaseFirstLetter: Boolean): String = super.getTestName(lowercaseFirstLetter)
  override def skipSpaces: Boolean                                = true
  override def includeRanges(): Boolean                           = true

  def testVariableDeclarations(): Unit = doTest(true)
  def testFunctionDeclaration(): Unit  = doTest(true)
  def testWhileStatement(): Unit       = doTest(true)
end LoxParsingTest
