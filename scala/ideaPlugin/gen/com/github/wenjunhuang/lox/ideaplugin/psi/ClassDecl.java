// This is a generated file. Not intended for manual editing.
package com.github.wenjunhuang.lox.ideaplugin.psi;

import java.util.List;
import org.jetbrains.annotations.*;
import com.intellij.psi.PsiElement;

public interface ClassDecl extends PsiElement {

  @NotNull
  List<Function> getFunctionList();

  @NotNull
  List<Initializer> getInitializerList();

  @NotNull
  PsiElement getIdentifier();

}
