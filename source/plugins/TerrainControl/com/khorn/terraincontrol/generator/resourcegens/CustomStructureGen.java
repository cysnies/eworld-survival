package com.khorn.terraincontrol.generator.resourcegens;

import com.khorn.terraincontrol.LocalWorld;
import com.khorn.terraincontrol.TerrainControl;
import com.khorn.terraincontrol.configuration.BiomeConfig;
import com.khorn.terraincontrol.customobjects.CustomObject;
import com.khorn.terraincontrol.customobjects.CustomObjectCoordinate;
import com.khorn.terraincontrol.customobjects.CustomObjectStructure;
import com.khorn.terraincontrol.customobjects.Rotation;
import com.khorn.terraincontrol.customobjects.StructuredCustomObject;
import com.khorn.terraincontrol.exception.InvalidConfigException;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class CustomStructureGen extends Resource {
   private List objects;
   private List objectChances;
   private List objectNames;

   public CustomStructureGen() {
      super();
   }

   public void load(List args) throws InvalidConfigException {
      this.objects = new ArrayList();
      this.objectNames = new ArrayList();
      this.objectChances = new ArrayList();
      int i = 0;

      while(i < args.size() - 1) {
         CustomObject object = TerrainControl.getCustomObjectManager().getObjectFromString((String)args.get(i), ((BiomeConfig)this.getHolder()).worldConfig);
         if (object != null && object.canSpawnAsObject()) {
            if (object instanceof StructuredCustomObject && ((StructuredCustomObject)object).getBranches(Rotation.NORTH).length != 0) {
               this.objects.add((StructuredCustomObject)object);
               this.objectNames.add(args.get(i));
               this.objectChances.add(this.readRarity((String)args.get(i + 1)));
               i += 2;
               continue;
            }

            throw new InvalidConfigException("The object " + (String)args.get(i) + " isn't a structure");
         }

         throw new InvalidConfigException("No custom object found with the name " + (String)args.get(i));
      }

      if (((BiomeConfig)this.getHolder()).structureGen != null) {
         throw new InvalidConfigException("There can only be one CustomStructure resource in each BiomeConfig");
      } else {
         ((BiomeConfig)this.getHolder()).structureGen = this;
      }
   }

   public void spawn(LocalWorld world, Random random, boolean villageInChunk, int x, int z) {
   }

   protected void spawnInChunk(LocalWorld world, Random random, boolean villageInChunk, int currentChunkX, int currentChunkZ) {
      int searchRadius = 5;

      for(int searchChunkX = currentChunkX - searchRadius; searchChunkX < currentChunkX + searchRadius; ++searchChunkX) {
         for(int searchChunkZ = currentChunkZ - searchRadius; searchChunkZ < currentChunkZ + searchRadius; ++searchChunkZ) {
            CustomObjectStructure structureStart = world.getStructureCache().getStructureStart(searchChunkX, searchChunkZ);
            if (structureStart != null) {
               structureStart.spawnForChunk(currentChunkX, currentChunkZ);
            }
         }
      }

   }

   public String makeString() {
      if (this.objects.size() == 0) {
         return "CustomStructure()";
      } else {
         String output = "CustomStructure(" + (String)this.objectNames.get(0) + "," + this.objectChances.get(0);

         for(int i = 1; i < this.objectNames.size(); ++i) {
            output = output + "," + (String)this.objectNames.get(i) + "," + this.objectChances.get(i);
         }

         return output + ")";
      }
   }

   public CustomObjectCoordinate getRandomObjectCoordinate(Random random, int chunkX, int chunkZ) {
      if (this.objects.size() == 0) {
         return null;
      } else {
         for(int objectNumber = 0; objectNumber < this.objects.size(); ++objectNumber) {
            if (random.nextDouble() * (double)100.0F < (Double)this.objectChances.get(objectNumber)) {
               return ((StructuredCustomObject)this.objects.get(objectNumber)).makeCustomObjectCoordinate(random, chunkX, chunkZ);
            }
         }

         return null;
      }
   }
}
