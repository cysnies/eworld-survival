package org.hibernate.dialect;

public class DerbyTenSevenDialect extends DerbyTenSixDialect {
   public DerbyTenSevenDialect() {
      super();
      this.registerColumnType(16, "boolean");
   }

   public String toBooleanValueString(boolean bool) {
      return String.valueOf(bool);
   }
}
