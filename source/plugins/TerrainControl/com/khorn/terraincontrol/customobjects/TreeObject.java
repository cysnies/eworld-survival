package com.khorn.terraincontrol.customobjects;

import com.khorn.terraincontrol.LocalBiome;
import com.khorn.terraincontrol.LocalWorld;
import com.khorn.terraincontrol.TerrainControl;
import com.khorn.terraincontrol.generator.resourcegens.TreeType;
import java.util.Map;
import java.util.Random;

public class TreeObject implements CustomObject {
   private TreeType type;
   private int minHeight;
   private int maxHeight;

   public TreeObject(TreeType type) {
      super();
      this.minHeight = TerrainControl.worldDepth;
      this.maxHeight = TerrainControl.worldHeight;
      this.type = type;
   }

   public void onEnable(Map otherObjectsInDirectory) {
   }

   public TreeObject(TreeType type, Map settings) {
      super();
      this.minHeight = TerrainControl.worldDepth;
      this.maxHeight = TerrainControl.worldHeight;
      this.type = type;

      for(Map.Entry entry : settings.entrySet()) {
         try {
            if (((String)entry.getKey()).equalsIgnoreCase("MinHeight")) {
               this.minHeight = Math.max(TerrainControl.worldDepth, Math.min(Integer.parseInt((String)entry.getValue()), TerrainControl.worldHeight));
            }

            if (((String)entry.getKey()).equalsIgnoreCase("MaxHeight")) {
               this.maxHeight = Math.max(this.minHeight, Math.min(Integer.parseInt((String)entry.getValue()), TerrainControl.worldHeight));
            }
         } catch (NumberFormatException var6) {
            TerrainControl.log("Cannot parse " + (String)entry.getKey() + " of a " + type + " tree: invalid number!");
         }
      }

   }

   public String getName() {
      return this.type.name();
   }

   public boolean canSpawnAsTree() {
      return true;
   }

   public boolean canSpawnAsObject() {
      return false;
   }

   public boolean spawnForced(LocalWorld world, Random random, Rotation rotation, int x, int y, int z) {
      return world.PlaceTree(this.type, random, x, y, z);
   }

   public boolean spawnAsTree(LocalWorld world, Random random, int x, int z) {
      int y = world.getHighestBlockYAt(x, z);
      return y >= this.minHeight && y <= this.maxHeight ? world.PlaceTree(this.type, random, x, y, z) : false;
   }

   public boolean process(LocalWorld world, Random random, int chunkX, int chunkZ) {
      int x = chunkX * 16 + random.nextInt(16);
      int z = chunkZ * 16 + random.nextInt(16);
      return this.spawnAsTree(world, random, x, z);
   }

   public CustomObject applySettings(Map settings) {
      return new TreeObject(this.type, settings);
   }

   public boolean hasPreferenceToSpawnIn(LocalBiome biome) {
      return true;
   }

   public boolean canSpawnAt(LocalWorld world, Rotation rotation, int x, int y, int z) {
      return true;
   }

   public boolean canRotateRandomly() {
      return false;
   }
}
