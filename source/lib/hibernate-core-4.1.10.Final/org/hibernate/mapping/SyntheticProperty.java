package org.hibernate.mapping;

public class SyntheticProperty extends Property {
   public SyntheticProperty() {
      super();
   }

   public boolean isSynthetic() {
      return true;
   }
}
