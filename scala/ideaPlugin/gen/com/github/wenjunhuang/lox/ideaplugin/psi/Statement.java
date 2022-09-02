// This is a generated file. Not intended for manual editing.
package com.github.wenjunhuang.lox.ideaplugin.psi;

import java.util.List;
import org.jetbrains.annotations.*;
import com.intellij.psi.PsiElement;

public interface Statement extends PsiElement {

  @Nullable
  BlockStmt getBlockStmt();

  @Nullable
  ExprStmt getExprStmt();

  @Nullable
  ForStmt getForStmt();

  @Nullable
  IfStmt getIfStmt();

  @Nullable
  PrintStmt getPrintStmt();

  @Nullable
  ReturnStmt getReturnStmt();

  @Nullable
  WhileStmt getWhileStmt();

}
