package org.mozilla.javascript.ast;

public class WithStatement extends AstNode {
   private AstNode expression;
   private AstNode statement;
   private int lp = -1;
   private int rp = -1;

   public WithStatement() {
      super();
      this.type = 123;
   }

   public WithStatement(int pos) {
      super(pos);
      this.type = 123;
   }

   public WithStatement(int pos, int len) {
      super(pos, len);
      this.type = 123;
   }

   public AstNode getExpression() {
      return this.expression;
   }

   public void setExpression(AstNode expression) {
      this.assertNotNull(expression);
      this.expression = expression;
      expression.setParent(this);
   }

   public AstNode getStatement() {
      return this.statement;
   }

   public void setStatement(AstNode statement) {
      this.assertNotNull(statement);
      this.statement = statement;
      statement.setParent(this);
   }

   public int getLp() {
      return this.lp;
   }

   public void setLp(int lp) {
      this.lp = lp;
   }

   public int getRp() {
      return this.rp;
   }

   public void setRp(int rp) {
      this.rp = rp;
   }

   public void setParens(int lp, int rp) {
      this.lp = lp;
      this.rp = rp;
   }

   public String toSource(int depth) {
      StringBuilder sb = new StringBuilder();
      sb.append(this.makeIndent(depth));
      sb.append("with (");
      sb.append(this.expression.toSource(0));
      sb.append(") ");
      sb.append(this.statement.toSource(depth + 1));
      if (!(this.statement instanceof Block)) {
         sb.append(";\n");
      }

      return sb.toString();
   }

   public void visit(NodeVisitor v) {
      if (v.visit(this)) {
         this.expression.visit(v);
         this.statement.visit(v);
      }

   }
}
