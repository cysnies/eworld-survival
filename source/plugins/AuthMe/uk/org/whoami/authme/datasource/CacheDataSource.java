package uk.org.whoami.authme.datasource;

import java.util.HashMap;
import java.util.List;
import org.bukkit.entity.Player;
import uk.org.whoami.authme.AuthMe;
import uk.org.whoami.authme.cache.auth.PlayerAuth;
import uk.org.whoami.authme.cache.auth.PlayerCache;

public class CacheDataSource implements DataSource {
   private DataSource source;
   public AuthMe plugin;
   private final HashMap cache = new HashMap();

   public CacheDataSource(AuthMe plugin, DataSource source) {
      super();
      this.plugin = plugin;
      this.source = source;
   }

   public synchronized boolean isAuthAvailable(String user) {
      return this.cache.containsKey(user) ? true : this.source.isAuthAvailable(user);
   }

   public synchronized PlayerAuth getAuth(String user) {
      if (this.cache.containsKey(user)) {
         return (PlayerAuth)this.cache.get(user);
      } else {
         PlayerAuth auth = this.source.getAuth(user);
         this.cache.put(user, auth);
         return auth;
      }
   }

   public synchronized boolean saveAuth(PlayerAuth auth) {
      if (this.source.saveAuth(auth)) {
         this.cache.put(auth.getNickname(), auth);
         return true;
      } else {
         return false;
      }
   }

   public synchronized boolean updatePassword(PlayerAuth auth) {
      if (this.source.updatePassword(auth)) {
         ((PlayerAuth)this.cache.get(auth.getNickname())).setHash(auth.getHash());
         return true;
      } else {
         return false;
      }
   }

   public boolean updateSession(PlayerAuth auth) {
      if (this.source.updateSession(auth)) {
         ((PlayerAuth)this.cache.get(auth.getNickname())).setIp(auth.getIp());
         ((PlayerAuth)this.cache.get(auth.getNickname())).setLastLogin(auth.getLastLogin());
         return true;
      } else {
         return false;
      }
   }

   public boolean updateQuitLoc(PlayerAuth auth) {
      if (this.source.updateQuitLoc(auth)) {
         ((PlayerAuth)this.cache.get(auth.getNickname())).setQuitLocX(auth.getQuitLocX());
         ((PlayerAuth)this.cache.get(auth.getNickname())).setQuitLocY(auth.getQuitLocY());
         ((PlayerAuth)this.cache.get(auth.getNickname())).setQuitLocZ(auth.getQuitLocZ());
         ((PlayerAuth)this.cache.get(auth.getNickname())).setWorld(auth.getWorld());
         return true;
      } else {
         return false;
      }
   }

   public int getIps(String ip) {
      return this.source.getIps(ip);
   }

   public int purgeDatabase(long until) {
      int cleared = this.source.purgeDatabase(until);
      if (cleared > 0) {
         for(PlayerAuth auth : this.cache.values()) {
            if (auth.getLastLogin() < until) {
               this.cache.remove(auth.getNickname());
            }
         }
      }

      return cleared;
   }

   public synchronized boolean removeAuth(String user) {
      if (this.source.removeAuth(user)) {
         this.cache.remove(user);
         return true;
      } else {
         return false;
      }
   }

   public synchronized void close() {
      this.source.close();
   }

   public void reload() {
      this.cache.clear();

      Player[] var4;
      for(Player player : var4 = this.plugin.getServer().getOnlinePlayers()) {
         String user = player.getName().toLowerCase();
         if (PlayerCache.getInstance().isAuthenticated(user)) {
            try {
               PlayerAuth auth = this.source.getAuth(user);
               this.cache.put(user, auth);
            } catch (NullPointerException var7) {
            }
         }
      }

   }

   public boolean updateEmail(PlayerAuth auth) {
      if (this.source.updateEmail(auth)) {
         ((PlayerAuth)this.cache.get(auth.getNickname())).setEmail(auth.getEmail());
         return true;
      } else {
         return false;
      }
   }

   public boolean updateSalt(PlayerAuth auth) {
      if (this.source.updateSalt(auth)) {
         ((PlayerAuth)this.cache.get(auth.getNickname())).setSalt(auth.getSalt());
         return true;
      } else {
         return false;
      }
   }

   public List getAllAuthsByName(PlayerAuth auth) {
      return this.source.getAllAuthsByName(auth);
   }

   public List getAllAuthsByIp(String ip) {
      return this.source.getAllAuthsByIp(ip);
   }

   public List getAllAuthsByEmail(String email) {
      return this.source.getAllAuthsByEmail(email);
   }

   public void purgeBanned(List banned) {
      this.source.purgeBanned(banned);

      for(PlayerAuth auth : this.cache.values()) {
         if (banned.contains(auth.getNickname())) {
            this.cache.remove(auth.getNickname());
         }
      }

   }
}
