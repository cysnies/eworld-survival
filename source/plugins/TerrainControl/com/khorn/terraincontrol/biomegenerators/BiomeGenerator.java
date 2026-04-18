package com.khorn.terraincontrol.biomegenerators;

import com.khorn.terraincontrol.LocalWorld;
import com.khorn.terraincontrol.configuration.WorldConfig;

public abstract class BiomeGenerator {
   protected WorldConfig worldConfig;
   protected BiomeCache cache;
   public final Object lockObject = new Object();

   public BiomeGenerator(LocalWorld world, BiomeCache cache) {
      super();
      this.worldConfig = world.getSettings();
      this.cache = cache;
   }

   public int[] getBiomesUnZoomed(int[] biomeArray, int x, int z, int xSize, int zSize) {
      return this.getBiomes(biomeArray, x, z, xSize, zSize);
   }

   public abstract float[] getTemperatures(float[] var1, int var2, int var3, int var4, int var5);

   public abstract float[] getRainfall(float[] var1, int var2, int var3, int var4, int var5);

   public abstract int[] getBiomes(int[] var1, int var2, int var3, int var4, int var5);

   public abstract int getBiome(int var1, int var2);

   public abstract void cleanupCache();
}
