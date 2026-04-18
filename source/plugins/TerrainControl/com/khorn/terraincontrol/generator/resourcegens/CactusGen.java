package com.khorn.terraincontrol.generator.resourcegens;

import com.khorn.terraincontrol.LocalWorld;
import com.khorn.terraincontrol.TerrainControl;
import com.khorn.terraincontrol.exception.InvalidConfigException;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class CactusGen extends Resource {
   private int minAltitude;
   private int maxAltitude;
   private List sourceBlocks;

   public CactusGen() {
      super();
   }

   public void spawn(LocalWorld world, Random rand, boolean villageInChunk, int x, int z) {
      int y = rand.nextInt(this.maxAltitude - this.minAltitude) + this.minAltitude;

      for(int i = 0; i < 10; ++i) {
         int j = x + rand.nextInt(8) - rand.nextInt(8);
         int k = y + rand.nextInt(4) - rand.nextInt(4);
         int m = z + rand.nextInt(8) - rand.nextInt(8);
         if (world.isEmpty(j, k, m)) {
            int n = 1 + rand.nextInt(rand.nextInt(3) + 1);

            for(int i1 = 0; i1 < n; ++i1) {
               int id = world.getTypeId(j, k + i1 - 1, m);
               if (this.sourceBlocks.contains(id)) {
                  world.setBlock(j, k + i1, m, this.blockId, this.blockData, false, false, false);
               }
            }
         }
      }

   }

   public String makeString() {
      return "Cactus(" + this.makeMaterial(this.blockId, this.blockData) + "," + this.frequency + "," + this.rarity + "," + this.minAltitude + "," + this.maxAltitude + this.makeMaterial(this.sourceBlocks) + ")";
   }

   public void load(List args) throws InvalidConfigException {
      this.assureSize(6, args);
      this.blockId = this.readBlockId((String)args.get(0));
      this.blockData = this.readBlockData((String)args.get(0));
      this.frequency = this.readInt((String)args.get(1), 1, 100);
      this.rarity = this.readRarity((String)args.get(2));
      this.minAltitude = this.readInt((String)args.get(3), TerrainControl.worldDepth, TerrainControl.worldHeight);
      this.maxAltitude = this.readInt((String)args.get(4), this.minAltitude + 1, TerrainControl.worldHeight);
      this.sourceBlocks = new ArrayList();

      for(int i = 5; i < args.size(); ++i) {
         this.sourceBlocks.add(this.readBlockId((String)args.get(i)));
      }

   }
}
