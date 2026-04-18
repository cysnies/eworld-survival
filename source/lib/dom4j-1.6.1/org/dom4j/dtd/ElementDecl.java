package org.dom4j.dtd;

public class ElementDecl {
   private String name;
   private String model;

   public ElementDecl() {
      super();
   }

   public ElementDecl(String name, String model) {
      super();
      this.name = name;
      this.model = model;
   }

   public String getName() {
      return this.name;
   }

   public void setName(String name) {
      this.name = name;
   }

   public String getModel() {
      return this.model;
   }

   public void setModel(String model) {
      this.model = model;
   }

   public String toString() {
      return "<!ELEMENT " + this.name + " " + this.model + ">";
   }
}
