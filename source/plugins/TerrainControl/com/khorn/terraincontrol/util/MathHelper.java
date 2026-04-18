package com.khorn.terraincontrol.util;

import java.util.Random;

public class MathHelper {
   private static float[] a = new float[65536];

   public MathHelper() {
      super();
   }

   public static float sqrt(float paramFloat) {
      return (float)Math.sqrt((double)paramFloat);
   }

   public static float sin(float paramFloat) {
      return a[(int)(paramFloat * 10430.378F) & '\uffff'];
   }

   public static float cos(float paramFloat) {
      return a[(int)(paramFloat * 10430.378F + 16384.0F) & '\uffff'];
   }

   public static int floor(double d0) {
      int i = (int)d0;
      return d0 < (double)i ? i - 1 : i;
   }

   public static long floor_double_long(double d) {
      long l = (long)d;
      return d >= (double)l ? l : l - 1L;
   }

   public static int abs(int number) {
      return number > 0 ? number : -number;
   }

   public static int getRandomNumberInRange(Random random, int min, int max) {
      return min + random.nextInt(max - min + 1);
   }

   static {
      for(int i = 0; i < 65536; ++i) {
         a[i] = (float)Math.sin((double)i * Math.PI * (double)2.0F / (double)65536.0F);
      }

   }
}
