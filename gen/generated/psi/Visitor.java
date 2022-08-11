// This is a generated file. Not intended for manual editing.
package generated.psi;

import org.jetbrains.annotations.*;
import com.intellij.psi.PsiElementVisitor;
import com.intellij.psi.PsiElement;

public class Visitor extends PsiElementVisitor {

  public void visitExpr(@NotNull Expr o) {
    visitPsiElement(o);
  }

  public void visitFactorialExpr(@NotNull FactorialExpr o) {
    visitExpr(o);
  }

  public void visitLiteralExpr(@NotNull LiteralExpr o) {
    visitExpr(o);
  }

  public void visitMulExpr(@NotNull MulExpr o) {
    visitExpr(o);
  }

  public void visitParenExpr(@NotNull ParenExpr o) {
    visitExpr(o);
  }

  public void visitPlusExpr(@NotNull PlusExpr o) {
    visitExpr(o);
  }

  public void visitProperty(@NotNull Property o) {
    visitPsiElement(o);
  }

  public void visitRefExpr(@NotNull RefExpr o) {
    visitExpr(o);
  }

  public void visitPsiElement(@NotNull PsiElement o) {
    visitElement(o);
  }

}
