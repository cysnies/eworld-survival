package smelt;

import java.io.File;
import java.util.regex.Pattern;
import lib.config.ReloadConfigEvent;
import lib.hashList.HashList;
import lib.types.InvalidTypeException;
import lib.util.Util;
import lib.util.UtilConfig;
import lib.util.UtilFormat;
import lib.util.UtilItems;
import lib.util.UtilPer;
import lib.util.UtilTypes;
import org.bukkit.Server;
import org.bukkit.block.Block;
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
   private static String pn;
   private Server server;
   private PluginManager pm;
   private BukkitScheduler scheduler;
   private String dataFolder;
   private String pluginVersion;
   private String pluginPath;
   private Smelt smelt;
   private Luck luck;
   private Repair repair;
   private Seal seal;
   private Star star;
   private Spec spec;
   private Jd jd;
   private Dig dig;
   private Protect protect;
   private Gem gem;
   private Light light;
   private Bind bind;
   private String per_smelt_admin;
   private static String ignore;

   public Main() {
      super();
   }

   public void onEnable() {
      this.initBasic();
      this.initConfig();
      this.loadConfig0(UtilConfig.getConfig(pn));
      this.server.getPluginManager().registerEvents(this, this);
      this.smelt = new Smelt(this);
      this.luck = new Luck(this);
      this.repair = new Repair(this);
      this.seal = new Seal(this);
      this.star = new Star(this);
      this.spec = new Spec(this);
      this.jd = new Jd(this);
      this.dig = new Dig(this);
      this.protect = new Protect(this);
      this.gem = new Gem(this);
      this.light = new Light(this);
      this.bind = new Bind(this);
      Util.sendConsoleMessage(UtilFormat.format(pn, "pluginEnabled", new Object[]{pn, this.pluginVersion}));
   }

   public void onDisable() {
      this.scheduler.cancelAllTasks();
      Util.sendConsoleMessage(UtilFormat.format(pn, "pluginDisabled", new Object[]{pn, this.pluginVersion}));
   }

   public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
      Player p = null;
      if (sender instanceof Player) {
         p = (Player)sender;
      }

      String cmdName = cmd.getName();
      int length = args.length;
      if (cmdName.equalsIgnoreCase("smelt")) {
         if ((length != 1 || !args[0].equalsIgnoreCase("?")) && length == 1) {
            if (args[0].equalsIgnoreCase("reload")) {
               this.reloadConfig(sender);
               return true;
            }

            if (args[0].equalsIgnoreCase("repair")) {
               if (p == null) {
                  sender.sendMessage(UtilFormat.format(pn, "fail", new Object[]{this.get(40)}));
               } else {
                  this.smelt.repair(p);
               }

               return true;
            }
         }

         sender.sendMessage(UtilFormat.format(pn, "cmdHelpHeader", new Object[]{this.get(10)}));
         if (p == null || UtilPer.hasPer(p, this.per_smelt_admin)) {
            sender.sendMessage(UtilFormat.format(pn, "cmdHelpItem", new Object[]{this.get(15), this.get(20)}));
         }

         sender.sendMessage(UtilFormat.format(pn, "cmdHelpItem", new Object[]{this.get(600), UtilFormat.format(pn, "tip1", new Object[]{this.smelt.getRepairCost()})}));
      }

      return true;
   }

   @EventHandler(
      priority = EventPriority.LOW
   )
   public void reloadConfig(ReloadConfigEvent e) {
      if (e.getCallPlugin().equals(pn)) {
         this.loadConfig0(e.getConfig());
      }

   }

   private void reloadConfig(CommandSender sender) {
      Player p = null;
      if (sender instanceof Player) {
         p = (Player)sender;
      }

      if (p != null && !UtilPer.hasPer(p, this.per_smelt_admin)) {
         p.sendMessage(UtilFormat.format(pn, "noPer", new Object[]{this.per_smelt_admin}));
      } else {
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
      this.per_smelt_admin = config.getString("per_smelt_admin");
      ignore = config.getString("ignore");
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
      UtilConfig.register(new File(this.pluginPath + File.separator + pn + ".jar"), this.dataFolder, filter, pn);
      this.loadConfig((CommandSender)null);
   }

   public static boolean isIgnored(Block b) {
      try {
         return UtilTypes.checkItem(pn, ignore, b.getType().name());
      } catch (InvalidTypeException var2) {
         return false;
      } catch (Exception var3) {
         return false;
      }
   }

   public String getPn() {
      return pn;
   }

   public PluginManager getPm() {
      return this.pm;
   }

   public Smelt getNeed() {
      return this.smelt;
   }

   public String getPluginPath() {
      return this.pluginPath;
   }

   public Repair getRepair() {
      return this.repair;
   }

   public Star getStar() {
      return this.star;
   }

   public Jd getJd() {
      return this.jd;
   }

   public Protect getProtect() {
      return this.protect;
   }

   public Gem getGem() {
      return this.gem;
   }

   public Dig getDig() {
      return this.dig;
   }

   public Luck getLuck() {
      return this.luck;
   }

   public Spec getSpec() {
      return this.spec;
   }

   public Light getLight() {
      return this.light;
   }

   public Seal getSeal() {
      return this.seal;
   }

   public Bind getBind() {
      return this.bind;
   }
}
