package uk.org.whoami.authme.cache.auth;

import java.util.HashMap;

public class PlayerCache {
   private static PlayerCache singleton = null;
   private HashMap cache = new HashMap();

   private PlayerCache() {
      super();
   }

   public void addPlayer(PlayerAuth auth) {
      this.cache.put(auth.getNickname().toLowerCase(), auth);
   }

   public void updatePlayer(PlayerAuth auth) {
      this.cache.remove(auth.getNickname().toLowerCase());
      this.cache.put(auth.getNickname().toLowerCase(), auth);
   }

   public void removePlayer(String user) {
      this.cache.remove(user.toLowerCase());
   }

   public boolean isAuthenticated(String user) {
      return this.cache.containsKey(user.toLowerCase());
   }

   public PlayerAuth getAuth(String user) {
      return (PlayerAuth)this.cache.get(user.toLowerCase());
   }

   public static PlayerCache getInstance() {
      if (singleton == null) {
         singleton = new PlayerCache();
      }

      return singleton;
   }
}
