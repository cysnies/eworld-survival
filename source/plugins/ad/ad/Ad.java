package ad;

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

public class Ad extends JavaPlugin implements Listener {
   private static String pn;
   private Server server;
   private PluginManager pm;
   private BukkitScheduler scheduler;
   private String dataFolder;
   private String pluginVersion;
   private String pluginPath;
   private static Dao dao;
   private String per_ad_admin;
   private String per_ad_remove;
   private static ShowManager showManager;
   private static Bc bc;
   private static Chat chat;

   public Ad() {
      super();
   }

   public void onEnable() {
      this.initBasic();
      this.initConfig();
      this.loadConfig0(UtilConfig.getConfig(pn));
      this.initDatabase();
      this.server.getPluginManager().registerEvents(this, this);
      showManager = new ShowManager(this);
      bc = new Bc(this);
      chat = new Chat(this);
      Util.sendConsoleMessage(UtilFormat.format(pn, "pluginEnabled", new Object[]{pn, this.pluginVersion}));
   }

   public void onDisable() {
      this.scheduler.cancelAllTasks();
      dao.close();
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
         if (cmdName.equalsIgnoreCase("ad")) {
            if (length != 1 || !args[0].equalsIgnoreCase("?")) {
               if (length == 1) {
                  if (args[0].equalsIgnoreCase("reload")) {
                     this.reloadConfig(sender);
                     return true;
                  }

                  if (args[0].equalsIgnoreCase("list")) {
                     chat.showList(sender, 1);
                     return true;
                  }

                  if (args[0].equalsIgnoreCase("info")) {
                     if (p == null) {
                        sender.sendMessage(UtilFormat.format(getPn(), "fail", new Object[]{this.get(365)}));
                     } else {
                        chat.showInfo(p, p.getName());
                     }

                     return true;
                  }

                  if (args[0].equalsIgnoreCase("create")) {
                     if (p == null) {
                        sender.sendMessage(UtilFormat.format(getPn(), "fail", new Object[]{this.get(365)}));
                     } else {
                        chat.create(p);
                     }

                     return true;
                  }
               } else if (length == 2) {
                  if (args[0].equalsIgnoreCase("remove")) {
                     this.remove(sender, args[1]);
                     return true;
                  }

                  if (args[0].equalsIgnoreCase("list")) {
                     chat.showList(sender, Integer.parseInt(args[1]));
                     return true;
                  }

                  if (args[0].equalsIgnoreCase("info")) {
                     chat.showInfo(sender, args[1]);
                     return true;
                  }

                  if (args[0].equalsIgnoreCase("del")) {
                     chat.del(sender, args[1]);
                     return true;
                  }

                  if (args[0].equalsIgnoreCase("clear")) {
                     if (p == null) {
                        sender.sendMessage(UtilFormat.format(getPn(), "fail", new Object[]{this.get(365)}));
                     } else {
                        chat.clear(p, Integer.parseInt(args[1]));
                     }

                     return true;
                  }

                  if (args[0].equalsIgnoreCase("addGold")) {
                     if (p == null) {
                        sender.sendMessage(UtilFormat.format(getPn(), "fail", new Object[]{this.get(365)}));
                     } else {
                        chat.addGold(p, Integer.parseInt(args[1]));
                     }

                     return true;
                  }

                  if (args[0].equalsIgnoreCase("addTicket")) {
                     if (p == null) {
                        sender.sendMessage(UtilFormat.format(getPn(), "fail", new Object[]{this.get(365)}));
                     } else {
                        chat.addTicket(p, Integer.parseInt(args[1]));
                     }

                     return true;
                  }
               } else if (length >= 3 && args[0].equalsIgnoreCase("set")) {
                  if (p == null) {
                     sender.sendMessage(UtilFormat.format(getPn(), "fail", new Object[]{this.get(365)}));
                  } else {
                     String content = Util.combine(args, " ", 2, length);
                     chat.set(p, Integer.parseInt(args[1]), content);
                  }

                  return true;
               }
            }

            sender.sendMessage(UtilFormat.format(pn, "cmdHelpHeader", new Object[]{this.get(10)}));
            if (p == null || UtilPer.hasPer(p, this.per_ad_admin)) {
               sender.sendMessage(UtilFormat.format(pn, "cmdHelpItem", new Object[]{this.get(15), this.get(20)}));
            }

            if (p == null || UtilPer.hasPer(p, this.per_ad_remove)) {
               sender.sendMessage(UtilFormat.format(pn, "cmdHelpItem", new Object[]{this.get(120), this.get(125)}));
               sender.sendMessage(UtilFormat.format(pn, "cmdHelpItem", new Object[]{this.get(300), this.get(305)}));
            }

            sender.sendMessage(UtilFormat.format(pn, "cmdHelpItem", new Object[]{this.get(380), this.get(385)}));
            sender.sendMessage(UtilFormat.format(pn, "cmdHelpItem", new Object[]{this.get(310), this.get(315)}));
            sender.sendMessage(UtilFormat.format(pn, "cmdHelpItem", new Object[]{this.get(330), this.get(335)}));
            sender.sendMessage(UtilFormat.format(pn, "cmdHelpItem", new Object[]{this.get(320), UtilFormat.format(getPn(), "info1", new Object[]{chat.getGold()})}));
            sender.sendMessage(UtilFormat.format(pn, "cmdHelpItem", new Object[]{this.get(325), UtilFormat.format(getPn(), "info2", new Object[]{chat.getTicket()})}));
            sender.sendMessage(UtilFormat.format(pn, "cmdHelpItem", new Object[]{this.get(345), UtilFormat.format(getPn(), "info4", new Object[]{chat.getMaxLine()})}));
            sender.sendMessage(UtilFormat.format(pn, "cmdHelpItem", new Object[]{this.get(340), UtilFormat.format(getPn(), "info3", new Object[]{chat.getMaxLine()})}));
         } else if (cmdName.equalsIgnoreCase("bc")) {
            bc.onCommand(sender, cmd, label, args);
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

   public static String getPn() {
      return pn;
   }

   public static Dao getDao() {
      return dao;
   }

   public static Bc getBc() {
      return bc;
   }

   public static Chat getChat() {
      return chat;
   }

   public static ShowManager getShowManager() {
      return showManager;
   }

   public String getPluginPath() {
      return this.pluginPath;
   }

   private void initDatabase() {
      dao = new Dao(this);
   }

   private void reloadConfig(CommandSender sender) {
      Player p = null;
      if (sender instanceof Player) {
         p = (Player)sender;
      }

      if (p == null || UtilPer.checkPer(p, this.per_ad_admin)) {
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

   private void remove(CommandSender sender, String tar) {
      if (!(sender instanceof Player) || UtilPer.checkPer((Player)sender, this.per_ad_remove)) {
         if (tar != null) {
            String s = Util.getRealName(sender, tar);
            if (s != null) {
               tar = s;
            }

            showManager.removeAd(tar);
            sender.sendMessage(UtilFormat.format(pn, "del", new Object[]{tar}));
         }
      }
   }

   private void loadConfig0(YamlConfiguration config) {
      this.per_ad_admin = config.getString("per_ad_admin");
      this.per_ad_remove = config.getString("per_ad_remove");
      UtilItems.reloadItems(pn, config);
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
      filter.add(Pattern.compile("bc.yml"));
      UtilConfig.register(new File(this.pluginPath + File.separator + pn + ".jar"), this.dataFolder, filter, pn);
      this.loadConfig((CommandSender)null);
   }
}
