package com.khorn.terraincontrol;

import java.util.HashMap;
import java.util.Map;

public enum MobAlternativeNames {
   CREEPER("Creeper", new String[]{"creeper"}),
   SKELETON("Skeleton", new String[]{"skeleton"}),
   SPIDER("Spider", new String[]{"spider"}),
   GIANT("Giant", new String[]{"giant", "giantzombie", "zombiegiant"}),
   ZOMBIE("Zombie", new String[]{"zombie"}),
   SLIME("Slime", new String[]{"slime"}),
   GHAST("Ghast", new String[]{"ghast"}),
   PIG_ZOMBIE("PigZombie", new String[]{"pigzombie", "pig_zombie"}),
   ENDERMAN("Enderman", new String[]{"enderman"}),
   CAVE_SPIDER("CaveSpider", new String[]{"cavespider", "cave_spider"}),
   SILVERFISH("Silverfish", new String[]{"silverfish", "silver_fish"}),
   BLAZE("Blaze", new String[]{"blaze"}),
   MAGMA_CUBE("LavaSlime", new String[]{"lavaslime", "lava_slime", "magmacube", "magma_cube"}),
   ENDER_DRAGON("EnderDragon", new String[]{"enderdragon", "ender_dragon"}),
   WITHER_BOSS("WitherBoss", new String[]{"witherboss", "wither_boss", "Wither", "wither"}),
   BAT("Bat", new String[]{"bat"}),
   WITCH("Witch", new String[]{"witch"}),
   PIG("Pig", new String[]{"pig"}),
   SHEEP("Sheep", new String[]{"sheep"}),
   COW("Cow", new String[]{"cow"}),
   CHICKEN("Chicken", new String[]{"chicken"}),
   SQUID("Squid", new String[]{"squid"}),
   WOLF("Wolf", new String[]{"wolf"}),
   MUSHROOM_COW("MushroomCow", new String[]{"mushroomcow", "shroom", "mooshroom", "moshoom", "mcow", "shroomcow"}),
   SNOWMAN("SnowMan", new String[]{"snowman"}),
   OCELOT("Ozelot", new String[]{"ozelot", "Ocelot", "ocelot"}),
   IRON_GOLEM("VillagerGolem", new String[]{"villagergolem", "villager_golem", "IronGolem", "irongolem", "iron_golem"}),
   VILLAGER("Villager", new String[]{"villager"});

   private static Map mobAliases = new HashMap();
   private String internalMinecraftName;
   private String[] aliases;

   private MobAlternativeNames(String internalMinecraftName, String... aliases) {
      this.internalMinecraftName = internalMinecraftName;
      this.aliases = aliases;
   }

   public static void register(String internalMinecraftName, String... aliases) {
      for(String alias : aliases) {
         mobAliases.put(alias, internalMinecraftName);
      }

   }

   public static String getInternalMinecraftName(String alias) {
      return mobAliases.containsKey(alias) ? (String)mobAliases.get(alias) : alias;
   }

   static {
      for(MobAlternativeNames alt : values()) {
         register(alt.internalMinecraftName, alt.aliases);
      }

   }
}
