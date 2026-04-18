package com.khorn.terraincontrol.biomegenerators;

import com.khorn.terraincontrol.DefaultBiome;
import com.khorn.terraincontrol.LocalWorld;
import com.khorn.terraincontrol.util.NoiseGeneratorOctaves2;
import java.util.Random;

public class OldBiomeGenerator extends BiomeGenerator {
   private NoiseGeneratorOctaves2 temperatureGenerator1;
   private NoiseGeneratorOctaves2 wetnessGenerator;
   private NoiseGeneratorOctaves2 temperatureGenerator2;
   public double[] oldTemperature1;
   public double[] oldWetness;
   private double[] oldTemperature2;
   private static int[] biomeDiagram = new int[4096];
   private static boolean hasGeneratedBiomeDiagram;

   public OldBiomeGenerator(LocalWorld world, BiomeCache cache) {
      super(world, cache);
      this.temperatureGenerator1 = new NoiseGeneratorOctaves2(new Random(world.getSeed() * 9871L), 4);
      this.wetnessGenerator = new NoiseGeneratorOctaves2(new Random(world.getSeed() * 39811L), 4);
      this.temperatureGenerator2 = new NoiseGeneratorOctaves2(new Random(world.getSeed() * 543321L), 2);
      if (!hasGeneratedBiomeDiagram) {
         hasGeneratedBiomeDiagram = true;
         generateBiomeDiagram();
      }

   }

   public float[] getTemperatures(float[] temp_out, int x, int z, int x_size, int z_size) {
      if (temp_out == null || temp_out.length < x_size * z_size) {
         temp_out = new float[x_size * z_size];
      }

      this.oldTemperature1 = this.temperatureGenerator1.a(this.oldTemperature1, (double)x, (double)z, x_size, z_size, 0.025000000372529 / this.worldConfig.oldBiomeSize, 0.025000000372529 / this.worldConfig.oldBiomeSize, (double)0.25F);
      this.oldTemperature2 = this.temperatureGenerator2.a(this.oldTemperature2, (double)x, (double)z, x_size, z_size, (double)0.25F / this.worldConfig.oldBiomeSize, (double)0.25F / this.worldConfig.oldBiomeSize, 0.5882352941176471);
      int i = 0;

      for(int j = 0; j < x_size; ++j) {
         for(int k = 0; k < z_size; ++k) {
            double d1 = this.oldTemperature2[i] * 1.1 + (double)0.5F;
            double d2 = 0.01;
            double d3 = (double)1.0F - d2;
            double d4 = ((double)temp_out[i] * 0.15 + 0.7) * d3 + d1 * d2;
            d4 = (double)1.0F - ((double)1.0F - d4) * ((double)1.0F - d4);
            if (d4 < (double)this.worldConfig.minTemperature) {
               d4 = (double)this.worldConfig.minTemperature;
            }

            if (d4 > (double)this.worldConfig.maxTemperature) {
               d4 = (double)this.worldConfig.maxTemperature;
            }

            temp_out[i] = (float)d4;
            ++i;
         }
      }

      if (this.worldConfig.isDeprecated) {
         this.worldConfig = this.worldConfig.newSettings;
      }

      return temp_out;
   }

   public float[] getRainfall(float[] temp_out, int x, int z, int x_size, int z_size) {
      if (temp_out == null || temp_out.length < x_size * z_size) {
         temp_out = new float[x_size * z_size];
      }

      int[] temp_biomeBases = new int[x_size * z_size];
      this.getBiomes(temp_biomeBases, x, z, x_size, z_size, false);

      for(int i = 0; i < temp_out.length; ++i) {
         temp_out[i] = (float)this.oldWetness[i];
      }

      return temp_out;
   }

   public int[] getBiomes(int[] paramArrayOfBiomeBase, int x, int z, int x_size, int z_size, boolean useCache) {
      if (paramArrayOfBiomeBase == null || paramArrayOfBiomeBase.length < x_size * z_size) {
         paramArrayOfBiomeBase = new int[x_size * z_size];
      }

      if (useCache && x_size == 16 && z_size == 16 && (x & 15) == 0 && (z & 15) == 0) {
         int[] localObject = this.cache.getCachedBiomes(x, z);
         System.arraycopy(localObject, 0, paramArrayOfBiomeBase, 0, x_size * z_size);
         return paramArrayOfBiomeBase;
      } else {
         this.oldTemperature1 = this.temperatureGenerator1.a(this.oldTemperature1, (double)x, (double)z, x_size, x_size, 0.025000000372529 / this.worldConfig.oldBiomeSize, 0.025000000372529 / this.worldConfig.oldBiomeSize, (double)0.25F);
         this.oldWetness = this.wetnessGenerator.a(this.oldWetness, (double)x, (double)z, x_size, x_size, 0.0500000007450581 / this.worldConfig.oldBiomeSize, 0.0500000007450581 / this.worldConfig.oldBiomeSize, 0.3333333333333333);
         this.oldTemperature2 = this.temperatureGenerator2.a(this.oldTemperature2, (double)x, (double)z, x_size, x_size, (double)0.25F / this.worldConfig.oldBiomeSize, (double)0.25F / this.worldConfig.oldBiomeSize, 0.5882352941176471);
         int i = 0;

         for(int j = 0; j < x_size; ++j) {
            for(int k = 0; k < z_size; ++k) {
               double d1 = this.oldTemperature2[i] * 1.1 + (double)0.5F;
               double d2 = 0.01;
               double d3 = (double)1.0F - d2;
               double d4 = (this.oldTemperature1[i] * 0.15 + 0.7) * d3 + d1 * d2;
               d2 = 0.002;
               d3 = (double)1.0F - d2;
               double d5 = (this.oldWetness[i] * 0.15 + (double)0.5F) * d3 + d1 * d2;
               d4 = (double)1.0F - ((double)1.0F - d4) * ((double)1.0F - d4);
               if (d4 < (double)this.worldConfig.minTemperature) {
                  d4 = (double)this.worldConfig.minTemperature;
               }

               if (d5 < (double)this.worldConfig.minMoisture) {
                  d5 = (double)this.worldConfig.minMoisture;
               }

               if (d4 > (double)this.worldConfig.maxTemperature) {
                  d4 = (double)this.worldConfig.maxTemperature;
               }

               if (d5 > (double)this.worldConfig.maxMoisture) {
                  d5 = (double)this.worldConfig.maxMoisture;
               }

               this.oldTemperature1[i] = d4;
               this.oldWetness[i] = d5;
               paramArrayOfBiomeBase[i++] = getBiomeFromDiagram(d4, d5);
            }
         }

         if (this.worldConfig.isDeprecated) {
            this.worldConfig = this.worldConfig.newSettings;
         }

         return paramArrayOfBiomeBase;
      }
   }

   public int[] getBiomes(int[] biomeArray, int x, int z, int x_size, int z_size) {
      return this.getBiomes(biomeArray, x, z, x_size, z_size, false);
   }

   public int getBiome(int x, int z) {
      return this.cache.getBiome(x, z);
   }

   public void cleanupCache() {
      this.cache.cleanupCache();
   }

   private static int getBiomeFromDiagram(double temp, double rain) {
      int i = (int)(temp * (double)63.0F);
      int j = (int)(rain * (double)63.0F);
      return biomeDiagram[i + j * 64];
   }

   private static void generateBiomeDiagram() {
      for(int i = 0; i < 64; ++i) {
         for(int j = 0; j < 64; ++j) {
            biomeDiagram[i + j * 64] = generatePositionOnBiomeDiagram((double)((float)i / 63.0F), (double)((float)j / 63.0F));
         }
      }

   }

   private static int generatePositionOnBiomeDiagram(double paramFloat1, double paramFloat2) {
      paramFloat2 *= paramFloat1;
      if (paramFloat1 < (double)0.1F) {
         return DefaultBiome.PLAINS.Id;
      } else if (paramFloat2 < (double)0.2F) {
         if (paramFloat1 < (double)0.5F) {
            return DefaultBiome.PLAINS.Id;
         } else {
            return paramFloat1 < (double)0.95F ? DefaultBiome.PLAINS.Id : DefaultBiome.DESERT.Id;
         }
      } else if (paramFloat2 > (double)0.5F && paramFloat1 < (double)0.7F) {
         return DefaultBiome.SWAMPLAND.Id;
      } else if (paramFloat1 < (double)0.5F) {
         return DefaultBiome.TAIGA.Id;
      } else if (paramFloat1 < (double)0.97F) {
         return paramFloat2 < (double)0.35F ? DefaultBiome.TAIGA.Id : DefaultBiome.FOREST.Id;
      } else if (paramFloat2 < (double)0.45F) {
         return DefaultBiome.PLAINS.Id;
      } else {
         return paramFloat2 < (double)0.9F ? DefaultBiome.FOREST.Id : DefaultBiome.FOREST.Id;
      }
   }
}
