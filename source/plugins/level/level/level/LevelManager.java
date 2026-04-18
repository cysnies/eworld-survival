package level;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Random;
import lib.config.ReloadConfigEvent;
import lib.hashList.HashList;
import lib.hashList.HashListImpl;
import lib.realDamage.RealDamageEvent;
import lib.tab.Tab;
import lib.time.TimeEvent;
import lib.util.Util;
import lib.util.UtilConfig;
import lib.util.UtilFormat;
import lib.util.UtilItems;
import lib.util.UtilPer;
import lib.util.UtilRewards;
import lib.util.UtilScoreboard;
import lib.util.UtilSpeed;
import lib.util.UtilTab;
import org.bukkit.Bukkit;
import org.bukkit.Server;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.MemorySection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

public class LevelManager implements Listener {
   private static final int HOUR = 3600000;
   private static final String SPEED_CHANGESHOW = "changeShow";
   private static final Random r = new Random();
   private static final String LEVEL = "level";
   private Main main;
   private Server server;
   private HashMap addTypeHash;
   private HashMap typeHash;
   private HashMap levelHash;
   private HashMap userHash;
   private HashMap pageHash;
   private HashMap typeListHash;
   private HashMap itemHash;
   private HashMap itemHash2;
   private HashMap attack1Hash;
   private HashMap health1Hash;
   private HashMap attack2Hash;
   private HashMap health2Hash;
   private HashMap missHash;
   private int checkInterval;
   private int showInterval;
   private String preHas;
   private String preHasnot;
   private HashMap modeHash = new HashMap();
   private HashMap playerHash;

   public LevelManager(Main main) {
      super();
      this.main = main;
      this.server = main.getServer();
      this.attack1Hash = new HashMap();
      this.health1Hash = new HashMap();
      this.attack2Hash = new HashMap();
      this.health2Hash = new HashMap();
      this.missHash = new HashMap();
      this.loadConfig(UtilConfig.getConfig(Main.getPn()));
      this.server.getPluginManager().registerEvents(this, main);
      this.loadData();
      UtilSpeed.register(Main.getPn(), "changeShow");
      UtilTab.register("level");
      this.playerHash = UtilTab.getMode("level");
   }

   @EventHandler(
      priority = EventPriority.LOW
   )
   public void onReloadConfig(ReloadConfigEvent e) {
      if (e.getCallPlugin().equals(Main.getPn())) {
         this.loadConfig(e.getConfig());
      }

   }

   @EventHandler(
      priority = EventPriority.LOW
   )
   public void onTime(TimeEvent e) {
      if (TimeEvent.getTime() % (long)this.checkInterval == 0L) {
         Player[] var5;
         for(Player p : var5 = this.server.getOnlinePlayers()) {
            this.checkTimeLimit(p);
         }
      }

   }

   @EventHandler(
      priority = EventPriority.LOW
   )
   public void onPlayerJoin(PlayerJoinEvent e) {
      this.checkTotalEffect(e.getPlayer().getName(), false);
      Tab.Mode mode = (Tab.Mode)this.modeHash.get(e.getPlayer().getName());
      if (mode != null) {
         this.playerHash.put(e.getPlayer(), mode);
      }

   }

   @EventHandler(
      priority = EventPriority.LOWEST
   )
   public void onPlayerQuit(PlayerQuitEvent e) {
      this.playerHash.remove(e.getPlayer());
   }

   @EventHandler(
      priority = EventPriority.HIGHEST
   )
   public void onRealDamage(RealDamageEvent e) {
      if (e.getVictim() instanceof Player && !e.getVictim().isDead()) {
         Player p = (Player)e.getVictim();
         Double chance = (Double)this.missHash.get(p.getName());
         if (chance != null && (double)r.nextInt(100) < chance) {
            e.setDamage((double)0.0F);
            p.sendMessage(this.get(235));
         }
      }

   }

   public LevelUser getLevelUser(String name) {
      return (LevelUser)this.userHash.get(name);
   }

   public void addLevel(CommandSender sender, String tar, int id) {
      this.addLevel(sender, tar, id, -1, true);
   }

   public void addLevel(CommandSender sender, String tar, int id, int time, boolean rewards) {
      if (sender instanceof Player) {
         Player p = (Player)sender;
         if (!UtilPer.checkPer(p, Main.getPer_level_admin())) {
            return;
         }
      }

      tar = Util.getRealName(sender, tar);
      if (tar != null) {
         if (this.checkExsit(sender, id)) {
            Level level = (Level)this.levelHash.get(id);
            LevelUser levelUser = this.checkInit(tar);
            if (levelUser.getLevelHash().containsKey(id) && level.getTimeLimit() <= 0) {
               if (sender != null) {
                  sender.sendMessage(UtilFormat.format(Main.getPn(), "fail", new Object[]{this.get(135)}));
               }

            } else {
               if (sender != null) {
                  sender.sendMessage(UtilFormat.format(Main.getPn(), "addEffect0", new Object[]{tar, level.getName()}));
               }

               Player tarP = this.server.getPlayerExact(tar);
               if (tarP != null) {
                  tarP.sendMessage(UtilFormat.format(Main.getPn(), "addEffect", new Object[]{level.getName()}));
               }

               LevelUserInfo levelUserInfo = (LevelUserInfo)levelUser.getLevelHash().get(id);
               if (levelUserInfo == null) {
                  long now = System.currentTimeMillis();
                  levelUserInfo = new LevelUserInfo(id, now, 0, false);
                  levelUser.getLevelHash().put(id, levelUserInfo);
               }

               if (level.getTimeLimit() > 0) {
                  int addTime;
                  if (time == -1) {
                     addTime = level.getTimeLimit();
                  } else {
                     addTime = time;
                  }

                  levelUserInfo.setLast(levelUserInfo.getLast() + addTime);
                  if (tarP != null) {
                     tarP.sendMessage(UtilFormat.format(Main.getPn(), "addEffectTime", new Object[]{addTime / 24, addTime % 24}));
                  }
               } else if (level.getTimeLimit() == -2) {
                  if (tarP != null) {
                     tarP.sendMessage(UtilFormat.format(Main.getPn(), "success", new Object[]{this.get(115)}));
                  }
               } else if (tarP != null) {
                  tarP.sendMessage(UtilFormat.format(Main.getPn(), "success", new Object[]{this.get(120)}));
               }

               if (level.isOverlap()) {
                  if (!levelUserInfo.isEffect()) {
                     levelUserInfo.setEffect(true);
                     if (tarP != null) {
                        tarP.sendMessage(UtilFormat.format(Main.getPn(), "success", new Object[]{this.get(125)}));
                     }
                  } else if (tarP != null) {
                     tarP.sendMessage(UtilFormat.format(Main.getPn(), "success", new Object[]{this.get(123)}));
                  }
               } else if (levelUserInfo.isEffect()) {
                  if (tarP != null) {
                     tarP.sendMessage(UtilFormat.format(Main.getPn(), "success", new Object[]{this.get(123)}));
                  }
               } else if (tarP != null) {
                  tarP.sendMessage(UtilFormat.format(Main.getPn(), "success", new Object[]{this.get(130)}));
               }

               Main.getDao().addOrUpdateLevelUser(levelUser);
               this.checkTotalEffect(tar, true);
               Tab.Mode mode = (Tab.Mode)this.modeHash.get(tar);
               if (mode != null) {
                  mode.set(String.valueOf(id), this.getTabName(level, true));
               }

               if (rewards) {
                  if (level.getRewards() != null && !level.getRewards().isEmpty()) {
                     UtilRewards.addRewards(this.server.getConsoleSender(), tar, Main.getPn(), level.getRewards());
                     if (tarP != null) {
                        tarP.sendMessage(UtilFormat.format(Main.getPn(), "success", new Object[]{this.get(170)}));
                     }
                  } else if (tarP != null) {
                     tarP.sendMessage(UtilFormat.format(Main.getPn(), "success", new Object[]{this.get(165)}));
                  }
               }

               this.server.dispatchCommand(this.server.getConsoleSender(), "manuaddsub " + tar + " " + level.getGroup());
               this.server.broadcastMessage(UtilFormat.format(Main.getPn(), "conAdd", new Object[]{tar, level.getShow()}));
            }
         }
      }
   }

   public void delLevel(CommandSender sender, String tar, int id, boolean force) {
      if (sender != null && sender instanceof Player) {
         Player p = (Player)sender;
         if (!UtilPer.checkPer(p, Main.getPer_level_admin())) {
            return;
         }
      }

      tar = Util.getRealName(sender, tar);
      if (tar != null) {
         if (this.checkExsit(sender, id)) {
            Level level = (Level)this.levelHash.get(id);
            LevelUser levelUser = this.checkInit(tar);
            if (!levelUser.getLevelHash().containsKey(id)) {
               if (sender != null) {
                  sender.sendMessage(UtilFormat.format(Main.getPn(), "fail", new Object[]{this.get(138)}));
               }

            } else if (!force && level.getTimeLimit() == -2) {
               if (sender != null) {
                  sender.sendMessage(UtilFormat.format(Main.getPn(), "fail", new Object[]{this.get(139)}));
               }

            } else {
               if (sender != null) {
                  sender.sendMessage(UtilFormat.format(Main.getPn(), "delEffect0", new Object[]{tar, level.getName()}));
               }

               Player tarP = this.server.getPlayerExact(tar);
               if (tarP != null) {
                  tarP.sendMessage(UtilFormat.format(Main.getPn(), "delEffect", new Object[]{level.getName()}));
               }

               levelUser.getLevelHash().remove(id);
               if (levelUser.getShowLevelId() == id) {
                  levelUser.setShowLevelId(0);
               }

               Main.getDao().addOrUpdateLevelUser(levelUser);
               this.checkTotalEffect(tar, true);
               Tab.Mode mode = (Tab.Mode)this.modeHash.get(tar);
               if (mode != null) {
                  mode.set(String.valueOf(id), this.getTabName(level, false));
               }

               this.server.dispatchCommand(this.server.getConsoleSender(), "manudelsub " + tar + " " + level.getGroup());
            }
         }
      }
   }

   public HashMap getUserHash() {
      return this.userHash;
   }

   public void toggleEffect(Player p, int id) {
      if (UtilSpeed.check(p, Main.getPn(), "changeShow", this.showInterval)) {
         if (this.checkExsit(p, id)) {
            if (!this.hasLevel(p.getName(), id)) {
               p.sendMessage(UtilFormat.format(Main.getPn(), "fail", new Object[]{this.get(50)}));
            } else {
               Level level = (Level)this.levelHash.get(id);
               if (level.isOverlap()) {
                  p.sendMessage(UtilFormat.format(Main.getPn(), "fail", new Object[]{this.get(55)}));
               } else {
                  LevelUser levelUser = this.checkInit(p.getName());
                  HashMap<Integer, LevelUserInfo> levelHash = levelUser.getLevelHash();
                  LevelUserInfo levelUserInfo = (LevelUserInfo)levelHash.get(id);
                  if (!levelUserInfo.isEffect()) {
                     for(int i : levelHash.keySet()) {
                        if (i != id && !((Level)this.levelHash.get(i)).isOverlap() && ((LevelUserInfo)levelHash.get(i)).isEffect() && ((Level)this.levelHash.get(i)).getTypeId() == level.getTypeId()) {
                           p.sendMessage(UtilFormat.format(Main.getPn(), "fail", new Object[]{this.get(110)}));
                           return;
                        }
                     }
                  }

                  levelUserInfo.setEffect(!levelUserInfo.isEffect());
                  Main.getDao().addOrUpdateLevelUser(levelUser);
                  this.checkTotalEffect(p.getName(), true);
                  String to;
                  if (levelUserInfo.isEffect()) {
                     to = this.get(140);
                  } else {
                     to = this.get(145);
                  }

                  p.sendMessage(UtilFormat.format(Main.getPn(), "toggleEffect", new Object[]{level.getName(), to}));
               }
            }
         }
      }
   }

   public void selectShow(Player p, int id) {
      if (this.checkExsit(p, id)) {
         if (!this.hasLevel(p.getName(), id)) {
            p.sendMessage(UtilFormat.format(Main.getPn(), "fail", new Object[]{this.get(50)}));
         } else if (UtilSpeed.check(p, Main.getPn(), "changeShow", this.showInterval)) {
            LevelUser levelUser = this.checkInit(p.getName());
            levelUser.setShowLevelId(id);
            Level level = (Level)this.levelHash.get(id);
            LevelNameSetEvent event = new LevelNameSetEvent(p, level, level.getShow());
            Bukkit.getPluginManager().callEvent(event);
            UtilScoreboard.setPrefix(p, event.getShow());
            p.setDisplayName(event.getShow() + p.getName());
            Main.getDao().addOrUpdateLevelUser(levelUser);
            p.sendMessage(UtilFormat.format(Main.getPn(), "success", new Object[]{this.get(150)}));
         }
      }
   }

   public boolean hasLevel(String name, int id) {
      LevelUser levelUser = this.checkInit(name);
      return levelUser.getLevelHash().containsKey(id);
   }

   public ItemStack getInfoItem(String name) {
      ItemStack result = UtilItems.getItem(Main.getPn(), "main_info").clone();
      ItemMeta im = result.getItemMeta();
      List<String> lore = im.getLore();
      String now = "";
      LevelUser levelUser = this.checkInit(name);
      int id = levelUser.getShowLevelId();
      if (id != 0) {
         Level level = (Level)this.levelHash.get(id);
         if (level != null) {
            now = level.getShow();
         }
      }

      if (now.isEmpty()) {
         now = this.get(160);
      }

      im.setDisplayName(im.getDisplayName().replace("{0}", now));
      lore.set(1, ((String)lore.get(1)).replace("{0}", "" + this.attack1Hash.get(name)));
      lore.set(2, ((String)lore.get(2)).replace("{1}", "" + this.health1Hash.get(name)));
      lore.set(3, ((String)lore.get(3)).replace("{2}", String.valueOf(Util.getDouble((Double)this.attack2Hash.get(name) * (double)100.0F, 2))));
      lore.set(4, ((String)lore.get(4)).replace("{3}", String.valueOf(Util.getDouble((Double)this.health2Hash.get(name) * (double)100.0F, 2))));
      lore.set(5, ((String)lore.get(5)).replace("{4}", String.valueOf(Util.getDouble((Double)this.missHash.get(name), 2))));
      im.setLore(lore);
      result.setItemMeta(im);
      return result;
   }

   public int getMaxPage() {
      return this.pageHash.size();
   }

   public int getPageType(int page) {
      return (Integer)this.pageHash.get(page);
   }

   public String getTypeName(int typeId) {
      return (String)this.typeHash.get(typeId);
   }

   public HashMap getTypeListHash() {
      return this.typeListHash;
   }

   public ItemStack getItem(int id) {
      return (ItemStack)this.itemHash.get(id);
   }

   public ItemStack getHasItem(String name, int id) {
      LevelUser levelUser = this.checkInit(name);
      LevelUserInfo levelUserInfo = (LevelUserInfo)levelUser.getLevelHash().get(id);
      if (levelUserInfo == null) {
         return null;
      } else {
         ItemStack result = (ItemStack)this.itemHash2.get(id);
         if (levelUserInfo.isEffect() || levelUserInfo.getLast() > 0) {
            result = result.clone();
            ItemMeta im = result.getItemMeta();
            List<String> lore = im.getLore();
            if (levelUserInfo.isEffect()) {
               lore.set(lore.size() - 1, UtilFormat.format(Main.getPn(), "levelShowAdd1", new Object[]{this.get(140)}));
            }

            if (levelUserInfo.getLast() > 0) {
               String endTime = Util.getDateTime(new Date(levelUserInfo.getStart()), 0, levelUserInfo.getLast(), 0);
               lore.add(UtilFormat.format(Main.getPn(), "levelShowAdd2", new Object[]{endTime}));
            }

            im.setLore(lore);
            result.setItemMeta(im);
         }

         return result;
      }
   }

   public ItemStack getHasnotItem(String name, int id) {
      LevelUser levelUser = this.checkInit(name);
      LevelUserInfo levelUserInfo = (LevelUserInfo)levelUser.getLevelHash().get(id);
      if (levelUserInfo != null) {
         return null;
      } else {
         ItemStack result = (ItemStack)this.itemHash.get(id);
         return result;
      }
   }

   public ItemStack getSelectItem(String name, int id) {
      LevelUser levelUser = this.checkInit(name);
      LevelUserInfo levelUserInfo = (LevelUserInfo)levelUser.getLevelHash().get(id);
      if (levelUserInfo == null) {
         return null;
      } else {
         ItemStack result = (ItemStack)this.itemHash.get(id);
         return result;
      }
   }

   public LevelUser checkInit(String name) {
      LevelUser levelUser = (LevelUser)this.userHash.get(name);
      if (levelUser == null) {
         levelUser = new LevelUser(name);
         this.userHash.put(name, levelUser);
         Main.getDao().addOrUpdateLevelUser(levelUser);
         this.initMode(name, levelUser);
      }

      return levelUser;
   }

   private void checkTimeLimit(Player p) {
      LevelUser levelUser = this.checkInit(p.getName());

      for(int id : levelUser.getLevelHash().keySet()) {
         Level level = (Level)this.levelHash.get(id);
         if (level != null && level.getTimeLimit() > 0) {
            LevelUserInfo levelUserInfo = (LevelUserInfo)levelUser.getLevelHash().get(id);
            long now = System.currentTimeMillis();
            int hour = (int)((now - levelUserInfo.getStart()) / 3600000L);
            if (hour >= levelUserInfo.getLast()) {
               this.server.getScheduler().scheduleSyncDelayedTask(this.main, new DelLevel(p, id, level.getName()));
            }
         }
      }

   }

   private void checkTotalEffect(String name, boolean update) {
      LevelUser levelUser = this.checkInit(name);
      if (!this.attack1Hash.containsKey(name) || update) {
         double attack1Amount = (double)0.0F;
         double health1Amount = (double)0.0F;
         double attack2Amount = (double)0.0F;
         double health2Amount = (double)0.0F;
         double missAmount = (double)0.0F;

         for(int id : levelUser.getLevelHash().keySet()) {
            LevelUserInfo levelUserInfo = (LevelUserInfo)levelUser.getLevelHash().get(id);
            if (levelUserInfo.isEffect()) {
               Level level = (Level)this.levelHash.get(id);

               for(Effect effect : level.getEffect()) {
                  if (effect.getType().equals(Effect.Type.attack)) {
                     if (effect.getOperation() == 0) {
                        attack1Amount += effect.getAmount();
                     } else {
                        attack2Amount += effect.getAmount();
                     }
                  } else if (effect.getType().equals(Effect.Type.health)) {
                     if (effect.getOperation() == 0) {
                        health1Amount += effect.getAmount();
                     } else {
                        health2Amount += effect.getAmount();
                     }
                  } else if (effect.getType().equals(Effect.Type.miss)) {
                     missAmount += effect.getAmount();
                  }
               }
            }
         }

         attack1Amount = Util.getDouble(attack1Amount, 2);
         health1Amount = Util.getDouble(health1Amount, 2);
         attack2Amount = Util.getDouble(attack2Amount, 2);
         health2Amount = Util.getDouble(health2Amount, 2);
         missAmount = Util.getDouble(missAmount, 2);
         this.attack1Hash.put(name, attack1Amount);
         this.health1Hash.put(name, health1Amount);
         this.attack2Hash.put(name, attack2Amount);
         this.health2Hash.put(name, health2Amount);
         this.missHash.put(name, missAmount);
         this.checkApply(name);
      }

      Player p = this.server.getPlayerExact(name);
      if (p != null) {
         if (levelUser.getShowLevelId() == 0) {
            UtilScoreboard.setPrefix(p, (String)null);
            p.setDisplayName(p.getName());
         } else {
            Level level = (Level)this.levelHash.get(levelUser.getShowLevelId());
            if (level != null) {
               LevelNameSetEvent event = new LevelNameSetEvent(p, level, level.getShow());
               Bukkit.getPluginManager().callEvent(event);
               UtilScoreboard.setPrefix(p, event.getShow());
               p.setDisplayName(event.getShow() + p.getName());
            }
         }
      }

   }

   private void checkApply(String name) {
      Player p = this.server.getPlayerExact(name);
      if (p != null) {
         double amount = (Double)this.attack1Hash.get(name);
         Effect e = new Effect(Effect.Type.attack, 0, amount);
         e.apply(p);
         amount = (Double)this.health1Hash.get(name);
         e = new Effect(Effect.Type.health, 0, amount);
         e.apply(p);
         amount = (Double)this.attack2Hash.get(name);
         e = new Effect(Effect.Type.attack, 1, amount);
         e.apply(p);
         amount = (Double)this.health2Hash.get(name);
         e = new Effect(Effect.Type.health, 1, amount);
         e.apply(p);
      }

   }

   private boolean checkExsit(CommandSender sender, int id) {
      if (!this.levelHash.containsKey(id)) {
         if (sender != null) {
            sender.sendMessage(UtilFormat.format(Main.getPn(), "notExsit", new Object[]{id}));
         }

         return false;
      } else {
         return true;
      }
   }

   private void loadData() {
      Util.sendConsoleMessage(this.get(175));
      this.userHash = new HashMap();

      for(LevelUser levelUser : Main.getDao().getAllLevelUsers()) {
         this.userHash.put(levelUser.getName(), levelUser);
      }

      Util.sendConsoleMessage(this.get(180));

      for(LevelUser levelUser : this.userHash.values()) {
         boolean update = false;
         int id = levelUser.getShowLevelId();
         if (!this.checkExsit((CommandSender)null, id)) {
            levelUser.setShowLevelId(0);
            update = true;
         }

         boolean effect = false;
         Iterator<Integer> it = levelUser.getLevelHash().keySet().iterator();

         while(it.hasNext()) {
            int i = (Integer)it.next();
            if (!this.checkExsit((CommandSender)null, i)) {
               it.remove();
               update = true;
            } else {
               Level level = (Level)this.levelHash.get(i);
               if (level.isOverlap()) {
                  if (!((LevelUserInfo)levelUser.getLevelHash().get(i)).isEffect()) {
                     ((LevelUserInfo)levelUser.getLevelHash().get(i)).setEffect(true);
                     update = true;
                  }
               } else if (((LevelUserInfo)levelUser.getLevelHash().get(i)).isEffect()) {
                  if (!effect) {
                     effect = true;
                  } else {
                     ((LevelUserInfo)levelUser.getLevelHash().get(i)).setEffect(false);
                     update = true;
                  }
               }

               int timeLimit = ((LevelUserInfo)levelUser.getLevelHash().get(i)).getLast();
               if (timeLimit < 0) {
                  timeLimit = 0;
                  ((LevelUserInfo)levelUser.getLevelHash().get(i)).setLast(timeLimit);
                  update = true;
               }

               if (timeLimit == 0) {
                  if (level.getTimeLimit() > 0) {
                     timeLimit = level.getTimeLimit();
                     ((LevelUserInfo)levelUser.getLevelHash().get(i)).setLast(timeLimit);
                     update = true;
                  }
               } else if (level.getTimeLimit() < 0) {
                  timeLimit = 0;
                  ((LevelUserInfo)levelUser.getLevelHash().get(i)).setLast(timeLimit);
                  update = true;
               }
            }
         }

         if (update) {
            Main.getDao().addOrUpdateLevelUser(levelUser);
         }
      }

      for(String name : this.userHash.keySet()) {
         LevelUser lu = (LevelUser)this.userHash.get(name);
         this.initMode(name, lu);
      }

      Util.sendConsoleMessage(this.get(185));
   }

   private void initMode(String name, LevelUser lu) {
      Tab.Mode mode = new Tab.Mode();
      this.modeHash.put(name, mode);

      for(int id : this.levelHash.keySet()) {
         LevelUserInfo lui = (LevelUserInfo)lu.getLevelHash().get(id);
         String tabName = this.getTabName((Level)this.levelHash.get(id), lui != null);
         mode.add(String.valueOf(id), tabName);
      }

   }

   private String getTabName(Level level, boolean has) {
      String s;
      if (has) {
         s = this.preHas;
      } else {
         s = this.preHasnot;
      }

      String result = s + level.getShow();
      result = result.substring(0, Math.min(16, result.length()));
      return result;
   }

   private void loadConfig(YamlConfiguration config) {
      this.addTypeHash = new HashMap();

      for(String s : config.getStringList("addType")) {
         int id = Integer.parseInt(s.split(" ")[0]);
         String show = s.split(" ")[1];
         this.addTypeHash.put(id, show);
      }

      this.typeHash = new HashMap();
      this.typeListHash = new HashMap();
      this.pageHash = new HashMap();
      int index = 1;

      for(String s : config.getStringList("type")) {
         int id = Integer.parseInt(s.split(" ")[0]);
         String show = s.split(" ")[1];
         this.typeHash.put(id, show);
         this.typeListHash.put(id, new ArrayList());
         this.pageHash.put(index++, id);
      }

      index = 1;
      this.levelHash = new HashMap();
      MemorySection ms = (MemorySection)config.get("level");

      for(String type : ms.getValues(false).keySet()) {
         int id = ms.getInt(type + ".id");
         int typeId = ms.getInt(type + ".typeId");
         ((List)this.typeListHash.get(typeId)).add(id);
         boolean overlap = ms.getBoolean(type + ".overlap");
         String name = ms.getString(type + ".name");
         String show = Util.convert(ms.getString(type + ".show"));
         String condition = ms.getString(type + ".condition");
         int addType = ms.getInt(type + ".addType");
         int timeLimit = ms.getInt(type + ".timeLimit");
         String group = ms.getString(type + ".group");
         List<String> pers = ms.getStringList(type + ".pers");
         if (pers == null) {
            pers = new ArrayList();
         }

         for(int i = 0; i < pers.size(); ++i) {
            pers.set(i, Util.convert((String)pers.get(i)));
         }

         List<String> rewardsShow = ms.getStringList(type + ".rewardsShow");
         if (rewardsShow == null) {
            rewardsShow = new ArrayList();
         }

         for(int i = 0; i < rewardsShow.size(); ++i) {
            rewardsShow.set(i, Util.convert((String)rewardsShow.get(i)));
         }

         HashList<Effect> effect = new HashListImpl();

         for(String s : ms.getStringList(type + ".effects")) {
            Effect.Type effectType;
            switch (Integer.parseInt(s.split(" ")[0])) {
               case 0:
                  effectType = Effect.Type.attack;
                  break;
               case 1:
                  effectType = Effect.Type.health;
                  break;
               case 2:
                  effectType = Effect.Type.miss;
                  break;
               default:
                  effectType = Effect.Type.health;
            }

            int operation = Integer.parseInt(s.split(" ")[1]);
            double amount = Double.parseDouble(s.split(" ")[2]);
            effect.add(new Effect(effectType, operation, amount));
         }

         String rewards = ms.getString(type + ".rewards");
         Level level = new Level(id, typeId, name, show, overlap, condition, rewards, rewardsShow, group, pers, timeLimit, addType, effect);
         this.levelHash.put(id, level);
      }

      this.itemHash = new HashMap();
      this.itemHash2 = new HashMap();

      for(Level level : this.levelHash.values()) {
         ItemStack is = this.getItem(level);
         this.itemHash.put(level.getId(), is);
         ItemStack is2 = this.getItem2(level);
         this.itemHash2.put(level.getId(), is2);
      }

      this.checkInterval = config.getInt("check.interval");
      this.showInterval = config.getInt("show.interval");
      this.preHas = Util.convert(config.getString("pre.has"));
      this.preHasnot = Util.convert(config.getString("pre.hasnot"));
   }

   private ItemStack getItem(Level level) {
      ItemStack is = UtilItems.getItem(Main.getPn(), "has_info").clone();
      ItemMeta im = is.getItemMeta();
      List<String> lore = new ArrayList();
      im.setDisplayName(UtilFormat.format(Main.getPn(), "levelShow", new Object[]{level.getName()}));
      String show = UtilFormat.format(Main.getPn(), "levelShowList", new Object[]{level.getShow(), level.getOverlapShow(), level.getCondition(), level.getAddTypeShow(), level.getTimeLimitShow(), level.getPerShow(), level.getEffectShow(), level.getRewardsShow2()});

      String[] var9;
      for(String s : var9 = show.split("\n")) {
         lore.add(s.trim());
      }

      im.setLore(lore);
      is.setItemMeta(im);
      return is;
   }

   private ItemStack getItem2(Level level) {
      ItemStack is = this.getItem(level).clone();
      ItemMeta im = is.getItemMeta();
      List<String> lore = im.getLore();
      String s = UtilFormat.format(Main.getPn(), "levelShowAdd1", new Object[]{this.get(145)});
      lore.add(s);
      im.setLore(lore);
      is.setItemMeta(im);
      return is;
   }

   private String get(int id) {
      return UtilFormat.format(Main.getPn(), id);
   }

   private class DelLevel implements Runnable {
      private Player p;
      private int id;
      private String name;

      public DelLevel(Player p, int id, String name) {
         super();
         this.p = p;
         this.id = id;
         this.name = name;
      }

      public void run() {
         if (this.p != null && this.p.isOnline()) {
            LevelManager.this.delLevel((CommandSender)null, this.p.getName(), this.id, false);
            this.p.sendMessage(UtilFormat.format(Main.getPn(), "reachTimeLimit", new Object[]{this.name}));
         }

      }
   }
}
