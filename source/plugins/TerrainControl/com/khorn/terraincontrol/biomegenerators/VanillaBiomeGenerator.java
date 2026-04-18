package com.khorn.terraincontrol.biomegenerators;

import com.khorn.terraincontrol.LocalWorld;

public class VanillaBiomeGenerator extends BiomeGenerator {
   public VanillaBiomeGenerator(LocalWorld world, BiomeCache cache) {
      super(world, cache);
   }

   public int[] getBiomesUnZoomed(int[] biomeArray, int x, int z, int x_size, int z_size) {
      return null;
   }

   public float[] getTemperatures(float[] paramArrayOfFloat, int x, int z, int x_size, int z_size) {
      return null;
   }

   public float[] getRainfall(float[] paramArrayOfFloat, int x, int z, int x_size, int z_size) {
      return null;
   }

   public int[] getBiomes(int[] biomeArray, int x, int z, int x_size, int z_size) {
      return null;
   }

   public int getBiome(int x, int z) {
      return 0;
   }

   public void cleanupCache() {
   }
}
