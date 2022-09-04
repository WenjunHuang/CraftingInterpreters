// This is a generated file. Not intended for manual editing.
package generated;

import com.intellij.psi.tree.IElementType;
import com.intellij.psi.PsiElement;
import com.intellij.lang.ASTNode;
import generated.psi.impl.*;

public interface GeneratedTypes {

  IElementType COMPARISON_EXPR = new IElementType("COMPARISON_EXPR", null);
  IElementType DECLARATION = new IElementType("DECLARATION", null);
  IElementType EQUALITY_EXPR = new IElementType("EQUALITY_EXPR", null);
  IElementType EXPRESSION = new IElementType("EXPRESSION", null);
  IElementType EXPR_STMT = new IElementType("EXPR_STMT", null);
  IElementType FACTOR_EXPR = new IElementType("FACTOR_EXPR", null);
  IElementType PRIMARY_EXPR = new IElementType("PRIMARY_EXPR", null);
  IElementType PRINT_STMT = new IElementType("PRINT_STMT", null);
  IElementType STATEMENT = new IElementType("STATEMENT", null);
  IElementType TERM_EXPR = new IElementType("TERM_EXPR", null);
  IElementType UNARY_EXPR = new IElementType("UNARY_EXPR", null);
  IElementType VAR_DECL = new IElementType("VAR_DECL", null);

  IElementType COMMENT = new IElementType("COMMENT", null);
  IElementType IDENTIFIER = new IElementType("IDENTIFIER", null);
  IElementType NUMBER = new IElementType("NUMBER", null);
  IElementType STRING = new IElementType("STRING", null);

  class Factory {
    public static PsiElement createElement(ASTNode node) {
      IElementType type = node.getElementType();
      if (type == COMPARISON_EXPR) {
        return new ComparisonExprImpl(node);
      }
      else if (type == DECLARATION) {
        return new DeclarationImpl(node);
      }
      else if (type == EQUALITY_EXPR) {
        return new EqualityExprImpl(node);
      }
      else if (type == EXPR_STMT) {
        return new ExprStmtImpl(node);
      }
      else if (type == FACTOR_EXPR) {
        return new FactorExprImpl(node);
      }
      else if (type == PRIMARY_EXPR) {
        return new PrimaryExprImpl(node);
      }
      else if (type == PRINT_STMT) {
        return new PrintStmtImpl(node);
      }
      else if (type == TERM_EXPR) {
        return new TermExprImpl(node);
      }
      else if (type == UNARY_EXPR) {
        return new UnaryExprImpl(node);
      }
      else if (type == VAR_DECL) {
        return new VarDeclImpl(node);
      }
      throw new AssertionError("Unknown element type: " + type);
    }
  }
}
