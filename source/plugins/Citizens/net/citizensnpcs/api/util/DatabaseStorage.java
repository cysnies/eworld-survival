package net.citizensnpcs.api.util;

import com.google.common.base.Splitter;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

public class DatabaseStorage implements Storage {
   private Connection conn;
   private final QueryRunner queryRunner = new QueryRunner();
   private final Map tables = Maps.newHashMap();
   private final Map traverseCache = Maps.newHashMap();
   private final DatabaseType type;
   private final String url;
   private final String username;
   private final String password;
   private static final Pattern INTEGER = Pattern.compile("([\\+-]?\\d+)([eE][\\+-]?\\d+)?");
   private static final Traversed INVALID_TRAVERSAL = new Traversed((Table)null, (String)null, (String)null);

   public DatabaseStorage(String driver, String url, String username, String password) throws SQLException {
      super();
      this.url = "jdbc:" + url;
      this.username = username;
      this.password = password;
      this.type = DatabaseType.match(driver);
      boolean success = this.type.load();
      if (!success) {
         throw new SQLException("Couldn't load driver");
      }
   }

   private void createForeignKey(Table from, Table to) {
      String fk = "fk_" + to.name;
      Connection conn = this.getConnection();

      try {
         for(String sql : this.type.prepareForeignKeySQL(from, to, fk)) {
            PreparedStatement stmt = conn.prepareStatement(sql);
            stmt.execute();
            stmt.close();
         }

         from.addForeignKey(fk);
      } catch (SQLException ex) {
         ex.printStackTrace();
      }

   }

   private Table createTable(String name, int type, boolean autoIncrement) {
      if (name == null) {
         throw new IllegalArgumentException("name cannot be null");
      } else {
         Table t = (Table)this.tables.get(name);
         if (t != null) {
            return t;
         } else {
            String pk = name + "_id";
            String primaryType = " NOT NULL PRIMARY KEY";
            String directType;
            switch (type) {
               case 4:
                  directType = "INTEGER";
                  if (autoIncrement) {
                     primaryType = primaryType + " AUTO_INCREMENT";
                  }
                  break;
               case 12:
                  directType = "VARCHAR(255)";
                  break;
               default:
                  throw new IllegalArgumentException("type not supported");
            }

            Connection conn = this.getConnection();
            PreparedStatement stmt = null;
            Table created = null;

            try {
               stmt = conn.prepareStatement("CREATE TABLE IF NOT EXISTS `" + name + "`(`" + pk + "` " + directType + primaryType + ")");
               stmt.execute();
               created = (new Table()).setName(name).setPrimaryKey(pk).setPrimaryKeyType(directType);
               this.tables.put(name, created);
            } catch (SQLException ex) {
               ex.printStackTrace();
            } finally {
               closeQuietly((Statement)stmt);
            }

            return created;
         }
      }
   }

   private String ensureRelation(String pk, Table from, final Table to) {
      Connection conn = this.getConnection();

      try {
         String existing = (String)this.queryRunner.query(conn, "SELECT `fk_" + to.name + "` FROM " + from.name + " WHERE " + from.primaryKey + " = ?", new ResultSetHandler() {
            public String handle(ResultSet rs) throws SQLException {
               return rs.next() ? rs.getString("fk_" + to.name) : null;
            }
         }, pk);
         if (existing == null) {
            String generated = to.generateRow();
            this.queryRunner.update(conn, "UPDATE `" + from.name + "` SET `fk_" + to.name + "`=?", (Object)generated);
            return generated;
         } else {
            return existing;
         }
      } catch (SQLException ex) {
         ex.printStackTrace();
         return null;
      }
   }

   private Connection getConnection() {
      if (this.conn != null) {
         try {
            if (this.conn.isClosed()) {
               this.conn = null;
            } else {
               this.conn.prepareStatement("SELECT 1;").execute();
            }
         } catch (SQLException ex) {
            if ("08S01".equals(ex.getSQLState())) {
               closeQuietly(this.conn);
            }
         }
      }

      try {
         if (this.conn == null || this.conn.isClosed()) {
            this.conn = this.username.isEmpty() && this.password.isEmpty() ? DriverManager.getConnection(this.url) : DriverManager.getConnection(this.url, this.username, this.password);
            return this.conn;
         }
      } catch (SQLException ex) {
         ex.printStackTrace();
         return null;
      }

      return this.conn;
   }

   public DataKey getKey(String root) {
      return new DatabaseKey(root);
   }

   public boolean load() {
      this.tables.clear();
      this.traverseCache.clear();
      Connection conn = this.getConnection();

      try {
         ResultSet rs = conn.getMetaData().getTables((String)null, (String)null, (String)null, new String[]{"TABLE"});

         while(rs.next()) {
            this.tables.put(rs.getString("TABLE_NAME"), new Table());
         }

         rs.close();

         for(Map.Entry entry : this.tables.entrySet()) {
            Table table = (Table)entry.getValue();
            table.name = (String)entry.getKey();
            rs = conn.getMetaData().getColumns((String)null, (String)null, table.name, (String)null);

            while(rs.next()) {
               table.addColumn(rs.getString("COLUMN_NAME"));
            }

            rs.close();
            rs = conn.getMetaData().getPrimaryKeys((String)null, (String)null, table.name);

            while(rs.next()) {
               table.primaryKey = rs.getString("COLUMN_NAME");
               table.setPrimaryKeyType(rs.getMetaData().getColumnTypeName(4));
            }

            rs.close();
            rs = conn.getMetaData().getImportedKeys((String)null, (String)null, table.name);

            while(rs.next()) {
               table.addForeignKey(rs.getString("PKCOLUMN_NAME"));
            }

            rs.close();
         }

         return true;
      } catch (SQLException ex) {
         ex.printStackTrace();
         return false;
      }
   }

   public void save() {
      commitAndCloseQuietly(this.conn);
   }

   public String toString() {
      return "DatabaseStorage [tables=" + this.tables + ", url=" + this.url + ", username=" + this.username + ", password=" + this.password + "]";
   }

   private static void closeQuietly(Connection conn) {
      try {
         if (conn != null) {
            conn.close();
         }
      } catch (SQLException var2) {
      }

   }

   private static void closeQuietly(ResultSet rs) {
      try {
         if (rs != null) {
            rs.close();
         }
      } catch (SQLException var2) {
      }

   }

   private static void closeQuietly(Statement stmt) {
      try {
         if (stmt != null) {
            stmt.close();
         }
      } catch (SQLException var2) {
      }

   }

   private static void commitAndCloseQuietly(Connection conn) {
      try {
         try {
            conn.commit();
         } finally {
            conn.close();
         }
      } catch (SQLException var5) {
      }

   }

   public static boolean loadDriver(ClassLoader classLoader, String driverClassName) {
      try {
         classLoader.loadClass(driverClassName).newInstance();
         return true;
      } catch (IllegalAccessException var3) {
         return true;
      } catch (Exception var4) {
         return false;
      }
   }

   public class DatabaseKey extends DataKey {
      private DatabaseKey() {
         super("");
      }

      private DatabaseKey(String root) {
         super(root);
      }

      protected String createRelativeKey(String from) {
         return super.createRelativeKey(from.replace("-", ""));
      }

      public boolean getBoolean(String key) {
         final Traversed t = this.traverse(this.createRelativeKey(key), false);
         if (t == DatabaseStorage.INVALID_TRAVERSAL) {
            return false;
         } else {
            Boolean value = (Boolean)this.getValue(t, new ResultSetHandler() {
               public Boolean handle(ResultSet rs) throws SQLException {
                  return rs.next() ? rs.getBoolean(t.column) : null;
               }
            });
            return value == null ? false : value;
         }
      }

      public double getDouble(String key) {
         final Traversed t = this.traverse(this.createRelativeKey(key), false);
         if (t == DatabaseStorage.INVALID_TRAVERSAL) {
            return (double)0.0F;
         } else {
            Double value = (Double)this.getValue(t, new ResultSetHandler() {
               public Double handle(ResultSet rs) throws SQLException {
                  return rs.next() ? rs.getDouble(t.column) : null;
               }
            });
            return value == null ? (double)0.0F : value;
         }
      }

      public int getInt(String key) {
         final Traversed t = this.traverse(this.createRelativeKey(key), false);
         if (t == DatabaseStorage.INVALID_TRAVERSAL) {
            return 0;
         } else {
            Integer value = (Integer)this.getValue(t, new ResultSetHandler() {
               public Integer handle(ResultSet rs) throws SQLException {
                  return rs.next() ? rs.getInt(t.column) : null;
               }
            });
            return value == null ? 0 : value;
         }
      }

      public long getLong(String key) {
         final Traversed t = this.traverse(this.createRelativeKey(key), false);
         if (t == DatabaseStorage.INVALID_TRAVERSAL) {
            return 0L;
         } else {
            Long value = (Long)this.getValue(t, new ResultSetHandler() {
               public Long handle(ResultSet rs) throws SQLException {
                  return rs.next() ? rs.getLong(t.column) : null;
               }
            });
            return value == null ? 0L : value;
         }
      }

      public Object getRaw(String key) {
         final Traversed t = this.traverse(this.createRelativeKey(key), false);
         if (t == DatabaseStorage.INVALID_TRAVERSAL) {
            return null;
         } else {
            Object value = this.getValue(t, new ResultSetHandler() {
               public Object handle(ResultSet rs) throws SQLException {
                  return rs.next() ? rs.getObject(t.column) : null;
               }
            });
            return value;
         }
      }

      public DataKey getRelative(String relative) {
         return relative != null && !relative.isEmpty() ? DatabaseStorage.this.new DatabaseKey(this.createRelativeKey(relative)) : this;
      }

      protected Traversed getRoot() {
         return null;
      }

      private Iterable getSingleKeys(List keys) {
         if (!DatabaseStorage.this.tables.containsKey(this.path)) {
            return keys;
         } else {
            Table table = (Table)DatabaseStorage.this.tables.get(this.path);
            if (table.primaryKey == null) {
               return keys;
            } else {
               PreparedStatement stmt = null;
               ResultSet rs = null;

               try {
                  Connection conn = DatabaseStorage.this.getConnection();
                  stmt = conn.prepareStatement("SELECT `" + table.primaryKey + "` FROM `" + this.path + "`");
                  rs = stmt.executeQuery();

                  while(rs.next()) {
                     final Traversed found = new Traversed(table, rs.getString(table.primaryKey), table.primaryKey);
                     keys.add(new DatabaseKey() {
                        public Traversed getRoot() {
                           return found;
                        }
                     });
                  }
               } catch (SQLException e) {
                  e.printStackTrace();
               } finally {
                  DatabaseStorage.closeQuietly((Statement)stmt);
                  DatabaseStorage.closeQuietly(rs);
               }

               return keys;
            }
         }
      }

      public String getString(String key) {
         final Traversed t = this.traverse(this.createRelativeKey(key), false);
         if (t == DatabaseStorage.INVALID_TRAVERSAL) {
            return "";
         } else {
            String value = (String)this.getValue(t, new ResultSetHandler() {
               public String handle(ResultSet rs) throws SQLException {
                  return rs.next() ? rs.getString(t.column) : null;
               }
            });
            return value == null ? "" : value;
         }
      }

      public Iterable getSubKeys() {
         List<DataKey> keys = Lists.newArrayList();
         return (Iterable)(this.path.split("\\.").length == 1 ? this.getSingleKeys(keys) : keys);
      }

      private Object getValue(Traversed t, ResultSetHandler resultSetHandler) {
         if (!t.found.hasColumn(t.column)) {
            return null;
         } else {
            try {
               return DatabaseStorage.this.queryRunner.query(DatabaseStorage.this.getConnection(), "SELECT `" + t.column + "` FROM " + t.found.name + " WHERE `" + t.found.primaryKey + "`=?", resultSetHandler, t.key);
            } catch (SQLException ex) {
               ex.printStackTrace();
               return null;
            }
         }
      }

      public Map getValuesDeep() {
         throw new UnsupportedOperationException();
      }

      public boolean keyExists(String key) {
         return this.traverse(this.createRelativeKey(key), false) != DatabaseStorage.INVALID_TRAVERSAL;
      }

      public String name() {
         Traversed t = this.traverse(this.path, true);
         System.err.println(t);
         return t.key != null ? t.key : t.found.name;
      }

      public void removeKey(String key) {
         Traversed t = this.traverse(this.createRelativeKey(key), false);
         if (t != DatabaseStorage.INVALID_TRAVERSAL) {
            Connection conn = DatabaseStorage.this.getConnection();

            try {
               if (t.found.hasColumn(t.column)) {
                  DatabaseStorage.this.queryRunner.update(conn, "UPDATE `" + t.found.name + "` SET `" + t.column + "`=? WHERE `" + t.found.primaryKey + "`=?", null, t.key);
               } else {
                  DatabaseStorage.this.queryRunner.update(conn, "DELETE FROM `" + t.found.name + "` WHERE `" + t.found.primaryKey + "=?", (Object)t.key);
               }
            } catch (SQLException ex) {
               ex.printStackTrace();
            }

         }
      }

      public void setBoolean(String key, boolean value) {
         this.setValue("SMALLINT", key, value);
      }

      public void setDouble(String key, double value) {
         this.setValue("DOUBLE", key, value);
      }

      public void setInt(String key, int value) {
         this.setValue("STRING", key, value);
      }

      public void setLong(String key, long value) {
         this.setValue("BIGINT", key, value);
      }

      public void setRaw(String key, Object value) {
         this.setValue("JAVA_OBJECT", key, value);
      }

      public void setString(String key, String value) {
         this.setValue("VARCHAR", key, value);
      }

      private void setValue(String type, String key, Object value) {
         Traversed t = this.traverse(this.createRelativeKey(key), true);
         if (t == DatabaseStorage.INVALID_TRAVERSAL) {
            System.err.println("Could not set " + value + " at " + key);
         } else {
            Connection conn = DatabaseStorage.this.getConnection();

            try {
               if (!t.found.hasColumn(t.column)) {
                  PreparedStatement stmt = conn.prepareStatement("ALTER TABLE `" + t.found.name + "` ADD `" + t.column + "` " + type);
                  stmt.execute();
                  DatabaseStorage.closeQuietly((Statement)stmt);
                  t.found.addColumn(t.column);
               }

               DatabaseStorage.this.queryRunner.update(conn, "UPDATE " + t.found.name + " SET " + t.column + "=? WHERE " + t.found.primaryKey + "=?", value, t.key);
            } catch (SQLException ex) {
               ex.printStackTrace();
               System.out.println("UPDATE " + t.found.name + " SET " + t.column + "=? WHERE " + t.found.primaryKey + "=?" + " " + value + " " + t.key);
            }

         }
      }

      private Traversed traverse(String path, boolean createRelations) {
         Traversed prev = (Traversed)DatabaseStorage.this.traverseCache.get(path);
         if (prev != null) {
            return prev;
         } else {
            String[] parts = (String[])Iterables.toArray(Splitter.on('.').omitEmptyStrings().trimResults().split(path), String.class);
            Traversed root = this.getRoot();
            int i = 0;
            Table table;
            String pk;
            if (root == null) {
               if (parts.length < 2) {
                  return DatabaseStorage.INVALID_TRAVERSAL;
               }

               table = (Table)DatabaseStorage.this.tables.get(parts[0]);
               pk = parts[1];
               if (table == null) {
                  if (!createRelations) {
                     return DatabaseStorage.INVALID_TRAVERSAL;
                  }

                  int type = DatabaseStorage.INTEGER.matcher(pk).matches() ? 4 : 12;
                  table = DatabaseStorage.this.createTable(parts[0], type, false);
                  if (table == null) {
                     return DatabaseStorage.INVALID_TRAVERSAL;
                  }

                  table.insert(pk);
               }

               i = 2;
            } else {
               table = root.found;
               pk = root.key;
            }

            while(i < parts.length - 1) {
               String part = parts[i];
               Table next = (Table)DatabaseStorage.this.tables.get(part);
               boolean missingTable = next == null;
               if (missingTable) {
                  next = DatabaseStorage.this.createTable(part, 4, true);
                  if (next == null) {
                     return DatabaseStorage.INVALID_TRAVERSAL;
                  }
               }

               boolean needRelationToNext = !table.hasColumn("fk_" + next.name);
               if (needRelationToNext) {
                  if (!createRelations) {
                     return DatabaseStorage.INVALID_TRAVERSAL;
                  }

                  DatabaseStorage.this.createForeignKey(table, next);
               }

               pk = DatabaseStorage.this.ensureRelation(pk, table, next);
               if (pk == null) {
                  return DatabaseStorage.INVALID_TRAVERSAL;
               }

               table = next;
               ++i;
            }

            String setColumn = parts.length == 0 ? null : parts[parts.length - 1];
            Traversed t = new Traversed(table, pk, setColumn);
            DatabaseStorage.this.traverseCache.put(path, t);
            return t;
         }
      }
   }

   public class Table {
      private final List columns = Lists.newArrayList();
      String name;
      String primaryKey;
      String primaryKeyType;

      public Table() {
         super();
      }

      public void addColumn(String column) {
         if (this.columns.contains(column)) {
            throw new IllegalArgumentException(column + " already exists in " + this.name);
         } else if (!column.equalsIgnoreCase(this.primaryKey) && !column.equalsIgnoreCase(this.name)) {
            this.columns.add(column);
         }
      }

      public void addForeignKey(String fk) {
         if (this.columns.contains(fk)) {
            throw new IllegalArgumentException(fk + " already exists in " + this.name);
         } else {
            this.columns.add(fk);
         }
      }

      public String generateRow() {
         StringBuilder nullBuilder = new StringBuilder();
         int size = this.columns.size() + 1;

         for(int i = 0; i < size; ++i) {
            nullBuilder.append("NULL,");
         }

         String nulls = nullBuilder.substring(0, nullBuilder.length() - 1);
         Connection conn = DatabaseStorage.this.getConnection();
         PreparedStatement stmt = null;
         ResultSet rs = null;

         String var7;
         try {
            stmt = conn.prepareStatement("INSERT INTO `" + this.name + "` VALUES (" + nulls + ")", 1);
            stmt.execute();
            rs = stmt.getGeneratedKeys();
            if (rs.next()) {
               var7 = rs.getString(1);
               return var7;
            }

            var7 = null;
         } catch (SQLException ex) {
            ex.printStackTrace();
            return null;
         } finally {
            DatabaseStorage.closeQuietly((Statement)stmt);
            DatabaseStorage.closeQuietly(rs);
         }

         return var7;
      }

      public boolean hasColumn(String column) {
         return this.columns.contains(column);
      }

      public void insert(String primary) {
         Connection conn = DatabaseStorage.this.getConnection();

         try {
            DatabaseStorage.this.queryRunner.update(conn, "INSERT INTO `" + this.name + "` (`" + this.primaryKey + "`) VALUES (?)", (Object)primary);
         } catch (SQLException ex) {
            ex.printStackTrace();
         }

      }

      public Table setName(String tableName) {
         this.name = tableName;
         return this;
      }

      public Table setPrimaryKey(String pk) {
         this.primaryKey = pk;
         return this;
      }

      public Table setPrimaryKeyType(String type) {
         this.primaryKeyType = type;
         return this;
      }

      public String toString() {
         return "Table {name=" + this.name + ", primaryKey=" + this.primaryKey + ", columns=" + this.columns + "}";
      }
   }

   private static class Traversed {
      private final String column;
      private final Table found;
      private final String key;

      Traversed(Table found, String pk, String column) {
         super();
         this.found = found;
         this.key = pk;
         this.column = column;
      }

      public String toString() {
         return "Traversed [column=" + this.column + ", found=" + this.found + ", key=" + this.key + "]";
      }
   }
}
