package com.khorn.terraincontrol.generator.resourcegens;

import com.khorn.terraincontrol.LocalWorld;
import com.khorn.terraincontrol.exception.InvalidConfigException;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class UnderWaterOreGen extends Resource {
   private List sourceBlocks;
   private int size;

   public UnderWaterOreGen() {
      super();
   }

   public void spawn(LocalWorld world, Random rand, boolean villageInChunk, int x, int z) {
      int firstSolidBlock = world.getSolidHeight(x, z) - 1;
      if (world.getLiquidHeight(x, z) >= firstSolidBlock && firstSolidBlock != -1) {
         int currentSize = rand.nextInt(this.size);
         int two = 2;

         for(int currentX = x - currentSize; currentX <= x + currentSize; ++currentX) {
            for(int currentZ = z - currentSize; currentZ <= z + currentSize; ++currentZ) {
               int deltaX = currentX - x;
               int deltaZ = currentZ - z;
               if (deltaX * deltaX + deltaZ * deltaZ <= currentSize * currentSize) {
                  for(int y = firstSolidBlock - two; y <= firstSolidBlock + two; ++y) {
                     int i3 = world.getTypeId(currentX, y, currentZ);
                     if (this.sourceBlocks.contains(i3)) {
                        world.setBlock(currentX, y, currentZ, this.blockId, this.blockData, false, false, false);
                     }
                  }
               }
            }
         }

      }
   }

   public void load(List args) throws InvalidConfigException {
      this.assureSize(5, args);
      this.blockId = this.readBlockId((String)args.get(0));
      this.blockData = this.readBlockData((String)args.get(0));
      this.size = this.readInt((String)args.get(1), 1, 8);
      this.frequency = this.readInt((String)args.get(2), 1, 100);
      this.rarity = this.readRarity((String)args.get(3));
      this.sourceBlocks = new ArrayList();

      for(int i = 4; i < args.size(); ++i) {
         this.sourceBlocks.add(this.readBlockId((String)args.get(i)));
      }

   }

   public String makeString() {
      return "UnderWaterOre(" + this.makeMaterial(this.blockId, this.blockData) + "," + this.size + "," + this.frequency + "," + this.rarity + this.makeMaterial(this.sourceBlocks) + ")";
   }
}
