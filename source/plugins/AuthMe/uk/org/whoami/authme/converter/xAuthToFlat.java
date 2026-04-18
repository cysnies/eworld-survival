package uk.org.whoami.authme.converter;

import com.cypherx.xauth.xAuth;
import com.cypherx.xauth.database.Table;
import com.cypherx.xauth.utils.xAuthLog;
import java.io.File;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import org.bukkit.command.CommandSender;
import uk.org.whoami.authme.AuthMe;
import uk.org.whoami.authme.cache.auth.PlayerAuth;
import uk.org.whoami.authme.datasource.DataSource;

public class xAuthToFlat {
   public AuthMe instance;
   public DataSource database;

   public xAuthToFlat(AuthMe instance, DataSource database) {
      super();
      this.instance = instance;
      this.database = database;
   }

   public boolean convert(CommandSender sender) {
      if (this.instance.getServer().getPluginManager().getPlugin("xAuth") == null) {
         sender.sendMessage("[AuthMe] xAuth plugin not found");
         return false;
      } else {
         if (!(new File("./plugins/xAuth/xAuth.h2.db")).exists()) {
            sender.sendMessage("[AuthMe] xAuth H2 database not found, checking for MySQL or SQLite data...");
         }

         List<Integer> players = this.getXAuthPlayers();
         if (players != null && !players.isEmpty()) {
            sender.sendMessage("[AuthMe] Starting import...");

            for(int id : players) {
               String pl = this.getIdPlayer(id);
               String psw = this.getPassword(id);
               if (psw != null && !psw.isEmpty() && pl != null) {
                  PlayerAuth auth = new PlayerAuth(pl, psw, "198.18.0.1", 0L);
                  this.database.saveAuth(auth);
               }
            }

            sender.sendMessage("[AuthMe] Import done!");
            return true;
         } else {
            sender.sendMessage("[AuthMe] Error while import xAuthPlayers");
            return false;
         }
      }
   }

   public String getIdPlayer(int id) {
      String realPass = "";
      Connection conn = xAuth.getPlugin().getDatabaseController().getConnection();
      PreparedStatement ps = null;
      ResultSet rs = null;

      try {
         String sql = String.format("SELECT `playername` FROM `%s` WHERE `id` = ?", xAuth.getPlugin().getDatabaseController().getTable(Table.ACCOUNT));
         ps = conn.prepareStatement(sql);
         ps.setInt(1, id);
         rs = ps.executeQuery();
         if (rs.next()) {
            realPass = rs.getString("playername").toLowerCase();
            return realPass;
         }
      } catch (SQLException e) {
         xAuthLog.severe("Failed to retrieve name for account: " + id, e);
         return null;
      } finally {
         xAuth.getPlugin().getDatabaseController().close(conn, ps, rs);
      }

      return null;
   }

   public List getXAuthPlayers() {
      List<Integer> xP = new ArrayList();
      Connection conn = xAuth.getPlugin().getDatabaseController().getConnection();
      PreparedStatement ps = null;
      ResultSet rs = null;

      ArrayList var7;
      try {
         String sql = String.format("SELECT * FROM `%s`", xAuth.getPlugin().getDatabaseController().getTable(Table.ACCOUNT));
         ps = conn.prepareStatement(sql);
         rs = ps.executeQuery();

         while(rs.next()) {
            xP.add(rs.getInt("id"));
         }

         return xP;
      } catch (SQLException e) {
         xAuthLog.severe("Cannot import xAuthPlayers", e);
         var7 = new ArrayList();
      } finally {
         xAuth.getPlugin().getDatabaseController().close(conn, ps, rs);
      }

      return var7;
   }

   public String getPassword(int accountId) {
      String realPass = "";
      Connection conn = xAuth.getPlugin().getDatabaseController().getConnection();
      PreparedStatement ps = null;
      ResultSet rs = null;

      try {
         String sql = String.format("SELECT `password`, `pwtype` FROM `%s` WHERE `id` = ?", xAuth.getPlugin().getDatabaseController().getTable(Table.ACCOUNT));
         ps = conn.prepareStatement(sql);
         ps.setInt(1, accountId);
         rs = ps.executeQuery();
         if (rs.next()) {
            realPass = rs.getString("password");
            return realPass;
         }
      } catch (SQLException e) {
         xAuthLog.severe("Failed to retrieve password hash for account: " + accountId, e);
         return null;
      } finally {
         xAuth.getPlugin().getDatabaseController().close(conn, ps, rs);
      }

      return null;
   }
}
