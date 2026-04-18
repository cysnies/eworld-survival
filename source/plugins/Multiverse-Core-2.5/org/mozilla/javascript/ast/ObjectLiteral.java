package org.mozilla.javascript.ast;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class ObjectLiteral extends AstNode implements DestructuringForm {
   private static final List NO_ELEMS = Collections.unmodifiableList(new ArrayList());
   private List elements;
   boolean isDestructuring;

   public ObjectLiteral() {
      super();
      this.type = 66;
   }

   public ObjectLiteral(int pos) {
      super(pos);
      this.type = 66;
   }

   public ObjectLiteral(int pos, int len) {
      super(pos, len);
      this.type = 66;
   }

   public List getElements() {
      return this.elements != null ? this.elements : NO_ELEMS;
   }

   public void setElements(List elements) {
      if (elements == null) {
         this.elements = null;
      } else {
         if (this.elements != null) {
            this.elements.clear();
         }

         for(ObjectProperty o : elements) {
            this.addElement(o);
         }
      }

   }

   public void addElement(ObjectProperty element) {
      this.assertNotNull(element);
      if (this.elements == null) {
         this.elements = new ArrayList();
      }

      this.elements.add(element);
      element.setParent(this);
   }

   public void setIsDestructuring(boolean destructuring) {
      this.isDestructuring = destructuring;
   }

   public boolean isDestructuring() {
      return this.isDestructuring;
   }

   public String toSource(int depth) {
      StringBuilder sb = new StringBuilder();
      sb.append(this.makeIndent(depth));
      sb.append("{");
      if (this.elements != null) {
         this.printList(this.elements, sb);
      }

      sb.append("}");
      return sb.toString();
   }

   public void visit(NodeVisitor v) {
      if (v.visit(this)) {
         for(ObjectProperty prop : this.getElements()) {
            prop.visit(v);
         }
      }

   }
}
