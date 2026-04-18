package com.khorn.terraincontrol.generator.resourcegens;

import com.khorn.terraincontrol.LocalWorld;
import com.khorn.terraincontrol.exception.InvalidConfigException;
import java.util.List;
import java.util.Random;

public class AboveWaterGen extends Resource {
   public AboveWaterGen() {
      super();
   }

   public void load(List args) throws InvalidConfigException {
      this.assureSize(3, args);
      this.blockId = this.readBlockId((String)args.get(0));
      this.blockData = this.readBlockData((String)args.get(0));
      this.frequency = this.readInt((String)args.get(1), 1, 100);
      this.rarity = this.readRarity((String)args.get(2));
   }

   public void spawn(LocalWorld world, Random rand, boolean villageInChunk, int x, int z) {
      int y = world.getLiquidHeight(x, z);
      if (y != -1) {
         ++y;

         for(int i = 0; i < 10; ++i) {
            int j = x + rand.nextInt(8) - rand.nextInt(8);
            int m = z + rand.nextInt(8) - rand.nextInt(8);
            if (world.isEmpty(j, y, m) && world.getMaterial(j, y - 1, m).isLiquid()) {
               world.setBlock(j, y, m, this.blockId, this.blockData, false, false, false);
            }
         }

      }
   }

   public String makeString() {
      return "AboveWaterRes(" + this.makeMaterial(this.blockId) + "," + this.frequency + "," + this.rarity + ")";
   }
}
