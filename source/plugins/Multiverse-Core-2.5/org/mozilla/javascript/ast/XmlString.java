package org.mozilla.javascript.ast;

public class XmlString extends XmlFragment {
   private String xml;

   public XmlString() {
      super();
   }

   public XmlString(int pos) {
      super(pos);
   }

   public XmlString(int pos, String s) {
      super(pos);
      this.setXml(s);
   }

   public void setXml(String s) {
      this.assertNotNull(s);
      this.xml = s;
      this.setLength(s.length());
   }

   public String getXml() {
      return this.xml;
   }

   public String toSource(int depth) {
      return this.makeIndent(depth) + this.xml;
   }

   public void visit(NodeVisitor v) {
      v.visit(this);
   }
}
