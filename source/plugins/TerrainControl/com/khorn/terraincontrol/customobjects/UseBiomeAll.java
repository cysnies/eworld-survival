package com.khorn.terraincontrol.customobjects;

import com.khorn.terraincontrol.LocalWorld;
import java.util.Random;

public class UseBiomeAll extends UseBiome {
   public UseBiomeAll() {
      super();
   }

   public String getName() {
      return "UseBiomeAll";
   }

   public boolean spawnAsTree(LocalWorld world, Random random, int x, int z) {
      for(CustomObject object : this.getPossibleObjectsAt(world, x, z)) {
         if (object.spawnAsTree(world, random, x, z)) {
            return true;
         }
      }

      return false;
   }

   public boolean process(LocalWorld world, Random random, int chunkX, int chunkZ) {
      boolean spawnedAtLeastOneObject = false;

      for(CustomObject object : this.getPossibleObjectsAt(world, chunkX * 16 + 8, chunkZ * 16 + 8)) {
         if (object.process(world, random, chunkX, chunkZ)) {
            spawnedAtLeastOneObject = true;
         }
      }

      return spawnedAtLeastOneObject;
   }
}
