package fix;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Random;
import lib.Eco;
import lib.config.ReloadConfigEvent;
import lib.hashList.HashList;
import lib.hashList.HashListImpl;
import lib.time.TimeEvent;
import lib.util.UtilConfig;
import lib.util.UtilFormat;
import lib.util.UtilNames;
import org.bukkit.Location;
import org.bukkit.Server;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Animals;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockGrowEvent;
import org.bukkit.event.block.BlockSpreadEvent;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.bukkit.event.entity.CreatureSpawnEvent.SpawnReason;
import org.bukkit.event.player.PlayerEggThrowEvent;
import org.bukkit.event.player.PlayerFishEvent;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.inventory.ItemStack;

public class Season implements Listener {
   private static final String LIB = "lib";
   private static List bf = new ArrayList();
   private Random r = new Random();
   private Fix fix;
   private Server server;
   private Eco eco;
   private String pn;
   private boolean enableSeason;
   private int sugarCaneFix;
   private int cocoFix;
   private int eggFix;
   private boolean cancelMushroomCow;
   private int failTipDistance;
   private HashMap chances;
   private int interval;
   private int giftBase;
   private int giftAdd;
   private HashMap seasonInfoHash;
   private int year;
   private int season;
   private int next;

   static {
      bf.add(BlockFace.EAST);
      bf.add(BlockFace.SOUTH);
      bf.add(BlockFace.WEST);
      bf.add(BlockFace.NORTH);
      bf.add(BlockFace.EAST_NORTH_EAST);
      bf.add(BlockFace.EAST_SOUTH_EAST);
      bf.add(BlockFace.WEST_NORTH_WEST);
      bf.add(BlockFace.WEST_SOUTH_WEST);
   }

   public Season(Fix fix) {
      super();
      this.fix = fix;
      this.server = fix.getServer();
      this.pn = fix.getPn();
      this.loadConfig(UtilConfig.getConfig(this.pn));
      fix.getPm().registerEvents(this, fix);
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
   public void onTime(TimeEvent e) {
      if (this.enableSeason && TimeEvent.getTime() % 60L == 0L) {
         --this.next;
         if (this.next <= 0) {
            this.next = this.interval;
            ++this.season;
            if (this.season >= this.seasonInfoHash.size()) {
               this.season = 0;
            }

            this.server.broadcastMessage(UtilFormat.format(this.pn, "season1", new Object[]{((SeasonInfo)this.seasonInfoHash.get(this.season)).getName(), this.get(100 + this.season)}));
            if (this.season == 0) {
               ++this.year;
               this.server.broadcastMessage(UtilFormat.format(this.pn, "year1", new Object[]{this.year}));

               Player[] var6;
               for(Player p : var6 = this.server.getOnlinePlayers()) {
                  int get = this.r.nextInt(this.giftAdd) + this.giftBase;
                  this.eco.add(p, get);
                  p.sendMessage(UtilFormat.format(this.pn, "year2", new Object[]{get}));
               }
            }
         }

         this.saveTime();
      }
   }

   @EventHandler(
      priority = EventPriority.LOW,
      ignoreCancelled = true
   )
   public void onBlockGrow(BlockGrowEvent e) {
      if (this.enableSeason) {
         if (this.season == 3) {
            e.setCancelled(true);
         } else {
            int id;
            switch (e.getNewState().getTypeId()) {
               case 59:
               case 141:
               case 142:
                  if (!((SeasonInfo)this.seasonInfoHash.get(this.season)).getCrop1().has(e.getNewState().getRawData())) {
                     e.setCancelled(true);
                  }

                  return;
               case 83:
                  if (this.r.nextInt(this.sugarCaneFix) >= 1) {
                     e.setCancelled(true);
                  }

                  return;
               case 86:
                  id = 104;
                  break;
               case 103:
                  id = 105;
                  break;
               case 104:
               case 105:
                  if (!((SeasonInfo)this.seasonInfoHash.get(this.season)).getCrop2().has(e.getNewState().getRawData())) {
                     e.setCancelled(true);
                  }

                  return;
               case 127:
                  if (this.r.nextInt(this.cocoFix) >= 1) {
                     e.setCancelled(true);
                  }

                  return;
               default:
                  return;
            }

            switch (this.season) {
               case 1:
                  Block b = e.getBlock();

                  for(BlockFace blockFace : bf) {
                     if (b.getRelative(blockFace).getTypeId() == id) {
                        b.getRelative(blockFace).setTypeId(0);
                        b.getRelative(blockFace).getRelative(BlockFace.DOWN).setTypeId(2);
                        return;
                     }
                  }
                  break;
               default:
                  e.setCancelled(true);
            }

         }
      }
   }

   @EventHandler(
      priority = EventPriority.LOW,
      ignoreCancelled = true
   )
   public void onBlockSpread(BlockSpreadEvent e) {
      if (this.enableSeason) {
         if (this.season == 3) {
            int id = e.getNewState().getTypeId();
            if (id == 39 || id == 40) {
               e.setCancelled(true);
            }

         }
      }
   }

   @EventHandler(
      priority = EventPriority.LOW,
      ignoreCancelled = true
   )
   public void onPlayerInteractEntity(PlayerInteractEntityEvent e) {
      if (this.enableSeason) {
         if (this.season == 3 && e.getRightClicked() instanceof Animals) {
            e.setCancelled(true);
            e.getPlayer().sendMessage(UtilFormat.format("lib", "fail", new Object[]{this.get(115)}));
         }

      }
   }

   @EventHandler(
      priority = EventPriority.LOW,
      ignoreCancelled = true
   )
   public void onPlayerFish(PlayerFishEvent e) {
      if (this.enableSeason) {
         if (this.season == 3) {
            e.setCancelled(true);
            e.getPlayer().sendMessage(UtilFormat.format("lib", "fail", new Object[]{this.get(120)}));
         }

      }
   }

   @EventHandler(
      priority = EventPriority.LOW,
      ignoreCancelled = true
   )
   public void onPlayerEggThrow(PlayerEggThrowEvent e) {
      if (this.enableSeason) {
         if (this.season == 3) {
            e.setHatching(false);
            e.getPlayer().sendMessage(UtilFormat.format("lib", "fail", new Object[]{this.get(125)}));
         }

      }
   }

   @EventHandler(
      priority = EventPriority.LOW,
      ignoreCancelled = true
   )
   public void onPlayerInteract(PlayerInteractEvent e) {
      if (this.enableSeason) {
         if (this.season == 3 && e.getAction().equals(Action.RIGHT_CLICK_BLOCK)) {
            ItemStack is = e.getPlayer().getItemInHand();
            if (is != null && is.getTypeId() == 383 && is.getDurability() >= 90) {
               e.setCancelled(true);
               e.getPlayer().sendMessage(UtilFormat.format("lib", "fail", new Object[]{this.get(125)}));
            }
         }

      }
   }

   @EventHandler(
      priority = EventPriority.LOW,
      ignoreCancelled = true
   )
   public void onCreatureSpawn(CreatureSpawnEvent e) {
      if (this.enableSeason) {
         if (e.getSpawnReason().equals(SpawnReason.BREEDING)) {
            int id;
            try {
               id = e.getEntityType().getTypeId();
            } catch (Exception var7) {
               return;
            }

            if (this.chances.containsKey(id) && this.r.nextInt(100) >= (Integer)this.chances.get(id)) {
               e.setCancelled(true);
               Location l = e.getEntity().getLocation();
               String tip = UtilFormat.format(this.pn, "breedFail", new Object[]{UtilNames.getEntityName(id)});

               for(Player p : e.getEntity().getWorld().getPlayers()) {
                  if (p.getLocation().distance(l) < (double)this.failTipDistance) {
                     p.sendMessage(tip);
                  }
               }
            }
         } else if (e.getSpawnReason().equals(SpawnReason.EGG)) {
            if (e.getEntityType().equals(EntityType.CHICKEN) && this.r.nextInt(100) >= this.eggFix) {
               e.setCancelled(true);
            }
         } else if (e.getEntityType().equals(EntityType.MUSHROOM_COW) && this.cancelMushroomCow && (e.getSpawnReason().equals(SpawnReason.CHUNK_GEN) || e.getSpawnReason().equals(SpawnReason.NATURAL))) {
            e.setCancelled(true);
         }

      }
   }

   @EventHandler(
      priority = EventPriority.HIGHEST
   )
   public void onPlayerJoin(PlayerJoinEvent e) {
      if (this.enableSeason) {
         String msg = UtilFormat.format(this.pn, "season", new Object[]{this.getYear(), this.getSeasonInfo().getName(), this.getNext() / 60, this.getNext() % 60, this.get(100 + this.getSeasonInfo().getSeason())});
         e.getPlayer().sendMessage(msg);
      }
   }

   public int getYear() {
      return this.year;
   }

   public SeasonInfo getSeasonInfo() {
      return (SeasonInfo)this.seasonInfoHash.get(this.season);
   }

   public int getNext() {
      return this.next;
   }

   private void loadConfig(YamlConfiguration config) {
      this.enableSeason = config.getBoolean("enableSeason");
      this.sugarCaneFix = config.getInt("fix.sugarCane");
      this.cocoFix = config.getInt("fix.coco");
      this.eggFix = config.getInt("fix.egg");
      this.cancelMushroomCow = config.getBoolean("fix.cancelMushroomCow");
      this.failTipDistance = config.getInt("fix.failTipDistance");
      this.chances = new HashMap();

      for(String s : config.getStringList("fix.chances")) {
         int id = Integer.parseInt(s.split(" ")[0]);
         int chance = Integer.parseInt(s.split(" ")[1]);
         this.chances.put(id, chance);
      }

      this.interval = config.getInt("time.interval");
      this.giftBase = config.getInt("time.gift.base");
      this.giftAdd = config.getInt("time.gift.add");
      this.seasonInfoHash = new HashMap();

      for(int index = 0; config.contains("time.season" + index); ++index) {
         String name = config.getString("time.season" + index + ".name");
         HashList<Byte> crop1 = new HashListImpl();
         HashList<Byte> crop2 = new HashListImpl();

         for(int i : config.getIntegerList("time.season" + index + ".crop1")) {
            crop1.add((byte)i);
         }

         for(int i : config.getIntegerList("time.season" + index + ".crop2")) {
            crop2.add((byte)i);
         }

         this.seasonInfoHash.put(index, new SeasonInfo(index, name, crop1, crop2));
      }

      this.loadTime();
   }

   private void loadTime() {
      try {
         YamlConfiguration timeConfig = new YamlConfiguration();
         timeConfig.load(this.fix.getDataFolder() + File.separator + "time.yml");
         this.year = timeConfig.getInt("year");
         this.season = timeConfig.getInt("season");
         this.next = timeConfig.getInt("next");
      } catch (FileNotFoundException e) {
         e.printStackTrace();
      } catch (IOException e) {
         e.printStackTrace();
      } catch (InvalidConfigurationException e) {
         e.printStackTrace();
      }

   }

   private void saveTime() {
      try {
         YamlConfiguration timeConfig = new YamlConfiguration();
         timeConfig.set("year", this.year);
         timeConfig.set("season", this.season);
         timeConfig.set("next", this.next);
         timeConfig.save(this.fix.getDataFolder() + File.separator + "time.yml");
      } catch (FileNotFoundException e) {
         e.printStackTrace();
      } catch (IOException e) {
         e.printStackTrace();
      }

   }

   private String get(int id) {
      return UtilFormat.format(this.pn, id);
   }

   class SeasonInfo {
      private int season;
      private String name;
      private HashList crop1;
      private HashList crop2;

      public SeasonInfo(int season, String name, HashList crop1, HashList crop2) {
         super();
         this.season = season;
         this.name = name;
         this.crop1 = crop1;
         this.crop2 = crop2;
      }

      public int getSeason() {
         return this.season;
      }

      public void setSeason(int season) {
         this.season = season;
      }

      public String getName() {
         return this.name;
      }

      public void setName(String name) {
         this.name = name;
      }

      public HashList getCrop1() {
         return this.crop1;
      }

      public void setCrop1(HashList crop1) {
         this.crop1 = crop1;
      }

      public HashList getCrop2() {
         return this.crop2;
      }

      public void setCrop2(HashList crop2) {
         this.crop2 = crop2;
      }
   }
}
