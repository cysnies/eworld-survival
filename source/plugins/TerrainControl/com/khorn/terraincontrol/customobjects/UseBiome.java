package com.khorn.terraincontrol.customobjects;

import com.khorn.terraincontrol.LocalBiome;
import com.khorn.terraincontrol.LocalWorld;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Random;

public class UseBiome implements CustomObject {
   public UseBiome() {
      super();
   }

   public ArrayList getPossibleObjectsAt(LocalWorld world, int x, int z) {
      return world.getSettings().biomeConfigs[world.getBiome(x, z).getId()].biomeObjects;
   }

   public void onEnable(Map otherObjectsInDirectory) {
   }

   public String getName() {
      return "UseBiome";
   }

   public boolean canSpawnAsTree() {
      return true;
   }

   public boolean canSpawnAsObject() {
      return true;
   }

   public boolean spawnForced(LocalWorld world, Random random, Rotation rotation, int x, int y, int z) {
      for(CustomObject object : this.getPossibleObjectsAt(world, x, z)) {
         if (object.spawnForced(world, random, rotation, x, y, z)) {
            return true;
         }
      }

      return false;
   }

   public boolean spawnAsTree(LocalWorld world, Random random, int x, int z) {
      List<CustomObject> possibleObjects = this.getPossibleObjectsAt(world, x, z);
      int objectSpawnRatio = world.getSettings().objectSpawnRatio;
      if (possibleObjects.size() == 0) {
         return false;
      } else {
         boolean objectSpawned = false;

         CustomObject selectedObject;
         for(int spawnattemps = 0; !objectSpawned; objectSpawned = selectedObject.process(world, random, x, x)) {
            if (spawnattemps > objectSpawnRatio) {
               return false;
            }

            ++spawnattemps;
            selectedObject = (CustomObject)possibleObjects.get(random.nextInt(possibleObjects.size()));
         }

         return objectSpawned;
      }
   }

   public boolean process(LocalWorld world, Random random, int chunkX, int chunkZ) {
      List<CustomObject> possibleObjects = this.getPossibleObjectsAt(world, chunkX * 16 + 8, chunkZ * 16 + 8);
      int objectSpawnRatio = world.getSettings().objectSpawnRatio;
      if (possibleObjects.size() == 0) {
         return false;
      } else {
         boolean objectSpawned = false;

         CustomObject selectedObject;
         for(int spawnattemps = 0; !objectSpawned; objectSpawned = selectedObject.process(world, random, chunkX, chunkZ)) {
            if (spawnattemps > objectSpawnRatio) {
               return false;
            }

            ++spawnattemps;
            selectedObject = (CustomObject)possibleObjects.get(random.nextInt(possibleObjects.size()));
         }

         return objectSpawned;
      }
   }

   public CustomObject applySettings(Map settings) {
      return this;
   }

   public boolean hasPreferenceToSpawnIn(LocalBiome biome) {
      return false;
   }

   public boolean canSpawnAt(LocalWorld world, Rotation rotation, int x, int y, int z) {
      List<CustomObject> objects = this.getPossibleObjectsAt(world, x, z);
      if (objects.size() == 0) {
         return false;
      } else {
         for(CustomObject object : objects) {
            if (!object.canSpawnAt(world, rotation, x, y, z)) {
               return false;
            }
         }

         return true;
      }
   }

   public boolean canRotateRandomly() {
      return true;
   }
}
