// This is a generated file. Not intended for manual editing.
package generated;

import com.intellij.psi.tree.IElementType;
import com.intellij.psi.PsiElement;
import com.intellij.lang.ASTNode;
import generated.psi.impl.*;

public interface GeneratedTypes {

  IElementType EXPR = new IElementType("EXPR", null);
  IElementType FACTORIAL_EXPR = new IElementType("FACTORIAL_EXPR", null);
  IElementType LITERAL_EXPR = new IElementType("LITERAL_EXPR", null);
  IElementType MUL_EXPR = new IElementType("MUL_EXPR", null);
  IElementType PAREN_EXPR = new IElementType("PAREN_EXPR", null);
  IElementType PLUS_EXPR = new IElementType("PLUS_EXPR", null);
  IElementType PROPERTY = new IElementType("PROPERTY", null);
  IElementType REF_EXPR = new IElementType("REF_EXPR", null);

  IElementType COMMENT = new IElementType("COMMENT", null);
  IElementType EQ = new IElementType("=", null);
  IElementType FLOAT = new IElementType("float", null);
  IElementType ID = new IElementType("id", null);
  IElementType LP = new IElementType("(", null);
  IElementType NUMBER = new IElementType("number", null);
  IElementType OP_1 = new IElementType("+", null);
  IElementType OP_2 = new IElementType("-", null);
  IElementType OP_3 = new IElementType("*", null);
  IElementType OP_4 = new IElementType("/", null);
  IElementType OP_5 = new IElementType("!", null);
  IElementType RP = new IElementType(")", null);
  IElementType SEMI = new IElementType(";", null);
  IElementType STRING = new IElementType("string", null);

  class Factory {
    public static PsiElement createElement(ASTNode node) {
      IElementType type = node.getElementType();
      if (type == FACTORIAL_EXPR) {
        return new FactorialExprImpl(node);
      }
      else if (type == LITERAL_EXPR) {
        return new LiteralExprImpl(node);
      }
      else if (type == MUL_EXPR) {
        return new MulExprImpl(node);
      }
      else if (type == PAREN_EXPR) {
        return new ParenExprImpl(node);
      }
      else if (type == PLUS_EXPR) {
        return new PlusExprImpl(node);
      }
      else if (type == PROPERTY) {
        return new PropertyImpl(node);
      }
      else if (type == REF_EXPR) {
        return new RefExprImpl(node);
      }
      throw new AssertionError("Unknown element type: " + type);
    }
  }
}
