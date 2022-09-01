// This is a generated file. Not intended for manual editing.
package com.github.wenjunhuang.lox.ideaplugin.psi.impl;

import java.util.List;
import org.jetbrains.annotations.*;
import com.intellij.lang.ASTNode;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiElementVisitor;
import com.intellij.psi.util.PsiTreeUtil;
import static com.github.wenjunhuang.lox.ideaplugin.LoxElementTypes.*;
import com.github.wenjunhuang.lox.ideaplugin.psi.*;

public class CallExprImpl extends ExpressionImpl implements CallExpr {

  public CallExprImpl(@NotNull ASTNode node) {
    super(node);
  }

  @Override
  public void accept(@NotNull Visitor visitor) {
    visitor.visitCallExpr(this);
  }

  @Override
  public void accept(@NotNull PsiElementVisitor visitor) {
    if (visitor instanceof Visitor) accept((Visitor)visitor);
    else super.accept(visitor);
  }

  @Override
  @NotNull
  public List<Arguments> getArgumentsList() {
    return PsiTreeUtil.getChildrenOfTypeAsList(this, Arguments.class);
  }

  @Override
  @NotNull
  public PrimaryExpr getPrimaryExpr() {
    return findNotNullChildByClass(PrimaryExpr.class);
  }

}
