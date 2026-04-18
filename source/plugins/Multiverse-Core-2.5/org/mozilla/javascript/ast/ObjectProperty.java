package org.mozilla.javascript.ast;

public class ObjectProperty extends InfixExpression {
   public void setNodeType(int nodeType) {
      if (nodeType != 103 && nodeType != 151 && nodeType != 152) {
         throw new IllegalArgumentException("invalid node type: " + nodeType);
      } else {
         this.setType(nodeType);
      }
   }

   public ObjectProperty() {
      super();
      this.type = 103;
   }

   public ObjectProperty(int pos) {
      super(pos);
      this.type = 103;
   }

   public ObjectProperty(int pos, int len) {
      super(pos, len);
      this.type = 103;
   }

   public void setIsGetter() {
      this.type = 151;
   }

   public boolean isGetter() {
      return this.type == 151;
   }

   public void setIsSetter() {
      this.type = 152;
   }

   public boolean isSetter() {
      return this.type == 152;
   }

   public String toSource(int depth) {
      StringBuilder sb = new StringBuilder();
      sb.append(this.makeIndent(depth));
      if (this.isGetter()) {
         sb.append("get ");
      } else if (this.isSetter()) {
         sb.append("set ");
      }

      sb.append(this.left.toSource(0));
      if (this.type == 103) {
         sb.append(": ");
      }

      sb.append(this.right.toSource(0));
      return sb.toString();
   }
}
