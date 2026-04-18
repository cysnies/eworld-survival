package org.hibernate.cfg.annotations;

import org.hibernate.mapping.Collection;
import org.hibernate.mapping.PersistentClass;
import org.hibernate.mapping.PrimitiveArray;

public class PrimitiveArrayBinder extends ArrayBinder {
   public PrimitiveArrayBinder() {
      super();
   }

   protected Collection createCollection(PersistentClass persistentClass) {
      return new PrimitiveArray(this.getMappings(), persistentClass);
   }
}
