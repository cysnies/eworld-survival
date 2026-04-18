package org.mozilla.javascript.ast;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class ArrayLiteral extends AstNode implements DestructuringForm {
   private static final List NO_ELEMS = Collections.unmodifiableList(new ArrayList());
   private List elements;
   private int destructuringLength;
   private int skipCount;
   private boolean isDestructuring;

   public ArrayLiteral() {
      super();
      this.type = 65;
   }

   public ArrayLiteral(int pos) {
      super(pos);
      this.type = 65;
   }

   public ArrayLiteral(int pos, int len) {
      super(pos, len);
      this.type = 65;
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

         for(AstNode e : elements) {
            this.addElement(e);
         }
      }

   }

   public void addElement(AstNode element) {
      this.assertNotNull(element);
      if (this.elements == null) {
         this.elements = new ArrayList();
      }

      this.elements.add(element);
      element.setParent(this);
   }

   public int getSize() {
      return this.elements == null ? 0 : this.elements.size();
   }

   public AstNode getElement(int index) {
      if (this.elements == null) {
         throw new IndexOutOfBoundsException("no elements");
      } else {
         return (AstNode)this.elements.get(index);
      }
   }

   public int getDestructuringLength() {
      return this.destructuringLength;
   }

   public void setDestructuringLength(int destructuringLength) {
      this.destructuringLength = destructuringLength;
   }

   public int getSkipCount() {
      return this.skipCount;
   }

   public void setSkipCount(int count) {
      this.skipCount = count;
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
      sb.append("[");
      if (this.elements != null) {
         this.printList(this.elements, sb);
      }

      sb.append("]");
      return sb.toString();
   }

   public void visit(NodeVisitor v) {
      if (v.visit(this)) {
         for(AstNode e : this.getElements()) {
            e.visit(v);
         }
      }

   }
}
