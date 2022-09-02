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

public class IfStmtImpl extends ASTWrapperPsiElement implements IfStmt {

  public IfStmtImpl(@NotNull ASTNode node) {
    super(node);
  }

  public void accept(@NotNull Visitor visitor) {
    visitor.visitIfStmt(this);
  }

  @Override
  public void accept(@NotNull PsiElementVisitor visitor) {
    if (visitor instanceof Visitor) accept((Visitor)visitor);
    else super.accept(visitor);
  }

  @Override
  @NotNull
  public Expression getExpression() {
    return findNotNullChildByClass(Expression.class);
  }

  @Override
  @NotNull
  public List<Statement> getStatementList() {
    return PsiTreeUtil.getChildrenOfTypeAsList(this, Statement.class);
  }

}
