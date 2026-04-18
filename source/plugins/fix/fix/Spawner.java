package fix;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Random;
import lib.config.ReloadConfigEvent;
import lib.hashList.ChanceHashList;
import lib.hashList.ChanceHashListImpl;
import lib.hashList.HashList;
import lib.hashList.HashListImpl;
import lib.types.InvalidTypeException;
import lib.util.Util;
import lib.util.UtilConfig;
import lib.util.UtilFormat;
import lib.util.UtilNames;
import lib.util.UtilPer;
import lib.util.UtilTypes;
import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.Server;
import org.bukkit.World.Environment;
import org.bukkit.block.Block;
import org.bukkit.block.CreatureSpawner;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Creature;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.bukkit.event.entity.CreatureSpawnEvent.SpawnReason;
import org.bukkit.event.world.ChunkPopulateEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

public class Spawner implements Listener {
   private static final String LIB = "lib";
   private UseQuicker useQuicker;
   private Random r;
   private String pn;
   private Server server;
   private String per_fix_spawner_break;
   private ChanceHashList monList;
   private HashMap monHash;
   private HashList spawnerChunkWorlds;
   private int spawnerChunkChance;
   private String airType;
   private int spawnerDisappearRaidus;
   private int tipRadius;
   private int drop;
   private boolean dropTip;
   private boolean flyCancel;
   private int occure;
   private boolean cancelSkeleton;
   private HashList netherList;
   private String spawnerName;
   private String spawnerLore;

   public Spawner(Fix fix) {
      super();
      this.useQuicker = fix.getUseQuicker();
      this.r = new Random();
      this.pn = fix.getPn();
      this.server = fix.getServer();
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
      priority = EventPriority.MONITOR,
      ignoreCancelled = true
   )
   public void onBlockPlace(BlockPlaceEvent e) {
      if (e.canBuild() && e.getBlock().getTypeId() == 52) {
         ItemStack is = e.getItemInHand();
         if (is.hasItemMeta() && is.getItemMeta().hasLore()) {
            List<String> lore = is.getItemMeta().getLore();
            if (lore.size() > 0) {
               String s = (String)lore.get(0);
               if (s.length() > this.spawnerLore.length()) {
                  try {
                     int id = Integer.parseInt(s.substring(this.spawnerLore.length(), s.length()));
                     if (id == 51 && e.getBlock().getWorld().getEnvironment().equals(Environment.NETHER)) {
                        e.setBuild(false);
                        e.setCancelled(true);
                        e.getPlayer().sendMessage(UtilFormat.format("lib", "fail", new Object[]{this.get(415)}));
                        return;
                     }

                     CreatureSpawner spawner = (CreatureSpawner)e.getBlock().getState();
                     spawner.setSpawnedType(EntityType.fromId(id));
                  } catch (Exception var7) {
                  }
               }
            }
         }
      }

   }

   @EventHandler(
      priority = EventPriority.HIGHEST,
      ignoreCancelled = true
   )
   public void blockBreakHighest(BlockBreakEvent e) {
      if (e.getBlock().getTypeId() == 52) {
         if (UtilPer.hasPer(e.getPlayer(), this.per_fix_spawner_break)) {
            return;
         }

         if (!this.spawnerChunkWorlds.has(e.getPlayer().getWorld().getName())) {
            return;
         }

         Player p = e.getPlayer();
         if (this.flyCancel && p.isFlying()) {
            e.setCancelled(true);
            p.sendMessage(UtilFormat.format("lib", "fail", new Object[]{this.get(405)}));
            return;
         }

         CreatureSpawner spawner = (CreatureSpawner)e.getBlock().getState();
         EntityType type = spawner.getSpawnedType();
         Mon mon = (Mon)this.monHash.get(Integer.valueOf(type.getTypeId()));
         if (mon != null) {
            this.useQuicker.toolUseMore(p, 52);
            p.sendMessage(UtilFormat.format("lib", "fail", new Object[]{this.get(410)}));
            if (this.r.nextInt(mon.getCancel()) >= 1) {
               e.setCancelled(true);
               e.getPlayer().sendMessage(UtilFormat.format("lib", "fail", new Object[]{this.get(400)}));
               if (this.r.nextInt(100) < this.occure) {
                  int amount = this.r.nextInt(1 + mon.getSpawnMax() - mon.getSpawnMin()) + mon.getSpawnMin();

                  for(int i = 0; i < amount; ++i) {
                     Entity entity = e.getPlayer().getWorld().spawnEntity(e.getPlayer().getLocation(), type);
                     if (entity instanceof Creature) {
                        Creature creature = (Creature)entity;
                        creature.setTarget(e.getPlayer());
                     }
                  }
               }

               return;
            }
         }
      }

   }

   @EventHandler(
      priority = EventPriority.MONITOR,
      ignoreCancelled = true
   )
   public void blockBreakMonitor(BlockBreakEvent e) {
      if (e.getBlock().getTypeId() == 52) {
         CreatureSpawner spawner = (CreatureSpawner)e.getBlock().getState();
         EntityType type = spawner.getSpawnedType();
         Player p = e.getPlayer();
         Mon mon = (Mon)this.monHash.get(Integer.valueOf(type.getTypeId()));
         if (mon != null && mon.getExpBoxMax() > 0) {
            int amount = this.r.nextInt(1 + mon.getExpBoxMax() - mon.getExpBoxMin()) + mon.getExpBoxMin();
            Location l = e.getBlock().getLocation();
            l.getWorld().dropItemNaturally(l, new ItemStack(384, amount));
         }

         if (!this.spawnerChunkWorlds.has(e.getPlayer().getWorld().getName())) {
            return;
         }

         if (this.r.nextInt(100) < this.drop) {
            int id = spawner.getSpawnedType().getTypeId();
            ItemStack result = new ItemStack(52, 1);
            ItemMeta im = result.getItemMeta();
            im.setDisplayName(this.spawnerName + UtilNames.getEntityName(id));
            List<String> lore = new ArrayList();
            lore.add(this.spawnerLore + id);
            im.setLore(lore);
            result.setItemMeta(im);
            Location l = e.getBlock().getLocation();
            l.getWorld().dropItemNaturally(l, result);
            if (this.dropTip) {
               this.server.broadcastMessage(UtilFormat.format(this.pn, "dropTip", new Object[]{p.getName(), im.getDisplayName()}));
            }
         }
      }

   }

   @EventHandler(
      priority = EventPriority.LOW,
      ignoreCancelled = true
   )
   public void creatureSpawnLow(CreatureSpawnEvent e) {
      if (this.cancelSkeleton && e.getEntityType().equals(EntityType.SKELETON) && e.getSpawnReason().equals(SpawnReason.SPAWNER) && this.netherList.has(e.getEntity().getWorld().getName())) {
         e.setCancelled(true);
      }

   }

   @EventHandler(
      priority = EventPriority.HIGHEST,
      ignoreCancelled = true
   )
   public void creatureSpawn(CreatureSpawnEvent e) {
      try {
         if (e.getSpawnReason().equals(SpawnReason.SPAWNER) && this.spawnerChunkWorlds.has(e.getEntity().getWorld().getName())) {
            EntityType type = e.getEntityType();
            Mon mon = (Mon)this.monHash.get(Integer.valueOf(type.getTypeId()));
            if (mon != null && this.r.nextInt(mon.getDisappear()) < 1) {
               Location l = e.getEntity().getLocation();

               for(int x = -this.spawnerDisappearRaidus; x <= this.spawnerDisappearRaidus; ++x) {
                  for(int y = -this.spawnerDisappearRaidus; y <= this.spawnerDisappearRaidus; ++y) {
                     for(int z = -this.spawnerDisappearRaidus; z <= this.spawnerDisappearRaidus; ++z) {
                        Location tarLoc = new Location(l.getWorld(), (double)(x + l.getBlockX()), (double)(y + l.getBlockY()), (double)(z + l.getBlockZ()));
                        if (tarLoc.getBlock().getTypeId() == 52) {
                           CreatureSpawner creatureSpawner = (CreatureSpawner)tarLoc.getBlock().getState();
                           if (creatureSpawner.getSpawnedType().equals(e.getEntityType())) {
                              tarLoc.getBlock().setTypeId(0);
                              Entity entity = tarLoc.getWorld().spawnEntity(tarLoc, EntityType.SILVERFISH);
                              String msg = UtilFormat.format(this.pn, "disappear", new Object[]{UtilNames.getEntityName(e.getEntity())});

                              for(Entity ee : entity.getNearbyEntities((double)this.tipRadius, (double)this.tipRadius, (double)this.tipRadius)) {
                                 if (ee instanceof Player) {
                                    Player p = (Player)ee;
                                    p.sendMessage(msg);
                                 }
                              }

                              entity.remove();
                              return;
                           }
                        }
                     }
                  }
               }
            }
         }
      } catch (Exception var15) {
      }

   }

   @EventHandler(
      priority = EventPriority.HIGHEST
   )
   public void onChunkPopulate(ChunkPopulateEvent e) {
      try {
         if (this.spawnerChunkWorlds.has(e.getWorld().getName()) && this.r.nextInt(100) < this.spawnerChunkChance) {
            Chunk c = e.getChunk();
            int x = this.r.nextInt(16);
            int y = this.r.nextInt(128);
            int z = this.r.nextInt(16);

            for(int yy = y; yy > 0; --yy) {
               Block b = c.getBlock(x, yy, z);
               if (UtilTypes.checkItem(this.pn, this.airType, b.getTypeId() + ":" + b.getData())) {
                  for(int yyy = yy; yyy > 0; --yyy) {
                     b = c.getBlock(x, yyy, z);
                     if (!UtilTypes.checkItem(this.pn, this.airType, b.getTypeId() + ":" + b.getData())) {
                        this.generateMonsterSpawner(c.getBlock(x, yyy - 2, z).getLocation());
                        return;
                     }
                  }
                  break;
               }
            }

            for(int yy = y; yy < 127; ++yy) {
               Block b = c.getBlock(x, yy, z);
               if (!UtilTypes.checkItem(this.pn, this.airType, b.getTypeId() + ":" + b.getData())) {
                  for(int yyy = yy; yyy < 127; ++yyy) {
                     b = c.getBlock(x, yyy, z);
                     if (UtilTypes.checkItem(this.pn, this.airType, b.getTypeId() + ":" + b.getData())) {
                        this.generateMonsterSpawner(c.getBlock(x, yyy - 3, z).getLocation());
                        return;
                     }
                  }
               }
            }
         }
      } catch (InvalidTypeException e1) {
         e1.printStackTrace();
      }

   }

   private void generateMonsterSpawner(Location l) {
      try {
         l.getBlock().setTypeId(52);
         CreatureSpawner spawner = (CreatureSpawner)l.getBlock().getState();
         Mon mon = (Mon)this.monList.getRandom();
         spawner.setSpawnedType(EntityType.fromId(mon.getId()));
      } catch (Exception var4) {
      }

   }

   private void loadConfig(YamlConfiguration config) {
      this.per_fix_spawner_break = config.getString("per_fix_spawner_break");
      this.monList = new ChanceHashListImpl();
      this.monHash = new HashMap();

      for(int index = 1; config.contains("spawner.monster.mon" + index); ++index) {
         int id = config.getInt("spawner.monster.mon" + index + ".id");
         int chance = config.getInt("spawner.monster.mon" + index + ".chance");
         int cancel = config.getInt("spawner.monster.mon" + index + ".cancel");
         int spawnMin = config.getInt("spawner.monster.mon" + index + ".spawnMin");
         int spawnMax = config.getInt("spawner.monster.mon" + index + ".spawnMax");
         int disappear = config.getInt("spawner.monster.mon" + index + ".disappear");
         int expBoxMin = config.getInt("spawner.monster.mon" + index + ".expBoxMin");
         int expBoxMax = config.getInt("spawner.monster.mon" + index + ".expBoxMax");
         Mon mon = new Mon(id, chance, cancel, spawnMin, spawnMax, disappear, expBoxMin, expBoxMax);
         this.monList.addChance(mon, chance);
         this.monHash.put(id, mon);
      }

      this.spawnerChunkWorlds = new HashListImpl();

      for(String s : config.getStringList("spawner.chunk.worlds")) {
         this.spawnerChunkWorlds.add(s);
      }

      this.spawnerChunkChance = config.getInt("spawner.chunk.chance");
      this.airType = config.getString("spawner.chunk.air");
      this.spawnerDisappearRaidus = config.getInt("spawner.disappearRaidus");
      this.tipRadius = config.getInt("spawner.tipRadius");
      this.drop = config.getInt("spawner.drop");
      this.dropTip = config.getBoolean("spawner.dropTip");
      this.flyCancel = config.getBoolean("spawner.flyCancel");
      this.occure = config.getInt("spawner.occure");
      this.cancelSkeleton = config.getBoolean("spawner.cancelSkeleton");
      this.netherList = new HashListImpl();

      for(String s : config.getStringList("spawner.nether")) {
         this.netherList.add(s);
      }

      this.spawnerName = Util.convert(config.getString("spawner.place.name"));
      this.spawnerLore = Util.convert(config.getString("spawner.place.lore"));
   }

   private String get(int id) {
      return UtilFormat.format(this.pn, id);
   }

   class Mon {
      private int id;
      private int chance;
      private int cancel;
      private int spawnMin;
      private int spawnMax;
      private int disappear;
      private int expBoxMin;
      private int expBoxMax;

      public Mon(int id, int chance, int cancel, int spawnMin, int spawnMax, int disappear, int expBoxMin, int expBoxMax) {
         super();
         this.id = id;
         this.chance = chance;
         this.cancel = cancel;
         this.spawnMin = spawnMin;
         this.spawnMax = spawnMax;
         this.disappear = disappear;
         this.expBoxMin = expBoxMin;
         this.expBoxMax = expBoxMax;
      }

      public int getId() {
         return this.id;
      }

      public int getChance() {
         return this.chance;
      }

      public int getCancel() {
         return this.cancel;
      }

      public int getSpawnMin() {
         return this.spawnMin;
      }

      public int getSpawnMax() {
         return this.spawnMax;
      }

      public int getDisappear() {
         return this.disappear;
      }

      public int getExpBoxMin() {
         return this.expBoxMin;
      }

      public int getExpBoxMax() {
         return this.expBoxMax;
      }
   }
}
