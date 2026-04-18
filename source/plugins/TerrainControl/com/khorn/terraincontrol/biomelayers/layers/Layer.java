package com.khorn.terraincontrol.biomelayers.layers;

import com.khorn.terraincontrol.DefaultBiome;
import com.khorn.terraincontrol.LocalBiome;
import com.khorn.terraincontrol.LocalWorld;
import com.khorn.terraincontrol.TerrainControl;
import com.khorn.terraincontrol.configuration.BiomeConfig;
import com.khorn.terraincontrol.configuration.WorldConfig;
import java.util.ArrayList;

public abstract class Layer {
   protected long b;
   protected Layer child;
   private long c;
   protected long d;
   protected static final int BiomeBits = 255;
   protected static final int LandBit = 256;
   protected static final int RiverBits = 3072;
   protected static final int IceBit = 512;
   protected static final int IslandBit = 4096;

   protected static int GetBiomeFromLayer(int BiomeAndLand) {
      return (BiomeAndLand & 256) != 0 ? BiomeAndLand & 255 : 0;
   }

   public static Layer[] Init(long paramLong, LocalWorld world) {
      WorldConfig config = world.getSettings();
      LocalBiome[][] NormalBiomeMap = new LocalBiome[config.GenerationDepth + 1][];
      LocalBiome[][] IceBiomeMap = new LocalBiome[config.GenerationDepth + 1][];

      for(int i = 0; i < config.GenerationDepth + 1; ++i) {
         ArrayList<LocalBiome> normalBiomes = new ArrayList();
         ArrayList<LocalBiome> iceBiomes = new ArrayList();

         for(BiomeConfig biomeConfig : config.biomeConfigs) {
            if (biomeConfig != null && biomeConfig.BiomeSize == i) {
               if (config.NormalBiomes.contains(biomeConfig.name)) {
                  for(int t = 0; t < biomeConfig.BiomeRarity; ++t) {
                     normalBiomes.add(biomeConfig.Biome);
                  }

                  config.normalBiomesRarity -= biomeConfig.BiomeRarity;
               }

               if (config.IceBiomes.contains(biomeConfig.name)) {
                  for(int t = 0; t < biomeConfig.BiomeRarity; ++t) {
                     iceBiomes.add(biomeConfig.Biome);
                  }

                  config.iceBiomesRarity -= biomeConfig.BiomeRarity;
               }
            }
         }

         if (normalBiomes.size() != 0) {
            NormalBiomeMap[i] = (LocalBiome[])normalBiomes.toArray(new LocalBiome[normalBiomes.size() + config.normalBiomesRarity]);
         } else {
            NormalBiomeMap[i] = new LocalBiome[0];
         }

         if (iceBiomes.size() != 0) {
            IceBiomeMap[i] = (LocalBiome[])iceBiomes.toArray(new LocalBiome[iceBiomes.size() + config.iceBiomesRarity]);
         } else {
            IceBiomeMap[i] = new LocalBiome[0];
         }
      }

      Layer MainLayer = new LayerEmpty(1L);

      for(int depth = 0; depth <= config.GenerationDepth; ++depth) {
         MainLayer = new LayerZoom((long)(2001 + depth), MainLayer);
         if (config.LandSize == depth) {
            Layer var20 = new LayerLand(1L, MainLayer, config.LandRarity);
            MainLayer = new LayerZoomFuzzy(2000L, var20);
         }

         if (depth < config.LandSize + config.LandFuzzy) {
            MainLayer = new LayerLandRandom((long)depth, MainLayer);
         }

         if (NormalBiomeMap[depth].length != 0 || IceBiomeMap[depth].length != 0) {
            LayerBiome layerBiome = new LayerBiome(200L, MainLayer);
            layerBiome.biomes = NormalBiomeMap[depth];
            layerBiome.ice_biomes = IceBiomeMap[depth];
            MainLayer = layerBiome;
         }

         if (config.IceSize == depth) {
            MainLayer = new LayerIce((long)depth, MainLayer, config.IceRarity);
         }

         if (config.RiverRarity == depth) {
            MainLayer = new LayerRiverInit(155L, MainLayer);
         }

         if (config.GenerationDepth - config.RiverSize == depth) {
            MainLayer = new LayerRiver((long)(5 + depth), MainLayer);
         }

         LayerBiomeBorder layerBiomeBorder = new LayerBiomeBorder((long)(3000 + depth), world);
         boolean haveBorder = false;

         for(BiomeConfig biomeConfig : config.biomeConfigs) {
            if (biomeConfig != null && biomeConfig.BiomeSize == depth) {
               if (config.IsleBiomes.contains(biomeConfig.name) && biomeConfig.IsleInBiome != null) {
                  int id = biomeConfig.Biome.getId();
                  if (biomeConfig.Biome.isCustom()) {
                     id = biomeConfig.Biome.getCustomId();
                  }

                  LayerBiomeInBiome layerBiome = new LayerBiomeInBiome((long)(4000 + id), MainLayer);
                  layerBiome.biome = biomeConfig.Biome;

                  for(String islandInName : biomeConfig.IsleInBiome) {
                     int islandIn = world.getBiomeIdByName(islandInName);
                     if (islandIn == DefaultBiome.OCEAN.Id) {
                        layerBiome.inOcean = true;
                     } else {
                        layerBiome.BiomeIsles[islandIn] = true;
                     }
                  }

                  layerBiome.chance = config.BiomeRarityScale + 1 - biomeConfig.BiomeRarity;
                  MainLayer = layerBiome;
               }

               if (config.BorderBiomes.contains(biomeConfig.name) && biomeConfig.BiomeIsBorder != null) {
                  haveBorder = true;

                  for(String replaceFromName : biomeConfig.BiomeIsBorder) {
                     int replaceFrom = world.getBiomeIdByName(replaceFromName);
                     layerBiomeBorder.AddBiome(biomeConfig, replaceFrom, world);
                  }
               }
            }
         }

         if (haveBorder) {
            layerBiomeBorder.child = MainLayer;
            MainLayer = layerBiomeBorder;
         }
      }

      Layer var21 = new LayerMix(1L, MainLayer, config, world);
      var21 = new LayerSmooth(400L, var21);
      if (config.biomeMode == TerrainControl.getBiomeModeManager().FROM_IMAGE) {
         if (config.imageMode == WorldConfig.ImageMode.ContinueNormal) {
            var21 = new LayerFromImage(1L, var21, config, world);
         } else {
            var21 = new LayerFromImage(1L, (Layer)null, config, world);
         }
      }

      Layer ZoomedLayer = new LayerZoomVoronoi(10L, var21);
      ZoomedLayer.b(paramLong);
      Layer var23 = new LayerCacheInit(1L, var21);
      ZoomedLayer = new LayerCacheInit(1L, ZoomedLayer);
      return new Layer[]{var23, ZoomedLayer};
   }

   public Layer(long paramLong) {
      super();
      this.d = paramLong;
      this.d *= this.d * 6364136223846793005L + 1442695040888963407L;
      this.d += paramLong;
      this.d *= this.d * 6364136223846793005L + 1442695040888963407L;
      this.d += paramLong;
      this.d *= this.d * 6364136223846793005L + 1442695040888963407L;
      this.d += paramLong;
   }

   public void b(long paramLong) {
      this.b = paramLong;
      if (this.child != null) {
         this.child.b(paramLong);
      }

      this.b *= this.b * 6364136223846793005L + 1442695040888963407L;
      this.b += this.d;
      this.b *= this.b * 6364136223846793005L + 1442695040888963407L;
      this.b += this.d;
      this.b *= this.b * 6364136223846793005L + 1442695040888963407L;
      this.b += this.d;
   }

   protected void SetSeed(long paramLong1, long paramLong2) {
      this.c = this.b;
      this.c *= this.c * 6364136223846793005L + 1442695040888963407L;
      this.c += paramLong1;
      this.c *= this.c * 6364136223846793005L + 1442695040888963407L;
      this.c += paramLong2;
      this.c *= this.c * 6364136223846793005L + 1442695040888963407L;
      this.c += paramLong1;
      this.c *= this.c * 6364136223846793005L + 1442695040888963407L;
      this.c += paramLong2;
   }

   protected int nextInt(int paramInt) {
      int i = (int)((this.c >> 24) % (long)paramInt);
      if (i < 0) {
         i += paramInt;
      }

      this.c *= this.c * 6364136223846793005L + 1442695040888963407L;
      this.c += this.b;
      return i;
   }

   protected abstract int[] GetBiomes(int var1, int var2, int var3, int var4, int var5);

   public int[] Calculate(int x, int z, int x_size, int z_size) {
      return new int[0];
   }
}
