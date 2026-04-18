package fix;

import java.io.File;
import java.util.Random;
import java.util.regex.Pattern;
import lib.config.ReloadConfigEvent;
import lib.hashList.HashList;
import lib.hashList.HashListImpl;
import lib.util.Util;
import lib.util.UtilConfig;
import lib.util.UtilCosts;
import lib.util.UtilEco;
import lib.util.UtilFormat;
import lib.util.UtilItems;
import lib.util.UtilPer;
import lib.util.UtilTypes;
import org.bukkit.Bukkit;
import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.Server;
import org.bukkit.World;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Villager;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitScheduler;
import ticket.Ticket;

public class Fix extends JavaPlugin implements Listener {
   private static final String LIB = "lib";
   private static final Random r = new Random();
   private Ticket ticket;
   private String pn;
   private Server server;
   private PluginManager pm;
   private BukkitScheduler scheduler;
   private String dataFolder;
   private String pluginVersion;
   private String pluginPath;
   private Fixs fixs;
   private Enchant enchant;
   private UseQuicker useQuicker;
   private Night night;
   private Season season;
   private Spawner spawner;
   private Draw draw;
   private Fish fish;
   private int buy1cost;
   private int buy1chance;
   private int buy1target;
   private int buy1rewards;
   private int buy2cost;
   private int buy2chance;
   private int buy2target;
   private int buy2rewards;
   private String per_fix_admin;
   private String per_fix_craft;
   private HashList fixBugList = new HashListImpl();

   public Fix() {
      super();
   }

   public void onEnable() {
      this.initBasic();
      this.initConfig();
      this.loadConfig0(UtilConfig.getConfig(this.pn));
      this.server.getPluginManager().registerEvents(this, this);
      this.fixs = new Fixs(this);
      this.enchant = new Enchant(this);
      this.useQuicker = new UseQuicker(this);
      this.night = new Night(this);
      this.season = new Season(this);
      this.spawner = new Spawner(this);
      this.draw = new Draw(this);
      this.fish = new Fish(this);
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
      if (cmdName.equalsIgnoreCase("fix")) {
         if (length != 1 || !args[0].equalsIgnoreCase("?")) {
            if (length == 1) {
               if (args[0].equalsIgnoreCase("check")) {
                  if (p != null && !p.isOp()) {
                     return true;
                  }

                  for(World w : this.server.getWorlds()) {
                     for(Entity e : w.getEntities()) {
                        if (e instanceof LivingEntity) {
                           LivingEntity le = (LivingEntity)e;
                           if (le.getCustomName() != null && le.getCustomName().length() > 64) {
                              le.setCustomName(le.getCustomName().substring(0, 64));
                           }
                        }

                        if (e instanceof Villager) {
                           Villager villager = (Villager)e;
                           if (villager.isValid()) {
                              int sum = 0;

                              for(Entity ee : villager.getNearbyEntities((double)10.0F, (double)10.0F, (double)10.0F)) {
                                 if (ee instanceof Villager && ee.isValid()) {
                                    ++sum;
                                    if (sum >= 8) {
                                       ee.remove();
                                    }
                                 }
                              }
                           }
                        }
                     }
                  }

                  sender.sendMessage("§4检测去除多余村民及名字过长实体结束!");
                  return true;
               }

               if (args[0].equalsIgnoreCase("reload")) {
                  this.reloadConfig(sender);
                  return true;
               }

               if (args[0].equalsIgnoreCase("checkFaraway")) {
                  if (p != null && !UtilPer.checkPer(p, this.per_fix_admin)) {
                     return true;
                  }

                  this.fixs.checkFaraway();
                  sender.sendMessage(UtilFormat.format("lib", "success", new Object[]{this.get(55)}));
                  return true;
               }
            } else if (length == 2 && args[0].equalsIgnoreCase("fixBug")) {
               if (p != null && !UtilPer.checkPer(p, this.per_fix_admin)) {
                  return true;
               }

               String tar = args[1];
               tar = Util.getRealName(sender, tar);
               if (tar != null) {
                  this.fixBugList.add(tar);
                  sender.sendMessage("添加修复玩家'" + tar + "'bug成功!");
                  return true;
               }
            }
         }

         sender.sendMessage(UtilFormat.format("lib", "cmdHelpHeader", new Object[]{this.get(10)}));
         if (p == null || UtilPer.hasPer(p, this.per_fix_admin)) {
            sender.sendMessage(UtilFormat.format("lib", "cmdHelpItem", new Object[]{this.get(15), this.get(20)}));
            sender.sendMessage(UtilFormat.format("lib", "cmdHelpItem", new Object[]{this.get(45), this.get(50)}));
         }
      } else if (cmdName.equalsIgnoreCase("c")) {
         if (p == null) {
            sender.sendMessage(UtilFormat.format(this.pn, "fail", new Object[]{this.get(40)}));
         } else {
            if (!UtilPer.hasPer(p, this.per_fix_craft)) {
               p.sendMessage(this.get(655));
               return true;
            }

            p.closeInventory();
            p.openWorkbench((Location)null, true);
         }
      } else if (cmdName.equalsIgnoreCase("buy") && length == 1) {
         try {
            int id = Integer.parseInt(args[0]);
            if (p == null) {
               sender.sendMessage(this.get(40));
            } else {
               this.buy(p, id);
            }
         } catch (NumberFormatException var16) {
            sender.sendMessage(this.get(1000));
            return true;
         }
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

   @EventHandler(
      priority = EventPriority.LOWEST
   )
   public void onPlayerJoin(PlayerJoinEvent e) {
      if (this.fixBugList.has(e.getPlayer().getName())) {
         int x = e.getPlayer().getLocation().getBlockX() >> 4;
         int z = e.getPlayer().getLocation().getBlockZ() >> 4;

         for(int xx = x - 4; xx < x + 4; ++xx) {
            for(int zz = z - 4; zz < z + 4; ++zz) {
               Chunk c = e.getPlayer().getWorld().getChunkAt(xx, zz);
               if (c != null && c.load(true)) {
                  Entity[] var10;
                  for(Entity ee : var10 = c.getEntities()) {
                     if (ee instanceof LivingEntity) {
                        LivingEntity le = (LivingEntity)ee;
                        if (le.getCustomName() != null && le.getCustomName().length() > 64) {
                           le.setCustomName(le.getCustomName().substring(0, 64));
                        }
                     }
                  }
               }
            }
         }

         e.getPlayer().teleport(Bukkit.getServer().getWorld("world").getSpawnLocation());
         this.fixBugList.remove(e.getPlayer().getName());
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

   public UseQuicker getUseQuicker() {
      return this.useQuicker;
   }

   public Enchant getEnchant() {
      return this.enchant;
   }

   public Season getSeason() {
      return this.season;
   }

   public Fixs getFixs() {
      return this.fixs;
   }

   public Night getNight() {
      return this.night;
   }

   public Spawner getSpawner() {
      return this.spawner;
   }

   public Draw getDraw() {
      return this.draw;
   }

   public Fish getFish() {
      return this.fish;
   }

   private void buy(Player p, int id) {
      this.checkInitTicket();
      if (id == 1) {
         if (UtilEco.get(p.getName()) < (double)this.buy1cost) {
            p.sendMessage(UtilFormat.format(this.pn, "buyCost1", new Object[]{this.buy1cost}));
            return;
         }

         if (UtilEco.del(p.getName(), (double)this.buy1cost)) {
            p.sendMessage(UtilFormat.format(this.pn, "buyCost2", new Object[]{this.buy1cost}));
            if (r.nextInt(this.buy1chance) == this.buy1target) {
               this.ticket.add(Bukkit.getConsoleSender(), p.getName(), this.buy1rewards, this.pn, this.get(1005));
               Bukkit.broadcastMessage(UtilFormat.format(this.pn, "buyCost3", new Object[]{p.getName(), this.buy1rewards}));
            } else {
               p.sendMessage(this.get(1010));
            }
         }
      } else if (id == 2) {
         if (Ticket.getTicket(p.getName()) < this.buy2cost) {
            p.sendMessage(UtilFormat.format(this.pn, "buyCost4", new Object[]{this.buy2cost}));
            return;
         }

         if (this.ticket.del(Bukkit.getConsoleSender(), p.getName(), this.buy2cost, this.pn, this.get(1015))) {
            p.sendMessage(UtilFormat.format(this.pn, "buyCost5", new Object[]{this.buy2cost}));
            if (r.nextInt(this.buy2chance) == this.buy2target) {
               UtilEco.add(p.getName(), (double)this.buy2rewards);
               Bukkit.broadcastMessage(UtilFormat.format(this.pn, "buyCost6", new Object[]{p.getName(), this.buy2rewards}));
            } else {
               p.sendMessage(this.get(1012));
            }
         }
      }

   }

   private void checkInitTicket() {
      if (this.ticket == null) {
         this.ticket = (Ticket)Bukkit.getPluginManager().getPlugin("ticket");
      }

   }

   private void reloadConfig(CommandSender sender) {
      Player p = null;
      if (sender instanceof Player) {
         p = (Player)sender;
      }

      if (p == null || UtilPer.checkPer(p, this.per_fix_admin)) {
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
      this.buy1cost = config.getInt("buy.buy1.cost");
      this.buy1chance = config.getInt("buy.buy1.chance");
      this.buy1target = config.getInt("buy.buy1.target");
      this.buy1rewards = config.getInt("buy.buy1.rewards");
      this.buy2cost = config.getInt("buy.buy2.cost");
      this.buy2chance = config.getInt("buy.buy2.chance");
      this.buy2target = config.getInt("buy.buy2.target");
      this.buy2rewards = config.getInt("buy.buy2.rewards");
      this.per_fix_admin = config.getString("per_fix_admin");
      this.per_fix_craft = config.getString("per_fix_craft");
      UtilTypes.reloadTypes(this.pn, config);
      UtilItems.reloadItems(this.pn, config);
      UtilCosts.reloadCosts(this.pn, config);
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
      filter.add(Pattern.compile("time.yml"));
      filter.add(Pattern.compile("fish.yml"));
      UtilConfig.register(new File(this.pluginPath + File.separator + this.pn + ".jar"), this.dataFolder, filter, this.pn);
      this.loadConfig((CommandSender)null);
   }

   private String get(int id) {
      return UtilFormat.format(this.pn, id);
   }
}
