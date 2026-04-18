package cus;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.HashMap;
import java.util.regex.Pattern;
import lib.config.ReloadConfigEvent;
import lib.hashList.HashList;
import lib.util.Util;
import lib.util.UtilConfig;
import lib.util.UtilFormat;
import lib.util.UtilItems;
import lib.util.UtilPer;
import lib.util.UtilPotions;
import org.bukkit.Server;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitScheduler;
import skill.Skill;
import skill.SkillManager;
import strength.StrengthManager;

public class Cus extends JavaPlugin implements Listener {
   private static Plugin plugin;
   private String pn;
   private Server server;
   private PluginManager pm;
   private BukkitScheduler scheduler;
   private String dataFolder;
   private String pluginVersion;
   private String pluginPath;
   private String potionsPath;
   private String itemsPath;
   private static Dao dao;
   private String per_cus_admin;
   private static HashMap numHash;
   private static CusManager cusManager;
   private static LevelManager levelManager;
   private static SkillManager skillManager;
   private static StrengthManager strengthManager;

   public Cus() {
      super();
   }

   public void onLoad() {
      plugin = this;
      CustomEntityUtil.init(this);
      CustomEntityType.registerEntities();
   }

   public void onEnable() {
      this.initBasic();
      this.initConfig();
      this.loadConfig0(UtilConfig.getConfig(this.pn));
      this.initDatabase();
      this.server.getPluginManager().registerEvents(this, this);
      Skill.init(this);
      cusManager = new CusManager(this);
      levelManager = new LevelManager(this);
      skillManager = new SkillManager(this);
      strengthManager = new StrengthManager(this);
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
         if (cmdName.equalsIgnoreCase("cus")) {
            if (length != 1 || !args[0].equalsIgnoreCase("?")) {
               if (length == 1) {
                  if (args[0].equalsIgnoreCase("reload")) {
                     this.reloadConfig(sender);
                     return true;
                  }

                  if (args[0].equalsIgnoreCase("create")) {
                     if (p == null) {
                        sender.sendMessage(UtilFormat.format(this.pn, "fail", new Object[]{this.get(40)}));
                     } else {
                        strengthManager.create(p);
                     }

                     return true;
                  }

                  if (args[0].equalsIgnoreCase("list")) {
                     strengthManager.showList(sender, 1);
                     return true;
                  }

                  if (args[0].equalsIgnoreCase("info")) {
                     if (p == null) {
                        sender.sendMessage(UtilFormat.format(this.pn, "fail", new Object[]{this.get(40)}));
                     } else {
                        strengthManager.info(p);
                     }

                     return true;
                  }

                  if (args[0].equalsIgnoreCase("sel")) {
                     if (p == null) {
                        sender.sendMessage(UtilFormat.format(this.pn, "fail", new Object[]{this.get(40)}));
                     } else {
                        strengthManager.showSel(p);
                     }

                     return true;
                  }
               } else if (length == 2) {
                  if (args[0].equalsIgnoreCase("show")) {
                     if (p == null) {
                        sender.sendMessage(UtilFormat.format(this.pn, "fail", new Object[]{this.get(40)}));
                     } else {
                        strengthManager.show(p, Integer.parseInt(args[1]));
                     }

                     return true;
                  }

                  if (args[0].equalsIgnoreCase("del")) {
                     strengthManager.del(sender, Long.parseLong(args[1]));
                     return true;
                  }

                  if (args[0].equalsIgnoreCase("list")) {
                     strengthManager.showList(sender, Integer.parseInt(args[1]));
                     return true;
                  }

                  if (args[0].equalsIgnoreCase("info")) {
                     strengthManager.info(sender, Long.parseLong(args[1]));
                     return true;
                  }

                  if (args[0].equalsIgnoreCase("tp")) {
                     if (p == null) {
                        sender.sendMessage(UtilFormat.format(this.pn, "fail", new Object[]{this.get(40)}));
                     } else {
                        strengthManager.tp(p, Long.parseLong(args[1]));
                     }

                     return true;
                  }

                  if (args[0].equalsIgnoreCase("clear")) {
                     strengthManager.clear(sender, Long.parseLong(args[1]));
                     return true;
                  }

                  if (args[0].equalsIgnoreCase("sel")) {
                     if (p == null) {
                        sender.sendMessage(UtilFormat.format(this.pn, "fail", new Object[]{this.get(40)}));
                     } else {
                        strengthManager.sel(p, args[1]);
                     }

                     return true;
                  }

                  if (args[0].equalsIgnoreCase("path")) {
                     if (p == null) {
                        sender.sendMessage(UtilFormat.format(this.pn, "fail", new Object[]{this.get(40)}));
                        return true;
                     }

                     if (args[1].equalsIgnoreCase("add")) {
                        strengthManager.addPath(p);
                        return true;
                     }

                     if (args[1].equalsIgnoreCase("clear")) {
                        strengthManager.clearPath(p);
                        return true;
                     }

                     if (args[1].equalsIgnoreCase("show")) {
                        strengthManager.showPath(p);
                        return true;
                     }
                  }
               } else if (length == 3) {
                  if (args[0].equalsIgnoreCase("tp")) {
                     if (p == null) {
                        sender.sendMessage(UtilFormat.format(this.pn, "fail", new Object[]{this.get(40)}));
                     } else {
                        strengthManager.tp(p, Long.parseLong(args[1]), Integer.parseInt(args[2]));
                     }

                     return true;
                  }
               } else if (length == 4 && args[0].equalsIgnoreCase("var")) {
                  strengthManager.setVar(sender, Long.parseLong(args[1]), args[2], args[3]);
                  return true;
               }
            }

            sender.sendMessage(UtilFormat.format(this.pn, "cmdHelpHeader", new Object[]{this.get(10)}));
            if (p == null || UtilPer.hasPer(p, this.per_cus_admin)) {
               sender.sendMessage(UtilFormat.format(this.pn, "cmdHelpItem", new Object[]{this.get(15), this.get(20)}));
               sender.sendMessage(UtilFormat.format(this.pn, "cmdHelpItem", new Object[]{this.get(100), this.get(105)}));
               sender.sendMessage(UtilFormat.format(this.pn, "cmdHelpItem", new Object[]{this.get(110), this.get(115)}));
               sender.sendMessage(UtilFormat.format(this.pn, "cmdHelpItem", new Object[]{this.get(120), this.get(125)}));
               sender.sendMessage(UtilFormat.format(this.pn, "cmdHelpItem", new Object[]{this.get(130), this.get(135)}));
               sender.sendMessage(UtilFormat.format(this.pn, "cmdHelpItem", new Object[]{this.get(140), this.get(145)}));
               sender.sendMessage(UtilFormat.format(this.pn, "cmdHelpItem", new Object[]{this.get(150), this.get(155)}));
               sender.sendMessage(UtilFormat.format(this.pn, "cmdHelpItem", new Object[]{this.get(250), this.get(255)}));
               sender.sendMessage(UtilFormat.format(this.pn, "cmdHelpItem", new Object[]{this.get(270), this.get(275)}));
               sender.sendMessage(UtilFormat.format(this.pn, "cmdHelpItem", new Object[]{this.get(285), this.get(290)}));
               sender.sendMessage(UtilFormat.format(this.pn, "cmdHelpItem", new Object[]{this.get(310), this.get(315)}));
               sender.sendMessage(UtilFormat.format(this.pn, "cmdHelpItem", new Object[]{this.get(320), this.get(325)}));
            }
         }
      } catch (NumberFormatException var8) {
         sender.sendMessage(UtilFormat.format(this.pn, "fail", new Object[]{this.get(55)}));
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

   public String getPluginPath() {
      return this.pluginPath;
   }

   public static Dao getDao() {
      return dao;
   }

   public static CusManager getCusManager() {
      return cusManager;
   }

   public static LevelManager getLevelManager() {
      return levelManager;
   }

   public static Plugin getPlugin() {
      return plugin;
   }

   public static HashMap getNumHash() {
      return numHash;
   }

   public static SkillManager getSkillManager() {
      return skillManager;
   }

   public static StrengthManager getStrengthManager() {
      return strengthManager;
   }

   public static void setSkillManager(SkillManager skillManager) {
      Cus.skillManager = skillManager;
   }

   private void reloadConfig(CommandSender sender) {
      Player p = null;
      if (sender instanceof Player) {
         p = (Player)sender;
      }

      if (p == null || UtilPer.checkPer(p, this.per_cus_admin)) {
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
      this.per_cus_admin = config.getString("per_cus_admin");
      numHash = new HashMap();

      for(String s : config.getStringList("num")) {
         int num = Integer.parseInt(s.split(" ")[0]);
         String show = s.split(" ")[1];
         numHash.put(num, show);
      }

      YamlConfiguration potionsConfig = new YamlConfiguration();

      try {
         potionsConfig.load(this.potionsPath);
         UtilPotions.reloadPotions(this.pn, potionsConfig);
      } catch (FileNotFoundException e) {
         e.printStackTrace();
      } catch (IOException e) {
         e.printStackTrace();
      } catch (InvalidConfigurationException e) {
         e.printStackTrace();
      }

      try {
         YamlConfiguration itemsConfig = new YamlConfiguration();
         itemsConfig.load(this.itemsPath);
         UtilItems.reloadItems(this.pn, itemsConfig);
      } catch (FileNotFoundException e) {
         e.printStackTrace();
      } catch (IOException e) {
         e.printStackTrace();
      } catch (InvalidConfigurationException e) {
         e.printStackTrace();
      }

   }

   private void initBasic() {
      this.server = this.getServer();
      this.pn = this.getName();
      this.pm = this.server.getPluginManager();
      this.scheduler = this.server.getScheduler();
      this.pluginPath = this.getFile().getParentFile().getAbsolutePath();
      this.potionsPath = this.pluginPath + File.separator + this.pn + File.separator + "potions.yml";
      this.itemsPath = this.pluginPath + File.separator + this.pn + File.separator + "items.yml";
      this.dataFolder = this.pluginPath + File.separator + this.pn;
      this.pluginVersion = Util.getPluginVersion(this.getFile());
   }

   private void initDatabase() {
      dao = new Dao(this);
   }

   private void initConfig() {
      HashList<Pattern> filter = UtilConfig.getDefaultFilter();
      filter.add(Pattern.compile("skills.yml"));
      filter.add(Pattern.compile("potions.yml"));
      filter.add(Pattern.compile("levels.yml"));
      filter.add(Pattern.compile("items.yml"));
      filter.add(Pattern.compile("strength.yml"));
      UtilConfig.register(new File(this.pluginPath + File.separator + this.pn + ".jar"), this.dataFolder, filter, this.pn);
      this.loadConfig((CommandSender)null);
   }

   private String get(int id) {
      return UtilFormat.format(this.pn, id);
   }
}
