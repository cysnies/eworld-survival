package org.hibernate.dialect;

public class MySQLMyISAMDialect extends MySQLDialect {
   public MySQLMyISAMDialect() {
      super();
   }

   public String getTableTypeString() {
      return " type=MyISAM";
   }

   public boolean dropConstraints() {
      return false;
   }
}
