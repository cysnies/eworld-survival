package uk.org.whoami.authme.api;

import java.security.NoSuchAlgorithmException;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.Plugin;
import uk.org.whoami.authme.AuthMe;
import uk.org.whoami.authme.Utils;
import uk.org.whoami.authme.cache.auth.PlayerAuth;
import uk.org.whoami.authme.cache.auth.PlayerCache;
import uk.org.whoami.authme.datasource.DataSource;
import uk.org.whoami.authme.plugin.manager.CombatTagComunicator;
import uk.org.whoami.authme.security.PasswordSecurity;
import uk.org.whoami.authme.settings.Settings;

public class API {
   public static AuthMe instance;
   public static DataSource database;

   public API(AuthMe instance, DataSource database) {
      super();
      API.instance = instance;
      API.database = database;
   }

   public static AuthMe hookAuthMe() {
      Plugin plugin = Bukkit.getServer().getPluginManager().getPlugin("AuthMe");
      return plugin == null && !(plugin instanceof AuthMe) ? null : (AuthMe)plugin;
   }

   public AuthMe getPlugin() {
      return instance;
   }

   public static boolean isAuthenticated(Player player) {
      return PlayerCache.getInstance().isAuthenticated(player.getName().toLowerCase());
   }

   /** @deprecated */
   @Deprecated
   public boolean isaNPC(Player player) {
      return instance.getCitizensCommunicator().isNPC(player, instance) ? true : CombatTagComunicator.isNPC(player);
   }

   public boolean isNPC(Player player) {
      return instance.getCitizensCommunicator().isNPC(player, instance) ? true : CombatTagComunicator.isNPC(player);
   }

   public static boolean isUnrestricted(Player player) {
      return Utils.getInstance().isUnrestricted(player);
   }

   public static Location getLastLocation(Player player) {
      try {
         PlayerAuth auth = PlayerCache.getInstance().getAuth(player.getName().toLowerCase());
         if (auth != null) {
            Location loc = new Location(Bukkit.getWorld(auth.getWorld()), (double)auth.getQuitLocX(), (double)auth.getQuitLocY(), (double)auth.getQuitLocZ());
            return loc;
         } else {
            return null;
         }
      } catch (NullPointerException var3) {
         return null;
      }
   }

   public static void setPlayerInventory(Player player, ItemStack[] content, ItemStack[] armor) {
      try {
         player.getInventory().setContents(content);
         player.getInventory().setArmorContents(armor);
      } catch (NullPointerException var4) {
      }

   }

   public static boolean isRegistered(String playerName) {
      String player = playerName.toLowerCase();
      return database.isAuthAvailable(player);
   }

   public static boolean checkPassword(String playerName, String passwordToCheck) {
      if (!isRegistered(playerName)) {
         return false;
      } else {
         String player = playerName.toLowerCase();
         PlayerAuth auth = database.getAuth(player);

         try {
            return PasswordSecurity.comparePasswordWithHash(passwordToCheck, auth.getHash(), player);
         } catch (NoSuchAlgorithmException var5) {
            return false;
         }
      }
   }

   public static boolean registerPlayer(String playerName, String password) {
      try {
         String name = playerName.toLowerCase();
         String hash = PasswordSecurity.getHash(Settings.getPasswordHash, password, name);
         if (isRegistered(name)) {
            return false;
         } else {
            PlayerAuth auth = new PlayerAuth(name, hash, "198.18.0.1", 0L);
            return database.saveAuth(auth);
         }
      } catch (NoSuchAlgorithmException var5) {
         return false;
      }
   }
}
