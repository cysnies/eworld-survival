package com.khorn.terraincontrol.customobjects;

import com.khorn.terraincontrol.LocalWorld;
import com.khorn.terraincontrol.configuration.WorldConfig;
import java.util.Random;

public class UseWorldAll extends UseWorld {
   public UseWorldAll() {
      super();
   }

   public String getName() {
      return "UseWorldAll";
   }

   public boolean spawnAsTree(LocalWorld world, Random random, int x, int z) {
      for(CustomObject object : world.getSettings().customObjects) {
         if (object.hasPreferenceToSpawnIn(world.getBiome(x, z)) && object.spawnAsTree(world, random, x, z)) {
            return true;
         }
      }

      return false;
   }

   public boolean process(LocalWorld world, Random rand, int chunkX, int chunkZ) {
      WorldConfig worldSettings = world.getSettings();
      boolean spawnedAtLeastOneObject = false;
      if (worldSettings.customObjects.size() == 0) {
         return false;
      } else {
         for(CustomObject selectedObject : worldSettings.customObjects) {
            if (selectedObject.hasPreferenceToSpawnIn(world.getBiome(chunkX * 16 + 8, chunkZ * 16 + 8)) && selectedObject.process(world, rand, chunkX, chunkZ)) {
               spawnedAtLeastOneObject = true;
            }
         }

         return spawnedAtLeastOneObject;
      }
   }
}
