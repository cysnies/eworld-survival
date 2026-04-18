package strength;

import cus.Cus;
import cus.CustomEntityBlaze;
import cus.CustomEntityCaveSpider;
import cus.CustomEntityCreeper;
import cus.CustomEntityEnderDragon;
import cus.CustomEntityEnderman;
import cus.CustomEntityGhast;
import cus.CustomEntityGiant;
import cus.CustomEntityMagmaCube;
import cus.CustomEntityPigZombie;
import cus.CustomEntitySilverfish;
import cus.CustomEntitySkeleton;
import cus.CustomEntitySlime;
import cus.CustomEntitySpider;
import cus.CustomEntityUtil;
import cus.CustomEntityWitch;
import cus.CustomEntityZombie;
import cus.CustomMonster;
import cus.MonInfo;
import cus.MonPoint;
import cus.Point;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Random;
import land.Land;
import land.Pos;
import landMain.LandMain;
import lib.config.ReloadConfigEvent;
import lib.hashList.HashList;
import lib.hashList.HashListImpl;
import lib.time.TimeEvent;
import lib.util.Util;
import lib.util.UtilConfig;
import lib.util.UtilFormat;
import lib.util.UtilItems;
import lib.util.UtilNames;
import lib.util.UtilPer;
import org.bukkit.Bukkit;
import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.MemorySection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.craftbukkit.v1_6_R2.CraftWorld;
import org.bukkit.craftbukkit.v1_6_R2.entity.CraftCreature;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.bukkit.event.entity.CreatureSpawnEvent.SpawnReason;
import org.bukkit.event.world.ChunkLoadEvent;
import org.bukkit.event.world.ChunkUnloadEvent;
import org.bukkit.inventory.ItemStack;

public class StrengthManager implements Listener {
   private Random r = new Random();
   private String pn;
   private String strengthPath;
   private String per_cus_admin;
   private int showItem;
   private int showPathItem;
   private int pageSize;
   private static HashMap typeHash = new HashMap();
   private HashMap handleHash = new HashMap();
   private static HashMap strengthHash;
   private static HashList monPointList;
   private static HashMap monPointHash;
   private static HashMap posHash;
   private static HashMap monInfoHash;
   private static HashMap monHash;

   static {
      typeHash.put(50, CustomEntityCreeper.class);
      typeHash.put(51, CustomEntitySkeleton.class);
      typeHash.put(52, CustomEntitySpider.class);
      typeHash.put(53, CustomEntityGiant.class);
      typeHash.put(54, CustomEntityZombie.class);
      typeHash.put(55, CustomEntitySlime.class);
      typeHash.put(56, CustomEntityGhast.class);
      typeHash.put(57, CustomEntityPigZombie.class);
      typeHash.put(58, CustomEntityEnderman.class);
      typeHash.put(59, CustomEntityCaveSpider.class);
      typeHash.put(60, CustomEntitySilverfish.class);
      typeHash.put(61, CustomEntityBlaze.class);
      typeHash.put(62, CustomEntityMagmaCube.class);
      typeHash.put(63, CustomEntityEnderDragon.class);
      typeHash.put(66, CustomEntityWitch.class);
   }

   public StrengthManager(Cus cus) {
      super();
      this.pn = cus.getPn();
      this.strengthPath = cus.getPluginPath() + File.separator + this.pn + File.separator + "strength.yml";
      this.loadConfig(UtilConfig.getConfig(this.pn));
      cus.getPm().registerEvents(this, cus);
      this.loadData();
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
   public void onChunkLoad(ChunkLoadEvent e) {
      Entity[] var5;
      for(Entity entity : var5 = e.getChunk().getEntities()) {
         if (monInfoHash.containsKey(entity.getUniqueId())) {
            monHash.put(entity.getUniqueId(), entity);
         }
      }

   }

   @EventHandler(
      priority = EventPriority.MONITOR,
      ignoreCancelled = true
   )
   public void onChunkUnload(ChunkUnloadEvent e) {
      Entity[] var5;
      for(Entity entity : var5 = e.getChunk().getEntities()) {
         MonInfo mi = (MonInfo)monInfoHash.get(entity.getUniqueId());
         if (mi != null) {
            mi.setWorld(e.getWorld().getName());
            mi.setChunkX(e.getChunk().getX());
            mi.setChunkZ(e.getChunk().getZ());
            monHash.remove(entity.getUniqueId());
         }
      }

   }

   @EventHandler(
      priority = EventPriority.LOWEST
   )
   public void onTime(TimeEvent e) {
      for(MonPoint mp : monPointList) {
         if (mp.getInterval() > 0 && TimeEvent.getTime() % (long)mp.getInterval() == 0L && this.r.nextInt(100) < mp.getChance()) {
            World w = Bukkit.getServer().getWorld(mp.getPos().getWorld());
            if (w != null && w.isChunkLoaded(mp.getPos().getX() >> 4, mp.getPos().getZ() >> 4)) {
               this.checkValid(mp);
               if (mp.getMonList().size() < mp.getMax()) {
                  this.spawnMon(mp);
               }
            }
         }
      }

   }

   public void showList(CommandSender sender, int page) {
      if (!(sender instanceof Player) || UtilPer.checkPer((Player)sender, this.per_cus_admin)) {
         if (monPointList.isEmpty()) {
            sender.sendMessage(UtilFormat.format(this.pn, "fail", new Object[]{this.get(215)}));
         } else {
            int maxPage = monPointList.getMaxPage(this.pageSize);
            if (page >= 1 && page <= maxPage) {
               sender.sendMessage(UtilFormat.format(this.pn, "listHeader", new Object[]{this.get(220), page, maxPage}));

               for(MonPoint mp : monPointList.getPage(page, this.pageSize)) {
                  this.showInfo(sender, mp);
               }

            } else {
               sender.sendMessage(UtilFormat.format(this.pn, "pageErr", new Object[]{maxPage}));
            }
         }
      }
   }

   public void create(Player p) {
      if (UtilPer.checkPer(p, this.per_cus_admin)) {
         Pos pos = Pos.getPos(p.getLocation());
         MonPoint mp = (MonPoint)posHash.get(pos);
         if (mp != null) {
            p.sendMessage(UtilFormat.format(this.pn, "fail", new Object[]{this.get(200)}));
         } else {
            mp = new MonPoint(pos);
            Cus.getDao().addOrUpdateMonPoint(mp);
            monPointList.add(mp);
            monPointHash.put(mp.getId(), mp);
            posHash.put(pos, mp);
            p.sendMessage(UtilFormat.format(this.pn, "createSuccess", new Object[]{mp.getId()}));
         }
      }
   }

   public void del(CommandSender sender, long id) {
      if (!(sender instanceof Player) || UtilPer.checkPer((Player)sender, this.per_cus_admin)) {
         MonPoint mp = (MonPoint)monPointHash.get(id);
         if (mp == null) {
            sender.sendMessage(UtilFormat.format(this.pn, "fail", new Object[]{this.get(205)}));
         } else {
            monPointList.remove(mp);
            monPointHash.remove(id);
            posHash.remove(mp.getPos());

            for(MonInfo mi : mp.getMonList()) {
               monInfoHash.remove(mi);
               monHash.remove(mi.getUid());
            }

            Cus.getDao().removeMonPoint(mp);
            sender.sendMessage(UtilFormat.format(this.pn, "success", new Object[]{this.get(210)}));
         }
      }
   }

   public void show(Player p, int range) {
      if (UtilPer.checkPer(p, this.per_cus_admin)) {
         String worldName = p.getWorld().getName();
         Location l = p.getLocation();
         int amount = 0;

         for(MonPoint mp : monPointList) {
            if (worldName.equals(mp.getPos().getWorld()) && l.distance(Pos.toLoc(mp.getPos())) <= (double)range) {
               p.sendBlockChange(Pos.toLoc(mp.getPos()), this.showItem, (byte)0);
               ++amount;
            }
         }

         p.sendMessage(UtilFormat.format(this.pn, "showSuccess", new Object[]{amount}));
      }
   }

   public void strength(CustomMonster cm, StrengthInfo si) {
      if (si != null) {
         try {
            CraftCreature cc = (CraftCreature)cm.getEl().getBukkitEntity();
            if (cc != null) {
               Object obj = si.getData("name");
               if (obj != null) {
                  cm.setName((String)obj);
               }

               obj = si.getData("show");
               if (obj != null && (Boolean)obj) {
                  String landName = this.get(305);
                  Land land = LandMain.getLandManager().getHighestPriorityLand(cc.getLocation());
                  if (land != null) {
                     landName = land.getName();
                  }

                  String monName = UtilNames.getEntityName(cc);
                  String tip = UtilFormat.format(this.pn, "occurTip", new Object[]{landName, monName});
                  Bukkit.getServer().broadcastMessage(tip);
               }

               obj = si.getData("initMaxHealth");
               if (obj != null) {
                  cc.setMaxHealth((double)(Integer)obj);
               }

               obj = si.getData("initHealth");
               if (obj != null) {
                  cc.setHealth((double)(Integer)obj);
               }

               obj = si.getData("levels");
               if (obj != null) {
                  List<DataInfo.LevelInfo> levels = (List)obj;
                  HashMap<Integer, Integer> levelHash = Cus.getLevelManager().getLevels(levels);
                  if (levelHash != null) {
                     cm.setLevelHash(levelHash, true);
                  }
               }

               obj = si.getData("showLevel");
               if (obj != null) {
                  cm.setShowLevel((Boolean)obj);
               }

               obj = si.getData("visible");
               if (obj != null) {
                  cc.setCustomNameVisible((Boolean)obj);
               }

               obj = si.getData("ai");
               if (obj != null) {
                  cm.setAi((Integer)obj);
               }

               obj = si.getData("spawnReason");
               if (obj != null) {
                  cm.setSpawnReason((CreatureSpawnEvent.SpawnReason)obj);
               }

               obj = si.getData("camp");
               if (obj != null) {
                  cm.setCamp((CustomMonster.Camp)obj);
               }

               obj = si.getData("range");
               if (obj != null) {
                  cm.setRange((Double)obj);
               }

               obj = si.getData("speed");
               if (obj != null) {
                  cm.setSpeed((Double)obj);
               }

               obj = si.getData("nowPath");
               if (obj != null) {
                  cm.setNowPath((Integer)obj);
               }

               obj = si.getData("path");
               if (obj != null) {
                  cm.setPath((HashMap)obj);
               }

               obj = si.getData("recover");
               if (obj != null) {
                  cm.setRecover((Float)obj);
               }

               obj = si.getData("potionsSelfChance");
               if (obj != null) {
                  cm.setPotionsSelfChance((Integer)obj);
               }

               obj = si.getData("potionSelf");
               if (obj != null) {
                  cm.setPotionSelf((String)obj);
               }

               obj = si.getData("potionsEnemyChance");
               if (obj != null) {
                  cm.setPotionsEnemyChance((Integer)obj);
               }

               obj = si.getData("potionEnemy");
               if (obj != null) {
                  cm.setPotionEnemy((String)obj);
               }

               obj = si.getData("damageChance");
               if (obj != null) {
                  cm.setDamageChance((Integer)obj);
               }

               obj = si.getData("damageSkill");
               if (obj != null) {
                  cm.setDamageSkill((String)obj);
               }

               obj = si.getData("attackChance");
               if (obj != null) {
                  cm.setAttackChance((Integer)obj);
               }

               obj = si.getData("attackSkill");
               if (obj != null) {
                  cm.setAttackSkill((String)obj);
               }

               obj = si.getData("shoot");
               if (obj != null) {
                  cm.setShoot((Boolean)obj);
               }

               obj = si.getData("dropPowerMin");
               if (obj != null) {
                  int min = (Integer)obj;
                  obj = si.getData("dropPowerMax");
                  if (obj != null) {
                     int max = (Integer)obj;
                     cm.setDropPower(this.r.nextInt(max - min + 1) + min);
                  }
               }

               obj = si.getData("dropGoldMin");
               if (obj != null) {
                  int min = (Integer)obj;
                  obj = si.getData("dropGoldMax");
                  if (obj != null) {
                     int max = (Integer)obj;
                     cm.setDropGold(this.r.nextInt(max - min + 1) + min);
                  }
               }

               obj = si.getData("dropExpMin");
               if (obj != null) {
                  int min = (Integer)obj;
                  obj = si.getData("dropExpMax");
                  if (obj != null) {
                     int max = (Integer)obj;
                     cm.setDropExp(this.r.nextInt(max - min + 1) + min);
                  }
               }

               obj = si.getData("dropItems");
               if (obj != null) {
                  String type = (String)obj;
                  HashList<ItemStack> l = UtilItems.getItems(this.pn, type, true, false);
                  List<ItemStack> list = new ArrayList();

                  for(ItemStack is : l) {
                     list.add(is);
                  }

                  cm.setDropItems(list);
               }

               obj = si.getData("removeFaraway");
               if (obj != null) {
                  cc.setRemoveWhenFarAway((Boolean)obj);
               }

               obj = si.getData("equipWeaponChance");
               if (obj != null) {
                  int chance = (Integer)obj;
                  if (this.r.nextInt(100) < chance) {
                     obj = si.getData("equipWeapon");
                     if (obj != null) {
                        String type = (String)obj;
                        ItemStack is = UtilItems.getItem(this.pn, type);
                        if (is != null) {
                           cc.getEquipment().setItemInHand(is);
                           cc.getEquipment().setItemInHandDropChance(0.0F);
                        }
                     }
                  }
               }

               obj = si.getData("equipHelmetChance");
               if (obj != null) {
                  int chance = (Integer)obj;
                  if (this.r.nextInt(100) < chance) {
                     obj = si.getData("equipHelmet");
                     if (obj != null) {
                        String type = (String)obj;
                        ItemStack is = UtilItems.getItem(this.pn, type);
                        if (is != null) {
                           cc.getEquipment().setHelmet(is);
                           cc.getEquipment().setHelmetDropChance(0.0F);
                        }
                     }
                  }
               }

               obj = si.getData("equipChestplateChance");
               if (obj != null) {
                  int chance = (Integer)obj;
                  if (this.r.nextInt(100) < chance) {
                     obj = si.getData("equipChestplate");
                     if (obj != null) {
                        String type = (String)obj;
                        ItemStack is = UtilItems.getItem(this.pn, type);
                        if (is != null) {
                           cc.getEquipment().setChestplate(is);
                           cc.getEquipment().setChestplateDropChance(0.0F);
                        }
                     }
                  }
               }

               obj = si.getData("equipLeggingsChance");
               if (obj != null) {
                  int chance = (Integer)obj;
                  if (this.r.nextInt(100) < chance) {
                     obj = si.getData("equipLeggings");
                     if (obj != null) {
                        String type = (String)obj;
                        ItemStack is = UtilItems.getItem(this.pn, type);
                        if (is != null) {
                           cc.getEquipment().setLeggings(is);
                           cc.getEquipment().setLeggingsDropChance(0.0F);
                        }
                     }
                  }
               }

               obj = si.getData("equipBootsChance");
               if (obj != null) {
                  int chance = (Integer)obj;
                  if (this.r.nextInt(100) < chance) {
                     obj = si.getData("equipBoots");
                     if (obj != null) {
                        String type = (String)obj;
                        ItemStack is = UtilItems.getItem(this.pn, type);
                        if (is != null) {
                           cc.getEquipment().setBoots(is);
                           cc.getEquipment().setBootsDropChance(0.0F);
                        }
                     }
                  }
               }

               CustomEntityUtil.save(cm);
            }
         } catch (Exception var10) {
         }

      }
   }

   public void tp(Player p, long id) {
      if (UtilPer.checkPer(p, this.per_cus_admin)) {
         MonPoint mp = (MonPoint)monPointHash.get(id);
         if (mp == null) {
            p.sendMessage(UtilFormat.format(this.pn, "fail", new Object[]{this.get(235)}));
         } else {
            p.teleport(Pos.toLoc(mp.getPos()));
            p.sendMessage(UtilFormat.format(this.pn, "success", new Object[]{this.get(245)}));
         }
      }
   }

   public void info(Player p) {
      if (UtilPer.checkPer(p, this.per_cus_admin)) {
         Pos pos = Pos.getPos(p.getLocation());
         MonPoint mp = (MonPoint)posHash.get(pos);
         if (mp == null) {
            p.sendMessage(UtilFormat.format(this.pn, "fail", new Object[]{this.get(225)}));
         } else {
            p.sendMessage(UtilFormat.format(this.pn, "success", new Object[]{this.get(230)}));
            this.showInfo(p, mp);
         }
      }
   }

   public void info(CommandSender sender, long id) {
      if (!(sender instanceof Player) || UtilPer.checkPer((Player)sender, this.per_cus_admin)) {
         MonPoint mp = (MonPoint)monPointHash.get(id);
         if (mp == null) {
            sender.sendMessage(UtilFormat.format(this.pn, "fail", new Object[]{this.get(235)}));
         } else {
            this.showInfo(sender, mp);
         }
      }
   }

   public void setVar(CommandSender sender, long id, String param, String value) {
      try {
         if (sender instanceof Player && !UtilPer.checkPer((Player)sender, this.per_cus_admin)) {
            return;
         }

         MonPoint mp = (MonPoint)monPointHash.get(id);
         if (mp == null) {
            sender.sendMessage(UtilFormat.format(this.pn, "fail", new Object[]{this.get(205)}));
            return;
         }

         if (param.equalsIgnoreCase("type")) {
            mp.setType(value);
         } else if (param.equalsIgnoreCase("mon")) {
            int mon = Integer.parseInt(value);
            mp.setMonType(mon);
         } else if (param.equalsIgnoreCase("check")) {
            int check = Integer.parseInt(value);
            mp.setInterval(check);
         } else if (param.equalsIgnoreCase("chance")) {
            int chance = Integer.parseInt(value);
            if (chance < 0) {
               chance = 0;
            } else if (chance > 100) {
               chance = 100;
            }

            mp.setChance(chance);
         } else if (param.equalsIgnoreCase("max")) {
            int max = Integer.parseInt(value);
            mp.setMax(max);
         }

         sender.sendMessage(UtilFormat.format(this.pn, "success", new Object[]{this.get(265)}));
         Cus.getDao().addOrUpdateMonPoint(mp);
      } catch (Exception var8) {
         sender.sendMessage(UtilFormat.format(this.pn, "fail", new Object[]{this.get(260)}));
      }

   }

   public void clear(CommandSender sender, long id) {
      if (!(sender instanceof Player) || UtilPer.checkPer((Player)sender, this.per_cus_admin)) {
         MonPoint mp = (MonPoint)monPointHash.get(id);
         if (mp == null) {
            sender.sendMessage(UtilFormat.format(this.pn, "fail", new Object[]{this.get(205)}));
         } else {
            for(MonInfo mi : mp.getMonList()) {
               monInfoHash.remove(mi.getUid());
               Entity entity = (Entity)monHash.remove(mi.getUid());
               if (entity != null) {
                  entity.remove();
               } else {
                  World w = Bukkit.getServer().getWorld(mi.getWorld());
                  if (w != null) {
                     Chunk c = w.getChunkAt(mi.getChunkX(), mi.getChunkZ());
                     if (c.load(true)) {
                        Entity[] var13;
                        for(Entity e : var13 = c.getEntities()) {
                           if (e.getUniqueId().compareTo(mi.getUid()) == 0) {
                              e.remove();
                           }
                        }
                     }
                  }
               }
            }

            mp.getMonList().clear();
            Cus.getDao().addOrUpdateMonPoint(mp);
            sender.sendMessage(UtilFormat.format(this.pn, "success", new Object[]{this.get(280)}));
         }
      }
   }

   public void tp(Player p, long id, int index) {
      if (UtilPer.checkPer(p, this.per_cus_admin)) {
         MonPoint mp = (MonPoint)monPointHash.get(id);
         if (mp == null) {
            p.sendMessage(UtilFormat.format(this.pn, "fail", new Object[]{this.get(235)}));
         } else if (index >= 0 && index < mp.getMonList().size()) {
            MonInfo mi = (MonInfo)mp.getMonList().get(index);
            Entity ee = (Entity)monHash.get(mi.getUid());
            if (ee != null) {
               p.teleport(ee.getLocation());
               p.sendMessage(UtilFormat.format(this.pn, "success", new Object[]{this.get(245)}));
            } else {
               World w = Bukkit.getServer().getWorld(mi.getWorld());
               if (w != null) {
                  Chunk c = w.getChunkAt(mi.getChunkX(), mi.getChunkZ());
                  if (c.load(true)) {
                     Entity[] var13;
                     for(Entity entity : var13 = c.getEntities()) {
                        if (entity.getUniqueId().compareTo(mi.getUid()) == 0) {
                           p.teleport(entity);
                           p.sendMessage(UtilFormat.format(this.pn, "success", new Object[]{this.get(245)}));
                           return;
                        }
                     }
                  }
               }

               p.sendMessage(UtilFormat.format(this.pn, "fail", new Object[]{this.get(295)}));
            }
         } else {
            p.sendMessage(UtilFormat.format(this.pn, "fail", new Object[]{this.get(295)}));
         }
      }
   }

   public void sel(Player p, String type) {
      if (UtilPer.checkPer(p, this.per_cus_admin)) {
         StrengthInfo si = getStrengthInfo(type);
         if (si == null) {
            p.sendMessage(UtilFormat.format(this.pn, "fail", new Object[]{this.get(335)}));
         } else if (this.handleHash.containsKey(p) && ((String)this.handleHash.get(p)).equals(type)) {
            p.sendMessage(UtilFormat.format(this.pn, "hasSel", new Object[]{type}));
         } else {
            this.handleHash.put(p, type);
            p.sendMessage(UtilFormat.format(this.pn, "selSuccess", new Object[]{type}));
         }
      }
   }

   public void showSel(Player p) {
      if (UtilPer.checkPer(p, this.per_cus_admin)) {
         if (!this.handleHash.containsKey(p)) {
            p.sendMessage(UtilFormat.format(this.pn, "fail", new Object[]{this.get(330)}));
         } else {
            p.sendMessage(UtilFormat.format(this.pn, "showSel", new Object[]{this.handleHash.get(p)}));
         }
      }
   }

   public void addPath(Player p) {
      if (UtilPer.checkPer(p, this.per_cus_admin)) {
         if (!this.handleHash.containsKey(p)) {
            p.sendMessage(UtilFormat.format(this.pn, "fail", new Object[]{this.get(330)}));
         } else {
            String type = (String)this.handleHash.get(p);
            StrengthInfo si = getStrengthInfo(type);
            if (si == null) {
               this.handleHash.remove(p);
               p.sendMessage(UtilFormat.format(this.pn, "fail", new Object[]{this.get(335)}));
            } else {
               YamlConfiguration config = new YamlConfiguration();

               try {
                  config.load(this.strengthPath);
                  Object obj = si.getDataExact("path");
                  HashMap<Integer, Point> path;
                  if (obj != null) {
                     path = (HashMap)obj;
                  } else {
                     path = new HashMap();
                  }

                  Location l = p.getLocation();
                  path.put(path.size() + 1, new Point(l.getX(), l.getY(), l.getZ(), l.getYaw(), l.getPitch()));
                  si.setData("path", path);
                  List<String> pathList = new ArrayList();

                  for(int index : path.keySet()) {
                     Point point = (Point)path.get(index);
                     double x = Util.getDouble(point.x, 2);
                     double y = Util.getDouble(point.y, 2);
                     double z = Util.getDouble(point.z, 2);
                     float yaw = (float)Util.getDouble((double)point.yaw, 2);
                     float pitch = (float)Util.getDouble((double)point.pitch, 2);
                     pathList.add(x + " " + y + " " + z + " " + yaw + " " + pitch);
                  }

                  config.set("strength." + type + ".data.path", pathList);
                  config.save(this.strengthPath);
                  p.sendMessage(UtilFormat.format(this.pn, "addPath", new Object[]{type, path.size()}));
               } catch (FileNotFoundException e) {
                  e.printStackTrace();
               } catch (IOException e) {
                  e.printStackTrace();
               } catch (InvalidConfigurationException e) {
                  e.printStackTrace();
               }

            }
         }
      }
   }

   public void clearPath(Player p) {
      if (UtilPer.checkPer(p, this.per_cus_admin)) {
         if (!this.handleHash.containsKey(p)) {
            p.sendMessage(UtilFormat.format(this.pn, "fail", new Object[]{this.get(330)}));
         } else {
            String type = (String)this.handleHash.get(p);
            StrengthInfo si = getStrengthInfo(type);
            if (si == null) {
               this.handleHash.remove(p);
               p.sendMessage(UtilFormat.format(this.pn, "fail", new Object[]{this.get(335)}));
            } else {
               Object obj = si.getDataExact("path");
               HashMap<Integer, Point> path;
               if (obj != null) {
                  path = (HashMap)obj;
               } else {
                  path = new HashMap();
               }

               if (path.isEmpty()) {
                  p.sendMessage(UtilFormat.format(this.pn, "noPath", new Object[]{type}));
               } else {
                  path.clear();
                  YamlConfiguration config = new YamlConfiguration();

                  try {
                     config.load(this.strengthPath);
                     si.setData("path", path);
                     List<String> pathList = new ArrayList();
                     config.set("strength." + type + ".data.path", pathList);
                     config.save(this.strengthPath);
                     p.sendMessage(UtilFormat.format(this.pn, "clearPath", new Object[]{type}));
                  } catch (FileNotFoundException e) {
                     e.printStackTrace();
                  } catch (IOException e) {
                     e.printStackTrace();
                  } catch (InvalidConfigurationException e) {
                     e.printStackTrace();
                  }

               }
            }
         }
      }
   }

   public void showPath(Player p) {
      if (UtilPer.checkPer(p, this.per_cus_admin)) {
         if (!this.handleHash.containsKey(p)) {
            p.sendMessage(UtilFormat.format(this.pn, "fail", new Object[]{this.get(330)}));
         } else {
            String type = (String)this.handleHash.get(p);
            StrengthInfo si = getStrengthInfo(type);
            if (si == null) {
               this.handleHash.remove(p);
               p.sendMessage(UtilFormat.format(this.pn, "fail", new Object[]{this.get(335)}));
            } else {
               Object obj = si.getDataExact("path");
               HashMap<Integer, Point> path;
               if (obj != null) {
                  path = (HashMap)obj;
               } else {
                  path = new HashMap();
               }

               if (path.isEmpty()) {
                  p.sendMessage(UtilFormat.format(this.pn, "noPath", new Object[]{type}));
               } else {
                  for(int index : path.keySet()) {
                     Point point = (Point)path.get(index);
                     Location l = new Location(p.getWorld(), point.x, point.y, point.z);
                     p.sendBlockChange(l, this.showPathItem, (byte)0);
                  }

                  p.sendMessage(UtilFormat.format(this.pn, "showPath", new Object[]{type, path.size()}));
               }
            }
         }
      }
   }

   public static StrengthInfo getStrengthInfo(String name) {
      return name != null && !name.isEmpty() ? (StrengthInfo)strengthHash.get(name) : null;
   }

   private void spawnMon(MonPoint mp) {
      try {
         World w = Bukkit.getServer().getWorld(mp.getPos().getWorld());
         if (w != null) {
            int x = mp.getPos().getX() >> 4;
            int z = mp.getPos().getZ() >> 4;
            if (w.isChunkLoaded(x, z)) {
               Location l = Pos.toLoc(mp.getPos());
               Chunk c = l.getChunk();
               if (c.load(true)) {
                  Class cl = (Class)typeHash.get(mp.getMonType());
                  if (cl != null) {
                     Constructor con = cl.getDeclaredConstructor(net.minecraft.server.v1_6_R2.World.class);
                     CustomMonster cm = (CustomMonster)con.newInstance(((CraftWorld)w).getHandle());
                     cm.getEl().setLocation(l.getX(), l.getY(), l.getZ(), l.getYaw(), l.getPitch());
                     ((CraftWorld)w).getHandle().addEntity(cm.getEl(), SpawnReason.CUSTOM);
                     Entity entity = cm.getEl().getBukkitEntity();
                     this.strength(cm, getStrengthInfo(mp.getType()));
                     MonInfo mi = new MonInfo(mp.getId(), entity.getUniqueId(), mp.getPos().getWorld(), c.getX(), c.getZ());
                     mp.getMonList().add(mi);
                     monInfoHash.put(entity.getUniqueId(), mi);
                     monHash.put(entity.getUniqueId(), entity);
                     Cus.getDao().addOrUpdateMonPoint(mp);
                  }
               }
            }
         }
      } catch (Exception var12) {
      }

   }

   private void checkValid(MonPoint mp) {
      Iterator<MonInfo> it = mp.getMonList().iterator();
      boolean update = false;

      while(it.hasNext()) {
         MonInfo mi = (MonInfo)it.next();
         Entity e = (Entity)monHash.get(mi.getUid());
         if (e == null || !e.isValid()) {
            World w = Bukkit.getWorld(mi.getWorld());
            if (w == null || w.isChunkLoaded(mi.getChunkX(), mi.getChunkZ())) {
               it.remove();
               monInfoHash.remove(mi.getUid());
               monHash.remove(mi.getUid());
               update = true;
            }
         }
      }

      if (update) {
         Cus.getDao().addOrUpdateMonPoint(mp);
      }

   }

   private void showInfo(CommandSender sender, MonPoint mp) {
      String type = mp.getType();
      if (type == null) {
         type = this.get(240);
      }

      sender.sendMessage(UtilFormat.format(this.pn, "monList", new Object[]{mp.getPos().getWorld(), mp.getPos().getX(), mp.getPos().getY(), mp.getPos().getZ(), mp.getId(), type, mp.getMonType(), mp.getInterval(), mp.getChance(), mp.getMonList().size(), mp.getMax()}));
   }

   private void loadConfig(YamlConfiguration config) {
      this.per_cus_admin = config.getString("per_cus_admin");
      this.showItem = config.getInt("showItem");
      this.showPathItem = config.getInt("showPathItem");
      this.pageSize = config.getInt("pageSize");

      try {
         YamlConfiguration strengthConfig = new YamlConfiguration();
         strengthConfig.load(this.strengthPath);
         strengthHash = new HashMap();
         MemorySection ms = (MemorySection)strengthConfig.get("strength");
         if (ms != null) {
            for(String key : ms.getValues(false).keySet()) {
               String inherit = ms.getString(key + ".inherit");
               HashList<String> data = new HashListImpl();
               String path = key + ".data.show";
               boolean show = false;
               if (ms.contains(path)) {
                  data.add("show");
                  show = ms.getBoolean(path);
               }

               path = key + ".data.ai";
               int ai = 2;
               if (ms.contains(path)) {
                  data.add("ai");
                  ai = ms.getInt(path);
               }

               path = key + ".data.spawnReason";
               CreatureSpawnEvent.SpawnReason spawnReason = SpawnReason.CUSTOM;

               try {
                  if (ms.contains(path)) {
                     data.add("spawnReason");
                     spawnReason = SpawnReason.valueOf(ms.getString(path));
                  }
               } catch (Exception var59) {
               }

               path = key + ".data.camp";
               CustomMonster.Camp camp = CustomMonster.Camp.none;

               try {
                  if (ms.contains(path)) {
                     data.add("camp");
                     camp = CustomMonster.Camp.valueOf(ms.getString(path));
                  }
               } catch (Exception var58) {
               }

               path = key + ".data.range";
               double range = (double)32.0F;
               if (ms.contains(path)) {
                  data.add("range");
                  range = ms.getDouble(path);
               }

               path = key + ".data.speed";
               double speed = 0.23;
               if (ms.contains(path)) {
                  data.add("speed");
                  speed = ms.getDouble(path);
               }

               path = key + ".data.nowPath";
               int nowPath = 1;
               if (ms.contains(path)) {
                  data.add("nowPath");
                  nowPath = ms.getInt(path);
               }

               path = key + ".data.path";
               HashMap<Integer, Point> pathHash = new HashMap();

               try {
                  if (ms.contains(path)) {
                     data.add("path");
                     int index = 1;

                     for(String s : ms.getStringList(path)) {
                        double x = Double.parseDouble(s.split(" ")[0]);
                        double y = Double.parseDouble(s.split(" ")[1]);
                        double z = Double.parseDouble(s.split(" ")[2]);
                        float yaw = (float)Double.parseDouble(s.split(" ")[3]);
                        float pitch = (float)Double.parseDouble(s.split(" ")[4]);
                        Point p = new Point(x, y, z, yaw, pitch);
                        pathHash.put(index, p);
                        ++index;
                     }
                  }
               } catch (Exception var60) {
               }

               path = key + ".data.name";
               String name = null;
               if (ms.contains(path)) {
                  data.add("name");
                  name = Util.convert(ms.getString(path));
               }

               path = key + ".data.showLevel";
               boolean showLevel = true;
               if (ms.contains(path)) {
                  data.add("showLevel");
                  showLevel = ms.getBoolean(path);
               }

               path = key + ".data.levels";
               List<DataInfo.LevelInfo> levels = new ArrayList();

               try {
                  if (ms.contains(path)) {
                     data.add("levels");

                     for(String s : ms.getStringList(path)) {
                        int id = Integer.parseInt(s.split(" ")[0]);
                        int min = Integer.parseInt(s.split(" ")[1].split("-")[0]);
                        int max = Integer.parseInt(s.split(" ")[1].split("-")[1]);
                        int chance = Integer.parseInt(s.split(" ")[2]);
                        levels.add(new DataInfo.LevelInfo(id, min, max, chance));
                     }
                  }
               } catch (Exception var61) {
               }

               path = key + ".data.recover";
               float recover = 0.0F;
               if (ms.contains(path)) {
                  data.add("recover");
                  recover = (float)ms.getDouble(path);
               }

               path = key + ".data.potions.selfChance";
               int potionsSelfChance = 0;
               if (ms.contains(path)) {
                  data.add("potionsSelfChance");
                  potionsSelfChance = ms.getInt(path);
               }

               path = key + ".data.potions.self";
               String potionSelf = null;
               if (ms.contains(path)) {
                  data.add("potionSelf");
                  potionSelf = ms.getString(path);
               }

               path = key + ".data.potions.enemyChance";
               int potionsEnemyChance = 0;
               if (ms.contains(path)) {
                  data.add("potionsEnemyChance");
                  potionsEnemyChance = ms.getInt(path);
               }

               path = key + ".data.potions.enemy";
               String potionEnemy = null;
               if (ms.contains(path)) {
                  data.add("potionEnemy");
                  potionEnemy = ms.getString(path);
               }

               path = key + ".data.skills.damageChance";
               int damageChance = 0;
               if (ms.contains(path)) {
                  data.add("damageChance");
                  damageChance = ms.getInt(path);
               }

               path = key + ".data.skills.damageSkill";
               String damageSkill = null;
               if (ms.contains(path)) {
                  data.add("damageSkill");
                  damageSkill = ms.getString(path);
               }

               path = key + ".data.skills.attackChance";
               int attackChance = 0;
               if (ms.contains(path)) {
                  data.add("attackChance");
                  attackChance = ms.getInt(path);
               }

               path = key + ".data.skills.attackSkill";
               String attackSkill = null;
               if (ms.contains(path)) {
                  data.add("attackSkill");
                  attackSkill = ms.getString(path);
               }

               path = key + ".data.shoot";
               boolean shoot = false;
               if (ms.contains(path)) {
                  data.add("shoot");
                  shoot = ms.getBoolean(path);
               }

               path = key + ".data.drop.power";
               int dropPowerMin = 0;
               int dropPowerMax = 0;

               try {
                  if (ms.contains(path)) {
                     data.add("dropPowerMin");
                     data.add("dropPowerMax");
                     dropPowerMin = Integer.parseInt(ms.getString(path).split("-")[0]);
                     dropPowerMax = Integer.parseInt(ms.getString(path).split("-")[1]);
                  }
               } catch (Exception var57) {
               }

               path = key + ".data.drop.gold";
               int dropGoldMin = 0;
               int dropGoldMax = 0;

               try {
                  if (ms.contains(path)) {
                     data.add("dropGoldMin");
                     data.add("dropGoldMax");
                     dropGoldMin = Integer.parseInt(ms.getString(path).split("-")[0]);
                     dropGoldMax = Integer.parseInt(ms.getString(path).split("-")[1]);
                  }
               } catch (Exception var56) {
               }

               path = key + ".data.drop.exp";
               int dropExpMin = 0;
               int dropExpMax = 0;

               try {
                  if (ms.contains(path)) {
                     data.add("dropExpMin");
                     data.add("dropExpMax");
                     dropExpMin = Integer.parseInt(ms.getString(path).split("-")[0]);
                     dropExpMax = Integer.parseInt(ms.getString(path).split("-")[1]);
                  }
               } catch (Exception var55) {
               }

               path = key + ".data.drop.items";
               String dropItems = null;
               if (ms.contains(path)) {
                  data.add("dropItems");
                  dropItems = ms.getString(path);
               }

               path = key + ".data.visible";
               boolean visible = false;
               if (ms.contains(path)) {
                  data.add("visible");
                  visible = ms.getBoolean(path);
               }

               path = key + ".data.removeFaraway";
               boolean removeFaraway = false;
               if (ms.contains(path)) {
                  data.add("removeFaraway");
                  removeFaraway = ms.getBoolean(path);
               }

               path = key + ".data.initHealth";
               int initHealth = 20;
               if (ms.contains(path)) {
                  data.add("initHealth");
                  initHealth = ms.getInt(path);
               }

               path = key + ".data.initMaxHealth";
               int initMaxHealth = 20;
               if (ms.contains(path)) {
                  data.add("initMaxHealth");
                  initMaxHealth = ms.getInt(path);
               }

               path = key + ".data.equip.weaponChance";
               int equipWeaponChance = 0;
               if (ms.contains(path)) {
                  data.add("equipWeaponChance");
                  equipWeaponChance = ms.getInt(path);
               }

               path = key + ".data.equip.weapon";
               String equipWeapon = null;
               if (ms.contains(path)) {
                  data.add("equipWeapon");
                  equipWeapon = ms.getString(path);
               }

               path = key + ".data.equip.helmetChance";
               int equipHelmetChance = 0;
               if (ms.contains(path)) {
                  data.add("equipHelmetChance");
                  equipHelmetChance = ms.getInt(path);
               }

               path = key + ".data.equip.helmet";
               String equipHelmet = null;
               if (ms.contains(path)) {
                  data.add("equipHelmet");
                  equipHelmet = ms.getString(path);
               }

               path = key + ".data.equip.chestplateChance";
               int equipChestplateChance = 0;
               if (ms.contains(path)) {
                  data.add("equipChestplateChance");
                  equipChestplateChance = ms.getInt(path);
               }

               path = key + ".data.equip.chestplate";
               String equipChestplate = null;
               if (ms.contains(path)) {
                  data.add("equipChestplate");
                  equipChestplate = ms.getString(path);
               }

               path = key + ".data.equip.leggingsChance";
               int equipLeggingsChance = 0;
               if (ms.contains(path)) {
                  data.add("equipLeggingsChance");
                  equipLeggingsChance = ms.getInt(path);
               }

               path = key + ".data.equip.leggings";
               String equipLeggings = null;
               if (ms.contains(path)) {
                  data.add("equipLeggings");
                  equipLeggings = ms.getString(path);
               }

               path = key + ".data.equip.bootsChance";
               int equipBootsChance = 0;
               if (ms.contains(path)) {
                  data.add("equipBootsChance");
                  equipBootsChance = ms.getInt(path);
               }

               path = key + ".data.equip.boots";
               String equipBoots = null;
               if (ms.contains(path)) {
                  data.add("equipBoots");
                  equipBoots = ms.getString(path);
               }

               DataInfo di = new DataInfo(show, ai, spawnReason, camp, range, speed, nowPath, pathHash, name, showLevel, levels, recover, potionsSelfChance, potionSelf, potionsEnemyChance, potionEnemy, damageChance, damageSkill, attackChance, attackSkill, shoot, dropPowerMin, dropPowerMax, dropGoldMin, dropGoldMax, dropExpMin, dropExpMax, dropItems, visible, removeFaraway, initHealth, initMaxHealth, equipWeaponChance, equipWeapon, equipHelmetChance, equipHelmet, equipChestplateChance, equipChestplate, equipLeggingsChance, equipLeggings, equipBootsChance, equipBoots);
               StrengthInfo si = new StrengthInfo(key, inherit, data, di);
               strengthHash.put(si.getType(), si);
            }
         }
      } catch (FileNotFoundException e) {
         e.printStackTrace();
      } catch (IOException e) {
         e.printStackTrace();
      } catch (InvalidConfigurationException e) {
         e.printStackTrace();
      }

   }

   private void loadData() {
      monPointList = new HashListImpl();
      monPointHash = new HashMap();
      posHash = new HashMap();
      monInfoHash = new HashMap();
      monHash = new HashMap();

      for(MonPoint mp : Cus.getDao().getAllMonPoints()) {
         boolean update = false;
         monPointList.add(mp);
         monPointHash.put(mp.getId(), mp);
         posHash.put(mp.getPos(), mp);
         Iterator<MonInfo> it = mp.getMonList().iterator();

         while(it.hasNext()) {
            MonInfo mi = (MonInfo)it.next();
            if (mi.getPoint() != mp.getId()) {
               mi.setPoint(mp.getId());
               update = true;
            }

            if (monInfoHash.containsKey(mi.getUid())) {
               it.remove();
               update = true;
            } else {
               monInfoHash.put(mi.getUid(), mi);
            }
         }

         if (update) {
            Cus.getDao().addOrUpdateMonPoint(mp);
         }
      }

   }

   private String get(int id) {
      return UtilFormat.format(this.pn, id);
   }
}
