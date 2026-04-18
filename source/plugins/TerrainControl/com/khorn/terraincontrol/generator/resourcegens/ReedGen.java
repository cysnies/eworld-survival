package com.khorn.terraincontrol.generator.resourcegens;

import com.khorn.terraincontrol.LocalWorld;
import com.khorn.terraincontrol.TerrainControl;
import com.khorn.terraincontrol.exception.InvalidConfigException;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class ReedGen extends Resource {
   private int minAltitude;
   private int maxAltitude;
   private List sourceBlocks;

   public ReedGen() {
      super();
   }

   public void spawn(LocalWorld world, Random rand, boolean villageInChunk, int x, int z) {
      int y = world.getHighestBlockYAt(x, z);
      if (y <= this.maxAltitude && y >= this.minAltitude && (world.getMaterial(x - 1, y - 1, z).isLiquid() || world.getMaterial(x + 1, y - 1, z).isLiquid() || world.getMaterial(x, y - 1, z - 1).isLiquid() || world.getMaterial(x, y - 1, z + 1).isLiquid())) {
         if (this.sourceBlocks.contains(world.getTypeId(x, y - 1, z))) {
            int n = 1 + rand.nextInt(2);

            for(int i1 = 0; i1 < n; ++i1) {
               world.setBlock(x, y + i1, z, this.blockId, this.blockData, false, false, false);
            }

         }
      }
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

   public String makeString() {
      return "Reed(" + this.makeMaterial(this.blockId, this.blockData) + "," + this.frequency + "," + this.rarity + "," + this.minAltitude + "," + this.maxAltitude + this.makeMaterial(this.sourceBlocks) + ")";
   }
}
