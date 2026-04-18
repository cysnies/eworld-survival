package com.onarandombox.MultiverseCore;

import com.onarandombox.MultiverseCore.configuration.EntryFee;
import com.onarandombox.MultiverseCore.configuration.SpawnLocation;
import com.onarandombox.MultiverseCore.configuration.SpawnSettings;
import com.onarandombox.MultiverseCore.configuration.WorldPropertyValidator;
import com.onarandombox.MultiverseCore.enums.AllowedPortalType;
import com.onarandombox.MultiverseCore.enums.EnglishChatColor;
import com.onarandombox.MultiverseCore.enums.EnglishChatStyle;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import me.main__.util.multiverse.SerializationConfig.IllegalPropertyValueException;
import me.main__.util.multiverse.SerializationConfig.Property;
import me.main__.util.multiverse.SerializationConfig.SerializationConfig;
import me.main__.util.multiverse.SerializationConfig.Serializor;
import me.main__.util.multiverse.SerializationConfig.Validator;
import me.main__.util.multiverse.SerializationConfig.VirtualProperty;
import org.bukkit.ChatColor;
import org.bukkit.Difficulty;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.World.Environment;
import org.bukkit.configuration.serialization.SerializableAs;

@SerializableAs("MVWorld")
public class WorldProperties extends SerializationConfig {
   private static final Map PROPERTY_ALIASES = new HashMap();
   @Property(
      description = "Sorry, 'hidden' must either be: true or false."
   )
   private volatile boolean hidden;
   @Property(
      description = "Alias must be a valid string."
   )
   private volatile String alias;
   @Property(
      serializor = EnumPropertySerializor.class,
      description = "Sorry, 'color' must be a valid color-name."
   )
   private volatile EnglishChatColor color;
   @Property(
      serializor = EnumPropertySerializor.class,
      description = "Sorry, 'style' must be a valid style-name."
   )
   private volatile EnglishChatStyle style;
   @Property(
      description = "Sorry, 'pvp' must either be: true or false.",
      virtualType = Boolean.class,
      persistVirtual = true
   )
   volatile VirtualProperty pvp;
   @Property(
      description = "Scale must be a positive double value. ex: 2.3"
   )
   private volatile double scale;
   @Property(
      description = "You must set this to the NAME not alias of a world."
   )
   private volatile String respawnWorld;
   @Property(
      description = "Sorry, this must either be: true or false."
   )
   private volatile boolean allowWeather;
   @Property(
      serializor = DifficultyPropertySerializor.class,
      virtualType = Difficulty.class,
      persistVirtual = true,
      description = "Difficulty must be set as one of the following: peaceful easy normal hard"
   )
   volatile VirtualProperty difficulty;
   @Property(
      description = "Sorry, 'animals' must either be: true or false."
   )
   private volatile SpawnSettings spawning;
   @Property
   private volatile EntryFee entryfee;
   @Property(
      description = "Sorry, 'hunger' must either be: true or false."
   )
   private volatile boolean hunger;
   @Property(
      description = "Sorry, 'autoheal' must either be: true or false."
   )
   private volatile boolean autoHeal;
   @Property(
      description = "Sorry, 'adjustspawn' must either be: true or false."
   )
   private volatile boolean adjustSpawn;
   @Property(
      serializor = EnumPropertySerializor.class,
      description = "Allow portal forming must be NONE, ALL, NETHER or END."
   )
   private volatile AllowedPortalType portalForm;
   @Property(
      serializor = GameModePropertySerializor.class,
      description = "GameMode must be set as one of the following: survival creative"
   )
   private volatile GameMode gameMode;
   @Property(
      description = "Sorry, this must either be: true or false.",
      virtualType = Boolean.class,
      persistVirtual = true
   )
   volatile VirtualProperty keepSpawnInMemory;
   @Property
   volatile SpawnLocation spawnLocation;
   @Property(
      virtualType = Location.class,
      description = "There is no help available for this variable. Go bug Rigby90 about it."
   )
   volatile VirtualProperty spawn;
   @Property(
      description = "Set this to false ONLY if you don't want this world to load itself on server restart."
   )
   private volatile boolean autoLoad;
   @Property(
      description = "If a player dies in this world, shoudld they go to their bed?"
   )
   private volatile boolean bedRespawn;
   @Property
   private volatile List worldBlacklist;
   @Property(
      serializor = TimePropertySerializor.class,
      virtualType = Long.class,
      description = "Set the time to whatever you want! (Will NOT freeze time)"
   )
   volatile VirtualProperty time;
   @Property
   volatile World.Environment environment;
   @Property
   volatile long seed;
   @Property
   private volatile String generator;
   @Property
   private volatile int playerLimit;

   public WorldProperties(Map values) {
      super(values);
   }

   public WorldProperties() {
      super();
   }

   public WorldProperties(boolean fixSpawn, World.Environment environment) {
      super();
      if (!fixSpawn) {
         this.adjustSpawn = false;
      }

      this.setScaling(getDefaultScale(environment));
   }

   void setMVWorld(MVWorld world) {
      this.registerObjectUsing(world);
      this.registerGlobalValidator(new WorldPropertyValidator());
   }

   void setValidator(String fieldName, Validator validator) {
      this.registerValidator(fieldName, validator);
   }

   public void copyValues(SerializationConfig other) {
      super.copyValues(other);
   }

   public void cacheVirtualProperties() {
      try {
         this.buildVPropChanges();
      } catch (IllegalStateException var2) {
      }

   }

   protected void setDefaults() {
      this.hidden = false;
      this.alias = new String();
      this.color = EnglishChatColor.WHITE;
      this.style = EnglishChatStyle.NORMAL;
      this.scale = (double)1.0F;
      this.respawnWorld = new String();
      this.allowWeather = true;
      this.spawning = new SpawnSettings();
      this.entryfee = new EntryFee();
      this.hunger = true;
      this.autoHeal = true;
      this.adjustSpawn = true;
      this.portalForm = AllowedPortalType.ALL;
      this.gameMode = GameMode.SURVIVAL;
      this.spawnLocation = new MVWorld.NullLocation();
      this.autoLoad = true;
      this.bedRespawn = true;
      this.worldBlacklist = new ArrayList();
      this.generator = null;
      this.playerLimit = -1;
   }

   private static double getDefaultScale(World.Environment environment) {
      if (environment == Environment.NETHER) {
         return (double)8.0F;
      } else {
         return environment == Environment.THE_END ? (double)16.0F : (double)1.0F;
      }
   }

   protected static Map getAliases() {
      return PROPERTY_ALIASES;
   }

   void flushChanges() {
      this.flushPendingVPropChanges();
   }

   String getAlias() {
      return this.alias;
   }

   public void setAlias(String alias) {
      this.setPropertyValueUnchecked("alias", alias);
   }

   public World.Environment getEnvironment() {
      return this.environment;
   }

   public void setEnvironment(World.Environment environment) {
      this.setPropertyValueUnchecked("environment", environment);
   }

   public long getSeed() {
      return this.seed;
   }

   public void setSeed(long seed) {
      this.setPropertyValueUnchecked("seed", seed);
   }

   public String getGenerator() {
      return this.generator;
   }

   public void setGenerator(String generator) {
      this.setPropertyValueUnchecked("generator", generator);
   }

   public int getPlayerLimit() {
      return this.playerLimit;
   }

   public void setPlayerLimit(int limit) {
      this.setPropertyValueUnchecked("playerLimit", limit);
   }

   public boolean canAnimalsSpawn() {
      return this.spawning.getAnimalSettings().doSpawn();
   }

   public void setAllowAnimalSpawn(boolean animals) {
      this.setPropertyValueUnchecked("spawning.animals.spawn", animals);
   }

   public List getAnimalList() {
      return this.spawning.getAnimalSettings().getExceptions();
   }

   public boolean canMonstersSpawn() {
      return this.spawning.getMonsterSettings().doSpawn();
   }

   public void setAllowMonsterSpawn(boolean monsters) {
      this.setPropertyValueUnchecked("spawning.monsters.spawn", monsters);
   }

   public int getAnimalSpawnRate() {
      return this.spawning.getAnimalSettings().getSpawnRate();
   }

   public int getMonsterSpawnRate() {
      return this.spawning.getMonsterSettings().getSpawnRate();
   }

   public List getMonsterList() {
      return this.spawning.getMonsterSettings().getExceptions();
   }

   public boolean isPVPEnabled() {
      return (Boolean)this.pvp.get();
   }

   public void setPVPMode(boolean pvp) {
      this.setPropertyValueUnchecked("pvp", pvp);
   }

   public boolean isHidden() {
      return this.hidden;
   }

   public void setHidden(boolean hidden) {
      this.setPropertyValueUnchecked("hidden", hidden);
   }

   public List getWorldBlacklist() {
      return this.worldBlacklist;
   }

   public double getScaling() {
      return this.scale;
   }

   public boolean setScaling(double scaling) {
      return this.setPropertyValueUnchecked("scale", scaling);
   }

   public boolean setColor(String aliasColor) {
      return this.setPropertyUnchecked("color", aliasColor);
   }

   public boolean setColor(EnglishChatColor color) {
      return this.setPropertyValueUnchecked("color", color);
   }

   public EnglishChatColor getColor() {
      return this.color;
   }

   public String getRespawnToWorld() {
      return this.respawnWorld;
   }

   public boolean setRespawnToWorld(String respawnToWorld) {
      return this.setPropertyValueUnchecked("respawnWorld", respawnToWorld);
   }

   public int getCurrency() {
      return this.entryfee.getCurrency();
   }

   public void setCurrency(int currency) {
      this.setPropertyValueUnchecked("entryfee.currency", currency);
   }

   public double getPrice() {
      return this.entryfee.getAmount();
   }

   public void setPrice(double price) {
      this.setPropertyValueUnchecked("entryfee.amount", price);
   }

   public boolean setGameMode(String mode) {
      return this.setPropertyUnchecked("gameMode", mode);
   }

   public boolean setGameMode(GameMode mode) {
      return this.setPropertyValueUnchecked("gameMode", mode);
   }

   public GameMode getGameMode() {
      return this.gameMode;
   }

   public void setEnableWeather(boolean weather) {
      this.setPropertyValueUnchecked("allowWeather", weather);
   }

   public boolean isWeatherEnabled() {
      return this.allowWeather;
   }

   public boolean isKeepingSpawnInMemory() {
      return (Boolean)this.keepSpawnInMemory.get();
   }

   public void setKeepSpawnInMemory(boolean value) {
      this.setPropertyValueUnchecked("keepSpawnInMemory", value);
   }

   public boolean getHunger() {
      return this.hunger;
   }

   public void setHunger(boolean hunger) {
      this.setPropertyValueUnchecked("hunger", hunger);
   }

   public Location getSpawnLocation() {
      return (Location)this.spawn.get();
   }

   public void setSpawnLocation(Location l) {
      this.setPropertyValueUnchecked("spawn", l);
   }

   public Difficulty getDifficulty() {
      return (Difficulty)this.difficulty.get();
   }

   /** @deprecated */
   @Deprecated
   public boolean setDifficulty(String difficulty) {
      return this.setPropertyUnchecked("difficulty", difficulty);
   }

   public boolean setDifficulty(Difficulty difficulty) {
      return this.setPropertyValueUnchecked("difficulty", difficulty);
   }

   public boolean getAutoHeal() {
      return this.autoHeal;
   }

   public void setAutoHeal(boolean heal) {
      this.setPropertyValueUnchecked("autoHeal", heal);
   }

   public void setAdjustSpawn(boolean adjust) {
      this.setPropertyValueUnchecked("adjustSpawn", adjust);
   }

   public boolean getAdjustSpawn() {
      return this.adjustSpawn;
   }

   public void setAutoLoad(boolean load) {
      this.setPropertyValueUnchecked("autoLoad", load);
   }

   public boolean getAutoLoad() {
      return this.autoLoad;
   }

   public void setBedRespawn(boolean respawn) {
      this.setPropertyValueUnchecked("bedRespawn", respawn);
   }

   public boolean getBedRespawn() {
      return this.bedRespawn;
   }

   public String getAllPropertyNames() {
      ChatColor myColor = ChatColor.AQUA;
      StringBuilder result = new StringBuilder();
      Map<String, Object> serialized = this.serialize();

      for(String key : serialized.keySet()) {
         result.append(myColor).append(key).append(' ');
         myColor = myColor == ChatColor.AQUA ? ChatColor.GOLD : ChatColor.AQUA;
      }

      return result.toString();
   }

   public String getTime() {
      return this.getPropertyUnchecked("time");
   }

   public boolean setTime(String timeAsString) {
      return this.setPropertyUnchecked("time", timeAsString);
   }

   public AllowedPortalType getAllowedPortals() {
      return this.portalForm;
   }

   public void allowPortalMaking(AllowedPortalType portalType) {
      this.setPropertyValueUnchecked("portalForm", portalType);
   }

   public EnglishChatStyle getStyle() {
      return this.style;
   }

   public boolean setStyle(String style) {
      return this.setPropertyUnchecked("style", style);
   }

   static {
      PROPERTY_ALIASES.put("curr", "currency");
      PROPERTY_ALIASES.put("scaling", "scale");
      PROPERTY_ALIASES.put("aliascolor", "color");
      PROPERTY_ALIASES.put("heal", "autoHeal");
      PROPERTY_ALIASES.put("storm", "allowWeather");
      PROPERTY_ALIASES.put("weather", "allowWeather");
      PROPERTY_ALIASES.put("spawnmemory", "keepSpawnInMemory");
      PROPERTY_ALIASES.put("memory", "keepSpawnInMemory");
      PROPERTY_ALIASES.put("mode", "gameMode");
      PROPERTY_ALIASES.put("diff", "difficulty");
      PROPERTY_ALIASES.put("spawnlocation", "spawn");
      PROPERTY_ALIASES.put("limit", "playerLimit");
      PROPERTY_ALIASES.put("animals", "spawning.animals.spawn");
      PROPERTY_ALIASES.put("monsters", "spawning.monsters.spawn");
      PROPERTY_ALIASES.put("animalsrate", "spawning.animals.spawnrate");
      PROPERTY_ALIASES.put("monstersrate", "spawning.monsters.spawnrate");
   }

   private static final class EnumPropertySerializor implements Serializor {
      private EnumPropertySerializor() {
         super();
      }

      public String serialize(Enum from) {
         return from.toString();
      }

      public Enum deserialize(String serialized, Class wanted) throws IllegalPropertyValueException {
         try {
            return Enum.valueOf(wanted, serialized.toUpperCase());
         } catch (IllegalArgumentException e) {
            throw new IllegalPropertyValueException(e);
         }
      }
   }

   private static final class DifficultyPropertySerializor implements Serializor {
      private DifficultyPropertySerializor() {
         super();
      }

      public String serialize(Difficulty from) {
         return from.toString();
      }

      public Difficulty deserialize(String serialized, Class wanted) throws IllegalPropertyValueException {
         try {
            return Difficulty.getByValue(Integer.parseInt(serialized));
         } catch (Exception var5) {
            try {
               return Difficulty.valueOf(serialized.toUpperCase());
            } catch (Exception var4) {
               throw new IllegalPropertyValueException();
            }
         }
      }
   }

   private static final class GameModePropertySerializor implements Serializor {
      private GameModePropertySerializor() {
         super();
      }

      public String serialize(GameMode from) {
         return from.toString();
      }

      public GameMode deserialize(String serialized, Class wanted) throws IllegalPropertyValueException {
         try {
            return GameMode.getByValue(Integer.parseInt(serialized));
         } catch (NumberFormatException var5) {
            try {
               return GameMode.valueOf(serialized.toUpperCase());
            } catch (Exception var4) {
               throw new IllegalPropertyValueException();
            }
         }
      }
   }

   private static final class TimePropertySerializor implements Serializor {
      private static final String TIME_REGEX = "(\\d\\d?):?(\\d\\d)(a|p)?m?";
      private static final Map TIME_ALIASES;

      private TimePropertySerializor() {
         super();
      }

      public String serialize(Long from) {
         int hours = (int)((from / 1000L + 8L) % 24L);
         int minutes = (int)(60L * (from % 1000L) / 1000L);
         return String.format("%d:%02d", hours, minutes);
      }

      public Long deserialize(String serialized, Class wanted) throws IllegalPropertyValueException {
         if (TIME_ALIASES.containsKey(serialized.toLowerCase())) {
            serialized = (String)TIME_ALIASES.get(serialized.toLowerCase());
         }

         Pattern pattern = Pattern.compile("(\\d\\d?):?(\\d\\d)(a|p)?m?", 2);
         Matcher matcher = pattern.matcher(serialized);
         matcher.find();
         int hour = 0;
         double minute = (double)0.0F;
         int count = matcher.groupCount();
         if (count >= 2) {
            hour = Integer.parseInt(matcher.group(1));
            minute = (double)Integer.parseInt(matcher.group(2));
         }

         if (count == 4 && matcher.group(3).equals("p")) {
            hour += 12;
         }

         if (hour == 24) {
            hour = 0;
         }

         if (hour <= 23 && hour >= 0) {
            if (!(minute > (double)59.0F) && !(minute < (double)0.0F)) {
               double totaltime = ((double)hour + minute / (double)60.0F) * (double)1000.0F;
               totaltime -= (double)8000.0F;
               if (totaltime < (double)0.0F) {
                  totaltime += (double)24000.0F;
               }

               return (long)totaltime;
            } else {
               throw new IllegalPropertyValueException("Illegal minute!");
            }
         } else {
            throw new IllegalPropertyValueException("Illegal hour!");
         }
      }

      static {
         Map<String, String> staticTimes = new HashMap();
         staticTimes.put("morning", "8:00");
         staticTimes.put("day", "12:00");
         staticTimes.put("noon", "12:00");
         staticTimes.put("midnight", "0:00");
         staticTimes.put("night", "20:00");
         TIME_ALIASES = Collections.unmodifiableMap(staticTimes);
      }
   }
}
