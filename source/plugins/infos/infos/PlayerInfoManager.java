package infos;

import event.LandCreateEvent;
import event.LandRemoveEvent;
import event.OwnerChangeEvent;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Random;
import land.Land;
import landMain.LandMain;
import level.LevelManager;
import level.LevelUser;
import level.Main;
import lib.config.ReloadConfigEvent;
import lib.hashList.HashList;
import lib.hashList.HashListImpl;
import lib.tab.Tab;
import lib.time.TimeEvent;
import lib.types.InvalidTypeException;
import lib.util.Util;
import lib.util.UtilConfig;
import lib.util.UtilEco;
import lib.util.UtilFormat;
import lib.util.UtilItems;
import lib.util.UtilPer;
import lib.util.UtilScoreboard;
import lib.util.UtilSpeed;
import lib.util.UtilTab;
import lib.util.UtilTypes;
import org.bukkit.Bukkit;
import org.bukkit.Server;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Animals;
import org.bukkit.entity.Monster;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.SkullMeta;
import ticket.Ticket;

public class PlayerInfoManager implements Listener {
   private static final String CHECK_SEE = "per.infos.see";
   private static Random r = new Random();
   private static final String CHECK_SPAWN_PER1 = "per.infos.spawn1";
   private static final String CHECK_SPAWN_PER2 = "per.infos.spawn2";
   private static final String SPEED_XQ = "xq";
   private static final String SPEED_SEE = "see";
   private static final int DAYMILLS = 86400000;
   private static final int DAY = 1440;
   private static final int HOUR = 60;
   private static final String FLAG_SUCK = "suck";
   private static final String MONEY_MODE = "money";
   private static final String POWER_MODE = "power";
   private Infos infos;
   private Dao dao;
   private Server server;
   private String pn;
   private Update update;
   private String per_infos_playerInfo_other;
   private static final String RIGHT = "emptyright";
   private boolean emptyRight;
   private int emptyRightInterval;
   private int interval;
   private int suckPower;
   private int deathPower;
   private int killPower;
   private HashMap costHash;
   private HashMap levelHash;
   private HashMap playerHash;
   private HashMap playerDayInfoHash;
   private HashList needUpdateList;
   private int xqInterval;
   private String xqPer;
   private int xqCost;
   private HashMap xqHash;
   private String sexMan;
   private String sexWoman;
   private int seeInterval;
   private HashList hideList;
   private int head;
   private int headChance;
   private double headTipRange;
   private String moneyPre;
   private int moneyPeriod;
   private int moneyStart;
   private int moneyInterval;
   private HashList moneyList = new HashListImpl();
   private Tab.Mode moneyMode;
   private HashMap moneyPlayerHash;
   private String powerPre;
   private int powerPeriod;
   private int powerStart;
   private int powerInterval;
   private HashList powerList = new HashListImpl();
   private Tab.Mode powerMode;
   private HashMap powerPlayerHash;

   public PlayerInfoManager(Infos infos) {
      super();
      this.infos = infos;
      this.dao = infos.getDao();
      this.server = infos.getServer();
      this.pn = Infos.getPn();
      this.needUpdateList = new HashListImpl();
      this.hideList = new HashListImpl();
      this.loadConfig(UtilConfig.getConfig(this.pn));
      this.server.getPluginManager().registerEvents(this, infos);
      this.update = new Update((Update)null);
      this.server.getScheduler().scheduleSyncDelayedTask(infos, this.update, (long)(this.interval * 1200));
      this.loadData();
      UtilSpeed.register(this.pn, "xq");
      UtilSpeed.register(this.pn, "see");
      UtilSpeed.register(this.pn, "emptyright");
      this.checkGold();
      this.checkPower();
      this.checkGuiZu();
      UtilTab.register("money");
      this.moneyPlayerHash = UtilTab.getMode("money");
      UtilTab.register("power");
      this.powerPlayerHash = UtilTab.getMode("power");
      this.checkMoneyPeriod();
      this.checkMoneyInterval();
      this.checkPowerPeriod();
      this.checkPowerInterval();
   }

   public void disable() {
      this.saveAll();
   }

   @EventHandler(
      priority = EventPriority.LOW
   )
   public void onReloadConfig(ReloadConfigEvent e) {
      if (e.getCallPlugin().equals(this.pn)) {
         this.loadConfig(e.getConfig());
      }

   }

   @EventHandler(
      priority = EventPriority.HIGHEST,
      ignoreCancelled = true
   )
   public void onPlayerInteractEntity(PlayerInteractEntityEvent e) {
      if (this.emptyRight && e.getRightClicked() instanceof Player) {
         ItemStack is = e.getPlayer().getItemInHand();
         if ((is == null || is.getTypeId() == 0) && UtilSpeed.check(e.getPlayer(), this.pn, "emptyright", this.emptyRightInterval)) {
            Player tarP = (Player)e.getRightClicked();
            e.getPlayer().chat("/cmd " + tarP.getName());
         }
      }

   }

   @EventHandler(
      priority = EventPriority.LOW
   )
   public void onPlayerJoin(PlayerJoinEvent e) {
      Player p = e.getPlayer();
      PlayerInfo pi = this.checkInit(p.getName());
      pi.setLastTime(System.currentTimeMillis());
      this.needUpdateList.add(p.getName());
      if (!UtilPer.hasPer(p, this.xqPer)) {
         this.setXq2(p, -1);
      } else {
         this.setSex(p, this.getSex(p));
      }

      if (!this.isSee(p)) {
         this.hideList.add(p);

         Player[] var7;
         for(Player tar : var7 = this.server.getOnlinePlayers()) {
            p.hidePlayer(tar);
         }
      }

      for(Player tar : this.hideList) {
         if (!p.getName().equals(tar.getName())) {
            tar.hidePlayer(p);
         }
      }

      if (this.moneyMode != null) {
         this.moneyPlayerHash.put(e.getPlayer(), this.moneyMode);
      }

      if (this.powerMode != null) {
         this.powerPlayerHash.put(e.getPlayer(), this.powerMode);
      }

   }

   @EventHandler(
      priority = EventPriority.LOWEST
   )
   public void onPlayerQuit(PlayerQuitEvent e) {
      PlayerInfo pi = this.checkInit(e.getPlayer().getName());
      pi.setLastTime(System.currentTimeMillis());
      this.hideList.remove(e.getPlayer());
      this.moneyPlayerHash.remove(e.getPlayer());
      this.powerPlayerHash.remove(e.getPlayer());
   }

   @EventHandler(
      priority = EventPriority.LOW
   )
   public void onTime(TimeEvent e) {
      if (TimeEvent.getTime() % (long)this.moneyPeriod == 0L) {
         this.checkMoneyPeriod();
      }

      if (TimeEvent.getTime() % (long)this.moneyInterval == 0L) {
         this.checkMoneyInterval();
      }

      if (TimeEvent.getTime() % (long)this.powerPeriod == 0L) {
         this.checkPowerPeriod();
      }

      if (TimeEvent.getTime() % (long)this.powerInterval == 0L) {
         this.checkPowerInterval();
      }

   }

   @EventHandler(
      priority = EventPriority.MONITOR,
      ignoreCancelled = true
   )
   public void onBlockBreak(BlockBreakEvent e) {
      try {
         PlayerInfo pi = this.checkInit(e.getPlayer().getName());
         if (UtilTypes.checkItem(this.pn, "safeBlocks", "" + e.getBlock().getTypeId())) {
            pi.setMineNum(pi.getMineNum() + 1);
         } else {
            pi.setBreakNum(pi.getBreakNum() + 1);
         }
      } catch (InvalidTypeException e1) {
         e1.printStackTrace();
      }

   }

   @EventHandler(
      priority = EventPriority.MONITOR,
      ignoreCancelled = true
   )
   public void onBlockPlace(BlockPlaceEvent e) {
      PlayerInfo pi = this.checkInit(e.getPlayer().getName());
      pi.setPlaceNum(pi.getPlaceNum() + 1);
   }

   @EventHandler(
      priority = EventPriority.MONITOR,
      ignoreCancelled = true
   )
   public void onEntityDeath(EntityDeathEvent e) {
      Player killer = e.getEntity().getKiller();
      if (killer != null) {
         String name = killer.getName();
         if (e.getEntity() instanceof Monster) {
            PlayerInfo pi = this.checkInit(name);
            pi.setKillMonsterNum(pi.getKillMonsterNum() + 1);
         } else if (e.getEntity() instanceof Animals) {
            PlayerInfo pi = this.checkInit(name);
            pi.setKillAnimalNum(pi.getKillAnimalNum() + 1);
         } else if (e.getEntity() instanceof Player) {
            PlayerInfo pi = this.checkInit(name);
            pi.setKillPlayerNum(pi.getKillPlayerNum() + 1);
         }
      }

   }

   @EventHandler(
      priority = EventPriority.MONITOR,
      ignoreCancelled = true
   )
   public void onPlayerDeath(PlayerDeathEvent e) {
      PlayerInfo pi = this.checkInit(e.getEntity().getName());
      pi.setDeath(pi.getDeath() + 1);
      int del = this.delPower(e.getEntity().getName(), this.deathPower);
      if (del > 0) {
         Land land = LandMain.getLandManager().getHighestPriorityLand(e.getEntity().getLocation());
         if (land != null && land.hasFlag("suck")) {
            int suck = del * this.suckPower / 100;
            if (suck > 0) {
               this.addPower(land.getOwner(), suck);
               Util.sendMsg(land.getOwner(), UtilFormat.format(this.pn, "suck", new Object[]{e.getEntity().getName(), land.getName(), suck}));
            }
         }
      }

      Player killer = e.getEntity().getKiller();
      if (killer != null) {
         int power = this.getPower(killer, e.getEntity());
         if (power > 0) {
            this.checkDropHead(e.getEntity(), power);
         }
      }

   }

   @EventHandler(
      priority = EventPriority.HIGHEST
   )
   public void onLandCreate(LandCreateEvent e) {
      if (e.getLand().getType() == 2) {
         Main.getLevelManager().addLevel((CommandSender)null, e.getLand().getOwner(), 154);
      }

   }

   @EventHandler(
      priority = EventPriority.HIGHEST
   )
   public void onOwnerChange(OwnerChangeEvent e) {
      if (e.getLand().getType() == 2) {
         Main.getLevelManager().addLevel((CommandSender)null, e.getLand().getOwner(), 154);
         boolean delete = true;
         HashList<Land> list = LandMain.getLandManager().getLandCheck().getUserLands(e.getOldOwner());
         if (list != null) {
            for(Land land : list) {
               if (land.getType() == 2) {
                  delete = false;
                  break;
               }
            }
         }

         if (delete) {
            Main.getLevelManager().delLevel((CommandSender)null, e.getOldOwner(), 154, true);
         }
      }

   }

   @EventHandler(
      priority = EventPriority.HIGHEST
   )
   public void onLandRemove(LandRemoveEvent e) {
      if (e.getRemovedLand().getType() == 2) {
         boolean delete = true;
         HashList<Land> list = LandMain.getLandManager().getLandCheck().getUserLands(e.getRemovedLand().getOwner());
         if (list != null) {
            for(Land land : list) {
               if (land.getType() == 2) {
                  delete = false;
                  break;
               }
            }
         }

         if (delete) {
            Main.getLevelManager().delLevel((CommandSender)null, e.getRemovedLand().getOwner(), 154, true);
         }
      }

   }

   public void toggleSpawnLoc(Player p) {
      int pos = this.getSpawnLoc(p);
      ++pos;
      if (pos >= 3) {
         pos = 0;
      }

      this.setSpawnLoc(p, pos);
   }

   public int getSpawnLoc(Player p) {
      if (UtilPer.hasPer(p, "per.infos.spawn1")) {
         return UtilPer.hasPer(p, "per.infos.spawn2") ? 0 : 1;
      } else {
         return UtilPer.hasPer(p, "per.infos.spawn2") ? 2 : 0;
      }
   }

   public void setSpawnLoc(Player p, int pos) {
      if (pos == 0) {
         UtilPer.remove(p, "per.infos.spawn1");
         UtilPer.remove(p, "per.infos.spawn2");
      } else if (pos == 1) {
         UtilPer.add(p, "per.infos.spawn1");
         UtilPer.remove(p, "per.infos.spawn2");
      } else {
         UtilPer.add(p, "per.infos.spawn2");
         UtilPer.remove(p, "per.infos.spawn1");
      }

      p.sendMessage(UtilFormat.format(this.pn, "setSpawnLoc", new Object[]{this.getSpawnLocShow(p)}));
   }

   public String getSpawnLocShow(Player p) {
      switch (this.getSpawnLoc(p)) {
         case 0:
            return this.get(455);
         case 1:
            return this.get(460);
         case 2:
            return this.get(465);
         default:
            return this.get(455);
      }
   }

   public String getShowPlayerInfo(String tar) {
      long now = System.currentTimeMillis();
      PlayerInfo pi = (PlayerInfo)this.playerHash.get(tar);
      if (pi == null) {
         return null;
      } else {
         int health = 0;
         int maxHealth = 0;
         String status = this.get(130);
         if (this.server.getPlayer(tar) == null) {
            status = this.get(135);
         } else {
            health = (int)this.server.getPlayer(tar).getHealth();
            maxHealth = (int)this.server.getPlayer(tar).getMaxHealth();
         }

         String joinTime = Util.getDateTime(new Date(pi.getJoinTime()), 0, 0, 0);
         String lastTime = Util.getDateTime(new Date(pi.getLastTime()), 0, 0, 0);
         String alive = this.get(140);
         String active = this.get(140);
         if (!pi.isAlive()) {
            alive = this.get(145);
         }

         if (!pi.isActive()) {
            active = this.get(145);
         }

         int day = pi.getOnlineTime() / 1440;
         int hour = pi.getOnlineTime() % 1440 / 60;
         int minute = pi.getOnlineTime() % 1440 % 60;
         int lastDay = (int)((now - pi.getJoinTime()) / 86400000L);
         if (lastDay <= 0) {
            lastDay = 1;
         }

         int avgHour = pi.getOnlineTime() / lastDay / 60;
         int avgMinute = pi.getOnlineTime() / lastDay % 60;
         String join = Infos.getJoin().getJoin(tar);
         if (join == null) {
            join = this.get(405);
         }

         String qq;
         if (pi.getQq() != null && !pi.getQq().isEmpty()) {
            qq = pi.getQq();
         } else {
            qq = this.get(520);
         }

         int activeTime = this.infos.getServerInfoManager().getActiveTime(pi.getName());
         int aliveTime = this.infos.getServerInfoManager().getAliveTime(pi.getName());
         String result = UtilFormat.format(this.pn, "showPlayerInfo", new Object[]{tar, health, maxHealth, status, joinTime, lastTime, active, alive, day, hour, minute, avgHour, avgMinute, pi.getMineNum(), pi.getBreakNum(), pi.getPlaceNum(), pi.getKillMonsterNum(), pi.getKillAnimalNum(), pi.getKillPlayerNum(), pi.getDeath(), pi.getPower(), join, Infos.getJoin().getJoinAmount(tar), Infos.getJoin().getMax(), qq, (int)UtilEco.get(tar), Ticket.getTicket(tar), this.infos.getServerInfoManager().getActiveDay(), this.infos.getServerInfoManager().getActiveTime(), activeTime, this.infos.getServerInfoManager().getAliveDay(), this.infos.getServerInfoManager().getAliveTime(), aliveTime});
         return result;
      }
   }

   public PlayerInfo getPlayerInfo(String name) {
      return (PlayerInfo)this.playerHash.get(name);
   }

   public int getPlayerAmount() {
      return this.playerHash.size();
   }

   public String getXqPer() {
      return this.xqPer;
   }

   public String getPer_infos_playerInfo_other() {
      return this.per_infos_playerInfo_other;
   }

   public void upgrade(Player p) {
      PlayerInfo pi = this.checkInit(p.getName());
      int level = pi.getLevel();
      if (level >= this.costHash.size()) {
         p.sendMessage(UtilFormat.format(this.pn, "fail", new Object[]{this.get(180)}));
      } else {
         int cost = (Integer)this.costHash.get(level + 1);
         if (UtilEco.get(p.getName()) < (double)cost) {
            p.sendMessage(UtilFormat.format(this.pn, "costErr", new Object[]{cost}));
         } else {
            UtilEco.del(p.getName(), (double)cost);
            p.sendMessage(UtilFormat.format(this.pn, "delGold", new Object[]{cost}));
            int id = (Integer)this.levelHash.get(level + 1);
            Main.getLevelManager().addLevel((CommandSender)null, p.getName(), id);
            pi.setLevel(level + 1);
            p.sendMessage(UtilFormat.format(this.pn, "upSuccess", new Object[]{pi.getLevel()}));
         }
      }
   }

   public int getLevel(String name) {
      PlayerInfo pi = this.checkInit(name);
      return pi.getLevel();
   }

   public int getNextCost(int level) {
      return !this.costHash.containsKey(level) ? -1 : (Integer)this.costHash.get(level);
   }

   public void addPower(String name, int amount) {
      PlayerInfo pi = this.checkInit(name);
      int result = pi.getPower() + amount;
      pi.setPower(result);
      Player p = this.server.getPlayerExact(name);
      if (p != null) {
         p.sendMessage(UtilFormat.format(this.pn, "powerAdd", new Object[]{amount}));
      }

   }

   public int delPower(String name, int amount) {
      PlayerInfo pi = this.checkInit(name);
      int result = pi.getPower() - amount;
      if (result < 0) {
         result = 0;
      }

      if (pi.getPower() != result) {
         amount = pi.getPower() - result;
         pi.setPower(result);
         Player p = this.server.getPlayerExact(name);
         if (p != null) {
            p.sendMessage(UtilFormat.format(this.pn, "powerDel", new Object[]{amount}));
         }

         return amount;
      } else {
         return 0;
      }
   }

   public PlayerInfo checkInit(String name) {
      PlayerInfo result = (PlayerInfo)this.playerHash.get(name);
      if (result == null) {
         result = new PlayerInfo(name, System.currentTimeMillis());
         this.playerHash.put(name, result);
         this.dao.addOrUpdatePlayerInfo(result);
      }

      return result;
   }

   public ItemStack getSexItem(Player p) {
      ItemStack result = UtilItems.getItem(this.pn, "set_sex").clone();
      ItemMeta im = result.getItemMeta();
      String sex = this.getSexShow(p);
      List<String> lore = im.getLore();
      lore.set(0, ((String)lore.get(0)).replace("{0}", sex));
      im.setLore(lore);
      result.setItemMeta(im);
      return result;
   }

   public ItemStack getXqItem(Player p) {
      ItemStack result = UtilItems.getItem(this.pn, "set_xq").clone();
      ItemMeta im = result.getItemMeta();
      String xq = this.getXq(p);
      if (xq == null) {
         xq = this.get(190);
      }

      List<String> lore = im.getLore();
      lore.set(0, ((String)lore.get(0)).replace("{0}", xq));
      lore.set(1, ((String)lore.get(1)).replace("{1}", String.valueOf(this.xqCost)));
      im.setLore(lore);
      result.setItemMeta(im);
      return result;
   }

   public int getSex(Player p) {
      PlayerInfo pi = this.checkInit(p.getName());
      String xq = pi.getXq();
      if (xq == null) {
         this.setSex(p, 0);
         xq = pi.getXq();
      }

      if (xq.substring(0, 5).equalsIgnoreCase(this.sexMan)) {
         return 0;
      } else if (xq.substring(0, 5).equalsIgnoreCase(this.sexWoman)) {
         return 1;
      } else {
         this.setSex(p, 0);
         return 0;
      }
   }

   public String getSexShow(Player p) {
      int sex = this.getSex(p);
      return sex == 0 ? this.sexMan : this.sexWoman;
   }

   public String getSexXqShow(Player p) {
      PlayerInfo pi = this.checkInit(p.getName());
      String result = pi.getXq();
      if (result == null) {
         result = "";
      }

      return result;
   }

   public boolean setXq(Player p, int pos) {
      if (!UtilPer.checkPer(p, this.xqPer)) {
         return false;
      } else if (pos != -1 && UtilEco.get(p.getName()) < (double)this.xqCost) {
         p.sendMessage(UtilFormat.format(this.pn, "fail", new Object[]{this.get(200)}));
         return true;
      } else {
         if (pos != -1) {
            UtilEco.del(p.getName(), (double)this.xqCost);
            p.sendMessage(UtilFormat.format(this.pn, "delGold", new Object[]{this.xqCost}));
         }

         this.setXq2(p, pos);
         return true;
      }
   }

   public int getXqInterval() {
      return this.xqInterval;
   }

   public HashMap getXqHash() {
      return this.xqHash;
   }

   public void toggleSex(Player p) {
      int sex = this.getSex(p);
      if (sex == 0) {
         this.setSex(p, 1);
      } else {
         this.setSex(p, 0);
      }

   }

   public String getSeeShow(Player p) {
      return this.isSee(p) ? this.get(165) : this.get(170);
   }

   public boolean toggleSee(Player p) {
      if (!UtilSpeed.check(p, this.pn, "see", this.seeInterval)) {
         return false;
      } else {
         this.setSee(p, !this.isSee(p));
         p.sendMessage(UtilFormat.format(this.pn, "setSee", new Object[]{this.getSeeShow(p)}));
         return true;
      }
   }

   public static String getSpeedXq() {
      return "xq";
   }

   public void bind(Player p, String qq, String confirm) {
      if (qq != null && !qq.isEmpty() && qq.equals(confirm) && qq.length() <= 20) {
         try {
            Long.parseLong(qq);
         } catch (NumberFormatException var5) {
            p.sendMessage(this.get(510));
            return;
         }

         PlayerInfo pi = this.checkInit(p.getName());
         if (pi.getQq() != null && !pi.getQq().isEmpty()) {
            p.sendMessage(this.get(515));
         } else {
            pi.setQq(qq);
            p.sendMessage(UtilFormat.format(this.pn, "bind", new Object[]{qq}));
         }
      } else {
         p.sendMessage(this.get(510));
      }
   }

   public void bindChange(Player p, String tar, String qq, String confirm) {
      if (p.isOp()) {
         if (qq != null && !qq.isEmpty() && qq.equals(confirm)) {
            tar = Util.getRealName(p, tar);
            if (tar != null) {
               PlayerInfo pi = this.checkInit(tar);
               pi.setQq(qq);
               p.sendMessage(UtilFormat.format(this.pn, "bind2", new Object[]{tar, qq}));
            }
         }
      }
   }

   private void checkMoneyPeriod() {
      this.moneyList.clear();

      for(String name : this.playerHash.keySet()) {
         if (UtilEco.get(name) >= (double)this.moneyStart) {
            this.moneyList.add(name);
         }
      }

   }

   private void checkMoneyInterval() {
      this.moneyPlayerHash.clear();
      List<String> rankList = new ArrayList();

      for(String name : this.moneyList) {
         this.checkAddMoney(rankList, name);
      }

      Iterator<String> it = rankList.iterator();

      while(it.hasNext()) {
         String name = (String)it.next();
         if (UtilEco.get(name) < (double)this.moneyStart) {
            it.remove();
         }
      }

      this.moneyMode = new Tab.Mode();
      int rank = 1;

      for(String name : rankList) {
         this.moneyMode.add(name, this.getMoneyTabName(name, rank++));
      }

      Player[] var7;
      for(Player p : var7 = Bukkit.getOnlinePlayers()) {
         this.moneyPlayerHash.put(p, this.moneyMode);
      }

   }

   private void checkPowerPeriod() {
      this.powerList.clear();

      for(PlayerInfo pi : this.playerHash.values()) {
         if (pi.getPower() >= this.powerStart) {
            this.powerList.add(pi.getName());
         }
      }

   }

   private void checkPowerInterval() {
      this.powerPlayerHash.clear();
      List<String> rankList = new ArrayList();

      for(String name : this.powerList) {
         this.checkAddPower(rankList, name);
      }

      Iterator<String> it = rankList.iterator();

      while(it.hasNext()) {
         String name = (String)it.next();
         if (this.checkInit(name).getPower() < this.powerStart) {
            it.remove();
         }
      }

      this.powerMode = new Tab.Mode();
      int rank = 1;

      for(String name : rankList) {
         this.powerMode.add(name, this.getPowerTabName(name, rank++));
      }

      Player[] var7;
      for(Player p : var7 = Bukkit.getOnlinePlayers()) {
         this.powerPlayerHash.put(p, this.powerMode);
      }

   }

   private void checkAddMoney(List list, String name) {
      if (list.isEmpty()) {
         list.add(name);
      } else {
         int value = (int)UtilEco.get(name);
         int index = 0;

         for(String tar : list) {
            int check = (int)UtilEco.get(tar);
            if (value > check) {
               list.add(index, name);
               if (list.size() > 10) {
                  list.remove(list.size() - 1);
               }

               return;
            }

            ++index;
         }

         if (list.size() < 10) {
            list.add(name);
         }

      }
   }

   private void checkAddPower(List list, String name) {
      if (list.isEmpty()) {
         list.add(name);
      } else {
         int value = this.checkInit(name).getPower();
         int index = 0;

         for(String tar : list) {
            int check = this.checkInit(tar).getPower();
            if (value > check) {
               list.add(index, name);
               if (list.size() > 10) {
                  list.remove(list.size() - 1);
               }

               return;
            }

            ++index;
         }

         if (list.size() < 10) {
            list.add(name);
         }

      }
   }

   private String getMoneyTabName(String name, int rank) {
      String result = this.moneyPre.replace("*", String.valueOf(rank)) + name;
      result = result.substring(0, Math.min(16, result.length()));
      return result;
   }

   private String getPowerTabName(String name, int rank) {
      String result = this.powerPre.replace("*", String.valueOf(rank)) + name;
      result = result.substring(0, Math.min(16, result.length()));
      return result;
   }

   private void checkDropHead(Player p, int power) {
      if (power >= this.head && r.nextInt(100) < this.headChance) {
         ItemStack head = new ItemStack(397, 1, (short)3);
         SkullMeta skull = (SkullMeta)head.getItemMeta();
         skull.setOwner(p.getName());
         head.setItemMeta(skull);
         p.getWorld().dropItemNaturally(p.getLocation(), head);
         String tip = UtilFormat.format(this.pn, "dropHead", new Object[]{p.getName()});
         Util.sendMsg(p.getLocation(), this.headTipRange, false, tip);
      }

   }

   private void setSee(Player p, boolean show) {
      if (show) {
         if (!this.isSee(p)) {
            UtilPer.remove(p, "per.infos.see");
            this.hideList.remove(p);

            Player[] var6;
            for(Player tar : var6 = this.server.getOnlinePlayers()) {
               p.showPlayer(tar);
            }
         }
      } else if (this.isSee(p)) {
         UtilPer.add(p, "per.infos.see");
         this.hideList.add(p);

         Player[] var10;
         for(Player tar : var10 = this.server.getOnlinePlayers()) {
            p.hidePlayer(tar);
         }
      }

   }

   private boolean isSee(Player p) {
      return !UtilPer.hasPer(p, "per.infos.see");
   }

   private String getXq(Player p) {
      PlayerInfo pi = this.checkInit(p.getName());
      String xq = pi.getXq();
      if (xq == null) {
         this.setSex(p, 0);
         return null;
      } else {
         return xq.length() > 5 ? xq.substring(5, xq.length()) : null;
      }
   }

   private void setXq2(Player p, int id) {
      PlayerInfo pi = this.checkInit(p.getName());
      ItemStack is = (ItemStack)this.xqHash.get(id);
      String xq = pi.getXq();
      if (xq == null) {
         this.setSex(p, 0);
         xq = pi.getXq();
      }

      if (is == null) {
         pi.setXq(xq.substring(0, 5));
      } else {
         ItemMeta im = is.getItemMeta();
         String s = im.getDisplayName();
         pi.setXq(xq.substring(0, 5) + s);
      }

      UtilScoreboard.setSuffix(p, pi.getXq());
   }

   private void setSex(Player p, int sex) {
      PlayerInfo pi = this.checkInit(p.getName());
      String xq = pi.getXq();
      String result;
      if (xq != null && xq.length() > 5) {
         String tail = xq.substring(5, xq.length());
         if (sex == 0) {
            result = this.sexMan + tail;
         } else {
            result = this.sexWoman + tail;
         }
      } else if (sex == 0) {
         result = this.sexMan;
      } else {
         result = this.sexWoman;
      }

      pi.setXq(result);
      UtilScoreboard.setSuffix(p, result);
   }

   private void checkGuiZu() {
      LevelManager lm = Main.getLevelManager();

      for(String name : this.playerHash.keySet()) {
         LevelUser lu = (LevelUser)lm.getUserHash().get(name);
         if (lu != null) {
            int has = (int)UtilEco.get(name);
            if (has >= 100000) {
               if (!lu.getLevelHash().containsKey(153)) {
                  lm.addLevel((CommandSender)null, name, 153);
               }
            } else if (lu.getLevelHash().containsKey(153)) {
               lm.delLevel((CommandSender)null, name, 153, true);
            }
         }
      }

   }

   private void checkPower() {
      int level1 = 0;
      int level2 = 0;
      int level3 = 0;
      String name1 = null;
      String name2 = null;
      String name3 = null;

      for(String name : this.playerHash.keySet()) {
         int power = ((PlayerInfo)this.playerHash.get(name)).getPower();
         if (power > level1) {
            level1 = power;
            name1 = name;
         }
      }

      if (name1 != null) {
         for(String name : this.playerHash.keySet()) {
            if (!name.equals(name1)) {
               int power = ((PlayerInfo)this.playerHash.get(name)).getPower();
               if (power > level2) {
                  level2 = power;
                  name2 = name;
               }
            }
         }

         if (name2 != null) {
            for(String name : this.playerHash.keySet()) {
               if (!name.equals(name1) && !name.equals(name2)) {
                  int power = ((PlayerInfo)this.playerHash.get(name)).getPower();
                  if (power > level3) {
                     level3 = power;
                     name3 = name;
                  }
               }
            }
         }
      }

      LevelManager lm = Main.getLevelManager();

      for(LevelUser lu : lm.getUserHash().values()) {
         if (lu.getLevelHash().containsKey(103) && (name1 == null || !lu.getName().equals(name1))) {
            lm.delLevel((CommandSender)null, lu.getName(), 103, true);
         }

         if (lu.getLevelHash().containsKey(104) && (name2 == null || !lu.getName().equals(name2))) {
            lm.delLevel((CommandSender)null, lu.getName(), 104, true);
         }

         if (lu.getLevelHash().containsKey(105) && (name3 == null || !lu.getName().equals(name3))) {
            lm.delLevel((CommandSender)null, lu.getName(), 105, true);
         }
      }

      if (name1 != null) {
         lm.addLevel((CommandSender)null, name1, 103);
      }

      if (name2 != null) {
         lm.addLevel((CommandSender)null, name2, 104);
      }

      if (name3 != null) {
         lm.addLevel((CommandSender)null, name3, 105);
      }

   }

   private void checkGold() {
      int level1 = 0;
      int level2 = 0;
      int level3 = 0;
      String name1 = null;
      String name2 = null;
      String name3 = null;

      for(String name : this.playerHash.keySet()) {
         int has = (int)UtilEco.get(name);
         if (has > level1) {
            level1 = has;
            name1 = name;
         }
      }

      if (name1 != null) {
         for(String name : this.playerHash.keySet()) {
            if (!name.equals(name1)) {
               int has = (int)UtilEco.get(name);
               if (has > level2) {
                  level2 = has;
                  name2 = name;
               }
            }
         }

         if (name2 != null) {
            for(String name : this.playerHash.keySet()) {
               if (!name.equals(name1) && !name.equals(name2)) {
                  int has = (int)UtilEco.get(name);
                  if (has > level3) {
                     level3 = has;
                     name3 = name;
                  }
               }
            }
         }
      }

      LevelManager lm = Main.getLevelManager();

      for(LevelUser lu : lm.getUserHash().values()) {
         if (lu.getLevelHash().containsKey(100) && (name1 == null || !lu.getName().equals(name1))) {
            lm.delLevel((CommandSender)null, lu.getName(), 100, true);
         }

         if (lu.getLevelHash().containsKey(101) && (name2 == null || !lu.getName().equals(name2))) {
            lm.delLevel((CommandSender)null, lu.getName(), 101, true);
         }

         if (lu.getLevelHash().containsKey(102) && (name3 == null || !lu.getName().equals(name3))) {
            lm.delLevel((CommandSender)null, lu.getName(), 102, true);
         }
      }

      if (name1 != null) {
         lm.addLevel((CommandSender)null, name1, 100);
      }

      if (name2 != null) {
         lm.addLevel((CommandSender)null, name2, 101);
      }

      if (name3 != null) {
         lm.addLevel((CommandSender)null, name3, 102);
      }

   }

   private int getPower(Player killer, Player entity) {
      PlayerInfo pi = this.checkInit(entity.getName());
      if (pi.getPower() > 0) {
         PlayerInfo pi2 = this.checkInit(killer.getName());
         int get = Math.min(this.killPower, pi.getPower());
         pi.setPower(pi.getPower() - get);
         pi2.setPower(pi2.getPower() + get);
         killer.sendMessage(UtilFormat.format(this.pn, "powerAdd2", new Object[]{get}));
         entity.sendMessage(UtilFormat.format(this.pn, "powerDel2", new Object[]{get}));
         return get;
      } else {
         return 0;
      }
   }

   private void saveAll() {
      this.dao.updatePlayerInfos(this.needUpdateList, this.playerHash);
      this.needUpdateList.clear();

      Player[] var4;
      for(Player p : var4 = this.server.getOnlinePlayers()) {
         this.needUpdateList.add(p.getName());
      }

   }

   private void loadData() {
      this.playerHash = new HashMap();

      for(PlayerInfo pi : this.dao.getAllPlayerInfos()) {
         this.playerHash.put(pi.getName(), pi);
      }

      this.playerDayInfoHash = new HashMap();

      for(PlayerDayInfo pdi : this.dao.getAllPlayerDayInfos()) {
         if (!this.playerDayInfoHash.containsKey(pdi.getName())) {
            this.playerDayInfoHash.put(pdi.getName(), new HashMap());
         }

         ((HashMap)this.playerDayInfoHash.get(pdi.getName())).put(pdi.getTime(), pdi);
      }

   }

   private void loadConfig(YamlConfiguration config) {
      this.per_infos_playerInfo_other = config.getString("per_infos_playerInfo_other");
      this.emptyRight = config.getBoolean("playerInfo.emptyRight");
      this.emptyRightInterval = config.getInt("playerInfo.emptyRightInterval");
      this.interval = config.getInt("playerInfo.interval");
      this.suckPower = config.getInt("playerInfo.power.suck");
      this.deathPower = config.getInt("playerInfo.power.death");
      this.killPower = config.getInt("playerInfo.power.kill");
      this.costHash = new HashMap();
      this.levelHash = new HashMap();

      for(String s : config.getStringList("upgrade.levels")) {
         int level = Integer.parseInt(s.split(" ")[0]);
         int cost = Integer.parseInt(s.split(" ")[1]);
         int id = Integer.parseInt(s.split(" ")[2]);
         this.costHash.put(level, cost);
         this.levelHash.put(level, id);
      }

      this.xqInterval = config.getInt("xq.interval");
      this.xqPer = config.getString("xq.per");
      this.xqCost = config.getInt("xq.cost");
      HashList<String> tempList = new HashListImpl();
      InputStreamReader isr = null;
      BufferedReader br = null;

      try {
         File file = new File(this.infos.getPluginPath() + File.separator + this.pn + File.separator + "xq.ini");
         isr = new InputStreamReader(new FileInputStream(file), "UTF-8");
         br = new BufferedReader(isr);
         StringBuffer sb = new StringBuffer();

         String line;
         while((line = br.readLine()) != null) {
            sb.append(line + "\n");
         }

         String s = sb.toString();
         if (s != null) {
            int index = 0;

            String[] var13;
            for(String ss : var13 = s.split("\n")) {
               ss = Util.convert(ss);
               if (index == 0) {
                  this.sexMan = ss;
               } else if (index == 1) {
                  this.sexWoman = ss;
               } else {
                  tempList.add(ss.substring(0, Math.min(11, ss.length())));
               }

               ++index;
            }
         }
      } catch (FileNotFoundException e) {
         e.printStackTrace();
      } catch (IOException e) {
         e.printStackTrace();
      } finally {
         try {
            if (isr != null) {
               isr.close();
            }

            if (br != null) {
               br.close();
            }
         } catch (IOException e) {
            e.printStackTrace();
         }

      }

      this.xqHash = new HashMap();
      int index = 0;

      for(String xq : tempList) {
         ItemStack is = UtilItems.getItem(this.pn, "xq").clone();
         ItemMeta im = is.getItemMeta();
         im.setDisplayName(xq);
         is.setItemMeta(im);
         this.xqHash.put(index++, is);
         if (index >= 45) {
            break;
         }
      }

      this.seeInterval = config.getInt("item.seeInterval");
      this.head = config.getInt("playerInfo.power.head");
      this.headChance = config.getInt("playerInfo.power.headChance");
      this.headTipRange = config.getDouble("playerInfo.power.headTipRange");
      this.moneyPre = Util.convert(config.getString("tab.money.pre"));
      this.moneyPeriod = config.getInt("tab.money.period");
      this.moneyStart = config.getInt("tab.money.start");
      this.moneyInterval = config.getInt("tab.money.interval");
      this.powerPre = Util.convert(config.getString("tab.power.pre"));
      this.powerPeriod = config.getInt("tab.power.period");
      this.powerStart = config.getInt("tab.power.start");
      this.powerInterval = config.getInt("tab.power.interval");
   }

   private String get(int id) {
      return UtilFormat.format(this.pn, id);
   }

   private class Update implements Runnable {
      private Update() {
         super();
      }

      public void run() {
         PlayerInfoManager.this.server.getScheduler().scheduleSyncDelayedTask(PlayerInfoManager.this.infos, PlayerInfoManager.this.update, (long)(PlayerInfoManager.this.interval * 1200));

         Player[] var4;
         for(Player p : var4 = PlayerInfoManager.this.server.getOnlinePlayers()) {
            PlayerInfo pi = PlayerInfoManager.this.checkInit(p.getName());
            pi.setOnlineTime(pi.getOnlineTime() + PlayerInfoManager.this.interval);
         }

         long nowDay = System.currentTimeMillis() / 86400000L;
         HashList<PlayerDayInfo> result = new HashListImpl();

         for(String s : PlayerInfoManager.this.needUpdateList) {
            HashMap<Long, PlayerDayInfo> hash = (HashMap)PlayerInfoManager.this.playerDayInfoHash.get(s);
            if (hash == null) {
               hash = new HashMap();
               PlayerInfoManager.this.playerDayInfoHash.put(s, hash);
            }

            PlayerDayInfo pdi = (PlayerDayInfo)hash.get(nowDay);
            if (pdi == null) {
               pdi = new PlayerDayInfo(s, nowDay);
               hash.put(nowDay, pdi);
            }

            pdi.setOnlineTime(pdi.getOnlineTime() + PlayerInfoManager.this.interval);
            result.add(pdi);
         }

         PlayerInfoManager.this.dao.addOrSavePlayerDayInfos(result);
         PlayerInfoManager.this.saveAll();
      }

      // $FF: synthetic method
      Update(Update var2) {
         this();
      }
   }
}
