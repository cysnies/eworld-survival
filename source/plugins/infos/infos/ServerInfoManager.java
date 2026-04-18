package infos;

import java.util.Date;
import java.util.HashMap;
import level.Main;
import lib.config.ReloadConfigEvent;
import lib.types.InvalidTypeException;
import lib.util.Util;
import lib.util.UtilConfig;
import lib.util.UtilFormat;
import lib.util.UtilTypes;
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
import org.bukkit.event.player.PlayerJoinEvent;

public class ServerInfoManager implements Listener {
   private static final int DAY = 1440;
   private Infos infos;
   private Server server;
   private String pn;
   private Dao dao;
   private int clearDay;
   private int aliveDay;
   private int aliveTime;
   private int activeDay;
   private int activeTime;
   private int interval;
   private int delay;
   private Update update;
   private ServerTotalInfo serverTotalInfo;
   private HashMap activeHash = new HashMap();
   private HashMap aliveHash = new HashMap();

   public ServerInfoManager(Infos infos) {
      super();
      this.infos = infos;
      this.server = infos.getServer();
      this.pn = Infos.getPn();
      this.dao = infos.getDao();
      this.loadConfig(UtilConfig.getConfig(this.pn));
      this.server.getPluginManager().registerEvents(this, infos);
      this.loadData();
      this.update = new Update((Update)null);
      this.server.getScheduler().scheduleSyncDelayedTask(infos, this.update, (long)(this.interval * 1200));
      this.server.getScheduler().scheduleSyncDelayedTask(infos, new DelayTask((DelayTask)null), (long)(this.delay * 20));
   }

   public void disable() {
      this.update();
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
      priority = EventPriority.NORMAL
   )
   public void onPlayerJoin(PlayerJoinEvent e) {
      this.serverTotalInfo.setTotalJoinTimes(this.serverTotalInfo.getTotalJoinTimes() + 1L);
      this.serverTotalInfo.setTotalPlayers(Infos.getPlayerInfoManager().getPlayerAmount());
      int amount = this.server.getOnlinePlayers().length;
      if (this.serverTotalInfo.getMaxOnline() < amount) {
         this.serverTotalInfo.setMaxOnline(amount);
      }

   }

   @EventHandler(
      priority = EventPriority.MONITOR,
      ignoreCancelled = true
   )
   public void onBlockBreak(BlockBreakEvent e) {
      try {
         if (UtilTypes.checkItem(this.pn, "safeBlocks", "" + e.getBlock().getTypeId())) {
            this.serverTotalInfo.setTotalMines(this.serverTotalInfo.getTotalMines() + 1L);
         } else {
            this.serverTotalInfo.setTotalBreaks(this.serverTotalInfo.getTotalBreaks() + 1L);
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
      this.serverTotalInfo.setTotalPlaces(this.serverTotalInfo.getTotalPlaces() + 1L);
   }

   @EventHandler(
      priority = EventPriority.MONITOR,
      ignoreCancelled = true
   )
   public void onEntityDeath(EntityDeathEvent e) {
      Player killer = e.getEntity().getKiller();
      if (killer != null) {
         if (e.getEntity() instanceof Monster) {
            this.serverTotalInfo.setTotalKillMonsters(this.serverTotalInfo.getTotalKillMonsters() + 1L);
         } else if (e.getEntity() instanceof Animals) {
            this.serverTotalInfo.setTotalKillAnimals(this.serverTotalInfo.getTotalKillAnimals() + 1L);
         } else if (e.getEntity() instanceof Player) {
            this.serverTotalInfo.setTotalKills(this.serverTotalInfo.getTotalKills() + 1L);
         }
      }

   }

   @EventHandler(
      priority = EventPriority.MONITOR,
      ignoreCancelled = true
   )
   public void onPlayerDeath(PlayerDeathEvent e) {
      this.serverTotalInfo.setTotalDeaths(this.serverTotalInfo.getTotalDeaths() + 1L);
   }

   public String getShowInfo() {
      String startTime = Util.getDateTime(new Date(this.serverTotalInfo.getStartTime()), 0, 0, 0);
      int day = (int)(this.serverTotalInfo.getOpenTime() / 1440L);
      int hour = (int)(this.serverTotalInfo.getOpenTime() % 1440L / 60L);
      int minute = (int)(this.serverTotalInfo.getOpenTime() % 1440L % 60L);
      String result = UtilFormat.format(this.pn, "showServerInfo", new Object[]{startTime, day, hour, minute, this.serverTotalInfo.getMaxOnline(), this.serverTotalInfo.getTotalPlayers(), this.serverTotalInfo.getTotalJoinTimes(), this.serverTotalInfo.getActiveAmounts(), this.serverTotalInfo.getAliveAmounts(), this.serverTotalInfo.getTotalOnlineTime(), this.serverTotalInfo.getTotalMines(), this.serverTotalInfo.getTotalBreaks(), this.serverTotalInfo.getTotalPlaces(), this.serverTotalInfo.getTotalKillMonsters(), this.serverTotalInfo.getTotalKillAnimals(), this.serverTotalInfo.getTotalKills(), this.serverTotalInfo.getTotalDeaths()});
      return result;
   }

   private void loadConfig(YamlConfiguration config) {
      this.interval = config.getInt("serverInfo.interval");
      this.delay = config.getInt("serverInfo.delay");
      this.clearDay = config.getInt("serverInfo.clear.day");
      this.aliveDay = config.getInt("serverInfo.alive.day");
      this.aliveTime = config.getInt("serverInfo.alive.time");
      this.activeDay = config.getInt("serverInfo.active.day");
      this.activeTime = config.getInt("serverInfo.active.time");
   }

   private void loadData() {
      this.serverTotalInfo = this.dao.getServerTotalInfo();
      if (this.serverTotalInfo == null) {
         this.serverTotalInfo = new ServerTotalInfo();
         this.serverTotalInfo.setStartTime(System.currentTimeMillis());
         this.dao.addOrUpdateServerTotalInfo(this.serverTotalInfo);
      }

   }

   private void update() {
      this.dao.addOrUpdateServerTotalInfo(this.serverTotalInfo);
   }

   public int getAliveDay() {
      return this.aliveDay;
   }

   public int getAliveTime() {
      return this.aliveTime;
   }

   public int getActiveDay() {
      return this.activeDay;
   }

   public int getActiveTime() {
      return this.activeTime;
   }

   public int getActiveTime(String name) {
      return this.activeHash.containsKey(name) ? (Integer)this.activeHash.get(name) / 60 : -1;
   }

   public int getAliveTime(String name) {
      return this.aliveHash.containsKey(name) ? (Integer)this.aliveHash.get(name) / 60 : -1;
   }

   private class Update implements Runnable {
      private Update() {
         super();
      }

      public void run() {
         ServerInfoManager.this.server.getScheduler().scheduleSyncDelayedTask(ServerInfoManager.this.infos, ServerInfoManager.this.update, (long)(ServerInfoManager.this.interval * 1200));
         ServerInfoManager.this.serverTotalInfo.setOpenTime(ServerInfoManager.this.serverTotalInfo.getOpenTime() + (long)ServerInfoManager.this.interval);
         int amount = ServerInfoManager.this.server.getOnlinePlayers().length;
         if (amount > 0) {
            ServerInfoManager.this.serverTotalInfo.setTotalOnlineTime(ServerInfoManager.this.serverTotalInfo.getTotalOnlineTime() + (long)(amount * ServerInfoManager.this.interval));
         }

         ServerInfoManager.this.update();
      }

      // $FF: synthetic method
      Update(Update var2) {
         this();
      }
   }

   private class DelayTask implements Runnable {
      private DelayTask() {
         super();
      }

      public void run() {
         ServerInfoManager.this.dao.fixAllPlayerDayInfos();
         ServerInfoManager.this.dao.clearDayInfo(ServerInfoManager.this.clearDay);
         int aliveAmount = 0;

         for(PlayerDayInfo playerDayInfo : ServerInfoManager.this.dao.getAllPlayerDayInfos(ServerInfoManager.this.aliveDay)) {
            if (!ServerInfoManager.this.aliveHash.containsKey(playerDayInfo.getName())) {
               ServerInfoManager.this.aliveHash.put(playerDayInfo.getName(), 0);
            }

            ServerInfoManager.this.aliveHash.put(playerDayInfo.getName(), (Integer)ServerInfoManager.this.aliveHash.get(playerDayInfo.getName()) + playerDayInfo.getOnlineTime());
         }

         int checkTime = ServerInfoManager.this.aliveTime * 60;

         for(String s : ServerInfoManager.this.aliveHash.keySet()) {
            PlayerInfo pi = Infos.getPlayerInfoManager().getPlayerInfo(s);
            if ((Integer)ServerInfoManager.this.aliveHash.get(s) > checkTime) {
               ++aliveAmount;
               if (pi != null) {
                  pi.setAlive(true);
                  Main.getLevelManager().addLevel((CommandSender)null, pi.getName(), 156);
               }
            } else if (pi != null) {
               pi.setAlive(false);
               Main.getLevelManager().delLevel((CommandSender)null, pi.getName(), 156, true);
            }
         }

         ServerInfoManager.this.serverTotalInfo.setAliveAmounts(aliveAmount);
         int activeAmount = 0;

         for(PlayerDayInfo playerDayInfo : ServerInfoManager.this.dao.getAllPlayerDayInfos(ServerInfoManager.this.activeDay)) {
            if (!ServerInfoManager.this.activeHash.containsKey(playerDayInfo.getName())) {
               ServerInfoManager.this.activeHash.put(playerDayInfo.getName(), 0);
            }

            ServerInfoManager.this.activeHash.put(playerDayInfo.getName(), (Integer)ServerInfoManager.this.activeHash.get(playerDayInfo.getName()) + playerDayInfo.getOnlineTime());
         }

         checkTime = ServerInfoManager.this.activeTime * 60;

         for(String s : ServerInfoManager.this.activeHash.keySet()) {
            PlayerInfo pi = Infos.getPlayerInfoManager().getPlayerInfo(s);
            if ((Integer)ServerInfoManager.this.activeHash.get(s) > checkTime) {
               ++activeAmount;
               if (pi != null) {
                  pi.setActive(true);
                  Main.getLevelManager().addLevel((CommandSender)null, pi.getName(), 155);
               }
            } else if (pi != null) {
               pi.setActive(false);
               Main.getLevelManager().delLevel((CommandSender)null, pi.getName(), 155, true);
            }
         }

         ServerInfoManager.this.serverTotalInfo.setActiveAmounts(activeAmount);
         ServerInfoManager.this.update();
      }

      // $FF: synthetic method
      DelayTask(DelayTask var2) {
         this();
      }
   }
}
