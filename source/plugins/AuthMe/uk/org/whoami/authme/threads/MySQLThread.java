package uk.org.whoami.authme.threads;

import com.mysql.jdbc.jdbc2.optional.MysqlConnectionPoolDataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import uk.org.whoami.authme.AuthMe;
import uk.org.whoami.authme.ConsoleLogger;
import uk.org.whoami.authme.cache.auth.PlayerAuth;
import uk.org.whoami.authme.datasource.DataSource;
import uk.org.whoami.authme.datasource.MiniConnectionPoolManager;
import uk.org.whoami.authme.settings.Settings;

public class MySQLThread extends Thread implements DataSource {
   private String host;
   private String port;
   private String username;
   private String password;
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
   private List columnOthers;
   private MiniConnectionPoolManager conPool;

   public MySQLThread() {
      super();
   }

   public void run() {
      this.host = Settings.getMySQLHost;
      this.port = Settings.getMySQLPort;
      this.username = Settings.getMySQLUsername;
      this.password = Settings.getMySQLPassword;
      this.database = Settings.getMySQLDatabase;
      this.tableName = Settings.getMySQLTablename;
      this.columnName = Settings.getMySQLColumnName;
      this.columnPassword = Settings.getMySQLColumnPassword;
      this.columnIp = Settings.getMySQLColumnIp;
      this.columnLastLogin = Settings.getMySQLColumnLastLogin;
      this.lastlocX = Settings.getMySQLlastlocX;
      this.lastlocY = Settings.getMySQLlastlocY;
      this.lastlocZ = Settings.getMySQLlastlocZ;
      this.lastlocWorld = Settings.getMySQLlastlocWorld;
      this.columnSalt = Settings.getMySQLColumnSalt;
      this.columnGroup = Settings.getMySQLColumnGroup;
      this.columnEmail = Settings.getMySQLColumnEmail;
      this.columnOthers = Settings.getMySQLOtherUsernameColumn;
      this.columnID = Settings.getMySQLColumnId;

      try {
         this.connect();
         this.setup();
      } catch (ClassNotFoundException e) {
         ConsoleLogger.showError(e.getMessage());
         if (Settings.isStopEnabled) {
            ConsoleLogger.showError("Can't use MySQL... Please input correct MySQL informations ! SHUTDOWN...");
            AuthMe.getInstance().getServer().shutdown();
         }

         if (!Settings.isStopEnabled) {
            AuthMe.getInstance().getServer().getPluginManager().disablePlugin(AuthMe.getInstance());
         }

      } catch (SQLException e) {
         ConsoleLogger.showError(e.getMessage());
         if (Settings.isStopEnabled) {
            ConsoleLogger.showError("Can't use MySQL... Please input correct MySQL informations ! SHUTDOWN...");
            AuthMe.getInstance().getServer().shutdown();
         }

         if (!Settings.isStopEnabled) {
            AuthMe.getInstance().getServer().getPluginManager().disablePlugin(AuthMe.getInstance());
         }

      } catch (MiniConnectionPoolManager.TimeoutException e) {
         ConsoleLogger.showError(e.getMessage());
         if (Settings.isStopEnabled) {
            ConsoleLogger.showError("Can't use MySQL... Please input correct MySQL informations ! SHUTDOWN...");
            AuthMe.getInstance().getServer().shutdown();
         }

         if (!Settings.isStopEnabled) {
            AuthMe.getInstance().getServer().getPluginManager().disablePlugin(AuthMe.getInstance());
         }

      }
   }

   private synchronized void connect() throws ClassNotFoundException, SQLException, MiniConnectionPoolManager.TimeoutException {
      Class.forName("com.mysql.jdbc.Driver");
      ConsoleLogger.info("MySQL driver loaded");
      MysqlConnectionPoolDataSource dataSource = new MysqlConnectionPoolDataSource();
      dataSource.setDatabaseName(this.database);
      dataSource.setServerName(this.host);
      dataSource.setPort(Integer.parseInt(this.port));
      dataSource.setUser(this.username);
      dataSource.setPassword(this.password);
      this.conPool = new MiniConnectionPoolManager(dataSource, 10);
      ConsoleLogger.info("Connection pool ready");
   }

   private synchronized void setup() throws SQLException {
      Connection con = null;
      Statement st = null;
      ResultSet rs = null;

      try {
         con = this.makeSureConnectionIsReady();
         st = con.createStatement();
         st.executeUpdate("CREATE TABLE IF NOT EXISTS " + this.tableName + " (" + this.columnID + " INTEGER AUTO_INCREMENT," + this.columnName + " VARCHAR(255) NOT NULL UNIQUE," + this.columnPassword + " VARCHAR(255) NOT NULL," + this.columnIp + " VARCHAR(40) NOT NULL," + this.columnLastLogin + " BIGINT," + this.lastlocX + " smallint(6) DEFAULT '0'," + this.lastlocY + " smallint(6) DEFAULT '0'," + this.lastlocZ + " smallint(6) DEFAULT '0'," + this.lastlocWorld + " VARCHAR(255) DEFAULT 'world'," + this.columnEmail + " VARCHAR(255) DEFAULT 'your@email.com'," + "CONSTRAINT table_const_prim PRIMARY KEY (" + this.columnID + "));");
         rs = con.getMetaData().getColumns((String)null, (String)null, this.tableName, this.columnPassword);
         if (!rs.next()) {
            st.executeUpdate("ALTER TABLE " + this.tableName + " ADD COLUMN " + this.columnPassword + " VARCHAR(255) NOT NULL;");
         }

         rs.close();
         rs = con.getMetaData().getColumns((String)null, (String)null, this.tableName, this.columnIp);
         if (!rs.next()) {
            st.executeUpdate("ALTER TABLE " + this.tableName + " ADD COLUMN " + this.columnIp + " VARCHAR(40) NOT NULL;");
         }

         rs.close();
         rs = con.getMetaData().getColumns((String)null, (String)null, this.tableName, this.columnLastLogin);
         if (!rs.next()) {
            st.executeUpdate("ALTER TABLE " + this.tableName + " ADD COLUMN " + this.columnLastLogin + " BIGINT;");
         }

         rs.close();
         rs = con.getMetaData().getColumns((String)null, (String)null, this.tableName, this.lastlocX);
         if (!rs.next()) {
            st.executeUpdate("ALTER TABLE " + this.tableName + " ADD COLUMN " + this.lastlocX + " smallint(6) NOT NULL DEFAULT '0' AFTER " + this.columnLastLogin + " , ADD " + this.lastlocY + " smallint(6) NOT NULL DEFAULT '0' AFTER " + this.lastlocX + " , ADD " + this.lastlocZ + " smallint(6) NOT NULL DEFAULT '0' AFTER " + this.lastlocY + ";");
         }

         rs.close();
         rs = con.getMetaData().getColumns((String)null, (String)null, this.tableName, this.lastlocWorld);
         if (!rs.next()) {
            st.executeUpdate("ALTER TABLE " + this.tableName + " ADD COLUMN " + this.lastlocWorld + " VARCHAR(255) NOT NULL DEFAULT 'world' AFTER " + this.lastlocZ + ";");
         }

         rs.close();
         rs = con.getMetaData().getColumns((String)null, (String)null, this.tableName, this.columnEmail);
         if (!rs.next()) {
            st.executeUpdate("ALTER TABLE " + this.tableName + " ADD COLUMN " + this.columnEmail + " VARCHAR(255) DEFAULT 'your@email.com' AFTER " + this.lastlocZ + ";");
         }
      } finally {
         this.close(rs);
         this.close(st);
         this.close(con);
      }

   }

   public synchronized boolean isAuthAvailable(String user) {
      Connection con = null;
      PreparedStatement pst = null;
      ResultSet rs = null;

      try {
         con = this.makeSureConnectionIsReady();
         pst = con.prepareStatement("SELECT * FROM " + this.tableName + " WHERE " + this.columnName + "=?;");
         pst.setString(1, user);
         rs = pst.executeQuery();
         boolean var7 = rs.next();
         return var7;
      } catch (SQLException ex) {
         ConsoleLogger.showError(ex.getMessage());
      } catch (MiniConnectionPoolManager.TimeoutException ex) {
         ConsoleLogger.showError(ex.getMessage());
         return false;
      } finally {
         this.close(rs);
         this.close((Statement)pst);
         this.close(con);
      }

      return false;
   }

   public synchronized PlayerAuth getAuth(String user) {
      Connection con = null;
      PreparedStatement pst = null;
      ResultSet rs = null;

      PlayerAuth var7;
      try {
         con = this.makeSureConnectionIsReady();
         pst = con.prepareStatement("SELECT * FROM " + this.tableName + " WHERE " + this.columnName + "=?;");
         pst.setString(1, user);
         rs = pst.executeQuery();
         if (!rs.next()) {
            return null;
         }

         if (!rs.getString(this.columnIp).isEmpty()) {
            if (this.columnSalt.isEmpty()) {
               var7 = new PlayerAuth(rs.getString(this.columnName), rs.getString(this.columnPassword), rs.getString(this.columnIp), rs.getLong(this.columnLastLogin), rs.getInt(this.lastlocX), rs.getInt(this.lastlocY), rs.getInt(this.lastlocZ), rs.getString(this.lastlocWorld), rs.getString(this.columnEmail));
               return var7;
            }

            if (!this.columnGroup.isEmpty()) {
               var7 = new PlayerAuth(rs.getString(this.columnName), rs.getString(this.columnPassword), rs.getString(this.columnSalt), rs.getInt(this.columnGroup), rs.getString(this.columnIp), rs.getLong(this.columnLastLogin), rs.getInt(this.lastlocX), rs.getInt(this.lastlocY), rs.getInt(this.lastlocZ), rs.getString(this.lastlocWorld), rs.getString(this.columnEmail));
               return var7;
            }

            var7 = new PlayerAuth(rs.getString(this.columnName), rs.getString(this.columnPassword), rs.getString(this.columnSalt), rs.getString(this.columnIp), rs.getLong(this.columnLastLogin), rs.getInt(this.lastlocX), rs.getInt(this.lastlocY), rs.getInt(this.lastlocZ), rs.getString(this.lastlocWorld), rs.getString(this.columnEmail));
            return var7;
         }

         var7 = new PlayerAuth(rs.getString(this.columnName), rs.getString(this.columnPassword), "198.18.0.1", rs.getLong(this.columnLastLogin), rs.getInt(this.lastlocX), rs.getInt(this.lastlocY), rs.getInt(this.lastlocZ), rs.getString(this.lastlocWorld), rs.getString(this.columnEmail));
      } catch (SQLException ex) {
         ConsoleLogger.showError(ex.getMessage());
         return null;
      } catch (MiniConnectionPoolManager.TimeoutException ex) {
         ConsoleLogger.showError(ex.getMessage());
         return null;
      } finally {
         this.close(rs);
         this.close((Statement)pst);
         this.close(con);
      }

      return var7;
   }

   public synchronized boolean saveAuth(PlayerAuth auth) {
      Connection con = null;
      PreparedStatement pst = null;

      try {
         con = this.makeSureConnectionIsReady();
         if (!this.columnSalt.isEmpty() && this.columnSalt != null || !auth.getSalt().isEmpty() && auth.getSalt() != null) {
            pst = con.prepareStatement("INSERT INTO " + this.tableName + "(" + this.columnName + "," + this.columnPassword + "," + this.columnIp + "," + this.columnLastLogin + "," + this.columnSalt + ") VALUES (?,?,?,?,?);");
            pst.setString(1, auth.getNickname());
            pst.setString(2, auth.getHash());
            pst.setString(3, auth.getIp());
            pst.setLong(4, auth.getLastLogin());
            pst.setString(5, auth.getSalt());
            pst.executeUpdate();
         } else {
            pst = con.prepareStatement("INSERT INTO " + this.tableName + "(" + this.columnName + "," + this.columnPassword + "," + this.columnIp + "," + this.columnLastLogin + ") VALUES (?,?,?,?);");
            pst.setString(1, auth.getNickname());
            pst.setString(2, auth.getHash());
            pst.setString(3, auth.getIp());
            pst.setLong(4, auth.getLastLogin());
            pst.executeUpdate();
         }

         if (!this.columnOthers.isEmpty()) {
            for(String column : this.columnOthers) {
               pst = con.prepareStatement("UPDATE " + this.tableName + " SET " + this.tableName + "." + column + "=? WHERE " + this.columnName + "=?;");
               pst.setString(1, auth.getNickname());
               pst.setString(2, auth.getNickname());
               pst.executeUpdate();
            }
         }

         return true;
      } catch (SQLException ex) {
         ConsoleLogger.showError(ex.getMessage());
      } catch (MiniConnectionPoolManager.TimeoutException ex) {
         ConsoleLogger.showError(ex.getMessage());
         return false;
      } finally {
         this.close((Statement)pst);
         this.close(con);
      }

      return false;
   }

   public synchronized boolean updatePassword(PlayerAuth auth) {
      Connection con = null;
      PreparedStatement pst = null;

      try {
         con = this.makeSureConnectionIsReady();
         pst = con.prepareStatement("UPDATE " + this.tableName + " SET " + this.columnPassword + "=? WHERE " + this.columnName + "=?;");
         pst.setString(1, auth.getHash());
         pst.setString(2, auth.getNickname());
         pst.executeUpdate();
         return true;
      } catch (SQLException ex) {
         ConsoleLogger.showError(ex.getMessage());
      } catch (MiniConnectionPoolManager.TimeoutException ex) {
         ConsoleLogger.showError(ex.getMessage());
         return false;
      } finally {
         this.close((Statement)pst);
         this.close(con);
      }

      return false;
   }

   public synchronized boolean updateSession(PlayerAuth auth) {
      Connection con = null;
      PreparedStatement pst = null;

      try {
         con = this.makeSureConnectionIsReady();
         pst = con.prepareStatement("UPDATE " + this.tableName + " SET " + this.columnIp + "=?, " + this.columnLastLogin + "=? WHERE " + this.columnName + "=?;");
         pst.setString(1, auth.getIp());
         pst.setLong(2, auth.getLastLogin());
         pst.setString(3, auth.getNickname());
         pst.executeUpdate();
         return true;
      } catch (SQLException ex) {
         ConsoleLogger.showError(ex.getMessage());
      } catch (MiniConnectionPoolManager.TimeoutException ex) {
         ConsoleLogger.showError(ex.getMessage());
         return false;
      } finally {
         this.close((Statement)pst);
         this.close(con);
      }

      return false;
   }

   public synchronized int purgeDatabase(long until) {
      Connection con = null;
      PreparedStatement pst = null;

      try {
         con = this.makeSureConnectionIsReady();
         pst = con.prepareStatement("DELETE FROM " + this.tableName + " WHERE " + this.columnLastLogin + "<?;");
         pst.setLong(1, until);
         int var7 = pst.executeUpdate();
         return var7;
      } catch (SQLException ex) {
         ConsoleLogger.showError(ex.getMessage());
      } catch (MiniConnectionPoolManager.TimeoutException ex) {
         ConsoleLogger.showError(ex.getMessage());
         return 0;
      } finally {
         this.close((Statement)pst);
         this.close(con);
      }

      return 0;
   }

   public synchronized boolean removeAuth(String user) {
      Connection con = null;
      PreparedStatement pst = null;

      try {
         con = this.makeSureConnectionIsReady();
         pst = con.prepareStatement("DELETE FROM " + this.tableName + " WHERE " + this.columnName + "=?;");
         pst.setString(1, user);
         pst.executeUpdate();
         return true;
      } catch (SQLException ex) {
         ConsoleLogger.showError(ex.getMessage());
      } catch (MiniConnectionPoolManager.TimeoutException ex) {
         ConsoleLogger.showError(ex.getMessage());
         return false;
      } finally {
         this.close((Statement)pst);
         this.close(con);
      }

      return false;
   }

   public synchronized boolean updateQuitLoc(PlayerAuth auth) {
      Connection con = null;
      PreparedStatement pst = null;

      try {
         con = this.makeSureConnectionIsReady();
         pst = con.prepareStatement("UPDATE " + this.tableName + " SET " + this.lastlocX + " =?, " + this.lastlocY + "=?, " + this.lastlocZ + "=?, " + this.lastlocWorld + "=? WHERE " + this.columnName + "=?;");
         pst.setLong(1, (long)auth.getQuitLocX());
         pst.setLong(2, (long)auth.getQuitLocY());
         pst.setLong(3, (long)auth.getQuitLocZ());
         pst.setString(4, auth.getWorld());
         pst.setString(5, auth.getNickname());
         pst.executeUpdate();
         return true;
      } catch (SQLException ex) {
         ConsoleLogger.showError(ex.getMessage());
      } catch (MiniConnectionPoolManager.TimeoutException ex) {
         ConsoleLogger.showError(ex.getMessage());
         return false;
      } finally {
         this.close((Statement)pst);
         this.close(con);
      }

      return false;
   }

   public synchronized int getIps(String ip) {
      Connection con = null;
      PreparedStatement pst = null;
      ResultSet rs = null;
      int countIp = 0;

      try {
         con = this.makeSureConnectionIsReady();
         pst = con.prepareStatement("SELECT * FROM " + this.tableName + " WHERE " + this.columnIp + "=?;");
         pst.setString(1, ip);

         for(rs = pst.executeQuery(); rs.next(); ++countIp) {
         }

         int var8 = countIp;
         return var8;
      } catch (SQLException ex) {
         ConsoleLogger.showError(ex.getMessage());
      } catch (MiniConnectionPoolManager.TimeoutException ex) {
         ConsoleLogger.showError(ex.getMessage());
         return 0;
      } finally {
         this.close(rs);
         this.close((Statement)pst);
         this.close(con);
      }

      return 0;
   }

   public synchronized boolean updateEmail(PlayerAuth auth) {
      Connection con = null;
      PreparedStatement pst = null;

      try {
         con = this.makeSureConnectionIsReady();
         pst = con.prepareStatement("UPDATE " + this.tableName + " SET " + this.columnEmail + " =? WHERE " + this.columnName + "=?;");
         pst.setString(1, auth.getEmail());
         pst.setString(2, auth.getNickname());
         pst.executeUpdate();
         return true;
      } catch (SQLException ex) {
         ConsoleLogger.showError(ex.getMessage());
      } catch (MiniConnectionPoolManager.TimeoutException ex) {
         ConsoleLogger.showError(ex.getMessage());
         return false;
      } finally {
         this.close((Statement)pst);
         this.close(con);
      }

      return false;
   }

   public synchronized boolean updateSalt(PlayerAuth auth) {
      if (this.columnSalt.isEmpty()) {
         return false;
      } else {
         Connection con = null;
         PreparedStatement pst = null;

         try {
            con = this.makeSureConnectionIsReady();
            pst = con.prepareStatement("UPDATE " + this.tableName + " SET " + this.columnSalt + " =? WHERE " + this.columnName + "=?;");
            pst.setString(1, auth.getSalt());
            pst.setString(2, auth.getNickname());
            pst.executeUpdate();
            return true;
         } catch (SQLException ex) {
            ConsoleLogger.showError(ex.getMessage());
         } catch (MiniConnectionPoolManager.TimeoutException ex) {
            ConsoleLogger.showError(ex.getMessage());
            return false;
         } finally {
            this.close((Statement)pst);
            this.close(con);
         }

         return false;
      }
   }

   public synchronized void close() {
      try {
         this.conPool.dispose();
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

   public synchronized List getAllAuthsByName(PlayerAuth auth) {
      Connection con = null;
      PreparedStatement pst = null;
      ResultSet rs = null;
      List<String> countIp = new ArrayList();

      List var15;
      try {
         con = this.makeSureConnectionIsReady();
         pst = con.prepareStatement("SELECT * FROM " + this.tableName + " WHERE " + this.columnIp + "=?;");
         pst.setString(1, auth.getIp());
         rs = pst.executeQuery();

         while(rs.next()) {
            countIp.add(rs.getString(this.columnName));
         }

         var15 = countIp;
         return var15;
      } catch (SQLException ex) {
         ConsoleLogger.showError(ex.getMessage());
         var15 = new ArrayList();
      } catch (MiniConnectionPoolManager.TimeoutException ex) {
         ConsoleLogger.showError(ex.getMessage());
         var15 = new ArrayList();
         return var15;
      } finally {
         this.close(rs);
         this.close((Statement)pst);
         this.close(con);
      }

      return var15;
   }

   public synchronized List getAllAuthsByIp(String ip) {
      Connection con = null;
      PreparedStatement pst = null;
      ResultSet rs = null;
      List<String> countIp = new ArrayList();

      List var15;
      try {
         con = this.makeSureConnectionIsReady();
         pst = con.prepareStatement("SELECT * FROM " + this.tableName + " WHERE " + this.columnIp + "=?;");
         pst.setString(1, ip);
         rs = pst.executeQuery();

         while(rs.next()) {
            countIp.add(rs.getString(this.columnName));
         }

         var15 = countIp;
         return var15;
      } catch (SQLException ex) {
         ConsoleLogger.showError(ex.getMessage());
         var15 = new ArrayList();
      } catch (MiniConnectionPoolManager.TimeoutException ex) {
         ConsoleLogger.showError(ex.getMessage());
         var15 = new ArrayList();
         return var15;
      } finally {
         this.close(rs);
         this.close((Statement)pst);
         this.close(con);
      }

      return var15;
   }

   public synchronized List getAllAuthsByEmail(String email) {
      Connection con = null;
      PreparedStatement pst = null;
      ResultSet rs = null;
      List<String> countEmail = new ArrayList();

      List var15;
      try {
         con = this.makeSureConnectionIsReady();
         pst = con.prepareStatement("SELECT * FROM " + this.tableName + " WHERE " + this.columnEmail + "=?;");
         pst.setString(1, email);
         rs = pst.executeQuery();

         while(rs.next()) {
            countEmail.add(rs.getString(this.columnName));
         }

         var15 = countEmail;
         return var15;
      } catch (SQLException ex) {
         ConsoleLogger.showError(ex.getMessage());
         var15 = new ArrayList();
      } catch (MiniConnectionPoolManager.TimeoutException ex) {
         ConsoleLogger.showError(ex.getMessage());
         var15 = new ArrayList();
         return var15;
      } finally {
         this.close(rs);
         this.close((Statement)pst);
         this.close(con);
      }

      return var15;
   }

   public synchronized void purgeBanned(List banned) {
      Connection con = null;
      PreparedStatement pst = null;

      try {
         for(String name : banned) {
            con = this.makeSureConnectionIsReady();
            pst = con.prepareStatement("DELETE FROM " + this.tableName + " WHERE " + this.columnName + "=?;");
            pst.setString(1, name);
            pst.executeUpdate();
         }
      } catch (SQLException ex) {
         ConsoleLogger.showError(ex.getMessage());
      } finally {
         this.close((Statement)pst);
         this.close(con);
      }

   }

   private synchronized Connection makeSureConnectionIsReady() {
      Connection con = null;

      try {
         con = this.conPool.getValidConnection();
      } catch (Exception var6) {
         try {
            con = null;
            this.reconnect();
         } catch (Exception e) {
            ConsoleLogger.showError(e.getMessage());
            if (Settings.isStopEnabled) {
               ConsoleLogger.showError("Can't reconnect to MySQL database... Please check your MySQL informations ! SHUTDOWN...");
               AuthMe.getInstance().getServer().shutdown();
            }

            if (!Settings.isStopEnabled) {
               AuthMe.getInstance().getServer().getPluginManager().disablePlugin(AuthMe.getInstance());
            }
         }
      } catch (AssertionError ae) {
         if (!ae.getMessage().equalsIgnoreCase("AuthMeDatabaseError")) {
            throw new AssertionError(ae.getMessage());
         }

         try {
            con = null;
            this.reconnect();
         } catch (Exception e) {
            ConsoleLogger.showError(e.getMessage());
            if (Settings.isStopEnabled) {
               ConsoleLogger.showError("Can't reconnect to MySQL database... Please check your MySQL informations ! SHUTDOWN...");
               AuthMe.getInstance().getServer().shutdown();
            }

            if (!Settings.isStopEnabled) {
               AuthMe.getInstance().getServer().getPluginManager().disablePlugin(AuthMe.getInstance());
            }
         }
      }

      if (con == null) {
         con = this.conPool.getValidConnection();
      }

      return con;
   }

   private synchronized void reconnect() throws ClassNotFoundException, SQLException, MiniConnectionPoolManager.TimeoutException {
      this.conPool.dispose();
      Class.forName("com.mysql.jdbc.Driver");
      MysqlConnectionPoolDataSource dataSource = new MysqlConnectionPoolDataSource();
      dataSource.setDatabaseName(this.database);
      dataSource.setServerName(this.host);
      dataSource.setPort(Integer.parseInt(this.port));
      dataSource.setUser(this.username);
      dataSource.setPassword(this.password);
      this.conPool = new MiniConnectionPoolManager(dataSource, 10);
      ConsoleLogger.info("ConnectionPool was unavailable... Reconnected!");
   }
}
