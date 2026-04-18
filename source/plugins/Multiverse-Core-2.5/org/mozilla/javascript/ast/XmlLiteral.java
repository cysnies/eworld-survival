package org.mozilla.javascript.ast;

import java.util.ArrayList;
import java.util.List;

public class XmlLiteral extends AstNode {
   private List fragments = new ArrayList();

   public XmlLiteral() {
      super();
      this.type = 145;
   }

   public XmlLiteral(int pos) {
      super(pos);
      this.type = 145;
   }

   public XmlLiteral(int pos, int len) {
      super(pos, len);
      this.type = 145;
   }

   public List getFragments() {
      return this.fragments;
   }

   public void setFragments(List fragments) {
      this.assertNotNull(fragments);
      this.fragments.clear();

      for(XmlFragment fragment : fragments) {
         this.addFragment(fragment);
      }

   }

   public void addFragment(XmlFragment fragment) {
      this.assertNotNull(fragment);
      this.fragments.add(fragment);
      fragment.setParent(this);
   }

   public String toSource(int depth) {
      StringBuilder sb = new StringBuilder(250);

      for(XmlFragment frag : this.fragments) {
         sb.append(frag.toSource(0));
      }

      return sb.toString();
   }

   public void visit(NodeVisitor v) {
      if (v.visit(this)) {
         for(XmlFragment frag : this.fragments) {
            frag.visit(v);
         }
      }

   }
}
