// This is a generated file. Not intended for manual editing.
package com.github.wenjunhuang.lox.ideaplugin.psi;

import java.util.List;
import org.jetbrains.annotations.*;
import com.intellij.psi.PsiElement;

public interface AssignmentExpr extends Expression {

  @NotNull
  List<Expression> getExpressionList();

  @Nullable
  PsiElement getIdentifier();

}
