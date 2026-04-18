package com.khorn.terraincontrol.generator.resourcegens;

import com.khorn.terraincontrol.LocalWorld;
import com.khorn.terraincontrol.TerrainControl;
import com.khorn.terraincontrol.exception.InvalidConfigException;
import com.khorn.terraincontrol.util.MathHelper;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class OreGen extends Resource {
   private int minAltitude;
   private int maxAltitude;
   private int maxSize;
   private List sourceBlocks;

   public OreGen() {
      super();
   }

   public void spawn(LocalWorld world, Random rand, boolean villageInChunk, int x, int z) {
      int y = rand.nextInt(this.maxAltitude - this.minAltitude) + this.minAltitude;
      float f = rand.nextFloat() * 3.141593F;
      double d1 = (double)((float)(x + 8) + MathHelper.sin(f) * (float)this.maxSize / 8.0F);
      double d2 = (double)((float)(x + 8) - MathHelper.sin(f) * (float)this.maxSize / 8.0F);
      double d3 = (double)((float)(z + 8) + MathHelper.cos(f) * (float)this.maxSize / 8.0F);
      double d4 = (double)((float)(z + 8) - MathHelper.cos(f) * (float)this.maxSize / 8.0F);
      double d5 = (double)(y + rand.nextInt(3) - 2);
      double d6 = (double)(y + rand.nextInt(3) - 2);

      for(int i = 0; i <= this.maxSize; ++i) {
         double d7 = d1 + (d2 - d1) * (double)i / (double)this.maxSize;
         double d8 = d5 + (d6 - d5) * (double)i / (double)this.maxSize;
         double d9 = d3 + (d4 - d3) * (double)i / (double)this.maxSize;
         double d10 = rand.nextDouble() * (double)this.maxSize / (double)16.0F;
         double d11 = (double)(MathHelper.sin((float)i * 3.141593F / (float)this.maxSize) + 1.0F) * d10 + (double)1.0F;
         double d12 = (double)(MathHelper.sin((float)i * 3.141593F / (float)this.maxSize) + 1.0F) * d10 + (double)1.0F;
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
                        if (d13 * d13 + d14 * d14 + d15 * d15 < (double)1.0F && this.sourceBlocks.contains(world.getTypeId(i3, i4, i5))) {
                           world.setBlock(i3, i4, i5, this.blockId, this.blockData, false, false, false);
                        }
                     }
                  }
               }
            }
         }
      }

   }

   public void load(List args) throws InvalidConfigException {
      this.assureSize(7, args);
      this.blockId = this.readBlockId((String)args.get(0));
      this.blockData = this.readBlockData((String)args.get(0));
      this.maxSize = this.readInt((String)args.get(1), 1, 128);
      this.frequency = this.readInt((String)args.get(2), 1, 100);
      this.rarity = this.readRarity((String)args.get(3));
      this.minAltitude = this.readInt((String)args.get(4), TerrainControl.worldDepth, TerrainControl.worldHeight);
      this.maxAltitude = this.readInt((String)args.get(5), this.minAltitude + 1, TerrainControl.worldHeight);
      this.sourceBlocks = new ArrayList();

      for(int i = 6; i < args.size(); ++i) {
         this.sourceBlocks.add(this.readBlockId((String)args.get(i)));
      }

   }

   public String makeString() {
      return "Ore(" + this.makeMaterial(this.blockId, this.blockData) + "," + this.maxSize + "," + this.frequency + "," + this.rarity + "," + this.minAltitude + "," + this.maxAltitude + this.makeMaterial(this.sourceBlocks) + ")";
   }
}
