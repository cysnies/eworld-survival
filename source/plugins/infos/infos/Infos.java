package infos;

import basic.Basic;
import death.Death;
import friend.Main;
import house.House;
import java.io.File;
import java.util.regex.Pattern;
import lib.config.ReloadConfigEvent;
import lib.hashList.HashList;
import lib.hashList.HashListImpl;
import lib.util.Util;
import lib.util.UtilConfig;
import lib.util.UtilFormat;
import lib.util.UtilItems;
import lib.util.UtilPer;
import lib.util.UtilTypes;
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
import sit.Sit;

public class Infos extends JavaPlugin implements Listener {
   private static String pn;
   private Server server;
   private PluginManager pm;
   private BukkitScheduler scheduler;
   private String dataFolder;
   private String pluginVersion;
   private String pluginPath;
   private Dao dao;
   private House house;
   private Basic basic;
   private Death death;
   private Main friend;
   private Sit sit;
   private String per_infos_admin;
   private HashList safeList;
   private static PlayerInfoManager playerInfoManager;
   private ServerInfoManager serverInfoManager;
   private Shows shows;
   private static ShowManager showManager;
   private Up up;
   private Gift gift;
   private static Join join;

   public Infos() {
      super();
   }

   public void onEnable() {
      this.initBasic();
      this.initConfig();
      this.loadConfig0(UtilConfig.getConfig(pn));
      this.initDatabase();
      this.server.getPluginManager().registerEvents(this, this);
      this.house = (House)this.server.getPluginManager().getPlugin("house");
      this.basic = (Basic)this.server.getPluginManager().getPlugin("basic");
      this.death = (Death)this.server.getPluginManager().getPlugin("death");
      this.friend = (Main)this.server.getPluginManager().getPlugin("friend");
      this.sit = (Sit)this.server.getPluginManager().getPlugin("sit");
      playerInfoManager = new PlayerInfoManager(this);
      this.serverInfoManager = new ServerInfoManager(this);
      this.shows = new Shows(this);
      showManager = new ShowManager(this);
      this.up = new Up(this);
      this.gift = new Gift(this);
      join = new Join(this);
      Util.sendConsoleMessage(UtilFormat.format(pn, "pluginEnabled", new Object[]{pn, this.pluginVersion}));
   }

   public void onDisable() {
      playerInfoManager.disable();
      this.serverInfoManager.disable();
      this.scheduler.cancelAllTasks();
      this.dao.close();
      Util.sendConsoleMessage(UtilFormat.format(pn, "pluginDisabled", new Object[]{pn, this.pluginVersion}));
   }

   public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
      try {
         Player p = null;
         if (sender instanceof Player) {
            p = (Player)sender;
         }

         String cmdName = cmd.getName();
         int length = args.length;
         if (cmdName.equalsIgnoreCase("infos")) {
            if ((length != 1 || !args[0].equalsIgnoreCase("?")) && length == 1 && args[0].equalsIgnoreCase("reload")) {
               this.reloadConfig(sender);
               return true;
            }

            sender.sendMessage(UtilFormat.format(pn, "cmdHelpHeader", new Object[]{this.get(10)}));
            if (p == null || UtilPer.hasPer(p, this.per_infos_admin)) {
               sender.sendMessage(UtilFormat.format(pn, "cmdHelpItem", new Object[]{this.get(15), this.get(20)}));
            }
         } else if (cmdName.equalsIgnoreCase("cmd")) {
            if (p == null) {
               sender.sendMessage(UtilFormat.format(pn, "fail", new Object[]{this.get(125)}));
            } else if (length == 1) {
               showManager.showMainMenu(p, args[0]);
            } else {
               showManager.showMainMenu(p, p.getName());
            }
         } else if (cmdName.equalsIgnoreCase("gift")) {
            this.gift.onCommand(sender, args);
         } else if (cmdName.equalsIgnoreCase("bind")) {
            if (p == null) {
               sender.sendMessage(UtilFormat.format(pn, "fail", new Object[]{this.get(125)}));
            } else if (length == 2) {
               playerInfoManager.bind(p, args[0], args[1]);
            } else if (length == 3) {
               playerInfoManager.bindChange(p, args[0], args[1], args[2]);
            } else {
               p.sendMessage(this.get(505));
            }
         }
      } catch (NumberFormatException var8) {
         sender.sendMessage(UtilFormat.format(pn, "fail", new Object[]{this.get(35)}));
      }

      return true;
   }

   @EventHandler(
      priority = EventPriority.LOW
   )
   public void onReloadConfig(ReloadConfigEvent e) {
      if (e.getCallPlugin().equals(pn)) {
         this.loadConfig0(e.getConfig());
      }

   }

   public PluginManager getPm() {
      return this.pm;
   }

   public static String getPn() {
      return pn;
   }

   public static PlayerInfoManager getPlayerInfoManager() {
      return playerInfoManager;
   }

   public ServerInfoManager getServerInfoManager() {
      return this.serverInfoManager;
   }

   public Dao getDao() {
      return this.dao;
   }

   public static ShowManager getShowManager() {
      return showManager;
   }

   public House getHouse() {
      return this.house;
   }

   public Basic getBasic() {
      return this.basic;
   }

   public Death getDeath() {
      return this.death;
   }

   public Main getFriend() {
      return this.friend;
   }

   public Sit getSit() {
      return this.sit;
   }

   public Shows getShows() {
      return this.shows;
   }

   public Up getUp() {
      return this.up;
   }

   public Gift getGift() {
      return this.gift;
   }

   public static Join getJoin() {
      return join;
   }

   public String getPluginPath() {
      return this.pluginPath;
   }

   private void initDatabase() {
      this.dao = new Dao(this);
   }

   private void reloadConfig(CommandSender sender) {
      Player p = null;
      if (sender instanceof Player) {
         p = (Player)sender;
      }

      if (p == null || UtilPer.checkPer(p, this.per_infos_admin)) {
         this.loadConfig(sender);
      }
   }

   private void loadConfig(CommandSender sender) {
      try {
         if (UtilConfig.loadConfig(pn)) {
            if (sender != null) {
               sender.sendMessage(UtilFormat.format(pn, "success", new Object[]{this.get(25)}));
            }
         } else if (sender != null) {
            sender.sendMessage(UtilFormat.format(pn, "fail", new Object[]{this.get(30)}));
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
      return UtilFormat.format(pn, id);
   }

   private void loadConfig0(YamlConfiguration config) {
      this.per_infos_admin = config.getString("per_infos_admin");
      this.safeList = new HashListImpl();

      for(int i : config.getIntegerList("safeList")) {
         this.safeList.add(i);
      }

      UtilItems.reloadItems(pn, config);
      UtilTypes.reloadTypes(pn, config);
   }

   private void initBasic() {
      this.server = this.getServer();
      pn = this.getName();
      this.pm = this.server.getPluginManager();
      this.scheduler = this.server.getScheduler();
      this.pluginPath = this.getFile().getParentFile().getAbsolutePath();
      this.dataFolder = this.pluginPath + File.separator + pn;
      this.pluginVersion = Util.getPluginVersion(this.getFile());
   }

   private void initConfig() {
      HashList<Pattern> filter = UtilConfig.getDefaultFilter();
      filter.add(Pattern.compile("time.yml"));
      filter.add(Pattern.compile("xq.ini"));
      UtilConfig.register(new File(this.pluginPath + File.separator + pn + ".jar"), this.dataFolder, filter, pn);
      this.loadConfig((CommandSender)null);
   }
}
