package org.hibernate.engine;

public enum FetchStyle {
   SELECT,
   JOIN,
   BATCH,
   SUBSELECT;

   private FetchStyle() {
   }
}
