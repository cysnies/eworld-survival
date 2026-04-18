package net.milkbowl.vault.permission.plugins;

import com.github.sebc722.xperms.core.Main;
import net.milkbowl.vault.permission.Permission;
import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.server.PluginDisableEvent;
import org.bukkit.event.server.PluginEnableEvent;
import org.bukkit.plugin.Plugin;

public class Permission_Xperms extends Permission {
   private final String name = "Xperms";
   private Main perms = null;

   public Permission_Xperms(Plugin plugin) {
      super();
      this.plugin = plugin;
      Bukkit.getServer().getPluginManager().registerEvents(new PermissionServerListener(this), plugin);
      if (this.perms == null) {
         Plugin perms = plugin.getServer().getPluginManager().getPlugin("Xperms");
         if (this.perms != null) {
            if (perms.isEnabled()) {
               try {
                  if (Double.valueOf(perms.getDescription().getVersion()) < 1.1) {
                     log.info(String.format("[%s] [Permission] %s Current version is not compatible with vault! Please Update!", plugin.getDescription().getName(), "Xperms"));
                  }
               } catch (NumberFormatException var4) {
                  log.info(String.format("[%s] [Permission] %s Current version is not compatibe with vault! Please Update!", plugin.getDescription().getName(), "Xperms"));
               }
            }

            this.perms = (Main)perms;
            log.info(String.format("[%s][Permission] %s hooked.", plugin.getDescription().getName(), "Xperms"));
         }
      }

   }

   public String getName() {
      return "Xperms";
   }

   public boolean isEnabled() {
      return this.perms.isEnabled();
   }

   public boolean hasSuperPermsCompat() {
      return true;
   }

   public boolean playerHas(String world, String player, String permission) {
      return this.perms.getXplayer().hasPerm(world, player, permission);
   }

   public boolean playerAdd(String world, String player, String permission) {
      return this.perms.getXplayer().addNode(world, player, permission);
   }

   public boolean playerRemove(String world, String player, String permission) {
      return this.perms.getXplayer().removeNode(world, player, permission);
   }

   public boolean groupHas(String world, String group, String permission) {
      return this.perms.getXgroup().hasPerm(group, permission);
   }

   public boolean groupAdd(String world, String group, String permission) {
      this.perms.getXgroup().addNode(group, permission);
      return true;
   }

   public boolean groupRemove(String world, String group, String permission) {
      return this.perms.getXgroup().removeNode(group, permission);
   }

   public boolean playerInGroup(String world, String player, String group) {
      String groupForWorld = this.perms.getXplayer().getGroupForWorld(player, world);
      return groupForWorld.equals(group);
   }

   public boolean playerAddGroup(String world, String player, String group) {
      return this.perms.getXplayer().setPlayerGroup(world, player, group);
   }

   public boolean playerRemoveGroup(String world, String player, String group) {
      return this.perms.getXplayer().setPlayerDefault(world, player);
   }

   public String[] getPlayerGroups(String world, String player) {
      return this.perms.getXplayer().getPlayerGroups(player);
   }

   public String getPrimaryGroup(String world, String player) {
      return this.perms.getXplayer().getGroupForWorld(player, world);
   }

   public String[] getGroups() {
      return this.perms.getXgroup().getGroups();
   }

   public boolean hasGroupSupport() {
      return true;
   }

   public class PermissionServerListener implements Listener {
      Permission_Xperms permission = null;

      public PermissionServerListener(Permission_Xperms permission) {
         super();
         this.permission = permission;
      }

      @EventHandler(
         priority = EventPriority.MONITOR
      )
      public void onPluginEnable(PluginEnableEvent event) {
         if (this.permission.perms == null) {
            Plugin perms = event.getPlugin();
            if (perms.getDescription().getName().equals("Xperms")) {
               try {
                  if (Double.valueOf(perms.getDescription().getVersion()) < 1.1) {
                     Permission_Xperms.log.info(String.format("[%s] [Permission] %s Current version is not compatible with vault! Please Update!", Permission_Xperms.this.plugin.getDescription().getName(), "Xperms"));
                  }
               } catch (NumberFormatException var4) {
                  Permission_Xperms.log.info(String.format("[%s] [Permission] %s Current version is not compatibe with vault! Please Update!", Permission_Xperms.this.plugin.getDescription().getName(), "Xperms"));
               }

               this.permission.perms = (Main)perms;
               Permission_Xperms.log.info(String.format("[%s][Permission] %s hooked.", Permission_Xperms.this.plugin.getDescription().getName(), "Xperms"));
            }
         }

      }

      @EventHandler(
         priority = EventPriority.MONITOR
      )
      public void onPluginDisable(PluginDisableEvent event) {
         if (this.permission.perms != null && event.getPlugin().getName().equals("Xperms")) {
            this.permission.perms = null;
            Permission_Xperms.log.info(String.format("[%s][Permission] %s un-hooked.", Permission_Xperms.this.plugin.getDescription().getName(), "Xperms"));
         }

      }
   }
}
