package com.khorn.terraincontrol.customobjects;

import com.khorn.terraincontrol.LocalWorld;
import com.khorn.terraincontrol.generator.resourcegens.CustomStructureGen;
import com.khorn.terraincontrol.util.ChunkCoordinate;
import com.khorn.terraincontrol.util.RandomHelper;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

public class CustomObjectStructureCache {
   private Map structureCache;
   private LocalWorld world;

   public CustomObjectStructureCache(LocalWorld world) {
      super();
      this.world = world;
      this.structureCache = new HashMap();
   }

   public void reload(LocalWorld world) {
      this.world = world;
      this.structureCache.clear();
   }

   public CustomObjectStructure getStructureStart(int chunkX, int chunkZ) {
      ChunkCoordinate coord = ChunkCoordinate.fromChunkCoords(chunkX, chunkZ);
      CustomObjectStructure structureStart = (CustomObjectStructure)this.structureCache.get(coord);
      if (this.structureCache.size() > 400) {
         this.structureCache.clear();
      }

      if (structureStart != null) {
         return structureStart;
      } else {
         Random random = RandomHelper.getRandomForCoords(chunkX ^ 2, (chunkZ + 1) * 2, this.world.getSeed());
         CustomStructureGen structureGen = this.world.getSettings().biomeConfigs[this.world.getBiomeId(chunkX * 16 + 15, chunkZ * 16 + 15)].structureGen;
         if (structureGen != null) {
            CustomObjectCoordinate customObject = structureGen.getRandomObjectCoordinate(random, chunkX, chunkZ);
            if (customObject != null) {
               structureStart = new CustomObjectStructure(this.world, customObject);
               this.structureCache.put(coord, structureStart);
               return structureStart;
            }
         }

         return null;
      }
   }
}
