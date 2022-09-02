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

public class EqualityExprImpl extends ASTWrapperPsiElement implements EqualityExpr {

  public EqualityExprImpl(@NotNull ASTNode node) {
    super(node);
  }

  public void accept(@NotNull Visitor visitor) {
    visitor.visitEqualityExpr(this);
  }

  @Override
  public void accept(@NotNull PsiElementVisitor visitor) {
    if (visitor instanceof Visitor) accept((Visitor)visitor);
    else super.accept(visitor);
  }

  @Override
  @NotNull
  public List<ComparisonExpr> getComparisonExprList() {
    return PsiTreeUtil.getChildrenOfTypeAsList(this, ComparisonExpr.class);
  }

}
