package com.khorn.terraincontrol.util;

import com.khorn.terraincontrol.DefaultMaterial;
import com.khorn.terraincontrol.TerrainControl;
import com.khorn.terraincontrol.exception.InvalidConfigException;
import java.util.Collection;
import java.util.List;

public abstract class StringHelper {
   public StringHelper() {
      super();
   }

   public static String join(Collection coll, String glue) {
      return join(coll.toArray(new Object[coll.size()]), glue);
   }

   public static String join(Object[] list, String glue) {
      StringBuilder ret = new StringBuilder();

      for(int i = 0; i < list.length; ++i) {
         if (i != 0) {
            ret.append(glue);
         }

         ret.append(list[i]);
      }

      return ret.toString();
   }

   public static int readInt(String string, int minValue, int maxValue) throws InvalidConfigException {
      try {
         int number = Integer.parseInt(string);
         if (number < minValue) {
            return minValue;
         } else {
            return number > maxValue ? maxValue : number;
         }
      } catch (NumberFormatException var4) {
         throw new InvalidConfigException("Incorrect number: " + string);
      }
   }

   public static double readDouble(String string, double minValue, double maxValue) throws InvalidConfigException {
      try {
         double number = Double.parseDouble(string);
         if (number < minValue) {
            return minValue;
         } else {
            return number > maxValue ? maxValue : number;
         }
      } catch (NumberFormatException var7) {
         throw new InvalidConfigException("Incorrect number: " + string);
      }
   }

   public static int readBlockId(String string) throws InvalidConfigException {
      if (string.indexOf(46) != -1) {
         string = string.split("\\.")[0];
      }

      if (string.indexOf(58) != -1) {
         string = string.split(":")[0];
      }

      DefaultMaterial material = DefaultMaterial.getMaterial(string);
      if (material != null) {
         return material.id;
      } else {
         int blockId = readInt(string, 0, TerrainControl.supportedBlockIds);
         if (!TerrainControl.getEngine().isValidBlockId(blockId)) {
            throw new InvalidConfigException("There is no block with the block id " + blockId);
         } else {
            return blockId;
         }
      }
   }

   public static int readBlockData(String string) throws InvalidConfigException {
      if (string.indexOf(58) != -1) {
         string = string.split(":")[1];
         return readInt(string, 0, 15);
      } else if (string.indexOf(46) != -1) {
         string = string.split("\\.")[1];
         return readInt(string, 0, 15);
      } else {
         return 0;
      }
   }

   public static String makeMaterial(int id, int data) {
      String materialString = "" + id;
      DefaultMaterial material = DefaultMaterial.getMaterial(id);
      if (material != DefaultMaterial.UNKNOWN_BLOCK) {
         materialString = material.toString();
      }

      if (data > 0) {
         materialString = materialString + ":" + data;
      }

      return materialString;
   }

   public static String makeMaterial(int id) {
      return makeMaterial(id, 0);
   }

   public static String makeMaterial(List ids) {
      String string = "";

      for(int blockId : ids) {
         string = string + ",";
         string = string + makeMaterial(blockId);
      }

      return string;
   }
}
