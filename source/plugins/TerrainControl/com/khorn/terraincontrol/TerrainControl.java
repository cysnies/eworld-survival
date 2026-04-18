package com.khorn.terraincontrol;

import com.khorn.terraincontrol.biomegenerators.BiomeModeManager;
import com.khorn.terraincontrol.configuration.ConfigFunctionsManager;
import com.khorn.terraincontrol.customobjects.CustomObject;
import com.khorn.terraincontrol.customobjects.CustomObjectLoader;
import com.khorn.terraincontrol.customobjects.CustomObjectManager;
import com.khorn.terraincontrol.events.EventHandler;
import com.khorn.terraincontrol.events.EventPriority;
import com.khorn.terraincontrol.generator.resourcegens.Resource;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.logging.Level;

public class TerrainControl {
   public static int worldHeight = 256;
   public static int worldDepth = 0;
   public static int supportedBlockIds = 255;
   private static TerrainControlEngine engine;
   private static ConfigFunctionsManager configFunctionsManager;
   private static CustomObjectManager customObjectManager;
   private static BiomeModeManager biomeManagers;
   private static List cancelableEventHandlers = new ArrayList();
   private static List monitoringEventHandlers = new ArrayList();

   private TerrainControl() {
      super();
   }

   public static void startEngine(TerrainControlEngine engine) {
      if (TerrainControl.engine != null) {
         throw new UnsupportedOperationException("Engine is already set!");
      } else {
         TerrainControl.engine = engine;
         configFunctionsManager = new ConfigFunctionsManager();
         customObjectManager = new CustomObjectManager();
         biomeManagers = new BiomeModeManager();

         for(EventHandler handler : cancelableEventHandlers) {
            handler.onStart();
         }

         for(EventHandler handler : monitoringEventHandlers) {
            handler.onStart();
         }

         customObjectManager.loadGlobalObjects();
      }
   }

   public static void stopEngine() {
      for(CustomObjectLoader loader : customObjectManager.loaders.values()) {
         loader.onShutdown();
      }

      engine = null;
      customObjectManager = null;
      configFunctionsManager = null;
      biomeManagers = null;
      cancelableEventHandlers.clear();
      monitoringEventHandlers.clear();
   }

   public static TerrainControlEngine getEngine() {
      return engine;
   }

   public static LocalWorld getWorld(String name) {
      return engine.getWorld(name);
   }

   public static String getBiomeName(String worldName, int x, int z) {
      LocalWorld world = getWorld(worldName);
      return world == null ? null : world.getBiome(x, z).getName();
   }

   public static void log(String... messages) {
      engine.log(Level.INFO, messages);
   }

   public static void log(Level level, String... messages) {
      engine.log(level, messages);
   }

   public static CustomObjectManager getCustomObjectManager() {
      return customObjectManager;
   }

   public static ConfigFunctionsManager getConfigFunctionsManager() {
      return configFunctionsManager;
   }

   public static BiomeModeManager getBiomeModeManager() {
      return biomeManagers;
   }

   public static void registerEventHandler(EventHandler handler) {
      cancelableEventHandlers.add(handler);
   }

   public static void registerEventHandler(EventHandler handler, EventPriority priority) {
      if (priority == EventPriority.CANCELABLE) {
         cancelableEventHandlers.add(handler);
      } else {
         monitoringEventHandlers.add(handler);
      }

   }

   public static boolean fireCanCustomObjectSpawnEvent(CustomObject object, LocalWorld world, int x, int y, int z) {
      boolean success = true;

      for(EventHandler handler : cancelableEventHandlers) {
         if (!handler.canCustomObjectSpawn(object, world, x, y, z, !success)) {
            success = false;
         }
      }

      for(EventHandler handler : monitoringEventHandlers) {
         handler.canCustomObjectSpawn(object, world, x, y, z, !success);
      }

      return success;
   }

   public static boolean fireResourceProcessEvent(Resource resource, LocalWorld world, Random random, boolean villageInChunk, int chunkX, int chunkZ) {
      boolean success = true;

      for(EventHandler handler : cancelableEventHandlers) {
         if (!handler.onResourceProcess(resource, world, random, villageInChunk, chunkX, chunkZ, !success)) {
            success = false;
         }
      }

      for(EventHandler handler : monitoringEventHandlers) {
         handler.onResourceProcess(resource, world, random, villageInChunk, chunkX, chunkZ, !success);
      }

      return success;
   }

   public static void firePopulationStartEvent(LocalWorld world, Random random, boolean villageInChunk, int chunkX, int chunkZ) {
      for(EventHandler handler : cancelableEventHandlers) {
         handler.onPopulateStart(world, random, villageInChunk, chunkX, chunkZ);
      }

      for(EventHandler handler : monitoringEventHandlers) {
         handler.onPopulateStart(world, random, villageInChunk, chunkX, chunkZ);
      }

   }

   public static void firePopulationEndEvent(LocalWorld world, Random random, boolean villageInChunk, int chunkX, int chunkZ) {
      for(EventHandler handler : cancelableEventHandlers) {
         handler.onPopulateEnd(world, random, villageInChunk, chunkX, chunkZ);
      }

      for(EventHandler handler : monitoringEventHandlers) {
         handler.onPopulateEnd(world, random, villageInChunk, chunkX, chunkZ);
      }

   }
}
