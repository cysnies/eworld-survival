package com.khorn.terraincontrol.generator;

import com.khorn.terraincontrol.DefaultMaterial;
import com.khorn.terraincontrol.LocalWorld;
import com.khorn.terraincontrol.TerrainControl;
import com.khorn.terraincontrol.configuration.BiomeConfig;
import com.khorn.terraincontrol.configuration.TCDefaultValues;
import com.khorn.terraincontrol.configuration.WorldConfig;
import com.khorn.terraincontrol.generator.resourcegens.Resource;
import java.util.Random;

public class ObjectSpawner {
   private WorldConfig worldSettings;
   private Random rand;
   private LocalWorld world;

   public ObjectSpawner(WorldConfig wrk, LocalWorld localWorld) {
      super();
      this.worldSettings = wrk;
      this.rand = new Random();
      this.world = localWorld;
   }

   public void populate(int chunkX, int chunkZ) {
      int x = chunkX * 16;
      int z = chunkZ * 16;
      int biomeId = this.world.getBiomeId(x + 15, z + 15);
      BiomeConfig localBiomeConfig = this.worldSettings.biomeConfigs[biomeId];
      if (localBiomeConfig == null) {
         TerrainControl.log("Unknown biome id " + biomeId + " at " + (x + 15) + "," + (z + 15) + "  (chunk " + chunkX + "," + chunkZ + "). Population failed.");
      } else {
         long resourcesSeed = this.worldSettings.resourcesSeed != 0L ? this.worldSettings.resourcesSeed : this.world.getSeed();
         this.rand.setSeed(resourcesSeed);
         long l1 = this.rand.nextLong() / 2L * 2L + 1L;
         long l2 = this.rand.nextLong() / 2L * 2L + 1L;
         this.rand.setSeed((long)chunkX * l1 + (long)chunkZ * l2 ^ resourcesSeed);
         boolean hasGeneratedAVillage = this.world.PlaceTerrainObjects(this.rand, chunkX, chunkZ);
         TerrainControl.firePopulationStartEvent(this.world, this.rand, hasGeneratedAVillage, chunkX, chunkZ);

         for(int i = 0; i < localBiomeConfig.ResourceCount; ++i) {
            Resource res = localBiomeConfig.ResourceSequence[i];
            this.world.setChunksCreations(false);
            res.process(this.world, this.rand, hasGeneratedAVillage, chunkX, chunkZ);
         }

         this.world.placePopulationMobs(localBiomeConfig, this.rand, chunkX, chunkZ);
         this.placeSnowAndIce(chunkX, chunkZ);
         this.world.replaceBlocks();
         this.world.replaceBiomes();
         if (this.worldSettings.isDeprecated) {
            this.worldSettings = this.worldSettings.newSettings;
         }

         TerrainControl.firePopulationEndEvent(this.world, this.rand, hasGeneratedAVillage, chunkX, chunkZ);
      }
   }

   protected void placeSnowAndIce(int chunkX, int chunkZ) {
      int x = chunkX * 16 + 8;
      int z = chunkZ * 16 + 8;

      for(int i = 0; i < 16; ++i) {
         for(int j = 0; j < 16; ++j) {
            int blockToFreezeX = x + i;
            int blockToFreezeZ = z + j;
            BiomeConfig biomeConfig = this.worldSettings.biomeConfigs[this.world.getBiomeId(blockToFreezeX, blockToFreezeZ)];
            if (biomeConfig != null && biomeConfig.BiomeTemperature < TCDefaultValues.snowAndIceMaxTemp.floatValue()) {
               int blockToFreezeY = this.world.getHighestBlockYAt(blockToFreezeX, blockToFreezeZ);
               if (blockToFreezeY > 0) {
                  if (DefaultMaterial.getMaterial(this.world.getTypeId(blockToFreezeX, blockToFreezeY - 1, blockToFreezeZ)).isLiquid()) {
                     this.world.setBlock(blockToFreezeX, blockToFreezeY - 1, blockToFreezeZ, biomeConfig.iceBlock, 0);
                  } else if (this.world.getMaterial(blockToFreezeX, blockToFreezeY, blockToFreezeZ) == DefaultMaterial.AIR && this.world.getMaterial(blockToFreezeX, blockToFreezeY - 1, blockToFreezeZ).isSolid()) {
                     this.world.setBlock(blockToFreezeX, blockToFreezeY, blockToFreezeZ, DefaultMaterial.SNOW.id, 0);
                  }
               }
            }
         }
      }

   }
}
