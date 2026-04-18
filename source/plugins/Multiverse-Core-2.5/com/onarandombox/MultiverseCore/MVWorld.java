package com.onarandombox.MultiverseCore;

import com.onarandombox.MultiverseCore.api.BlockSafety;
import com.onarandombox.MultiverseCore.api.MultiverseWorld;
import com.onarandombox.MultiverseCore.api.SafeTTeleporter;
import com.onarandombox.MultiverseCore.configuration.MVConfigProperty;
import com.onarandombox.MultiverseCore.configuration.SpawnLocation;
import com.onarandombox.MultiverseCore.configuration.WorldPropertyValidator;
import com.onarandombox.MultiverseCore.enums.AllowedPortalType;
import com.onarandombox.MultiverseCore.enums.EnglishChatColor;
import com.onarandombox.MultiverseCore.exceptions.PropertyDoesNotExistException;
import com.onarandombox.MultiverseCore.utils.CoreLogging;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.logging.Level;
import me.main__.util.multiverse.SerializationConfig.ChangeDeniedException;
import me.main__.util.multiverse.SerializationConfig.NoSuchPropertyException;
import me.main__.util.multiverse.SerializationConfig.VirtualProperty;
import org.bukkit.ChatColor;
import org.bukkit.Difficulty;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.WorldType;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.serialization.SerializableAs;
import org.bukkit.entity.Player;
import org.bukkit.permissions.Permission;
import org.bukkit.permissions.PermissionDefault;
import org.bukkit.util.Vector;
import org.json.simple.JSONObject;

public class MVWorld implements MultiverseWorld {
   private static final int SPAWN_LOCATION_SEARCH_TOLERANCE = 16;
   private static final int SPAWN_LOCATION_SEARCH_RADIUS = 16;
   private final MultiverseCore plugin;
   private final String name;
   private final UUID worldUID;
   final WorldProperties props;
   private Permission permission;
   private Permission exempt;
   private Permission ignoreperm;
   private Permission limitbypassperm;

   public MVWorld(MultiverseCore plugin, World world, WorldProperties properties) {
      this(plugin, world, properties, true);
   }

   public MVWorld(MultiverseCore plugin, World world, WorldProperties properties, boolean fixSpawn) {
      super();
      this.plugin = plugin;
      this.name = world.getName();
      this.worldUID = world.getUID();
      this.props = properties;
      this.setupProperties();
      if (!fixSpawn) {
         this.props.setAdjustSpawn(false);
      }

      SpawnLocationPropertyValidator spawnValidator = new SpawnLocationPropertyValidator();
      this.props.setValidator("spawn", spawnValidator);
      this.props.spawnLocation.setWorld(world);
      if (this.props.spawnLocation instanceof NullLocation) {
         SpawnLocation newLoc = new SpawnLocation(this.readSpawnFromWorld(world));
         this.props.spawnLocation = newLoc;
         world.setSpawnLocation(newLoc.getBlockX(), newLoc.getBlockY(), newLoc.getBlockZ());
      }

      this.props.environment = world.getEnvironment();
      this.props.seed = world.getSeed();
      this.initPerms();
      this.props.flushChanges();
      this.validateProperties();
   }

   private void setupProperties() {
      this.props.setMVWorld(this);
      this.props.pvp = new VirtualProperty() {
         public void set(Boolean newValue) {
            World world = MVWorld.this.getCBWorld();
            if (world != null) {
               world.setPVP(newValue);
            }

         }

         public Boolean get() {
            World world = MVWorld.this.getCBWorld();
            return world != null ? world.getPVP() : null;
         }
      };
      this.props.difficulty = new VirtualProperty() {
         public void set(Difficulty newValue) {
            World world = MVWorld.this.getCBWorld();
            if (world != null) {
               world.setDifficulty(newValue);
            }

         }

         public Difficulty get() {
            World world = MVWorld.this.getCBWorld();
            return world != null ? world.getDifficulty() : null;
         }
      };
      this.props.keepSpawnInMemory = new VirtualProperty() {
         public void set(Boolean newValue) {
            World world = MVWorld.this.getCBWorld();
            if (world != null) {
               world.setKeepSpawnInMemory(newValue);
            }

         }

         public Boolean get() {
            World world = MVWorld.this.getCBWorld();
            return world != null ? world.getKeepSpawnInMemory() : null;
         }
      };
      this.props.spawn = new VirtualProperty() {
         public void set(Location newValue) {
            if (MVWorld.this.getCBWorld() != null) {
               MVWorld.this.getCBWorld().setSpawnLocation(newValue.getBlockX(), newValue.getBlockY(), newValue.getBlockZ());
            }

            MVWorld.this.props.spawnLocation = new SpawnLocation(newValue);
         }

         public Location get() {
            MVWorld.this.props.spawnLocation.setWorld(MVWorld.this.getCBWorld());
            return MVWorld.this.props.spawnLocation;
         }
      };
      this.props.time = new VirtualProperty() {
         public void set(Long newValue) {
            World world = MVWorld.this.getCBWorld();
            if (world != null) {
               world.setTime(newValue);
            }

         }

         public Long get() {
            World world = MVWorld.this.getCBWorld();
            return world != null ? world.getTime() : null;
         }
      };
      this.props.setValidator("scale", new ScalePropertyValidator());
      this.props.setValidator("respawnWorld", new RespawnWorldPropertyValidator());
      this.props.setValidator("allowWeather", new AllowWeatherPropertyValidator());
      this.props.setValidator("spawning", new SpawningPropertyValidator());
      this.props.setValidator("gameMode", new GameModePropertyValidator());
   }

   private void validateProperties() {
      this.setPVPMode(this.isPVPEnabled());
      this.setDifficulty(this.getDifficulty());
      this.setKeepSpawnInMemory(this.isKeepingSpawnInMemory());
      this.setScaling(this.getScaling());
      this.setRespawnToWorld(this.props.getRespawnToWorld());
      this.setAllowAnimalSpawn(this.canAnimalsSpawn());
      this.setAllowMonsterSpawn(this.canMonstersSpawn());
      this.setGameMode(this.getGameMode());
   }

   private void initPerms() {
      this.permission = new Permission("multiverse.access." + this.getName(), "Allows access to " + this.getName(), PermissionDefault.OP);
      this.ignoreperm = new Permission("mv.bypass.gamemode." + this.getName(), "Allows players with this permission to ignore gamemode changes.", PermissionDefault.FALSE);
      this.exempt = new Permission("multiverse.exempt." + this.getName(), "A player who has this does not pay to enter this world, or use any MV portals in it " + this.getName(), PermissionDefault.OP);
      this.limitbypassperm = new Permission("mv.bypass.playerlimit." + this.getName(), "A player who can enter this world regardless of wether its full", PermissionDefault.OP);

      try {
         this.plugin.getServer().getPluginManager().addPermission(this.permission);
         this.plugin.getServer().getPluginManager().addPermission(this.exempt);
         this.plugin.getServer().getPluginManager().addPermission(this.ignoreperm);
         this.plugin.getServer().getPluginManager().addPermission(this.limitbypassperm);
         this.addToUpperLists(this.permission);
         this.ignoreperm.addParent("mv.bypass.gamemode.*", true);
         this.limitbypassperm.addParent("mv.bypass.playerlimit.*", true);
      } catch (IllegalArgumentException var2) {
         this.plugin.log(Level.FINER, "Permissions nodes were already added for " + this.name);
      }

   }

   private Location readSpawnFromWorld(World w) {
      Location location = w.getSpawnLocation();
      BlockSafety bs = this.plugin.getBlockSafety();
      if (!bs.playerCanSpawnHereSafely(location)) {
         if (!this.getAdjustSpawn()) {
            this.plugin.log(Level.FINE, "Spawn location from world.dat file was unsafe!!");
            this.plugin.log(Level.FINE, "NOT adjusting spawn for '" + this.getAlias() + "' because you told me not to.");
            this.plugin.log(Level.FINE, "To turn on spawn adjustment for this world simply type:");
            this.plugin.log(Level.FINE, "/mvm set adjustspawn true " + this.getAlias());
            return location;
         }

         SafeTTeleporter teleporter = this.plugin.getSafeTTeleporter();
         this.plugin.log(Level.WARNING, "Spawn location from world.dat file was unsafe. Adjusting...");
         this.plugin.log(Level.WARNING, "Original Location: " + this.plugin.getLocationManipulation().strCoordsRaw(location));
         Location newSpawn = teleporter.getSafeLocation(location, 16, 16);
         if (newSpawn != null) {
            CoreLogging.info("New Spawn for '%s' is located at: %s", this.getName(), this.plugin.getLocationManipulation().locationToString(newSpawn));
            return newSpawn;
         }

         Location newerSpawn = bs.getTopBlock(new Location(w, (double)0.0F, (double)0.0F, (double)0.0F));
         if (newerSpawn != null) {
            CoreLogging.info("New Spawn for '%s' is located at: %s", this.getName(), this.plugin.getLocationManipulation().locationToString(newerSpawn));
            return newerSpawn;
         }

         this.plugin.log(Level.SEVERE, "Safe spawn NOT found!!!");
      }

      return location;
   }

   private void addToUpperLists(Permission perm) {
      Permission all = this.plugin.getServer().getPluginManager().getPermission("multiverse.*");
      Permission allWorlds = this.plugin.getServer().getPluginManager().getPermission("multiverse.access.*");
      Permission allExemption = this.plugin.getServer().getPluginManager().getPermission("multiverse.exempt.*");
      if (allWorlds == null) {
         allWorlds = new Permission("multiverse.access.*");
         this.plugin.getServer().getPluginManager().addPermission(allWorlds);
      }

      allWorlds.getChildren().put(perm.getName(), true);
      if (allExemption == null) {
         allExemption = new Permission("multiverse.exempt.*");
         this.plugin.getServer().getPluginManager().addPermission(allExemption);
      }

      allExemption.getChildren().put(this.exempt.getName(), true);
      if (all == null) {
         all = new Permission("multiverse.*");
         this.plugin.getServer().getPluginManager().addPermission(all);
      }

      all.getChildren().put("multiverse.access.*", true);
      all.getChildren().put("multiverse.exempt.*", true);
      this.plugin.getServer().getPluginManager().recalculatePermissionDefaults(all);
      this.plugin.getServer().getPluginManager().recalculatePermissionDefaults(allWorlds);
   }

   public void copyValues(MVWorld other) {
      this.props.copyValues(other.props);
   }

   public void copyValues(WorldProperties other) {
      this.props.copyValues(other);
   }

   public World getCBWorld() {
      World world = this.plugin.getServer().getWorld(this.worldUID);
      if (world == null) {
         throw new IllegalStateException("Lost reference to bukkit world '" + this.name + "'");
      } else {
         return world;
      }
   }

   public String getColoredWorldString() {
      if (this.props.getAlias().length() == 0) {
         this.props.setAlias(this.getName());
      }

      if (this.props.getColor() == null || this.props.getColor().getColor() == null) {
         this.props.setColor(EnglishChatColor.WHITE);
      }

      StringBuilder nameBuilder = (new StringBuilder()).append(this.props.getColor().getColor());
      if (this.props.getStyle().getColor() != null) {
         nameBuilder.append(this.props.getStyle().getColor());
      }

      nameBuilder.append(this.props.getAlias()).append(ChatColor.WHITE).toString();
      return nameBuilder.toString();
   }

   /** @deprecated */
   @Deprecated
   public boolean clearList(String property) {
      return this.clearVariable(property);
   }

   /** @deprecated */
   @Deprecated
   public boolean clearVariable(String property) {
      List<String> list = this.getOldAndEvilList(property);
      if (list == null) {
         return false;
      } else {
         list.clear();
         return true;
      }
   }

   /** @deprecated */
   @Deprecated
   public boolean addToVariable(String property, String value) {
      List<String> list = this.getOldAndEvilList(property);
      if (list == null) {
         return false;
      } else {
         list.add(value);
         return true;
      }
   }

   /** @deprecated */
   @Deprecated
   public boolean removeFromVariable(String property, String value) {
      List<String> list = this.getOldAndEvilList(property);
      if (list == null) {
         return false;
      } else {
         list.remove(value);
         return true;
      }
   }

   /** @deprecated */
   @Deprecated
   private List getOldAndEvilList(String property) {
      if (property.equalsIgnoreCase("worldblacklist")) {
         return this.props.getWorldBlacklist();
      } else if (property.equalsIgnoreCase("animals")) {
         return this.props.getAnimalList();
      } else {
         return property.equalsIgnoreCase("monsters") ? this.props.getMonsterList() : null;
      }
   }

   /** @deprecated */
   @Deprecated
   public MVConfigProperty getProperty(String property, Class expected) throws PropertyDoesNotExistException {
      throw new UnsupportedOperationException("'MVConfigProperty<T> getProperty(String,Class<T>)' is no longer supported!");
   }

   /** @deprecated */
   @Deprecated
   public boolean setProperty(String name, String value, CommandSender sender) throws PropertyDoesNotExistException {
      return this.setPropertyValue(name, value);
   }

   public String getPropertyValue(String property) throws PropertyDoesNotExistException {
      try {
         return this.props.getProperty(property, true);
      } catch (NoSuchPropertyException e) {
         throw new PropertyDoesNotExistException(property, e);
      }
   }

   public boolean setPropertyValue(String property, String value) throws PropertyDoesNotExistException {
      try {
         return this.props.setProperty(property, value, true);
      } catch (NoSuchPropertyException e) {
         throw new PropertyDoesNotExistException(property, e);
      }
   }

   public String getPropertyHelp(String property) throws PropertyDoesNotExistException {
      try {
         return this.props.getPropertyDescription(property, true);
      } catch (NoSuchPropertyException e) {
         throw new PropertyDoesNotExistException(property, e);
      }
   }

   public WorldType getWorldType() {
      World world = this.getCBWorld();
      return world != null ? world.getWorldType() : null;
   }

   public World.Environment getEnvironment() {
      return this.props.getEnvironment();
   }

   public void setEnvironment(World.Environment environment) {
      this.props.setEnvironment(environment);
   }

   public long getSeed() {
      return this.props.getSeed();
   }

   public void setSeed(long seed) {
      this.props.setSeed(seed);
   }

   public String getGenerator() {
      return this.props.getGenerator();
   }

   public void setGenerator(String generator) {
      this.props.setGenerator(generator);
   }

   public int getPlayerLimit() {
      return this.props.getPlayerLimit();
   }

   public void setPlayerLimit(int limit) {
      this.props.setPlayerLimit(limit);
   }

   public String getName() {
      return this.name;
   }

   public String getPermissibleName() {
      return this.name.toLowerCase();
   }

   public String getAlias() {
      return this.props.getAlias() != null && this.props.getAlias().length() != 0 ? this.props.getAlias() : this.name;
   }

   public void setAlias(String alias) {
      this.props.setAlias(alias);
   }

   public boolean canAnimalsSpawn() {
      return this.props.canAnimalsSpawn();
   }

   public void setAllowAnimalSpawn(boolean animals) {
      this.props.setAllowAnimalSpawn(animals);
   }

   public List getAnimalList() {
      return this.props.getAnimalList();
   }

   public boolean canMonstersSpawn() {
      return this.props.canMonstersSpawn();
   }

   public void setAllowMonsterSpawn(boolean monsters) {
      this.props.setAllowMonsterSpawn(monsters);
   }

   public List getMonsterList() {
      return this.props.getMonsterList();
   }

   public boolean isPVPEnabled() {
      return this.props.isPVPEnabled();
   }

   public void setPVPMode(boolean pvp) {
      this.props.setPVPMode(pvp);
   }

   public boolean isHidden() {
      return this.props.isHidden();
   }

   public void setHidden(boolean hidden) {
      this.props.setHidden(hidden);
   }

   public List getWorldBlacklist() {
      return this.props.getWorldBlacklist();
   }

   public double getScaling() {
      return this.props.getScaling();
   }

   public boolean setScaling(double scaling) {
      return this.props.setScaling(scaling);
   }

   public boolean setColor(String aliasColor) {
      return this.props.setColor(aliasColor);
   }

   /** @deprecated */
   @Deprecated
   public boolean isValidAliasColor(String aliasColor) {
      return EnglishChatColor.fromString(aliasColor) != null;
   }

   public ChatColor getColor() {
      return this.props.getColor().getColor();
   }

   /** @deprecated */
   @Deprecated
   public boolean getFakePVP() {
      return false;
   }

   public World getRespawnToWorld() {
      return this.plugin.getServer().getWorld(this.props.getRespawnToWorld());
   }

   public boolean setRespawnToWorld(String respawnToWorld) {
      return !this.plugin.getMVWorldManager().isMVWorld(respawnToWorld) ? false : this.props.setRespawnToWorld(respawnToWorld);
   }

   public Permission getAccessPermission() {
      return this.permission;
   }

   public int getCurrency() {
      return this.props.getCurrency();
   }

   public void setCurrency(int currency) {
      this.props.setCurrency(currency);
   }

   public double getPrice() {
      return this.props.getPrice();
   }

   public void setPrice(double price) {
      this.props.setPrice(price);
   }

   public Permission getExemptPermission() {
      return this.exempt;
   }

   public boolean setGameMode(String mode) {
      return this.props.setGameMode(mode);
   }

   public boolean setGameMode(GameMode mode) {
      return this.props.setGameMode(mode);
   }

   public GameMode getGameMode() {
      return this.props.getGameMode();
   }

   public void setEnableWeather(boolean weather) {
      this.props.setEnableWeather(weather);
   }

   public boolean isWeatherEnabled() {
      return this.props.isWeatherEnabled();
   }

   public boolean isKeepingSpawnInMemory() {
      return this.props.isKeepingSpawnInMemory();
   }

   public void setKeepSpawnInMemory(boolean value) {
      this.props.setKeepSpawnInMemory(value);
   }

   public boolean getHunger() {
      return this.props.getHunger();
   }

   public void setHunger(boolean hunger) {
      this.props.setHunger(hunger);
   }

   public Location getSpawnLocation() {
      return this.props.getSpawnLocation();
   }

   public void setSpawnLocation(Location l) {
      this.props.setSpawnLocation(l);
   }

   public Difficulty getDifficulty() {
      return this.props.getDifficulty();
   }

   /** @deprecated */
   @Deprecated
   public boolean setDifficulty(String difficulty) {
      return this.props.setDifficulty(difficulty);
   }

   public boolean setDifficulty(Difficulty difficulty) {
      return this.props.setDifficulty(difficulty);
   }

   public boolean getAutoHeal() {
      return this.props.getAutoHeal();
   }

   public void setAutoHeal(boolean heal) {
      this.props.setAutoHeal(heal);
   }

   public void setAdjustSpawn(boolean adjust) {
      this.props.setAdjustSpawn(adjust);
   }

   public boolean getAdjustSpawn() {
      return this.props.getAdjustSpawn();
   }

   public void setAutoLoad(boolean load) {
      this.props.setAutoLoad(load);
   }

   public boolean getAutoLoad() {
      return this.props.getAutoLoad();
   }

   public void setBedRespawn(boolean respawn) {
      this.props.setBedRespawn(respawn);
   }

   public boolean getBedRespawn() {
      return this.props.getBedRespawn();
   }

   public String getAllPropertyNames() {
      return this.props.getAllPropertyNames();
   }

   public String getTime() {
      return this.props.getTime();
   }

   public boolean setTime(String timeAsString) {
      return this.props.setTime(timeAsString);
   }

   public AllowedPortalType getAllowedPortals() {
      return this.props.getAllowedPortals();
   }

   public void allowPortalMaking(AllowedPortalType portalType) {
      this.props.allowPortalMaking(portalType);
   }

   public ChatColor getStyle() {
      return this.props.getStyle().getColor();
   }

   public boolean setStyle(String style) {
      return this.props.setStyle(style);
   }

   public String toString() {
      JSONObject jsonData = new JSONObject();
      jsonData.put("Name", this.getName());
      jsonData.put("Env", this.getEnvironment().toString());
      jsonData.put("Type", this.getWorldType().toString());
      jsonData.put("Gen", this.getGenerator());
      JSONObject topLevel = new JSONObject();
      topLevel.put(this.getClass().getSimpleName() + "@" + this.hashCode(), jsonData);
      return topLevel.toString();
   }

   private final class ScalePropertyValidator extends WorldPropertyValidator {
      private ScalePropertyValidator() {
         super();
      }

      public Double validateChange(String property, Double newValue, Double oldValue, MVWorld object) throws ChangeDeniedException {
         if (newValue <= (double)0.0F) {
            MVWorld.this.plugin.log(Level.FINE, "Someone tried to set a scale <= 0, aborting!");
            throw new ChangeDeniedException();
         } else {
            return (Double)super.validateChange(property, newValue, oldValue, (MVWorld)object);
         }
      }
   }

   private final class RespawnWorldPropertyValidator extends WorldPropertyValidator {
      private RespawnWorldPropertyValidator() {
         super();
      }

      public String validateChange(String property, String newValue, String oldValue, MVWorld object) throws ChangeDeniedException {
         if (!newValue.isEmpty() && !MVWorld.this.plugin.getMVWorldManager().isMVWorld(newValue)) {
            throw new ChangeDeniedException();
         } else {
            return (String)super.validateChange(property, newValue, oldValue, (MVWorld)object);
         }
      }
   }

   private final class AllowWeatherPropertyValidator extends WorldPropertyValidator {
      private AllowWeatherPropertyValidator() {
         super();
      }

      public Boolean validateChange(String property, Boolean newValue, Boolean oldValue, MVWorld object) throws ChangeDeniedException {
         if (!newValue) {
            World world = MVWorld.this.getCBWorld();
            if (world != null) {
               world.setStorm(false);
               world.setThundering(false);
            }
         }

         return (Boolean)super.validateChange(property, newValue, oldValue, (MVWorld)object);
      }
   }

   private final class SpawningPropertyValidator extends WorldPropertyValidator {
      private SpawningPropertyValidator() {
         super();
      }

      public Boolean validateChange(String property, Boolean newValue, Boolean oldValue, MVWorld object) throws ChangeDeniedException {
         boolean allowAnimals;
         if (MVWorld.this.getAnimalList().isEmpty()) {
            allowAnimals = MVWorld.this.canAnimalsSpawn();
         } else {
            allowAnimals = true;
         }

         boolean allowMonsters;
         if (MVWorld.this.getMonsterList().isEmpty()) {
            allowMonsters = MVWorld.this.canMonstersSpawn();
         } else {
            allowMonsters = true;
         }

         World world = MVWorld.this.getCBWorld();
         if (world != null) {
            if (MVWorld.this.props.getAnimalSpawnRate() != -1) {
               world.setTicksPerAnimalSpawns(MVWorld.this.props.getAnimalSpawnRate());
            }

            if (MVWorld.this.props.getMonsterSpawnRate() != -1) {
               world.setTicksPerMonsterSpawns(MVWorld.this.props.getMonsterSpawnRate());
            }

            world.setSpawnFlags(allowMonsters, allowAnimals);
         }

         MVWorld.this.plugin.getMVWorldManager().getTheWorldPurger().purgeWorld(MVWorld.this);
         return (Boolean)super.validateChange(property, newValue, oldValue, (MVWorld)object);
      }
   }

   private final class GameModePropertyValidator extends WorldPropertyValidator {
      private GameModePropertyValidator() {
         super();
      }

      public GameMode validateChange(String property, GameMode newValue, GameMode oldValue, MVWorld object) throws ChangeDeniedException {
         for(Player p : MVWorld.this.plugin.getServer().getWorld(MVWorld.this.getName()).getPlayers()) {
            MVWorld.this.plugin.log(Level.FINER, String.format("Setting %s's GameMode to %s", p.getName(), newValue.toString()));
            MVWorld.this.plugin.getPlayerListener().handleGameMode(p, (MultiverseWorld)MVWorld.this);
         }

         return (GameMode)super.validateChange(property, newValue, oldValue, (MVWorld)object);
      }
   }

   private final class SpawnLocationPropertyValidator extends WorldPropertyValidator {
      private SpawnLocationPropertyValidator() {
         super();
      }

      public Location validateChange(String property, Location newValue, Location oldValue, MVWorld object) throws ChangeDeniedException {
         if (newValue == null) {
            throw new ChangeDeniedException();
         } else {
            if (MVWorld.this.props.getAdjustSpawn()) {
               BlockSafety bs = MVWorld.this.plugin.getBlockSafety();
               if (!bs.playerCanSpawnHereSafely(newValue)) {
                  MVWorld.this.plugin.log(Level.WARNING, String.format("Somebody tried to set the spawn location for '%s' to an unsafe value! Adjusting...", MVWorld.this.getAlias()));
                  MVWorld.this.plugin.log(Level.WARNING, "Old Location: " + MVWorld.this.plugin.getLocationManipulation().strCoordsRaw(oldValue));
                  MVWorld.this.plugin.log(Level.WARNING, "New (unsafe) Location: " + MVWorld.this.plugin.getLocationManipulation().strCoordsRaw(newValue));
                  SafeTTeleporter teleporter = MVWorld.this.plugin.getSafeTTeleporter();
                  newValue = teleporter.getSafeLocation(newValue, 16, 16);
                  if (newValue == null) {
                     MVWorld.this.plugin.log(Level.WARNING, "Couldn't fix the location. I have to abort the spawn location-change :/");
                     throw new ChangeDeniedException();
                  }

                  MVWorld.this.plugin.log(Level.WARNING, "New (safe) Location: " + MVWorld.this.plugin.getLocationManipulation().strCoordsRaw(newValue));
               }
            }

            return (Location)super.validateChange(property, newValue, oldValue, (MVWorld)object);
         }
      }
   }

   @SerializableAs("MVNullLocation (It's a bug if you see this in your config file)")
   public static final class NullLocation extends SpawnLocation {
      public NullLocation() {
         super((double)0.0F, (double)-1.0F, (double)0.0F);
      }

      public Location clone() {
         throw new UnsupportedOperationException();
      }

      public Map serialize() {
         return Collections.EMPTY_MAP;
      }

      public static NullLocation deserialize(Map args) {
         return new NullLocation();
      }

      public Vector toVector() {
         throw new UnsupportedOperationException();
      }

      public int hashCode() {
         return -1;
      }

      public String toString() {
         return "NULL LOCATION";
      }
   }
}
