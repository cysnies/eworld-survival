package com.khorn.terraincontrol.bukkit;

import com.khorn.terraincontrol.biomegenerators.BiomeCache;
import net.minecraft.server.v1_6_R2.BiomeBase;
import net.minecraft.server.v1_6_R2.WorldChunkManager;

public class BiomeCacheWrapper implements BiomeCache {
   private net.minecraft.server.v1_6_R2.BiomeCache handle;

   public BiomeCacheWrapper(WorldChunkManager manager) {
      super();
      this.handle = new net.minecraft.server.v1_6_R2.BiomeCache(manager);
   }

   public int getBiome(int x, int z) {
      return this.handle.b(x, z).id;
   }

   public void cleanupCache() {
      this.handle.a();
   }

   public int[] getCachedBiomes(int x, int z) {
      BiomeBase[] cached = this.handle.e(x, z);
      int[] intCache = new int[cached.length];

      for(int i = 0; i < cached.length; ++i) {
         intCache[i] = cached[i].id;
      }

      return intCache;
   }
}
