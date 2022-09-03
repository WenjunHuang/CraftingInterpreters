// This is a generated file. Not intended for manual editing.
package com.github.wenjunhuang.lox.ideaplugin.psi;

import org.jetbrains.annotations.*;
import com.intellij.psi.PsiElementVisitor;
import com.intellij.psi.PsiElement;

public class Visitor extends PsiElementVisitor {

  public void visitArguments(@NotNull Arguments o) {
    visitPsiElement(o);
  }

  public void visitAssignmentExpr(@NotNull AssignmentExpr o) {
    visitExpression(o);
  }

  public void visitBlockStmt(@NotNull BlockStmt o) {
    visitStatement(o);
  }

  public void visitCallExpr(@NotNull CallExpr o) {
    visitExpression(o);
  }

  public void visitComparisonExpr(@NotNull ComparisonExpr o) {
    visitExpression(o);
  }

  public void visitDeclaration(@NotNull Declaration o) {
    visitPsiElement(o);
  }

  public void visitEqualityExpr(@NotNull EqualityExpr o) {
    visitExpression(o);
  }

  public void visitExprStmt(@NotNull ExprStmt o) {
    visitStatement(o);
  }

  public void visitExpression(@NotNull Expression o) {
    visitPsiElement(o);
  }

  public void visitFactorExpr(@NotNull FactorExpr o) {
    visitExpression(o);
  }

  public void visitForStmt(@NotNull ForStmt o) {
    visitStatement(o);
  }

  public void visitFunDecl(@NotNull FunDecl o) {
    visitPsiElement(o);
  }

  public void visitFunction(@NotNull Function o) {
    visitPsiElement(o);
  }

  public void visitIfStmt(@NotNull IfStmt o) {
    visitStatement(o);
  }

  public void visitLogicAndExpr(@NotNull LogicAndExpr o) {
    visitExpression(o);
  }

  public void visitLogicOrExpr(@NotNull LogicOrExpr o) {
    visitExpression(o);
  }

  public void visitParameters(@NotNull Parameters o) {
    visitPsiElement(o);
  }

  public void visitPrimaryExpr(@NotNull PrimaryExpr o) {
    visitExpression(o);
  }

  public void visitPrintStmt(@NotNull PrintStmt o) {
    visitStatement(o);
  }

  public void visitReturnStmt(@NotNull ReturnStmt o) {
    visitStatement(o);
  }

  public void visitStatement(@NotNull Statement o) {
    visitPsiElement(o);
  }

  public void visitTermExpr(@NotNull TermExpr o) {
    visitExpression(o);
  }

  public void visitUnaryExpr(@NotNull UnaryExpr o) {
    visitExpression(o);
  }

  public void visitVarDecl(@NotNull VarDecl o) {
    visitPsiElement(o);
  }

  public void visitWhileStmt(@NotNull WhileStmt o) {
    visitStatement(o);
  }

  public void visitPsiElement(@NotNull PsiElement o) {
    visitElement(o);
  }

}
