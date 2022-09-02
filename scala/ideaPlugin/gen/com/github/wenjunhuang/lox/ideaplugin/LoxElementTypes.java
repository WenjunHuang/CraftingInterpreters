// This is a generated file. Not intended for manual editing.
package com.github.wenjunhuang.lox.ideaplugin;

import com.intellij.psi.tree.IElementType;
import com.intellij.psi.PsiElement;
import com.intellij.lang.ASTNode;
import com.github.wenjunhuang.lox.ideaplugin.psi.impl.*;

public interface LoxElementTypes {

  IElementType ARGUMENTS = new IElementType("ARGUMENTS", null);
  IElementType ASSIGNMENT_EXPR = new IElementType("ASSIGNMENT_EXPR", null);
  IElementType BLOCK_STMT = new IElementType("BLOCK_STMT", null);
  IElementType CALL_EXPR = new IElementType("CALL_EXPR", null);
  IElementType COMPARISON_EXPR = new IElementType("COMPARISON_EXPR", null);
  IElementType DECLARATION = new IElementType("DECLARATION", null);
  IElementType EQUALITY_EXPR = new IElementType("EQUALITY_EXPR", null);
  IElementType EXPRESSION = new IElementType("EXPRESSION", null);
  IElementType EXPR_STMT = new IElementType("EXPR_STMT", null);
  IElementType FACTOR_EXPR = new IElementType("FACTOR_EXPR", null);
  IElementType FOR_STMT = new IElementType("FOR_STMT", null);
  IElementType FUNCTION = new IElementType("FUNCTION", null);
  IElementType FUN_DECL = new IElementType("FUN_DECL", null);
  IElementType IF_STMT = new IElementType("IF_STMT", null);
  IElementType LOGIC_AND_EXPR = new IElementType("LOGIC_AND_EXPR", null);
  IElementType LOGIC_OR_EXPR = new IElementType("LOGIC_OR_EXPR", null);
  IElementType PARAMETERS = new IElementType("PARAMETERS", null);
  IElementType PRIMARY_EXPR = new IElementType("PRIMARY_EXPR", null);
  IElementType PRINT_STMT = new IElementType("PRINT_STMT", null);
  IElementType RETURN_STMT = new IElementType("RETURN_STMT", null);
  IElementType STATEMENT = new IElementType("STATEMENT", null);
  IElementType TERM_EXPR = new IElementType("TERM_EXPR", null);
  IElementType UNARY_EXPR = new IElementType("UNARY_EXPR", null);
  IElementType VAR_DECL = new IElementType("VAR_DECL", null);
  IElementType WHILE_STMT = new IElementType("WHILE_STMT", null);

  IElementType BLOCK_COMMENT = new LoxTokenType("BLOCK_COMMENT");
  IElementType IDENTIFIER = new LoxTokenType("IDENTIFIER");
  IElementType LINE_COMMENT = new LoxTokenType("LINE_COMMENT");
  IElementType NUMBER = new LoxTokenType("NUMBER");
  IElementType STRING = new LoxTokenType("STRING");

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
      else if (type == EXPRESSION) {
        return new ExpressionImpl(node);
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
      else if (type == STATEMENT) {
        return new StatementImpl(node);
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
