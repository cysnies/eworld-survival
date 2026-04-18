package com.earth2me.essentials;

import com.earth2me.essentials.utils.StringUtil;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Random;
import java.util.logging.Logger;
import org.bukkit.DyeColor;
import org.bukkit.Material;
import org.bukkit.entity.Ageable;
import org.bukkit.entity.Creeper;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.ExperienceOrb;
import org.bukkit.entity.Horse;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Ocelot;
import org.bukkit.entity.Pig;
import org.bukkit.entity.Player;
import org.bukkit.entity.Skeleton;
import org.bukkit.entity.Slime;
import org.bukkit.entity.Tameable;
import org.bukkit.entity.Villager;
import org.bukkit.entity.Wolf;
import org.bukkit.entity.Zombie;
import org.bukkit.entity.Horse.Color;
import org.bukkit.entity.Horse.Style;
import org.bukkit.entity.Horse.Variant;
import org.bukkit.entity.Ocelot.Type;
import org.bukkit.entity.Skeleton.SkeletonType;
import org.bukkit.entity.Villager.Profession;
import org.bukkit.inventory.EntityEquipment;
import org.bukkit.inventory.ItemStack;
import org.bukkit.material.Colorable;

public enum MobData {
   BABY_AGEABLE("baby", Ageable.class, MobData.Data.BABY, true),
   BABY_PIG("piglet", EntityType.PIG, MobData.Data.BABY, false),
   BABY_WOLF("puppy", EntityType.WOLF, MobData.Data.BABY, false),
   BABY_CHICKEN("chick", EntityType.CHICKEN, MobData.Data.BABY, false),
   BABY_HORSE("colt", EntityType.HORSE, MobData.Data.BABY, false),
   BABY_OCELOT("kitten", EntityType.OCELOT, MobData.Data.BABY, false),
   BABY_SHEEP("lamb", EntityType.SHEEP, MobData.Data.BABY, false),
   BABY_COW("calf", EntityType.COW.getEntityClass(), MobData.Data.BABY, false),
   BABY_VILLAGER("child", EntityType.VILLAGER, MobData.Data.BABY, false),
   TAMED_TAMEABLE("tamed", Tameable.class, MobData.Data.TAMED, true),
   TAME_TAMEABLE("tame", Tameable.class, MobData.Data.TAMED, false),
   RANDOM_SHEEP("random", EntityType.SHEEP, MobData.Data.COLORABLE, true),
   COLORABLE_SHEEP("", StringUtil.joinList(DyeColor.values()).toLowerCase(Locale.ENGLISH), EntityType.SHEEP, MobData.Data.COLORABLE, true),
   DONKEY_HORSE("donkey", EntityType.HORSE, Variant.DONKEY, true),
   MULE_HORSE("mule", EntityType.HORSE, Variant.MULE, true),
   SKELETON_HORSE("skeleton", EntityType.HORSE, Variant.SKELETON_HORSE, true),
   UNDEAD_HORSE("undead", EntityType.HORSE, Variant.UNDEAD_HORSE, true),
   ZOMBIE_HORSE("zombie", EntityType.HORSE, Variant.UNDEAD_HORSE, false),
   POLKA_HORSE("polka", EntityType.HORSE, Style.BLACK_DOTS, true),
   SOOTY_HORSE("sooty", EntityType.HORSE, Style.BLACK_DOTS, false),
   BLAZE_HORSE("blaze", EntityType.HORSE, Style.WHITE, true),
   SOCKS_HORSE("socks", EntityType.HORSE, Style.WHITE, false),
   LEOPARD_HORSE("leopard", EntityType.HORSE, Style.WHITE_DOTS, true),
   APPALOOSA_HORSE("appaloosa", EntityType.HORSE, Style.WHITE_DOTS, false),
   PAINT_HORSE("paint", EntityType.HORSE, Style.WHITEFIELD, true),
   MILKY_HORSE("milky", EntityType.HORSE, Style.WHITEFIELD, false),
   SPLOTCHY_HORSE("splotchy", EntityType.HORSE, Style.WHITEFIELD, false),
   BLACK_HORSE("black", EntityType.HORSE, Color.BLACK, true),
   CHESTNUT_HORSE("chestnut", EntityType.HORSE, Color.CHESTNUT, true),
   LIVER_HORSE("liver", EntityType.HORSE, Color.CHESTNUT, false),
   CREAMY_HORSE("creamy", EntityType.HORSE, Color.CREAMY, true),
   FLAXEN_HORSE("flaxen", EntityType.HORSE, Color.CREAMY, false),
   GRAY_HORSE("gray", EntityType.HORSE, Color.GRAY, true),
   DAPPLE_HORSE("dapple", EntityType.HORSE, Color.GRAY, false),
   BUCKSKIN_HORSE("buckskin", EntityType.HORSE, Color.DARK_BROWN, true),
   DARKBROWN_HORSE("darkbrown", EntityType.HORSE, Color.DARK_BROWN, false),
   DARK_HORSE("dark", EntityType.HORSE, Color.DARK_BROWN, false),
   DBROWN_HORSE("dbrown", EntityType.HORSE, Color.DARK_BROWN, false),
   BAY_HORSE("bay", EntityType.HORSE, Color.BROWN, true),
   BROWN_HORSE("brown", EntityType.HORSE, Color.BROWN, false),
   CHEST_HORSE("chest", EntityType.HORSE, MobData.Data.CHEST, true),
   SADDLE_HORSE("saddle", EntityType.HORSE, MobData.Data.HORSESADDLE, true),
   GOLD_ARMOR_HORSE("goldarmor", EntityType.HORSE, Material.GOLD_BARDING, true),
   DIAMOND_ARMOR_HORSE("diamondarmor", EntityType.HORSE, Material.DIAMOND_BARDING, true),
   ARMOR_HORSE("armor", EntityType.HORSE, Material.IRON_BARDING, true),
   SIAMESE_CAT("siamese", EntityType.OCELOT, Type.SIAMESE_CAT, true),
   WHITE_CAT("white", EntityType.OCELOT, Type.SIAMESE_CAT, false),
   RED_CAT("red", EntityType.OCELOT, Type.RED_CAT, true),
   ORANGE_CAT("orange", EntityType.OCELOT, Type.RED_CAT, false),
   TABBY_CAT("tabby", EntityType.OCELOT, Type.RED_CAT, false),
   BLACK_CAT("black", EntityType.OCELOT, Type.BLACK_CAT, true),
   TUXEDO_CAT("tuxedo", EntityType.OCELOT, Type.BLACK_CAT, false),
   VILLAGER_ZOMBIE("villager", EntityType.ZOMBIE.getEntityClass(), MobData.Data.VILLAGER, true),
   BABY_ZOMBIE("baby", EntityType.ZOMBIE.getEntityClass(), MobData.Data.BABYZOMBIE, true),
   DIAMOND_SWORD_ZOMBIE("diamondsword", EntityType.ZOMBIE.getEntityClass(), Material.DIAMOND_SWORD, true),
   GOLD_SWORD_ZOMBIE("goldsword", EntityType.ZOMBIE.getEntityClass(), Material.GOLD_SWORD, true),
   IRON_SWORD_ZOMBIE("ironsword", EntityType.ZOMBIE.getEntityClass(), Material.IRON_SWORD, true),
   STONE_SWORD_ZOMBIE("stonesword", EntityType.ZOMBIE.getEntityClass(), Material.STONE_SWORD, false),
   SWORD_ZOMBIE("sword", EntityType.ZOMBIE.getEntityClass(), Material.STONE_SWORD, true),
   DIAMOND_SWORD_SKELETON("diamondsword", EntityType.SKELETON, Material.DIAMOND_SWORD, true),
   GOLD_SWORD_SKELETON("goldsword", EntityType.SKELETON, Material.GOLD_SWORD, true),
   IRON_SWORD_SKELETON("ironsword", EntityType.SKELETON, Material.IRON_SWORD, true),
   STONE_SWORD_SKELETON("stonesword", EntityType.SKELETON, Material.STONE_SWORD, false),
   SWORD_SKELETON("sword", EntityType.SKELETON, Material.STONE_SWORD, true),
   WHITHER_SKELETON("wither", EntityType.SKELETON, MobData.Data.WITHER, true),
   POWERED_CREEPER("powered", EntityType.CREEPER, MobData.Data.ELECTRIFIED, true),
   ELECTRIC_CREEPER("electric", EntityType.CREEPER, MobData.Data.ELECTRIFIED, false),
   CHARGED_CREEPER("charged", EntityType.CREEPER, MobData.Data.ELECTRIFIED, false),
   SADDLE_PIG("saddle", EntityType.PIG, MobData.Data.PIGSADDLE, true),
   ANGRY_WOLF("angry", EntityType.WOLF, MobData.Data.ANGRY, true),
   RABID_WOLF("rabid", EntityType.WOLF, MobData.Data.ANGRY, false),
   FARMER_VILLAGER("farmer", EntityType.VILLAGER, Profession.FARMER, true),
   LIBRARIAN_VILLAGER("librarian", EntityType.VILLAGER, Profession.LIBRARIAN, true),
   PRIEST_VILLAGER("priest", EntityType.VILLAGER, Profession.PRIEST, true),
   FATHER_VILLAGER("father", EntityType.VILLAGER, Profession.PRIEST, false),
   SMITH_VILLAGER("smith", EntityType.VILLAGER, Profession.BLACKSMITH, true),
   BUTCHER_VILLAGER("butcher", EntityType.VILLAGER, Profession.BUTCHER, true),
   SIZE_SLIME("", "<1-100>", EntityType.SLIME.getEntityClass(), MobData.Data.SIZE, true),
   NUM_EXPERIENCE_ORB("", "<1-2000000000>", EntityType.EXPERIENCE_ORB, MobData.Data.EXP, true);

   public static final Logger logger = Logger.getLogger("Minecraft");
   private final String nickname;
   private final String helpMessage;
   private final Object type;
   private final Object value;
   private final boolean isPublic;
   private String matched;

   private MobData(String n, Object type, Object value, boolean isPublic) {
      this.nickname = n;
      this.matched = n;
      this.helpMessage = n;
      this.type = type;
      this.value = value;
      this.isPublic = isPublic;
   }

   private MobData(String n, String h, Object type, Object value, boolean isPublic) {
      this.nickname = n;
      this.matched = n;
      this.helpMessage = h;
      this.type = type;
      this.value = value;
      this.isPublic = isPublic;
   }

   public static LinkedHashMap getPossibleData(Entity spawned, boolean publicOnly) {
      LinkedHashMap<String, MobData> mobList = new LinkedHashMap();

      for(MobData data : values()) {
         if (!(data.type instanceof EntityType) || !spawned.getType().equals(data.type) || (!publicOnly || !data.isPublic) && publicOnly) {
            if (data.type instanceof Class && ((Class)data.type).isAssignableFrom(spawned.getClass()) && (publicOnly && data.isPublic || !publicOnly)) {
               mobList.put(data.nickname.toLowerCase(Locale.ENGLISH), data);
            }
         } else {
            mobList.put(data.nickname.toLowerCase(Locale.ENGLISH), data);
         }
      }

      return mobList;
   }

   public static List getValidHelp(Entity spawned) {
      List<String> output = new ArrayList();
      LinkedHashMap<String, MobData> posData = getPossibleData(spawned, true);

      for(MobData data : posData.values()) {
         output.add(data.helpMessage);
      }

      return output;
   }

   public static MobData fromData(Entity spawned, String name) {
      if (name.isEmpty()) {
         return null;
      } else {
         LinkedHashMap<String, MobData> posData = getPossibleData(spawned, false);

         for(String data : posData.keySet()) {
            if (name.contains(data)) {
               return (MobData)posData.get(data);
            }
         }

         return null;
      }
   }

   public String getMatched() {
      return this.matched;
   }

   public void setData(Entity spawned, Player target, String rawData) throws Exception {
      if (this.value.equals(MobData.Data.ANGRY)) {
         ((Wolf)spawned).setAngry(true);
      } else if (this.value.equals(MobData.Data.BABY)) {
         ((Ageable)spawned).setBaby();
      } else if (this.value.equals(MobData.Data.BABYZOMBIE)) {
         ((Zombie)spawned).setBaby(true);
      } else if (this.value.equals(MobData.Data.CHEST)) {
         ((Horse)spawned).setTamed(true);
         ((Horse)spawned).setCarryingChest(true);
      } else if (this.value.equals(MobData.Data.ELECTRIFIED)) {
         ((Creeper)spawned).setPowered(true);
      } else if (this.value.equals(MobData.Data.HORSESADDLE)) {
         ((Horse)spawned).setTamed(true);
         ((Horse)spawned).getInventory().setSaddle(new ItemStack(Material.SADDLE, 1));
      } else if (this.value.equals(MobData.Data.PIGSADDLE)) {
         ((Pig)spawned).setSaddle(true);
      } else if (this.value.equals(MobData.Data.TAMED)) {
         Tameable tameable = (Tameable)spawned;
         tameable.setTamed(true);
         tameable.setOwner(target);
      } else if (this.value.equals(MobData.Data.VILLAGER)) {
         ((Zombie)spawned).setVillager(this.value.equals(MobData.Data.VILLAGER));
      } else if (this.value.equals(MobData.Data.WITHER)) {
         ((Skeleton)spawned).setSkeletonType(SkeletonType.WITHER);
      } else if (this.value.equals(MobData.Data.COLORABLE)) {
         String color = rawData.toUpperCase(Locale.ENGLISH);

         try {
            if (color.equals("RANDOM")) {
               Random rand = new Random();
               ((Colorable)spawned).setColor(DyeColor.values()[rand.nextInt(DyeColor.values().length)]);
            } else if (!color.isEmpty()) {
               ((Colorable)spawned).setColor(DyeColor.valueOf(color));
            }

            this.matched = rawData;
         } catch (Exception e) {
            throw new Exception(I18n._("sheepMalformedColor"), e);
         }
      } else if (this.value.equals(MobData.Data.EXP)) {
         try {
            ((ExperienceOrb)spawned).setExperience(Integer.parseInt(rawData));
            this.matched = rawData;
         } catch (Exception e) {
            throw new Exception(I18n._("invalidNumber"), e);
         }
      } else if (this.value.equals(MobData.Data.SIZE)) {
         try {
            ((Slime)spawned).setSize(Integer.parseInt(rawData));
            this.matched = rawData;
         } catch (Exception e) {
            throw new Exception(I18n._("slimeMalformedSize"), e);
         }
      } else if (this.value instanceof Horse.Color) {
         ((Horse)spawned).setColor((Horse.Color)this.value);
      } else if (this.value instanceof Horse.Style) {
         ((Horse)spawned).setStyle((Horse.Style)this.value);
      } else if (this.value instanceof Horse.Variant) {
         ((Horse)spawned).setVariant((Horse.Variant)this.value);
      } else if (this.value instanceof Ocelot.Type) {
         ((Ocelot)spawned).setCatType((Ocelot.Type)this.value);
      } else if (this.value instanceof Villager.Profession) {
         ((Villager)spawned).setProfession((Villager.Profession)this.value);
      } else if (this.value instanceof Material) {
         if (this.type.equals(EntityType.HORSE)) {
            ((Horse)spawned).setTamed(true);
            ((Horse)spawned).getInventory().setArmor(new ItemStack((Material)this.value, 1));
         } else if (this.type.equals(EntityType.ZOMBIE.getEntityClass()) || this.type.equals(EntityType.SKELETON)) {
            EntityEquipment invent = ((LivingEntity)spawned).getEquipment();
            invent.setItemInHand(new ItemStack((Material)this.value, 1));
            invent.setItemInHandDropChance(0.1F);
         }
      }

   }

   public static enum Data {
      BABY,
      CHEST,
      BABYZOMBIE,
      VILLAGER,
      HORSESADDLE,
      PIGSADDLE,
      ELECTRIFIED,
      WITHER,
      ANGRY,
      TAMED,
      COLORABLE,
      EXP,
      SIZE;

      private Data() {
      }
   }
}
