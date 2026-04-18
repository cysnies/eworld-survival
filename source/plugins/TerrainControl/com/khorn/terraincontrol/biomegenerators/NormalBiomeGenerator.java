package com.khorn.terraincontrol.biomegenerators;

import com.khorn.terraincontrol.LocalWorld;
import com.khorn.terraincontrol.biomelayers.layers.Layer;

public class NormalBiomeGenerator extends BiomeGenerator {
   private Layer unZoomedLayer;
   private Layer biomeLayer;

   public NormalBiomeGenerator(LocalWorld world, BiomeCache cache) {
      super(world, cache);
      Layer[] layers = Layer.Init(world.getSeed(), world);
      this.unZoomedLayer = layers[0];
      this.biomeLayer = layers[1];
   }

   public int[] getBiomesUnZoomed(int[] biomeArray, int x, int z, int x_size, int z_size) {
      if (biomeArray == null || biomeArray.length < x_size * z_size) {
         biomeArray = new int[x_size * z_size];
      }

      int[] arrayOfInt = this.unZoomedLayer.Calculate(x, z, x_size, z_size);
      System.arraycopy(arrayOfInt, 0, biomeArray, 0, x_size * z_size);
      return biomeArray;
   }

   public float[] getTemperatures(float[] paramArrayOfFloat, int x, int z, int x_size, int z_size) {
      if (paramArrayOfFloat == null || paramArrayOfFloat.length < x_size * z_size) {
         paramArrayOfFloat = new float[x_size * z_size];
      }

      int[] arrayOfInt = this.biomeLayer.Calculate(x, z, x_size, z_size);

      for(int i = 0; i < x_size * z_size; ++i) {
         float f1 = (float)this.worldConfig.biomeConfigs[arrayOfInt[i]].getTemperature() / 65536.0F;
         if (f1 < this.worldConfig.minTemperature) {
            f1 = this.worldConfig.minTemperature;
         }

         if (f1 > this.worldConfig.maxTemperature) {
            f1 = this.worldConfig.maxTemperature;
         }

         paramArrayOfFloat[i] = f1;
      }

      return paramArrayOfFloat;
   }

   public float[] getRainfall(float[] paramArrayOfFloat, int paramInt1, int paramInt2, int paramInt3, int paramInt4) {
      if (paramArrayOfFloat == null || paramArrayOfFloat.length < paramInt3 * paramInt4) {
         paramArrayOfFloat = new float[paramInt3 * paramInt4];
      }

      int[] arrayOfInt = this.biomeLayer.Calculate(paramInt1, paramInt2, paramInt3, paramInt4);

      for(int i = 0; i < paramInt3 * paramInt4; ++i) {
         float f1 = (float)this.worldConfig.biomeConfigs[arrayOfInt[i]].getWetness() / 65536.0F;
         if (f1 < this.worldConfig.minMoisture) {
            f1 = this.worldConfig.minMoisture;
         }

         if (f1 > this.worldConfig.maxMoisture) {
            f1 = this.worldConfig.maxMoisture;
         }

         paramArrayOfFloat[i] = f1;
      }

      return paramArrayOfFloat;
   }

   public int[] getBiomes(int[] biomeArray, int x, int z, int x_size, int z_size) {
      boolean useCache = true;
      if (biomeArray == null || biomeArray.length < x_size * z_size) {
         biomeArray = new int[x_size * z_size];
         useCache = false;
      }

      if (useCache && x_size == 16 && z_size == 16 && (x & 15) == 0 && (z & 15) == 0) {
         synchronized(this.lockObject) {
            biomeArray = this.cache.getCachedBiomes(x, z);
            return biomeArray;
         }
      } else {
         int[] arrayOfInt = this.biomeLayer.Calculate(x, z, x_size, z_size);
         System.arraycopy(arrayOfInt, 0, biomeArray, 0, x_size * z_size);
         return biomeArray;
      }
   }

   public int getBiome(int x, int z) {
      return this.cache.getBiome(x, z);
   }

   public void cleanupCache() {
      this.cache.cleanupCache();
   }
}
