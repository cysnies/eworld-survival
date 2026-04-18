package com.khorn.terraincontrol.generator.resourcegens;

import com.khorn.terraincontrol.LocalWorld;
import com.khorn.terraincontrol.TerrainControl;
import com.khorn.terraincontrol.configuration.BiomeConfig;
import com.khorn.terraincontrol.customobjects.CustomObject;
import com.khorn.terraincontrol.exception.InvalidConfigException;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class TreeGen extends Resource {
   private List trees;
   private List treeNames;
   private List treeChances;

   public TreeGen() {
      super();
   }

   protected void spawnInChunk(LocalWorld world, Random random, boolean villageInChunk, int chunkX, int chunkZ) {
      for(int i = 0; i < this.frequency; ++i) {
         for(int treeNumber = 0; treeNumber < this.trees.size(); ++treeNumber) {
            if (random.nextInt(100) < (Integer)this.treeChances.get(treeNumber)) {
               int x = chunkX * 16 + random.nextInt(16) + 8;
               int z = chunkZ * 16 + random.nextInt(16) + 8;
               if (((CustomObject)this.trees.get(treeNumber)).spawnAsTree(world, random, x, z)) {
                  break;
               }
            }
         }
      }

   }

   public void load(List args) throws InvalidConfigException {
      this.assureSize(3, args);
      this.frequency = this.readInt((String)args.get(0), 1, 100);
      this.trees = new ArrayList();
      this.treeNames = new ArrayList();
      this.treeChances = new ArrayList();

      for(int i = 1; i < args.size() - 1; i += 2) {
         CustomObject object = TerrainControl.getCustomObjectManager().getObjectFromString((String)args.get(i), ((BiomeConfig)this.getHolder()).worldConfig);
         if (object == null) {
            throw new InvalidConfigException("Custom object " + (String)args.get(i) + " not found!");
         }

         if (!object.canSpawnAsTree()) {
            throw new InvalidConfigException("Custom object " + (String)args.get(i) + " is not a tree!");
         }

         this.trees.add(object);
         this.treeNames.add(args.get(i));
         this.treeChances.add(this.readInt((String)args.get(i + 1), 1, 100));
      }

   }

   public String makeString() {
      String output = "Tree(" + this.frequency;

      for(int i = 0; i < this.treeNames.size(); ++i) {
         output = output + "," + (String)this.treeNames.get(i) + "," + this.treeChances.get(i);
      }

      return output + ")";
   }

   public void spawn(LocalWorld world, Random random, boolean villageInChunk, int x, int z) {
   }
}
