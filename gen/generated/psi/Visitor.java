// This is a generated file. Not intended for manual editing.
package generated.psi;

import org.jetbrains.annotations.*;
import com.intellij.psi.PsiElementVisitor;
import com.intellij.psi.PsiElement;

public class Visitor extends PsiElementVisitor {

  public void visitComparisonExpr(@NotNull ComparisonExpr o) {
    visitExpression(o);
  }

  public void visitDeclaration(@NotNull Declaration o) {
    visitPsiElement(o);
  }

  public void visitEqualityExpr(@NotNull EqualityExpr o) {
    visitExpression(o);
  }

  public void visitExprStmt(@NotNull ExprStmt o) {
    visitStatement(o);
  }

  public void visitExpression(@NotNull Expression o) {
    visitPsiElement(o);
  }

  public void visitFactorExpr(@NotNull FactorExpr o) {
    visitExpression(o);
  }

  public void visitPrimaryExpr(@NotNull PrimaryExpr o) {
    visitExpression(o);
  }

  public void visitPrintStmt(@NotNull PrintStmt o) {
    visitStatement(o);
  }

  public void visitStatement(@NotNull Statement o) {
    visitPsiElement(o);
  }

  public void visitTermExpr(@NotNull TermExpr o) {
    visitExpression(o);
  }

  public void visitUnaryExpr(@NotNull UnaryExpr o) {
    visitExpression(o);
  }

  public void visitVarDecl(@NotNull VarDecl o) {
    visitPsiElement(o);
  }

  public void visitPsiElement(@NotNull PsiElement o) {
    visitElement(o);
  }

}
