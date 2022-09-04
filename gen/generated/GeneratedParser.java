// This is a generated file. Not intended for manual editing.
package generated;

import com.intellij.lang.PsiBuilder;
import com.intellij.lang.PsiBuilder.Marker;
import static generated.GeneratedTypes.*;
import static com.intellij.lang.parser.GeneratedParserUtilBase.*;
import com.intellij.psi.tree.IElementType;
import com.intellij.lang.ASTNode;
import com.intellij.psi.tree.TokenSet;
import com.intellij.lang.PsiParser;
import com.intellij.lang.LightPsiParser;

@SuppressWarnings({"SimplifiableIfStatement", "UnusedAssignment"})
public class GeneratedParser implements PsiParser, LightPsiParser {

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
    create_token_set_(EXPR_STMT, PRINT_STMT, STATEMENT),
    create_token_set_(COMPARISON_EXPR, EQUALITY_EXPR, EXPRESSION, FACTOR_EXPR,
      PRIMARY_EXPR, TERM_EXPR, UNARY_EXPR),
  };

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
  // varDecl | statement
  public static boolean declaration(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "declaration")) return false;
    boolean r;
    Marker m = enter_section_(b, l, _NONE_, DECLARATION, "<declaration>");
    r = varDecl(b, l + 1);
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
  // equalityExpr
  public static boolean expression(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "expression")) return false;
    boolean r;
    Marker m = enter_section_(b, l, _COLLAPSE_, EXPRESSION, "<expression>");
    r = equalityExpr(b, l + 1);
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
  // exprStmt | printStmt
  public static boolean statement(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "statement")) return false;
    boolean r;
    Marker m = enter_section_(b, l, _COLLAPSE_, STATEMENT, "<statement>");
    r = exprStmt(b, l + 1);
    if (!r) r = printStmt(b, l + 1);
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
  // ('!'|'-') unaryExpr | primaryExpr
  public static boolean unaryExpr(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "unaryExpr")) return false;
    boolean r;
    Marker m = enter_section_(b, l, _COLLAPSE_, UNARY_EXPR, "<unary expr>");
    r = unaryExpr_0(b, l + 1);
    if (!r) r = primaryExpr(b, l + 1);
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

}
