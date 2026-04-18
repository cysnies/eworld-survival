package com.khorn.terraincontrol.events;

import com.khorn.terraincontrol.LocalWorld;
import com.khorn.terraincontrol.customobjects.CustomObject;
import com.khorn.terraincontrol.generator.resourcegens.Resource;
import java.util.Random;

public abstract class EventHandler {
   public EventHandler() {
      super();
   }

   public void onStart() {
   }

   public boolean canCustomObjectSpawn(CustomObject object, LocalWorld world, int x, int y, int z, boolean isCancelled) {
      return true;
   }

   public boolean onResourceProcess(Resource resource, LocalWorld world, Random random, boolean villageInChunk, int chunkX, int chunkZ, boolean isCancelled) {
      return true;
   }

   public void onPopulateStart(LocalWorld world, Random random, boolean villageInChunk, int chunkX, int chunkZ) {
   }

   public void onPopulateEnd(LocalWorld world, Random random, boolean villageInChunk, int chunkX, int chunkZ) {
   }
}
