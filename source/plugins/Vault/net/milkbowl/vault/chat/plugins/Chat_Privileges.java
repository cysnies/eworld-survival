package net.milkbowl.vault.chat.plugins;

import net.krinsoft.privileges.Privileges;
import net.milkbowl.vault.chat.Chat;
import net.milkbowl.vault.permission.Permission;
import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.server.PluginDisableEvent;
import org.bukkit.event.server.PluginEnableEvent;
import org.bukkit.plugin.Plugin;

public class Chat_Privileges extends Chat {
   private static final String FRIENDLY_NAME = "Privileges - Chat";
   private static final String PLUGIN_NAME = "Privileges";
   private static final String CHAT_PREFIX_KEY = "prefix";
   private static final String CHAT_SUFFIX_KEY = "suffix";
   private Privileges privs;
   private final Plugin plugin;

   public Chat_Privileges(Plugin plugin, Permission perms) {
      super(perms);
      this.plugin = plugin;
      Bukkit.getServer().getPluginManager().registerEvents(new PermissionServerListener(), plugin);
      if (this.privs == null) {
         Plugin privsPlugin = plugin.getServer().getPluginManager().getPlugin("Privileges");
         if (privsPlugin != null && privsPlugin.isEnabled()) {
            this.privs = (Privileges)privsPlugin;
            plugin.getLogger().info(String.format("[Chat] %s hooked.", "Privileges - Chat"));
         }
      }

   }

   public String getName() {
      return "Privileges - Chat";
   }

   public boolean isEnabled() {
      return this.privs != null && this.privs.isEnabled();
   }

   private String getPlayerOrGroupInfoString(String world, String player, String key, String defaultValue) {
      String value = this.getPlayerInfoString(world, player, key, (String)null);
      if (value != null) {
         return value;
      } else {
         value = this.getGroupInfoString(world, this.getPrimaryGroup(world, player), key, (String)null);
         return value != null ? value : defaultValue;
      }
   }

   private void worldCheck(String world) {
      if (world != null && !world.isEmpty()) {
         throw new UnsupportedOperationException("Privileges does not support multiple worlds for player/group metadata.");
      }
   }

   public String getPlayerPrefix(String world, String player) {
      return this.getPlayerOrGroupInfoString(world, player, "prefix", (String)null);
   }

   public void setPlayerPrefix(String world, String player, String prefix) {
      this.setPlayerInfoString(world, player, "prefix", prefix);
   }

   public String getPlayerSuffix(String world, String player) {
      return this.getPlayerOrGroupInfoString(world, player, "suffix", (String)null);
   }

   public void setPlayerSuffix(String world, String player, String suffix) {
      this.setPlayerInfoString(world, player, "suffix", suffix);
   }

   public String getGroupPrefix(String world, String group) {
      return this.getGroupInfoString(world, group, "prefix", (String)null);
   }

   public void setGroupPrefix(String world, String group, String prefix) {
      this.setGroupInfoString(world, group, "prefix", prefix);
   }

   public String getGroupSuffix(String world, String group) {
      return this.getGroupInfoString(world, group, "suffix", (String)null);
   }

   public void setGroupSuffix(String world, String group, String suffix) {
      this.setGroupInfoString(world, group, "suffix", suffix);
   }

   public int getPlayerInfoInteger(String world, String player, String node, int defaultValue) {
      return this.privs.getUserNode(player).getInt(node, defaultValue);
   }

   public void setPlayerInfoInteger(String world, String player, String node, int value) {
      this.worldCheck(world);
      this.privs.getUserNode(player).set(node, value);
   }

   public int getGroupInfoInteger(String world, String group, String node, int defaultValue) {
      return this.privs.getGroupNode(group).getInt(node, defaultValue);
   }

   public void setGroupInfoInteger(String world, String group, String node, int value) {
      this.worldCheck(world);
      this.privs.getGroupNode(group).set(node, value);
   }

   public double getPlayerInfoDouble(String world, String player, String node, double defaultValue) {
      return this.privs.getUserNode(player).getDouble(node, defaultValue);
   }

   public void setPlayerInfoDouble(String world, String player, String node, double value) {
      this.worldCheck(world);
      this.privs.getUserNode(player).set(node, value);
   }

   public double getGroupInfoDouble(String world, String group, String node, double defaultValue) {
      return this.privs.getGroupNode(group).getDouble(node, defaultValue);
   }

   public void setGroupInfoDouble(String world, String group, String node, double value) {
      this.worldCheck(world);
      this.privs.getGroupNode(group).set(node, value);
   }

   public boolean getPlayerInfoBoolean(String world, String player, String node, boolean defaultValue) {
      return this.privs.getUserNode(player).getBoolean(node, defaultValue);
   }

   public void setPlayerInfoBoolean(String world, String player, String node, boolean value) {
      this.worldCheck(world);
      this.privs.getUserNode(player).set(node, value);
   }

   public boolean getGroupInfoBoolean(String world, String group, String node, boolean defaultValue) {
      return this.privs.getGroupNode(group).getBoolean(node, defaultValue);
   }

   public void setGroupInfoBoolean(String world, String group, String node, boolean value) {
      this.worldCheck(world);
      this.privs.getGroupNode(group).set(node, value);
   }

   public String getPlayerInfoString(String world, String player, String node, String defaultValue) {
      return this.privs.getUserNode(player).getString(node, defaultValue);
   }

   public void setPlayerInfoString(String world, String player, String node, String value) {
      this.worldCheck(world);
      this.privs.getUserNode(player).set(node, value);
   }

   public String getGroupInfoString(String world, String group, String node, String defaultValue) {
      return this.privs.getGroupNode(group).getString(node, defaultValue);
   }

   public void setGroupInfoString(String world, String group, String node, String value) {
      this.worldCheck(world);
      this.privs.getGroupNode(group).set(node, value);
   }

   public class PermissionServerListener implements Listener {
      public PermissionServerListener() {
         super();
      }

      @EventHandler(
         priority = EventPriority.MONITOR
      )
      public void onPluginEnable(PluginEnableEvent event) {
         if (Chat_Privileges.this.privs == null) {
            Plugin permChat = event.getPlugin();
            if ("Privileges".equals(permChat.getDescription().getName()) && permChat.isEnabled()) {
               Chat_Privileges.this.privs = (Privileges)permChat;
               Chat_Privileges.this.plugin.getLogger().info(String.format("[Chat] %s hooked.", "Privileges - Chat"));
            }
         }

      }

      @EventHandler(
         priority = EventPriority.MONITOR
      )
      public void onPluginDisable(PluginDisableEvent event) {
         if (Chat_Privileges.this.privs != null && "Privileges".equals(event.getPlugin().getDescription().getName())) {
            Chat_Privileges.this.privs = null;
            Chat_Privileges.this.plugin.getLogger().info(String.format("[Chat] %s un-hooked.", "Privileges - Chat"));
         }

      }
   }
}
