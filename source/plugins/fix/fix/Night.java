package fix;

import java.util.Random;
import lib.config.ReloadConfigEvent;
import lib.hashList.ChanceHashList;
import lib.hashList.ChanceHashListImpl;
import lib.hashList.HashList;
import lib.hashList.HashListImpl;
import lib.time.DayChangeEvent;
import lib.time.DayChangeEvent.State;
import lib.util.Util;
import lib.util.UtilConfig;
import lib.util.UtilFormat;
import lib.util.UtilPer;
import org.bukkit.Server;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Monster;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.bukkit.event.entity.CreatureSpawnEvent.SpawnReason;

public class Night implements Listener {
   private static final String LIB = "lib";
   private Random r = new Random();
   private Fix main;
   private String pn;
   private Server server;
   private boolean tipDayChange;
   private boolean dayBlack;
   private CheckMonster checkMonster;
   private String per_fix_banNight;
   private HashList worlds;
   private int interval;
   private int playerBornChance;
   private int playerBornDark;
   private ChanceHashList playerBornMonsterHash;
   private int nearBornChance;
   private int nearBornDark;
   private int nearBornMinDistance;
   private int nearBornMaxDistance;
   private ChanceHashList nearBornMonsterHash;

   public Night(Fix fix) {
      super();
      this.main = fix;
      this.pn = fix.getPn();
      this.server = fix.getServer();
      this.checkMonster = new CheckMonster();
      this.loadConfig(UtilConfig.getConfig(this.pn));
      fix.getPm().registerEvents(this, fix);
      this.checkMonster = new CheckMonster();
      if (this.interval > 0) {
         this.server.getScheduler().runTaskLater(this.main, this.checkMonster, (long)(this.interval * 20));
      }

   }

   @EventHandler(
      priority = EventPriority.LOW
   )
   public void reloadConfig(ReloadConfigEvent e) {
      if (e.getCallPlugin().equals(this.pn)) {
         this.loadConfig(e.getConfig());
      }

   }

   @EventHandler(
      priority = EventPriority.LOW
   )
   public void onDayChange(DayChangeEvent e) {
      if (this.tipDayChange) {
         if (e.getTo().equals(State.day)) {
            String tip = this.get(300);

            for(Player p : e.getW().getPlayers()) {
               p.sendMessage(tip);
            }
         } else {
            String tip = this.get(305);

            for(Player p : e.getW().getPlayers()) {
               p.sendMessage(tip);
            }
         }
      }

   }

   @EventHandler(
      priority = EventPriority.LOW,
      ignoreCancelled = true
   )
   public void onCreatureSpawn(CreatureSpawnEvent e) {
      if (this.dayBlack && (e.getSpawnReason().equals(SpawnReason.DEFAULT) || e.getSpawnReason().equals(SpawnReason.NATURAL)) && e.getEntity() instanceof Monster && Util.isDay(e.getEntity().getWorld())) {
         e.setCancelled(true);
      }

   }

   private void loadConfig(YamlConfiguration config) {
      this.tipDayChange = config.getBoolean("tipDayChange");
      this.dayBlack = config.getBoolean("dayBlack");
      this.per_fix_banNight = config.getString("per_fix_banNight");
      this.worlds = new HashListImpl();

      for(String s : config.getStringList("night.worlds")) {
         this.worlds.add(s);
      }

      this.interval = config.getInt("night.interval");
      this.playerBornChance = config.getInt("night.playerBorn.chance");
      this.playerBornDark = config.getInt("night.playerBorn.dark");
      this.playerBornMonsterHash = new ChanceHashListImpl();

      for(String s : config.getStringList("night.playerBorn.monster")) {
         int id = Integer.parseInt(s.split(" ")[0]);
         int chance = Integer.parseInt(s.split(" ")[1]);
         this.playerBornMonsterHash.addChance(id, chance);
      }

      this.nearBornChance = config.getInt("night.nearBorn.chance");
      this.nearBornDark = config.getInt("night.nearBorn.dark");
      this.nearBornMinDistance = config.getInt("night.nearBorn.minDistance");
      this.nearBornMaxDistance = config.getInt("night.nearBorn.maxDistance");
      this.nearBornMonsterHash = new ChanceHashListImpl();

      for(String s : config.getStringList("night.nearBorn.monster")) {
         int id = Integer.parseInt(s.split(" ")[0]);
         int chance = Integer.parseInt(s.split(" ")[1]);
         this.nearBornMonsterHash.addChance(id, chance);
      }

   }

   private String get(int id) {
      return UtilFormat.format(this.pn, id);
   }

   class CheckMonster implements Runnable {
      CheckMonster() {
         super();
      }

      public void run() {
         for(String s : Night.this.worlds) {
            World w = Night.this.server.getWorld(s);
            if (w != null && !Util.isDay(w)) {
               for(Player p : w.getPlayers()) {
                  if (!UtilPer.hasPer(p, Night.this.per_fix_banNight)) {
                     if (Night.this.r.nextInt(100) < Night.this.playerBornChance && p.getLocation().getBlock().getLightLevel() <= Night.this.playerBornDark) {
                        p.getWorld().spawnEntity(p.getLocation(), EntityType.fromId((Integer)Night.this.playerBornMonsterHash.getRandom()));
                        p.sendMessage(UtilFormat.format("lib", "fail", new Object[]{Night.this.get(310)}));
                     } else if (Night.this.r.nextInt(100) < Night.this.nearBornChance) {
                        int xdistance = Night.this.r.nextInt(Night.this.nearBornMaxDistance - Night.this.nearBornMinDistance + 1) + Night.this.nearBornMinDistance;
                        if (Night.this.r.nextInt(2) == 1) {
                           xdistance = -xdistance;
                        }

                        int zdistance = Night.this.r.nextInt(Night.this.nearBornMaxDistance - Night.this.nearBornMinDistance + 1) + Night.this.nearBornMinDistance;
                        if (Night.this.r.nextInt(2) == 0) {
                           zdistance = -zdistance;
                        }

                        Block bornLoc;
                        if ((bornLoc = p.getWorld().getHighestBlockAt(p.getLocation().add((double)xdistance, (double)0.0F, (double)zdistance))).getLightLevel() <= Night.this.nearBornDark) {
                           bornLoc.getWorld().spawnEntity(bornLoc.getLocation(), EntityType.fromId((Integer)Night.this.nearBornMonsterHash.getRandom()));
                        }
                     }
                  }
               }
            }
         }

         if (Night.this.interval > 0) {
            Night.this.server.getScheduler().runTaskLater(Night.this.main, Night.this.checkMonster, (long)(Night.this.interval * 20));
         }

      }
   }
}
