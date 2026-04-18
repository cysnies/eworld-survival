package com.khorn.terraincontrol.forge;

import com.khorn.terraincontrol.DefaultBiome;
import com.khorn.terraincontrol.biomegenerators.BiomeGenerator;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import net.minecraft.world.ChunkPosition;
import net.minecraft.world.biome.BiomeGenBase;
import net.minecraft.world.biome.WorldChunkManager;
import net.minecraft.world.gen.structure.MapGenVillage;

public class TCWorldChunkManager extends WorldChunkManager {
   private BiomeGenerator biomeManager;
   private ArrayList field_76943_g = new ArrayList();
   private SingleWorld localWorld;

   public TCWorldChunkManager(SingleWorld world) {
      super();
      this.field_76943_g.add(BiomeGenBase.field_76773_a[DefaultBiome.FOREST.Id]);
      this.field_76943_g.add(BiomeGenBase.field_76773_a[DefaultBiome.PLAINS.Id]);
      this.field_76943_g.add(BiomeGenBase.field_76773_a[DefaultBiome.TAIGA.Id]);
      this.field_76943_g.add(BiomeGenBase.field_76773_a[DefaultBiome.DESERT_HILLS.Id]);
      this.field_76943_g.add(BiomeGenBase.field_76773_a[DefaultBiome.FOREST_HILLS.Id]);
      this.field_76943_g.add(BiomeGenBase.field_76773_a[DefaultBiome.JUNGLE.Id]);
      this.field_76943_g.add(BiomeGenBase.field_76773_a[DefaultBiome.JUNGLE_HILLS.Id]);
      this.localWorld = world;
   }

   public void setBiomeManager(BiomeGenerator manager) {
      this.biomeManager = manager;
   }

   public List a() {
      return this.field_76943_g;
   }

   public BiomeGenBase func_76935_a(int paramInt1, int paramInt2) {
      return BiomeGenBase.field_76773_a[this.biomeManager.getBiome(paramInt1, paramInt2)];
   }

   public float[] func_76936_a(float[] paramArrayOfFloat, int paramInt1, int paramInt2, int paramInt3, int paramInt4) {
      return this.biomeManager.getRainfall(paramArrayOfFloat, paramInt1, paramInt2, paramInt3, paramInt4);
   }

   public float[] func_76934_b(float[] paramArrayOfFloat, int paramInt1, int paramInt2, int paramInt3, int paramInt4) {
      return this.biomeManager.getTemperatures(paramArrayOfFloat, paramInt1, paramInt2, paramInt3, paramInt4);
   }

   public BiomeGenBase[] func_76937_a(BiomeGenBase[] paramArrayOfBiomeBase, int paramInt1, int paramInt2, int paramInt3, int paramInt4) {
      int[] arrayOfInt = this.biomeManager.getBiomesUnZoomed((int[])null, paramInt1, paramInt2, paramInt3, paramInt4);
      if (paramArrayOfBiomeBase == null || paramArrayOfBiomeBase.length < arrayOfInt.length) {
         paramArrayOfBiomeBase = new BiomeGenBase[arrayOfInt.length];
      }

      for(int i = 0; i < paramInt3 * paramInt4; ++i) {
         paramArrayOfBiomeBase[i] = BiomeGenBase.field_76773_a[arrayOfInt[i]];
      }

      return paramArrayOfBiomeBase;
   }

   public BiomeGenBase[] func_76931_a(BiomeGenBase[] paramArrayOfBiomeBase, int paramInt1, int paramInt2, int paramInt3, int paramInt4, boolean paramBoolean) {
      int[] arrayOfInt = this.biomeManager.getBiomes((int[])null, paramInt1, paramInt2, paramInt3, paramInt4);
      if (paramArrayOfBiomeBase == null || paramArrayOfBiomeBase.length < arrayOfInt.length) {
         paramArrayOfBiomeBase = new BiomeGenBase[arrayOfInt.length];
      }

      for(int i = 0; i < paramInt3 * paramInt4; ++i) {
         paramArrayOfBiomeBase[i] = BiomeGenBase.field_76773_a[arrayOfInt[i]];
      }

      return paramArrayOfBiomeBase;
   }

   public boolean func_76940_a(int paramInt1, int paramInt2, int paramInt3, List paramList) {
      if (paramList == MapGenVillage.field_75055_e) {
         paramList = this.localWorld.villageGen.villageSpawnBiomes;
      }

      int i = paramInt1 - paramInt3 >> 2;
      int j = paramInt2 - paramInt3 >> 2;
      int k = paramInt1 + paramInt3 >> 2;
      int m = paramInt2 + paramInt3 >> 2;
      int n = k - i + 1;
      int i1 = m - j + 1;
      BiomeGenBase[] arrayOfInt = this.func_76937_a((BiomeGenBase[])null, i, j, n, i1);

      for(int i2 = 0; i2 < n * i1; ++i2) {
         BiomeGenBase localBiomeBase = arrayOfInt[i2];
         if (!paramList.contains(localBiomeBase)) {
            return false;
         }
      }

      return true;
   }

   public ChunkPosition func_76941_a(int paramInt1, int paramInt2, int paramInt3, List paramList, Random paramRandom) {
      int i = paramInt1 - paramInt3 >> 2;
      int j = paramInt2 - paramInt3 >> 2;
      int k = paramInt1 + paramInt3 >> 2;
      int m = paramInt2 + paramInt3 >> 2;
      int n = k - i + 1;
      int i1 = m - j + 1;
      int[] arrayOfInt = this.biomeManager.getBiomesUnZoomed((int[])null, i, j, n, i1);
      ChunkPosition localChunkPosition = null;
      int i2 = 0;

      for(int i3 = 0; i3 < arrayOfInt.length; ++i3) {
         if (arrayOfInt[i3] < DefaultBiome.values().length) {
            int i4 = i + i3 % n << 2;
            int i5 = j + i3 / n << 2;
            BiomeGenBase localBiomeBase = BiomeGenBase.field_76773_a[arrayOfInt[i3]];
            if (paramList.contains(localBiomeBase) && (localChunkPosition == null || paramRandom.nextInt(i2 + 1) == 0)) {
               localChunkPosition = new ChunkPosition(i4, 0, i5);
               ++i2;
            }
         }
      }

      return localChunkPosition;
   }

   public void func_76938_b() {
      this.biomeManager.cleanupCache();
   }
}
