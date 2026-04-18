package cus;

import infos.Infos;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Random;
import java.util.UUID;
import land.Land;
import land.Pos;
import landMain.LandMain;
import lib.config.ReloadConfigEvent;
import lib.hashList.ChanceHashList;
import lib.hashList.ChanceHashListImpl;
import lib.realDamage.RealDamageEvent;
import lib.time.TimeEvent;
import lib.util.Util;
import lib.util.UtilConfig;
import lib.util.UtilEco;
import lib.util.UtilFormat;
import lib.util.UtilNames;
import lib.util.UtilPer;
import lib.util.UtilPotions;
import net.minecraft.server.v1_6_R2.EntityCreature;
import net.minecraft.server.v1_6_R2.EntityLiving;
import net.minecraft.server.v1_6_R2.PathEntity;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.World;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.MemorySection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.craftbukkit.v1_6_R2.entity.CraftEntity;
import org.bukkit.craftbukkit.v1_6_R2.entity.CraftLivingEntity;
import org.bukkit.craftbukkit.v1_6_R2.entity.CraftMonster;
import org.bukkit.entity.Creature;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Monster;
import org.bukkit.entity.Player;
import org.bukkit.entity.Wither;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.entity.CreatureSpawnEvent.SpawnReason;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerTeleportEvent.TeleportCause;
import org.bukkit.event.world.ChunkUnloadEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import skill.Skill;

public class CusManager implements Listener {
   private static final String FLAG_BAN_STUPID = "banStupid";
   private static final String FLAG_DROP_ADD = "dropAdd";
   private String pn;
   private Random r = new Random();
   private String per_cus_admin;
   private int setItem;
   private int moveInterval;
   private double near;
   private long maxWaitTime;
   private long maxTpWaitTime;
   private int clearInterval;
   private int tipRange;
   private HashMap animalHash;
   private HashMap speedHash;
   private HashMap dropGoldHash;
   private HashMap dropExpHash;
   private HashMap dropPowerHash;
   private HashMap dropItemHash;
   private HashMap setHash;
   private HashMap lastTimeHash = new HashMap();
   private HashMap lastPosHash = new HashMap();

   public CusManager(Cus cus) {
      super();
      this.pn = cus.getPn();
      this.setHash = new HashMap();
      this.loadConfig(UtilConfig.getConfig(this.pn));
      cus.getPm().registerEvents(this, cus);
      Bukkit.getScheduler().scheduleSyncRepeatingTask(cus, new Runnable() {
         public void run() {
            CusManager.this.checkMove();
         }
      }, (long)this.moveInterval, (long)this.moveInterval);
      Bukkit.getScheduler().scheduleSyncRepeatingTask(cus, new Runnable() {
         public void run() {
            this.checkClearMove();
         }

         private void checkClearMove() {
            CusManager.this.lastPosHash.clear();
            CusManager.this.lastTimeHash.clear();
         }
      }, (long)(this.clearInterval * 20), (long)(this.clearInterval * 20));
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
      priority = EventPriority.LOWEST
   )
   public void onPlayerQuit(PlayerQuitEvent e) {
      this.setHash.remove(e.getPlayer());
   }

   @EventHandler(
      priority = EventPriority.LOWEST,
      ignoreCancelled = true
   )
   public void onCreatureSpawn(CreatureSpawnEvent e) {
      if (e.getEntity() instanceof Monster && !(((CraftMonster)e.getEntity()).getHandle() instanceof CustomMonster) && !(e.getEntity() instanceof Wither)) {
         e.setCancelled(true);
      }

   }

   @EventHandler(
      priority = EventPriority.MONITOR,
      ignoreCancelled = true
   )
   public void onCreatureSpawnMonitor(CreatureSpawnEvent e) {
      try {
         EntityLiving el = ((CraftLivingEntity)e.getEntity()).getHandle();
         if (el instanceof CustomAnimal) {
            int id = e.getEntity().getType().getTypeId();
            if (this.animalHash.containsKey(id)) {
               int health = (Integer)this.animalHash.get(id);
               if (health > 0) {
                  e.getEntity().setMaxHealth((double)health);
                  e.getEntity().setHealth((double)health);
               }
            }

            CustomAnimal ca = (CustomAnimal)el;
            Land land = LandMain.getLandManager().getHighestPriorityLand(e.getLocation());
            if (land != null && land.hasFlag("banStupid")) {
               ca.setAi(2);
            } else {
               ca.setAi(1);
            }
         } else if (el instanceof CustomMonster) {
            CustomMonster cm = (CustomMonster)el;
            cm.setSpawnReason(e.getSpawnReason());
            cm.setCamp(CustomMonster.Camp.bad);
            CustomEntityUtil.clearRangeModifier(e.getEntity());
            int id = e.getEntity().getType().getTypeId();
            if (this.speedHash.containsKey(id)) {
               cm.setSpeed((Double)this.speedHash.get(id));
            } else {
               cm.setSpeed(0.2);
            }

            if (this.dropExpHash.containsKey(id)) {
               int exp = (Integer)this.dropExpHash.get(id);
               cm.setDropExp(exp);
            }

            DropInfo dropInfo = (DropInfo)this.dropItemHash.get(id);
            if (dropInfo != null && dropInfo.getAmountHash() != null) {
               List<ItemStack> drop = new ArrayList();

               for(int itemId : dropInfo.getAmountHash().keySet()) {
                  int amount = (Integer)((ChanceHashList)dropInfo.getAmountHash().get(itemId)).getRandom();
                  if (amount > 0) {
                     drop.add(new ItemStack(itemId, amount));
                  }
               }

               if (!drop.isEmpty()) {
                  cm.setDropItems(drop);
               }
            }

            if (!e.getSpawnReason().equals(SpawnReason.SPAWNER)) {
               cm.setAi(2);
               cm.setRange((double)32.0F);
               if (this.dropPowerHash.containsKey(id)) {
                  cm.setDropPower((Integer)this.dropPowerHash.get(id));
               }

               if (this.dropGoldHash.containsKey(id)) {
                  cm.setDropGold((Integer)this.dropGoldHash.get(id));
               }
            } else {
               Location l = e.getLocation();
               Land land = LandMain.getLandManager().getHighestPriorityLand(l);
               if (land != null && land.hasFlag("banStupid")) {
                  cm.setAi(2);
                  cm.setRange((double)32.0F);
               } else {
                  cm.setAi(1);
                  cm.setRange((double)8.0F);
               }

               if (land != null && land.hasFlag("dropAdd")) {
                  if (this.dropPowerHash.containsKey(id)) {
                     cm.setDropPower((Integer)this.dropPowerHash.get(id) + 1);
                  }

                  if (this.dropGoldHash.containsKey(id)) {
                     cm.setDropGold((Integer)this.dropGoldHash.get(id) + 1);
                  }

                  cm.setDropExp(cm.getDropExp() + 1);
               } else {
                  cm.setDropPower(1);
                  cm.setDropExp(Math.max(1, cm.getDropExp() - 1));
               }
            }

            CustomEntityUtil.save(cm);
         }
      } catch (Exception var10) {
      }

   }

   @EventHandler(
      priority = EventPriority.MONITOR
   )
   public void onRealDamage(RealDamageEvent e) {
      if (e.getDamage() > (double)0.0F) {
         EntityLiving el = ((CraftLivingEntity)e.getVictim()).getHandle();
         if (el instanceof CustomMonster) {
            CustomMonster cm = (CustomMonster)el;
            this.checkSelfPotion(cm);
            this.checkSkillDamage(cm);
         }

         EntityLiving el2 = ((CraftLivingEntity)e.getDamager()).getHandle();
         if (el2 instanceof CustomMonster) {
            CustomMonster cm2 = (CustomMonster)el2;
            this.checkEnemyPotion(cm2, e.getVictim());
            this.checkSkillAttack(cm2, e.getVictim(), e.getDamage());
         }
      }

   }

   @EventHandler(
      priority = EventPriority.LOWEST
   )
   public void onEntityDeathLowest(EntityDeathEvent e) {
      EntityLiving el = ((CraftLivingEntity)e.getEntity()).getHandle();
      if (el instanceof CustomMonster) {
         CustomMonster cm = (CustomMonster)el;
         List<ItemStack> drops = e.getDrops();
         drops.clear();
         if (cm.getDropItems() != null) {
            drops.addAll(cm.getDropItems());
         }

         if (cm.getDropExp() >= 0) {
            e.setDroppedExp(cm.getDropExp());
         }
      } else if (el instanceof CustomEntitySheep && !e.getDrops().isEmpty()) {
         Land land = LandMain.getLandManager().getHighestPriorityLand(e.getEntity().getLocation());
         if (land != null && land.hasFlag("dropAdd")) {
            int value = land.getFlag("dropAdd");
            if (value > 0) {
               ItemStack is = ((ItemStack)e.getDrops().get(0)).clone();
               is.setAmount(value);
               e.getDrops().add(is);
            }
         }
      }

   }

   @EventHandler(
      priority = EventPriority.MONITOR
   )
   public void onEntityDeath(EntityDeathEvent e) {
      EntityLiving el = ((CraftLivingEntity)e.getEntity()).getHandle();
      if (el instanceof CustomMonster) {
         CustomMonster cm = (CustomMonster)el;
         if (cm.isShoot()) {
            List<ItemStack> drops = e.getDrops();
            if (drops != null) {
               World w = e.getEntity().getWorld();

               for(ItemStack item : drops) {
                  Util.ejectRandom(w.dropItem(e.getEntity().getEyeLocation(), item));
                  w.playSound(e.getEntity().getEyeLocation(), Sound.SHOOT_ARROW, 2.3F, 0.6F);
               }

               drops.clear();
            }
         }
      }

      Player killer = e.getEntity().getKiller();
      if (killer != null && el instanceof CustomMonster) {
         CustomMonster cm = (CustomMonster)el;
         int dropPower = cm.getDropPower();
         if (dropPower > 0) {
            Infos.getPlayerInfoManager().addPower(killer.getName(), dropPower);
         }

         int dropGold = cm.getDropGold();
         if (dropGold > 0 && UtilEco.add(killer.getName(), (double)dropGold) && killer.isOnline()) {
            killer.sendMessage(UtilFormat.format(this.pn, "addGold", new Object[]{dropGold}));
         }
      }

   }

   @EventHandler(
      priority = EventPriority.MONITOR,
      ignoreCancelled = true
   )
   public void onChunkUnload(ChunkUnloadEvent e) {
      Entity[] var5;
      for(Entity ee : var5 = e.getChunk().getEntities()) {
         net.minecraft.server.v1_6_R2.Entity entity = ((CraftEntity)ee).getHandle();
         if (entity != null && entity instanceof CustomMonster && entity.isAlive()) {
            CustomMonster cm = (CustomMonster)entity;
            CustomEntityUtil.save(cm);
         }
      }

   }

   @EventHandler(
      priority = EventPriority.HIGHEST,
      ignoreCancelled = true
   )
   public void onPlayerInteractEntity(PlayerInteractEntityEvent e) {
      if (UtilPer.hasPer(e.getPlayer(), this.per_cus_admin) && e.getPlayer().getItemInHand() != null && e.getPlayer().getItemInHand().getTypeId() == this.setItem) {
         net.minecraft.server.v1_6_R2.Entity entity = ((CraftEntity)e.getRightClicked()).getHandle();
         Player p = e.getPlayer();
         if (entity instanceof CustomMonster) {
            CustomMonster cm = (CustomMonster)entity;
            String title = cm.getName();
            if (title == null) {
               title = e.getRightClicked().getType().getName();
            }

            if (title == null) {
               title = "none";
            }

            title = title.substring(0, Math.min(16, title.length()));
            Inventory inv = Bukkit.getServer().createInventory((InventoryHolder)null, 54, title);
            int index = 0;
            if (cm.getDropItems() == null) {
               cm.setDropItems(new ArrayList());
            }

            for(ItemStack is : cm.getDropItems()) {
               inv.setItem(index, is.clone());
               ++index;
               if (index >= 54) {
                  break;
               }
            }

            p.closeInventory();
            p.openInventory(inv);
            this.setHash.put(p, cm);
         } else {
            p.sendMessage(UtilFormat.format(this.pn, "fail", new Object[]{this.get(45)}));
         }
      }

   }

   @EventHandler(
      priority = EventPriority.HIGHEST
   )
   public void onInventoryClose(InventoryCloseEvent e) {
      if (e.getPlayer() instanceof Player && this.setHash.containsKey((Player)e.getPlayer())) {
         CustomMonster cm = (CustomMonster)this.setHash.get((Player)e.getPlayer());
         this.setHash.remove((Player)e.getPlayer());
         if (cm.getEl().isAlive()) {
            List<ItemStack> drop = new ArrayList();

            ItemStack[] var7;
            for(ItemStack is : var7 = e.getInventory().getContents()) {
               if (is != null && is.getTypeId() != 0) {
                  drop.add(is);
               }
            }

            cm.setDropItems(drop);
            CustomEntityUtil.save(cm);
            ((Player)e.getPlayer()).sendMessage(UtilFormat.format(this.pn, "success", new Object[]{this.get(50)}));
         }
      }

   }

   @EventHandler(
      priority = EventPriority.LOWEST
   )
   public void onTime(TimeEvent e) {
      this.checkRecover();
   }

   private void checkSkillDamage(CustomMonster cm) {
      try {
         if (this.r.nextInt(1000) < cm.getDamageChance()) {
            Skill skill = Cus.getSkillManager().getRandomSkill(cm.getDamageSkill());
            if (skill != null) {
               skill.triggerDamage((CraftLivingEntity)cm.getEl().getBukkitEntity());
               String tip = UtilFormat.format(this.pn, "skillDamageTip", new Object[]{CustomEntityUtil.getMonsterName(cm), Cus.getSkillManager().getSkillName(skill.getId())});
               Util.sendMsg(cm.getEl().getBukkitEntity().getLocation(), (double)this.tipRange, false, tip);
            }
         }
      } catch (Exception var4) {
      }

   }

   private void checkSkillAttack(CustomMonster cm, LivingEntity victim, double damage) {
      try {
         if (this.r.nextInt(1000) < cm.getAttackChance()) {
            Skill skill = Cus.getSkillManager().getRandomSkill(cm.getAttackSkill());
            if (skill != null) {
               skill.triggerAttack((CraftLivingEntity)cm.getEl().getBukkitEntity(), victim, damage);
               String tip = UtilFormat.format(this.pn, "skillAttackTip", new Object[]{CustomEntityUtil.getMonsterName(cm), Cus.getSkillManager().getSkillName(skill.getId())});
               Util.sendMsg(cm.getEl().getBukkitEntity().getLocation(), (double)this.tipRange, false, tip);
            }
         }
      } catch (Exception var7) {
      }

   }

   private void checkSelfPotion(CustomMonster cm) {
      try {
         if (cm == null || !cm.getEl().getBukkitEntity().isValid()) {
            return;
         }

         if (this.r.nextInt(1000) < cm.getPotionsSelfChance()) {
            LivingEntity le = (LivingEntity)cm.getEl().getBukkitEntity();
            String type = cm.getPotionSelf();
            if (type == null || type.isEmpty()) {
               return;
            }

            List<PotionEffect> list = UtilPotions.addPotions((CommandSender)null, this.pn, type, le, true, false, false);
            if (list != null) {
               for(PotionEffect pe : list) {
                  String tip = UtilFormat.format(this.pn, "potionSelfTip", new Object[]{CustomEntityUtil.getMonsterName(cm), UtilNames.getPotionName(pe.getType().getId())});
                  Util.sendMsg(le.getLocation(), (double)this.tipRange, false, tip);
               }
            }
         }
      } catch (Exception var8) {
      }

   }

   private void checkEnemyPotion(CustomMonster cm, LivingEntity victim) {
      try {
         if (cm == null || !cm.getEl().getBukkitEntity().isValid() || victim == null || !victim.isValid()) {
            return;
         }

         if (this.r.nextInt(1000) < cm.getPotionsEnemyChance()) {
            LivingEntity le = (LivingEntity)cm.getEl().getBukkitEntity();
            String type = cm.getPotionEnemy();
            if (type == null || type.isEmpty()) {
               return;
            }

            List<PotionEffect> list = UtilPotions.addPotions((CommandSender)null, this.pn, type, victim, true, false, false);
            if (list != null) {
               for(PotionEffect pe : list) {
                  String tip = UtilFormat.format(this.pn, "potionEnemyTip", new Object[]{CustomEntityUtil.getMonsterName(cm), UtilNames.getPotionName(pe.getType().getId())});
                  Util.sendMsg(le.getLocation(), (double)this.tipRange, false, tip);
               }
            }
         }
      } catch (Exception var9) {
      }

   }

   private void checkMove() {
      for(World w : Bukkit.getServer().getWorlds()) {
         for(Entity e : w.getEntities()) {
            if (e.isValid()) {
               net.minecraft.server.v1_6_R2.Entity entity = ((CraftEntity)e).getHandle();
               if (entity instanceof CustomMonster) {
                  CustomMonster cm = (CustomMonster)entity;
                  EntityCreature ec = null;
                  if (cm.getEl() instanceof EntityCreature) {
                     ec = (EntityCreature)cm.getEl();
                     if (ec.getGoalTarget() != null && ec.getGoalTarget().isAlive()) {
                        continue;
                     }
                  }

                  HashMap<Integer, Point> path = cm.getPath();
                  if (path != null && !path.isEmpty()) {
                     int sum = path.size();
                     int nowPath = cm.getNowPath();
                     Point tar = (Point)path.get(nowPath);
                     if (tar != null) {
                        if (this.reach(cm.getEl(), tar)) {
                           if (sum != 1) {
                              ++nowPath;
                              if (nowPath > sum) {
                                 nowPath = 1;
                              }

                              cm.setNowPath(nowPath);
                           }
                        } else if (ec != null && ec.getNavigation().g()) {
                           UUID uid = entity.getUniqueID();
                           if (!this.lastPosHash.containsKey(uid)) {
                              this.lastTimeHash.put(uid, System.currentTimeMillis());
                              this.lastPosHash.put(uid, Pos.getPos(e.getLocation()));
                           } else {
                              Pos prePos = (Pos)this.lastPosHash.get(uid);
                              Pos nowPos = Pos.getPos(e.getLocation());
                              if (!nowPos.equals(prePos)) {
                                 this.lastTimeHash.put(uid, System.currentTimeMillis());
                                 this.lastPosHash.put(uid, nowPos);
                              } else if (this.lastTimeHash.containsKey(uid)) {
                                 long now = System.currentTimeMillis();
                                 long last = now - (Long)this.lastTimeHash.get(uid);
                                 if (last > this.maxTpWaitTime) {
                                    e.teleport(new Location(e.getWorld(), tar.x, tar.y, tar.z), TeleportCause.PLUGIN);
                                    continue;
                                 }

                                 if (last > this.maxWaitTime) {
                                    ++nowPath;
                                    if (nowPath > sum) {
                                       nowPath = 1;
                                    }

                                    cm.setNowPath(nowPath);
                                    continue;
                                 }
                              } else {
                                 this.lastTimeHash.put(uid, System.currentTimeMillis());
                              }
                           }

                           PathEntity pe = ec.getNavigation().a(tar.x, tar.y, tar.z);
                           if (pe != null) {
                              ec.getNavigation().a(pe, (double)1.0F);
                           } else {
                              ++nowPath;
                              if (nowPath > sum) {
                                 nowPath = 1;
                              }

                              cm.setNowPath(nowPath);
                           }
                        }
                     } else if (nowPath != 1) {
                        nowPath = 1;
                        cm.setNowPath(nowPath);
                     }
                  }
               }
            }
         }
      }

   }

   private boolean reach(EntityLiving el, Point tar) {
      try {
         double x = el.locX - tar.x;
         double y = el.locY - tar.y;
         double z = el.locZ - tar.z;
         return Math.sqrt(x * x + y * y + z * z) <= this.near;
      } catch (Exception var9) {
         return true;
      }
   }

   private void checkRecover() {
      for(World w : Bukkit.getServer().getWorlds()) {
         for(Entity e : w.getEntities()) {
            if (e.isValid()) {
               net.minecraft.server.v1_6_R2.Entity entity = ((CraftEntity)e).getHandle();
               if (entity instanceof CustomMonster) {
                  CustomMonster cm = (CustomMonster)entity;
                  float recover = cm.getRecover();
                  if (recover >= 0.01F && e instanceof Creature) {
                     Creature c = (Creature)e;
                     c.setHealth(Math.min(c.getHealth() + (double)recover, c.getMaxHealth()));
                  }
               }
            }
         }
      }

   }

   private void loadConfig(YamlConfiguration config) {
      this.per_cus_admin = config.getString("per_cus_admin");
      this.setItem = config.getInt("setItem");
      this.moveInterval = config.getInt("moveInterval");
      this.near = config.getDouble("near");
      this.maxWaitTime = config.getLong("maxWaitTime");
      this.maxTpWaitTime = config.getLong("maxTpWaitTime");
      this.clearInterval = config.getInt("clearInterval");
      this.tipRange = config.getInt("tipRange");
      this.animalHash = new HashMap();

      for(String s : config.getStringList("animal")) {
         int id = Integer.parseInt(s.split(" ")[0]);
         int health = Integer.parseInt(s.split(" ")[1]);
         this.animalHash.put(id, health);
      }

      this.speedHash = new HashMap();

      for(String s : config.getStringList("speed")) {
         int id = Integer.parseInt(s.split(" ")[0]);
         double speed = Double.parseDouble(s.split(" ")[1]);
         this.speedHash.put(id, speed);
      }

      this.dropGoldHash = new HashMap();
      this.dropPowerHash = new HashMap();

      for(String s : config.getStringList("drop.gold")) {
         int id = Integer.parseInt(s.split(" ")[0]);
         int gold = Integer.parseInt(s.split(" ")[1]);
         this.dropGoldHash.put(id, gold);
      }

      this.dropExpHash = new HashMap();

      for(String s : config.getStringList("drop.exp")) {
         int id = Integer.parseInt(s.split(" ")[0]);
         int exp = Integer.parseInt(s.split(" ")[1]);
         this.dropExpHash.put(id, exp);
      }

      for(String s : config.getStringList("drop.power")) {
         int id = Integer.parseInt(s.split(" ")[0]);
         int power = Integer.parseInt(s.split(" ")[1]);
         this.dropPowerHash.put(id, power);
      }

      this.dropItemHash = new HashMap();
      MemorySection ms = (MemorySection)config.get("drop.item");

      for(String key : ms.getValues(false).keySet()) {
         int id = ms.getInt(key + ".type");
         HashMap<Integer, ChanceHashList<Integer>> amountHash = new HashMap();

         for(String s : ms.getStringList(key + ".drops")) {
            int type = Integer.parseInt(s.split(" ")[0]);
            String check = s.split(" ")[1];
            ChanceHashList<Integer> list = new ChanceHashListImpl();

            String[] var15;
            for(String ss : var15 = check.split(";")) {
               int amount = Integer.parseInt(ss.split("-")[0]);
               int chance = Integer.parseInt(ss.split("-")[1]);
               list.addChance(amount, chance);
            }

            amountHash.put(type, list);
         }

         DropInfo dropInfo = new DropInfo(id, amountHash);
         this.dropItemHash.put(id, dropInfo);
      }

   }

   private String get(int id) {
      return UtilFormat.format(this.pn, id);
   }

   public class DropInfo {
      private int id;
      private HashMap amountHash;

      public DropInfo(int id, HashMap amountHash) {
         super();
         this.id = id;
         this.amountHash = amountHash;
      }

      public int getId() {
         return this.id;
      }

      public HashMap getAmountHash() {
         return this.amountHash;
      }
   }
}
