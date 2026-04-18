package need;

import com.earth2me.essentials.Essentials;
import com.earth2me.essentials.User;
import com.earth2me.essentials.commands.WarpNotFoundException;
import java.io.File;
import java.util.regex.Pattern;
import lib.config.ReloadConfigEvent;
import lib.hashList.HashList;
import lib.util.Util;
import lib.util.UtilConfig;
import lib.util.UtilCosts;
import lib.util.UtilFormat;
import lib.util.UtilItems;
import lib.util.UtilPer;
import net.ess3.api.InvalidWorldException;
import org.bukkit.Location;
import org.bukkit.Server;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerTeleportEvent.TeleportCause;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitScheduler;

public class Main extends JavaPlugin implements Listener {
   private static final String LIB = "lib";
   private String pn;
   private Server server;
   private PluginManager pm;
   private BukkitScheduler scheduler;
   private String dataFolder;
   private String pluginVersion;
   private String pluginPath;
   private Essentials ess;
   private Need need;
   private Tpr tpr;
   private Sell sell;
   private Stone stone;
   private ShopFix shopFix;
   private String per_need_admin;

   public Main() {
      super();
   }

   public void onEnable() {
      this.initBasic();
      this.initConfig();
      this.loadConfig0(UtilConfig.getConfig(this.pn));
      this.server.getPluginManager().registerEvents(this, this);
      this.ess = (Essentials)this.pm.getPlugin("Essentials");
      this.need = new Need(this);
      this.tpr = new Tpr(this);
      this.sell = new Sell(this);
      this.stone = new Stone(this);
      this.shopFix = new ShopFix(this);
      Util.sendConsoleMessage(UtilFormat.format("lib", "pluginEnabled", new Object[]{this.pn, this.pluginVersion}));
   }

   public void onDisable() {
      this.scheduler.cancelAllTasks();
      Util.sendConsoleMessage(UtilFormat.format("lib", "pluginDisabled", new Object[]{this.pn, this.pluginVersion}));
   }

   public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
      Player p = null;
      if (sender instanceof Player) {
         p = (Player)sender;
      }

      String cmdName = cmd.getName();
      int length = args.length;
      if (cmdName.equalsIgnoreCase("need")) {
         if ((length != 1 || !args[0].equalsIgnoreCase("?")) && length == 1 && args[0].equalsIgnoreCase("reload")) {
            this.reloadConfig(sender);
            return true;
         }

         sender.sendMessage(UtilFormat.format("lib", "cmdHelpHeader", new Object[]{this.get(10)}));
         if (p == null || UtilPer.hasPer(p, this.per_need_admin)) {
            sender.sendMessage(UtilFormat.format("lib", "cmdHelpItem", new Object[]{this.get(15), this.get(20)}));
         }
      } else if (cmdName.equalsIgnoreCase("pay")) {
         this.need.onCommand(sender, cmd, label, args);
      } else if (cmdName.equalsIgnoreCase("tpr")) {
         this.tpr.onCommand(sender, cmd, label, args);
      } else if (cmdName.equalsIgnoreCase("sell")) {
         this.sell.onCommand(sender, cmd, label, args);
      } else if (cmdName.equalsIgnoreCase("stone")) {
         this.stone.onCommand(sender, args);
      } else if (cmdName.equalsIgnoreCase("see")) {
         this.shopFix.onCommand(sender, cmd, label, args);
      } else if (cmdName.equalsIgnoreCase("p")) {
         if (p != null && !UtilPer.checkPer(p, this.per_need_admin)) {
            return true;
         }

         if (length == 1) {
            this.backNow(args[0]);
            return true;
         }

         if (length == 2) {
            this.warpNow(args[0], args[1]);
            return true;
         }

         sender.sendMessage(UtilFormat.format(this.pn, "cmdHelpHeader", new Object[]{this.get(4500)}));
         sender.sendMessage(UtilFormat.format(this.pn, "cmdHelpItem", new Object[]{this.get(4505), this.get(4510)}));
         sender.sendMessage(UtilFormat.format(this.pn, "cmdHelpItem", new Object[]{this.get(4515), this.get(4520)}));
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

   public Need getNeed() {
      return this.need;
   }

   public Tpr getTpr() {
      return this.tpr;
   }

   public Stone getStone() {
      return this.stone;
   }

   public ShopFix getShopFix() {
      return this.shopFix;
   }

   private void warpNow(String name, String tar) {
      try {
         Location l = this.ess.getWarps().getWarp(tar);
         if (l != null) {
            User user = this.ess.getUserMap().getUser(name);
            user.getTeleport().now(l, false, TeleportCause.PLUGIN);
         }
      } catch (WarpNotFoundException var5) {
      } catch (InvalidWorldException var6) {
      } catch (Exception var7) {
      }

   }

   private void backNow(String name) {
      User um = this.ess.getUserMap().getUser(name);
      if (um != null) {
         try {
            um.getTeleport().back();
         } catch (Exception var4) {
         }
      }

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

      if (p == null || UtilPer.checkPer(p, this.per_need_admin)) {
         this.loadConfig(sender);
      }
   }

   private void loadConfig(CommandSender sender) {
      try {
         if (UtilConfig.loadConfig(this.pn)) {
            if (sender != null) {
               sender.sendMessage(UtilFormat.format("lib", "success", new Object[]{this.get(25)}));
            }
         } else if (sender != null) {
            sender.sendMessage(UtilFormat.format("lib", "fail", new Object[]{this.get(30)}));
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
      this.per_need_admin = config.getString("per_need_admin");
      UtilItems.reloadItems(this.pn, config);
      UtilCosts.reloadCosts(this.pn, config);
   }

   private String get(int id) {
      return UtilFormat.format(this.pn, id);
   }
}
