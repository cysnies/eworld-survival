package com.khorn.terraincontrol.customobjects;

import com.khorn.terraincontrol.LocalWorld;
import com.khorn.terraincontrol.util.ChunkCoordinate;
import com.khorn.terraincontrol.util.RandomHelper;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Random;
import java.util.Set;

public class CustomObjectStructure {
   protected final Random random;
   protected LocalWorld world;
   protected CustomObjectCoordinate start;
   protected CustomObjectCoordinate.SpawnHeight height;
   protected Map objectsToSpawn;
   protected int maxBranchDepth;

   public CustomObjectStructure(LocalWorld world, CustomObjectCoordinate start) {
      super();
      if (!(start.getObject() instanceof StructuredCustomObject)) {
         throw new IllegalArgumentException("Start object has to be a structure!");
      } else {
         this.world = world;
         this.start = start;
         this.height = start.getStructuredObject().getSpawnHeight();
         this.maxBranchDepth = start.getStructuredObject().getMaxBranchDepth();
         this.random = RandomHelper.getRandomForCoords(start.getX(), start.getY(), start.getZ(), world.getSeed());
         this.objectsToSpawn = new HashMap();
         this.addToChunk(start);
         this.addBranches(start, 1);
      }
   }

   protected void addBranches(CustomObjectCoordinate coordObject, int depth) {
      for(Branch branch : coordObject.getStructuredObject().getBranches(coordObject.getRotation())) {
         CustomObjectCoordinate childCoordObject = branch.toCustomObjectCoordinate(this.world, this.random, coordObject.getX(), coordObject.getY(), coordObject.getZ());
         if (childCoordObject != null) {
            this.addToChunk(childCoordObject);
            if (depth < this.maxBranchDepth) {
               this.addBranches(childCoordObject, depth + 1);
            }
         }
      }

   }

   public void addToChunk(CustomObjectCoordinate coordObject) {
      ChunkCoordinate chunkCoordinate = ChunkCoordinate.fromBlockCoords(coordObject.getX(), coordObject.getZ());
      Set<CustomObjectCoordinate> objectsInChunk = (Set)this.objectsToSpawn.get(chunkCoordinate);
      if (objectsInChunk == null) {
         objectsInChunk = new HashSet();
      }

      objectsInChunk.add(coordObject);
      this.objectsToSpawn.put(chunkCoordinate, objectsInChunk);
   }

   public void spawnForChunk(int chunkX, int chunkZ) {
      ChunkCoordinate chunkCoordinate = ChunkCoordinate.fromChunkCoords(chunkX, chunkZ);
      Set<CustomObjectCoordinate> objectsInChunk = (Set)this.objectsToSpawn.get(chunkCoordinate);
      if (objectsInChunk != null) {
         for(CustomObjectCoordinate coordObject : objectsInChunk) {
            coordObject.spawnWithChecks(this.world, this.height, this.random);
         }
      }

   }
}
