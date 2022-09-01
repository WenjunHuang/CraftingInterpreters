package com.github.wenjunhuang.lox.ideaplugin

import com.intellij.lexer.FlexAdapter

class LoxLexerAdapter extends FlexAdapter(LoxLexer()):

end LoxLexerAdapter

