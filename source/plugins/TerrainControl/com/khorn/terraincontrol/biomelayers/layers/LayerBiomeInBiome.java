package com.khorn.terraincontrol.biomelayers.layers;

import com.khorn.terraincontrol.LocalBiome;
import com.khorn.terraincontrol.biomelayers.ArraysCache;

public class LayerBiomeInBiome extends Layer {
   public LocalBiome biome;
   public int chance = 10;
   public boolean inOcean = false;
   public boolean[] BiomeIsles = new boolean[256];

   public LayerBiomeInBiome(long paramLong, Layer paramGenLayer) {
      super(paramLong);
      this.child = paramGenLayer;

      for(int i = 0; i < this.BiomeIsles.length; ++i) {
         this.BiomeIsles[i] = false;
      }

   }

   public int[] GetBiomes(int cacheId, int paramInt1, int paramInt2, int paramInt3, int paramInt4) {
      int i = paramInt1 - 1;
      int j = paramInt2 - 1;
      int k = paramInt3 + 2;
      int m = paramInt4 + 2;
      int[] arrayOfInt1 = this.child.GetBiomes(cacheId, i, j, k, m);
      int[] arrayOfInt2 = ArraysCache.GetArray(cacheId, paramInt3 * paramInt4);

      for(int n = 0; n < paramInt4; ++n) {
         for(int i1 = 0; i1 < paramInt3; ++i1) {
            this.SetSeed((long)(i1 + paramInt1), (long)(n + paramInt2));
            int currentPiece = arrayOfInt1[i1 + 1 + (n + 1) * k];
            boolean spawn = false;
            if (this.inOcean) {
               int i2 = arrayOfInt1[i1 + 0 + (n + 0) * k] & 256;
               int i3 = arrayOfInt1[i1 + 2 + (n + 0) * k] & 256;
               int i4 = arrayOfInt1[i1 + 0 + (n + 2) * k] & 256;
               int i5 = arrayOfInt1[i1 + 2 + (n + 2) * k] & 256;
               if ((currentPiece & 256) == 0 && i2 == 0 && i3 == 0 && i4 == 0 && i5 == 0 && this.nextInt(this.chance) == 0) {
                  currentPiece = currentPiece & 512 | currentPiece & 3072 | 256 | this.biome.getId() | 4096;
                  spawn = true;
               }
            }

            if (!spawn) {
               int i2 = arrayOfInt1[i1 + 0 + (n + 0) * k] & 255;
               int i3 = arrayOfInt1[i1 + 2 + (n + 0) * k] & 255;
               int i4 = arrayOfInt1[i1 + 0 + (n + 2) * k] & 255;
               int i5 = arrayOfInt1[i1 + 2 + (n + 2) * k] & 255;
               if (this.BiomeIsles[currentPiece & 255] && this.BiomeIsles[i2] && this.BiomeIsles[i3] && this.BiomeIsles[i4] && this.BiomeIsles[i5] && this.nextInt(this.chance) == 0) {
                  currentPiece = currentPiece & 256 | currentPiece & 512 | currentPiece & 3072 | this.biome.getId() | 4096;
               }
            }

            arrayOfInt2[i1 + n * paramInt3] = currentPiece;
         }
      }

      return arrayOfInt2;
   }
}
