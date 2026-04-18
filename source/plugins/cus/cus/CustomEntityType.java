package cus;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.List;
import net.minecraft.server.v1_6_R2.BiomeBase;
import net.minecraft.server.v1_6_R2.BiomeMeta;
import net.minecraft.server.v1_6_R2.EntityBlaze;
import net.minecraft.server.v1_6_R2.EntityCaveSpider;
import net.minecraft.server.v1_6_R2.EntityChicken;
import net.minecraft.server.v1_6_R2.EntityCow;
import net.minecraft.server.v1_6_R2.EntityCreeper;
import net.minecraft.server.v1_6_R2.EntityEnderDragon;
import net.minecraft.server.v1_6_R2.EntityEnderman;
import net.minecraft.server.v1_6_R2.EntityGhast;
import net.minecraft.server.v1_6_R2.EntityGiantZombie;
import net.minecraft.server.v1_6_R2.EntityInsentient;
import net.minecraft.server.v1_6_R2.EntityMagmaCube;
import net.minecraft.server.v1_6_R2.EntityMushroomCow;
import net.minecraft.server.v1_6_R2.EntityPig;
import net.minecraft.server.v1_6_R2.EntityPigZombie;
import net.minecraft.server.v1_6_R2.EntitySheep;
import net.minecraft.server.v1_6_R2.EntitySilverfish;
import net.minecraft.server.v1_6_R2.EntitySkeleton;
import net.minecraft.server.v1_6_R2.EntitySlime;
import net.minecraft.server.v1_6_R2.EntitySpider;
import net.minecraft.server.v1_6_R2.EntityTypes;
import net.minecraft.server.v1_6_R2.EntityWitch;
import net.minecraft.server.v1_6_R2.EntityZombie;
import org.bukkit.entity.EntityType;

public enum CustomEntityType {
   PIG("Pig", 90, EntityType.PIG, EntityPig.class, CustomEntityPig.class),
   SHEEP("Sheep", 91, EntityType.SHEEP, EntitySheep.class, CustomEntitySheep.class),
   COW("Cow", 92, EntityType.COW, EntityCow.class, CustomEntityCow.class),
   CHICKEN("Chicken", 93, EntityType.CHICKEN, EntityChicken.class, CustomEntityChicken.class),
   MUSHROOM_COW("MushroomCow", 96, EntityType.MUSHROOM_COW, EntityMushroomCow.class, CustomEntityMushroomCow.class),
   CREEPER("Creeper", 50, EntityType.CREEPER, EntityCreeper.class, CustomEntityCreeper.class),
   SKELETON("Skeleton", 51, EntityType.SKELETON, EntitySkeleton.class, CustomEntitySkeleton.class),
   SPIDER("Spider", 52, EntityType.SPIDER, EntitySpider.class, CustomEntitySpider.class),
   GIANT("Giant", 53, EntityType.GIANT, EntityGiantZombie.class, CustomEntityGiant.class),
   ZOMBIE("Zombie", 54, EntityType.ZOMBIE, EntityZombie.class, CustomEntityZombie.class),
   SLIME("Slime", 55, EntityType.SLIME, EntitySlime.class, CustomEntitySlime.class),
   GHAST("Ghast", 56, EntityType.GHAST, EntityGhast.class, CustomEntityGhast.class),
   PIG_ZOMBIE("PigZombie", 57, EntityType.PIG_ZOMBIE, EntityPigZombie.class, CustomEntityPigZombie.class),
   ENDERMAN("Enderman", 58, EntityType.ENDERMAN, EntityEnderman.class, CustomEntityEnderman.class),
   CAVE_SPIDER("CaveSpider", 59, EntityType.CAVE_SPIDER, EntityCaveSpider.class, CustomEntityCaveSpider.class),
   SILVERFISH("Silverfish", 60, EntityType.SILVERFISH, EntitySilverfish.class, CustomEntitySilverfish.class),
   BLAZE("Blaze", 61, EntityType.BLAZE, EntityBlaze.class, CustomEntityBlaze.class),
   MAGMA_CUBE("MagmaCube", 62, EntityType.MAGMA_CUBE, EntityMagmaCube.class, CustomEntityMagmaCube.class),
   ENDER_DRAGON("EnderDragon", 63, EntityType.ENDER_DRAGON, EntityEnderDragon.class, CustomEntityEnderDragon.class),
   WITCH("Witch", 66, EntityType.WITCH, EntityWitch.class, CustomEntityWitch.class);

   private String name;
   private int id;
   private EntityType entityType;
   private Class nmsClass;
   private Class customClass;

   private CustomEntityType(String name, int id, EntityType entityType, Class nmsClass, Class customClass) {
      this.name = name;
      this.id = id;
      this.entityType = entityType;
      this.nmsClass = nmsClass;
      this.customClass = customClass;
   }

   public String getName() {
      return this.name;
   }

   public int getID() {
      return this.id;
   }

   public EntityType getEntityType() {
      return this.entityType;
   }

   public Class getNMSClass() {
      return this.nmsClass;
   }

   public Class getCustomClass() {
      return this.customClass;
   }

   public static void registerEntities() {
      CustomEntityType[] var3;
      for(CustomEntityType entity : var3 = values()) {
         try {
            Method a = EntityTypes.class.getDeclaredMethod("a", Class.class, String.class, Integer.TYPE);
            a.setAccessible(true);
            a.invoke((Object)null, entity.getCustomClass(), entity.getName(), entity.getID());
         } catch (Exception e) {
            e.printStackTrace();
         }
      }

      for(BiomeBase biomeBase : var21 = BiomeBase.biomes) {
         if (biomeBase == null) {
            break;
         }

         String[] var7;
         for(String field : var7 = new String[]{"K", "J", "L", "M"}) {
            try {
               Field list = BiomeBase.class.getDeclaredField(field);
               list.setAccessible(true);

               for(BiomeMeta meta : (List)list.get(biomeBase)) {
                  CustomEntityType[] var15;
                  for(CustomEntityType entity : var15 = values()) {
                     if (entity.getNMSClass().equals(meta.b)) {
                        meta.b = entity.getCustomClass();
                     }
                  }
               }
            } catch (Exception e) {
               e.printStackTrace();
            }
         }
      }

   }
}
