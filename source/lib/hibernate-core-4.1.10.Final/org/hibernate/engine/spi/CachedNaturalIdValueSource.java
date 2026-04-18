package org.hibernate.engine.spi;

public enum CachedNaturalIdValueSource {
   LOAD,
   INSERT,
   UPDATE;

   private CachedNaturalIdValueSource() {
   }
}
