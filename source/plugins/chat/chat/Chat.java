package chat;

import friend.FriendManager;
import friend.Main;
import java.io.File;
import java.util.regex.Pattern;
import lib.config.ReloadConfigEvent;
import lib.hashList.HashList;
import lib.util.Util;
import lib.util.UtilConfig;
import lib.util.UtilFormat;
import lib.util.UtilItems;
import lib.util.UtilPer;
import org.bukkit.Server;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitScheduler;

public class Chat extends JavaPlugin implements Listener {
   private String pn;
   private Server server;
   private PluginManager pm;
   private BukkitScheduler scheduler;
   private String dataFolder;
   private String pluginVersion;
   private String pluginPath;
   private Dao dao;
   private String per_chat_admin;
   private FriendManager friendManager;
   private static ChatColor chatColor;
   private static BlackList blackList;
   private static ChatLimit chatLimit;
   private static Channel channel;
   private static ShowManager showManager;

   public Chat() {
      super();
   }

   public void onEnable() {
      this.initBasic();
      this.initConfig();
      this.loadConfig0(UtilConfig.getConfig(this.pn));
      this.server.getPluginManager().registerEvents(this, this);
      this.initDatabase();
      this.loadData();
      this.friendManager = ((Main)this.pm.getPlugin("friend")).getFriendManager();
      chatColor = new ChatColor(this);
      blackList = new BlackList(this);
      chatLimit = new ChatLimit(this);
      channel = new Channel(this);
      showManager = new ShowManager(this);
      Util.sendConsoleMessage(UtilFormat.format(this.pn, "pluginEnabled", new Object[]{this.pn, this.pluginVersion}));
   }

   public void onDisable() {
      this.scheduler.cancelAllTasks();
      this.dao.close();
      channel.onDisable();
      Util.sendConsoleMessage(UtilFormat.format(this.pn, "pluginDisabled", new Object[]{this.pn, this.pluginVersion}));
   }

   public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
      try {
         Player p = null;
         if (sender instanceof Player) {
            p = (Player)sender;
         }

         String cmdName = cmd.getName();
         int length = args.length;
         if (cmdName.equalsIgnoreCase("chat")) {
            if ((length != 1 || !args[0].equalsIgnoreCase("?")) && length == 1 && args[0].equalsIgnoreCase("reload")) {
               this.reloadConfig(sender);
               return true;
            }

            sender.sendMessage(UtilFormat.format(this.pn, "cmdHelpHeader", new Object[]{this.get(10)}));
            if (p == null || UtilPer.hasPer(p, this.per_chat_admin)) {
               sender.sendMessage(UtilFormat.format(this.pn, "cmdHelpItem", new Object[]{this.get(15), this.get(20)}));
            }
         } else if (cmdName.equals("1")) {
            if (p == null) {
               sender.sendMessage(UtilFormat.format(this.pn, "fail", new Object[]{this.get(65)}));
            } else {
               channel.toggleChannel(p, 1);
            }
         } else if (cmdName.equals("2")) {
            if (p == null) {
               sender.sendMessage(UtilFormat.format(this.pn, "fail", new Object[]{this.get(65)}));
            } else {
               channel.toggleChannel(p, 2);
            }
         } else if (cmdName.equals("3")) {
            if (p == null) {
               sender.sendMessage(UtilFormat.format(this.pn, "fail", new Object[]{this.get(65)}));
            } else {
               channel.toggleChannel(p, 3);
            }
         } else if (cmdName.equals("4")) {
            if (p == null) {
               sender.sendMessage(UtilFormat.format(this.pn, "fail", new Object[]{this.get(65)}));
            } else {
               channel.toggleChannel(p, 4);
            }
         } else if (cmdName.equals("5")) {
            if (p == null) {
               sender.sendMessage(UtilFormat.format(this.pn, "fail", new Object[]{this.get(65)}));
            } else {
               channel.toggleChannel(p, 5);
            }
         }
      } catch (NumberFormatException var8) {
         sender.sendMessage(UtilFormat.format(this.pn, "fail", new Object[]{this.get(35)}));
      }

      return true;
   }

   @EventHandler(
      priority = EventPriority.LOW
   )
   public void onReloadConfig(ReloadConfigEvent e) {
      if (e.getCallPlugin().equals(this.pn)) {
         this.loadConfig0(e.getConfig());
      }

   }

   public PluginManager getPm() {
      return this.pm;
   }

   public String getPn() {
      return this.pn;
   }

   public Dao getDao() {
      return this.dao;
   }

   public String getPluginPath() {
      return this.pluginPath;
   }

   public static ChatLimit getChatLimit() {
      return chatLimit;
   }

   public static Channel getChannel() {
      return channel;
   }

   public static BlackList getBlackList() {
      return blackList;
   }

   public FriendManager getFriendManager() {
      return this.friendManager;
   }

   public static ShowManager getShowManager() {
      return showManager;
   }

   public static ChatColor getChatColor() {
      return chatColor;
   }

   private void reloadConfig(CommandSender sender) {
      Player p = null;
      if (sender instanceof Player) {
         p = (Player)sender;
      }

      if (p == null || UtilPer.checkPer(p, this.per_chat_admin)) {
         this.loadConfig(sender);
      }
   }

   private void loadConfig(CommandSender sender) {
      try {
         if (UtilConfig.loadConfig(this.pn)) {
            if (sender != null) {
               sender.sendMessage(UtilFormat.format(this.pn, "success", new Object[]{this.get(25)}));
            }
         } else if (sender != null) {
            sender.sendMessage(UtilFormat.format(this.pn, "fail", new Object[]{this.get(30)}));
         }
      } catch (InvalidConfigurationException e) {
         if (sender == null) {
            Util.sendConsoleMessage(e.getMessage());
         } else {
            sender.sendMessage(e.getMessage());
         }
      }

   }

   private String get(int id) {
      return UtilFormat.format(this.pn, id);
   }

   private void loadConfig0(YamlConfiguration config) {
      this.per_chat_admin = config.getString("per_chat_admin");
      UtilItems.reloadItems(this.pn, config);
   }

   private void initBasic() {
      this.server = this.getServer();
      this.pn = this.getName();
      this.pm = this.server.getPluginManager();
      this.scheduler = this.server.getScheduler();
      this.pluginPath = this.getFile().getParentFile().getAbsolutePath();
      this.dataFolder = this.pluginPath + File.separator + this.pn;
      this.pluginVersion = Util.getPluginVersion(this.getFile());
   }

   private void initDatabase() {
      this.dao = new Dao(this);
   }

   private void loadData() {
   }

   private void initConfig() {
      HashList<Pattern> filter = UtilConfig.getDefaultFilter();
      UtilConfig.register(new File(this.pluginPath + File.separator + this.pn + ".jar"), this.dataFolder, filter, this.pn);
      this.loadConfig((CommandSender)null);
   }
}
