package com.khorn.terraincontrol.generator.resourcegens;

import com.khorn.terraincontrol.LocalWorld;
import com.khorn.terraincontrol.TerrainControl;
import com.khorn.terraincontrol.exception.InvalidConfigException;
import java.util.List;
import java.util.Random;

public class WellGen extends Resource {
   private int minAltitude;
   private int maxAltitude;
   private int slabId;
   private int slabData;
   private int waterId;
   private int waterData;
   private List sourceBlocks;

   public WellGen() {
      super();
   }

   public void load(List args) throws InvalidConfigException {
      this.assureSize(8, args);
      this.blockId = this.readBlockId((String)args.get(0));
      this.blockData = this.readBlockData((String)args.get(0));
      this.slabId = this.readBlockId((String)args.get(1));
      this.slabData = this.readBlockData((String)args.get(1));
      this.waterId = this.readBlockId((String)args.get(2));
      this.waterData = this.readBlockData((String)args.get(2));
      this.frequency = this.readInt((String)args.get(3), 1, 100);
      this.rarity = this.readRarity((String)args.get(4));
      this.minAltitude = this.readInt((String)args.get(5), TerrainControl.worldDepth, TerrainControl.worldHeight);
      this.maxAltitude = this.readInt((String)args.get(6), this.minAltitude + 1, TerrainControl.worldHeight);
      this.sourceBlocks = this.readBlockIds(args, 7);
   }

   public void spawn(LocalWorld world, Random random, boolean villageInChunk, int x, int z) {
      int y;
      for(y = random.nextInt(this.maxAltitude - this.minAltitude) + this.minAltitude; world.isEmpty(x, y, z) && y > this.minAltitude; --y) {
      }

      int sourceBlock = world.getTypeId(x, y, z);
      if (this.sourceBlocks.contains(sourceBlock)) {
         for(int i = -2; i <= 2; ++i) {
            for(int j = -2; j <= 2; ++j) {
               if (world.isEmpty(x + i, y - 1, z + j) && world.isEmpty(x + i, y - 2, z + j)) {
                  return;
               }
            }
         }

         for(int var11 = -1; var11 <= 0; ++var11) {
            for(int j = -2; j <= 2; ++j) {
               for(int var9 = -2; var9 <= 2; ++var9) {
                  world.setBlock(x + j, y + var11, z + var9, this.blockId, this.blockData);
               }
            }
         }

         world.setBlock(x, y, z, this.waterId, this.waterData);
         world.setBlock(x - 1, y, z, this.waterId, this.waterData);
         world.setBlock(x + 1, y, z, this.waterId, this.waterData);
         world.setBlock(x, y, z - 1, this.waterId, this.waterData);
         world.setBlock(x, y, z + 1, this.waterId, this.waterData);

         for(int var12 = -2; var12 <= 2; ++var12) {
            for(int j = -2; j <= 2; ++j) {
               if (var12 == -2 || var12 == 2 || j == -2 || j == 2) {
                  world.setBlock(x + var12, y + 1, z + j, this.blockId, this.blockData);
               }
            }
         }

         world.setBlock(x + 2, y + 1, z, this.slabId, this.slabData);
         world.setBlock(x - 2, y + 1, z, this.slabId, this.slabData);
         world.setBlock(x, y + 1, z + 2, this.slabId, this.slabData);
         world.setBlock(x, y + 1, z - 2, this.slabId, this.slabData);

         for(int var13 = -1; var13 <= 1; ++var13) {
            for(int j = -1; j <= 1; ++j) {
               if (var13 == 0 && j == 0) {
                  world.setBlock(x + var13, y + 4, z + j, this.blockId, this.blockData);
               } else {
                  world.setBlock(x + var13, y + 4, z + j, this.slabId, this.slabData);
               }
            }
         }

         for(int var14 = 1; var14 <= 3; ++var14) {
            world.setBlock(x - 1, y + var14, z - 1, this.blockId, this.blockData);
            world.setBlock(x - 1, y + var14, z + 1, this.blockId, this.blockData);
            world.setBlock(x + 1, y + var14, z - 1, this.blockId, this.blockData);
            world.setBlock(x + 1, y + var14, z + 1, this.blockId, this.blockData);
         }

      }
   }

   public String makeString() {
      String output = "Well(" + this.makeMaterial(this.blockId, this.blockData) + "," + this.makeMaterial(this.slabId, this.slabData) + "," + this.makeMaterial(this.waterId, this.waterData) + ",";
      output = output + this.frequency + "," + this.rarity + "," + this.minAltitude + "," + this.maxAltitude + this.makeMaterial(this.sourceBlocks) + ")";
      return output;
   }
}
