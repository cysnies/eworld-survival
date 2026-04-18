package net.citizensnpcs.util;

import java.util.Random;
import java.util.concurrent.locks.ReentrantLock;

public class XORShiftRNG extends Random {
   private final ReentrantLock lock = new ReentrantLock();
   private final byte[] seed = new byte[20];
   private int state1;
   private int state2;
   private int state3;
   private int state4;
   private int state5;
   private static final int BITWISE_BYTE_TO_INT = 255;
   private static Random SEED_GENERATOR = new Random();
   private static final int SEED_SIZE_BYTES = 20;
   private static final long serialVersionUID = -1843001897066722618L;

   public XORShiftRNG() {
      super();
      SEED_GENERATOR.nextBytes(this.seed);
      int[] state = convertBytesToInts(this.seed);
      this.state1 = state[0];
      this.state2 = state[1];
      this.state3 = state[2];
      this.state4 = state[3];
      this.state5 = state[4];
   }

   public byte[] getSeed() {
      return (byte[])this.seed.clone();
   }

   protected int next(int bits) {
      int var4;
      try {
         this.lock.lock();
         int t = this.state1 ^ this.state1 >> 7;
         this.state1 = this.state2;
         this.state2 = this.state3;
         this.state3 = this.state4;
         this.state4 = this.state5;
         this.state5 = this.state5 ^ this.state5 << 6 ^ t ^ t << 13;
         int value = (this.state2 + this.state2 + 1) * this.state5;
         var4 = value >>> 32 - bits;
      } finally {
         this.lock.unlock();
      }

      return var4;
   }

   public static int convertBytesToInt(byte[] bytes, int offset) {
      return 255 & bytes[offset + 3] | (255 & bytes[offset + 2]) << 8 | (255 & bytes[offset + 1]) << 16 | (255 & bytes[offset]) << 24;
   }

   public static int[] convertBytesToInts(byte[] bytes) {
      if (bytes.length % 4 != 0) {
         throw new IllegalArgumentException("Number of input bytes must be a multiple of 4.");
      } else {
         int[] ints = new int[bytes.length / 4];

         for(int i = 0; i < ints.length; ++i) {
            ints[i] = convertBytesToInt(bytes, i * 4);
         }

         return ints;
      }
   }
}
