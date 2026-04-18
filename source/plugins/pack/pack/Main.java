package pack;

import java.io.File;
import java.util.regex.Pattern;
import lib.config.ReloadConfigEvent;
import lib.hashList.HashList;
import lib.util.Util;
import lib.util.UtilConfig;
import lib.util.UtilFormat;
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
   private static Dao dao;
   private String per_pack_admin;
   private static Pack pack;

   public Main() {
      super();
   }

   public void onEnable() {
      this.initBasic();
      this.initConfig();
      this.loadConfig0(UtilConfig.getConfig(this.pn));
      this.initDatabase();
      pack = new Pack(this);
      this.server.getPluginManager().registerEvents(this, this);
      Util.sendConsoleMessage(UtilFormat.format(this.pn, "pluginEnabled", new Object[]{this.pn, this.pluginVersion}));
   }

   public void onDisable() {
      this.scheduler.cancelAllTasks();
      dao.close();
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
         if (cmdName.equalsIgnoreCase("pack")) {
            if ((length != 1 || !args[0].equalsIgnoreCase("?")) && length == 1) {
               if (args[0].equalsIgnoreCase("reload")) {
                  this.reloadConfig(sender);
                  return true;
               }

               if (p == null) {
                  sender.sendMessage(UtilFormat.format(this.pn, "fail", new Object[]{this.get(45)}));
               } else {
                  pack.open(p, Integer.parseInt(args[0]));
               }

               return true;
            }

            sender.sendMessage(UtilFormat.format(this.pn, "cmdHelpHeader", new Object[]{this.get(10)}));
            if (p == null || UtilPer.hasPer(p, this.per_pack_admin)) {
               sender.sendMessage(UtilFormat.format(this.pn, "cmdHelpItem", new Object[]{this.get(15), this.get(20)}));
            }

            sender.sendMessage(UtilFormat.format(this.pn, "cmdHelpItem", new Object[]{this.get(35), this.get(40)}));
            if (p != null) {
               p.sendMessage(UtilFormat.format(this.pn, "canUse", new Object[]{pack.getCanUse(p)}));
            }
         }
      } catch (NumberFormatException var8) {
         sender.sendMessage(UtilFormat.format(this.pn, "fail", new Object[]{this.get(50)}));
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

   public static Dao getDao() {
      return dao;
   }

   public String getPluginPath() {
      return this.pluginPath;
   }

   public PluginManager getPm() {
      return this.pm;
   }

   private void reloadConfig(CommandSender sender) {
      Player p = null;
      if (sender instanceof Player) {
         p = (Player)sender;
      }

      if (p == null || UtilPer.checkPer(p, this.per_pack_admin)) {
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
      this.per_pack_admin = config.getString("per_pack_admin");
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
      dao = new Dao(this);
   }

   private void initConfig() {
      HashList<Pattern> filter = UtilConfig.getDefaultFilter();
      UtilConfig.register(new File(this.pluginPath + File.separator + this.pn + ".jar"), this.dataFolder, filter, this.pn);
      this.loadConfig((CommandSender)null);
   }
}
