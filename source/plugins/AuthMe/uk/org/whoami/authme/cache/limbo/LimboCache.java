package uk.org.whoami.authme.cache.limbo;

import java.util.HashMap;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import uk.org.whoami.authme.AuthMe;
import uk.org.whoami.authme.cache.backup.FileCache;
import uk.org.whoami.authme.events.ResetInventoryEvent;
import uk.org.whoami.authme.events.StoreInventoryEvent;
import uk.org.whoami.authme.settings.Settings;
import uk.org.whoami.authme.settings.Spawn;

public class LimboCache {
   private static LimboCache singleton = null;
   private HashMap cache;
   private FileCache playerData = new FileCache();
   public AuthMe plugin;

   private LimboCache(AuthMe plugin) {
      super();
      this.plugin = plugin;
      this.cache = new HashMap();
   }

   public void addLimboPlayer(Player player) {
      String name = player.getName().toLowerCase();
      Location loc = player.getLocation();
      int gameMode = player.getGameMode().getValue();
      String playerGroup = "";
      ItemStack[] arm;
      ItemStack[] inv;
      boolean operator;
      boolean flying;
      if (this.playerData.doesCacheExist(name)) {
         StoreInventoryEvent event = new StoreInventoryEvent(player, this.playerData);
         Bukkit.getServer().getPluginManager().callEvent(event);
         if (!event.isCancelled() && event.getInventory() != null && event.getArmor() != null) {
            inv = event.getInventory();
            arm = event.getArmor();
         } else {
            inv = (ItemStack[])null;
            arm = (ItemStack[])null;
         }

         playerGroup = this.playerData.readCache(name).getGroup();
         operator = this.playerData.readCache(name).getOperator();
         flying = this.playerData.readCache(name).isFlying();
      } else {
         StoreInventoryEvent event = new StoreInventoryEvent(player);
         Bukkit.getServer().getPluginManager().callEvent(event);
         if (!event.isCancelled() && event.getInventory() != null && event.getArmor() != null) {
            inv = event.getInventory();
            arm = event.getArmor();
         } else {
            inv = (ItemStack[])null;
            arm = (ItemStack[])null;
         }

         if (player.isOp()) {
            operator = true;
         } else {
            operator = false;
         }

         if (player.isFlying()) {
            flying = true;
         } else {
            flying = false;
         }
      }

      if (Settings.isForceSurvivalModeEnabled) {
         if (Settings.isResetInventoryIfCreative && gameMode != 0) {
            ResetInventoryEvent event = new ResetInventoryEvent(player);
            Bukkit.getServer().getPluginManager().callEvent(event);
            if (!event.isCancelled()) {
               player.sendMessage("Your inventory has been cleaned!");
            }
         }

         gameMode = 0;
      }

      if (player.isDead()) {
         loc = player.getWorld().getSpawnLocation();
         if (this.plugin.mv != null) {
            try {
               loc = this.plugin.mv.getMVWorldManager().getMVWorld(player.getWorld().getName()).getSpawnLocation();
            } catch (NullPointerException var12) {
            }
         }

         if (Spawn.getInstance().getLocation() != null) {
            loc = Spawn.getInstance().getLocation();
         }
      }

      try {
         if (this.cache.containsKey(name) && playerGroup.isEmpty()) {
            LimboPlayer groupLimbo = (LimboPlayer)this.cache.get(name);
            playerGroup = groupLimbo.getGroup();
         }
      } catch (NullPointerException var11) {
      }

      this.cache.put(player.getName().toLowerCase(), new LimboPlayer(name, loc, inv, arm, gameMode, operator, playerGroup, flying));
   }

   public void addLimboPlayer(Player player, String group) {
      this.cache.put(player.getName().toLowerCase(), new LimboPlayer(player.getName().toLowerCase(), group));
   }

   public void deleteLimboPlayer(String name) {
      this.cache.remove(name);
   }

   public LimboPlayer getLimboPlayer(String name) {
      return (LimboPlayer)this.cache.get(name);
   }

   public boolean hasLimboPlayer(String name) {
      return this.cache.containsKey(name);
   }

   public static LimboCache getInstance() {
      if (singleton == null) {
         singleton = new LimboCache(AuthMe.getInstance());
      }

      return singleton;
   }

   public void updateLimboPlayer(Player player) {
      if (this.hasLimboPlayer(player.getName().toLowerCase())) {
         this.deleteLimboPlayer(player.getName().toLowerCase());
      }

      this.addLimboPlayer(player);
   }
}
