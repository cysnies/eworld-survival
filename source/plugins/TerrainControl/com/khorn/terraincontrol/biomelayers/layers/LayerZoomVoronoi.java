package com.khorn.terraincontrol.biomelayers.layers;

import com.khorn.terraincontrol.biomelayers.ArraysCache;

public class LayerZoomVoronoi extends Layer {
   public LayerZoomVoronoi(long paramLong, Layer paramGenLayer) {
      super(paramLong);
      this.child = paramGenLayer;
   }

   public int[] GetBiomes(int cacheId, int paramInt1, int paramInt2, int paramInt3, int paramInt4) {
      paramInt1 -= 2;
      paramInt2 -= 2;
      int i = 2;
      int j = 1 << i;
      int k = paramInt1 >> i;
      int m = paramInt2 >> i;
      int n = (paramInt3 >> i) + 3;
      int i1 = (paramInt4 >> i) + 3;
      int[] arrayOfInt1 = this.child.GetBiomes(cacheId, k, m, n, i1);
      int i2 = n << i;
      int i3 = i1 << i;
      int[] arrayOfInt2 = ArraysCache.GetArray(cacheId, i2 * i3);

      for(int i4 = 0; i4 < i1 - 1; ++i4) {
         int i5 = arrayOfInt1[0 + (i4 + 0) * n];
         int i6 = arrayOfInt1[0 + (i4 + 1) * n];

         for(int i7 = 0; i7 < n - 1; ++i7) {
            double d1 = (double)j * 0.9;
            this.SetSeed((long)(i7 + k << i), (long)(i4 + m << i));
            double d2 = ((double)this.nextInt(1024) / (double)1024.0F - (double)0.5F) * d1;
            double d3 = ((double)this.nextInt(1024) / (double)1024.0F - (double)0.5F) * d1;
            this.SetSeed((long)(i7 + k + 1 << i), (long)(i4 + m << i));
            double d4 = ((double)this.nextInt(1024) / (double)1024.0F - (double)0.5F) * d1 + (double)j;
            double d5 = ((double)this.nextInt(1024) / (double)1024.0F - (double)0.5F) * d1;
            this.SetSeed((long)(i7 + k << i), (long)(i4 + m + 1 << i));
            double d6 = ((double)this.nextInt(1024) / (double)1024.0F - (double)0.5F) * d1;
            double d7 = ((double)this.nextInt(1024) / (double)1024.0F - (double)0.5F) * d1 + (double)j;
            this.SetSeed((long)(i7 + k + 1 << i), (long)(i4 + m + 1 << i));
            double d8 = ((double)this.nextInt(1024) / (double)1024.0F - (double)0.5F) * d1 + (double)j;
            double d9 = ((double)this.nextInt(1024) / (double)1024.0F - (double)0.5F) * d1 + (double)j;
            int i8 = arrayOfInt1[i7 + 1 + (i4 + 0) * n];
            int i9 = arrayOfInt1[i7 + 1 + (i4 + 1) * n];

            for(int i10 = 0; i10 < j; ++i10) {
               int i11 = ((i4 << i) + i10) * i2 + (i7 << i);

               for(int i12 = 0; i12 < j; ++i12) {
                  double d10 = ((double)i10 - d3) * ((double)i10 - d3) + ((double)i12 - d2) * ((double)i12 - d2);
                  double d11 = ((double)i10 - d5) * ((double)i10 - d5) + ((double)i12 - d4) * ((double)i12 - d4);
                  double d12 = ((double)i10 - d7) * ((double)i10 - d7) + ((double)i12 - d6) * ((double)i12 - d6);
                  double d13 = ((double)i10 - d9) * ((double)i10 - d9) + ((double)i12 - d8) * ((double)i12 - d8);
                  if (d10 < d11 && d10 < d12 && d10 < d13) {
                     arrayOfInt2[i11++] = i5;
                  } else if (d11 < d10 && d11 < d12 && d11 < d13) {
                     arrayOfInt2[i11++] = i8;
                  } else if (d12 < d10 && d12 < d11 && d12 < d13) {
                     arrayOfInt2[i11++] = i6;
                  } else {
                     arrayOfInt2[i11++] = i9;
                  }
               }
            }

            i5 = i8;
            i6 = i9;
         }
      }

      int[] arrayOfInt3 = ArraysCache.GetArray(cacheId, paramInt3 * paramInt4);

      for(int i5 = 0; i5 < paramInt4; ++i5) {
         System.arraycopy(arrayOfInt2, (i5 + (paramInt2 & j - 1)) * (n << i) + (paramInt1 & j - 1), arrayOfInt3, i5 * paramInt3, paramInt3);
      }

      return arrayOfInt3;
   }
}
