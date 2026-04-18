package cus;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;
import lib.util.UtilItems;
import lib.util.UtilNames;
import net.minecraft.server.v1_6_R2.AttributeInstance;
import net.minecraft.server.v1_6_R2.AttributeModifier;
import net.minecraft.server.v1_6_R2.Chunk;
import net.minecraft.server.v1_6_R2.EntityCreature;
import net.minecraft.server.v1_6_R2.EntityHuman;
import net.minecraft.server.v1_6_R2.EntityLiving;
import net.minecraft.server.v1_6_R2.GenericAttributes;
import net.minecraft.server.v1_6_R2.PathfinderGoalSelector;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.MemorySection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.craftbukkit.v1_6_R2.entity.CraftLivingEntity;
import org.bukkit.craftbukkit.v1_6_R2.util.UnsafeList;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.event.entity.CreatureSpawnEvent.SpawnReason;
import org.bukkit.inventory.ItemStack;

public class CustomEntityUtil {
   static Field gsa;
   private static final UUID uid;
   private static Cus cus;

   static {
      try {
         gsa = PathfinderGoalSelector.class.getDeclaredField("a");
         gsa.setAccessible(true);
      } catch (NoSuchFieldException e) {
         e.printStackTrace();
      } catch (SecurityException e) {
         e.printStackTrace();
      }

      uid = UUID.fromString("8c254b48-89f3-4b54-86fb-deb178b5ed16");
   }

   public CustomEntityUtil() {
      super();
   }

   public static void init(Cus cus) {
      CustomEntityUtil.cus = cus;
   }

   public static Opposite getOpposite(EntityLiving el1, EntityLiving el2) {
      if (el1 != null && el2 != null) {
         boolean b1;
         if (el1 instanceof EntityHuman) {
            b1 = true;
         } else {
            if (!(el1 instanceof CustomMonster)) {
               return CustomEntityUtil.Opposite.no;
            }

            CustomMonster cm = (CustomMonster)el1;
            if (cm.getCamp() == null) {
               b1 = false;
            } else {
               if (cm.getCamp().equals(CustomMonster.Camp.none)) {
                  return CustomEntityUtil.Opposite.no;
               }

               b1 = cm.getCamp().equals(CustomMonster.Camp.good);
            }
         }

         boolean b2;
         if (el2 instanceof EntityHuman) {
            b2 = true;
         } else {
            if (!(el2 instanceof CustomMonster)) {
               return CustomEntityUtil.Opposite.no;
            }

            CustomMonster cm = (CustomMonster)el2;
            if (cm.getCamp() == null) {
               b2 = false;
            } else {
               if (cm.getCamp().equals(CustomMonster.Camp.none)) {
                  return CustomEntityUtil.Opposite.no;
               }

               b2 = cm.getCamp().equals(CustomMonster.Camp.good);
            }
         }

         return b1 ^ b2 ? CustomEntityUtil.Opposite.yes : CustomEntityUtil.Opposite.no;
      } else {
         return CustomEntityUtil.Opposite.none;
      }
   }

   public static boolean isOpposite(CustomMonster.Camp camp1, CustomMonster.Camp camp2) {
      if (camp1 != null && camp2 != null) {
         return camp1.equals(CustomMonster.Camp.good) && camp2.equals(CustomMonster.Camp.bad) || camp1.equals(CustomMonster.Camp.bad) && camp2.equals(CustomMonster.Camp.good);
      } else {
         return false;
      }
   }

   public static UUID getUid() {
      return uid;
   }

   public static EntityLiving getNearestTarget(EntityLiving el, boolean opposite) {
      if (!(el instanceof CustomMonster)) {
         return null;
      } else {
         CustomMonster cm = (CustomMonster)el;
         int seeChunk = (int)(cm.getRange() / (double)16.0F) + 1;
         int chunkX = (int)(el.locX / (double)16.0F);
         if (el.locX < (double)0.0F) {
            --chunkX;
         }

         int chunkZ = (int)(el.locZ / (double)16.0F);
         if (el.locZ < (double)0.0F) {
            --chunkZ;
         }

         LivingEntity le = null;
         double minDistance = (double)-1.0F;
         Location l = new Location(el.world.getWorld(), el.locX, el.locY, el.locZ);

         for(int x = chunkX - seeChunk; x <= chunkX + seeChunk; ++x) {
            for(int z = chunkZ - seeChunk; z <= chunkZ + seeChunk; ++z) {
               Chunk c = el.world.getChunkAt(x, z);
               if (c != null) {
                  try {
                     Entity[] var16;
                     for(Entity entity : var16 = c.bukkitChunk.getEntities()) {
                        if (entity instanceof LivingEntity) {
                           double dis = entity.getLocation().distance(l);
                           if (le == null || dis < minDistance) {
                              LivingEntity temp = (LivingEntity)entity;
                              EntityLiving el2 = ((CraftLivingEntity)temp).getHandle();
                              if (!opposite || getOpposite(el2, el).equals(CustomEntityUtil.Opposite.yes)) {
                                 le = temp;
                                 minDistance = dis;
                              }
                           }
                        }
                     }
                  } catch (Exception var21) {
                  }
               }
            }
         }

         if (le != null) {
            return ((CraftLivingEntity)le).getHandle();
         } else {
            return null;
         }
      }
   }

   public static void clearTarget(PathfinderGoalSelector goal, PathfinderGoalSelector target) {
      try {
         gsa.set(goal, new UnsafeList());
         gsa.set(target, new UnsafeList());
      } catch (IllegalArgumentException e) {
         e.printStackTrace();
      } catch (IllegalAccessException e) {
         e.printStackTrace();
      }

   }

   public static void clearRangeModifier(Entity entity) {
      AttributeInstance ai = ((CraftLivingEntity)entity).getHandle().getAttributeInstance(GenericAttributes.b);

      for(Object obj : ai.c()) {
         AttributeModifier m = (AttributeModifier)obj;
         ai.b(m);
      }

   }

   public static Field getGsa() {
      return gsa;
   }

   public static void save(CustomMonster cm) {
      EntityLiving el = cm.getEl();
      YamlConfiguration config = cm.getConfig();
      if (config != null) {
         AttributeInstance ai = el.getAttributeInstance(GenericAttributes.a);
         if (ai != null) {
            String data = config.saveToString();
            if (data != null && !data.isEmpty()) {
               AttributeModifier am = new AttributeModifier(getUid(), data, (double)0.0F, 0);
               ai.b(am);
               ai.a(am);
            }
         }

      }
   }

   public static void load(CustomMonster cm) {
      EntityLiving el = cm.getEl();
      AttributeInstance ai = el.getAttributeInstance(GenericAttributes.a);
      if (ai != null) {
         AttributeModifier am = ai.a(getUid());
         if (am != null) {
            String data = am.b();
            if (data != null && !data.isEmpty()) {
               try {
                  YamlConfiguration config = cm.getConfig();
                  config.loadFromString(data);

                  try {
                     if (config.contains("ai")) {
                        cm.setAi(config.getInt("ai"));
                     }
                  } catch (Exception var39) {
                  }

                  try {
                     if (config.contains("spawnReason")) {
                        cm.setSpawnReason(SpawnReason.valueOf(config.getString("spawnReason")));
                     }
                  } catch (Exception var38) {
                  }

                  try {
                     if (config.contains("camp")) {
                        cm.setCamp(CustomMonster.Camp.valueOf(config.getString("camp")));
                     }
                  } catch (Exception var37) {
                  }

                  try {
                     if (config.contains("range")) {
                        cm.setRange(config.getDouble("range"));
                     }
                  } catch (Exception var36) {
                  }

                  try {
                     if (config.contains("speed")) {
                        cm.setSpeed(config.getDouble("speed"));
                     }
                  } catch (Exception var35) {
                  }

                  try {
                     if (config.contains("drop.power")) {
                        cm.setDropPower(config.getInt("drop.power"));
                     }
                  } catch (Exception var34) {
                  }

                  try {
                     if (config.contains("drop.gold")) {
                        cm.setDropGold(config.getInt("drop.gold"));
                     }
                  } catch (Exception var33) {
                  }

                  try {
                     if (config.contains("drop.exp")) {
                        cm.setDropExp(config.getInt("drop.exp"));
                     }
                  } catch (Exception var32) {
                  }

                  try {
                     if (config.contains("drop.items")) {
                        MemorySection ms = (MemorySection)config.get("drop.items");
                        List<ItemStack> dropItems = new ArrayList();

                        for(String key : ms.getValues(false).keySet()) {
                           ItemStack is = UtilItems.loadItem(ms.getString(key));
                           if (is != null) {
                              dropItems.add(is);
                           }
                        }

                        cm.setDropItems(dropItems);
                     }
                  } catch (Exception var42) {
                  }

                  try {
                     if (config.contains("shoot")) {
                        cm.setShoot(config.getBoolean("shoot"));
                     }
                  } catch (Exception var31) {
                  }

                  try {
                     if (config.contains("showLevel")) {
                        cm.setShowLevel(config.getBoolean("showLevel"));
                     }
                  } catch (Exception var30) {
                  }

                  try {
                     if (config.contains("name")) {
                        cm.setName(config.getString("name"));
                     }
                  } catch (Exception var29) {
                  }

                  try {
                     if (config.contains("levels")) {
                        HashMap<Integer, Integer> levelHash = new HashMap();

                        for(String s : config.getStringList("levels")) {
                           int id = Integer.parseInt(s.split(" ")[0]);
                           int level = Integer.parseInt(s.split(" ")[1]);
                           levelHash.put(id, level);
                        }

                        cm.setLevelHash(levelHash, true);
                     }
                  } catch (Exception var41) {
                  }

                  try {
                     if (config.contains("recover")) {
                        cm.setRecover((float)config.getDouble("recover"));
                     }
                  } catch (Exception var28) {
                  }

                  try {
                     if (config.contains("nowPath")) {
                        cm.setNowPath(config.getInt("nowPath"));
                     }
                  } catch (Exception var27) {
                  }

                  try {
                     if (config.contains("path")) {
                        HashMap<Integer, Point> path = new HashMap();
                        int index = 1;

                        for(String s : config.getStringList("path")) {
                           double x = Double.parseDouble(s.split(" ")[0]);
                           double y = Double.parseDouble(s.split(" ")[1]);
                           double z = Double.parseDouble(s.split(" ")[2]);
                           float yaw = (float)Double.parseDouble(s.split(" ")[3]);
                           float pitch = (float)Double.parseDouble(s.split(" ")[4]);
                           Point p = new Point(x, y, z, yaw, pitch);
                           path.put(index, p);
                           ++index;
                        }

                        cm.setPath(path);
                     }
                  } catch (Exception var40) {
                  }

                  try {
                     if (config.contains("potions.selfChance")) {
                        cm.setPotionsSelfChance(config.getInt("potions.selfChance"));
                     }
                  } catch (Exception var26) {
                  }

                  try {
                     if (config.contains("potions.enemyChance")) {
                        cm.setPotionsEnemyChance(config.getInt("potions.enemyChance"));
                     }
                  } catch (Exception var25) {
                  }

                  try {
                     if (config.contains("potions.self")) {
                        cm.setPotionSelf(config.getString("potions.self"));
                     }
                  } catch (Exception var24) {
                  }

                  try {
                     if (config.contains("potions.enemy")) {
                        cm.setPotionEnemy(config.getString("potions.enemy"));
                     }
                  } catch (Exception var23) {
                  }

                  try {
                     if (config.contains("skills.damageChance")) {
                        cm.setDamageChance(config.getInt("skills.damageChance"));
                     }
                  } catch (Exception var22) {
                  }

                  try {
                     if (config.contains("skills.damageSkill")) {
                        cm.setDamageSkill(config.getString("skills.damageSkill"));
                     }
                  } catch (Exception var21) {
                  }

                  try {
                     if (config.contains("skills.attackChance")) {
                        cm.setAttackChance(config.getInt("skills.attackChance"));
                     }
                  } catch (Exception var20) {
                  }

                  try {
                     if (config.contains("skills.attackSkill")) {
                        cm.setAttackSkill(config.getString("skills.attackSkill"));
                     }
                  } catch (Exception var19) {
                  }
               } catch (InvalidConfigurationException var43) {
               }
            }
         }
      }

   }

   public static void loadDelay(CustomMonster cm) {
      Bukkit.getScheduler().scheduleSyncDelayedTask(cus, new Load(cm));
   }

   public static void updateName(CustomMonster cm) {
      EntityLiving el = cm.getEl();
      if (el instanceof EntityCreature) {
         EntityCreature ec = (EntityCreature)el;
         if (cm.getName() == null) {
            ec.setCustomName((String)null);
            return;
         }

         ec.setCustomName(cm.getName().substring(0, Math.min(64, cm.getName().length())));
         if (cm.isShowLevel() && cm.getLevelHash() != null) {
            HashMap<Integer, LevelManager.Level> levelHash = Cus.getLevelManager().getLevelHash();

            for(int levelId : cm.getLevelHash().keySet()) {
               LevelManager.Level level = (LevelManager.Level)levelHash.get(levelId);
               if (level != null) {
                  String result = level.getName((Integer)cm.getLevelHash().get(levelId)) + ec.getCustomName();
                  if (result.length() <= 64) {
                     ec.setCustomName(result);
                  }
               }
            }
         }
      }

   }

   public static String getMonsterName(CustomMonster cm) {
      String name = cm.getName();
      if (name == null || name.isEmpty()) {
         name = UtilNames.getEntityName(cm.getEl().getBukkitEntity().getType().getTypeId());
      }

      return name;
   }

   private static class Load implements Runnable {
      private CustomMonster cm;

      public Load(CustomMonster cm) {
         super();
         this.cm = cm;
      }

      public void run() {
         if (this.cm.getEl().isAlive()) {
            CustomEntityUtil.load(this.cm);
         }

      }
   }

   public static enum Opposite {
      yes,
      no,
      none;

      private Opposite() {
      }
   }
}
