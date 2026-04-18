package org.hibernate.internal.util;

import java.io.Serializable;

public class MarkerObject implements Serializable {
   private String name;

   public MarkerObject(String name) {
      super();
      this.name = name;
   }

   public String toString() {
      return this.name;
   }
}
