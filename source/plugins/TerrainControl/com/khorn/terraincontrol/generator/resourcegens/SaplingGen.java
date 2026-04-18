package com.khorn.terraincontrol.generator.resourcegens;

import com.khorn.terraincontrol.LocalWorld;
import com.khorn.terraincontrol.TerrainControl;
import com.khorn.terraincontrol.configuration.BiomeConfig;
import com.khorn.terraincontrol.configuration.ConfigFunction;
import com.khorn.terraincontrol.customobjects.CustomObject;
import com.khorn.terraincontrol.customobjects.Rotation;
import com.khorn.terraincontrol.exception.InvalidConfigException;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class SaplingGen extends ConfigFunction {
   public List trees;
   public List treeNames;
   public List treeChances;
   public SaplingType saplingType;

   public SaplingGen() {
      super();
   }

   public Class getHolderType() {
      return BiomeConfig.class;
   }

   public void load(List args) throws InvalidConfigException {
      this.assureSize(3, args);
      this.saplingType = SaplingType.get((String)args.get(0));
      if (this.saplingType == null) {
         throw new InvalidConfigException("Unknown sapling type " + (String)args.get(0));
      } else {
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
   }

   public String makeString() {
      String output = "Sapling(" + this.saplingType;

      for(int i = 0; i < this.treeNames.size(); ++i) {
         output = output + "," + (String)this.treeNames.get(i) + "," + this.treeChances.get(i);
      }

      return output + ")";
   }

   public boolean growSapling(LocalWorld world, Random random, int x, int y, int z) {
      for(int treeNumber = 0; treeNumber < this.trees.size(); ++treeNumber) {
         if (random.nextInt(100) < (Integer)this.treeChances.get(treeNumber)) {
            Rotation rotation = ((CustomObject)this.trees.get(treeNumber)).canRotateRandomly() ? Rotation.getRandomRotation(random) : Rotation.NORTH;
            if (((CustomObject)this.trees.get(treeNumber)).spawnForced(world, random, rotation, x, y, z)) {
               return true;
            }
         }
      }

      return false;
   }
}
