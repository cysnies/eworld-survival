package clear;

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
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitScheduler;

public class Main extends JavaPlugin implements Listener {
   private static final String LIB = "lib";
   private Server server;
   private String pn;
   private BukkitScheduler scheduler;
   private String mainPath;
   private String pluginPath;
   private String dataFolder;
   private String pluginVersion;
   private ServerManager serverManager;
   private Clear clear;
   private RedStone redStone;
   private Crop crop;
   private Liquid liquid;
   private String per_clear_admin;

   public Main() {
      super();
   }

   public void onEnable() {
      this.initBasic();
      this.initConfig();
      this.loadConfig0(UtilConfig.getConfig(this.pn));
      this.serverManager = new ServerManager(this);
      this.redStone = new RedStone(this);
      this.crop = new Crop(this);
      this.liquid = new Liquid(this);
      this.clear = new Clear(this);
      this.server.getPluginManager().registerEvents(this, this);
      Util.sendConsoleMessage(UtilFormat.format("lib", "pluginEnabled", new Object[]{this.pn, this.pluginVersion}));
   }

   public void onDisable() {
      if (this.scheduler != null) {
         this.scheduler.cancelAllTasks();
      }

      Util.sendConsoleMessage(UtilFormat.format("lib", "pluginDisabled", new Object[]{this.pn, this.pluginVersion}));
   }

   public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
      Player p = null;
      if (sender instanceof Player) {
         p = (Player)sender;
      }

      String cmdName = cmd.getName();
      int length = args.length;

      try {
         if (cmdName.equalsIgnoreCase("clear")) {
            if (length != 1 || !args[0].equalsIgnoreCase("?")) {
               if (length == 1) {
                  if (args[0].equalsIgnoreCase("reload")) {
                     this.reloadConfig(sender);
                     return true;
                  }

                  if (args[0].equalsIgnoreCase("info")) {
                     if (sender instanceof Player && !UtilPer.checkPer((Player)sender, this.per_clear_admin)) {
                        return true;
                     }

                     this.clear.info(sender);
                     return true;
                  }

                  if (args[0].equalsIgnoreCase("start")) {
                     if (sender instanceof Player && !UtilPer.checkPer((Player)sender, this.per_clear_admin)) {
                        return true;
                     }

                     this.clear.clear(true, -1);
                     return true;
                  }
               } else if (length == 2 && args[0].equalsIgnoreCase("start")) {
                  if (sender instanceof Player && !UtilPer.checkPer((Player)sender, this.per_clear_admin)) {
                     return true;
                  }

                  this.clear.clear(true, Integer.parseInt(args[1]));
                  return true;
               }
            }

            sender.sendMessage(UtilFormat.format("lib", "cmdHelpHeader", new Object[]{this.get(10)}));
            if (p == null || UtilPer.hasPer(p, this.per_clear_admin)) {
               sender.sendMessage(UtilFormat.format("lib", "cmdHelpItem", new Object[]{this.get(15), this.get(20)}));
               sender.sendMessage(UtilFormat.format("lib", "cmdHelpItem", new Object[]{this.get(1215), this.get(1220)}));
               sender.sendMessage(UtilFormat.format("lib", "cmdHelpItem", new Object[]{this.get(1225), this.get(1230)}));
            }
         }
      } catch (NumberFormatException var9) {
         sender.sendMessage(UtilFormat.format("lib", "fail", new Object[]{this.get(190)}));
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

   private void initBasic() {
      this.server = this.getServer();
      this.pn = this.getName();
      this.scheduler = this.server.getScheduler();
      this.mainPath = System.getProperty("user.dir");
      this.pluginPath = this.getFile().getParentFile().getAbsolutePath();
      this.dataFolder = this.pluginPath + File.separator + this.pn;
      this.pluginVersion = Util.getPluginVersion(this.getFile());
   }

   private void initConfig() {
      HashList<Pattern> filter = UtilConfig.getDefaultFilter();
      UtilConfig.register(new File(this.pluginPath + File.separator + this.pn + ".jar"), this.dataFolder, filter, this.pn);
      this.loadConfig((CommandSender)null);
   }

   private void reloadConfig(CommandSender sender) {
      Player p = null;
      if (sender instanceof Player) {
         p = (Player)sender;
      }

      if (p == null || UtilPer.checkPer(p, this.per_clear_admin)) {
         if (this.loadConfig(sender)) {
            sender.sendMessage(UtilFormat.format("lib", "success", new Object[]{this.get(25)}));
         } else {
            sender.sendMessage(UtilFormat.format("lib", "fail", new Object[]{this.get(30)}));
         }

      }
   }

   private boolean loadConfig(CommandSender sender) {
      try {
         return UtilConfig.loadConfig(this.pn);
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

   private void loadConfig0(YamlConfiguration config) {
      this.per_clear_admin = config.getString("per_clear_admin");
   }

   private String get(int id) {
      return UtilFormat.format(this.pn, id);
   }

   public String getPn() {
      return this.pn;
   }

   public ServerManager getServerManager() {
      return this.serverManager;
   }

   public RedStone getRedStone() {
      return this.redStone;
   }

   public Crop getCrop() {
      return this.crop;
   }

   public String getMainPath() {
      return this.mainPath;
   }

   public Liquid getLiquid() {
      return this.liquid;
   }
}
