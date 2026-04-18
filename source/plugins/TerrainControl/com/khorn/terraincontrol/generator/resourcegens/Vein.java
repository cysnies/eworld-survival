package com.khorn.terraincontrol.generator.resourcegens;

import com.khorn.terraincontrol.LocalWorld;
import com.khorn.terraincontrol.util.MathHelper;
import java.util.List;
import java.util.Random;

class Vein {
   private int x;
   private int y;
   private int z;
   private int size;

   public Vein(int blockX, int blockY, int blockZ, int size) {
      super();
      this.x = blockX;
      this.y = blockY;
      this.z = blockZ;
      this.size = size;
   }

   public int getChunkSize() {
      return (this.size + 15) / 16;
   }

   public boolean reachesChunk(int otherChunkX, int otherChunkZ) {
      int chunkX = (this.x + 8) / 16;
      int chunkZ = (this.z + 8) / 16;
      int chunkSize = this.getChunkSize();
      return MathHelper.abs(otherChunkX - chunkX) <= chunkSize && MathHelper.abs(otherChunkZ - chunkZ) <= chunkSize;
   }

   public void spawn(LocalWorld world, Random random, int chunkX, int chunkZ, VeinGen gen) {
      int sizeSquared = this.size * this.size;

      for(int i = 0; i < gen.oreFrequency; ++i) {
         if (random.nextInt(100) < gen.oreRarity) {
            int oreX = chunkX * 16 + 8 + random.nextInt(16);
            int oreY = MathHelper.getRandomNumberInRange(random, gen.minAltitude, gen.maxAltitude);
            int oreZ = chunkZ * 16 + 8 + random.nextInt(16);
            if ((oreX - this.x) * (oreX - this.x) + (oreY - this.y) * (oreY - this.y) + (oreZ - this.z) * (oreZ - this.z) < sizeSquared) {
               this.spawnOre(world, random, oreX, oreY, oreZ, gen);
            }
         }
      }

   }

   protected void spawnOre(LocalWorld world, Random rand, int x, int y, int z, VeinGen gen) {
      int maxSize = gen.oreSize;
      int blockId = gen.blockId;
      int blockData = gen.blockData;
      List<Integer> sourceBlocks = gen.sourceBlocks;
      float f = rand.nextFloat() * 3.141593F;
      double d1 = (double)((float)(x + 8) + MathHelper.sin(f) * (float)maxSize / 8.0F);
      double d2 = (double)((float)(x + 8) - MathHelper.sin(f) * (float)maxSize / 8.0F);
      double d3 = (double)((float)(z + 8) + MathHelper.cos(f) * (float)maxSize / 8.0F);
      double d4 = (double)((float)(z + 8) - MathHelper.cos(f) * (float)maxSize / 8.0F);
      double d5 = (double)(y + rand.nextInt(3) - 2);
      double d6 = (double)(y + rand.nextInt(3) - 2);

      for(int i = 0; i <= maxSize; ++i) {
         double d7 = d1 + (d2 - d1) * (double)i / (double)maxSize;
         double d8 = d5 + (d6 - d5) * (double)i / (double)maxSize;
         double d9 = d3 + (d4 - d3) * (double)i / (double)maxSize;
         double d10 = rand.nextDouble() * (double)maxSize / (double)16.0F;
         double d11 = (double)(MathHelper.sin((float)i * 3.141593F / (float)maxSize) + 1.0F) * d10 + (double)1.0F;
         double d12 = (double)(MathHelper.sin((float)i * 3.141593F / (float)maxSize) + 1.0F) * d10 + (double)1.0F;
         int j = MathHelper.floor(d7 - d11 / (double)2.0F);
         int k = MathHelper.floor(d8 - d12 / (double)2.0F);
         int m = MathHelper.floor(d9 - d11 / (double)2.0F);
         int n = MathHelper.floor(d7 + d11 / (double)2.0F);
         int i1 = MathHelper.floor(d8 + d12 / (double)2.0F);
         int i2 = MathHelper.floor(d9 + d11 / (double)2.0F);

         for(int i3 = j; i3 <= n; ++i3) {
            double d13 = ((double)i3 + (double)0.5F - d7) / (d11 / (double)2.0F);
            if (d13 * d13 < (double)1.0F) {
               for(int i4 = k; i4 <= i1; ++i4) {
                  double d14 = ((double)i4 + (double)0.5F - d8) / (d12 / (double)2.0F);
                  if (d13 * d13 + d14 * d14 < (double)1.0F) {
                     for(int i5 = m; i5 <= i2; ++i5) {
                        double d15 = ((double)i5 + (double)0.5F - d9) / (d11 / (double)2.0F);
                        if (d13 * d13 + d14 * d14 + d15 * d15 < (double)1.0F && sourceBlocks.contains(world.getTypeId(i3, i4, i5))) {
                           world.setBlock(i3, i4, i5, blockId, blockData, false, false, false);
                        }
                     }
                  }
               }
            }
         }
      }

   }
}
