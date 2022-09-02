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

public class ForStmtImpl extends ASTWrapperPsiElement implements ForStmt {

  public ForStmtImpl(@NotNull ASTNode node) {
    super(node);
  }

  public void accept(@NotNull Visitor visitor) {
    visitor.visitForStmt(this);
  }

  @Override
  public void accept(@NotNull PsiElementVisitor visitor) {
    if (visitor instanceof Visitor) accept((Visitor)visitor);
    else super.accept(visitor);
  }

  @Override
  @Nullable
  public ExprStmt getExprStmt() {
    return findChildByClass(ExprStmt.class);
  }

  @Override
  @NotNull
  public List<Expression> getExpressionList() {
    return PsiTreeUtil.getChildrenOfTypeAsList(this, Expression.class);
  }

  @Override
  @NotNull
  public Statement getStatement() {
    return findNotNullChildByClass(Statement.class);
  }

  @Override
  @Nullable
  public VarDecl getVarDecl() {
    return findChildByClass(VarDecl.class);
  }

}
