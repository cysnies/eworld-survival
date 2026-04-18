package com.khorn.terraincontrol.generator.resourcegens;

import com.khorn.terraincontrol.DefaultMaterial;
import com.khorn.terraincontrol.LocalWorld;
import com.khorn.terraincontrol.exception.InvalidConfigException;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class GrassGen extends Resource {
   private List sourceBlocks;

   public GrassGen() {
      super();
   }

   public void load(List args) throws InvalidConfigException {
      this.assureSize(5, args);
      this.blockId = this.readBlockId((String)args.get(0));
      this.blockData = this.readInt((String)args.get(1), 0, 16);
      this.frequency = this.readInt((String)args.get(2), 1, 500);
      this.rarity = this.readRarity((String)args.get(3));
      this.sourceBlocks = new ArrayList();

      for(int i = 4; i < args.size(); ++i) {
         this.sourceBlocks.add(this.readBlockId((String)args.get(i)));
      }

   }

   public void spawn(LocalWorld world, Random random, boolean villageInChunk, int x, int z) {
   }

   protected void spawnInChunk(LocalWorld world, Random random, boolean villageInChunk, int chunkX, int chunkZ) {
      for(int t = 0; t < this.frequency; ++t) {
         if (!((double)random.nextInt(100) >= this.rarity)) {
            int x = chunkX * 16 + random.nextInt(16) + 8;
            int z = chunkZ * 16 + random.nextInt(16) + 8;

            int y;
            int i;
            for(y = world.getHighestBlockYAt(x, z); ((i = world.getTypeId(x, y, z)) == 0 || i == DefaultMaterial.LEAVES.id) && y > 0; --y) {
            }

            if (world.isEmpty(x, y + 1, z) && this.sourceBlocks.contains(world.getTypeId(x, y, z))) {
               world.setBlock(x, y + 1, z, this.blockId, this.blockData, false, false, false);
            }
         }
      }

   }

   public String makeString() {
      return "Grass(" + this.makeMaterial(this.blockId) + "," + this.blockData + "," + this.frequency + "," + this.rarity + this.makeMaterial(this.sourceBlocks) + ")";
   }
}
