package org.mozilla.javascript.ast;

public class XmlElemRef extends XmlRef {
   private AstNode indexExpr;
   private int lb = -1;
   private int rb = -1;

   public XmlElemRef() {
      super();
      this.type = 77;
   }

   public XmlElemRef(int pos) {
      super(pos);
      this.type = 77;
   }

   public XmlElemRef(int pos, int len) {
      super(pos, len);
      this.type = 77;
   }

   public AstNode getExpression() {
      return this.indexExpr;
   }

   public void setExpression(AstNode expr) {
      this.assertNotNull(expr);
      this.indexExpr = expr;
      expr.setParent(this);
   }

   public int getLb() {
      return this.lb;
   }

   public void setLb(int lb) {
      this.lb = lb;
   }

   public int getRb() {
      return this.rb;
   }

   public void setRb(int rb) {
      this.rb = rb;
   }

   public void setBrackets(int lb, int rb) {
      this.lb = lb;
      this.rb = rb;
   }

   public String toSource(int depth) {
      StringBuilder sb = new StringBuilder();
      sb.append(this.makeIndent(depth));
      if (this.isAttributeAccess()) {
         sb.append("@");
      }

      if (this.namespace != null) {
         sb.append(this.namespace.toSource(0));
         sb.append("::");
      }

      sb.append("[");
      sb.append(this.indexExpr.toSource(0));
      sb.append("]");
      return sb.toString();
   }

   public void visit(NodeVisitor v) {
      if (v.visit(this)) {
         if (this.namespace != null) {
            this.namespace.visit(v);
         }

         this.indexExpr.visit(v);
      }

   }
}
