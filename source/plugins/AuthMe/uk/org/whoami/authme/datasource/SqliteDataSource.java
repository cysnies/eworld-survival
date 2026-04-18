package uk.org.whoami.authme.datasource;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import uk.org.whoami.authme.ConsoleLogger;
import uk.org.whoami.authme.cache.auth.PlayerAuth;
import uk.org.whoami.authme.settings.Settings;

public class SqliteDataSource implements DataSource {
   private String database;
   private String tableName;
   private String columnName;
   private String columnPassword;
   private String columnIp;
   private String columnLastLogin;
   private String columnSalt;
   private String columnGroup;
   private String lastlocX;
   private String lastlocY;
   private String lastlocZ;
   private String lastlocWorld;
   private String columnEmail;
   private String columnID;
   private Connection con;

   public SqliteDataSource() throws ClassNotFoundException, SQLException {
      super();
      this.database = Settings.getMySQLDatabase;
      this.tableName = Settings.getMySQLTablename;
      this.columnName = Settings.getMySQLColumnName;
      this.columnPassword = Settings.getMySQLColumnPassword;
      this.columnIp = Settings.getMySQLColumnIp;
      this.columnLastLogin = Settings.getMySQLColumnLastLogin;
      this.columnSalt = Settings.getMySQLColumnSalt;
      this.columnGroup = Settings.getMySQLColumnGroup;
      this.lastlocX = Settings.getMySQLlastlocX;
      this.lastlocY = Settings.getMySQLlastlocY;
      this.lastlocZ = Settings.getMySQLlastlocZ;
      this.lastlocWorld = Settings.getMySQLlastlocWorld;
      this.columnEmail = Settings.getMySQLColumnEmail;
      this.columnID = Settings.getMySQLColumnId;
      this.connect();
      this.setup();
   }

   private synchronized void connect() throws ClassNotFoundException, SQLException {
      Class.forName("org.sqlite.JDBC");
      ConsoleLogger.info("SQLite driver loaded");
      this.con = DriverManager.getConnection("jdbc:sqlite:plugins/AuthMe/" + this.database + ".db");
   }

   private synchronized void setup() throws SQLException {
      Statement st = null;
      ResultSet rs = null;

      try {
         st = this.con.createStatement();
         st.executeUpdate("CREATE TABLE IF NOT EXISTS " + this.tableName + " (" + this.columnID + " INTEGER AUTO_INCREMENT," + this.columnName + " VARCHAR(255) NOT NULL UNIQUE," + this.columnPassword + " VARCHAR(255) NOT NULL," + this.columnIp + " VARCHAR(40) NOT NULL," + this.columnLastLogin + " BIGINT," + this.lastlocX + " smallint(6) DEFAULT '0'," + this.lastlocY + " smallint(6) DEFAULT '0'," + this.lastlocZ + " smallint(6) DEFAULT '0'," + this.lastlocWorld + " VARCHAR(255) DEFAULT 'world'," + this.columnEmail + " VARCHAR(255) DEFAULT 'your@email.com'," + "CONSTRAINT table_const_prim PRIMARY KEY (" + this.columnID + "));");
         rs = this.con.getMetaData().getColumns((String)null, (String)null, this.tableName, this.columnPassword);
         if (!rs.next()) {
            st.executeUpdate("ALTER TABLE " + this.tableName + " ADD COLUMN " + this.columnPassword + " VARCHAR(255) NOT NULL;");
         }

         rs.close();
         rs = this.con.getMetaData().getColumns((String)null, (String)null, this.tableName, this.columnIp);
         if (!rs.next()) {
            st.executeUpdate("ALTER TABLE " + this.tableName + " ADD COLUMN " + this.columnIp + " VARCHAR(40) NOT NULL;");
         }

         rs.close();
         rs = this.con.getMetaData().getColumns((String)null, (String)null, this.tableName, this.columnLastLogin);
         if (!rs.next()) {
            st.executeUpdate("ALTER TABLE " + this.tableName + " ADD COLUMN " + this.columnLastLogin + " BIGINT;");
         }

         rs.close();
         rs = this.con.getMetaData().getColumns((String)null, (String)null, this.tableName, this.lastlocX);
         if (!rs.next()) {
            st.executeUpdate("ALTER TABLE " + this.tableName + " ADD COLUMN " + this.lastlocX + " smallint(6) NOT NULL DEFAULT '0'; " + "ALTER TABLE " + this.tableName + " ADD COLUMN " + this.lastlocY + " smallint(6) NOT NULL DEFAULT '0'; " + "ALTER TABLE " + this.tableName + " ADD COLUMN " + this.lastlocZ + " smallint(6) NOT NULL DEFAULT '0';");
         }

         rs.close();
         rs = this.con.getMetaData().getColumns((String)null, (String)null, this.tableName, this.lastlocWorld);
         if (!rs.next()) {
            st.executeUpdate("ALTER TABLE " + this.tableName + " ADD COLUMN " + this.lastlocWorld + " VARCHAR(255) NOT NULL DEFAULT 'world' AFTER " + this.lastlocZ + ";");
         }

         rs.close();
         rs = this.con.getMetaData().getColumns((String)null, (String)null, this.tableName, this.columnEmail);
         if (!rs.next()) {
            st.executeUpdate("ALTER TABLE " + this.tableName + " ADD COLUMN " + this.columnEmail + "  VARCHAR(255) DEFAULT 'your@email.com';");
         }
      } finally {
         this.close(rs);
         this.close(st);
      }

      ConsoleLogger.info("SQLite Setup finished");
   }

   public synchronized boolean isAuthAvailable(String user) {
      PreparedStatement pst = null;
      ResultSet rs = null;

      try {
         pst = this.con.prepareStatement("SELECT * FROM " + this.tableName + " WHERE " + this.columnName + "=?");
         pst.setString(1, user);
         rs = pst.executeQuery();
         boolean var6 = rs.next();
         return var6;
      } catch (SQLException ex) {
         ConsoleLogger.showError(ex.getMessage());
      } finally {
         this.close(rs);
         this.close((Statement)pst);
      }

      return false;
   }

   public synchronized PlayerAuth getAuth(String user) {
      PreparedStatement pst = null;
      ResultSet rs = null;

      PlayerAuth var6;
      try {
         pst = this.con.prepareStatement("SELECT * FROM " + this.tableName + " WHERE " + this.columnName + "=?;");
         pst.setString(1, user);
         rs = pst.executeQuery();
         if (!rs.next()) {
            return null;
         }

         if (!rs.getString(this.columnIp).isEmpty()) {
            if (!this.columnSalt.isEmpty()) {
               var6 = new PlayerAuth(rs.getString(this.columnName), rs.getString(this.columnPassword), rs.getString(this.columnSalt), rs.getInt(this.columnGroup), rs.getString(this.columnIp), rs.getLong(this.columnLastLogin), rs.getInt(this.lastlocX), rs.getInt(this.lastlocY), rs.getInt(this.lastlocZ), rs.getString(this.lastlocWorld), rs.getString(this.columnEmail));
               return var6;
            }

            var6 = new PlayerAuth(rs.getString(this.columnName), rs.getString(this.columnPassword), rs.getString(this.columnIp), rs.getLong(this.columnLastLogin), rs.getInt(this.lastlocX), rs.getInt(this.lastlocY), rs.getInt(this.lastlocZ), rs.getString(this.lastlocWorld), rs.getString(this.columnEmail));
            return var6;
         }

         var6 = new PlayerAuth(rs.getString(this.columnName), rs.getString(this.columnPassword), "198.18.0.1", rs.getLong(this.columnLastLogin), rs.getInt(this.lastlocX), rs.getInt(this.lastlocY), rs.getInt(this.lastlocZ), rs.getString(this.lastlocWorld), rs.getString(this.columnEmail));
      } catch (SQLException ex) {
         ConsoleLogger.showError(ex.getMessage());
         return null;
      } finally {
         this.close(rs);
         this.close((Statement)pst);
      }

      return var6;
   }

   public synchronized boolean saveAuth(PlayerAuth auth) {
      PreparedStatement pst = null;

      try {
         if (this.columnSalt.isEmpty() && auth.getSalt().isEmpty()) {
            pst = this.con.prepareStatement("INSERT INTO " + this.tableName + "(" + this.columnName + "," + this.columnPassword + "," + this.columnIp + "," + this.columnLastLogin + ") VALUES (?,?,?,?);");
            pst.setString(1, auth.getNickname());
            pst.setString(2, auth.getHash());
            pst.setString(3, auth.getIp());
            pst.setLong(4, auth.getLastLogin());
            pst.executeUpdate();
         } else {
            pst = this.con.prepareStatement("INSERT INTO " + this.tableName + "(" + this.columnName + "," + this.columnPassword + "," + this.columnIp + "," + this.columnLastLogin + "," + this.columnSalt + ") VALUES (?,?,?,?,?);");
            pst.setString(1, auth.getNickname());
            pst.setString(2, auth.getHash());
            pst.setString(3, auth.getIp());
            pst.setLong(4, auth.getLastLogin());
            pst.setString(5, auth.getSalt());
            pst.executeUpdate();
         }

         return true;
      } catch (SQLException ex) {
         ConsoleLogger.showError(ex.getMessage());
      } finally {
         this.close((Statement)pst);
      }

      return false;
   }

   public synchronized boolean updatePassword(PlayerAuth auth) {
      PreparedStatement pst = null;

      try {
         pst = this.con.prepareStatement("UPDATE " + this.tableName + " SET " + this.columnPassword + "=? WHERE " + this.columnName + "=?;");
         pst.setString(1, auth.getHash());
         pst.setString(2, auth.getNickname());
         pst.executeUpdate();
         return true;
      } catch (SQLException ex) {
         ConsoleLogger.showError(ex.getMessage());
      } finally {
         this.close((Statement)pst);
      }

      return false;
   }

   public boolean updateSession(PlayerAuth auth) {
      PreparedStatement pst = null;

      try {
         pst = this.con.prepareStatement("UPDATE " + this.tableName + " SET " + this.columnIp + "=?, " + this.columnLastLogin + "=? WHERE " + this.columnName + "=?;");
         pst.setString(1, auth.getIp());
         pst.setLong(2, auth.getLastLogin());
         pst.setString(3, auth.getNickname());
         pst.executeUpdate();
         return true;
      } catch (SQLException ex) {
         ConsoleLogger.showError(ex.getMessage());
      } finally {
         this.close((Statement)pst);
      }

      return false;
   }

   public int purgeDatabase(long until) {
      PreparedStatement pst = null;

      try {
         pst = this.con.prepareStatement("DELETE FROM " + this.tableName + " WHERE " + this.columnLastLogin + "<?;");
         pst.setLong(1, until);
         int var6 = pst.executeUpdate();
         return var6;
      } catch (SQLException ex) {
         ConsoleLogger.showError(ex.getMessage());
      } finally {
         this.close((Statement)pst);
      }

      return 0;
   }

   public synchronized boolean removeAuth(String user) {
      PreparedStatement pst = null;

      try {
         pst = this.con.prepareStatement("DELETE FROM " + this.tableName + " WHERE " + this.columnName + "=?;");
         pst.setString(1, user);
         pst.executeUpdate();
         return true;
      } catch (SQLException ex) {
         ConsoleLogger.showError(ex.getMessage());
      } finally {
         this.close((Statement)pst);
      }

      return false;
   }

   public boolean updateQuitLoc(PlayerAuth auth) {
      PreparedStatement pst = null;

      try {
         pst = this.con.prepareStatement("UPDATE " + this.tableName + " SET " + this.lastlocX + "=?, " + this.lastlocY + "=?, " + this.lastlocZ + "=?, " + this.lastlocWorld + "=? WHERE " + this.columnName + "=?;");
         pst.setLong(1, (long)auth.getQuitLocX());
         pst.setLong(2, (long)auth.getQuitLocY());
         pst.setLong(3, (long)auth.getQuitLocZ());
         pst.setString(4, auth.getWorld());
         pst.setString(5, auth.getNickname());
         pst.executeUpdate();
         return true;
      } catch (SQLException ex) {
         ConsoleLogger.showError(ex.getMessage());
      } finally {
         this.close((Statement)pst);
      }

      return false;
   }

   public int getIps(String ip) {
      PreparedStatement pst = null;
      ResultSet rs = null;
      int countIp = 0;

      try {
         pst = this.con.prepareStatement("SELECT * FROM " + this.tableName + " WHERE " + this.columnIp + "=?;");
         pst.setString(1, ip);

         for(rs = pst.executeQuery(); rs.next(); ++countIp) {
         }

         int var7 = countIp;
         return var7;
      } catch (SQLException ex) {
         ConsoleLogger.showError(ex.getMessage());
      } finally {
         this.close(rs);
         this.close((Statement)pst);
      }

      return 0;
   }

   public boolean updateEmail(PlayerAuth auth) {
      PreparedStatement pst = null;

      try {
         pst = this.con.prepareStatement("UPDATE " + this.tableName + " SET " + this.columnEmail + "=? WHERE " + this.columnName + "=?;");
         pst.setString(1, auth.getEmail());
         pst.setString(2, auth.getNickname());
         pst.executeUpdate();
         return true;
      } catch (SQLException ex) {
         ConsoleLogger.showError(ex.getMessage());
      } finally {
         this.close((Statement)pst);
      }

      return false;
   }

   public boolean updateSalt(PlayerAuth auth) {
      if (this.columnSalt.isEmpty()) {
         return false;
      } else {
         PreparedStatement pst = null;

         try {
            pst = this.con.prepareStatement("UPDATE " + this.tableName + " SET " + this.columnSalt + "=? WHERE " + this.columnName + "=?;");
            pst.setString(1, auth.getSalt());
            pst.setString(2, auth.getNickname());
            pst.executeUpdate();
            return true;
         } catch (SQLException ex) {
            ConsoleLogger.showError(ex.getMessage());
         } finally {
            this.close((Statement)pst);
         }

         return false;
      }
   }

   public synchronized void close() {
      try {
         this.con.close();
      } catch (SQLException ex) {
         ConsoleLogger.showError(ex.getMessage());
      }

   }

   public void reload() {
   }

   private void close(Statement st) {
      if (st != null) {
         try {
            st.close();
         } catch (SQLException ex) {
            ConsoleLogger.showError(ex.getMessage());
         }
      }

   }

   private void close(ResultSet rs) {
      if (rs != null) {
         try {
            rs.close();
         } catch (SQLException ex) {
            ConsoleLogger.showError(ex.getMessage());
         }
      }

   }

   private void close(Connection con) {
      if (con != null) {
         try {
            con.close();
         } catch (SQLException ex) {
            ConsoleLogger.showError(ex.getMessage());
         }
      }

   }

   public List getAllAuthsByName(PlayerAuth auth) {
      PreparedStatement pst = null;
      ResultSet rs = null;
      List<String> countIp = new ArrayList();

      List var17;
      try {
         pst = this.con.prepareStatement("SELECT * FROM " + this.tableName + " WHERE " + this.columnIp + "=?;");
         pst.setString(1, auth.getIp());
         rs = pst.executeQuery();

         while(rs.next()) {
            countIp.add(rs.getString(this.columnName));
         }

         var17 = countIp;
         return var17;
      } catch (SQLException ex) {
         ConsoleLogger.showError(ex.getMessage());
         var17 = new ArrayList();
      } catch (MiniConnectionPoolManager.TimeoutException ex) {
         ConsoleLogger.showError(ex.getMessage());
         var17 = new ArrayList();
         return var17;
      } catch (NullPointerException var14) {
         var17 = new ArrayList();
         return var17;
      } finally {
         this.close(rs);
         this.close((Statement)pst);
      }

      return var17;
   }

   public List getAllAuthsByIp(String ip) {
      PreparedStatement pst = null;
      ResultSet rs = null;
      List<String> countIp = new ArrayList();

      List var17;
      try {
         pst = this.con.prepareStatement("SELECT * FROM " + this.tableName + " WHERE " + this.columnIp + "=?;");
         pst.setString(1, ip);
         rs = pst.executeQuery();

         while(rs.next()) {
            countIp.add(rs.getString(this.columnName));
         }

         var17 = countIp;
         return var17;
      } catch (SQLException ex) {
         ConsoleLogger.showError(ex.getMessage());
         var17 = new ArrayList();
      } catch (MiniConnectionPoolManager.TimeoutException ex) {
         ConsoleLogger.showError(ex.getMessage());
         var17 = new ArrayList();
         return var17;
      } catch (NullPointerException var14) {
         var17 = new ArrayList();
         return var17;
      } finally {
         this.close(rs);
         this.close((Statement)pst);
      }

      return var17;
   }

   public List getAllAuthsByEmail(String email) {
      PreparedStatement pst = null;
      ResultSet rs = null;
      List<String> countEmail = new ArrayList();

      List var17;
      try {
         pst = this.con.prepareStatement("SELECT * FROM " + this.tableName + " WHERE " + this.columnEmail + "=?;");
         pst.setString(1, email);
         rs = pst.executeQuery();

         while(rs.next()) {
            countEmail.add(rs.getString(this.columnName));
         }

         var17 = countEmail;
         return var17;
      } catch (SQLException ex) {
         ConsoleLogger.showError(ex.getMessage());
         var17 = new ArrayList();
      } catch (MiniConnectionPoolManager.TimeoutException ex) {
         ConsoleLogger.showError(ex.getMessage());
         var17 = new ArrayList();
         return var17;
      } catch (NullPointerException var14) {
         var17 = new ArrayList();
         return var17;
      } finally {
         this.close(rs);
         this.close((Statement)pst);
      }

      return var17;
   }

   public void purgeBanned(List banned) {
      PreparedStatement pst = null;

      try {
         for(String name : banned) {
            pst = this.con.prepareStatement("DELETE FROM " + this.tableName + " WHERE " + this.columnName + "=?;");
            pst.setString(1, name);
            pst.executeUpdate();
         }
      } catch (SQLException ex) {
         ConsoleLogger.showError(ex.getMessage());
      } finally {
         this.close((Statement)pst);
      }

   }
}
