package org.mozilla.javascript.ast;

public class NumberLiteral extends AstNode {
   private String value;
   private double number;

   public NumberLiteral() {
      super();
      this.type = 40;
   }

   public NumberLiteral(int pos) {
      super(pos);
      this.type = 40;
   }

   public NumberLiteral(int pos, int len) {
      super(pos, len);
      this.type = 40;
   }

   public NumberLiteral(int pos, String value) {
      super(pos);
      this.type = 40;
      this.setValue(value);
      this.setLength(value.length());
   }

   public NumberLiteral(int pos, String value, double number) {
      this(pos, value);
      this.setDouble(number);
   }

   public NumberLiteral(double number) {
      super();
      this.type = 40;
      this.setDouble(number);
      this.setValue(Double.toString(number));
   }

   public String getValue() {
      return this.value;
   }

   public void setValue(String value) {
      this.assertNotNull(value);
      this.value = value;
   }

   public double getNumber() {
      return this.number;
   }

   public void setNumber(double value) {
      this.number = value;
   }

   public String toSource(int depth) {
      return this.makeIndent(depth) + (this.value == null ? "<null>" : this.value);
   }

   public void visit(NodeVisitor v) {
      v.visit(this);
   }
}
