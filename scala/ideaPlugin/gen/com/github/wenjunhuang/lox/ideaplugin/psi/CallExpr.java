// This is a generated file. Not intended for manual editing.
package com.github.wenjunhuang.lox.ideaplugin.psi;

import java.util.List;
import org.jetbrains.annotations.*;
import com.intellij.psi.PsiElement;

public interface CallExpr extends Expression {

  @NotNull
  List<Arguments> getArgumentsList();

  @NotNull
  PrimaryExpr getPrimaryExpr();

}
