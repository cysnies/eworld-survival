package com.khorn.terraincontrol.bukkit;

import com.khorn.terraincontrol.LocalWorld;
import com.khorn.terraincontrol.TerrainControl;
import com.khorn.terraincontrol.TerrainControlEngine;
import com.khorn.terraincontrol.bukkit.commands.TCCommandExecutor;
import com.khorn.terraincontrol.bukkit.util.BukkitMetricsHelper;
import com.khorn.terraincontrol.configuration.TCDefaultValues;
import com.khorn.terraincontrol.configuration.WorldConfig;
import com.khorn.terraincontrol.customobjects.BODefaultValues;
import com.khorn.terraincontrol.util.StringHelper;
import java.io.File;
import java.util.HashMap;
import java.util.logging.Level;
import java.util.logging.Logger;
import net.minecraft.server.v1_6_R2.BiomeBase;
import net.minecraft.server.v1_6_R2.Block;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.craftbukkit.v1_6_R2.block.CraftBlock;
import org.bukkit.generator.ChunkGenerator;
import org.bukkit.plugin.java.JavaPlugin;

public class TCPlugin extends JavaPlugin implements TerrainControlEngine {
   private final HashMap notInitedWorlds = new HashMap();
   public TCListener listener;
   public TCCommandExecutor commandExecutor;
   public boolean cleanupOnDisable = false;
   public final HashMap worlds = new HashMap();
   // $FF: synthetic field
   private static int[] $SWITCH_TABLE$com$khorn$terraincontrol$configuration$WorldConfig$TerrainMode;

   public TCPlugin() {
      super();
   }

   public void onDisable() {
      if (this.cleanupOnDisable) {
         for(BukkitWorld world : this.worlds.values()) {
            world.disable();
         }

         this.worlds.clear();
         TerrainControl.stopEngine();
      }

   }

   public void onEnable() {
      if (Bukkit.getWorlds().size() != 0 && !this.cleanupOnDisable) {
         this.log(Level.SEVERE, "The server was just /reloaded! Terrain Control has problems handling this,");
         this.log(Level.SEVERE, "as old parts from before the reload have not been cleaned up.");
         this.log(Level.SEVERE, "Unexpected things may happen! Please restart the server!");
         this.log(Level.SEVERE, "In the future, instead of /reloading, please restart the server,");
         this.log(Level.SEVERE, "or reload a plugin using it's built-in command (like /tc reload),");
         this.log(Level.SEVERE, "or use a plugin managing plugin that can reload one plugin at a time.");
         this.setEnabled(false);
      } else {
         if (Bukkit.getVersion().contains("MCPC-Plus")) {
            TerrainControl.supportedBlockIds = 4095;
            this.log(Level.INFO, "MCPC+ detected, enabling extended block id support.");
         }

         TerrainControl.startEngine(this);
         this.commandExecutor = new TCCommandExecutor(this);
         this.listener = new TCListener(this);
         Bukkit.getMessenger().registerOutgoingPluginChannel(this, TCDefaultValues.ChannelName.stringValue());
         TerrainControl.log("Global objects loaded, waiting for worlds to load");
         new BukkitMetricsHelper(this);
      }

   }

   public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
      return this.commandExecutor.onCommand(sender, command, label, args);
   }

   public ChunkGenerator getDefaultWorldGenerator(String worldName, String id) {
      if (worldName.equals("")) {
         TerrainControl.log("Ignoring empty world name. Is some generator plugin checking if \"TerrainControl\" is a valid world name?");
         return new TCChunkGenerator(this);
      } else if (worldName.equals("test")) {
         TerrainControl.log("Ignoring world with the name \"test\". This is not a valid world name,");
         TerrainControl.log("as it's used by Multiverse to check if \"TerrainControl\" a valid generator name.");
         TerrainControl.log("So if you were just using /mv create, don't worry about this message.");
         return new TCChunkGenerator(this);
      } else {
         for(BukkitWorld world : this.worlds.values()) {
            if (world.getName().equals(worldName)) {
               TerrainControl.log("Already enabled for '" + worldName + "'");
               return world.getChunkGenerator();
            }
         }

         TerrainControl.log("Starting to enable world '" + worldName + "'...");
         BukkitWorld localWorld = new BukkitWorld(worldName);
         CraftBlock.biomeBaseToBiome(BiomeBase.OCEAN);
         File baseFolder = this.getWorldSettingsFolder(worldName);
         WorldConfig worldConfig = new WorldConfig(baseFolder, localWorld, false);
         localWorld.setSettings(worldConfig);
         this.notInitedWorlds.put(worldName, localWorld);
         TCChunkGenerator generator = null;
         switch (worldConfig.ModeTerrain) {
            case Normal:
            case OldGenerator:
            case TerrainTest:
            case NotGenerate:
               generator = new TCChunkGenerator(this);
            case Default:
            default:
               localWorld.setChunkGenerator(generator);
               return generator;
         }
      }
   }

   public File getWorldSettingsFolder(String worldName) {
      File baseFolder = new File(this.getDataFolder(), "worlds" + File.separator + worldName);
      if (!baseFolder.exists()) {
         TerrainControl.log("settings does not exist, creating defaults");
         if (!baseFolder.mkdirs()) {
            TerrainControl.log(Level.SEVERE, "cant create folder " + baseFolder.getName());
         }
      }

      return baseFolder;
   }

   public void onWorldInit(World world) {
      if (this.notInitedWorlds.containsKey(world.getName())) {
         BukkitWorld bukkitWorld = (BukkitWorld)this.notInitedWorlds.remove(world.getName());
         bukkitWorld.enable(world);
         this.worlds.put(world.getUID(), bukkitWorld);
         TerrainControl.log("World " + bukkitWorld.getName() + " is now enabled!");
      }

   }

   public void log(Level level, String... msg) {
      Logger.getLogger("Minecraft").log(level, "[TerrainControl] " + StringHelper.join((Object[])msg, " "));
   }

   public LocalWorld getWorld(String name) {
      World world = Bukkit.getWorld(name);
      return world == null ? null : (LocalWorld)this.worlds.get(world.getUID());
   }

   public File getGlobalObjectsDirectory() {
      return new File(this.getDataFolder(), BODefaultValues.BO_GlobalDirectoryName.stringValue());
   }

   public boolean isValidBlockId(int id) {
      if (id == 0) {
         return true;
      } else if (id >= 0 && id <= TerrainControl.supportedBlockIds) {
         return Block.byId[id] != null;
      } else {
         return false;
      }
   }

   // $FF: synthetic method
   static int[] $SWITCH_TABLE$com$khorn$terraincontrol$configuration$WorldConfig$TerrainMode() {
      int[] var10000 = $SWITCH_TABLE$com$khorn$terraincontrol$configuration$WorldConfig$TerrainMode;
      if (var10000 != null) {
         return var10000;
      } else {
         int[] var0 = new int[WorldConfig.TerrainMode.values().length];

         try {
            var0[WorldConfig.TerrainMode.Default.ordinal()] = 5;
         } catch (NoSuchFieldError var5) {
         }

         try {
            var0[WorldConfig.TerrainMode.Normal.ordinal()] = 1;
         } catch (NoSuchFieldError var4) {
         }

         try {
            var0[WorldConfig.TerrainMode.NotGenerate.ordinal()] = 4;
         } catch (NoSuchFieldError var3) {
         }

         try {
            var0[WorldConfig.TerrainMode.OldGenerator.ordinal()] = 2;
         } catch (NoSuchFieldError var2) {
         }

         try {
            var0[WorldConfig.TerrainMode.TerrainTest.ordinal()] = 3;
         } catch (NoSuchFieldError var1) {
         }

         $SWITCH_TABLE$com$khorn$terraincontrol$configuration$WorldConfig$TerrainMode = var0;
         return var0;
      }
   }
}
