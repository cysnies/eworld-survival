package com.onarandombox.MultiverseCore.utils;

import com.onarandombox.MultiverseCore.MVWorld;
import com.onarandombox.MultiverseCore.MultiverseCore;
import com.onarandombox.MultiverseCore.WorldProperties;
import com.onarandombox.MultiverseCore.api.MVWorldManager;
import com.onarandombox.MultiverseCore.api.MultiverseWorld;
import com.onarandombox.MultiverseCore.api.WorldPurger;
import com.onarandombox.MultiverseCore.event.MVWorldDeleteEvent;
import com.onarandombox.MultiverseCore.exceptions.PropertyDoesNotExistException;
import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Stack;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.WorldCreator;
import org.bukkit.WorldType;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.generator.ChunkGenerator;
import org.bukkit.permissions.Permission;
import org.bukkit.permissions.PermissionDefault;
import org.bukkit.plugin.Plugin;

public class WorldManager implements MVWorldManager {
   private final MultiverseCore plugin;
   private final WorldPurger worldPurger;
   private final Map worlds;
   private Map worldsFromTheConfig;
   private FileConfiguration configWorlds = null;
   private Map defaultGens;
   private String firstSpawn;
   private static final char SEPARATOR = '\uf8ff';

   public WorldManager(MultiverseCore core) {
      super();
      this.plugin = core;
      this.worldsFromTheConfig = new HashMap();
      this.worlds = new ConcurrentHashMap();
      this.worldPurger = new SimpleWorldPurger(this.plugin);
   }

   public void getDefaultWorldGenerators() {
      this.defaultGens = new HashMap();
      File[] files = this.plugin.getServerFolder().listFiles(new FilenameFilter() {
         public boolean accept(File file, String s) {
            return s.equalsIgnoreCase("bukkit.yml");
         }
      });
      if (files != null && files.length == 1) {
         FileConfiguration bukkitConfig = YamlConfiguration.loadConfiguration(files[0]);
         if (bukkitConfig.isConfigurationSection("worlds")) {
            for(String key : bukkitConfig.getConfigurationSection("worlds").getKeys(false)) {
               this.defaultGens.put(key, bukkitConfig.getString("worlds." + key + ".generator", ""));
            }
         }
      } else {
         this.plugin.log(Level.WARNING, "Could not read 'bukkit.yml'. Any Default worldgenerators will not be loaded!");
      }

   }

   public boolean cloneWorld(String oldName, String newName, String generator) {
      if (this.isMVWorld(newName)) {
         return false;
      } else if (!this.getUnloadedWorlds().contains(oldName) && this.isMVWorld(oldName)) {
         final File oldWorldFile = new File(this.plugin.getServer().getWorldContainer(), oldName);
         final File newWorldFile = new File(this.plugin.getServer().getWorldContainer(), newName);
         if (newWorldFile.exists()) {
            return false;
         } else {
            this.unloadWorld(oldName);
            this.removePlayersFromWorld(oldName);
            CoreLogging.config("Copying data for world '%s'", oldName);

            try {
               Thread t = new Thread(new Runnable() {
                  public void run() {
                     FileUtils.copyFolder(oldWorldFile, newWorldFile, Logger.getLogger("Minecraft"));
                  }
               });
               t.start();

               try {
                  t.join();
               } catch (InterruptedException var13) {
               }

               File uidFile = new File(newWorldFile, "uid.dat");
               uidFile.delete();
            } catch (NullPointerException e) {
               e.printStackTrace();
               return false;
            }

            CoreLogging.fine("Kind of copied stuff");
            WorldCreator worldCreator = new WorldCreator(newName);
            CoreLogging.fine("Started to copy settings");
            worldCreator.copy(this.getMVWorld(oldName).getCBWorld());
            CoreLogging.fine("Copied lots of settings");
            boolean useSpawnAdjust = this.getMVWorld(oldName).getAdjustSpawn();
            CoreLogging.fine("Copied more settings");
            World.Environment environment = worldCreator.environment();
            CoreLogging.fine("Copied most settings");
            if (newWorldFile.exists()) {
               CoreLogging.fine("Succeeded at copying stuff");
               if (this.addWorld(newName, environment, (String)null, (WorldType)null, (Boolean)null, generator, useSpawnAdjust)) {
                  CoreLogging.fine("Succeeded at importing stuff");
                  MVWorld newWorld = (MVWorld)this.getMVWorld(newName);
                  MVWorld oldWorld = (MVWorld)this.getMVWorld(oldName);
                  newWorld.copyValues(oldWorld);

                  try {
                     newWorld.setPropertyValue("alias", newName);
                     return true;
                  } catch (PropertyDoesNotExistException e) {
                     throw new RuntimeException(e);
                  }
               }
            }

            return false;
         }
      } else {
         return false;
      }
   }

   public boolean addWorld(String name, World.Environment env, String seedString, WorldType type, Boolean generateStructures, String generator) {
      return this.addWorld(name, env, seedString, type, generateStructures, generator, true);
   }

   public boolean addWorld(String name, World.Environment env, String seedString, WorldType type, Boolean generateStructures, String generator, boolean useSpawnAdjust) {
      Long seed = null;
      WorldCreator c = new WorldCreator(name);
      if (seedString != null && seedString.length() > 0) {
         try {
            seed = Long.parseLong(seedString);
         } catch (NumberFormatException var11) {
            seed = (long)seedString.hashCode();
         }

         c.seed(seed);
      }

      if (generator != null && generator.length() != 0) {
         c.generator(generator);
      }

      c.environment(env);
      if (type != null) {
         c.type(type);
      }

      if (generateStructures != null) {
         c.generateStructures(generateStructures);
      }

      if (!this.worldsFromTheConfig.containsKey(name)) {
         WorldProperties props = new WorldProperties(useSpawnAdjust, env);
         this.worldsFromTheConfig.put(name, props);
      }

      StringBuilder builder = new StringBuilder();
      builder.append("Loading World & Settings - '").append(name).append("'");
      builder.append(" - Env: ").append(env);
      builder.append(" - Type: ").append(type);
      if (seed != null) {
         builder.append(" & seed: ").append(seed);
      }

      if (generator != null) {
         builder.append(" & generator: ").append(generator);
      }

      CoreLogging.info(builder.toString());
      if (!this.doLoad(c, true)) {
         this.plugin.log(Level.SEVERE, "Failed to Create/Load the world '" + name + "'");
         return false;
      } else {
         ((MultiverseWorld)this.worlds.get(name)).setGenerator(generator);
         this.saveWorldsConfig();
         return true;
      }
   }

   public ChunkGenerator getChunkGenerator(String generator, String generatorID, String worldName) {
      if (generator == null) {
         return null;
      } else {
         Plugin myPlugin = this.plugin.getServer().getPluginManager().getPlugin(generator);
         return myPlugin == null ? null : myPlugin.getDefaultWorldGenerator(worldName, generatorID);
      }
   }

   public boolean removeWorldFromConfig(String name) {
      if (!this.unloadWorld(name)) {
         return false;
      } else if (this.worldsFromTheConfig.containsKey(name)) {
         this.worldsFromTheConfig.remove(name);
         CoreLogging.info("World '%s' was removed from config.yml", name);
         this.saveWorldsConfig();
         return true;
      } else {
         CoreLogging.info("World '%s' was already removed from config.yml", name);
         return false;
      }
   }

   public void setFirstSpawnWorld(String world) {
      if (world == null && this.plugin.getServer().getWorlds().size() > 0) {
         this.firstSpawn = ((World)this.plugin.getServer().getWorlds().get(0)).getName();
      } else {
         this.firstSpawn = world;
      }

   }

   public MultiverseWorld getFirstSpawnWorld() {
      MultiverseWorld world = this.getMVWorld(this.firstSpawn);
      if (world == null) {
         this.plugin.log(Level.WARNING, "The world specified as the spawn world (" + this.firstSpawn + ") did not exist!!");

         try {
            return this.getMVWorld((World)this.plugin.getServer().getWorlds().get(0));
         } catch (IndexOutOfBoundsException var3) {
            return null;
         }
      } else {
         return world;
      }
   }

   public boolean unloadWorld(String name) {
      if (this.worlds.containsKey(name)) {
         ((WorldProperties)this.worldsFromTheConfig.get(name)).cacheVirtualProperties();
         if (this.unloadWorldFromBukkit(name, true)) {
            this.worlds.remove(name);
            CoreLogging.info("World '%s' was unloaded from memory.", name);
            return true;
         }

         CoreLogging.warning("World '%s' could not be unloaded. Is it a default world?", name);
      } else if (this.plugin.getServer().getWorld(name) != null) {
         CoreLogging.warning("Hmm Multiverse does not know about this world but it's loaded in memory.");
         CoreLogging.warning("To let Multiverse know about it, use:");
         CoreLogging.warning("/mv import %s %s", name, this.plugin.getServer().getWorld(name).getEnvironment().toString());
      } else {
         if (this.worldsFromTheConfig.containsKey(name)) {
            return true;
         }

         CoreLogging.info("Multiverse does not know about '%s' and it's not loaded by Bukkit.", name);
      }

      return false;
   }

   public boolean loadWorld(String name) {
      if (this.worlds.containsKey(name)) {
         return true;
      } else {
         return this.worldsFromTheConfig.containsKey(name) ? this.doLoad(name) : false;
      }
   }

   private void brokenWorld(String name) {
      this.plugin.log(Level.SEVERE, "The world '" + name + "' could NOT be loaded because it contains errors!");
      this.plugin.log(Level.SEVERE, "Try using Chukster to repair your world! '" + name + "'");
      this.plugin.log(Level.SEVERE, "http://forums.bukkit.org/threads/admin-chunkster.8186/");
   }

   private boolean doLoad(String name) {
      return this.doLoad(name, false, (WorldType)null);
   }

   private boolean doLoad(String name, boolean ignoreExists, WorldType type) {
      if (!this.worldsFromTheConfig.containsKey(name)) {
         throw new IllegalArgumentException("That world doesn't exist!");
      } else {
         WorldProperties world = (WorldProperties)this.worldsFromTheConfig.get(name);
         WorldCreator creator = WorldCreator.name(name);
         creator.environment(world.getEnvironment()).seed(world.getSeed());
         if (type != null) {
            creator.type(type);
         }

         if (world.getGenerator() != null && !world.getGenerator().equals("null")) {
            creator.generator(world.getGenerator());
         }

         return this.doLoad(creator, ignoreExists);
      }
   }

   private boolean doLoad(WorldCreator creator, boolean ignoreExists) {
      String worldName = creator.name();
      if (!this.worldsFromTheConfig.containsKey(worldName)) {
         throw new IllegalArgumentException("That world doesn't exist!");
      } else if (this.worlds.containsKey(worldName)) {
         throw new IllegalArgumentException("That world is already loaded!");
      } else if (!ignoreExists && !(new File(this.plugin.getServer().getWorldContainer(), worldName)).exists()) {
         this.plugin.log(Level.WARNING, "WorldManager: Can't load this world because the folder was deleted/moved: " + worldName);
         this.plugin.log(Level.WARNING, "Use '/mv remove' to remove it from the config!");
         return false;
      } else {
         WorldProperties mvworld = (WorldProperties)this.worldsFromTheConfig.get(worldName);

         World cbworld;
         try {
            cbworld = creator.createWorld();
         } catch (Exception e) {
            e.printStackTrace();
            this.brokenWorld(worldName);
            return false;
         }

         MVWorld world = new MVWorld(this.plugin, cbworld, mvworld);
         this.worldPurger.purgeWorld(world);
         this.worlds.put(worldName, world);
         return true;
      }
   }

   public boolean deleteWorld(String name, boolean removeFromConfig, boolean deleteWorldFolder) {
      World world = this.plugin.getServer().getWorld(name);
      if (world == null) {
         return false;
      } else {
         MVWorldDeleteEvent mvwde = new MVWorldDeleteEvent(this.getMVWorld(name), removeFromConfig);
         this.plugin.getServer().getPluginManager().callEvent(mvwde);
         if (mvwde.isCancelled()) {
            this.plugin.log(Level.FINE, "Tried to delete a world, but the event was cancelled!");
            return false;
         } else {
            if (removeFromConfig) {
               if (!this.removeWorldFromConfig(name)) {
                  return false;
               }
            } else if (!this.unloadWorld(name)) {
               return false;
            }

            try {
               label34: {
                  File worldFile = world.getWorldFolder();
                  this.plugin.log(Level.FINER, "deleteWorld(): worldFile: " + worldFile.getAbsolutePath());
                  if (deleteWorldFolder) {
                     if (FileUtils.deleteFolder(worldFile)) {
                        break label34;
                     }
                  } else if (FileUtils.deleteFolderContents(worldFile)) {
                     break label34;
                  }

                  CoreLogging.severe("World '%s' was NOT deleted.", name);
                  CoreLogging.severe("Are you sure the folder %s exists?", name);
                  CoreLogging.severe("Please check your file permissions on '%s'", name);
                  return false;
               }

               CoreLogging.info("World '%s' was DELETED.", name);
               return true;
            } catch (Throwable e) {
               CoreLogging.severe("Hrm, something didn't go as planned. Here's an exception for ya.");
               CoreLogging.severe("You can go politely explain your situation in #multiverse on esper.net");
               CoreLogging.severe("But from here, it looks like your folder is oddly named.");
               CoreLogging.severe("This world has been removed from Multiverse-Core so your best bet is to go delete the folder by hand. Sorry.");
               CoreLogging.severe(e.getMessage());
               return false;
            }
         }
      }
   }

   public boolean deleteWorld(String name, boolean removeFromConfig) {
      return this.deleteWorld(name, removeFromConfig, true);
   }

   public boolean deleteWorld(String name) {
      return this.deleteWorld(name, true);
   }

   private boolean unloadWorldFromBukkit(String name, boolean safely) {
      this.removePlayersFromWorld(name);
      return this.plugin.getServer().unloadWorld(name, safely);
   }

   public void removePlayersFromWorld(String name) {
      World w = this.plugin.getServer().getWorld(name);
      if (w != null) {
         World safeWorld = (World)this.plugin.getServer().getWorlds().get(0);
         List<Player> ps = w.getPlayers();
         com.onarandombox.MultiverseCore.api.SafeTTeleporter teleporter = this.plugin.getSafeTTeleporter();

         for(Player p : ps) {
            teleporter.safelyTeleport((CommandSender)null, p, safeWorld.getSpawnLocation(), true);
         }
      }

   }

   public Collection getMVWorlds() {
      return this.worlds.values();
   }

   public MultiverseWorld getMVWorld(String name) {
      if (name == null) {
         return null;
      } else {
         MultiverseWorld world = (MultiverseWorld)this.worlds.get(name);
         return world != null ? world : this.getMVWorldByAlias(name);
      }
   }

   public MultiverseWorld getMVWorld(World world) {
      return world != null ? this.getMVWorld(world.getName()) : null;
   }

   private MultiverseWorld getMVWorldByAlias(String alias) {
      for(MultiverseWorld w : this.worlds.values()) {
         if (w.getAlias().equalsIgnoreCase(alias)) {
            return w;
         }
      }

      return null;
   }

   public boolean isMVWorld(String name) {
      return this.worlds.containsKey(name) || this.isMVWorldAlias(name);
   }

   public boolean isMVWorld(World world) {
      return world != null && this.isMVWorld(world.getName());
   }

   private boolean isMVWorldAlias(String alias) {
      for(MultiverseWorld w : this.worlds.values()) {
         if (w.getAlias().equalsIgnoreCase(alias)) {
            return true;
         }
      }

      return false;
   }

   public void loadDefaultWorlds() {
      this.ensureConfigIsPrepared();

      for(World w : this.plugin.getServer().getWorlds()) {
         String name = w.getName();
         if (!this.worldsFromTheConfig.containsKey(name)) {
            String generator = null;
            if (this.defaultGens.containsKey(name)) {
               generator = (String)this.defaultGens.get(name);
            }

            this.addWorld(name, w.getEnvironment(), String.valueOf(w.getSeed()), w.getWorldType(), w.canGenerateStructures(), generator);
         }
      }

   }

   private void ensureConfigIsPrepared() {
      this.configWorlds.options().pathSeparator('\uf8ff');
      if (this.configWorlds.getConfigurationSection("worlds") == null) {
         this.configWorlds.createSection("worlds");
      }

   }

   public void loadWorlds(boolean forceLoad) {
      int count = 0;
      this.ensureConfigIsPrepared();
      this.ensureSecondNamespaceIsPrepared();
      if (forceLoad) {
         Permission allAccess = this.plugin.getServer().getPluginManager().getPermission("multiverse.access.*");
         Permission allExempt = this.plugin.getServer().getPluginManager().getPermission("multiverse.exempt.*");

         for(MultiverseWorld w : this.worlds.values()) {
            if (allAccess != null) {
               allAccess.getChildren().remove(w.getAccessPermission().getName());
            }

            if (allExempt != null) {
               allExempt.getChildren().remove(w.getAccessPermission().getName());
            }

            this.plugin.getServer().getPluginManager().removePermission(w.getAccessPermission().getName());
            this.plugin.getServer().getPluginManager().removePermission(w.getExemptPermission().getName());
            this.plugin.getServer().getPluginManager().removePermission("mv.bypass.gamemode." + w.getName());
         }

         this.plugin.getServer().getPluginManager().recalculatePermissionDefaults(allAccess);
         this.plugin.getServer().getPluginManager().recalculatePermissionDefaults(allExempt);
         this.worlds.clear();
      }

      for(Map.Entry entry : this.worldsFromTheConfig.entrySet()) {
         if (!this.worlds.containsKey(entry.getKey()) && ((WorldProperties)entry.getValue()).getAutoLoad() && this.doLoad((String)entry.getKey())) {
            ++count;
         }
      }

      CoreLogging.config("%s - World(s) loaded.", count);
      this.saveWorldsConfig();
   }

   private void ensureSecondNamespaceIsPrepared() {
      Permission special = this.plugin.getServer().getPluginManager().getPermission("mv.bypass.gamemode.*");
      if (special == null) {
         special = new Permission("mv.bypass.gamemode.*", PermissionDefault.FALSE);
         this.plugin.getServer().getPluginManager().addPermission(special);
      }

   }

   /** @deprecated */
   @Deprecated
   public PurgeWorlds getWorldPurger() {
      return new PurgeWorlds(this.plugin);
   }

   public WorldPurger getTheWorldPurger() {
      return this.worldPurger;
   }

   public FileConfiguration loadWorldConfig(File file) {
      this.configWorlds = YamlConfiguration.loadConfiguration(file);
      this.ensureConfigIsPrepared();

      try {
         this.configWorlds.save(new File(this.plugin.getDataFolder(), "worlds.yml"));
      } catch (IOException e) {
         e.printStackTrace();
      }

      Stack<String> worldKeys = new Stack();
      worldKeys.addAll(this.configWorlds.getConfigurationSection("worlds").getKeys(false));
      Map<String, WorldProperties> newWorldsFromTheConfig = new HashMap();

      while(!worldKeys.isEmpty()) {
         String key = (String)worldKeys.pop();
         String path = "worlds\uf8ff" + key;
         Object obj = this.configWorlds.get(path);
         if (obj != null && obj instanceof WorldProperties) {
            String worldName = key.replaceAll(String.valueOf('\uf8ff'), ".");
            WorldProperties props = (WorldProperties)obj;
            if (this.worldsFromTheConfig.containsKey(worldName)) {
               MVWorld mvWorld = (MVWorld)this.worlds.get(worldName);
               if (mvWorld != null) {
                  mvWorld.copyValues((WorldProperties)obj);
               }
            }

            newWorldsFromTheConfig.put(worldName, props);
         } else if (this.configWorlds.isConfigurationSection(path)) {
            ConfigurationSection section = this.configWorlds.getConfigurationSection(path);

            for(String subkey : section.getKeys(false)) {
               worldKeys.push(key + '\uf8ff' + subkey);
            }
         }
      }

      this.worldsFromTheConfig = newWorldsFromTheConfig;
      this.worlds.keySet().retainAll(this.worldsFromTheConfig.keySet());
      return this.configWorlds;
   }

   public boolean saveWorldsConfig() {
      try {
         this.configWorlds.options().pathSeparator('\uf8ff');
         this.configWorlds.set("worlds", (Object)null);

         for(Map.Entry entry : this.worldsFromTheConfig.entrySet()) {
            this.configWorlds.set("worlds\uf8ff" + (String)entry.getKey(), entry.getValue());
         }

         this.configWorlds.save(new File(this.plugin.getDataFolder(), "worlds.yml"));
         return true;
      } catch (IOException var3) {
         this.plugin.log(Level.SEVERE, "Could not save worlds.yml. Please check your settings.");
         return false;
      }
   }

   public MultiverseWorld getSpawnWorld() {
      return this.getMVWorld((World)this.plugin.getServer().getWorlds().get(0));
   }

   public List getUnloadedWorlds() {
      List<String> allNames = new ArrayList(this.worldsFromTheConfig.keySet());
      allNames.removeAll(this.worlds.keySet());
      return allNames;
   }

   public boolean regenWorld(String name, boolean useNewSeed, boolean randomSeed, String seed) {
      MultiverseWorld world = this.getMVWorld(name);
      if (world == null) {
         return false;
      } else {
         List<Player> ps = world.getCBWorld().getPlayers();
         if (useNewSeed) {
            long theSeed;
            if (randomSeed) {
               theSeed = (new Random()).nextLong();
            } else {
               try {
                  theSeed = Long.parseLong(seed);
               } catch (NumberFormatException var14) {
                  theSeed = (long)seed.hashCode();
               }
            }

            world.setSeed(theSeed);
         }

         WorldType type = world.getWorldType();
         if (!this.deleteWorld(name, false, false)) {
            return false;
         } else {
            this.doLoad(name, true, type);
            com.onarandombox.MultiverseCore.api.SafeTTeleporter teleporter = this.plugin.getSafeTTeleporter();
            Location newSpawn = world.getSpawnLocation();

            for(Player p : ps) {
               teleporter.safelyTeleport((CommandSender)null, p, newSpawn, true);
            }

            return true;
         }
      }
   }

   public FileConfiguration getConfigWorlds() {
      return this.configWorlds;
   }
}
