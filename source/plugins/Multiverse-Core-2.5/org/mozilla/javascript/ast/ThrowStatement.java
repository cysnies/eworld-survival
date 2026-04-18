package org.mozilla.javascript.ast;

public class ThrowStatement extends AstNode {
   private AstNode expression;

   public ThrowStatement() {
      super();
      this.type = 50;
   }

   public ThrowStatement(int pos) {
      super(pos);
      this.type = 50;
   }

   public ThrowStatement(int pos, int len) {
      super(pos, len);
      this.type = 50;
   }

   public ThrowStatement(AstNode expr) {
      super();
      this.type = 50;
      this.setExpression(expr);
   }

   public ThrowStatement(int pos, AstNode expr) {
      super(pos, expr.getLength());
      this.type = 50;
      this.setExpression(expr);
   }

   public ThrowStatement(int pos, int len, AstNode expr) {
      super(pos, len);
      this.type = 50;
      this.setExpression(expr);
   }

   public AstNode getExpression() {
      return this.expression;
   }

   public void setExpression(AstNode expression) {
      this.assertNotNull(expression);
      this.expression = expression;
      expression.setParent(this);
   }

   public String toSource(int depth) {
      StringBuilder sb = new StringBuilder();
      sb.append(this.makeIndent(depth));
      sb.append("throw");
      sb.append(" ");
      sb.append(this.expression.toSource(0));
      sb.append(";\n");
      return sb.toString();
   }

   public void visit(NodeVisitor v) {
      if (v.visit(this)) {
         this.expression.visit(v);
      }

   }
}
