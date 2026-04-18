package death;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.Random;
import java.util.UUID;
import java.util.regex.Pattern;
import land.Land;
import landMain.LandMain;
import lib.config.ReloadConfigEvent;
import lib.hashList.HashList;
import lib.nbt.Attributes;
import lib.nbt.Attributes.Attribute;
import lib.nbt.Attributes.AttributeType;
import lib.nbt.Attributes.Operation;
import lib.time.TimeEvent;
import lib.util.Util;
import lib.util.UtilConfig;
import lib.util.UtilEco;
import lib.util.UtilFormat;
import lib.util.UtilItems;
import lib.util.UtilPer;
import net.minecraft.server.v1_6_R2.AttributeInstance;
import net.minecraft.server.v1_6_R2.AttributeModifier;
import net.minecraft.server.v1_6_R2.EntityLiving;
import net.minecraft.server.v1_6_R2.GenericAttributes;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Server;
import org.bukkit.World;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.MemorySection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.craftbukkit.v1_6_R2.entity.CraftBat;
import org.bukkit.entity.Bat;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitScheduler;

public class Death extends JavaPlugin implements Listener {
   private static final UUID uid = UUID.fromString("5f70cc39-fb52-4f68-af37-6181d4de7350");
   private static final String FLAG_SUCK = "suck";
   private static final String LIB = "lib";
   private static Random r = new Random();
   private Server server;
   private String pn;
   private PluginManager pm;
   private BukkitScheduler scheduler;
   private String mainPath;
   private String pluginPath;
   private String dataFolder;
   private String pluginVersion;
   private String per_death_admin;
   private String per_death_gold;
   private String per_death_item;
   private String per_death_exp;
   private String per_death_durability;
   private String per_death_bat;
   private int durabilityDel;
   private int deathGold;
   private int deathGoldAdd;
   private int deathSuckGold;
   private boolean dropItem;
   private boolean dropGold;
   private String savePath;
   private boolean batEnable;
   private int batInterval;
   private int batLiveTime;
   private int batDropMin;
   private int batDropMax;
   private int batMax;
   private String batName;
   private String defaultColor;
   private List colorsAmount;
   private List colors;
   private DeathMessage deathMessage;

   public Death() {
      super();
   }

   public void onEnable() {
      this.initBasic();
      this.initConfig();
      this.loadConfig0(UtilConfig.getConfig(this.pn));
      this.savePath = this.getDataFolder() + File.separator + "drop";
      (new File(this.savePath)).mkdirs();
      this.server.getPluginManager().registerEvents(this, this);
      this.deathMessage = new DeathMessage(this);
      this.sendConsoleMessage(UtilFormat.format("lib", "pluginEnabled", new Object[]{this.pn, this.pluginVersion}));
   }

   public void onDisable() {
      if (this.scheduler != null) {
         this.scheduler.cancelAllTasks();
      }

      this.sendConsoleMessage(UtilFormat.format("lib", "pluginDisabled", new Object[]{this.pn, this.pluginVersion}));
   }

   public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
      Player p = null;
      if (sender instanceof Player) {
         p = (Player)sender;
      }

      String cmdName = cmd.getName();
      int length = args.length;
      if (cmdName.equalsIgnoreCase("death")) {
         if (p != null && !UtilPer.checkPer(p, this.per_death_admin)) {
            return true;
         }

         if ((length != 1 || !args[0].equalsIgnoreCase("?")) && length == 1 && args[0].equalsIgnoreCase("reload")) {
            this.reloadConfig(sender);
            return true;
         }

         sender.sendMessage(UtilFormat.format("lib", "cmdHelpHeader", new Object[]{this.get(10)}));
         sender.sendMessage(UtilFormat.format("lib", "cmdHelpItem", new Object[]{this.get(15), this.get(20)}));
      }

      return true;
   }

   @EventHandler(
      priority = EventPriority.LOW
   )
   public void reloadConfig(ReloadConfigEvent e) {
      if (e.getCallPlugin().equals(this.pn)) {
         this.loadConfig0(e.getConfig());
      }

   }

   @EventHandler(
      priority = EventPriority.LOW
   )
   public void onTime(TimeEvent e) {
      if (TimeEvent.getTime() % (long)this.batInterval == 0L) {
         for(World w : this.server.getWorlds()) {
            for(Entity entity : w.getEntities()) {
               if (entity instanceof Bat) {
                  Bat bat = (Bat)entity;
                  long liveTime = this.getLiveTime(bat);
                  if (liveTime > 0L) {
                     this.setLiveTime(bat, liveTime - (long)this.batInterval);
                  } else {
                     entity.remove();
                  }
               }
            }
         }
      }

   }

   @EventHandler(
      priority = EventPriority.LOWEST
   )
   public void onEntityDeath(EntityDeathEvent e) {
      if (e.getEntity() instanceof Bat) {
         Bat bat = (Bat)e.getEntity();
         Player killer = bat.getKiller();
         if (killer != null) {
            int gold = this.getGold(bat);
            if (gold > 0) {
               UtilEco.add(killer.getName(), (double)gold);
               killer.sendMessage(UtilFormat.format(this.pn, "addGold", new Object[]{gold}));
            }
         }
      }

   }

   @EventHandler(
      priority = EventPriority.LOWEST,
      ignoreCancelled = true
   )
   public void playerDeath(PlayerDeathEvent e) {
      Player p = e.getEntity();
      File file = new File(this.savePath + File.separator + p.getName() + ".ini");
      file.delete();
      e.setDroppedExp(0);
      if (e.getEntity() != null) {
         p.sendMessage(UtilFormat.format("lib", "tip", new Object[]{this.get(125)}));
         int goldNeed = 0;
         int has = (int)UtilEco.get(p.getName());
         int need = 0;
         if (!UtilPer.hasPer(p, this.per_death_gold)) {
            need = this.deathGold + has * this.deathGoldAdd / 10000;
         }

         int needBat = 0;
         if (this.batEnable && !UtilPer.hasPer(p, this.per_death_bat)) {
            needBat = r.nextInt(this.batDropMax - this.batDropMin + 1) + this.batDropMin;
         }

         int totalNeed = need + needBat;
         if (has < totalNeed) {
            goldNeed = totalNeed;
         }

         int cost = Math.min(has, totalNeed);
         if (cost > 0) {
            if (goldNeed <= 0 || goldNeed > 0 && this.dropGold) {
               UtilEco.del(p.getName(), (double)cost);
               p.sendMessage(UtilFormat.format(this.pn, "death", new Object[]{cost}));
               Land land = LandMain.getLandManager().getHighestPriorityLand(p.getLocation());
               if (land != null && land.hasFlag("suck")) {
                  int suck = Math.min(cost, need) * this.deathSuckGold / 100;
                  if (suck > 0) {
                     UtilEco.add(land.getOwner(), (double)suck);
                     Util.sendMsg(land.getOwner(), UtilFormat.format(this.pn, "suck", new Object[]{p.getName(), land.getName(), suck}));
                  }
               }

               int leftBat = cost - need;
               if (leftBat > 0) {
                  SpawnBat spawnBat = new SpawnBat(p.getEyeLocation(), leftBat);
                  Bukkit.getScheduler().scheduleSyncDelayedTask(this, spawnBat);
               }
            }
         } else {
            p.sendMessage(UtilFormat.format("lib", "tip", new Object[]{this.get(110)}));
         }

         if (UtilPer.hasPer(p, this.per_death_exp)) {
            e.setKeepLevel(true);
            p.sendMessage(UtilFormat.format("lib", "tip", new Object[]{this.get(115)}));
         } else {
            e.setKeepLevel(false);
            p.sendMessage(UtilFormat.format("lib", "fail", new Object[]{this.get(120)}));
         }

         boolean vip = true;
         if (!UtilPer.hasPer(p, this.per_death_durability)) {
            vip = false;

            for(int i = 0; i <= 39; ++i) {
               ItemStack is = p.getInventory().getItem(i);
               if (is != null && is.getTypeId() != 0 && is.getAmount() > 0 && UtilItems.hasDurability(is)) {
                  short result = (short)(is.getType().getMaxDurability() - (is.getType().getMaxDurability() - is.getDurability()) * (100 - this.durabilityDel) / 100);
                  result = (short)Math.min(result, is.getType().getMaxDurability());
                  is.setDurability(result);
               }
            }
         }

         if (UtilPer.hasPer(p, this.per_death_item)) {
            if (this.dropItem && goldNeed > 0) {
               p.sendMessage(UtilFormat.format(this.pn, "deathDrop", new Object[]{goldNeed}));
            } else {
               boolean result = false;
               Properties pro = new Properties();
               YamlConfiguration saveConfig = new YamlConfiguration();

               for(int i = 0; i <= 39; ++i) {
                  try {
                     ItemStack is = p.getInventory().getItem(i);
                     if (is != null && is.getAmount() > 0) {
                        result = true;
                        saveConfig.createSection(String.valueOf(i), is.serialize());
                        Attributes at = new Attributes(is);
                        if (at.size() > 0) {
                           int index = 1;

                           for(Attributes.Attribute a : at.values()) {
                              String s = a.getAmount() + " " + a.getAttributeType().getMinecraftId() + " " + a.getOperation().getId() + " " + a.getUUID().toString() + " " + a.getName();
                              pro.setProperty(i + "-" + index, s);
                              ++index;
                           }
                        }
                     }
                  } catch (Exception var30) {
                  }
               }

               if (!result) {
                  p.sendMessage(UtilFormat.format("lib", "fail", new Object[]{this.get(100)}));
                  return;
               }

               FileOutputStream fos = null;
               OutputStreamWriter osw = null;

               try {
                  String s = saveConfig.saveToString();
                  pro.setProperty("a", s);
                  fos = new FileOutputStream(file);
                  osw = new OutputStreamWriter(fos, Charset.forName("UTF-8"));
                  pro.store(osw, "drop");
                  if (vip) {
                     p.sendMessage(UtilFormat.format(this.pn, "tip", new Object[]{this.get(106)}));
                  } else {
                     p.sendMessage(UtilFormat.format(this.pn, "tip", new Object[]{this.get(105)}));
                  }
               } catch (IOException var28) {
                  this.server.getConsoleSender().sendMessage(UtilFormat.format("lib", "fail", new Object[]{this.get(80)}));
               } finally {
                  try {
                     if (fos != null) {
                        fos.close();
                     }

                     if (osw != null) {
                        osw.close();
                     }
                  } catch (IOException e1) {
                     e1.printStackTrace();
                  }

               }

               e.getDrops().clear();
            }
         } else {
            p.sendMessage(UtilFormat.format("lib", "fail", new Object[]{this.get(95)}));
         }

         p.sendMessage(UtilFormat.format("lib", "tip", new Object[]{this.get(125)}));
      }
   }

   @EventHandler(
      priority = EventPriority.LOWEST
   )
   public void playerRespawn(PlayerRespawnEvent e) {
      this.getback(e.getPlayer());
   }

   public void getback(Player p) {
      boolean result = false;
      File file = new File(this.savePath + File.separator + p.getName() + ".ini");
      Properties pro = new Properties();
      FileInputStream fis = null;
      InputStreamReader isr = null;

      try {
         fis = new FileInputStream(file);
         isr = new InputStreamReader(fis, Charset.forName("UTF-8"));
         pro.load(isr);
         YamlConfiguration loadConfig = new YamlConfiguration();
         String s = pro.getProperty("a");
         loadConfig.loadFromString(s);
         PlayerInventory pi = p.getInventory();
         MemorySection ms = null;

         for(int i = 0; i <= 39; ++i) {
            try {
               ms = (MemorySection)loadConfig.get(String.valueOf(i));
               if (ms != null) {
                  ItemStack is = ItemStack.deserialize(ms.getValues(true));
                  if (is.getAmount() > 0) {
                     result = true;

                     try {
                        int index = 1;
                        Attributes at = new Attributes(is);
                        boolean has = false;

                        while(true) {
                           String nbt = pro.getProperty(i + "-" + index);
                           ++index;
                           if (nbt == null) {
                              if (has) {
                                 is = at.getStack();
                              }
                              break;
                           }

                           has = true;
                           String[] ss = nbt.split(" ");
                           double amount = Double.parseDouble(ss[0]);
                           String minecraftId = ss[1];
                           int operationId = Integer.parseInt(ss[2]);
                           String UUIDStr = ss[3];
                           String name = Util.combine(ss, " ", 4, ss.length);
                           at.add(Attribute.newBuilder().name(name).amount(amount).type(AttributeType.fromId(minecraftId)).operation(Operation.fromId(operationId)).uuid(UUID.fromString(UUIDStr)).build());
                        }
                     } catch (Exception var40) {
                     }

                     if (pi.getItem(i) == null) {
                        pi.setItem(i, is);
                     } else {
                        pi.addItem(new ItemStack[]{is});
                     }
                  }
               }
            } catch (Exception var41) {
            }
         }

         if (result) {
            p.sendMessage(UtilFormat.format("lib", "success", new Object[]{this.get(85)}));
         } else {
            p.sendMessage(this.get(90));
         }
      } catch (FileNotFoundException var42) {
         p.sendMessage(this.get(90));
      } catch (IOException var43) {
         Util.sendConsoleMessage(UtilFormat.format("lib", "fail", new Object[]{this.get(80)}));
      } catch (InvalidConfigurationException e) {
         e.printStackTrace();
      } catch (Exception var45) {
      } finally {
         try {
            if (isr != null) {
               isr.close();
            }

            if (fis != null) {
               fis.close();
            }

            file.delete();
         } catch (Exception e) {
            e.printStackTrace();
         }

      }

   }

   public PluginManager getPm() {
      return this.pm;
   }

   public String getMainPath() {
      return this.mainPath;
   }

   public String getPluginPath() {
      return this.pluginPath;
   }

   public String getPn() {
      return this.pn;
   }

   public DeathMessage getDeathMessage() {
      return this.deathMessage;
   }

   private int getGold(Bat bat) {
      try {
         String name = bat.getCustomName();
         int result = Integer.parseInt(name.substring(2, name.length() - 2));
         if (result > this.batMax) {
            result = this.batMax;
         }

         return result;
      } catch (Exception var4) {
         return 0;
      }
   }

   private void setGold(Bat bat, int gold) {
      if (gold <= 0) {
         bat.setCustomNameVisible(false);
         bat.setCustomName((String)null);
      } else {
         if (gold > this.batMax) {
            gold = this.batMax;
         }

         String color = this.defaultColor;

         for(int i = 0; i < this.colorsAmount.size(); ++i) {
            int amount = (Integer)this.colorsAmount.get(i);
            if (gold <= amount) {
               color = (String)this.colors.get(i);
               break;
            }
         }

         bat.setCustomNameVisible(true);
         bat.setCustomName(this.batName.replace("{0}", color).replace("{1}", String.valueOf(gold)));
      }

   }

   private long getLiveTime(Bat bat) {
      try {
         EntityLiving el = ((CraftBat)bat).getHandle();
         AttributeInstance ai = el.getAttributeInstance(GenericAttributes.a);
         if (ai != null) {
            AttributeModifier am = ai.a(uid);
            if (am != null) {
               String data = am.b();
               if (data != null && !data.isEmpty()) {
                  YamlConfiguration config = new YamlConfiguration();
                  config.loadFromString(data);
                  long liveTime = config.getLong("liveTime");
                  return liveTime;
               }
            }
         }
      } catch (Exception var9) {
      }

      return 0L;
   }

   private void setLiveTime(Bat bat, long liveTime) {
      EntityLiving el = ((CraftBat)bat).getHandle();
      AttributeInstance ai = el.getAttributeInstance(GenericAttributes.a);
      if (ai != null) {
         YamlConfiguration config = new YamlConfiguration();
         config.set("liveTime", liveTime);
         String data = config.saveToString();
         AttributeModifier am = new AttributeModifier(uid, data, (double)0.0F, 0);
         ai.b(am);
         ai.a(am);
      }

   }

   private void loadConfig0(FileConfiguration config) {
      this.per_death_admin = config.getString("per_death_admin");
      this.per_death_gold = config.getString("per_death_gold");
      this.per_death_item = config.getString("per_death_item");
      this.per_death_exp = config.getString("per_death_exp");
      this.per_death_durability = config.getString("per_death_durability");
      this.per_death_bat = config.getString("per_death_bat");
      this.durabilityDel = config.getInt("durabilityDel");
      this.deathGold = config.getInt("death.gold");
      this.deathGoldAdd = config.getInt("death.goldAdd");
      this.deathSuckGold = config.getInt("death.suckGold");
      this.dropItem = config.getBoolean("death.dropItem");
      this.dropGold = config.getBoolean("death.dropGold");
      this.batEnable = config.getBoolean("bat.enable");
      this.batInterval = config.getInt("bat.interval");
      this.batLiveTime = config.getInt("bat.liveTime");
      this.batDropMin = config.getInt("bat.gold.min");
      this.batDropMax = config.getInt("bat.gold.max");
      this.batMax = config.getInt("bat.max");
      this.batName = Util.convert(config.getString("bat.name"));
      this.defaultColor = config.getString("bat.defaultColor");
      this.colorsAmount = new ArrayList();
      this.colors = new ArrayList();

      for(String s : config.getStringList("bat.colors")) {
         int amount = Integer.parseInt(s.split(" ")[0]);
         String color = s.split(" ")[1];
         this.colorsAmount.add(amount);
         this.colors.add(color);
      }

   }

   private void sendConsoleMessage(String msg) {
      if (this.server.getConsoleSender() != null) {
         this.server.getConsoleSender().sendMessage(msg);
      } else {
         this.server.getLogger().info(msg);
      }

   }

   private void initBasic() {
      this.server = this.getServer();
      this.pn = this.getName();
      this.pm = this.server.getPluginManager();
      this.scheduler = this.server.getScheduler();
      this.mainPath = System.getProperty("user.dir");
      this.pluginPath = this.getFile().getParentFile().getAbsolutePath();
      this.dataFolder = this.pluginPath + File.separator + this.pn;
      this.pluginVersion = Util.getPluginVersion(this.getFile());
   }

   private void initConfig() {
      HashList<Pattern> filter = UtilConfig.getDefaultFilter();
      filter.add(Pattern.compile("deathMsg.yml"));
      UtilConfig.register(new File(this.pluginPath + File.separator + this.pn + ".jar"), this.dataFolder, filter, this.pn);
      this.loadConfig((CommandSender)null);
   }

   private void reloadConfig(CommandSender sender) {
      if (this.loadConfig(sender)) {
         sender.sendMessage(UtilFormat.format("lib", "success", new Object[]{this.get(25)}));
      } else {
         sender.sendMessage(UtilFormat.format("lib", "fail", new Object[]{this.get(30)}));
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

   private String get(int id) {
      return UtilFormat.format(this.pn, id);
   }

   private class SpawnBat implements Runnable {
      Location l;
      int gold;

      public SpawnBat(Location l, int gold) {
         super();
         this.l = l;
         this.gold = gold;
      }

      public void run() {
         Bat bat = (Bat)this.l.getWorld().spawnEntity(this.l, EntityType.BAT);
         Death.this.setGold(bat, this.gold);
         Death.this.setLiveTime(bat, (long)Death.this.batLiveTime);
      }
   }
}
