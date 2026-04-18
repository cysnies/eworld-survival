package net.milkbowl.vault.permission.plugins;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import net.ae97.totalpermissions.PermissionManager;
import net.ae97.totalpermissions.TotalPermissions;
import net.ae97.totalpermissions.permission.PermissionBase;
import net.ae97.totalpermissions.permission.PermissionUser;
import net.milkbowl.vault.permission.Permission;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.server.PluginDisableEvent;
import org.bukkit.event.server.PluginEnableEvent;
import org.bukkit.plugin.Plugin;

public class Permission_TotalPermissions extends Permission {
   private final String name = "TotalPermissions";
   private PermissionManager manager;
   private TotalPermissions totalperms;

   public Permission_TotalPermissions(Plugin pl) {
      super();
      this.plugin = pl;
   }

   public String getName() {
      return "TotalPermissions";
   }

   public boolean isEnabled() {
      return this.plugin != null && this.plugin.isEnabled() && this.totalperms != null && this.totalperms.isEnabled();
   }

   public boolean hasSuperPermsCompat() {
      return true;
   }

   public boolean hasGroupSupport() {
      return true;
   }

   public boolean playerHas(String world, String player, String permission) {
      PermissionBase user = this.manager.getUser(player);
      return user.has(permission, world);
   }

   public boolean playerAdd(String world, String player, String permission) {
      try {
         PermissionBase user = this.manager.getUser(player);
         user.addPerm(permission, world);
         return true;
      } catch (IOException ex) {
         this.plugin.getLogger().log(Level.SEVERE, String.format("[%s] An error occured while saving perms", this.totalperms.getDescription().getName()), ex);
         return false;
      }
   }

   public boolean playerRemove(String world, String player, String permission) {
      try {
         PermissionBase user = this.manager.getUser(player);
         user.remPerm(permission, world);
         return true;
      } catch (IOException ex) {
         this.plugin.getLogger().log(Level.SEVERE, String.format("[%s] An error occured while saving perms", this.totalperms.getDescription().getName()), ex);
         return false;
      }
   }

   public boolean groupHas(String world, String group, String permission) {
      PermissionBase permGroup = this.manager.getGroup(group);
      return permGroup.has(permission, world);
   }

   public boolean groupAdd(String world, String group, String permission) {
      try {
         PermissionBase permGroup = this.manager.getGroup(group);
         permGroup.addPerm(permission, world);
         return true;
      } catch (IOException ex) {
         this.plugin.getLogger().log(Level.SEVERE, String.format("[%s] An error occured while saving perms", this.totalperms.getDescription().getName()), ex);
         return false;
      }
   }

   public boolean groupRemove(String world, String group, String permission) {
      try {
         PermissionBase permGroup = this.manager.getGroup(group);
         permGroup.remPerm(permission, world);
         return true;
      } catch (IOException ex) {
         this.plugin.getLogger().log(Level.SEVERE, String.format("[%s] An error occured while saving perms", this.totalperms.getDescription().getName()), ex);
         return false;
      }
   }

   public boolean playerInGroup(String world, String player, String group) {
      PermissionUser user = this.manager.getUser(player);
      List<String> groups = user.getGroups(world);
      return groups.contains(group);
   }

   public boolean playerAddGroup(String world, String player, String group) {
      try {
         PermissionUser user = this.manager.getUser(player);
         user.addGroup(group, world);
         return true;
      } catch (IOException ex) {
         this.plugin.getLogger().log(Level.SEVERE, String.format("[%s] An error occured while saving perms", this.totalperms.getDescription().getName()), ex);
         return false;
      }
   }

   public boolean playerRemoveGroup(String world, String player, String group) {
      try {
         PermissionUser user = this.manager.getUser(player);
         user.remGroup(group, world);
         return true;
      } catch (IOException ex) {
         this.plugin.getLogger().log(Level.SEVERE, String.format("[%s] An error occured while saving perms", this.totalperms.getDescription().getName()), ex);
         return false;
      }
   }

   public String[] getPlayerGroups(String world, String player) {
      PermissionUser user = this.manager.getUser(player);
      List<String> groups = user.getGroups(world);
      if (groups == null) {
         groups = new ArrayList();
      }

      return (String[])groups.toArray(new String[groups.size()]);
   }

   public String getPrimaryGroup(String world, String player) {
      String[] groups = this.getPlayerGroups(world, player);
      return groups.length == 0 ? "" : groups[0];
   }

   public String[] getGroups() {
      return this.manager.getGroups();
   }

   public class PermissionServerListener implements Listener {
      public PermissionServerListener() {
         super();
      }

      @EventHandler(
         priority = EventPriority.MONITOR
      )
      public void onPluginEnable(PluginEnableEvent event) {
         if (Permission_TotalPermissions.this.manager == null || Permission_TotalPermissions.this.totalperms == null) {
            Plugin permPlugin = event.getPlugin();
            if (permPlugin.getDescription().getName().equals("TotalPermissions")) {
               Permission_TotalPermissions.this.totalperms = (TotalPermissions)permPlugin;
               Permission_TotalPermissions.this.manager = Permission_TotalPermissions.this.totalperms.getManager();
               Permission_TotalPermissions.log.info(String.format("[%s][Permission] %s hooked.", Permission_TotalPermissions.this.plugin.getDescription().getName(), "TotalPermissions"));
            }
         }

      }

      @EventHandler(
         priority = EventPriority.MONITOR
      )
      public void onPluginDisable(PluginDisableEvent event) {
         if (Permission_TotalPermissions.this.manager != null && event.getPlugin().getDescription().getName().equals("TotalPermissions")) {
            Permission_TotalPermissions.this.totalperms = null;
            Permission_TotalPermissions.this.manager = null;
            Permission_TotalPermissions.log.info(String.format("[%s][Permission] %s un-hooked.", Permission_TotalPermissions.this.plugin.getDescription().getName(), "TotalPermissions"));
         }

      }
   }
}
