package chest;

import java.io.File;
import java.util.regex.Pattern;
import lib.config.ReloadConfigEvent;
import lib.hashList.HashList;
import lib.util.Util;
import lib.util.UtilConfig;
import lib.util.UtilEnchants;
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

public class Chest extends JavaPlugin implements Listener {
   private static String pn;
   private Server server;
   private PluginManager pm;
   private BukkitScheduler scheduler;
   private String dataFolder;
   private String pluginVersion;
   private String pluginPath;
   private Dao dao;
   private String per_chest_admin;
   private static ChestManager chestManager;

   public Chest() {
      super();
   }

   public void onEnable() {
      this.initBasic();
      this.initConfig();
      this.loadConfig0(UtilConfig.getConfig(pn));
      this.initDatabase();
      this.server.getPluginManager().registerEvents(this, this);
      chestManager = new ChestManager(this);
      Util.sendConsoleMessage(UtilFormat.format(pn, "pluginEnabled", new Object[]{pn, this.pluginVersion}));
   }

   public void onDisable() {
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
         if (cmdName.equalsIgnoreCase("chest") || cmdName.equalsIgnoreCase("ch")) {
            if (length != 1 || !args[0].equalsIgnoreCase("?")) {
               if (length == 1) {
                  if (args[0].equalsIgnoreCase("reload")) {
                     this.reloadConfig(sender);
                     return true;
                  }

                  if (args[0].equalsIgnoreCase("create")) {
                     if (p == null) {
                        sender.sendMessage(UtilFormat.format(pn, "fail", new Object[]{this.get(37)}));
                     } else {
                        chestManager.create(p);
                     }

                     return true;
                  }

                  if (args[0].equalsIgnoreCase("list")) {
                     chestManager.showList(sender, 1);
                     return true;
                  }

                  if (args[0].equalsIgnoreCase("info")) {
                     if (p == null) {
                        sender.sendMessage(UtilFormat.format(pn, "fail", new Object[]{this.get(37)}));
                     } else {
                        chestManager.info(p);
                     }

                     return true;
                  }

                  if (args[0].equalsIgnoreCase("refreshAll")) {
                     chestManager.refreshAll(sender);
                     return true;
                  }
               } else if (length == 2) {
                  if (args[0].equalsIgnoreCase("list")) {
                     chestManager.showList(sender, Integer.parseInt(args[1]));
                     return true;
                  }

                  if (args[0].equalsIgnoreCase("tp")) {
                     if (p == null) {
                        sender.sendMessage(UtilFormat.format(pn, "fail", new Object[]{this.get(37)}));
                     } else {
                        chestManager.tp(p, Long.parseLong(args[1]));
                     }

                     return true;
                  }

                  if (args[0].equalsIgnoreCase("del")) {
                     chestManager.del(sender, Long.parseLong(args[1]));
                     return true;
                  }

                  if (args[0].equalsIgnoreCase("refresh")) {
                     chestManager.refresh(sender, Long.parseLong(args[1]));
                     return true;
                  }

                  if (args[0].equalsIgnoreCase("show")) {
                     if (p == null) {
                        sender.sendMessage(UtilFormat.format(pn, "fail", new Object[]{this.get(37)}));
                     } else {
                        chestManager.show(p, Integer.parseInt(args[1]));
                     }

                     return true;
                  }

                  if (args[0].equalsIgnoreCase("info")) {
                     chestManager.info(sender, Long.parseLong(args[1]));
                     return true;
                  }

                  if (args[0].equalsIgnoreCase("copy")) {
                     if (p == null) {
                        sender.sendMessage(UtilFormat.format(pn, "fail", new Object[]{this.get(37)}));
                     } else {
                        chestManager.copy(p, args[1]);
                     }

                     return true;
                  }
               } else if (length == 4 && args[0].equalsIgnoreCase("var")) {
                  chestManager.setVar(sender, Long.parseLong(args[1]), args[2], args[3]);
                  return true;
               }
            }

            sender.sendMessage(UtilFormat.format(pn, "cmdHelpHeader", new Object[]{this.get(10)}));
            if (p == null || UtilPer.hasPer(p, this.per_chest_admin)) {
               sender.sendMessage(UtilFormat.format(pn, "cmdHelpItem", new Object[]{this.get(15), this.get(20)}));
               sender.sendMessage(UtilFormat.format(pn, "cmdHelpItem", new Object[]{this.get(40), this.get(45)}));
               sender.sendMessage(UtilFormat.format(pn, "cmdHelpItem", new Object[]{this.get(50), this.get(55)}));
               sender.sendMessage(UtilFormat.format(pn, "cmdHelpItem", new Object[]{this.get(60), this.get(65)}));
               sender.sendMessage(UtilFormat.format(pn, "cmdHelpItem", new Object[]{this.get(70), this.get(75)}));
               sender.sendMessage(UtilFormat.format(pn, "cmdHelpItem", new Object[]{this.get(80), this.get(85)}));
               sender.sendMessage(UtilFormat.format(pn, "cmdHelpItem", new Object[]{this.get(90), this.get(95)}));
               sender.sendMessage(UtilFormat.format(pn, "cmdHelpItem", new Object[]{this.get(100), this.get(105)}));
               sender.sendMessage(UtilFormat.format(pn, "cmdHelpItem", new Object[]{this.get(110), this.get(115)}));
               sender.sendMessage(UtilFormat.format(pn, "cmdHelpItem", new Object[]{this.get(120), this.get(125)}));
               sender.sendMessage(UtilFormat.format(pn, "cmdHelpItem", new Object[]{this.get(200), this.get(205)}));
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

   public Dao getDao() {
      return this.dao;
   }

   public String getPluginPath() {
      return this.pluginPath;
   }

   public static String getPn() {
      return pn;
   }

   public static ChestManager getChestManager() {
      return chestManager;
   }

   private void initDatabase() {
      this.dao = new Dao(this);
   }

   private void reloadConfig(CommandSender sender) {
      Player p = null;
      if (sender instanceof Player) {
         p = (Player)sender;
      }

      if (p == null || UtilPer.checkPer(p, this.per_chest_admin)) {
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
      this.per_chest_admin = config.getString("per_chest_admin");
      UtilItems.reloadItems(pn, config);
      UtilEnchants.reloadEnchants(pn, config);
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
      UtilConfig.register(new File(this.pluginPath + File.separator + pn + ".jar"), this.dataFolder, filter, pn);
      this.loadConfig((CommandSender)null);
   }
}
