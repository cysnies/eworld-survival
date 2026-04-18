package com.khorn.terraincontrol.util;

import java.util.Random;

public class NoiseGenerator2 {
   private static int[][] d = new int[][]{{1, 1, 0}, {-1, 1, 0}, {1, -1, 0}, {-1, -1, 0}, {1, 0, 1}, {-1, 0, 1}, {1, 0, -1}, {-1, 0, -1}, {0, 1, 1}, {0, -1, 1}, {0, 1, -1}, {0, -1, -1}};
   private int[] e = new int[512];
   public double a;
   public double b;
   public double c;
   private static final double f = (double)0.5F * (Math.sqrt((double)3.0F) - (double)1.0F);
   private static final double g = ((double)3.0F - Math.sqrt((double)3.0F)) / (double)6.0F;

   public NoiseGenerator2(Random paramRandom) {
      super();
      this.a = paramRandom.nextDouble() * (double)256.0F;
      this.b = paramRandom.nextDouble() * (double)256.0F;
      this.c = paramRandom.nextDouble() * (double)256.0F;

      for(int i = 0; i < 256; this.e[i] = i++) {
      }

      for(int i = 0; i < 256; ++i) {
         int j = paramRandom.nextInt(256 - i) + i;
         int k = this.e[i];
         this.e[i] = this.e[j];
         this.e[j] = k;
         this.e[i + 256] = this.e[i];
      }

   }

   private static int a(double paramDouble) {
      return paramDouble > (double)0.0F ? (int)paramDouble : (int)paramDouble - 1;
   }

   private static double a(int[] paramArrayOfInt, double paramDouble1, double paramDouble2) {
      return (double)paramArrayOfInt[0] * paramDouble1 + (double)paramArrayOfInt[1] * paramDouble2;
   }

   public void a(double[] paramArrayOfDouble, double paramDouble1, double paramDouble2, int paramInt1, int paramInt2, double paramDouble3, double paramDouble4, double paramDouble5) {
      int i = 0;

      for(int j = 0; j < paramInt1; ++j) {
         double d2 = (paramDouble2 + (double)j) * paramDouble4 + this.b;

         for(int k = 0; k < paramInt2; ++k) {
            double d1 = (paramDouble1 + (double)k) * paramDouble3 + this.a;
            double d3 = (d1 + d2) * f;
            int m = a(d1 + d3);
            int n = a(d2 + d3);
            double d4 = (double)(m + n) * g;
            double d5 = (double)m - d4;
            double d6 = (double)n - d4;
            double d7 = d1 - d5;
            double d8 = d2 - d6;
            int i1;
            int i2;
            if (d7 > d8) {
               i1 = 1;
               i2 = 0;
            } else {
               i1 = 0;
               i2 = 1;
            }

            double d9 = d7 - (double)i1 + g;
            double d10 = d8 - (double)i2 + g;
            double d11 = d7 - (double)1.0F + (double)2.0F * g;
            double d12 = d8 - (double)1.0F + (double)2.0F * g;
            int i3 = m & 255;
            int i4 = n & 255;
            int i5 = this.e[i3 + this.e[i4]] % 12;
            int i6 = this.e[i3 + i1 + this.e[i4 + i2]] % 12;
            int i7 = this.e[i3 + 1 + this.e[i4 + 1]] % 12;
            double d13 = (double)0.5F - d7 * d7 - d8 * d8;
            double d14;
            if (d13 < (double)0.0F) {
               d14 = (double)0.0F;
            } else {
               d13 *= d13;
               d14 = d13 * d13 * a(d[i5], d7, d8);
            }

            double d15 = (double)0.5F - d9 * d9 - d10 * d10;
            double d16;
            if (d15 < (double)0.0F) {
               d16 = (double)0.0F;
            } else {
               d15 *= d15;
               d16 = d15 * d15 * a(d[i6], d9, d10);
            }

            double d17 = (double)0.5F - d11 * d11 - d12 * d12;
            double d18;
            if (d17 < (double)0.0F) {
               d18 = (double)0.0F;
            } else {
               d17 *= d17;
               d18 = d17 * d17 * a(d[i7], d11, d12);
            }

            int var10001 = i++;
            paramArrayOfDouble[var10001] += (double)70.0F * (d14 + d16 + d18) * paramDouble5;
         }
      }

   }
}
