package com.khorn.terraincontrol.biomegenerators;

public interface BiomeCache {
   int getBiome(int var1, int var2);

   void cleanupCache();

   int[] getCachedBiomes(int var1, int var2);
}
