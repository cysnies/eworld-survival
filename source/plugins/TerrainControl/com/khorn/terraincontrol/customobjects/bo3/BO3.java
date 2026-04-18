package com.khorn.terraincontrol.customobjects.bo3;

import com.khorn.terraincontrol.LocalBiome;
import com.khorn.terraincontrol.LocalWorld;
import com.khorn.terraincontrol.TerrainControl;
import com.khorn.terraincontrol.customobjects.Branch;
import com.khorn.terraincontrol.customobjects.CustomObject;
import com.khorn.terraincontrol.customobjects.CustomObjectCoordinate;
import com.khorn.terraincontrol.customobjects.Rotation;
import com.khorn.terraincontrol.customobjects.StructuredCustomObject;
import com.khorn.terraincontrol.util.MathHelper;
import java.io.File;
import java.util.Map;
import java.util.Random;

public class BO3 implements StructuredCustomObject {
   private BO3Config settings;
   private String name;
   private File file;

   public BO3(String name, File file) {
      super();
      this.name = name;
      this.file = file;
   }

   public void onEnable(Map otherObjectsInDirectory) {
      this.settings = new BO3Config(this.name, this.file, otherObjectsInDirectory);
   }

   public BO3(BO3 oldObject, Map extraSettings) {
      super();
      this.settings = new BO3Config(oldObject, extraSettings);
      this.name = this.settings.name;
      this.file = this.settings.file;
   }

   public String getName() {
      return this.name;
   }

   public BO3Config getSettings() {
      return this.settings;
   }

   public boolean canSpawnAsTree() {
      return this.settings.tree;
   }

   public boolean canSpawnAsObject() {
      return true;
   }

   public boolean canSpawnAt(LocalWorld world, Rotation rotation, int x, int y, int z) {
      BlockFunction[] blocks = this.settings.blocks[rotation.getRotationId()];
      BO3Check[] checks = this.settings.bo3Checks[rotation.getRotationId()];

      for(BO3Check check : checks) {
         if (check.preventsSpawn(world, x + check.x, y + check.y, z + check.z)) {
            return false;
         }
      }

      int blocksOutsideSourceBlock = 0;

      for(BlockFunction block : blocks) {
         if (!world.isLoaded(x + block.x, y + block.y, z + block.z)) {
            return false;
         }

         if (world.getTypeId(x + block.x, y + block.y, z + block.z) != this.settings.sourceBlock) {
            ++blocksOutsideSourceBlock;
         }
      }

      if ((double)blocksOutsideSourceBlock / (double)blocks.length * (double)100.0F > (double)this.settings.maxPercentageOutsideSourceBlock) {
         return false;
      } else if (!TerrainControl.fireCanCustomObjectSpawnEvent(this, world, x, y, z)) {
         return false;
      } else {
         return true;
      }
   }

   public boolean canRotateRandomly() {
      return this.settings.rotateRandomly;
   }

   public boolean spawnForced(LocalWorld world, Random random, Rotation rotation, int x, int y, int z) {
      BlockFunction[] blocks = this.settings.blocks[rotation.getRotationId()];

      for(BlockFunction block : blocks) {
         int previousBlock = world.getTypeId(x + block.x, y + block.y, z + block.z);
         if (previousBlock == this.settings.sourceBlock || this.settings.outsideSourceBlock == BO3Settings.OutsideSourceBlock.placeAnyway) {
            block.spawn(world, random, x + block.x, y + block.y, z + block.z);
         }
      }

      return true;
   }

   protected boolean spawn(LocalWorld world, Random random, int x, int z) {
      Rotation rotation = this.settings.rotateRandomly ? Rotation.getRandomRotation(random) : Rotation.NORTH;
      int y = 0;
      if (this.settings.spawnHeight == BO3Settings.SpawnHeightSetting.randomY) {
         y = MathHelper.getRandomNumberInRange(random, this.settings.minHeight, this.settings.maxHeight);
      }

      if (this.settings.spawnHeight == BO3Settings.SpawnHeightSetting.highestBlock) {
         y = world.getHighestBlockYAt(x, z);
         if (y < this.settings.minHeight || y > this.settings.maxHeight) {
            return false;
         }
      }

      if (this.settings.spawnHeight == BO3Settings.SpawnHeightSetting.highestSolidBlock) {
         y = world.getSolidHeight(x, z);
         if (y < this.settings.minHeight || y > this.settings.maxHeight) {
            return false;
         }
      }

      if (!this.canSpawnAt(world, rotation, x, y, z)) {
         return false;
      } else {
         return this.spawnForced(world, random, rotation, x, y, z);
      }
   }

   public boolean spawnAsTree(LocalWorld world, Random random, int x, int z) {
      return this.settings.tree ? this.spawn(world, random, x, z) : false;
   }

   public boolean process(LocalWorld world, Random random, int chunkX, int chunkZ) {
      boolean atLeastOneObjectHasSpawned = false;
      int chunkMiddleX = chunkX * 16 + 8;
      int chunkMiddleZ = chunkZ * 16 + 8;

      for(int i = 0; i < this.settings.frequency; ++i) {
         if (this.settings.rarity > random.nextDouble() * (double)100.0F && this.spawn(world, random, chunkMiddleX + random.nextInt(16), chunkMiddleZ + random.nextInt(16))) {
            atLeastOneObjectHasSpawned = true;
         }
      }

      return atLeastOneObjectHasSpawned;
   }

   public CustomObject applySettings(Map extraSettings) {
      return new BO3(this, extraSettings);
   }

   public boolean hasPreferenceToSpawnIn(LocalBiome biome) {
      return !this.settings.excludedBiomes.contains("All") && !this.settings.excludedBiomes.contains("all") && !this.settings.excludedBiomes.contains(biome.getName());
   }

   public boolean hasBranches() {
      return this.settings.branches[0].length != 0;
   }

   public Branch[] getBranches(Rotation rotation) {
      return this.settings.branches[rotation.getRotationId()];
   }

   public CustomObjectCoordinate makeCustomObjectCoordinate(Random random, int chunkX, int chunkZ) {
      if (this.settings.rarity > random.nextDouble() * (double)100.0F) {
         Rotation rotation = this.settings.rotateRandomly ? Rotation.getRandomRotation(random) : Rotation.NORTH;
         int height = MathHelper.getRandomNumberInRange(random, this.settings.minHeight, this.settings.maxHeight);
         return new CustomObjectCoordinate(this, rotation, chunkX * 16 + 8 + random.nextInt(16), height, chunkZ * 16 + 8 + random.nextInt(16));
      } else {
         return null;
      }
   }

   public int getMaxBranchDepth() {
      return this.settings.maxBranchDepth;
   }

   public CustomObjectCoordinate.SpawnHeight getSpawnHeight() {
      return this.settings.spawnHeight.toSpawnHeight();
   }
}
