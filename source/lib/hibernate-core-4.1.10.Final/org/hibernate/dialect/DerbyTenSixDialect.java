package org.hibernate.dialect;

public class DerbyTenSixDialect extends DerbyTenFiveDialect {
   public DerbyTenSixDialect() {
      super();
   }

   public boolean supportsSequences() {
      return true;
   }
}
