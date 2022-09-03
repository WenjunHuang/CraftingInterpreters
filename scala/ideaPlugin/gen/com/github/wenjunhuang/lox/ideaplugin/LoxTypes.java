// This is a generated file. Not intended for manual editing.
package com.github.wenjunhuang.lox.ideaplugin;

import com.intellij.psi.tree.IElementType;
import com.intellij.psi.PsiElement;
import com.intellij.lang.ASTNode;
import com.github.wenjunhuang.lox.ideaplugin.psi.impl.*;

public interface LoxTypes {

  IElementType ARGUMENTS = new LoxElementType("ARGUMENTS");
  IElementType ASSIGNMENT_EXPR = new LoxElementType("ASSIGNMENT_EXPR");
  IElementType BLOCK_STMT = new LoxElementType("BLOCK_STMT");
  IElementType CALL_EXPR = new LoxElementType("CALL_EXPR");
  IElementType COMPARISON_EXPR = new LoxElementType("COMPARISON_EXPR");
  IElementType DECLARATION = new LoxElementType("DECLARATION");
  IElementType EQUALITY_EXPR = new LoxElementType("EQUALITY_EXPR");
  IElementType EXPRESSION = new LoxElementType("EXPRESSION");
  IElementType EXPR_STMT = new LoxElementType("EXPR_STMT");
  IElementType FACTOR_EXPR = new LoxElementType("FACTOR_EXPR");
  IElementType FOR_STMT = new LoxElementType("FOR_STMT");
  IElementType FUNCTION = new LoxElementType("FUNCTION");
  IElementType FUN_DECL = new LoxElementType("FUN_DECL");
  IElementType IF_STMT = new LoxElementType("IF_STMT");
  IElementType LOGIC_AND_EXPR = new LoxElementType("LOGIC_AND_EXPR");
  IElementType LOGIC_OR_EXPR = new LoxElementType("LOGIC_OR_EXPR");
  IElementType PARAMETERS = new LoxElementType("PARAMETERS");
  IElementType PRIMARY_EXPR = new LoxElementType("PRIMARY_EXPR");
  IElementType PRINT_STMT = new LoxElementType("PRINT_STMT");
  IElementType RETURN_STMT = new LoxElementType("RETURN_STMT");
  IElementType STATEMENT = new LoxElementType("STATEMENT");
  IElementType TERM_EXPR = new LoxElementType("TERM_EXPR");
  IElementType UNARY_EXPR = new LoxElementType("UNARY_EXPR");
  IElementType VAR_DECL = new LoxElementType("VAR_DECL");
  IElementType WHILE_STMT = new LoxElementType("WHILE_STMT");

  IElementType AND = new LoxTokenType("and");
  IElementType BANG = new LoxTokenType("!");
  IElementType BANG_EQUAL = new LoxTokenType("!=");
  IElementType BLOCK_COMMENT = new LoxTokenType("BLOCK_COMMENT");
  IElementType CLASS = new LoxTokenType("class");
  IElementType COMMA = new LoxTokenType(",");
  IElementType DOT = new LoxTokenType(".");
  IElementType ELSE = new LoxTokenType("else");
  IElementType EQUAL = new LoxTokenType("=");
  IElementType EQUAL_EQUAL = new LoxTokenType("==");
  IElementType FALSE = new LoxTokenType("false");
  IElementType FOR = new LoxTokenType("for");
  IElementType FUN = new LoxTokenType("fun");
  IElementType GREATER = new LoxTokenType(">");
  IElementType GREATER_EQUAL = new LoxTokenType(">=");
  IElementType IDENTIFIER = new LoxTokenType("IDENTIFIER");
  IElementType IF = new LoxTokenType("if");
  IElementType LEFT_BRACE = new LoxTokenType("{");
  IElementType LEFT_PAREN = new LoxTokenType("(");
  IElementType LESS = new LoxTokenType("<");
  IElementType LESS_EQUAL = new LoxTokenType("<=");
  IElementType LINE_COMMENT = new LoxTokenType("LINE_COMMENT");
  IElementType MINUS = new LoxTokenType("-");
  IElementType NIL = new LoxTokenType("nil");
  IElementType NUMBER = new LoxTokenType("NUMBER");
  IElementType OR = new LoxTokenType("or");
  IElementType PLUS = new LoxTokenType("+");
  IElementType PRINT = new LoxTokenType("print");
  IElementType RETURN = new LoxTokenType("return");
  IElementType RIGHT_BRACE = new LoxTokenType("}");
  IElementType RIGHT_PAREN = new LoxTokenType(")");
  IElementType SEMICOLON = new LoxTokenType(";");
  IElementType SLASH = new LoxTokenType("/");
  IElementType STAR = new LoxTokenType("*");
  IElementType STRING = new LoxTokenType("STRING");
  IElementType SUPER = new LoxTokenType("super");
  IElementType THIS = new LoxTokenType("this");
  IElementType TRUE = new LoxTokenType("true");
  IElementType VAR = new LoxTokenType("var");
  IElementType WHILE = new LoxTokenType("while");

  class Factory {
    public static PsiElement createElement(ASTNode node) {
      IElementType type = node.getElementType();
      if (type == ARGUMENTS) {
        return new ArgumentsImpl(node);
      }
      else if (type == ASSIGNMENT_EXPR) {
        return new AssignmentExprImpl(node);
      }
      else if (type == BLOCK_STMT) {
        return new BlockStmtImpl(node);
      }
      else if (type == CALL_EXPR) {
        return new CallExprImpl(node);
      }
      else if (type == COMPARISON_EXPR) {
        return new ComparisonExprImpl(node);
      }
      else if (type == DECLARATION) {
        return new DeclarationImpl(node);
      }
      else if (type == EQUALITY_EXPR) {
        return new EqualityExprImpl(node);
      }
      else if (type == EXPR_STMT) {
        return new ExprStmtImpl(node);
      }
      else if (type == FACTOR_EXPR) {
        return new FactorExprImpl(node);
      }
      else if (type == FOR_STMT) {
        return new ForStmtImpl(node);
      }
      else if (type == FUNCTION) {
        return new FunctionImpl(node);
      }
      else if (type == FUN_DECL) {
        return new FunDeclImpl(node);
      }
      else if (type == IF_STMT) {
        return new IfStmtImpl(node);
      }
      else if (type == LOGIC_AND_EXPR) {
        return new LogicAndExprImpl(node);
      }
      else if (type == LOGIC_OR_EXPR) {
        return new LogicOrExprImpl(node);
      }
      else if (type == PARAMETERS) {
        return new ParametersImpl(node);
      }
      else if (type == PRIMARY_EXPR) {
        return new PrimaryExprImpl(node);
      }
      else if (type == PRINT_STMT) {
        return new PrintStmtImpl(node);
      }
      else if (type == RETURN_STMT) {
        return new ReturnStmtImpl(node);
      }
      else if (type == TERM_EXPR) {
        return new TermExprImpl(node);
      }
      else if (type == UNARY_EXPR) {
        return new UnaryExprImpl(node);
      }
      else if (type == VAR_DECL) {
        return new VarDeclImpl(node);
      }
      else if (type == WHILE_STMT) {
        return new WhileStmtImpl(node);
      }
      throw new AssertionError("Unknown element type: " + type);
    }
  }
}
