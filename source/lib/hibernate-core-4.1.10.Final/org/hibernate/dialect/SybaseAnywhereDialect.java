package org.hibernate.dialect;

public class SybaseAnywhereDialect extends SybaseDialect {
   public SybaseAnywhereDialect() {
      super();
   }

   public String getNoColumnsInsertString() {
      return "values (default)";
   }

   public boolean dropConstraints() {
      return false;
   }

   public boolean supportsInsertSelectIdentity() {
      return false;
   }
}
