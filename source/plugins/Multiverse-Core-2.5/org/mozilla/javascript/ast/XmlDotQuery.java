package org.mozilla.javascript.ast;

public class XmlDotQuery extends InfixExpression {
   private int rp = -1;

   public XmlDotQuery() {
      super();
      this.type = 146;
   }

   public XmlDotQuery(int pos) {
      super(pos);
      this.type = 146;
   }

   public XmlDotQuery(int pos, int len) {
      super(pos, len);
      this.type = 146;
   }

   public int getRp() {
      return this.rp;
   }

   public void setRp(int rp) {
      this.rp = rp;
   }

   public String toSource(int depth) {
      StringBuilder sb = new StringBuilder();
      sb.append(this.makeIndent(depth));
      sb.append(this.getLeft().toSource(0));
      sb.append(".(");
      sb.append(this.getRight().toSource(0));
      sb.append(")");
      return sb.toString();
   }
}
