package com.github.wenjunhuang.lox.ideaplugin;

import com.intellij.lexer.FlexLexer;
import com.intellij.psi.tree.IElementType;

import static com.intellij.psi.TokenType.BAD_CHARACTER;
import static com.intellij.psi.TokenType.WHITE_SPACE;
import static com.github.wenjunhuang.lox.ideaplugin.LoxTypes.*;

%%

%{
  public _LoxLexer() {
    this((java.io.Reader)null);
  }
%}

%public
%class _LoxLexer
%implements FlexLexer
%function advance
%type IElementType
%unicode

EOL=\R
WHITE_SPACE=\s+

LINE_COMMENT="//".*
BLOCK_COMMENT="/"\*(.|\n)*\*"/"
NUMBER=[0-9]+(\.[0-9]*)?
IDENTIFIER=[:letter:][a-zA-Z_0-9]*
STRING=('([^'\\]\\.)*'|\"([^\"\\]|\\.)*\")
SPACE=[ \t\n\x0B\f\r]+

%%
<YYINITIAL> {
  {WHITE_SPACE}        { return WHITE_SPACE; }

  ";"                  { return SEMICOLON; }
  "fun"                { return FUN; }
  "if"                 { return IF; }
  "else"               { return ELSE; }
  "for"                { return FOR; }
  "while"              { return WHILE; }
  "="                  { return EQUAL; }
  "=="                 { return EQUAL_EQUAL; }
  "("                  { return LEFT_PAREN; }
  ")"                  { return RIGHT_PAREN; }
  "{"                  { return LEFT_BRACE; }
  "}"                  { return RIGHT_BRACE; }
  ","                  { return COMMA; }
  "."                  { return DOT; }
  "-"                  { return MINUS; }
  "+"                  { return PLUS; }
  "/"                  { return SLASH; }
  "*"                  { return STAR; }
  "!"                  { return BANG; }
  "!="                 { return BANG_EQUAL; }
  ">"                  { return GREATER; }
  ">="                 { return GREATER_EQUAL; }
  "<"                  { return LESS; }
  "<="                 { return LESS_EQUAL; }
  "and"                { return AND; }
  "or"                 { return OR; }
  "class"              { return CLASS; }
  "true"               { return TRUE; }
  "false"              { return FALSE; }
  "nil"                { return NIL; }
  "print"              { return PRINT; }
  "return"             { return RETURN; }
  "super"              { return SUPER; }
  "init"               { return INIT; }
  "this"               { return THIS; }
  "var"                { return VAR; }

  {LINE_COMMENT}       { return LINE_COMMENT; }
  {BLOCK_COMMENT}      { return BLOCK_COMMENT; }
  {NUMBER}             { return NUMBER; }
  {IDENTIFIER}         { return IDENTIFIER; }
  {STRING}             { return STRING; }
  {SPACE}              { return SPACE; }

}

[^] { return BAD_CHARACTER; }
