// This is a generated file. Not intended for manual editing.
package com.github.wenjunhuang.lox.ideaplugin.psi.impl;

import java.util.List;
import org.jetbrains.annotations.*;
import com.intellij.lang.ASTNode;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiElementVisitor;
import com.intellij.psi.util.PsiTreeUtil;
import static com.github.wenjunhuang.lox.ideaplugin.LoxElementTypes.*;
import com.intellij.extapi.psi.ASTWrapperPsiElement;
import com.github.wenjunhuang.lox.ideaplugin.psi.*;

public class StatementImpl extends ASTWrapperPsiElement implements Statement {

  public StatementImpl(@NotNull ASTNode node) {
    super(node);
  }

  public void accept(@NotNull Visitor visitor) {
    visitor.visitStatement(this);
  }

  @Override
  public void accept(@NotNull PsiElementVisitor visitor) {
    if (visitor instanceof Visitor) accept((Visitor)visitor);
    else super.accept(visitor);
  }

  @Override
  @Nullable
  public BlockStmt getBlockStmt() {
    return findChildByClass(BlockStmt.class);
  }

  @Override
  @Nullable
  public ExprStmt getExprStmt() {
    return findChildByClass(ExprStmt.class);
  }

  @Override
  @Nullable
  public ForStmt getForStmt() {
    return findChildByClass(ForStmt.class);
  }

  @Override
  @Nullable
  public IfStmt getIfStmt() {
    return findChildByClass(IfStmt.class);
  }

  @Override
  @Nullable
  public PrintStmt getPrintStmt() {
    return findChildByClass(PrintStmt.class);
  }

  @Override
  @Nullable
  public ReturnStmt getReturnStmt() {
    return findChildByClass(ReturnStmt.class);
  }

  @Override
  @Nullable
  public WhileStmt getWhileStmt() {
    return findChildByClass(WhileStmt.class);
  }

}
