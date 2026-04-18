package com.khorn.terraincontrol.util;

import java.util.Random;

public class RandomHelper {
   public RandomHelper() {
      super();
   }

   public static Random getRandomForCoords(int x, int z, long seed) {
      Random random = new Random();
      random.setSeed(seed);
      long l1 = random.nextLong() + 1L;
      long l2 = random.nextLong() + 1L;
      random.setSeed((long)x * l1 + (long)z * l2 ^ seed);
      return random;
   }

   public static Random getRandomForCoords(int x, int y, int z, long seed) {
      Random random = getRandomForCoords(x, z, seed);
      random.setSeed((long)(random.nextInt() * y));
      return random;
   }
}
