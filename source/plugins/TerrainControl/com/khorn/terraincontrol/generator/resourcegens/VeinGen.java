package com.khorn.terraincontrol.generator.resourcegens;

import com.khorn.terraincontrol.LocalWorld;
import com.khorn.terraincontrol.TerrainControl;
import com.khorn.terraincontrol.exception.InvalidConfigException;
import com.khorn.terraincontrol.util.MathHelper;
import com.khorn.terraincontrol.util.RandomHelper;
import java.util.List;
import java.util.Random;

public class VeinGen extends Resource {
   public double veinRarity;
   public int minRadius;
   public int maxRadius;
   public int oreSize;
   public int oreFrequency;
   public int oreRarity;
   public int minAltitude;
   public int maxAltitude;
   public List sourceBlocks;

   public VeinGen() {
      super();
   }

   public void spawn(LocalWorld world, Random random, boolean villageInChunk, int x, int z) {
   }

   protected void spawnInChunk(LocalWorld world, Random random, boolean villageInChunk, int currentChunkX, int currentChunkZ) {
      int searchRadius = (this.maxRadius + 15) / 16;

      for(int searchChunkX = currentChunkX - searchRadius; searchChunkX < currentChunkX + searchRadius; ++searchChunkX) {
         for(int searchChunkZ = currentChunkZ - searchRadius; searchChunkZ < currentChunkZ + searchRadius; ++searchChunkZ) {
            Vein vein = this.getVeinStartInChunk(world, searchChunkX, searchChunkZ);
            if (vein != null && vein.reachesChunk(currentChunkX, currentChunkZ)) {
               vein.spawn(world, random, currentChunkX, currentChunkZ, this);
            }
         }
      }

   }

   public void load(List args) throws InvalidConfigException {
      this.assureSize(9, args);
      this.blockId = this.readBlockId((String)args.get(0));
      this.blockData = this.readBlockData((String)args.get(0));
      this.minRadius = this.readInt((String)args.get(1), 10, 200);
      this.maxRadius = this.readInt((String)args.get(2), this.minRadius, 201);
      this.veinRarity = this.readDouble((String)args.get(3), 1.0E-7, (double)100.0F);
      this.oreSize = this.readInt((String)args.get(4), 1, 64);
      this.oreFrequency = this.readInt((String)args.get(5), 1, 100);
      this.oreRarity = this.readInt((String)args.get(6), 1, 100);
      this.minAltitude = this.readInt((String)args.get(7), TerrainControl.worldDepth, TerrainControl.worldHeight - 1);
      this.maxAltitude = this.readInt((String)args.get(8), this.minAltitude + 1, TerrainControl.worldHeight);
      this.sourceBlocks = this.readBlockIds(args, 9);
   }

   public String makeString() {
      String result = "Vein(" + this.makeMaterial(this.blockId, this.blockData) + "," + this.minRadius + "," + this.maxRadius + "," + this.veinRarity + ",";
      result = result + this.oreSize + "," + this.oreFrequency + "," + this.oreRarity + "," + this.minAltitude + "," + this.maxAltitude + this.makeMaterial(this.sourceBlocks) + ")";
      return result;
   }

   public Vein getVeinStartInChunk(LocalWorld world, int chunkX, int chunkZ) {
      Random random = RandomHelper.getRandomForCoords(chunkX, chunkZ, (long)((this.blockId * 16 + this.blockData) * (this.minRadius + this.maxRadius + 100)) + world.getSeed());
      if (random.nextDouble() * (double)100.0F < this.veinRarity) {
         int veinX = chunkX * 16 + random.nextInt(16) + 8;
         int veinY = MathHelper.getRandomNumberInRange(random, this.minAltitude, this.maxAltitude);
         int veinZ = chunkZ * 16 + random.nextInt(16) + 8;
         int veinSize = MathHelper.getRandomNumberInRange(random, this.minRadius, this.maxRadius);
         return new Vein(veinX, veinY, veinZ, veinSize);
      } else {
         return null;
      }
   }
}
