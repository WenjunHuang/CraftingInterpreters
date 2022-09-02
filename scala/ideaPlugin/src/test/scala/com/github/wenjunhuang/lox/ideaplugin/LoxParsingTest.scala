package com.github.wenjunhuang.lox.ideaplugin

import com.intellij.testFramework.ParsingTestCase

class LoxParsingTest extends ParsingTestCase("", "lox", new LoxParserDefinition()):
  override def getTestDataPath: String = "src/test/testData"
  override def getTestName(lowercaseFirstLetter: Boolean): String = super.getTestName(lowercaseFirstLetter)
  def testParsing(): Unit = doTest(true)
end LoxParsingTest
