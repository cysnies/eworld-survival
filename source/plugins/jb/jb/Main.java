package jb;

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

public class Main extends JavaPlugin implements Listener {
   private String pn;
   private Server server;
   private PluginManager pm;
   private BukkitScheduler scheduler;
   private String dataFolder;
   private String pluginVersion;
   private String pluginPath;
   private String per_jb_admin;
   private String per_jb_op;
   private static Jb jb;

   public Main() {
      super();
   }

   public void onEnable() {
      this.initBasic();
      this.initConfig();
      this.loadConfig0(UtilConfig.getConfig(this.pn));
      jb = new Jb(this);
      this.server.getPluginManager().registerEvents(this, this);
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
         if (cmdName.equalsIgnoreCase("jb")) {
            if (length != 1 || !args[0].equalsIgnoreCase("?")) {
               if (length == 1) {
                  if (args[0].equalsIgnoreCase("reload")) {
                     this.reloadConfig(sender);
                     return true;
                  }

                  if (args[0].equalsIgnoreCase("pos")) {
                     if (p == null) {
                        sender.sendMessage(UtilFormat.format(this.pn, "fail", new Object[]{this.get(45)}));
                     } else {
                        jb.pos(p);
                     }

                     return true;
                  }

                  if (args[0].equalsIgnoreCase("del")) {
                     if (p == null) {
                        sender.sendMessage(UtilFormat.format(this.pn, "fail", new Object[]{this.get(45)}));
                     } else {
                        jb.delete(p, true);
                     }

                     return true;
                  }

                  if (args[0].equalsIgnoreCase("info")) {
                     if (p == null) {
                        sender.sendMessage(UtilFormat.format(this.pn, "fail", new Object[]{this.get(45)}));
                     } else {
                        jb.info(p, p.getName());
                     }

                     return true;
                  }

                  if (args[0].equalsIgnoreCase("list")) {
                     if (p == null) {
                        sender.sendMessage(UtilFormat.format(this.pn, "fail", new Object[]{this.get(45)}));
                     } else {
                        jb.list(p);
                     }

                     return true;
                  }
               } else if (length == 2) {
                  if (args[0].equalsIgnoreCase("info")) {
                     if (p == null) {
                        sender.sendMessage(UtilFormat.format(this.pn, "fail", new Object[]{this.get(45)}));
                     } else {
                        jb.info(p, args[1]);
                     }

                     return true;
                  }

                  if (args[0].equalsIgnoreCase("end")) {
                     if (p == null) {
                        sender.sendMessage(UtilFormat.format(this.pn, "fail", new Object[]{this.get(45)}));
                     } else {
                        jb.end(p, args[1]);
                     }

                     return true;
                  }

                  if (args[0].equalsIgnoreCase("tp")) {
                     if (p == null) {
                        sender.sendMessage(UtilFormat.format(this.pn, "fail", new Object[]{this.get(45)}));
                     } else {
                        jb.tp(p, args[1]);
                     }

                     return true;
                  }
               }

               if (args.length >= 3 && args[0].equalsIgnoreCase("re")) {
                  String msg = Util.combine(args, " ", 2, length);
                  if (p == null) {
                     sender.sendMessage(UtilFormat.format(this.pn, "fail", new Object[]{this.get(45)}));
                  } else {
                     jb.re(p, args[1], msg);
                  }

                  return true;
               }

               if (args.length >= 2 && args[0].equalsIgnoreCase("add")) {
                  String msg = Util.combine(args, " ", 1, length);
                  if (p == null) {
                     sender.sendMessage(UtilFormat.format(this.pn, "fail", new Object[]{this.get(45)}));
                  } else {
                     jb.add(p, msg);
                  }

                  return true;
               }

               if (args.length >= 1) {
                  String msg = Util.combine(args, " ", 0, length);
                  if (p == null) {
                     sender.sendMessage(UtilFormat.format(this.pn, "fail", new Object[]{this.get(45)}));
                  } else {
                     jb.create(p, msg);
                  }

                  return true;
               }
            }

            sender.sendMessage(UtilFormat.format(this.pn, "cmdHelpHeader", new Object[]{this.get(10)}));
            if (p == null || UtilPer.hasPer(p, this.per_jb_admin)) {
               sender.sendMessage(UtilFormat.format(this.pn, "cmdHelpItem", new Object[]{this.get(15), this.get(20)}));
            }

            sender.sendMessage(this.get(145));
            sender.sendMessage(UtilFormat.format(this.pn, "cmdHelpItem", new Object[]{this.get(115), this.get(120)}));
            if (jb.getCost() > 0) {
               sender.sendMessage(UtilFormat.format(this.pn, "cost", new Object[]{jb.getCost()}));
            }

            sender.sendMessage(UtilFormat.format(this.pn, "cmdHelpItem", new Object[]{this.get(155), this.get(160)}));
            sender.sendMessage(UtilFormat.format(this.pn, "cmdHelpItem", new Object[]{this.get(55), this.get(60)}));
            sender.sendMessage(UtilFormat.format(this.pn, "cmdHelpItem", new Object[]{this.get(75), this.get(80)}));
            sender.sendMessage(UtilFormat.format(this.pn, "cmdHelpItem", new Object[]{this.get(95), this.get(100)}));
            if (p == null || UtilPer.hasPer(p, this.per_jb_op)) {
               sender.sendMessage(this.get(150));
               sender.sendMessage(UtilFormat.format(this.pn, "cmdHelpItem", new Object[]{this.get(165), this.get(170)}));
               sender.sendMessage(UtilFormat.format(this.pn, "cmdHelpItem", new Object[]{this.get(85), this.get(90)}));
               sender.sendMessage(UtilFormat.format(this.pn, "cmdHelpItem", new Object[]{this.get(125), this.get(130)}));
               sender.sendMessage(UtilFormat.format(this.pn, "cmdHelpItem", new Object[]{this.get(105), this.get(110)}));
            }
         }
      } catch (NumberFormatException var9) {
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

   public String getPluginPath() {
      return this.pluginPath;
   }

   public PluginManager getPm() {
      return this.pm;
   }

   public static Jb getJb() {
      return jb;
   }

   private void reloadConfig(CommandSender sender) {
      Player p = null;
      if (sender instanceof Player) {
         p = (Player)sender;
      }

      if (p == null || UtilPer.checkPer(p, this.per_jb_admin)) {
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
      this.per_jb_admin = config.getString("per_jb_admin");
      this.per_jb_op = config.getString("per_jb_op");
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

   private void initConfig() {
      HashList<Pattern> filter = UtilConfig.getDefaultFilter();
      UtilConfig.register(new File(this.pluginPath + File.separator + this.pn + ".jar"), this.dataFolder, filter, this.pn);
      this.loadConfig((CommandSender)null);
   }
}
