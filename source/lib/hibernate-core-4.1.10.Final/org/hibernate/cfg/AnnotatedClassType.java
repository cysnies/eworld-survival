package org.hibernate.cfg;

public enum AnnotatedClassType {
   NONE,
   ENTITY,
   EMBEDDABLE,
   EMBEDDABLE_SUPERCLASS;

   private AnnotatedClassType() {
   }
}
