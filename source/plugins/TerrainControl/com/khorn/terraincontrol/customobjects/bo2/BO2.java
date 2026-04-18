package com.khorn.terraincontrol.customobjects.bo2;

import com.khorn.terraincontrol.DefaultMaterial;
import com.khorn.terraincontrol.LocalBiome;
import com.khorn.terraincontrol.LocalWorld;
import com.khorn.terraincontrol.TerrainControl;
import com.khorn.terraincontrol.configuration.ConfigFile;
import com.khorn.terraincontrol.customobjects.BODefaultValues;
import com.khorn.terraincontrol.customobjects.CustomObject;
import com.khorn.terraincontrol.customobjects.Rotation;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Random;

public class BO2 extends ConfigFile implements CustomObject {
   public ObjectCoordinate[][] data = new ObjectCoordinate[4][];
   public BO2[] groupObjects = null;
   public String name;
   public HashSet spawnInBiome;
   public String version;
   public HashSet spawnOnBlockType;
   public HashSet collisionBlockType;
   public boolean spawnWater;
   public boolean spawnLava;
   public boolean spawnAboveGround;
   public boolean spawnUnderGround;
   public boolean spawnSunlight;
   public boolean spawnDarkness;
   public boolean underFill;
   public boolean randomRotation;
   public boolean dig;
   public boolean tree;
   public boolean branch;
   public boolean diggingBranch;
   public boolean needsFoundation;
   public int rarity;
   public double collisionPercentage;
   public int spawnElevationMin;
   public int spawnElevationMax;
   public int groupFrequencyMin;
   public int groupFrequencyMax;
   public int groupSeparationMin;
   public int groupSeparationMax;
   public String groupId;
   public int branchLimit;

   public BO2(File file, String name) {
      super();
      this.readSettingsFile(file);
      this.name = name;
   }

   public BO2(Map settings, String name) {
      super();
      this.settingsCache = settings;
      this.name = name;
      this.readConfigSettings();
      this.correctSettings();
   }

   public void onEnable(Map otherObjectsInDirectory) {
      this.readConfigSettings();
      this.correctSettings();
   }

   public String getName() {
      return this.name;
   }

   public boolean canSpawnAsTree() {
      return this.tree;
   }

   public boolean canSpawnAsObject() {
      return true;
   }

   public boolean canRotateRandomly() {
      return this.randomRotation;
   }

   public boolean spawnForced(LocalWorld world, Random random, Rotation rotation, int x, int y, int z) {
      ObjectCoordinate[] data = this.data[rotation.getRotationId()];

      for(ObjectCoordinate point : data) {
         if (world.getTypeId(x + point.x, y + point.y, z + point.z) == 0) {
            world.setBlock(x + point.x, y + point.y, z + point.z, point.blockId, point.blockData, true, false, true);
         } else if (this.dig) {
            world.setBlock(x + point.x, y + point.y, z + point.z, point.blockId, point.blockData, true, false, true);
         }
      }

      return true;
   }

   public boolean canSpawnAt(LocalWorld world, Rotation rotation, int x, int y, int z) {
      if (world.getTypeId(x, y - 5, z) == 0 && this.needsFoundation) {
         return false;
      } else {
         int checkBlock = world.getTypeId(x, y + 2, z);
         if (this.spawnWater || checkBlock != DefaultMaterial.WATER.id && checkBlock != DefaultMaterial.STATIONARY_WATER.id) {
            if (this.spawnLava || checkBlock != DefaultMaterial.LAVA.id && checkBlock != DefaultMaterial.STATIONARY_LAVA.id) {
               checkBlock = world.getLightLevel(x, y + 2, z);
               if (!this.spawnSunlight && checkBlock > 8) {
                  return false;
               } else if (!this.spawnDarkness && checkBlock < 9) {
                  return false;
               } else if (y >= this.spawnElevationMin && y <= this.spawnElevationMax) {
                  if (!this.spawnOnBlockType.contains(world.getTypeId(x, y - 1, z))) {
                     return false;
                  } else {
                     ObjectCoordinate[] data = this.data[rotation.getRotationId()];
                     int faultCounter = 0;

                     for(ObjectCoordinate point : data) {
                        if (!world.isLoaded(x + point.x, y + point.y, z + point.z)) {
                           return false;
                        }

                        if (!this.dig && this.collisionBlockType.contains(world.getTypeId(x + point.x, y + point.y, z + point.z))) {
                           ++faultCounter;
                           if ((double)faultCounter > (double)data.length * (this.collisionPercentage / (double)100.0F)) {
                              return false;
                           }
                        }
                     }

                     if (!TerrainControl.fireCanCustomObjectSpawnEvent(this, world, x, y, z)) {
                        return false;
                     } else {
                        return true;
                     }
                  }
               } else {
                  return false;
               }
            } else {
               return false;
            }
         } else {
            return false;
         }
      }
   }

   protected boolean spawn(LocalWorld world, Random random, int x, int z) {
      int y;
      if (this.spawnAboveGround) {
         y = world.getSolidHeight(x, z);
      } else if (this.spawnUnderGround) {
         int solidHeight = world.getSolidHeight(x, z);
         if (solidHeight < 1 || solidHeight <= this.spawnElevationMin) {
            return false;
         }

         if (solidHeight > this.spawnElevationMax) {
            solidHeight = this.spawnElevationMax;
         }

         y = random.nextInt(solidHeight - this.spawnElevationMin) + this.spawnElevationMin;
      } else {
         y = world.getHighestBlockYAt(x, z);
      }

      if (y < 0) {
         return false;
      } else {
         Rotation rotation = this.randomRotation ? Rotation.getRandomRotation(random) : Rotation.NORTH;
         if (!this.canSpawnAt(world, rotation, x, y, z)) {
            return false;
         } else {
            boolean objectSpawned = this.spawnForced(world, random, rotation, x, y, z);
            return objectSpawned;
         }
      }
   }

   public boolean spawnAsTree(LocalWorld world, Random random, int x, int z) {
      return this.spawn(world, random, x, z);
   }

   public boolean process(LocalWorld world, Random rand, int chunkX, int chunkZ) {
      if (this.branch) {
         return false;
      } else {
         int randomRoll = rand.nextInt(100);
         int ObjectRarity = this.rarity;

         boolean objectSpawned;
         int x;
         int z;
         for(objectSpawned = false; randomRoll < ObjectRarity; objectSpawned = this.spawn(world, rand, x, z)) {
            ObjectRarity -= 100;
            x = chunkX * 16 + rand.nextInt(16);
            z = chunkZ * 16 + rand.nextInt(16);
         }

         return objectSpawned;
      }
   }

   public CustomObject applySettings(Map extraSettings) {
      Map<String, String> newSettings = new HashMap();
      newSettings.putAll(this.settingsCache);
      newSettings.putAll(extraSettings);
      return new BO2(newSettings, this.getName());
   }

   protected void writeConfigSettings() throws IOException {
   }

   protected void readConfigSettings() {
      this.version = this.readModSettings(BODefaultValues.version.name(), BODefaultValues.version.stringValue());
      this.spawnOnBlockType = this.ReadBlockList(this.readModSettings(BODefaultValues.spawnOnBlockType.name(), BODefaultValues.spawnOnBlockType.StringArrayListValue()), BODefaultValues.spawnOnBlockType.name());
      this.collisionBlockType = this.ReadBlockList(this.readModSettings(BODefaultValues.collisionBlockType.name(), BODefaultValues.collisionBlockType.StringArrayListValue()), BODefaultValues.collisionBlockType.name());
      this.spawnInBiome = new HashSet(this.readModSettings(BODefaultValues.spawnInBiome.name(), BODefaultValues.spawnInBiome.StringArrayListValue()));
      this.spawnSunlight = this.readModSettings(BODefaultValues.spawnSunlight.name(), BODefaultValues.spawnSunlight.booleanValue());
      this.spawnDarkness = this.readModSettings(BODefaultValues.spawnDarkness.name(), BODefaultValues.spawnDarkness.booleanValue());
      this.spawnWater = this.readModSettings(BODefaultValues.spawnWater.name(), BODefaultValues.spawnWater.booleanValue());
      this.spawnLava = this.readModSettings(BODefaultValues.spawnLava.name(), BODefaultValues.spawnLava.booleanValue());
      this.spawnAboveGround = this.readModSettings(BODefaultValues.spawnAboveGround.name(), BODefaultValues.spawnAboveGround.booleanValue());
      this.spawnUnderGround = this.readModSettings(BODefaultValues.spawnUnderGround.name(), BODefaultValues.spawnUnderGround.booleanValue());
      this.underFill = this.readModSettings(BODefaultValues.underFill.name(), BODefaultValues.underFill.booleanValue());
      this.randomRotation = this.readModSettings(BODefaultValues.randomRotation.name(), BODefaultValues.randomRotation.booleanValue());
      this.dig = this.readModSettings(BODefaultValues.dig.name(), BODefaultValues.dig.booleanValue());
      this.tree = this.readModSettings(BODefaultValues.tree.name(), BODefaultValues.tree.booleanValue());
      this.branch = this.readModSettings(BODefaultValues.branch.name(), BODefaultValues.branch.booleanValue());
      this.diggingBranch = this.readModSettings(BODefaultValues.diggingBranch.name(), BODefaultValues.diggingBranch.booleanValue());
      this.needsFoundation = this.readModSettings(BODefaultValues.needsFoundation.name(), BODefaultValues.needsFoundation.booleanValue());
      this.rarity = this.readModSettings(BODefaultValues.rarity.name(), BODefaultValues.rarity.intValue());
      this.collisionPercentage = (double)this.readModSettings(BODefaultValues.collisionPercentage.name(), BODefaultValues.collisionPercentage.intValue());
      this.spawnElevationMin = this.readModSettings(BODefaultValues.spawnElevationMin.name(), BODefaultValues.spawnElevationMin.intValue());
      this.spawnElevationMax = this.readModSettings(BODefaultValues.spawnElevationMax.name(), BODefaultValues.spawnElevationMax.intValue());
      this.groupFrequencyMin = this.readModSettings(BODefaultValues.groupFrequencyMin.name(), BODefaultValues.groupFrequencyMin.intValue());
      this.groupFrequencyMax = this.readModSettings(BODefaultValues.groupFrequencyMax.name(), BODefaultValues.groupFrequencyMax.intValue());
      this.groupSeparationMin = this.readModSettings(BODefaultValues.groupSeperationMin.name(), BODefaultValues.groupSeperationMin.intValue());
      this.groupSeparationMax = this.readModSettings(BODefaultValues.groupSeperationMax.name(), BODefaultValues.groupSeperationMax.intValue());
      this.groupId = this.readModSettings(BODefaultValues.groupId.name(), BODefaultValues.groupId.stringValue());
      this.branchLimit = this.readModSettings(BODefaultValues.branchLimit.name(), BODefaultValues.branchLimit.intValue());
      this.ReadCoordinates();
   }

   protected void correctSettings() {
   }

   protected void renameOldSettings() {
   }

   private void ReadCoordinates() {
      ArrayList<ObjectCoordinate> coordinates = new ArrayList();

      for(String key : this.settingsCache.keySet()) {
         ObjectCoordinate buffer = ObjectCoordinate.getCoordinateFromString(key, (String)this.settingsCache.get(key));
         if (buffer != null) {
            coordinates.add(buffer);
         }
      }

      this.data[0] = new ObjectCoordinate[coordinates.size()];
      this.data[1] = new ObjectCoordinate[coordinates.size()];
      this.data[2] = new ObjectCoordinate[coordinates.size()];
      this.data[3] = new ObjectCoordinate[coordinates.size()];

      for(int i = 0; i < coordinates.size(); ++i) {
         ObjectCoordinate coordinate = (ObjectCoordinate)coordinates.get(i);
         this.data[0][i] = coordinate;
         coordinate = coordinate.Rotate();
         this.data[1][i] = coordinate;
         coordinate = coordinate.Rotate();
         this.data[2][i] = coordinate;
         coordinate = coordinate.Rotate();
         this.data[3][i] = coordinate;
      }

   }

   private HashSet ReadBlockList(ArrayList blocks, String settingName) {
      HashSet<Integer> output = new HashSet();
      boolean nonIntegerValues = false;
      boolean all = false;
      boolean solid = false;

      for(String block : blocks) {
         if (block.equals(BODefaultValues.BO_ALL_KEY.stringValue())) {
            all = true;
         } else if (block.equals(BODefaultValues.BO_SolidKey.stringValue())) {
            solid = true;
         } else {
            try {
               int blockID = Integer.decode(block);
               if (blockID != 0) {
                  output.add(blockID);
               }
            } catch (NumberFormatException var11) {
               nonIntegerValues = true;
            }
         }
      }

      if (all || solid) {
         for(DefaultMaterial material : DefaultMaterial.values()) {
            if (material.id != 0 && (!solid || material.isSolid())) {
               output.add(material.id);
            }
         }
      }

      if (nonIntegerValues) {
         System.out.println("TerrainControl: Custom object " + this.name + " has wrong value " + settingName);
      }

      return output;
   }

   public boolean hasPreferenceToSpawnIn(LocalBiome biome) {
      return this.spawnInBiome.contains(biome.getName()) || this.spawnInBiome.contains("All");
   }
}
