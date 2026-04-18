package town;

import java.io.File;
import java.util.regex.Pattern;
import landMain.LandMain;
import level.LevelManager;
import lib.config.ReloadConfigEvent;
import lib.hashList.HashList;
import lib.util.Util;
import lib.util.UtilConfig;
import lib.util.UtilFormat;
import lib.util.UtilItems;
import lib.util.UtilPer;
import org.bukkit.Bukkit;
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
   private static TownManager townManager;
   private static ShowManager showManager;
   private static String per_town_admin;
   private LevelManager levelManager;

   public Main() {
      super();
   }

   public void onEnable() {
      this.levelManager = level.Main.getLevelManager();
      LandMain landPlugin = (LandMain)Bukkit.getPluginManager().getPlugin("land");
      if (!landPlugin.isEnabled()) {
         Bukkit.getPluginManager().enablePlugin(landPlugin);
      }

      this.initBasic();
      this.initConfig();
      this.loadConfig0(UtilConfig.getConfig(pn));
      this.initDatabase();
      townManager = new TownManager(this);
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
         if (cmdName.equalsIgnoreCase("town")) {
            if (length != 1 || !args[0].equalsIgnoreCase("?")) {
               if (length == 0) {
                  if (p == null) {
                     sender.sendMessage(UtilFormat.format(pn, "fail", new Object[]{this.get(47)}));
                  } else {
                     townManager.spawn(p);
                  }

                  return true;
               }

               if (length == 1) {
                  if (args[0].equalsIgnoreCase("reload")) {
                     this.reloadConfig(sender);
                     return true;
                  }

                  if (args[0].equalsIgnoreCase("rank")) {
                     townManager.showRank(sender, 1);
                     return true;
                  }
               } else if (length == 2 && args[0].equalsIgnoreCase("rank")) {
                  townManager.showRank(sender, Integer.parseInt(args[1]));
                  return true;
               }
            }

            sender.sendMessage(UtilFormat.format(pn, "cmdHelpHeader", new Object[]{this.get(10)}));
            if (p != null && !UtilPer.checkPer(p, per_town_admin)) {
               sender.sendMessage(UtilFormat.format(pn, "cmdHelpItem", new Object[]{this.get(15), this.get(20)}));
               sender.sendMessage(UtilFormat.format(pn, "cmdHelpItem", new Object[]{this.get(40), this.get(45)}));
               sender.sendMessage(UtilFormat.format(pn, "cmdHelpItem", new Object[]{this.get(305), this.get(310)}));
            }
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

   public static TownManager getTownManager() {
      return townManager;
   }

   public static ShowManager getShowManager() {
      return showManager;
   }

   public static String getPer_level_admin() {
      return per_town_admin;
   }

   private void loadConfig0(YamlConfiguration config) {
      per_town_admin = config.getString("per_town_admin");
      UtilItems.reloadItems(pn, config);
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
      filter.add(Pattern.compile("nowSize.yml"));
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

   public LevelManager getLevelManager() {
      return this.levelManager;
   }
}
