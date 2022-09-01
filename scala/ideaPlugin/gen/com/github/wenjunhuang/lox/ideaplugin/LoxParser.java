// This is a generated file. Not intended for manual editing.
package com.github.wenjunhuang.lox.ideaplugin;

import com.intellij.lang.PsiBuilder;
import com.intellij.lang.PsiBuilder.Marker;
import static com.github.wenjunhuang.lox.ideaplugin.LoxElementTypes.*;
import static com.intellij.lang.parser.GeneratedParserUtilBase.*;
import com.intellij.psi.tree.IElementType;
import com.intellij.lang.ASTNode;
import com.intellij.psi.tree.TokenSet;
import com.intellij.lang.PsiParser;
import com.intellij.lang.LightPsiParser;

@SuppressWarnings({"SimplifiableIfStatement", "UnusedAssignment"})
public class LoxParser implements PsiParser, LightPsiParser {

  public ASTNode parse(IElementType t, PsiBuilder b) {
    parseLight(t, b);
    return b.getTreeBuilt();
  }

  public void parseLight(IElementType t, PsiBuilder b) {
    boolean r;
    b = adapt_builder_(t, b, this, EXTENDS_SETS_);
    Marker m = enter_section_(b, 0, _COLLAPSE_, null);
    r = parse_root_(t, b);
    exit_section_(b, 0, m, t, r, true, TRUE_CONDITION);
  }

  protected boolean parse_root_(IElementType t, PsiBuilder b) {
    return parse_root_(t, b, 0);
  }

  static boolean parse_root_(IElementType t, PsiBuilder b, int l) {
    return program(b, l + 1);
  }

  public static final TokenSet[] EXTENDS_SETS_ = new TokenSet[] {
    create_token_set_(BLOCK_STMT, EXPR_STMT, FOR_STMT, IF_STMT,
      PRINT_STMT, RETURN_STMT, STATEMENT, WHILE_STMT),
    create_token_set_(ASSIGNMENT_EXPR, CALL_EXPR, COMPARISON_EXPR, EQUALITY_EXPR,
      EXPRESSION, FACTOR_EXPR, LOGIC_AND_EXPR, LOGIC_OR_EXPR,
      PRIMARY_EXPR, TERM_EXPR, UNARY_EXPR),
  };

  /* ********************************************************** */
  // expression ("," expression)*
  public static boolean arguments(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "arguments")) return false;
    boolean r;
    Marker m = enter_section_(b, l, _NONE_, ARGUMENTS, "<arguments>");
    r = expression(b, l + 1);
    r = r && arguments_1(b, l + 1);
    exit_section_(b, l, m, r, false, null);
    return r;
  }

  // ("," expression)*
  private static boolean arguments_1(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "arguments_1")) return false;
    while (true) {
      int c = current_position_(b);
      if (!arguments_1_0(b, l + 1)) break;
      if (!empty_element_parsed_guard_(b, "arguments_1", c)) break;
    }
    return true;
  }

  // "," expression
  private static boolean arguments_1_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "arguments_1_0")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeToken(b, ",");
    r = r && expression(b, l + 1);
    exit_section_(b, m, null, r);
    return r;
  }

  /* ********************************************************** */
  // IDENTIFIER '=' assignmentExpr | logicOrExpr
  public static boolean assignmentExpr(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "assignmentExpr")) return false;
    boolean r;
    Marker m = enter_section_(b, l, _COLLAPSE_, ASSIGNMENT_EXPR, "<assignment expr>");
    r = assignmentExpr_0(b, l + 1);
    if (!r) r = logicOrExpr(b, l + 1);
    exit_section_(b, l, m, r, false, null);
    return r;
  }

  // IDENTIFIER '=' assignmentExpr
  private static boolean assignmentExpr_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "assignmentExpr_0")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeToken(b, IDENTIFIER);
    r = r && consumeToken(b, "=");
    r = r && assignmentExpr(b, l + 1);
    exit_section_(b, m, null, r);
    return r;
  }

  /* ********************************************************** */
  // "{" declaration * "}"
  public static boolean blockStmt(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "blockStmt")) return false;
    boolean r;
    Marker m = enter_section_(b, l, _NONE_, BLOCK_STMT, "<block stmt>");
    r = consumeToken(b, "{");
    r = r && blockStmt_1(b, l + 1);
    r = r && consumeToken(b, "}");
    exit_section_(b, l, m, r, false, null);
    return r;
  }

  // declaration *
  private static boolean blockStmt_1(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "blockStmt_1")) return false;
    while (true) {
      int c = current_position_(b);
      if (!declaration(b, l + 1)) break;
      if (!empty_element_parsed_guard_(b, "blockStmt_1", c)) break;
    }
    return true;
  }

  /* ********************************************************** */
  // primaryExpr ("(" arguments? ")")*
  public static boolean callExpr(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "callExpr")) return false;
    boolean r;
    Marker m = enter_section_(b, l, _COLLAPSE_, CALL_EXPR, "<call expr>");
    r = primaryExpr(b, l + 1);
    r = r && callExpr_1(b, l + 1);
    exit_section_(b, l, m, r, false, null);
    return r;
  }

  // ("(" arguments? ")")*
  private static boolean callExpr_1(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "callExpr_1")) return false;
    while (true) {
      int c = current_position_(b);
      if (!callExpr_1_0(b, l + 1)) break;
      if (!empty_element_parsed_guard_(b, "callExpr_1", c)) break;
    }
    return true;
  }

  // "(" arguments? ")"
  private static boolean callExpr_1_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "callExpr_1_0")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeToken(b, "(");
    r = r && callExpr_1_0_1(b, l + 1);
    r = r && consumeToken(b, ")");
    exit_section_(b, m, null, r);
    return r;
  }

  // arguments?
  private static boolean callExpr_1_0_1(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "callExpr_1_0_1")) return false;
    arguments(b, l + 1);
    return true;
  }

  /* ********************************************************** */
  // termExpr (('>'|'>='|'<'|'<=') termExpr)*
  public static boolean comparisonExpr(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "comparisonExpr")) return false;
    boolean r;
    Marker m = enter_section_(b, l, _COLLAPSE_, COMPARISON_EXPR, "<comparison expr>");
    r = termExpr(b, l + 1);
    r = r && comparisonExpr_1(b, l + 1);
    exit_section_(b, l, m, r, false, null);
    return r;
  }

  // (('>'|'>='|'<'|'<=') termExpr)*
  private static boolean comparisonExpr_1(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "comparisonExpr_1")) return false;
    while (true) {
      int c = current_position_(b);
      if (!comparisonExpr_1_0(b, l + 1)) break;
      if (!empty_element_parsed_guard_(b, "comparisonExpr_1", c)) break;
    }
    return true;
  }

  // ('>'|'>='|'<'|'<=') termExpr
  private static boolean comparisonExpr_1_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "comparisonExpr_1_0")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = comparisonExpr_1_0_0(b, l + 1);
    r = r && termExpr(b, l + 1);
    exit_section_(b, m, null, r);
    return r;
  }

  // '>'|'>='|'<'|'<='
  private static boolean comparisonExpr_1_0_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "comparisonExpr_1_0_0")) return false;
    boolean r;
    r = consumeToken(b, ">");
    if (!r) r = consumeToken(b, ">=");
    if (!r) r = consumeToken(b, "<");
    if (!r) r = consumeToken(b, "<=");
    return r;
  }

  /* ********************************************************** */
  // funDecl | varDecl | statement
  public static boolean declaration(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "declaration")) return false;
    boolean r;
    Marker m = enter_section_(b, l, _NONE_, DECLARATION, "<declaration>");
    r = funDecl(b, l + 1);
    if (!r) r = varDecl(b, l + 1);
    if (!r) r = statement(b, l + 1);
    exit_section_(b, l, m, r, false, null);
    return r;
  }

  /* ********************************************************** */
  // comparisonExpr (('!=' | '==') comparisonExpr)*
  public static boolean equalityExpr(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "equalityExpr")) return false;
    boolean r;
    Marker m = enter_section_(b, l, _COLLAPSE_, EQUALITY_EXPR, "<equality expr>");
    r = comparisonExpr(b, l + 1);
    r = r && equalityExpr_1(b, l + 1);
    exit_section_(b, l, m, r, false, null);
    return r;
  }

  // (('!=' | '==') comparisonExpr)*
  private static boolean equalityExpr_1(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "equalityExpr_1")) return false;
    while (true) {
      int c = current_position_(b);
      if (!equalityExpr_1_0(b, l + 1)) break;
      if (!empty_element_parsed_guard_(b, "equalityExpr_1", c)) break;
    }
    return true;
  }

  // ('!=' | '==') comparisonExpr
  private static boolean equalityExpr_1_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "equalityExpr_1_0")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = equalityExpr_1_0_0(b, l + 1);
    r = r && comparisonExpr(b, l + 1);
    exit_section_(b, m, null, r);
    return r;
  }

  // '!=' | '=='
  private static boolean equalityExpr_1_0_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "equalityExpr_1_0_0")) return false;
    boolean r;
    r = consumeToken(b, "!=");
    if (!r) r = consumeToken(b, "==");
    return r;
  }

  /* ********************************************************** */
  // expression ";"
  public static boolean exprStmt(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "exprStmt")) return false;
    boolean r;
    Marker m = enter_section_(b, l, _NONE_, EXPR_STMT, "<expr stmt>");
    r = expression(b, l + 1);
    r = r && consumeToken(b, ";");
    exit_section_(b, l, m, r, false, null);
    return r;
  }

  /* ********************************************************** */
  // assignmentExpr
  public static boolean expression(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "expression")) return false;
    boolean r;
    Marker m = enter_section_(b, l, _COLLAPSE_, EXPRESSION, "<expression>");
    r = assignmentExpr(b, l + 1);
    exit_section_(b, l, m, r, false, null);
    return r;
  }

  /* ********************************************************** */
  // unaryExpr (('/'|'*') unaryExpr)*
  public static boolean factorExpr(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "factorExpr")) return false;
    boolean r;
    Marker m = enter_section_(b, l, _COLLAPSE_, FACTOR_EXPR, "<factor expr>");
    r = unaryExpr(b, l + 1);
    r = r && factorExpr_1(b, l + 1);
    exit_section_(b, l, m, r, false, null);
    return r;
  }

  // (('/'|'*') unaryExpr)*
  private static boolean factorExpr_1(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "factorExpr_1")) return false;
    while (true) {
      int c = current_position_(b);
      if (!factorExpr_1_0(b, l + 1)) break;
      if (!empty_element_parsed_guard_(b, "factorExpr_1", c)) break;
    }
    return true;
  }

  // ('/'|'*') unaryExpr
  private static boolean factorExpr_1_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "factorExpr_1_0")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = factorExpr_1_0_0(b, l + 1);
    r = r && unaryExpr(b, l + 1);
    exit_section_(b, m, null, r);
    return r;
  }

  // '/'|'*'
  private static boolean factorExpr_1_0_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "factorExpr_1_0_0")) return false;
    boolean r;
    r = consumeToken(b, "/");
    if (!r) r = consumeToken(b, "*");
    return r;
  }

  /* ********************************************************** */
  // "for" "(" (varDecl | exprStmt |";") expression? ";" expression? ")" statement
  public static boolean forStmt(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "forStmt")) return false;
    boolean r;
    Marker m = enter_section_(b, l, _NONE_, FOR_STMT, "<for stmt>");
    r = consumeToken(b, "for");
    r = r && consumeToken(b, "(");
    r = r && forStmt_2(b, l + 1);
    r = r && forStmt_3(b, l + 1);
    r = r && consumeToken(b, ";");
    r = r && forStmt_5(b, l + 1);
    r = r && consumeToken(b, ")");
    r = r && statement(b, l + 1);
    exit_section_(b, l, m, r, false, null);
    return r;
  }

  // varDecl | exprStmt |";"
  private static boolean forStmt_2(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "forStmt_2")) return false;
    boolean r;
    r = varDecl(b, l + 1);
    if (!r) r = exprStmt(b, l + 1);
    if (!r) r = consumeToken(b, ";");
    return r;
  }

  // expression?
  private static boolean forStmt_3(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "forStmt_3")) return false;
    expression(b, l + 1);
    return true;
  }

  // expression?
  private static boolean forStmt_5(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "forStmt_5")) return false;
    expression(b, l + 1);
    return true;
  }

  /* ********************************************************** */
  // "fun" function
  public static boolean funDecl(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "funDecl")) return false;
    boolean r;
    Marker m = enter_section_(b, l, _NONE_, FUN_DECL, "<fun decl>");
    r = consumeToken(b, "fun");
    r = r && function(b, l + 1);
    exit_section_(b, l, m, r, false, null);
    return r;
  }

  /* ********************************************************** */
  // IDENTIFIER "(" parameters? ")" blockStmt
  public static boolean function(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "function")) return false;
    if (!nextTokenIs(b, IDENTIFIER)) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeToken(b, IDENTIFIER);
    r = r && consumeToken(b, "(");
    r = r && function_2(b, l + 1);
    r = r && consumeToken(b, ")");
    r = r && blockStmt(b, l + 1);
    exit_section_(b, m, FUNCTION, r);
    return r;
  }

  // parameters?
  private static boolean function_2(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "function_2")) return false;
    parameters(b, l + 1);
    return true;
  }

  /* ********************************************************** */
  // "if" "(" expression ")" statement ("else" statement)?
  public static boolean ifStmt(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "ifStmt")) return false;
    boolean r;
    Marker m = enter_section_(b, l, _NONE_, IF_STMT, "<if stmt>");
    r = consumeToken(b, "if");
    r = r && consumeToken(b, "(");
    r = r && expression(b, l + 1);
    r = r && consumeToken(b, ")");
    r = r && statement(b, l + 1);
    r = r && ifStmt_5(b, l + 1);
    exit_section_(b, l, m, r, false, null);
    return r;
  }

  // ("else" statement)?
  private static boolean ifStmt_5(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "ifStmt_5")) return false;
    ifStmt_5_0(b, l + 1);
    return true;
  }

  // "else" statement
  private static boolean ifStmt_5_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "ifStmt_5_0")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeToken(b, "else");
    r = r && statement(b, l + 1);
    exit_section_(b, m, null, r);
    return r;
  }

  /* ********************************************************** */
  // equalityExpr ('and' equalityExpr)*
  public static boolean logicAndExpr(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "logicAndExpr")) return false;
    boolean r;
    Marker m = enter_section_(b, l, _COLLAPSE_, LOGIC_AND_EXPR, "<logic and expr>");
    r = equalityExpr(b, l + 1);
    r = r && logicAndExpr_1(b, l + 1);
    exit_section_(b, l, m, r, false, null);
    return r;
  }

  // ('and' equalityExpr)*
  private static boolean logicAndExpr_1(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "logicAndExpr_1")) return false;
    while (true) {
      int c = current_position_(b);
      if (!logicAndExpr_1_0(b, l + 1)) break;
      if (!empty_element_parsed_guard_(b, "logicAndExpr_1", c)) break;
    }
    return true;
  }

  // 'and' equalityExpr
  private static boolean logicAndExpr_1_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "logicAndExpr_1_0")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeToken(b, "and");
    r = r && equalityExpr(b, l + 1);
    exit_section_(b, m, null, r);
    return r;
  }

  /* ********************************************************** */
  // logicAndExpr ('or' logicAndExpr)*
  public static boolean logicOrExpr(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "logicOrExpr")) return false;
    boolean r;
    Marker m = enter_section_(b, l, _COLLAPSE_, LOGIC_OR_EXPR, "<logic or expr>");
    r = logicAndExpr(b, l + 1);
    r = r && logicOrExpr_1(b, l + 1);
    exit_section_(b, l, m, r, false, null);
    return r;
  }

  // ('or' logicAndExpr)*
  private static boolean logicOrExpr_1(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "logicOrExpr_1")) return false;
    while (true) {
      int c = current_position_(b);
      if (!logicOrExpr_1_0(b, l + 1)) break;
      if (!empty_element_parsed_guard_(b, "logicOrExpr_1", c)) break;
    }
    return true;
  }

  // 'or' logicAndExpr
  private static boolean logicOrExpr_1_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "logicOrExpr_1_0")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeToken(b, "or");
    r = r && logicAndExpr(b, l + 1);
    exit_section_(b, m, null, r);
    return r;
  }

  /* ********************************************************** */
  // IDENTIFIER ("," IDENTIFIER)*
  public static boolean parameters(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "parameters")) return false;
    if (!nextTokenIs(b, IDENTIFIER)) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeToken(b, IDENTIFIER);
    r = r && parameters_1(b, l + 1);
    exit_section_(b, m, PARAMETERS, r);
    return r;
  }

  // ("," IDENTIFIER)*
  private static boolean parameters_1(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "parameters_1")) return false;
    while (true) {
      int c = current_position_(b);
      if (!parameters_1_0(b, l + 1)) break;
      if (!empty_element_parsed_guard_(b, "parameters_1", c)) break;
    }
    return true;
  }

  // "," IDENTIFIER
  private static boolean parameters_1_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "parameters_1_0")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeToken(b, ",");
    r = r && consumeToken(b, IDENTIFIER);
    exit_section_(b, m, null, r);
    return r;
  }

  /* ********************************************************** */
  // NUMBER | STRING | 'true' | 'false' | 'nil' | '(' expression ')' | IDENTIFIER
  public static boolean primaryExpr(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "primaryExpr")) return false;
    boolean r;
    Marker m = enter_section_(b, l, _NONE_, PRIMARY_EXPR, "<primary expr>");
    r = consumeToken(b, NUMBER);
    if (!r) r = consumeToken(b, STRING);
    if (!r) r = consumeToken(b, "true");
    if (!r) r = consumeToken(b, "false");
    if (!r) r = consumeToken(b, "nil");
    if (!r) r = primaryExpr_5(b, l + 1);
    if (!r) r = consumeToken(b, IDENTIFIER);
    exit_section_(b, l, m, r, false, null);
    return r;
  }

  // '(' expression ')'
  private static boolean primaryExpr_5(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "primaryExpr_5")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeToken(b, "(");
    r = r && expression(b, l + 1);
    r = r && consumeToken(b, ")");
    exit_section_(b, m, null, r);
    return r;
  }

  /* ********************************************************** */
  // "print" expression ";"
  public static boolean printStmt(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "printStmt")) return false;
    boolean r;
    Marker m = enter_section_(b, l, _NONE_, PRINT_STMT, "<print stmt>");
    r = consumeToken(b, "print");
    r = r && expression(b, l + 1);
    r = r && consumeToken(b, ";");
    exit_section_(b, l, m, r, false, null);
    return r;
  }

  /* ********************************************************** */
  // declaration * <<eof>>
  static boolean program(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "program")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = program_0(b, l + 1);
    r = r && eof(b, l + 1);
    exit_section_(b, m, null, r);
    return r;
  }

  // declaration *
  private static boolean program_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "program_0")) return false;
    while (true) {
      int c = current_position_(b);
      if (!declaration(b, l + 1)) break;
      if (!empty_element_parsed_guard_(b, "program_0", c)) break;
    }
    return true;
  }

  /* ********************************************************** */
  // "return" expression? ";"
  public static boolean returnStmt(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "returnStmt")) return false;
    boolean r;
    Marker m = enter_section_(b, l, _NONE_, RETURN_STMT, "<return stmt>");
    r = consumeToken(b, "return");
    r = r && returnStmt_1(b, l + 1);
    r = r && consumeToken(b, ";");
    exit_section_(b, l, m, r, false, null);
    return r;
  }

  // expression?
  private static boolean returnStmt_1(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "returnStmt_1")) return false;
    expression(b, l + 1);
    return true;
  }

  /* ********************************************************** */
  // printStmt | blockStmt | ifStmt | whileStmt |forStmt|returnStmt|exprStmt
  public static boolean statement(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "statement")) return false;
    boolean r;
    Marker m = enter_section_(b, l, _COLLAPSE_, STATEMENT, "<statement>");
    r = printStmt(b, l + 1);
    if (!r) r = blockStmt(b, l + 1);
    if (!r) r = ifStmt(b, l + 1);
    if (!r) r = whileStmt(b, l + 1);
    if (!r) r = forStmt(b, l + 1);
    if (!r) r = returnStmt(b, l + 1);
    if (!r) r = exprStmt(b, l + 1);
    exit_section_(b, l, m, r, false, null);
    return r;
  }

  /* ********************************************************** */
  // factorExpr (( '-'|'+') factorExpr)*
  public static boolean termExpr(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "termExpr")) return false;
    boolean r;
    Marker m = enter_section_(b, l, _COLLAPSE_, TERM_EXPR, "<term expr>");
    r = factorExpr(b, l + 1);
    r = r && termExpr_1(b, l + 1);
    exit_section_(b, l, m, r, false, null);
    return r;
  }

  // (( '-'|'+') factorExpr)*
  private static boolean termExpr_1(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "termExpr_1")) return false;
    while (true) {
      int c = current_position_(b);
      if (!termExpr_1_0(b, l + 1)) break;
      if (!empty_element_parsed_guard_(b, "termExpr_1", c)) break;
    }
    return true;
  }

  // ( '-'|'+') factorExpr
  private static boolean termExpr_1_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "termExpr_1_0")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = termExpr_1_0_0(b, l + 1);
    r = r && factorExpr(b, l + 1);
    exit_section_(b, m, null, r);
    return r;
  }

  // '-'|'+'
  private static boolean termExpr_1_0_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "termExpr_1_0_0")) return false;
    boolean r;
    r = consumeToken(b, "-");
    if (!r) r = consumeToken(b, "+");
    return r;
  }

  /* ********************************************************** */
  // ('!'|'-') unaryExpr | callExpr
  public static boolean unaryExpr(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "unaryExpr")) return false;
    boolean r;
    Marker m = enter_section_(b, l, _COLLAPSE_, UNARY_EXPR, "<unary expr>");
    r = unaryExpr_0(b, l + 1);
    if (!r) r = callExpr(b, l + 1);
    exit_section_(b, l, m, r, false, null);
    return r;
  }

  // ('!'|'-') unaryExpr
  private static boolean unaryExpr_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "unaryExpr_0")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = unaryExpr_0_0(b, l + 1);
    r = r && unaryExpr(b, l + 1);
    exit_section_(b, m, null, r);
    return r;
  }

  // '!'|'-'
  private static boolean unaryExpr_0_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "unaryExpr_0_0")) return false;
    boolean r;
    r = consumeToken(b, "!");
    if (!r) r = consumeToken(b, "-");
    return r;
  }

  /* ********************************************************** */
  // "var" IDENTIFIER ("=" expression )? ";"
  public static boolean varDecl(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "varDecl")) return false;
    boolean r;
    Marker m = enter_section_(b, l, _NONE_, VAR_DECL, "<var decl>");
    r = consumeToken(b, "var");
    r = r && consumeToken(b, IDENTIFIER);
    r = r && varDecl_2(b, l + 1);
    r = r && consumeToken(b, ";");
    exit_section_(b, l, m, r, false, null);
    return r;
  }

  // ("=" expression )?
  private static boolean varDecl_2(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "varDecl_2")) return false;
    varDecl_2_0(b, l + 1);
    return true;
  }

  // "=" expression
  private static boolean varDecl_2_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "varDecl_2_0")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeToken(b, "=");
    r = r && expression(b, l + 1);
    exit_section_(b, m, null, r);
    return r;
  }

  /* ********************************************************** */
  // "while" "(" expression ")" statement
  public static boolean whileStmt(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "whileStmt")) return false;
    boolean r;
    Marker m = enter_section_(b, l, _NONE_, WHILE_STMT, "<while stmt>");
    r = consumeToken(b, "while");
    r = r && consumeToken(b, "(");
    r = r && expression(b, l + 1);
    r = r && consumeToken(b, ")");
    r = r && statement(b, l + 1);
    exit_section_(b, l, m, r, false, null);
    return r;
  }

}
