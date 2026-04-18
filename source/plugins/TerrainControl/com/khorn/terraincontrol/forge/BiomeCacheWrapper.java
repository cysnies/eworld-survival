package com.khorn.terraincontrol.forge;

import com.khorn.terraincontrol.biomegenerators.BiomeCache;
import net.minecraft.world.biome.BiomeGenBase;
import net.minecraft.world.biome.WorldChunkManager;

public class BiomeCacheWrapper implements BiomeCache {
   private net.minecraft.world.biome.BiomeCache handle;

   public BiomeCacheWrapper(WorldChunkManager manager) {
      super();
      this.handle = new net.minecraft.world.biome.BiomeCache(manager);
   }

   public int getBiome(int x, int z) {
      return this.handle.func_76837_b(x, z).field_76756_M;
   }

   public void cleanupCache() {
      this.handle.func_76838_a();
   }

   public int[] getCachedBiomes(int x, int z) {
      BiomeGenBase[] cached = this.handle.func_76839_e(x, z);
      int[] intCache = new int[cached.length];

      for(int i = 0; i < cached.length; ++i) {
         intCache[i] = cached[i].field_76756_M;
      }

      return intCache;
   }
}
