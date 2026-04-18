package fix;

import cus.CustomMonster;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Random;
import java.util.UUID;
import land.Land;
import land.Pos;
import landMain.LandMain;
import landMain.LandManager;
import lib.config.ReloadConfigEvent;
import lib.hashList.ChanceHashList;
import lib.hashList.ChanceHashListImpl;
import lib.hashList.HashList;
import lib.hashList.HashListImpl;
import lib.realDamage.RealDamageEvent;
import lib.time.TimeEvent;
import lib.types.InvalidTypeException;
import lib.util.Util;
import lib.util.UtilConfig;
import lib.util.UtilCosts;
import lib.util.UtilEco;
import lib.util.UtilFormat;
import lib.util.UtilItems;
import lib.util.UtilNames;
import lib.util.UtilPer;
import lib.util.UtilSpeed;
import lib.util.UtilTypes;
import net.minecraft.server.v1_6_R2.AttributeInstance;
import net.minecraft.server.v1_6_R2.AttributeModifier;
import net.minecraft.server.v1_6_R2.GenericAttributes;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Server;
import org.bukkit.Sound;
import org.bukkit.World;
import org.bukkit.World.Environment;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.craftbukkit.v1_6_R2.entity.CraftLivingEntity;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Animals;
import org.bukkit.entity.Blaze;
import org.bukkit.entity.Chicken;
import org.bukkit.entity.Creeper;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.IronGolem;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.entity.Sheep;
import org.bukkit.entity.Skeleton;
import org.bukkit.entity.Villager;
import org.bukkit.entity.Zombie;
import org.bukkit.entity.Villager.Profession;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockDispenseEvent;
import org.bukkit.event.block.BlockExpEvent;
import org.bukkit.event.block.BlockFromToEvent;
import org.bukkit.event.block.BlockGrowEvent;
import org.bukkit.event.block.BlockPistonExtendEvent;
import org.bukkit.event.block.BlockPistonRetractEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.block.SignChangeEvent;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.entity.EntityExplodeEvent;
import org.bukkit.event.entity.EntityTargetLivingEntityEvent;
import org.bukkit.event.entity.ItemSpawnEvent;
import org.bukkit.event.entity.SheepRegrowWoolEvent;
import org.bukkit.event.entity.CreatureSpawnEvent.SpawnReason;
import org.bukkit.event.hanging.HangingBreakByEntityEvent;
import org.bukkit.event.hanging.HangingBreakEvent;
import org.bukkit.event.hanging.HangingBreakEvent.RemoveCause;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.event.player.PlayerBucketEmptyEvent;
import org.bukkit.event.player.PlayerBucketFillEvent;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerLoginEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerShearEntityEvent;
import org.bukkit.event.player.PlayerLoginEvent.Result;
import org.bukkit.event.player.PlayerTeleportEvent.TeleportCause;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.SkullMeta;
import org.bukkit.potion.PotionEffect;
import org.maxgamer.QuickShop.QuickShop;
import org.maxgamer.QuickShop.Shop.Shop;
import town.Main;
import town.TownInfo;
import town.TownUser;

public class Fixs implements Listener {
   private static final String FLAG_BAN_EAT = "banEat";
   private static final String CHECK_LIGHT = "per.fix.light";
   private static final String CHECK_NEW = "per.fix.checkNew";
   private static final UUID UID_LEVEL = UUID.fromString("99fbcdc8-9435-421f-8964-abb1d5c10b8e");
   private static final String LIB = "lib";
   private static final String SPEED_NEWWORLD = "newWorld";
   private static final Object TOWN_WORLD = "townWorld";
   private static final String FLAG_ADMIN = "admin";
   private Random r;
   private Fix fix;
   private Server server;
   private String pn;
   private LandManager landManager;
   private QuickShop qs = (QuickShop)Bukkit.getPluginManager().getPlugin("QuickShop");
   private String per_fix_admin;
   private String per_fix_bucket_free;
   private String per_fix_bucket_water;
   private String per_fix_bucket_lava;
   private int longestEffect;
   private HashList banNaturalMonsterWorlds;
   private String monsterType;
   private boolean emptyBucket;
   private boolean resFree;
   private int costWater;
   private int costLava;
   private HashList dispenseBan;
   private HashList banUse;
   private int showHealthItem;
   private String showHealthEntityType;
   private boolean cancelIronGolemTargetZombie;
   private String cancelRemoveFaraway;
   private int cancelRemoveItem;
   private boolean cancelGrow;
   private String cancelBreedType;
   private int fixSheep;
   private int lowerSkeleton;
   private int eatMaxLevel;
   private int eatHealthMode;
   private HashList eatWorlds;
   private int eatTimerInterval;
   private int eatTimerTimes;
   private boolean eatTipEnable;
   private int eatTipRange;
   private int eatMax;
   private int eatRange;
   private String eatEntity;
   private int chatCheckInterval;
   private boolean chatCustomNameVisiable;
   private int chatMaxLength;
   private ChanceHashList chatList;
   private int dropFix;
   private int dropMax;
   private int egg;
   private int head;
   private HashMap dropHash;
   private HashMap sheepHash;
   private double eggFix;
   private HashMap tempHash;
   private boolean enableDropTip;
   private String dropTipAllow;
   private int dropTipRange;
   private boolean customName;
   private boolean enableWorldTip;
   private int worldTipInterval;
   private int worldTipSuccess;
   private HashMap worldTipHash;
   private boolean healEnable;
   private int healId;
   private String healCost;
   private int healRecovery;
   private int healInterval;
   private String dropExpVipPer;
   private int dropExpChance;
   private int dropExpMulti;
   private String mineExpVipPer;
   private int mineExpChance;
   private int mineExpMulti;
   private int lightTipInterval;
   private HashList effectList;
   private HashList lightWorlds;
   private HashMap lastLightHash;
   private String stoneGoldPer;
   private HashList stoneGoldWorlds;
   private int stoneGoldChance;
   private int stoneGoldRadius;
   private int stoneGoldAmount;
   private int stoneGoldId;
   private String statsGoodPer;
   private int statsGoodChance;
   private String statsGoodMineType;
   private String statsGoodToolType;
   private String mineGoodPer;
   private int mineGoodChance;
   private int mineGoodAdd;
   private String mineGoodMineType;
   private String mineGoodToolType;
   private String attackGoodPer;
   private int attackGoodChance;
   private int attackGoodAdd;
   private int fixSpawner;
   private int netherDoorTip;
   private int netherDoor;
   private HashMap lastNetherHash;
   private HashMap lastNetherLocHash;
   private int autoMonClearInterval;
   private int autoMonClearAmount;
   private int autoMonClearRange;
   private int autoMonClearMax;
   private int newWorldTipLimit;
   private int villageLife;
   private boolean animalCostEnable;
   private String animalCostType;
   private String animalCostFree;
   private int animalCostInterval;
   private int animalCostChance;
   private int animalCostCostChance;
   private int animalCostCost;

   public Fixs(Fix fix) {
      super();
      this.fix = fix;
      this.r = new Random();
      this.server = fix.getServer();
      this.pn = fix.getPn();
      this.landManager = LandMain.getLandManager();
      this.tempHash = new HashMap();
      this.lastLightHash = new HashMap();
      this.lastNetherHash = new HashMap();
      this.lastNetherLocHash = new HashMap();
      this.loadConfig(UtilConfig.getConfig(this.pn));
      fix.getPm().registerEvents(this, fix);
      UtilSpeed.register(this.pn, "heal");
      UtilSpeed.register(this.pn, "newWorld");
      Bukkit.getScheduler().scheduleSyncRepeatingTask(fix, new Runnable() {
         public void run() {
            this.checkLight();
         }

         private void checkLight() {
            for(String worldName : Fixs.this.lightWorlds) {
               World w = Fixs.this.server.getWorld(worldName);

               for(Player p : w.getPlayers()) {
                  if (UtilPer.hasPer(p, "per.fix.light")) {
                     ItemStack is = p.getInventory().getItem(6);
                     if (is != null && is.getTypeId() == 50) {
                        Fixs.this.light(p);
                     }
                  }
               }
            }

         }
      }, 10L, 10L);
      Bukkit.getScheduler().scheduleSyncRepeatingTask(fix, new Runnable() {
         public void run() {
            this.checkCancelFly();
         }

         private void checkCancelFly() {
            Player[] var4;
            for(Player p : var4 = Fixs.this.server.getOnlinePlayers()) {
               if (p.isFlying() && !UtilPer.hasPer(p, "essentials.fly") && !Fixs.this.landManager.getFlyHandler().isAllowFly(p)) {
                  p.setAllowFlight(false);
                  p.setFlying(false);
                  p.sendMessage(UtilFormat.format(Fixs.this.pn, "fail", new Object[]{Fixs.this.get(725)}));
               }
            }

         }
      }, 20L, 20L);
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
      priority = EventPriority.LOWEST,
      ignoreCancelled = true
   )
   public void onBlockGrow(BlockGrowEvent e) {
      if (this.cancelGrow) {
         e.setCancelled(true);
         String tip = UtilFormat.format(this.pn, "fail", new Object[]{this.get(775)});
         Util.sendMsg(e.getBlock().getLocation(), (double)10.0F, false, tip);
      }

   }

   @EventHandler(
      priority = EventPriority.LOW,
      ignoreCancelled = true
   )
   public void onHangingBreak(HangingBreakEvent e) {
      if (e.getCause().equals(RemoveCause.DEFAULT) || e.getCause().equals(RemoveCause.EXPLOSION)) {
         e.setCancelled(true);
      }

   }

   @EventHandler(
      priority = EventPriority.LOW,
      ignoreCancelled = true
   )
   public void onHangingBreakByEntity(HangingBreakByEntityEvent e) {
      if (!(e.getRemover() instanceof Player)) {
         e.setCancelled(true);
      }

   }

   @EventHandler(
      priority = EventPriority.LOWEST
   )
   public void onEntityExplode(EntityExplodeEvent e) {
      e.blockList().clear();
   }

   @EventHandler(
      priority = EventPriority.LOW,
      ignoreCancelled = true
   )
   public void onCreatureSpawnLow(CreatureSpawnEvent e) {
      try {
         if (e.getEntity() instanceof Villager) {
            Villager v = (Villager)e.getEntity();
            if (v.getProfession().equals(Profession.LIBRARIAN) && this.r.nextInt(100) < 90) {
               e.setCancelled(true);
               return;
            }
         }

         if (e.getEntityType().equals(EntityType.PIG_ZOMBIE) && !e.getLocation().getWorld().getEnvironment().equals(Environment.NETHER)) {
            e.setCancelled(true);
            return;
         }

         if (e.getEntityType().equals(EntityType.IRON_GOLEM) && e.getSpawnReason().equals(SpawnReason.VILLAGE_DEFENSE)) {
            e.setCancelled(true);
            return;
         }

         if (this.cancelBreedType != null && !this.cancelBreedType.isEmpty() && (e.getSpawnReason().equals(SpawnReason.BREEDING) || e.getSpawnReason().equals(SpawnReason.EGG)) && UtilTypes.checkEntity(this.pn, this.cancelBreedType, e.getEntityType().name())) {
            e.setCancelled(true);
            String tip = UtilFormat.format(this.pn, "cancelBreed", new Object[]{UtilNames.getEntityName(e.getEntity())});
            Util.sendMsg(e.getLocation(), (double)10.0F, false, tip);
            return;
         }

         if (e.getSpawnReason().equals(SpawnReason.SPAWNER) && this.r.nextInt(100) < this.fixSpawner) {
            e.setCancelled(true);
            return;
         }

         if (e.getSpawnReason().equals(SpawnReason.NATURAL) && this.banNaturalMonsterWorlds.has(e.getEntity().getWorld().getName()) && UtilTypes.checkEntity(this.pn, this.monsterType, e.getEntity().getType().name())) {
            e.setCancelled(true);
            return;
         }

         if (e.getEntity() instanceof Villager && (e.getSpawnReason().equals(SpawnReason.BREEDING) || e.getSpawnReason().equals(SpawnReason.NATURAL) || e.getSpawnReason().equals(SpawnReason.CHUNK_GEN))) {
            e.setCancelled(true);
         }
      } catch (InvalidTypeException e1) {
         e1.printStackTrace();
      }

   }

   @EventHandler(
      priority = EventPriority.HIGHEST,
      ignoreCancelled = true
   )
   public void onCreatureSpawn(CreatureSpawnEvent e) {
      try {
         if (UtilTypes.checkEntity(this.pn, this.cancelRemoveFaraway, e.getEntity().getType().name())) {
            e.getEntity().setRemoveWhenFarAway(false);
         }

         if (e.getEntity().getWorld().getEnvironment().equals(Environment.NETHER)) {
            if (e.getEntity() instanceof Skeleton && e.getSpawnReason().equals(SpawnReason.SPAWNER_EGG)) {
               e.setCancelled(true);
               return;
            }

            if (e.getSpawnReason().equals(SpawnReason.NATURAL) && e.getEntityType().equals(EntityType.SKELETON) && this.r.nextInt(100) < this.lowerSkeleton) {
               e.setCancelled(true);
            }
         }
      } catch (InvalidTypeException e1) {
         e1.printStackTrace();
      }

   }

   @EventHandler(
      priority = EventPriority.LOW,
      ignoreCancelled = true
   )
   public void onEntityTargetLivingEntity(EntityTargetLivingEntityEvent e) {
      if (this.cancelIronGolemTargetZombie && e.getTarget() instanceof Zombie && e.getEntity() instanceof IronGolem) {
         e.setTarget((Entity)null);
         e.setCancelled(true);
      }

   }

   @EventHandler(
      priority = EventPriority.LOW,
      ignoreCancelled = true
   )
   public void onPlayerInteract(PlayerInteractEvent e) {
      try {
         if (e.getAction().equals(Action.PHYSICAL) && e.getClickedBlock().getType().equals(Material.SOIL)) {
            e.setCancelled(true);
            return;
         }

         if (e.getClickedBlock().getTypeId() == 120 && !e.getPlayer().isOp()) {
            e.setCancelled(true);
            e.getPlayer().sendMessage(UtilFormat.format(this.pn, 710));
         } else if (e.hasItem()) {
            int id = e.getItem().getTypeId();
            if (this.banUse.has(id)) {
               e.setCancelled(true);
               e.getPlayer().sendMessage(UtilFormat.format(this.pn, 705));
            }
         }
      } catch (Exception var3) {
      }

   }

   @EventHandler(
      priority = EventPriority.MONITOR,
      ignoreCancelled = true
   )
   public void onPlayerInteractEntityMonitor(PlayerInteractEntityEvent e) {
      if (e.getRightClicked() instanceof Villager) {
         Villager v = (Villager)e.getRightClicked();
         if (this.r.nextInt(this.villageLife) < 1) {
            v.remove();
            e.setCancelled(true);
            String tip = UtilFormat.format(this.pn, "tip", new Object[]{this.get(770)});
            e.getPlayer().sendMessage(tip);
         }
      }

   }

   @EventHandler(
      priority = EventPriority.MONITOR,
      ignoreCancelled = true
   )
   public void onInventoryClickMonitor(InventoryClickEvent e) {
      try {
         if (e.getInventory().getType().equals(InventoryType.MERCHANT) && e.getWhoClicked() instanceof Player && e.getSlot() == 2 && e.getInventory().getItem(2).getTypeId() == 403 && this.r.nextInt(100) < 18) {
            Player p = (Player)e.getWhoClicked();
            String tip = UtilFormat.format(this.pn, "tip", new Object[]{this.get(770)});
            p.sendMessage(tip);
            DelVillager dv = new DelVillager(p);
            Bukkit.getScheduler().scheduleSyncDelayedTask(this.fix, dv);
         }
      } catch (Exception var5) {
      }

   }

   @EventHandler(
      priority = EventPriority.HIGHEST,
      ignoreCancelled = true
   )
   public void onPlayerInteractEntity(PlayerInteractEntityEvent e) {
      try {
         if (this.healEnable && e.getRightClicked() instanceof Player) {
            ItemStack is = e.getPlayer().getItemInHand();
            if (is != null && is.getTypeId() == this.healId) {
               Player tar = (Player)e.getRightClicked();
               TownUser tu = Main.getTownManager().checkInit(e.getPlayer());
               if (tu.getTownId() != -1L) {
                  TownInfo ti = (TownInfo)Main.getTownManager().getTownHash().get(tu.getTownId());
                  if (ti != null && ti.getLevel() < Main.getTownManager().getHealLevel()) {
                     e.getPlayer().sendMessage(UtilFormat.format(this.pn, "fail", new Object[]{this.get(610)}));
                     return;
                  }
               }

               if (!Main.getTownManager().isInSameTown(e.getPlayer().getName(), tar.getName())) {
                  e.getPlayer().sendMessage(UtilFormat.format(this.pn, "fail", new Object[]{this.get(600)}));
                  return;
               }

               if (tar.getHealth() < 0.1) {
                  return;
               }

               if (tar.getHealth() >= tar.getMaxHealth()) {
                  e.getPlayer().sendMessage(UtilFormat.format(this.pn, "fail", new Object[]{this.get(605)}));
                  return;
               }

               if (!UtilSpeed.check(e.getPlayer(), this.pn, "heal", this.healInterval)) {
                  return;
               }

               if (UtilCosts.cost(e.getPlayer(), this.pn, this.healCost, false)) {
                  int heal = (int)Math.min((double)this.healRecovery, tar.getMaxHealth() - tar.getHealth());
                  tar.setHealth(tar.getHealth() + (double)heal);
                  e.getPlayer().sendMessage(UtilFormat.format(this.pn, "heal1", new Object[]{heal, tar.getHealth(), tar.getMaxHealth()}));
                  tar.sendMessage(UtilFormat.format(this.pn, "heal2", new Object[]{heal}));
               }

               return;
            }
         }

         try {
            if (UtilTypes.checkEntity(this.pn, this.showHealthEntityType, e.getRightClicked().getType().name())) {
               ItemStack is = e.getPlayer().getItemInHand();
               int id = 0;
               if (is != null) {
                  id = is.getTypeId();
               }

               if (id == this.showHealthItem && e.getRightClicked() instanceof LivingEntity) {
                  LivingEntity livingEntity = (LivingEntity)e.getRightClicked();
                  int health = (int)livingEntity.getHealth();
                  int maxHealth = (int)livingEntity.getMaxHealth();
                  e.getPlayer().sendMessage(UtilFormat.format(this.pn, "showHealth", new Object[]{UtilNames.getEntityName(e.getRightClicked()), health, maxHealth}));
                  return;
               }
            }

            if (UtilPer.hasPer(e.getPlayer(), this.per_fix_admin) && e.getRightClicked() instanceof LivingEntity) {
               ItemStack is = e.getPlayer().getItemInHand();
               if (is != null && is.getTypeId() == this.cancelRemoveItem) {
                  if (((LivingEntity)e.getRightClicked()).getRemoveWhenFarAway()) {
                     e.getPlayer().sendMessage(UtilFormat.format("lib", "tip", new Object[]{this.get(500)}));
                  } else {
                     e.getPlayer().sendMessage(UtilFormat.format("lib", "tip", new Object[]{this.get(505)}));
                  }

                  return;
               }
            }
         } catch (InvalidTypeException e1) {
            e1.printStackTrace();
         } catch (Exception var8) {
         }

         if (e.getPlayer().getItemInHand() != null && e.getPlayer().getItemInHand().getTypeId() == 421) {
            e.setCancelled(true);
            e.getPlayer().sendMessage(UtilFormat.format(this.pn, "fail", new Object[]{this.get(650)}));
         }
      } catch (InvalidTypeException e1) {
         e1.printStackTrace();
      }

   }

   @EventHandler(
      priority = EventPriority.MONITOR,
      ignoreCancelled = false
   )
   public void onPlayerLogin(PlayerLoginEvent e) {
      if (e.getResult().equals(Result.KICK_FULL)) {
         e.setKickMessage(this.get(760));
      }

   }

   @EventHandler(
      priority = EventPriority.MONITOR
   )
   public void onPlayerJoin(PlayerJoinEvent e) {
      Player p = e.getPlayer();
      if (!UtilPer.hasPer(p, "per.fix.checkNew")) {
         UtilPer.add(p, "per.fix.checkNew");
         p.getInventory().addItem(new ItemStack[]{UtilItems.getItem(this.pn, "guide")});
      }

      this.forceLeaveVechile(p);
      Location l = e.getPlayer().getLocation();
      if (l == null || l.getBlockY() < 0) {
         p.teleport(this.server.getWorld("world").getSpawnLocation(), TeleportCause.PLUGIN);
         p.sendMessage(UtilFormat.format(this.pn, 700));
      }

      if (l.getBlock().getTypeId() != 0 && l.getBlock().getRelative(BlockFace.UP).getTypeId() == 0) {
         p.teleport(l.getBlock().getRelative(BlockFace.UP).getLocation().add((double)0.5F, 0.1, (double)0.5F));
      }

   }

   @EventHandler(
      priority = EventPriority.MONITOR
   )
   public void onPlayerQuit(PlayerQuitEvent e) {
      Player p = e.getPlayer();
      this.forceLeaveVechile(p);
      this.lastLightHash.remove(p);
   }

   @EventHandler(
      priority = EventPriority.LOW,
      ignoreCancelled = true
   )
   public void onBlockFromTo(BlockFromToEvent e) {
      if (e.getToBlock().getTypeId() == 144) {
         e.setCancelled(true);
      }

   }

   @EventHandler(
      priority = EventPriority.LOWEST,
      ignoreCancelled = true
   )
   public void onPlayerBucketEmptyLowest(PlayerBucketEmptyEvent e) {
      if (e.getBucket().equals(Material.WATER_BUCKET) && !UtilPer.checkPer(e.getPlayer(), this.per_fix_bucket_water)) {
         e.setCancelled(true);
      } else if (e.getBucket().equals(Material.LAVA_BUCKET) && !UtilPer.checkPer(e.getPlayer(), this.per_fix_bucket_lava)) {
         e.setCancelled(true);
      }

   }

   @EventHandler(
      priority = EventPriority.LOWEST,
      ignoreCancelled = true
   )
   public void onPlayerBucketFillLowest(PlayerBucketFillEvent e) {
      if (e.getBucket().equals(Material.WATER_BUCKET) && !UtilPer.checkPer(e.getPlayer(), this.per_fix_bucket_water)) {
         e.setCancelled(true);
      } else if (e.getBucket().equals(Material.LAVA_BUCKET) && !UtilPer.checkPer(e.getPlayer(), this.per_fix_bucket_lava)) {
         e.setCancelled(true);
      }

   }

   @EventHandler(
      priority = EventPriority.MONITOR,
      ignoreCancelled = false
   )
   public void onPlayerBucketEmpty(PlayerBucketEmptyEvent e) {
      if (!e.isCancelled() && !UtilPer.hasPer(e.getPlayer(), this.per_fix_bucket_free)) {
         int cost = 0;
         String name = "";
         ItemStack is = e.getPlayer().getItemInHand();
         Location l = e.getBlockClicked().getLocation();
         Land land = this.landManager.getHighestPriorityLand(l);
         if (!this.resFree || land == null) {
            if (is.getTypeId() == 326) {
               cost = this.costWater;
               name = this.get(205);
            } else if (is.getTypeId() == 327) {
               cost = this.costLava;
               name = this.get(210);
            }

            if (cost > 0) {
               if (UtilEco.get(e.getPlayer().getName()) < (double)cost) {
                  e.getPlayer().sendMessage(UtilFormat.format(this.pn, "costErr", new Object[]{name, cost}));
                  e.setCancelled(true);
               } else {
                  UtilEco.del(e.getPlayer().getName(), (double)cost);
                  e.getPlayer().sendMessage(UtilFormat.format(this.pn, "cost", new Object[]{name, cost}));
               }
            }
         }
      }

      if (this.emptyBucket && e.isCancelled() && e.getPlayer().getItemInHand() != null) {
         ItemStack is = e.getPlayer().getItemInHand();
         if (is.getTypeId() == 326 || is.getTypeId() == 327) {
            e.setCancelled(true);
            e.getPlayer().setItemInHand(new ItemStack(325, 1));
            e.getPlayer().sendMessage(UtilFormat.format("lib", "fail", new Object[]{this.get(200)}));
         }
      }

   }

   @EventHandler(
      priority = EventPriority.LOW,
      ignoreCancelled = true
   )
   public void onBlockDispense(BlockDispenseEvent e) {
      if (this.dispenseBan.has(e.getItem().getTypeId())) {
         e.setCancelled(true);
      }

   }

   @EventHandler(
      priority = EventPriority.MONITOR,
      ignoreCancelled = true
   )
   public void onBlockExp(BlockExpEvent e) {
      if (e.getExpToDrop() > 0 && this.r.nextInt(100) < this.mineExpChance) {
         Player p = this.getNearestPlayer(e.getBlock().getLocation());
         if (p != null) {
            if (UtilPer.hasPer(p, this.mineExpVipPer)) {
               int exp = e.getExpToDrop() * this.mineExpMulti;
               if (exp > 0) {
                  this.server.getScheduler().scheduleSyncDelayedTask(this.fix, new GiveExp(p, exp));
                  p.getWorld().playSound(p.getLocation(), Sound.LEVEL_UP, 2.0F, 1.0F);
                  if (p.isOnline()) {
                     p.sendMessage(UtilFormat.format(this.pn, "dropExp", new Object[]{exp}));
                  }
               }
            } else if (p.isOnline()) {
               p.sendMessage(this.get(755));
            }
         }
      }

   }

   @EventHandler(
      priority = EventPriority.MONITOR,
      ignoreCancelled = false
   )
   public void onBlockBreak(BlockBreakEvent e) {
      try {
         if (!e.isCancelled()) {
            if (this.r.nextInt(this.stoneGoldChance) < 1 && this.stoneGoldWorlds.has(e.getBlock().getWorld().getName()) && e.getBlock().getTypeId() == 1) {
               if (!UtilPer.hasPer(e.getPlayer(), this.stoneGoldPer)) {
                  e.getPlayer().sendMessage(this.get(736));
               } else {
                  Location l = e.getBlock().getLocation();
                  int success = 0;

                  for(int i = 0; i < this.stoneGoldAmount * 2; ++i) {
                     int x = l.getBlockX() + this.stoneGoldRadius - this.r.nextInt(this.stoneGoldRadius * 2 + 1);
                     int y = l.getBlockY() + this.stoneGoldRadius - this.r.nextInt(this.stoneGoldRadius * 2 + 1);
                     int z = l.getBlockZ() + this.stoneGoldRadius - this.r.nextInt(this.stoneGoldRadius * 2 + 1);
                     if (y < 1) {
                        y = 1;
                     } else if (y > 255) {
                        y = 255;
                     }

                     Block b = l.getWorld().getBlockAt(x, y, z);
                     if (b != null && b.getTypeId() == 1) {
                        ++success;
                        b.setTypeId(this.stoneGoldId);
                        if (success >= this.stoneGoldAmount) {
                           break;
                        }
                     }
                  }

                  e.getPlayer().sendMessage(this.get(735));
                  e.getPlayer().getWorld().playSound(e.getPlayer().getLocation(), Sound.LEVEL_UP, 3.0F, 1.2F);
               }
            }

            if (this.r.nextInt(this.statsGoodChance) < 1 && UtilTypes.checkItem(this.pn, this.statsGoodMineType, String.valueOf(e.getBlock().getTypeId()))) {
               ItemStack is = e.getPlayer().getItemInHand();
               if (is != null && UtilTypes.checkItem(this.pn, this.statsGoodToolType, String.valueOf(is.getTypeId()))) {
                  if (UtilPer.hasPer(e.getPlayer(), this.statsGoodPer)) {
                     e.getPlayer().setHealth(e.getPlayer().getMaxHealth());
                     e.getPlayer().setFoodLevel(20);
                     e.getPlayer().sendMessage(this.get(720));
                     e.getPlayer().getWorld().playSound(e.getPlayer().getLocation(), Sound.LEVEL_UP, 2.0F, 1.0F);
                  } else {
                     e.getPlayer().sendMessage(this.get(721));
                  }
               }
            }

            if (this.r.nextInt(this.mineGoodChance) < 1 && UtilTypes.checkItem(this.pn, this.mineGoodMineType, String.valueOf(e.getBlock().getTypeId()))) {
               ItemStack is = e.getPlayer().getItemInHand();
               if (is != null && UtilTypes.checkItem(this.pn, this.mineGoodToolType, String.valueOf(is.getTypeId()))) {
                  if (UtilPer.hasPer(e.getPlayer(), this.mineGoodPer)) {
                     int dura = is.getEnchantmentLevel(Enchantment.DURABILITY);
                     if (dura <= 0 || this.r.nextInt(dura + 1) < 1) {
                        int add = Math.min(is.getDurability(), this.mineGoodAdd);
                        if (add > 0) {
                           is.setDurability((short)(is.getDurability() - add));
                           e.getPlayer().sendMessage(UtilFormat.format(this.pn, "mineGood", new Object[]{add}));
                           e.getPlayer().updateInventory();
                           e.getPlayer().getWorld().playSound(e.getPlayer().getLocation(), Sound.LEVEL_UP, 2.0F, 1.0F);
                        }
                     }
                  } else {
                     e.getPlayer().sendMessage(this.get(745));
                  }
               }
            }
         } else if (UtilSpeed.check(e.getPlayer(), this.pn, "newWorld", this.newWorldTipLimit, false) && e.getBlock().getWorld().getName().equals("newWorld")) {
            e.getPlayer().sendMessage(this.get(740));
         }
      } catch (InvalidTypeException e1) {
         e1.printStackTrace();
      }

   }

   @EventHandler(
      priority = EventPriority.LOW,
      ignoreCancelled = true
   )
   public void onBlockPlaceLow(BlockPlaceEvent e) {
      if (e.getBlock().getTypeId() == 154) {
         try {
            Shop shop = this.qs.getShopManager().getShop(e.getBlock().getRelative(BlockFace.UP).getLocation());
            if (shop != null && !shop.getOwner().equals(e.getPlayer().getName())) {
               e.setCancelled(true);
               e.getPlayer().sendMessage(UtilFormat.format(this.pn, "fail", new Object[]{this.get(785)}));
            }
         } catch (Exception var3) {
         }
      }

   }

   @EventHandler(
      priority = EventPriority.MONITOR,
      ignoreCancelled = false
   )
   public void onBlockPlace(BlockPlaceEvent e) {
      if (e.isCancelled() && e.getBlock().getWorld().getName().equals("newWorld")) {
         e.getPlayer().sendMessage(this.get(740));
      }

   }

   @EventHandler(
      priority = EventPriority.LOW,
      ignoreCancelled = true
   )
   public void onBlockPistonExtend(BlockPistonExtendEvent e) {
      Land land = this.landManager.getHighestPriorityLand(e.getBlock().getLocation());

      for(Block b : e.getBlocks()) {
         Land check = this.landManager.getHighestPriorityLand(b.getLocation());
         if ((land != null || check != null) && (land == null || check == null || land.getId() != check.getId())) {
            e.setCancelled(true);
            Util.sendMsg(e.getBlock().getLocation(), (double)8.0F, false, UtilFormat.format(this.pn, "fail", new Object[]{this.get(790)}));
            return;
         }
      }

   }

   @EventHandler(
      priority = EventPriority.LOW,
      ignoreCancelled = true
   )
   public void onBlockPistonRetract(BlockPistonRetractEvent e) {
      Land land = this.landManager.getHighestPriorityLand(e.getRetractLocation());
      Land land2 = this.landManager.getHighestPriorityLand(e.getBlock().getLocation());
      if ((land != null || land2 != null) && (land == null || land2 == null || land.getId() != land2.getId())) {
         e.setCancelled(true);
         Util.sendMsg(e.getBlock().getLocation(), (double)8.0F, false, UtilFormat.format(this.pn, "fail", new Object[]{this.get(790)}));
      }

   }

   @EventHandler(
      priority = EventPriority.MONITOR,
      ignoreCancelled = true
   )
   public void onRealDamage(RealDamageEvent e) {
      if (e.getDamager() instanceof Player) {
         Player p = (Player)e.getDamager();
         if (this.r.nextInt(this.attackGoodChance) < 1) {
            ItemStack is = p.getItemInHand();
            if (is != null && UtilItems.hasDurability(is)) {
               if (UtilPer.hasPer(p, this.attackGoodPer)) {
                  int dura = is.getEnchantmentLevel(Enchantment.DURABILITY);
                  if (dura <= 0 || this.r.nextInt(dura + 1) < 1) {
                     int add = Math.min(is.getDurability(), this.attackGoodAdd);
                     if (add > 0) {
                        is.setDurability((short)(is.getDurability() - add));
                        p.sendMessage(UtilFormat.format(this.pn, "attackGood", new Object[]{add}));
                        p.updateInventory();
                        p.getWorld().playSound(p.getLocation(), Sound.LEVEL_UP, 2.0F, 1.0F);
                     }
                  }
               } else {
                  p.sendMessage(this.get(755));
               }
            }
         }
      }

   }

   @EventHandler(
      priority = EventPriority.MONITOR,
      ignoreCancelled = true
   )
   public void onRealDamage2(RealDamageEvent e) {
      if (e.getDamager() instanceof Player && !(e.getVictim() instanceof Player) && !(e.getVictim() instanceof Skeleton) && !(e.getVictim() instanceof IronGolem) && !(e.getVictim() instanceof Blaze)) {
         e.getVictim().getWorld().playSound(e.getVictim().getLocation(), Sound.HURT_FLESH, 3.0F, (float)(0.8 + e.getDamage() / (double)30.0F));
      }

   }

   @EventHandler(
      priority = EventPriority.LOW
   )
   public void onEntityDeathLow(EntityDeathEvent e) {
      try {
         if (e.getEntity() instanceof Creeper && e.getEntity().getLastDamageCause() instanceof EntityDamageByEntityEvent) {
            EntityDamageByEntityEvent ee = (EntityDamageByEntityEvent)e.getEntity().getLastDamageCause();
            if (ee.getDamager() instanceof Projectile) {
               Projectile pro = (Projectile)ee.getDamager();
               if (pro.getShooter() instanceof Skeleton) {
                  ItemStack is = new ItemStack(this.r.nextInt(12) + 2256);
                  e.getDrops().add(is);
               }
            }
         }
      } catch (Exception var8) {
      }

      try {
         if (UtilTypes.checkEntity(this.pn, this.eatEntity, e.getEntityType().name())) {
            DropInfo dropInfo = (DropInfo)this.dropHash.get(Integer.valueOf(e.getEntity().getType().getTypeId()));
            int level = this.getLevel(e.getEntity());
            if (dropInfo != null) {
               for(int id : dropInfo.getDropsHash().keySet()) {
                  double amount = (Double)dropInfo.getDropsHash().get(id);
                  amount *= (double)(level - 1);
                  if (this.dropFix > 0) {
                     amount = amount * (double)(100 - this.dropFix + this.r.nextInt(this.dropFix * 2)) / (double)100.0F;
                  }

                  this.drop(e.getDrops(), id, Math.min(this.dropMax, (int)amount));
               }

               if (this.egg <= level && this.r.nextInt(dropInfo.getEgg()) < level) {
                  ItemStack is = new ItemStack(383, 1, e.getEntity().getType().getTypeId());
                  e.getDrops().add(is);
                  String tip = UtilFormat.format(this.pn, "dropAddTip", new Object[]{UtilNames.getEntityName(e.getEntity().getType().getTypeId()), UtilNames.getItemName(is)});
                  Util.sendMsg(e.getEntity().getLocation(), (double)10.0F, true, tip);
               }

               if (this.head <= level && this.r.nextInt(dropInfo.getHead()) < level) {
                  ItemStack is = new ItemStack(397, 1, (short)3);
                  SkullMeta skull = (SkullMeta)is.getItemMeta();
                  String name = UtilFormat.format(this.pn, "headTip", new Object[]{UtilNames.getEntityName(e.getEntity().getType().getTypeId())});
                  skull.setDisplayName(name);
                  skull.setOwner(dropInfo.getHeadName());
                  is.setItemMeta(skull);
                  e.getDrops().add(is);
                  String tip = UtilFormat.format(this.pn, "dropAddTip", new Object[]{UtilNames.getEntityName(e.getEntity().getType().getTypeId()), name});
                  Util.sendMsg(e.getEntity().getLocation(), (double)10.0F, true, tip);
               }
            }
         }
      } catch (Exception var9) {
      }

   }

   @EventHandler(
      priority = EventPriority.HIGHEST
   )
   public void onEntityDeath(EntityDeathEvent e) {
      if (this.r.nextInt(100) < this.dropExpChance) {
         Player killer = e.getEntity().getKiller();
         if (killer != null) {
            if (UtilPer.hasPer(killer, this.dropExpVipPer)) {
               int exp = e.getDroppedExp() * this.dropExpMulti;
               if (exp > 0) {
                  this.server.getScheduler().scheduleSyncDelayedTask(this.fix, new GiveExp(killer, exp));
                  killer.getWorld().playSound(killer.getLocation(), Sound.LEVEL_UP, 2.0F, 1.0F);
                  if (killer.isOnline()) {
                     killer.sendMessage(UtilFormat.format(this.pn, "dropExp", new Object[]{exp}));
                  }
               }
            } else if (killer.isOnline()) {
               killer.sendMessage(this.get(755));
            }
         }
      }

      try {
         if (this.enableDropTip && UtilTypes.checkEntity(this.pn, this.dropTipAllow, e.getEntityType().name())) {
            this.tipDrop(e.getEntity(), e.getDroppedExp(), e.getDrops());
         }
      } catch (InvalidTypeException e1) {
         e1.printStackTrace();
      }

   }

   @EventHandler(
      priority = EventPriority.HIGHEST,
      ignoreCancelled = true
   )
   public void onInventoryClick(InventoryClickEvent e) {
      try {
         if (e.getCurrentItem().getType().equals(Material.SKULL_ITEM) && e.getInventory().getType().equals(InventoryType.ANVIL) && e.getWhoClicked() instanceof Player) {
            e.setCancelled(true);
            ((Player)e.getWhoClicked()).sendMessage(this.get(705));
            return;
         }

         if (e.getView().getTopInventory().getType().equals(InventoryType.BREWING) && e.isShiftClick()) {
            ItemStack is = e.getCursor();
            if (is != null && is.getAmount() > 1 && is.getTypeId() == 373) {
               e.setCancelled(true);
               ((Player)e.getWhoClicked()).sendMessage(UtilFormat.format(this.pn, "fail", new Object[]{this.get(715)}));
               return;
            }

            is = e.getCurrentItem();
            if (is != null && is.getAmount() > 1 && is.getTypeId() == 373) {
               e.setCancelled(true);
               ((Player)e.getWhoClicked()).sendMessage(UtilFormat.format(this.pn, "fail", new Object[]{this.get(715)}));
               return;
            }
         }
      } catch (Exception var3) {
      }

   }

   @EventHandler
   public void onTime(TimeEvent e) {
      if (TimeEvent.getTime() % 53L == 0L) {
         Player[] var5;
         for(Player p : var5 = Bukkit.getOnlinePlayers()) {
            if (!UtilPer.hasPer(p, this.per_fix_admin)) {
               for(PotionEffect pe : p.getActivePotionEffects()) {
                  if (pe.getDuration() <= 0 || pe.getDuration() > this.longestEffect) {
                     p.removePotionEffect(pe.getType());
                     break;
                  }
               }
            }
         }
      }

      try {
         if (this.animalCostEnable && TimeEvent.getTime() % (long)this.animalCostInterval == 0L && this.r.nextInt(100) < this.animalCostChance) {
            HashMap<String, Integer> costHash = new HashMap();

            for(World w : Bukkit.getWorlds()) {
               for(Entity entity : w.getEntities()) {
                  if (UtilTypes.checkEntity(this.pn, this.animalCostType, entity.getType().name())) {
                     Land land = this.landManager.getHighestPriorityLand(entity.getLocation());
                     if (land != null) {
                        if (entity.getWorld().getName().equals(TOWN_WORLD)) {
                           if (land.hasFlag("admin")) {
                              try {
                                 HashList<String> list = (HashList)land.getPers().get("admin");
                                 String tar = (String)list.get(this.r.nextInt(list.size()));
                                 if (!costHash.containsKey(tar)) {
                                    costHash.put(tar, 0);
                                 }

                                 costHash.put(tar, (Integer)costHash.get(tar) + this.animalCostCost);
                              } catch (Exception var14) {
                              }
                           }
                        } else if (!UtilPer.hasPer(land.getOwner(), this.animalCostFree)) {
                           if (!costHash.containsKey(land.getOwner())) {
                              costHash.put(land.getOwner(), 0);
                           }

                           costHash.put(land.getOwner(), (Integer)costHash.get(land.getOwner()) + this.animalCostCost);
                        }
                     }
                  }
               }
            }

            for(String name : costHash.keySet()) {
               if (this.r.nextInt(100) < this.animalCostCostChance) {
                  int cost = (Integer)costHash.get(name);
                  Util.addDebt(name, cost, this.get(780));
               }
            }
         }
      } catch (InvalidTypeException e2) {
         e2.printStackTrace();
      } catch (Exception var17) {
      }

      if (TimeEvent.getTime() % (long)this.lightTipInterval == 0L) {
         String msg = this.get(765);

         Player[] it;
         for(Player p : it = this.server.getOnlinePlayers()) {
            if (p.getWorld().getName().equals("oreWorld")) {
               p.sendMessage(msg);
            }
         }
      }

      Player[] i;
      for(Player p : i = this.server.getOnlinePlayers()) {
         Location l = p.getLocation();
         if (l.getBlock().getTypeId() == 90) {
            Pos pos = Pos.getPos(l);
            long now = System.currentTimeMillis();
            if (!this.lastNetherHash.containsKey(p)) {
               this.lastNetherHash.put(p, now);
               this.lastNetherLocHash.put(p, pos);
            }

            if (((Pos)this.lastNetherLocHash.get(p)).equals(pos)) {
               if ((now - (Long)this.lastNetherHash.get(p)) / 1000L == (long)this.netherDoorTip) {
                  p.sendMessage(UtilFormat.format(this.pn, "netherDoor", new Object[]{this.netherDoor}));
               } else if ((now - (Long)this.lastNetherHash.get(p)) / 1000L >= (long)this.netherDoor) {
                  this.lastNetherHash.remove(p);
                  this.lastNetherLocHash.remove(p);
                  p.sendMessage(UtilFormat.format(this.pn, "tip", new Object[]{this.get(730)}));
                  Location spawnLoc = this.server.getWorld("world").getSpawnLocation();
                  Util.tp(p, spawnLoc, true, false);
               }
            } else {
               this.lastNetherHash.put(p, now);
               this.lastNetherLocHash.put(p, pos);
            }
         }
      }

      if (TimeEvent.getTime() % (long)this.eatTimerInterval == 0L) {
         this.checkEat();
      }

      if (this.enableWorldTip && TimeEvent.getTime() % (long)this.worldTipInterval == 0L && this.r.nextInt(100) < this.worldTipSuccess) {
         for(World w : this.server.getWorlds()) {
            String tip = (String)this.worldTipHash.get(w.getName());
            if (tip != null && !tip.trim().isEmpty()) {
               for(Player p : w.getPlayers()) {
                  p.sendMessage(tip);
               }
            }
         }
      }

      if (TimeEvent.getTime() % (long)this.chatCheckInterval == 0L) {
         try {
            for(World w : this.server.getWorlds()) {
               Collection<Animals> list = w.getEntitiesByClass(Animals.class);
               if (!list.isEmpty()) {
                  int tar = this.r.nextInt(list.size());
                  Iterator<Animals> it = list.iterator();
                  int index = 0;

                  Animals result;
                  for(result = null; it.hasNext(); ++index) {
                     Animals le = (Animals)it.next();
                     if (index >= tar && UtilTypes.checkEntity(this.pn, this.eatEntity, le.getType().name())) {
                        result = le;
                        break;
                     }
                  }

                  if (result != null) {
                     this.chat(result);
                  }
               }
            }
         } catch (InvalidTypeException e1) {
            e1.printStackTrace();
         }
      }

      if (TimeEvent.getTime() % (long)this.autoMonClearInterval == 0L) {
         Iterator var30 = this.server.getWorlds().iterator();

         while(true) {
            Collection<Entity> list;
            label137:
            do {
               if (!var30.hasNext()) {
                  return;
               }

               World w = (World)var30.next();
               list = w.getEntitiesByClasses(new Class[]{CustomMonster.class});
            } while(list.size() <= 0);

            label159:
            for(int i = 0; i < this.autoMonClearAmount; ++i) {
               Iterator<Entity> it = list.iterator();
               int index = 0;
               int target = this.r.nextInt(list.size());

               while(it.hasNext()) {
                  Entity entity = (Entity)it.next();
                  if (index++ >= target) {
                     if (!(entity instanceof CustomMonster)) {
                        break;
                     }

                     CustomMonster cm = (CustomMonster)entity;
                     if (!cm.getSpawnReason().equals(SpawnReason.SPAWNER)) {
                        break;
                     }

                     int amount = 0;

                     for(Entity check : entity.getNearbyEntities((double)this.autoMonClearRange, (double)this.autoMonClearRange, (double)this.autoMonClearRange)) {
                        if (check.getType().equals(entity.getType())) {
                           ++amount;
                           if (amount > this.autoMonClearMax) {
                              entity.remove();
                              continue label159;
                           }
                        }
                     }
                     break label137;
                  }
               }
            }
         }
      }
   }

   @EventHandler(
      priority = EventPriority.MONITOR,
      ignoreCancelled = true
   )
   public void onSignChange(SignChangeEvent e) {
      for(int i = 0; i < 4; ++i) {
         String result = Util.convert(e.getLine(i));
         e.setLine(i, result.substring(0, Math.min(15, result.length())));
      }

   }

   @EventHandler(
      priority = EventPriority.HIGHEST,
      ignoreCancelled = true
   )
   public void onPlayerShearEntity(PlayerShearEntityEvent e) {
      if (e.getEntity() instanceof Sheep) {
         Sheep sheep = (Sheep)e.getEntity();
         int level = this.getLevel(sheep);

         for(int id : this.sheepHash.keySet()) {
            double amount = (Double)this.sheepHash.get(id);
            amount *= (double)level;
            if (this.dropFix > 0) {
               amount = amount * (double)(100 - this.dropFix + this.r.nextInt(this.dropFix * 2)) / (double)100.0F;
            }

            Location l = sheep.getLocation();
            short color = sheep.getColor().getWoolData();
            l.getWorld().dropItem(l, new ItemStack(id, Math.min((int)amount, Material.WOOL.getMaxStackSize()), color));
         }
      }

   }

   @EventHandler(
      priority = EventPriority.HIGHEST,
      ignoreCancelled = true
   )
   public void onItemSpawn(ItemSpawnEvent e) {
      ItemStack is = e.getEntity().getItemStack();
      if (is != null && is.getTypeId() == 344) {
         Iterator<Location> it = this.tempHash.keySet().iterator();
         long now = System.currentTimeMillis();
         Location l = e.getLocation();

         while(it.hasNext()) {
            Location loc = (Location)it.next();
            if (now - (Long)this.tempHash.get(loc) > 1000L) {
               it.remove();
            } else if (this.getDistance(loc, l) < (double)2.0F) {
               this.tempHash.put(l, now);
               return;
            }
         }

         this.tempHash.put(l, now);
         Chicken c = this.getNearestChicken(e.getEntity());
         if (c != null) {
            int level = this.getLevel(c);
            int add = (int)(this.eggFix * (double)level);
            if (add > 0) {
               ItemStack is2 = new ItemStack(344);
               int size = Material.EGG.getMaxStackSize();

               for(int i = 0; i < add / size; ++i) {
                  ItemStack iss = is2.clone();
                  iss.setAmount(size);
                  l.getWorld().dropItem(l, iss);
               }

               if (add % size != 0) {
                  ItemStack iss = is2.clone();
                  iss.setAmount(add % size);
                  l.getWorld().dropItem(l, iss);
               }
            }
         }
      }

   }

   @EventHandler(
      priority = EventPriority.HIGHEST,
      ignoreCancelled = true
   )
   public void onSheepRegrowWool(SheepRegrowWoolEvent e) {
      if (this.r.nextInt(100) >= this.fixSheep) {
         e.setCancelled(true);
      }

   }

   public void checkFaraway() {
      try {
         for(World w : this.server.getWorlds()) {
            for(Entity e : w.getEntities()) {
               if (e instanceof LivingEntity) {
                  LivingEntity le = (LivingEntity)e;
                  if (le.getRemoveWhenFarAway() && UtilTypes.checkEntity(this.pn, this.cancelRemoveFaraway, e.getType().name())) {
                     le.setRemoveWhenFarAway(false);
                  }
               }
            }
         }
      } catch (InvalidTypeException e) {
         e.printStackTrace();
      }

   }

   private void light(Player p) {
      Location l = (Location)this.lastLightHash.get(p);
      Block old = null;
      if (l != null && l.getWorld().equals(p.getWorld()) && l.distance(p.getLocation()) < (double)100.0F) {
         old = l.getBlock();
      }

      Location ll = p.getLocation();
      if (ll.getBlockY() > 0 && ll.getBlockY() <= 256) {
         ll.add((double)0.0F, (double)-1.0F, (double)0.0F);
         Block b = ll.getBlock();
         if (this.effectList.has(b.getTypeId())) {
            this.lastLightHash.put(p, ll);
            p.sendBlockChange(ll, 89, (byte)0);
            if (old != null && (l.getBlockX() != ll.getBlockX() || l.getBlockY() != ll.getBlockY() || l.getBlockZ() != ll.getBlockZ())) {
               p.sendBlockChange(l, old.getTypeId(), old.getData());
            }
         }

      }
   }

   private Player getNearestPlayer(Location l) {
      Player p = null;
      double distance = (double)9999.0F;

      for(Player pp : l.getWorld().getPlayers()) {
         if (p == null) {
            p = pp;
            distance = pp.getLocation().distance(l);
         } else {
            double dis = pp.getLocation().distance(l);
            if (dis < distance) {
               p = pp;
               distance = dis;
            }
         }
      }

      return p;
   }

   private double getDistance(Location loc, Location l) {
      return !loc.getWorld().equals(l.getWorld()) ? (double)9999.0F : loc.distance(l);
   }

   private Chicken getNearestChicken(Entity e) {
      Chicken result = null;
      double nearest = (double)9999.0F;

      for(Entity ee : e.getNearbyEntities((double)1.5F, (double)1.5F, (double)1.5F)) {
         double distance = e.getLocation().distance(ee.getLocation());
         if (ee.getType().equals(EntityType.CHICKEN) && distance < nearest) {
            result = (Chicken)ee;
            nearest = distance;
         }
      }

      return result;
   }

   private void chat(LivingEntity le) {
      String msg = (String)this.chatList.getRandom();
      msg = msg.substring(0, Math.min(this.chatMaxLength, msg.length()));
      int level = this.getLevel(le);
      String name = UtilFormat.format(this.pn, "tip2", new Object[]{UtilNames.getEntityName(le.getType().getTypeId()), level, msg});
      le.setCustomName(name.substring(0, Math.min(64, name.length())));
      le.setCustomNameVisible(this.chatCustomNameVisiable);
   }

   private void forceLeaveVechile(Player p) {
      p.setPassenger((Entity)null);

      try {
         p.getVehicle().setPassenger((Entity)null);
      } catch (Exception var3) {
      }

   }

   private void drop(List drops, int id, int amount) {
      if (amount > 0) {
         ItemStack is = new ItemStack(id);
         int size = is.getType().getMaxStackSize();

         for(int i = 0; i < amount / size; ++i) {
            ItemStack iss = is.clone();
            iss.setAmount(size);
            drops.add(iss);
         }

         if (amount % size != 0) {
            ItemStack iss = is.clone();
            iss.setAmount(amount % size);
            drops.add(iss);
         }

      }
   }

   private void checkEat() {
      for(World w : this.server.getWorlds()) {
         if (this.eatWorlds.has(w.getName())) {
            Object[] list = w.getEntitiesByClass(Animals.class).toArray();
            int size = list.length;
            if (size >= 1) {
               for(int i = 0; i < this.eatTimerTimes; ++i) {
                  Animals animals = (Animals)list[this.r.nextInt(size)];
                  if (animals.isValid() && animals.isAdult()) {
                     this.checkEat(animals);
                  }
               }
            }
         }
      }

   }

   private void checkEat(Animals animals) {
      Land land = this.landManager.getHighestPriorityLand(animals.getLocation());
      if (land == null || !land.hasFlag("banEat")) {
         try {
            if (UtilTypes.checkEntity(this.pn, this.eatEntity, animals.getType().name())) {
               int sum = 1;
               int level = this.getLevel(animals);
               int min = this.eatMaxLevel;
               Animals minA = null;

               for(Entity ee : animals.getNearbyEntities((double)this.eatRange, (double)this.eatRange, (double)this.eatRange)) {
                  if (ee.getType().equals(animals.getType()) && ee.isValid()) {
                     ++sum;
                     if (((Animals)ee).isAdult()) {
                        int tarLevel = this.getLevel((Animals)ee);
                        if (level + tarLevel < min) {
                           min = level + tarLevel;
                           minA = (Animals)ee;
                        }
                     }

                     if (sum > this.eatMax && minA != null && min <= this.eatMaxLevel) {
                        this.checkEat(animals, minA, min);
                        return;
                     }
                  }
               }

               if (sum > this.eatMax && minA != null) {
                  this.checkEat(animals, minA, Math.min(this.eatMaxLevel, level + this.getLevel(minA)));
               }
            }
         } catch (InvalidTypeException e1) {
            e1.printStackTrace();
         }

      }
   }

   private void checkEat(LivingEntity e, LivingEntity ee, int tarLevel) {
      if (e.getMaxHealth() >= ee.getMaxHealth()) {
         this.eat(e, ee, tarLevel);
      } else {
         this.eat(ee, e, tarLevel);
      }

   }

   private void eat(LivingEntity e, LivingEntity ee, int tarLevel) {
      this.setLevel(e, tarLevel);
      e.setCustomName(UtilFormat.format(this.pn, "tip1", new Object[]{UtilNames.getEntityName(e.getType().getTypeId()), tarLevel}));
      e.setCustomNameVisible(this.chatCustomNameVisiable);
      e.setMaxHealth(e.getMaxHealth() + ee.getMaxHealth());
      switch (this.eatHealthMode) {
         case 0:
         default:
            break;
         case 1:
            e.setHealth(Math.min(e.getMaxHealth(), e.getHealth() + ee.getHealth()));
            break;
         case 2:
            e.setHealth(e.getMaxHealth());
      }

      ee.remove();
      if (this.eatTipEnable) {
         String msg = UtilFormat.format(this.pn, "eatTip", new Object[]{UtilNames.getEntityName(e, false, true), tarLevel, e.getHealth(), e.getMaxHealth()});

         for(Entity eee : e.getNearbyEntities((double)this.eatTipRange, (double)this.eatTipRange, (double)this.eatTipRange)) {
            if (eee instanceof Player) {
               ((Player)eee).sendMessage(msg);
            }
         }
      }

   }

   private int getLevel(LivingEntity le) {
      AttributeInstance ai = ((CraftLivingEntity)le).getHandle().getAttributeInstance(GenericAttributes.a);
      AttributeModifier am = ai.a(UID_LEVEL);
      if (am == null) {
         return 1;
      } else {
         try {
            return Integer.parseInt(am.b());
         } catch (NumberFormatException var5) {
            return 1;
         }
      }
   }

   private void setLevel(LivingEntity le, int level) {
      AttributeInstance ai = ((CraftLivingEntity)le).getHandle().getAttributeInstance(GenericAttributes.a);
      AttributeModifier am = new AttributeModifier(UID_LEVEL, String.valueOf(level), (double)0.0F, 0);
      ai.b(am);
      ai.a(am);
   }

   private void tipDrop(LivingEntity le, int exp, List drops) {
      String dropMsg = "";
      boolean first = true;

      for(ItemStack is : drops) {
         if (first) {
            first = false;
         } else {
            dropMsg = dropMsg + " ";
         }

         dropMsg = dropMsg + UtilFormat.format(this.pn, "drops2", new Object[]{UtilNames.getItemName(is.getTypeId(), is.getDurability()), is.getAmount()});
      }

      String msg = UtilFormat.format(this.pn, "drops1", new Object[]{UtilNames.getEntityName(le, this.customName, true), exp, dropMsg});
      if (!msg.trim().isEmpty()) {
         for(Entity e : le.getNearbyEntities((double)this.dropTipRange, (double)this.dropTipRange, (double)this.dropTipRange)) {
            if (e instanceof Player) {
               ((Player)e).sendMessage(msg);
            }
         }
      }

   }

   private void loadConfig(YamlConfiguration config) {
      this.longestEffect = config.getInt("longestEffect") * 20;
      this.cancelGrow = config.getBoolean("cancelGrow");
      this.cancelBreedType = config.getString("cancelBreed.type");
      this.newWorldTipLimit = config.getInt("newWorldTipLimit");
      this.netherDoorTip = config.getInt("netherDoorTip");
      this.netherDoor = config.getInt("netherDoor");
      this.fixSheep = config.getInt("fixSheep");
      this.stoneGoldPer = config.getString("stoneGold.per");
      this.stoneGoldWorlds = new HashListImpl();

      for(String s : config.getStringList("stoneGold.worlds")) {
         this.stoneGoldWorlds.add(s);
      }

      this.stoneGoldChance = config.getInt("stoneGold.chance");
      this.stoneGoldRadius = config.getInt("stoneGold.radius");
      this.stoneGoldAmount = config.getInt("stoneGold.amount");
      this.stoneGoldId = config.getInt("stoneGold.id");
      this.statsGoodPer = config.getString("statsGood.per");
      this.statsGoodChance = config.getInt("statsGood.chance");
      this.statsGoodMineType = config.getString("statsGood.mineType");
      this.statsGoodToolType = config.getString("statsGood.toolType");
      this.mineGoodPer = config.getString("mineGood.per");
      this.mineGoodChance = config.getInt("mineGood.chance");
      this.mineGoodAdd = config.getInt("mineGood.add");
      this.mineGoodMineType = config.getString("mineGood.mineType");
      this.mineGoodToolType = config.getString("mineGood.toolType");
      this.attackGoodPer = config.getString("attackGood.per");
      this.attackGoodChance = config.getInt("attackGood.chance");
      this.attackGoodAdd = config.getInt("attackGood.add");
      this.lightTipInterval = config.getInt("light.tipInterval");
      this.effectList = new HashListImpl();

      for(int i : config.getIntegerList("light.effect")) {
         this.effectList.add(i);
      }

      this.lightWorlds = new HashListImpl();

      for(String s : config.getStringList("light.worlds")) {
         this.lightWorlds.add(s);
      }

      this.per_fix_admin = config.getString("per_fix_admin");
      this.per_fix_bucket_free = config.getString("per_fix_bucket_free");
      this.per_fix_bucket_water = config.getString("per_fix_bucket_water");
      this.per_fix_bucket_lava = config.getString("per_fix_bucket_lava");
      this.banNaturalMonsterWorlds = new HashListImpl();

      for(String s : config.getStringList("banNaturalMonsterWorlds")) {
         this.banNaturalMonsterWorlds.add(s);
      }

      this.monsterType = config.getString("monsterType");
      this.emptyBucket = config.getBoolean("emptyBucket");
      this.resFree = config.getBoolean("resFree");
      this.costWater = config.getInt("liquidCost.water");
      this.costLava = config.getInt("liquidCost.lava");
      this.dispenseBan = new HashListImpl();

      for(int i : config.getIntegerList("dispenseBan")) {
         this.dispenseBan.add(i);
      }

      this.banUse = new HashListImpl();

      for(int i : config.getIntegerList("banUse")) {
         this.banUse.add(i);
      }

      this.showHealthItem = config.getInt("showHealth.item");
      this.showHealthEntityType = config.getString("showHealth.entityType");
      this.cancelIronGolemTargetZombie = config.getBoolean("cancelIronGolemTargetZombie");
      this.cancelRemoveFaraway = config.getString("cancelRemoveFaraway");
      this.cancelRemoveItem = config.getInt("cancelRemoveItem");
      this.lowerSkeleton = config.getInt("lowerSkeleton");
      this.eatMaxLevel = config.getInt("eat.maxLevel");
      this.eatHealthMode = config.getInt("eat.health.mode");
      this.eatTimerInterval = config.getInt("eat.check.timer.interval");
      this.eatTimerTimes = config.getInt("eat.check.timer.times");
      this.eatTipEnable = config.getBoolean("eat.tip.enable");
      this.eatTipRange = config.getInt("eat.tip.range");
      this.eatMax = config.getInt("eat.max");
      this.eatRange = config.getInt("eat.range");
      this.eatEntity = config.getString("eat.entity");
      this.eatWorlds = new HashListImpl();

      for(String s : config.getStringList("eat.worlds")) {
         this.eatWorlds.add(s);
      }

      this.chatCheckInterval = config.getInt("chat.checkInterval");
      this.chatCustomNameVisiable = config.getBoolean("chat.customNameVisiable");
      this.chatMaxLength = config.getInt("chat.maxLength");
      this.chatList = new ChanceHashListImpl();

      for(String s : config.getStringList("chat.contents")) {
         this.chatList.add(Util.convert(s));
      }

      this.dropFix = config.getInt("drop.fix");
      this.dropMax = config.getInt("drop.max");
      this.egg = config.getInt("drop.egg");
      this.head = config.getInt("drop.head");
      this.dropHash = new HashMap();

      for(int index = 1; config.contains("drop.drop" + index); ++index) {
         int id = config.getInt("drop.drop" + index + ".id");
         int health = config.getInt("drop.drop" + index + ".health");
         HashMap<Integer, Double> dropsHash = new HashMap();

         for(String s : config.getStringList("drop.drop" + index + ".drops")) {
            dropsHash.put(Integer.parseInt(s.split(" ")[0]), Double.parseDouble(s.split(" ")[1]));
         }

         int egg = config.getInt("drop.drop" + index + ".egg");
         int head = config.getInt("drop.drop" + index + ".head");
         String headName = config.getString("drop.drop" + index + ".headName");
         DropInfo dropInfo = new DropInfo(id, health, dropsHash, egg, head, headName);
         this.dropHash.put(id, dropInfo);
      }

      this.sheepHash = new HashMap();

      for(String s : config.getStringList("drop.drop2.shears")) {
         this.sheepHash.put(Integer.parseInt(s.split(" ")[0]), Double.parseDouble(s.split(" ")[1]));
      }

      this.eggFix = config.getDouble("drop.drop4.fix");
      this.enableDropTip = config.getBoolean("dropTip.enable");
      this.dropTipAllow = config.getString("dropTip.allow");
      this.dropTipRange = config.getInt("dropTip.range");
      this.customName = config.getBoolean("dropTip.customName");
      this.enableWorldTip = config.getBoolean("worldTip.enable");
      this.worldTipInterval = config.getInt("worldTip.interval");
      this.worldTipSuccess = config.getInt("worldTip.success");
      this.worldTipHash = new HashMap();

      for(int var18 = 1; config.contains("worldTip.tips.tip" + var18); ++var18) {
         String world = config.getString("worldTip.tips.tip" + var18 + ".world");
         String tip = Util.convert(config.getString("worldTip.tips.tip" + var18 + ".tip"));
         this.worldTipHash.put(world, tip);
      }

      this.healEnable = config.getBoolean("heal.enable");
      this.healId = config.getInt("heal.id");
      this.healCost = config.getString("heal.cost");
      this.healRecovery = config.getInt("heal.recovery");
      this.healInterval = config.getInt("heal.interval");
      this.dropExpVipPer = config.getString("dropExp.vipPer");
      this.dropExpChance = config.getInt("dropExp.chance");
      this.dropExpMulti = config.getInt("dropExp.multi");
      this.mineExpVipPer = config.getString("mineExp.vipPer");
      this.mineExpChance = config.getInt("mineExp.chance");
      this.mineExpMulti = config.getInt("mineExp.multi");
      this.fixSpawner = config.getInt("fixSpawner");
      this.autoMonClearInterval = config.getInt("autoMonClear.interval");
      this.autoMonClearAmount = config.getInt("autoMonClear.amount");
      this.autoMonClearRange = config.getInt("autoMonClear.range");
      this.autoMonClearMax = config.getInt("autoMonClear.max");
      this.villageLife = config.getInt("villageLife");
      this.animalCostEnable = config.getBoolean("animalCost.enable");
      this.animalCostType = config.getString("animalCost.type");
      this.animalCostFree = config.getString("animalCost.free");
      this.animalCostInterval = config.getInt("animalCost.interval");
      this.animalCostChance = config.getInt("animalCost.chance");
      this.animalCostCostChance = config.getInt("animalCost.costChance");
      this.animalCostCost = config.getInt("animalCost.cost");
   }

   private String get(int id) {
      return UtilFormat.format(this.pn, id);
   }

   class DropInfo {
      private int id;
      private int health;
      private HashMap dropsHash;
      private int egg;
      private int head;
      private String headName;

      public DropInfo(int id, int health, HashMap dropsHash, int egg, int head, String headName) {
         super();
         this.id = id;
         this.health = health;
         this.dropsHash = dropsHash;
         this.egg = egg;
         this.head = head;
         this.headName = headName;
      }

      public int getId() {
         return this.id;
      }

      public int getHealth() {
         return this.health;
      }

      public HashMap getDropsHash() {
         return this.dropsHash;
      }

      public int getEgg() {
         return this.egg;
      }

      public int getHead() {
         return this.head;
      }

      public String getHeadName() {
         return this.headName;
      }
   }

   class GiveExp implements Runnable {
      private Player p;
      private int exp;

      public GiveExp(Player p, int exp) {
         super();
         this.p = p;
         this.exp = exp;
      }

      public void run() {
         try {
            this.p.giveExp(this.exp);
         } catch (Exception var2) {
         }

      }
   }

   class DelVillager implements Runnable {
      private Player p;

      public DelVillager(Player p) {
         super();
         this.p = p;
      }

      public void run() {
         try {
            this.p.closeInventory();
            Location l = this.p.getLocation();
            Entity del = null;
            double min = (double)1000.0F;

            for(Entity e : this.p.getNearbyEntities((double)10.0F, (double)10.0F, (double)10.0F)) {
               if (e instanceof Villager && ((Villager)e).getProfession().equals(Profession.LIBRARIAN)) {
                  double dis = e.getLocation().distance(l);
                  if (dis < min) {
                     min = dis;
                     del = e;
                  }
               }
            }

            if (del != null) {
               del.remove();
            }
         } catch (Exception var9) {
         }

      }
   }
}
