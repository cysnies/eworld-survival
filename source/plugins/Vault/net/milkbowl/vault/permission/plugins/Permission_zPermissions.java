package net.milkbowl.vault.permission.plugins;

import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import net.milkbowl.vault.permission.Permission;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.server.PluginDisableEvent;
import org.bukkit.event.server.PluginEnableEvent;
import org.bukkit.plugin.Plugin;
import org.tyrannyofheaven.bukkit.zPermissions.ZPermissionsService;

public class Permission_zPermissions extends Permission {
   private static final String PRIMARY_GROUP_TRACK_METADATA_KEY = "Vault.primary-group.track";
   private final String name = "zPermissions";
   private ZPermissionsService service;
   private boolean trackSupport;

   public Permission_zPermissions(Plugin plugin) {
      super();
      this.plugin = plugin;
      Bukkit.getServer().getPluginManager().registerEvents(new PermissionServerListener(), plugin);
      if (this.service == null) {
         this.service = (ZPermissionsService)plugin.getServer().getServicesManager().load(ZPermissionsService.class);
         if (this.service != null) {
            log.info(String.format("[%s][Permission] %s hooked.", plugin.getDescription().getName(), "zPermissions"));
            this.detectTrackMethods();
         }
      }

   }

   public String getName() {
      return "zPermissions";
   }

   public boolean isEnabled() {
      return this.service != null;
   }

   public boolean hasSuperPermsCompat() {
      return true;
   }

   public boolean playerHas(String world, String player, String permission) {
      Player p = Bukkit.getServer().getPlayer(player);
      if (p == null) {
         Map<String, Boolean> perms = this.service.getPlayerPermissions(world, (Set)null, player);
         Boolean value = (Boolean)perms.get(permission.toLowerCase());
         if (value != null) {
            return value;
         } else {
            org.bukkit.permissions.Permission perm = Bukkit.getPluginManager().getPermission(permission);
            if (perm != null) {
               OfflinePlayer op = Bukkit.getServer().getOfflinePlayer(player);
               return perm.getDefault().getValue(op != null ? op.isOp() : false);
            } else {
               return false;
            }
         }
      } else {
         return this.playerHas(p, permission);
      }
   }

   public boolean playerAdd(String world, String player, String permission) {
      if (world != null) {
         permission = world + ":" + permission;
      }

      return this.plugin.getServer().dispatchCommand(this.plugin.getServer().getConsoleSender(), "permissions player " + player + " set " + permission);
   }

   public boolean playerRemove(String world, String player, String permission) {
      if (world != null) {
         permission = world + ":" + permission;
      }

      return this.plugin.getServer().dispatchCommand(this.plugin.getServer().getConsoleSender(), "permissions player " + player + " unset " + permission);
   }

   public boolean groupHas(String world, String group, String permission) {
      Map<String, Boolean> perms = this.service.getGroupPermissions(world, (Set)null, group);
      Boolean value = (Boolean)perms.get(permission.toLowerCase());
      if (value != null) {
         return value;
      } else {
         org.bukkit.permissions.Permission perm = Bukkit.getPluginManager().getPermission(permission);
         return perm != null ? perm.getDefault().getValue(false) : false;
      }
   }

   public boolean groupAdd(String world, String group, String permission) {
      if (world != null) {
         permission = world + ":" + permission;
      }

      return this.plugin.getServer().dispatchCommand(this.plugin.getServer().getConsoleSender(), "permissions group " + group + " set " + permission);
   }

   public boolean groupRemove(String world, String group, String permission) {
      if (world != null) {
         permission = world + ":" + permission;
      }

      return this.plugin.getServer().dispatchCommand(this.plugin.getServer().getConsoleSender(), "permissions group " + group + " unset " + permission);
   }

   public boolean playerInGroup(String world, String player, String group) {
      for(String g : this.service.getPlayerGroups(player)) {
         if (g.equalsIgnoreCase(group)) {
            return true;
         }
      }

      return false;
   }

   public boolean playerAddGroup(String world, String player, String group) {
      return this.plugin.getServer().dispatchCommand(this.plugin.getServer().getConsoleSender(), "permissions group " + group + " add " + player);
   }

   public boolean playerRemoveGroup(String world, String player, String group) {
      return this.plugin.getServer().dispatchCommand(this.plugin.getServer().getConsoleSender(), "permissions group " + group + " remove " + player);
   }

   public String[] getPlayerGroups(String world, String player) {
      return (String[])this.service.getPlayerGroups(player).toArray(new String[0]);
   }

   public String getPrimaryGroup(String world, String player) {
      try {
         if (this.trackSupport) {
            String track = (String)this.service.getPlayerMetadata(player, "Vault.primary-group.track", String.class);
            if (track != null && !"".equals(track)) {
               List<String> groups = this.service.getTrackGroups(track);
               Collections.reverse(groups);
               Set<String> trackGroups = new LinkedHashSet(groups);
               trackGroups.retainAll(this.service.getPlayerAssignedGroups(player));
               if (!trackGroups.isEmpty()) {
                  return (String)trackGroups.iterator().next();
               }
            }
         }
      } catch (IllegalStateException var6) {
         log.warning("Bad property 'Vault.primary-group.track' for " + player + "; is it a string and does the track exist?");
      }

      List<String> groups = this.service.getPlayerAssignedGroups(player);
      return !groups.isEmpty() ? (String)groups.get(0) : null;
   }

   public String[] getGroups() {
      return (String[])this.service.getAllGroups().toArray(new String[0]);
   }

   public boolean hasGroupSupport() {
      return true;
   }

   private void detectTrackMethods() {
      try {
         this.service.getClass().getMethod("getTrackGroups", String.class);
         this.trackSupport = true;
      } catch (SecurityException var2) {
         this.trackSupport = false;
      } catch (NoSuchMethodException var3) {
         this.trackSupport = false;
      }

   }

   public class PermissionServerListener implements Listener {
      public PermissionServerListener() {
         super();
      }

      @EventHandler(
         priority = EventPriority.MONITOR
      )
      public void onPluginEnable(PluginEnableEvent event) {
         if (Permission_zPermissions.this.service == null && event.getPlugin().getDescription().getName().equals("zPermissions")) {
            Permission_zPermissions.this.service = (ZPermissionsService)Permission_zPermissions.this.plugin.getServer().getServicesManager().load(ZPermissionsService.class);
            if (Permission_zPermissions.this.service != null) {
               Permission_zPermissions.log.info(String.format("[%s][Permission] %s hooked.", Permission_zPermissions.this.plugin.getDescription().getName(), "zPermissions"));
               Permission_zPermissions.this.detectTrackMethods();
            }
         }

      }

      @EventHandler(
         priority = EventPriority.MONITOR
      )
      public void onPluginDisable(PluginDisableEvent event) {
         if (Permission_zPermissions.this.service != null && event.getPlugin().getDescription().getName().equals("zPermissions")) {
            Permission_zPermissions.this.service = null;
            Permission_zPermissions.log.info(String.format("[%s][Permission] %s un-hooked.", Permission_zPermissions.this.plugin.getDescription().getName(), "zPermissions"));
         }

      }
   }
}
