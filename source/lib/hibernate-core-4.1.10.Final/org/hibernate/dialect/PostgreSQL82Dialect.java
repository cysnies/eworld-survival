package org.hibernate.dialect;

public class PostgreSQL82Dialect extends PostgreSQL81Dialect {
   public PostgreSQL82Dialect() {
      super();
   }

   public boolean supportsIfExistsBeforeTableName() {
      return true;
   }
}
