package com.khorn.terraincontrol.generator.resourcegens;

import com.khorn.terraincontrol.LocalWorld;
import com.khorn.terraincontrol.TerrainControl;
import com.khorn.terraincontrol.exception.InvalidConfigException;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class LiquidGen extends Resource {
   private List sourceBlocks;
   private int minAltitude;
   private int maxAltitude;

   public LiquidGen() {
      super();
   }

   public void spawn(LocalWorld world, Random rand, boolean villageInChunk, int x, int z) {
      int y = rand.nextInt(this.maxAltitude - this.minAltitude) + this.minAltitude;
      if (!this.sourceBlocks.contains(world.getTypeId(x, y + 1, z))) {
         if (!this.sourceBlocks.contains(world.getTypeId(x, y - 1, z))) {
            if (world.getTypeId(x, y, z) == 0 || !this.sourceBlocks.contains(world.getTypeId(x, y, z))) {
               int i = 0;
               int j = 0;
               int tempBlock = world.getTypeId(x - 1, y, z);
               i = this.sourceBlocks.contains(tempBlock) ? i + 1 : i;
               j = tempBlock == 0 ? j + 1 : j;
               tempBlock = world.getTypeId(x + 1, y, z);
               i = this.sourceBlocks.contains(tempBlock) ? i + 1 : i;
               j = tempBlock == 0 ? j + 1 : j;
               tempBlock = world.getTypeId(x, y, z - 1);
               i = this.sourceBlocks.contains(tempBlock) ? i + 1 : i;
               j = tempBlock == 0 ? j + 1 : j;
               tempBlock = world.getTypeId(x, y, z + 1);
               i = this.sourceBlocks.contains(tempBlock) ? i + 1 : i;
               j = tempBlock == 0 ? j + 1 : j;
               if (i == 3 && j == 1) {
                  world.setBlock(x, y, z, this.blockId, 0, true, true, true);
               }

            }
         }
      }
   }

   public void load(List args) throws InvalidConfigException {
      this.assureSize(6, args);
      this.blockId = this.readBlockId((String)args.get(0));
      this.blockData = this.readBlockData((String)args.get(0));
      this.frequency = this.readInt((String)args.get(1), 1, 5000);
      this.rarity = this.readRarity((String)args.get(2));
      this.minAltitude = this.readInt((String)args.get(3), TerrainControl.worldDepth, TerrainControl.worldHeight);
      this.maxAltitude = this.readInt((String)args.get(4), this.minAltitude + 1, TerrainControl.worldHeight);
      this.sourceBlocks = new ArrayList();

      for(int i = 5; i < args.size(); ++i) {
         this.sourceBlocks.add(this.readBlockId((String)args.get(i)));
      }

   }

   public String makeString() {
      return "Liquid(" + this.makeMaterial(this.blockId, this.blockData) + "," + this.frequency + "," + this.rarity + "," + this.minAltitude + "," + this.maxAltitude + this.makeMaterial(this.sourceBlocks) + ")";
   }
}
