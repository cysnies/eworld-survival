package org.hibernate.dialect;

public class MySQLInnoDBDialect extends MySQLDialect {
   public MySQLInnoDBDialect() {
      super();
   }

   public boolean supportsCascadeDelete() {
      return true;
   }

   public String getTableTypeString() {
      return " type=InnoDB";
   }

   public boolean hasSelfReferentialForeignKeyBug() {
      return true;
   }
}
