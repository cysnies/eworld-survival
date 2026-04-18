package com.khorn.terraincontrol.customobjects.bo3;

import com.khorn.terraincontrol.DefaultMaterial;
import com.khorn.terraincontrol.configuration.TCSetting;
import com.khorn.terraincontrol.customobjects.CustomObjectCoordinate;
import java.util.ArrayList;
import java.util.Collections;

public enum BO3Settings implements TCSetting {
   author("Unknown"),
   description("No description given"),
   tree(true),
   frequency(1),
   rarity((double)100.0F),
   rotateRandomly(false),
   spawnHeight(BO3Settings.SpawnHeightSetting.highestBlock),
   minHeight(0),
   maxHeight(256),
   maxBranchDepth(10),
   excludedBiomes("All", TCSetting.SettingsType.StringArray),
   sourceBlock(DefaultMaterial.AIR.id),
   outsideSourceBlock(BO3Settings.OutsideSourceBlock.placeAnyway),
   maxPercentageOutsideSourceBlock(100);

   private Object value;
   private TCSetting.SettingsType returnType;

   private BO3Settings(int i) {
      this.value = i;
      this.returnType = TCSetting.SettingsType.Int;
   }

   private BO3Settings(long i) {
      this.value = i;
      this.returnType = TCSetting.SettingsType.Long;
   }

   private BO3Settings(double d) {
      this.value = d;
      this.returnType = TCSetting.SettingsType.Double;
   }

   private BO3Settings(float f) {
      this.value = f;
      this.returnType = TCSetting.SettingsType.Float;
   }

   private BO3Settings(String s) {
      this.value = s;
      this.returnType = TCSetting.SettingsType.String;
   }

   private BO3Settings(String s, TCSetting.SettingsType type) {
      this.returnType = type;
      if (type == TCSetting.SettingsType.StringArray) {
         ArrayList<String> list = new ArrayList();
         if (s.contains(",")) {
            Collections.addAll(list, s.split(","));
         } else if (!s.equals("")) {
            list.add(s);
         }

         this.value = list;
      } else {
         this.value = s;
      }
   }

   private BO3Settings(Enum e) {
      this.value = e;
      this.returnType = TCSetting.SettingsType.Enum;
   }

   private BO3Settings(Boolean b) {
      this.value = b;
      this.returnType = TCSetting.SettingsType.Boolean;
   }

   public int intValue() {
      return (Integer)this.value;
   }

   public double doubleValue() {
      return (Double)this.value;
   }

   public float floatValue() {
      return (Float)this.value;
   }

   public Enum enumValue() {
      return (Enum)this.value;
   }

   public TCSetting.SettingsType getReturnType() {
      return this.returnType;
   }

   public String stringValue() {
      return (String)this.value;
   }

   public ArrayList stringArrayListValue() {
      return (ArrayList)this.value;
   }

   public boolean booleanValue() {
      return (Boolean)this.value;
   }

   public long longValue() {
      return (Long)this.value;
   }

   public static enum SpawnHeightSetting {
      randomY(CustomObjectCoordinate.SpawnHeight.PROVIDED),
      highestBlock(CustomObjectCoordinate.SpawnHeight.HIGHEST_BLOCK),
      highestSolidBlock(CustomObjectCoordinate.SpawnHeight.HIGHEST_SOLID_BLOCK);

      private CustomObjectCoordinate.SpawnHeight height;

      private SpawnHeightSetting(CustomObjectCoordinate.SpawnHeight height) {
         this.height = height;
      }

      public CustomObjectCoordinate.SpawnHeight toSpawnHeight() {
         return this.height;
      }
   }

   public static enum OutsideSourceBlock {
      dontPlace,
      placeAnyway;

      private OutsideSourceBlock() {
      }
   }
}
