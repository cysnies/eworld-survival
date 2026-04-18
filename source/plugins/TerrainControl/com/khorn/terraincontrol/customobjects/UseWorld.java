package com.khorn.terraincontrol.customobjects;

import com.khorn.terraincontrol.LocalBiome;
import com.khorn.terraincontrol.LocalWorld;
import com.khorn.terraincontrol.configuration.WorldConfig;
import java.util.Map;
import java.util.Random;

public class UseWorld implements CustomObject {
   public UseWorld() {
      super();
   }

   public void onEnable(Map otherObjectsInDirectory) {
   }

   public String getName() {
      return "UseWorld";
   }

   public boolean canSpawnAsTree() {
      return true;
   }

   public boolean canSpawnAsObject() {
      return true;
   }

   public boolean spawnForced(LocalWorld world, Random random, Rotation rotation, int x, int y, int z) {
      for(CustomObject object : world.getSettings().customObjects) {
         if (object.hasPreferenceToSpawnIn(world.getBiome(x, z)) && object.spawnForced(world, random, rotation, x, y, z)) {
            return true;
         }
      }

      return false;
   }

   public boolean spawnAsTree(LocalWorld world, Random random, int x, int z) {
      WorldConfig worldSettings = world.getSettings();
      if (worldSettings.customObjects.size() == 0) {
         return false;
      } else {
         boolean objectSpawned = false;
         int spawnattemps = 0;

         while(!objectSpawned) {
            if (spawnattemps > worldSettings.objectSpawnRatio) {
               return false;
            }

            ++spawnattemps;
            CustomObject selectedObject = (CustomObject)worldSettings.customObjects.get(random.nextInt(worldSettings.customObjects.size()));
            if (selectedObject.hasPreferenceToSpawnIn(world.getBiome(x, z))) {
               objectSpawned = selectedObject.spawnAsTree(world, random, x, z);
            }
         }

         return objectSpawned;
      }
   }

   public boolean process(LocalWorld world, Random rand, int chunk_x, int chunk_z) {
      WorldConfig worldSettings = world.getSettings();
      if (worldSettings.customObjects.size() == 0) {
         return false;
      } else {
         boolean objectSpawned = false;
         int spawnattemps = 0;

         while(!objectSpawned) {
            if (spawnattemps > worldSettings.objectSpawnRatio) {
               return false;
            }

            ++spawnattemps;
            CustomObject selectedObject = (CustomObject)worldSettings.customObjects.get(rand.nextInt(worldSettings.customObjects.size()));
            if (selectedObject.hasPreferenceToSpawnIn(world.getBiome(chunk_x * 16 + 8, chunk_z * 16 + 8))) {
               objectSpawned = selectedObject.process(world, rand, chunk_x, chunk_z);
            }
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
      return true;
   }

   public boolean canRotateRandomly() {
      return true;
   }
}
