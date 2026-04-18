package net.milkbowl.vault.chat.plugins;

import java.util.logging.Logger;
import net.milkbowl.vault.chat.Chat;
import net.milkbowl.vault.permission.Permission;
import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.server.PluginDisableEvent;
import org.bukkit.event.server.PluginEnableEvent;
import org.bukkit.plugin.Plugin;
import org.tyrannyofheaven.bukkit.zPermissions.ZPermissionsService;

public class Chat_zPermissions extends Chat {
   private static final Logger log = Logger.getLogger("Minecraft");
   private final String name = "zPermissions";
   private final Plugin plugin;
   private ZPermissionsService service;

   public Chat_zPermissions(Plugin plugin, Permission perms) {
      super(perms);
      this.plugin = plugin;
      Bukkit.getServer().getPluginManager().registerEvents(new PermissionServerListener(), plugin);
      if (this.service == null) {
         this.service = (ZPermissionsService)plugin.getServer().getServicesManager().load(ZPermissionsService.class);
         if (this.service != null) {
            log.info(String.format("[%s][Chat] %s hooked.", plugin.getDescription().getName(), "zPermissions"));
         }
      }

   }

   public String getName() {
      return "zPermissions";
   }

   public boolean isEnabled() {
      return this.service != null;
   }

   public String getPlayerPrefix(String world, String player) {
      return this.getPlayerInfoString(world, player, "prefix", "");
   }

   public void setPlayerPrefix(String world, String player, String prefix) {
      this.setPlayerInfoString(world, player, "prefix", prefix);
   }

   public String getPlayerSuffix(String world, String player) {
      return this.getPlayerInfoString(world, player, "suffix", "");
   }

   public void setPlayerSuffix(String world, String player, String suffix) {
      this.setPlayerInfoString(world, player, "suffix", suffix);
   }

   public String getGroupPrefix(String world, String group) {
      return this.getGroupInfoString(world, group, "prefix", "");
   }

   public void setGroupPrefix(String world, String group, String prefix) {
      this.setGroupInfoString(world, group, "prefix", prefix);
   }

   public String getGroupSuffix(String world, String group) {
      return this.getGroupInfoString(world, group, "suffix", "");
   }

   public void setGroupSuffix(String world, String group, String suffix) {
      this.setGroupInfoString(world, group, "suffix", suffix);
   }

   public int getPlayerInfoInteger(String world, String player, String node, int defaultValue) {
      Integer result = (Integer)this.service.getPlayerMetadata(player, node, Integer.class);
      return result == null ? defaultValue : result;
   }

   public void setPlayerInfoInteger(String world, String player, String node, int value) {
      this.plugin.getServer().dispatchCommand(this.plugin.getServer().getConsoleSender(), "permissions player " + player + " metadata setint " + node + " " + value);
   }

   public int getGroupInfoInteger(String world, String group, String node, int defaultValue) {
      Integer result = (Integer)this.service.getGroupMetadata(group, node, Integer.class);
      return result == null ? defaultValue : result;
   }

   public void setGroupInfoInteger(String world, String group, String node, int value) {
      this.plugin.getServer().dispatchCommand(this.plugin.getServer().getConsoleSender(), "permissions group " + group + " metadata setint " + node + " " + value);
   }

   public double getPlayerInfoDouble(String world, String player, String node, double defaultValue) {
      Double result = (Double)this.service.getPlayerMetadata(player, node, Double.class);
      return result == null ? defaultValue : result;
   }

   public void setPlayerInfoDouble(String world, String player, String node, double value) {
      this.plugin.getServer().dispatchCommand(this.plugin.getServer().getConsoleSender(), "permissions player " + player + " metadata setreal " + node + " " + value);
   }

   public double getGroupInfoDouble(String world, String group, String node, double defaultValue) {
      Double result = (Double)this.service.getGroupMetadata(group, node, Double.class);
      return result == null ? defaultValue : result;
   }

   public void setGroupInfoDouble(String world, String group, String node, double value) {
      this.plugin.getServer().dispatchCommand(this.plugin.getServer().getConsoleSender(), "permissions group " + group + " metadata setreal " + node + " " + value);
   }

   public boolean getPlayerInfoBoolean(String world, String player, String node, boolean defaultValue) {
      Boolean result = (Boolean)this.service.getPlayerMetadata(player, node, Boolean.class);
      return result == null ? defaultValue : result;
   }

   public void setPlayerInfoBoolean(String world, String player, String node, boolean value) {
      this.plugin.getServer().dispatchCommand(this.plugin.getServer().getConsoleSender(), "permissions player " + player + " metadata setbool " + node + " " + value);
   }

   public boolean getGroupInfoBoolean(String world, String group, String node, boolean defaultValue) {
      Boolean result = (Boolean)this.service.getGroupMetadata(group, node, Boolean.class);
      return result == null ? defaultValue : result;
   }

   public void setGroupInfoBoolean(String world, String group, String node, boolean value) {
      this.plugin.getServer().dispatchCommand(this.plugin.getServer().getConsoleSender(), "permissions group " + group + " metadata setbool " + node + " " + value);
   }

   public String getPlayerInfoString(String world, String player, String node, String defaultValue) {
      String result = (String)this.service.getPlayerMetadata(player, node, String.class);
      return result == null ? defaultValue : result;
   }

   public void setPlayerInfoString(String world, String player, String node, String value) {
      this.plugin.getServer().dispatchCommand(this.plugin.getServer().getConsoleSender(), "permissions player " + player + " metadata set " + node + " " + this.quote(value));
   }

   public String getGroupInfoString(String world, String group, String node, String defaultValue) {
      String result = (String)this.service.getGroupMetadata(group, node, String.class);
      return result == null ? defaultValue : result;
   }

   public void setGroupInfoString(String world, String group, String node, String value) {
      this.plugin.getServer().dispatchCommand(this.plugin.getServer().getConsoleSender(), "permissions group " + group + " metadata set " + node + " " + this.quote(value));
   }

   private String quote(String input) {
      input = input.replaceAll("\\\\", "\\\\\\\\").replaceAll("\"", "\\\\\"");
      return input.matches(".*\\s.*") ? "\"" + input + "\"" : input;
   }

   public class PermissionServerListener implements Listener {
      public PermissionServerListener() {
         super();
      }

      @EventHandler(
         priority = EventPriority.MONITOR
      )
      public void onPluginEnable(PluginEnableEvent event) {
         if (Chat_zPermissions.this.service == null && event.getPlugin().getDescription().getName().equals("zPermissions")) {
            Chat_zPermissions.this.service = (ZPermissionsService)Chat_zPermissions.this.plugin.getServer().getServicesManager().load(ZPermissionsService.class);
            if (Chat_zPermissions.this.service != null) {
               Chat_zPermissions.log.info(String.format("[%s][Chat] %s hooked.", Chat_zPermissions.this.plugin.getDescription().getName(), "zPermissions"));
            }
         }

      }

      @EventHandler(
         priority = EventPriority.MONITOR
      )
      public void onPluginDisable(PluginDisableEvent event) {
         if (Chat_zPermissions.this.service != null && event.getPlugin().getDescription().getName().equals("zPermissions")) {
            Chat_zPermissions.this.service = null;
            Chat_zPermissions.log.info(String.format("[%s][Chat] %s un-hooked.", Chat_zPermissions.this.plugin.getDescription().getName(), "zPermissions"));
         }

      }
   }
}
