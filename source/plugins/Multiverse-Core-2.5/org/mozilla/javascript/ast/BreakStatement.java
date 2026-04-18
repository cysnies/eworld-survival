package org.mozilla.javascript.ast;

public class BreakStatement extends Jump {
   private Name breakLabel;
   private AstNode target;

   public BreakStatement() {
      super();
      this.type = 120;
   }

   public BreakStatement(int pos) {
      super();
      this.type = 120;
      this.position = pos;
   }

   public BreakStatement(int pos, int len) {
      super();
      this.type = 120;
      this.position = pos;
      this.length = len;
   }

   public Name getBreakLabel() {
      return this.breakLabel;
   }

   public void setBreakLabel(Name label) {
      this.breakLabel = label;
      if (label != null) {
         label.setParent(this);
      }

   }

   public AstNode getBreakTarget() {
      return this.target;
   }

   public void setBreakTarget(Jump target) {
      this.assertNotNull(target);
      this.target = target;
      this.setJumpStatement(target);
   }

   public String toSource(int depth) {
      StringBuilder sb = new StringBuilder();
      sb.append(this.makeIndent(depth));
      sb.append("break");
      if (this.breakLabel != null) {
         sb.append(" ");
         sb.append(this.breakLabel.toSource(0));
      }

      sb.append(";\n");
      return sb.toString();
   }

   public void visit(NodeVisitor v) {
      if (v.visit(this) && this.breakLabel != null) {
         this.breakLabel.visit(v);
      }

   }
}
