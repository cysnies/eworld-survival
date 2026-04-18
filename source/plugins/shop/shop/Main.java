package shop;

import java.io.File;
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

public class Main extends JavaPlugin implements Listener {
   private String pn;
   private Server server;
   private PluginManager pm;
   private BukkitScheduler scheduler;
   private String dataFolder;
   private String pluginVersion;
   private String pluginPath;
   private Dao dao;
   private String per_shop_admin;
   private static ShopManager shopManager;
   private static ShowManager showManager;

   public Main() {
      super();
   }

   public void onEnable() {
      this.initBasic();
      this.initConfig();
      this.loadConfig0(UtilConfig.getConfig(this.pn));
      this.server.getPluginManager().registerEvents(this, this);
      this.initDatabase();
      shopManager = new ShopManager(this);
      showManager = new ShowManager(this);
      Util.sendConsoleMessage(UtilFormat.format(this.pn, "pluginEnabled", new Object[]{this.pn, this.pluginVersion}));
   }

   public void onDisable() {
      this.scheduler.cancelAllTasks();
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
         if (cmdName.equalsIgnoreCase("shop")) {
            if (length != 1 || !args[0].equalsIgnoreCase("?")) {
               if (length == 0) {
                  if (p == null) {
                     sender.sendMessage(UtilFormat.format(this.pn, "fail", new Object[]{this.get(120)}));
                  } else {
                     showManager.showMainMenu(p, 1);
                  }

                  return true;
               }

               if (length == 1) {
                  if (args[0].equalsIgnoreCase("reload")) {
                     this.reloadConfig(sender);
                     return true;
                  }

                  if (p == null) {
                     sender.sendMessage(UtilFormat.format(this.pn, "fail", new Object[]{this.get(120)}));
                  } else {
                     shopManager.sell(p, Integer.parseInt(args[0]), false);
                  }

                  return true;
               }

               if (length == 2) {
                  if (p == null) {
                     sender.sendMessage(UtilFormat.format(this.pn, "fail", new Object[]{this.get(120)}));
                  } else {
                     int price = Integer.parseInt(args[0]);
                     int confirmPrice = Integer.parseInt(args[1]);
                     if (price != confirmPrice) {
                        sender.sendMessage(UtilFormat.format(this.pn, "fail", new Object[]{this.get(125)}));
                        return true;
                     }

                     shopManager.sell(p, price, true);
                  }

                  return true;
               }
            }

            sender.sendMessage(UtilFormat.format(this.pn, "cmdHelpHeader", new Object[]{this.get(10)}));
            if (p == null || UtilPer.hasPer(p, this.per_shop_admin)) {
               sender.sendMessage(UtilFormat.format(this.pn, "cmdHelpItem", new Object[]{this.get(15), this.get(20)}));
               sender.sendMessage(UtilFormat.format(this.pn, "cmdHelpItem", new Object[]{this.get(100), this.get(105)}));
               sender.sendMessage(UtilFormat.format(this.pn, "cmdHelpItem", new Object[]{this.get(110), this.get(115)}));
            }
         }
      } catch (NumberFormatException var10) {
         sender.sendMessage(UtilFormat.format(this.pn, "fail", new Object[]{this.get(40)}));
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

   public String getPn() {
      return this.pn;
   }

   public PluginManager getPm() {
      return this.pm;
   }

   public Dao getDao() {
      return this.dao;
   }

   public static ShopManager getShopManager() {
      return shopManager;
   }

   public static ShowManager getShowManager() {
      return showManager;
   }

   public String getPluginPath() {
      return this.pluginPath;
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

   private void initConfig() {
      UtilConfig.register(new File(this.pluginPath + File.separator + this.pn + ".jar"), this.dataFolder, (HashList)null, this.pn);
      this.loadConfig((CommandSender)null);
   }

   private void reloadConfig(CommandSender sender) {
      Player p = null;
      if (sender instanceof Player) {
         p = (Player)sender;
      }

      if (p == null || UtilPer.checkPer(p, this.per_shop_admin)) {
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

   private void loadConfig0(YamlConfiguration config) {
      this.per_shop_admin = config.getString("per_shop_admin");
      UtilItems.reloadItems(this.pn, config);
   }

   private String get(int id) {
      return UtilFormat.format(this.pn, id);
   }
}
