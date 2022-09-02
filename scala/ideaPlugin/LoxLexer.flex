package com.github.wenjunhuang.lox.ideaplugin;

import com.intellij.lexer.FlexLexer;
import com.intellij.psi.tree.IElementType;

import static com.intellij.psi.TokenType.BAD_CHARACTER;
import static com.intellij.psi.TokenType.WHITE_SPACE;
import static com.github.wenjunhuang.lox.ideaplugin.LoxElementTypes.*;

%%

%{
  public LoxLexer() {
    this((java.io.Reader)null);
  }
%}

%public
%class LoxLexer
%implements FlexLexer
%function advance
%type IElementType
%unicode

EOL=\R
WHITE_SPACE=\s+

SPACE=[ \t\n\x0B\f\r]+
LINE_COMMENT="//".*
BLOCK_COMMENT="/"\*(.|\n)*\*"/"
NUMBER=[0-9]+(\.[0-9]*)?
IDENTIFIER=[:letter:][a-zA-Z_0-9]*
STRING=('([^'\\]\\.)*'|\"([^\"\\]|\\.)*\")

%%
<YYINITIAL> {
  {WHITE_SPACE}        { return WHITE_SPACE; }


  {SPACE}              { return SPACE; }
  {LINE_COMMENT}       { return LINE_COMMENT; }
  {BLOCK_COMMENT}      { return BLOCK_COMMENT; }
  {NUMBER}             { return NUMBER; }
  {IDENTIFIER}         { return IDENTIFIER; }
  {STRING}             { return STRING; }

}

[^] { return BAD_CHARACTER; }
