// This is a generated file. Not intended for manual editing.
package com.github.wenjunhuang.lox.ideaplugin.psi;

import java.util.List;
import org.jetbrains.annotations.*;
import com.intellij.psi.PsiElement;

public interface ForStmt extends PsiElement {

  @Nullable
  ExprStmt getExprStmt();

  @NotNull
  List<Expression> getExpressionList();

  @NotNull
  Statement getStatement();

  @Nullable
  VarDecl getVarDecl();

}
