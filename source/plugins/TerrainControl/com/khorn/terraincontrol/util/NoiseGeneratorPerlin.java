package com.khorn.terraincontrol.util;

import java.util.Random;

public class NoiseGeneratorPerlin {
   private int[] permutations = new int[512];
   public double xCoord;
   public double yCoord;
   public double zCoord;

   public NoiseGeneratorPerlin(Random random) {
      super();
      this.xCoord = random.nextDouble() * (double)256.0F;
      this.yCoord = random.nextDouble() * (double)256.0F;
      this.zCoord = random.nextDouble() * (double)256.0F;

      for(int i = 0; i < 256; this.permutations[i] = i++) {
      }

      for(int j = 0; j < 256; ++j) {
         int k = random.nextInt(256 - j) + j;
         int l = this.permutations[j];
         this.permutations[j] = this.permutations[k];
         this.permutations[k] = l;
         this.permutations[j + 256] = this.permutations[j];
      }

   }

   public final double lerp(double d, double d1, double d2) {
      return d1 + d * (d2 - d1);
   }

   public final double func_4110_a(int i, double d, double d1) {
      int j = i & 15;
      double d2 = (double)(1 - ((j & 8) >> 3)) * d;
      double d3 = j >= 4 ? (j != 12 && j != 14 ? d1 : d) : (double)0.0F;
      return ((j & 1) != 0 ? -d2 : d2) + ((j & 2) != 0 ? -d3 : d3);
   }

   public final double grad(int i, double d, double d1, double d2) {
      int j = i & 15;
      double d3 = j >= 8 ? d1 : d;
      double d4 = j >= 4 ? (j != 12 && j != 14 ? d2 : d) : d1;
      return ((j & 1) != 0 ? -d3 : d3) + ((j & 2) != 0 ? -d4 : d4);
   }

   public void a(double[] ad, double d, double d1, double d2, int i, int j, int k, double d3, double d4, double d5, double d6) {
      if (j == 1) {
         int j3 = 0;
         double d12 = (double)1.0F / d6;

         for(int i4 = 0; i4 < i; ++i4) {
            double d14 = d + (double)i4 * d3 + this.xCoord;
            int j4 = (int)d14;
            if (d14 < (double)j4) {
               --j4;
            }

            int k4 = j4 & 255;
            d14 -= (double)j4;
            double d17 = d14 * d14 * d14 * (d14 * (d14 * (double)6.0F - (double)15.0F) + (double)10.0F);

            for(int l4 = 0; l4 < k; ++l4) {
               double d19 = d2 + (double)l4 * d5 + this.zCoord;
               int j5 = (int)d19;
               if (d19 < (double)j5) {
                  --j5;
               }

               int l5 = j5 & 255;
               d19 -= (double)j5;
               double d21 = d19 * d19 * d19 * (d19 * (d19 * (double)6.0F - (double)15.0F) + (double)10.0F);
               int l = this.permutations[k4] + 0;
               int j1 = this.permutations[l] + l5;
               int k1 = this.permutations[k4 + 1] + 0;
               int l1 = this.permutations[k1] + l5;
               double d9 = this.lerp(d17, this.func_4110_a(this.permutations[j1], d14, d19), this.grad(this.permutations[l1], d14 - (double)1.0F, (double)0.0F, d19));
               double d11 = this.lerp(d17, this.grad(this.permutations[j1 + 1], d14, (double)0.0F, d19 - (double)1.0F), this.grad(this.permutations[l1 + 1], d14 - (double)1.0F, (double)0.0F, d19 - (double)1.0F));
               double d23 = this.lerp(d21, d9, d11);
               int var79 = j3++;
               ad[var79] += d23 * d12;
            }
         }

      } else {
         int i1 = 0;
         double d7 = (double)1.0F / d6;
         int i2 = -1;
         double d13 = (double)0.0F;
         double d15 = (double)0.0F;
         double d16 = (double)0.0F;
         double d18 = (double)0.0F;

         for(int i5 = 0; i5 < i; ++i5) {
            double d20 = d + (double)i5 * d3 + this.xCoord;
            int k5 = (int)d20;
            if (d20 < (double)k5) {
               --k5;
            }

            int i6 = k5 & 255;
            d20 -= (double)k5;
            double d22 = d20 * d20 * d20 * (d20 * (d20 * (double)6.0F - (double)15.0F) + (double)10.0F);

            for(int j6 = 0; j6 < k; ++j6) {
               double d24 = d2 + (double)j6 * d5 + this.zCoord;
               int k6 = (int)d24;
               if (d24 < (double)k6) {
                  --k6;
               }

               int l6 = k6 & 255;
               d24 -= (double)k6;
               double d25 = d24 * d24 * d24 * (d24 * (d24 * (double)6.0F - (double)15.0F) + (double)10.0F);

               for(int i7 = 0; i7 < j; ++i7) {
                  double d26 = d1 + (double)i7 * d4 + this.yCoord;
                  int j7 = (int)d26;
                  if (d26 < (double)j7) {
                     --j7;
                  }

                  int k7 = j7 & 255;
                  d26 -= (double)j7;
                  double d27 = d26 * d26 * d26 * (d26 * (d26 * (double)6.0F - (double)15.0F) + (double)10.0F);
                  if (i7 == 0 || k7 != i2) {
                     i2 = k7;
                     int j2 = this.permutations[i6] + k7;
                     int k2 = this.permutations[j2] + l6;
                     int l2 = this.permutations[j2 + 1] + l6;
                     int i3 = this.permutations[i6 + 1] + k7;
                     int k3 = this.permutations[i3] + l6;
                     int l3 = this.permutations[i3 + 1] + l6;
                     d13 = this.lerp(d22, this.grad(this.permutations[k2], d20, d26, d24), this.grad(this.permutations[k3], d20 - (double)1.0F, d26, d24));
                     d15 = this.lerp(d22, this.grad(this.permutations[l2], d20, d26 - (double)1.0F, d24), this.grad(this.permutations[l3], d20 - (double)1.0F, d26 - (double)1.0F, d24));
                     d16 = this.lerp(d22, this.grad(this.permutations[k2 + 1], d20, d26, d24 - (double)1.0F), this.grad(this.permutations[k3 + 1], d20 - (double)1.0F, d26, d24 - (double)1.0F));
                     d18 = this.lerp(d22, this.grad(this.permutations[l2 + 1], d20, d26 - (double)1.0F, d24 - (double)1.0F), this.grad(this.permutations[l3 + 1], d20 - (double)1.0F, d26 - (double)1.0F, d24 - (double)1.0F));
                  }

                  double d28 = this.lerp(d27, d13, d15);
                  double d29 = this.lerp(d27, d16, d18);
                  double d30 = this.lerp(d25, d28, d29);
                  int var10001 = i1++;
                  ad[var10001] += d30 * d7;
               }
            }
         }

      }
   }
}
