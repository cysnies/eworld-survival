package org.dom4j.tree;

import org.dom4j.Element;
import org.dom4j.Node;

public class FlyweightEntity extends AbstractEntity {
   protected String name;
   protected String text;

   protected FlyweightEntity() {
      super();
   }

   public FlyweightEntity(String name) {
      super();
      this.name = name;
   }

   public FlyweightEntity(String name, String text) {
      super();
      this.name = name;
      this.text = text;
   }

   public String getName() {
      return this.name;
   }

   public String getText() {
      return this.text;
   }

   public void setText(String text) {
      if (this.text != null) {
         this.text = text;
      } else {
         throw new UnsupportedOperationException("This Entity is read-only. It cannot be modified");
      }
   }

   protected Node createXPathResult(Element parent) {
      return new DefaultEntity(parent, this.getName(), this.getText());
   }
}
