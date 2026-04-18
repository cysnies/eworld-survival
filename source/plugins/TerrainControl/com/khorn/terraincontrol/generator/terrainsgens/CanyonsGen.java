package com.khorn.terraincontrol.generator.terrainsgens;

import com.khorn.terraincontrol.DefaultMaterial;
import com.khorn.terraincontrol.LocalWorld;
import com.khorn.terraincontrol.configuration.WorldConfig;
import com.khorn.terraincontrol.util.MathHelper;
import java.util.Random;

public class CanyonsGen extends TerrainGenBase {
   private float[] a = new float[1024];
   private WorldConfig worldSettings;

   public CanyonsGen(WorldConfig wrk, LocalWorld world) {
      super(world);
      this.worldSettings = wrk;
   }

   protected void a(long paramLong, int chunk_x, int chunk_z, byte[] paramArrayOfByte, double paramDouble1, double paramDouble2, double paramDouble3, float paramFloat1, float paramFloat2, float paramFloat3, int size, double paramDouble4) {
      Random localRandom = new Random(paramLong);
      double d1 = (double)(chunk_x * 16 + 8);
      double d2 = (double)(chunk_z * 16 + 8);
      float f1 = 0.0F;
      float f2 = 0.0F;
      int i = 0;
      float f3 = 1.0F;

      for(int j = 0; j < this.worldSettings.WorldHeight; ++j) {
         if (j == 0 || localRandom.nextInt(3) == 0) {
            f3 = 1.0F + localRandom.nextFloat() * localRandom.nextFloat() * 1.0F;
         }

         this.a[j] = f3 * f3;
      }

      for(int stepCount = 0; stepCount < size; ++stepCount) {
         double d3 = (double)1.5F + (double)(MathHelper.sin((float)stepCount * 3.141593F / (float)size) * paramFloat1 * 1.0F);
         double d4 = d3 * paramDouble4;
         d3 *= (double)localRandom.nextFloat() * (double)0.25F + (double)0.75F;
         d4 *= (double)localRandom.nextFloat() * (double)0.25F + (double)0.75F;
         float f4 = MathHelper.cos(paramFloat3);
         float f5 = MathHelper.sin(paramFloat3);
         paramDouble1 += (double)(MathHelper.cos(paramFloat2) * f4);
         paramDouble2 += (double)f5;
         paramDouble3 += (double)(MathHelper.sin(paramFloat2) * f4);
         paramFloat3 *= 0.7F;
         paramFloat3 += f2 * 0.05F;
         paramFloat2 += f1 * 0.05F;
         f2 *= 0.8F;
         f1 *= 0.5F;
         f2 += (localRandom.nextFloat() - localRandom.nextFloat()) * localRandom.nextFloat() * 2.0F;
         f1 += (localRandom.nextFloat() - localRandom.nextFloat()) * localRandom.nextFloat() * 4.0F;
         if (i != 0 || localRandom.nextInt(4) != 0) {
            double d5 = paramDouble1 - d1;
            double d6 = paramDouble3 - d2;
            double d7 = (double)(size - stepCount);
            double d8 = (double)(paramFloat1 + 2.0F + 16.0F);
            if (d5 * d5 + d6 * d6 - d7 * d7 > d8 * d8) {
               return;
            }

            if (!(paramDouble1 < d1 - (double)16.0F - d3 * (double)2.0F) && !(paramDouble3 < d2 - (double)16.0F - d3 * (double)2.0F) && !(paramDouble1 > d1 + (double)16.0F + d3 * (double)2.0F) && !(paramDouble3 > d2 + (double)16.0F + d3 * (double)2.0F)) {
               int k = MathHelper.floor(paramDouble1 - d3) - chunk_x * 16 - 1;
               int m = MathHelper.floor(paramDouble1 + d3) - chunk_x * 16 + 1;
               int n = MathHelper.floor(paramDouble2 - d4) - 1;
               int i1 = MathHelper.floor(paramDouble2 + d4) + 1;
               int i2 = MathHelper.floor(paramDouble3 - d3) - chunk_z * 16 - 1;
               int i3 = MathHelper.floor(paramDouble3 + d3) - chunk_z * 16 + 1;
               if (k < 0) {
                  k = 0;
               }

               if (m > 16) {
                  m = 16;
               }

               if (n < 1) {
                  n = 1;
               }

               if (i1 > this.worldSettings.WorldHeight - 8) {
                  i1 = this.worldSettings.WorldHeight - 8;
               }

               if (i2 < 0) {
                  i2 = 0;
               }

               if (i3 > 16) {
                  i3 = 16;
               }

               int i4 = 0;

               for(int i5 = k; i4 == 0 && i5 < m; ++i5) {
                  for(int i6 = i2; i4 == 0 && i6 < i3; ++i6) {
                     for(int i7 = i1 + 1; i4 == 0 && i7 >= n - 1; --i7) {
                        int i8 = (i5 * 16 + i6) * this.worldSettings.WorldHeight + i7;
                        if (i7 >= 0 && i7 < this.worldSettings.WorldHeight) {
                           if (paramArrayOfByte[i8] == DefaultMaterial.WATER.id || paramArrayOfByte[i8] == DefaultMaterial.STATIONARY_WATER.id) {
                              i4 = 1;
                           }

                           if (i7 != n - 1 && i5 != k && i5 != m - 1 && i6 != i2 && i6 != i3 - 1) {
                              i7 = n;
                           }
                        }
                     }
                  }
               }

               if (i4 == 0) {
                  for(int i5 = k; i5 < m; ++i5) {
                     double d9 = ((double)(i5 + chunk_x * 16) + (double)0.5F - paramDouble1) / d3;

                     for(int i8 = i2; i8 < i3; ++i8) {
                        double d10 = ((double)(i8 + chunk_z * 16) + (double)0.5F - paramDouble3) / d3;
                        int i9 = (i5 * 16 + i8) * this.worldSettings.WorldHeight + i1;
                        int i10 = 0;
                        if (d9 * d9 + d10 * d10 < (double)1.0F) {
                           for(int i11 = i1 - 1; i11 >= n; --i11) {
                              double d11 = ((double)i11 + (double)0.5F - paramDouble2) / d4;
                              if ((d9 * d9 + d10 * d10) * (double)this.a[i11] + d11 * d11 / (double)6.0F < (double)1.0F) {
                                 int i12 = paramArrayOfByte[i9];
                                 if (i12 == DefaultMaterial.GRASS.id) {
                                    i10 = 1;
                                 }

                                 if (i12 == DefaultMaterial.STONE.id || i12 == DefaultMaterial.DIRT.id || i12 == DefaultMaterial.GRASS.id) {
                                    if (i11 < 10) {
                                       paramArrayOfByte[i9] = (byte)DefaultMaterial.LAVA.id;
                                    } else {
                                       paramArrayOfByte[i9] = 0;
                                       if (i10 != 0 && paramArrayOfByte[i9 - 1] == DefaultMaterial.DIRT.id) {
                                          paramArrayOfByte[i9 - 1] = (byte)DefaultMaterial.GRASS.id;
                                       }
                                    }
                                 }
                              }

                              --i9;
                           }
                        }
                     }
                  }

                  if (i != 0) {
                     break;
                  }
               }
            }
         }
      }

   }

   protected void a(int paramInt1, int paramInt2, int chunk_x, int chunk_z, byte[] paramArrayOfByte) {
      if (this.c.nextInt(100) < this.worldSettings.canyonRarity) {
         double d1 = (double)(paramInt1 * 16 + this.c.nextInt(16));
         double d2 = (double)(this.c.nextInt(this.worldSettings.canyonMaxAltitude - this.worldSettings.canyonMinAltitude) + this.worldSettings.canyonMinAltitude);
         double d3 = (double)(paramInt2 * 16 + this.c.nextInt(16));
         int i = 1;

         for(int j = 0; j < i; ++j) {
            float f1 = this.c.nextFloat() * 3.141593F * 2.0F;
            float f2 = (this.c.nextFloat() - 0.5F) * 2.0F / 8.0F;
            float f3 = (this.c.nextFloat() * 2.0F + this.c.nextFloat()) * 2.0F;
            int size = this.c.nextInt(this.worldSettings.canyonMaxLength - this.worldSettings.canyonMinLength) + this.worldSettings.canyonMinLength;
            this.a(this.c.nextLong(), chunk_x, chunk_z, paramArrayOfByte, d1, d2, d3, f3, f1, f2, size, this.worldSettings.canyonDepth);
         }

      }
   }
}
