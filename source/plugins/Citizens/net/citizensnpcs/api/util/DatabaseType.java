package net.citizensnpcs.api.util;

public enum DatabaseType {
   H2("org.h2.Driver"),
   MYSQL("com.mysql.jdbc.Driver"),
   POSTGRE("org.postgresql.Driver"),
   SQLITE("org.sqlite.JDBC") {
      public String[] prepareForeignKeySQL(DatabaseStorage.Table from, DatabaseStorage.Table to, String columnName) {
         return new String[]{String.format("ALTER TABLE `%s` ADD COLUMN `%s` %s REFERENCES `%s`(`%s`) ON DELETE CASCADE", from.name, columnName, to.primaryKeyType, to.name, to.primaryKey)};
      }
   };

   private final String driver;
   private boolean loaded;

   private DatabaseType(String driver) {
      this.loaded = false;
      this.driver = driver;
   }

   public boolean load() {
      if (this.loaded) {
         return true;
      } else {
         if (DatabaseStorage.loadDriver(DatabaseStorage.class.getClassLoader(), this.driver)) {
            this.loaded = true;
         }

         return this.loaded;
      }
   }

   public String[] prepareForeignKeySQL(DatabaseStorage.Table from, DatabaseStorage.Table to, String columnName) {
      String[] sql = new String[2];
      sql[0] = String.format("ALTER TABLE `%s` ADD `%s` %s", from.name, columnName, to.primaryKeyType);
      sql[1] = String.format("ALTER TABLE `%s` ADD FOREIGN KEY (`%s`) REFERENCES `%s`(`%s`) ON DELETE CASCADE", from.name, columnName, to.name, to.primaryKey);
      return sql;
   }

   public static DatabaseType match(String driver) {
      for(DatabaseType type : values()) {
         if (type.name().toLowerCase().contains(driver)) {
            return type;
         }
      }

      return null;
   }
}
