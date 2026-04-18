package level;

import java.io.File;
import java.util.regex.Pattern;
import lib.config.ReloadConfigEvent;
import lib.hashList.HashList;
import lib.util.Util;
import lib.util.UtilConfig;
import lib.util.UtilFormat;
import lib.util.UtilItems;
import lib.util.UtilPer;
import lib.util.UtilRewards;
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

public class Main extends JavaPlugin implements Listener {
   private Server server;
   private static String pn;
   private PluginManager pm;
   private BukkitScheduler scheduler;
   private String mainPath;
   private String pluginPath;
   private String dataFolder;
   private String pluginVersion;
   private static Dao dao;
   private static LevelManager levelManager;
   private static ShowManager showManager;
   private static String per_level_admin;

   public Main() {
      super();
   }

   public void onEnable() {
      this.initBasic();
      this.initConfig();
      this.loadConfig0(UtilConfig.getConfig(pn));
      this.initDatabase();
      Effect.init(this);
      levelManager = new LevelManager(this);
      showManager = new ShowManager(this);
      this.server.getPluginManager().registerEvents(this, this);
      Util.sendConsoleMessage(UtilFormat.format(pn, "pluginEnabled", new Object[]{pn, this.pluginVersion}));
   }

   public void onDisable() {
      if (this.scheduler != null) {
         this.scheduler.cancelAllTasks();
      }

      Util.sendConsoleMessage(UtilFormat.format(pn, "pluginDisabled", new Object[]{pn, this.pluginVersion}));
   }

   public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
      Player p = null;
      if (sender instanceof Player) {
         p = (Player)sender;
      }

      String cmdName = cmd.getName();
      int length = args.length;

      try {
         if (cmdName.equalsIgnoreCase("level")) {
            if (p != null && !UtilPer.checkPer(p, per_level_admin)) {
               return true;
            }

            if (length != 1 || !args[0].equalsIgnoreCase("?")) {
               if (length == 1) {
                  if (args[0].equalsIgnoreCase("reload")) {
                     this.reloadConfig(sender);
                     return true;
                  }
               } else if (length == 3) {
                  if (args[0].equalsIgnoreCase("add")) {
                     levelManager.addLevel(sender, args[1], Integer.parseInt(args[2]));
                     return true;
                  }

                  if (args[0].equalsIgnoreCase("del")) {
                     levelManager.delLevel(sender, args[1], Integer.parseInt(args[2]), true);
                     return true;
                  }
               } else if (length == 4 && args[0].equalsIgnoreCase("add")) {
                  levelManager.addLevel(sender, args[1], Integer.parseInt(args[2]), Integer.parseInt(args[3]), false);
                  return true;
               }
            }

            sender.sendMessage(UtilFormat.format(pn, "cmdHelpHeader", new Object[]{this.get(10)}));
            sender.sendMessage(UtilFormat.format(pn, "cmdHelpItem", new Object[]{this.get(15), this.get(20)}));
            sender.sendMessage(UtilFormat.format(pn, "cmdHelpItem", new Object[]{this.get(60), this.get(65)}));
            sender.sendMessage(UtilFormat.format(pn, "cmdHelpItem", new Object[]{this.get(70), this.get(75)}));
         }
      } catch (NumberFormatException var9) {
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

   public String getMainPath() {
      return this.mainPath;
   }

   public String getPluginPath() {
      return this.pluginPath;
   }

   public static Dao getDao() {
      return dao;
   }

   public static String getPn() {
      return pn;
   }

   public static LevelManager getLevelManager() {
      return levelManager;
   }

   public static ShowManager getShowManager() {
      return showManager;
   }

   public static String getPer_level_admin() {
      return per_level_admin;
   }

   private void loadConfig0(YamlConfiguration config) {
      per_level_admin = config.getString("per_level_admin");
      UtilItems.reloadItems(pn, config);
      UtilRewards.reloadRewards(pn, config);
   }

   private void initBasic() {
      this.server = this.getServer();
      pn = this.getName();
      this.pm = this.server.getPluginManager();
      this.scheduler = this.server.getScheduler();
      this.mainPath = System.getProperty("user.dir");
      this.pluginPath = this.getFile().getParentFile().getAbsolutePath();
      this.dataFolder = this.pluginPath + File.separator + pn;
      this.pluginVersion = Util.getPluginVersion(this.getFile());
   }

   private void initDatabase() {
      dao = new Dao(this);
   }

   private void initConfig() {
      HashList<Pattern> filter = UtilConfig.getDefaultFilter();
      UtilConfig.register(new File(this.pluginPath + File.separator + pn + ".jar"), this.dataFolder, filter, pn);
      this.loadConfig((CommandSender)null);
   }

   private void reloadConfig(CommandSender sender) {
      if (this.loadConfig(sender)) {
         sender.sendMessage(UtilFormat.format(pn, "success", new Object[]{this.get(25)}));
      } else {
         sender.sendMessage(UtilFormat.format(pn, "fail", new Object[]{this.get(30)}));
      }

   }

   private boolean loadConfig(CommandSender sender) {
      try {
         return UtilConfig.loadConfig(pn);
      } catch (InvalidConfigurationException e) {
         if (sender == null) {
            sender = this.server.getConsoleSender();
         }

         if (sender != null) {
            sender.sendMessage(e.getMessage());
         } else {
            this.server.getLogger().info(e.getMessage());
         }

         return false;
      }
   }

   private String get(int id) {
      return UtilFormat.format(pn, id);
   }
}
