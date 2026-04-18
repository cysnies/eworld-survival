package uk.org.whoami.authme.datasource;

import java.util.List;
import uk.org.whoami.authme.cache.auth.PlayerAuth;

public interface DataSource {
   boolean isAuthAvailable(String var1);

   PlayerAuth getAuth(String var1);

   boolean saveAuth(PlayerAuth var1);

   boolean updateSession(PlayerAuth var1);

   boolean updatePassword(PlayerAuth var1);

   int purgeDatabase(long var1);

   boolean removeAuth(String var1);

   boolean updateQuitLoc(PlayerAuth var1);

   int getIps(String var1);

   List getAllAuthsByName(PlayerAuth var1);

   List getAllAuthsByIp(String var1);

   List getAllAuthsByEmail(String var1);

   boolean updateEmail(PlayerAuth var1);

   boolean updateSalt(PlayerAuth var1);

   void close();

   void reload();

   void purgeBanned(List var1);

   public static enum DataSourceType {
      MYSQL,
      FILE,
      SQLITE;

      private DataSourceType() {
      }
   }
}
