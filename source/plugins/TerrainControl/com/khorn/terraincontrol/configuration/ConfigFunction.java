package com.khorn.terraincontrol.configuration;

import com.khorn.terraincontrol.TerrainControl;
import com.khorn.terraincontrol.exception.InvalidConfigException;
import com.khorn.terraincontrol.util.StringHelper;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public abstract class ConfigFunction {
   private Object holder;
   private boolean valid;
   private String error;
   private String inputName;
   private List inputArgs;

   public ConfigFunction() {
      super();
   }

   public void setHolder(Object holder) {
      this.holder = holder;
   }

   public Object getHolder() {
      return this.holder;
   }

   public abstract Class getHolderType();

   public static ConfigFunction create(Object holder, Class clazz, Object... args) {
      List<String> stringArgs = new ArrayList(args.length);

      for(Object arg : args) {
         stringArgs.add("" + arg);
      }

      ConfigFunction<?> configFunction;
      try {
         configFunction = (ConfigFunction)clazz.newInstance();
      } catch (InstantiationException var9) {
         return null;
      } catch (IllegalAccessException var10) {
         return null;
      }

      configFunction.setHolder(holder);

      try {
         configFunction.load(stringArgs);
      } catch (InvalidConfigException e) {
         TerrainControl.log("Invalid default config function! Please report! " + clazz.getName() + ": " + e.getMessage());
         e.printStackTrace();
      }

      return configFunction;
   }

   public final void read(String name, List args) throws InvalidConfigException {
      try {
         this.load(args);
      } catch (InvalidConfigException e) {
         this.valid = false;
         this.error = e.getMessage();
         this.inputArgs = args;
         this.inputName = name;
         throw e;
      }

      this.valid = true;
   }

   public boolean isValid() {
      return this.valid;
   }

   public void setValid(boolean valid) {
      this.valid = valid;
   }

   public final String write() {
      return !this.valid ? "## INVALID " + this.inputName.toUpperCase() + " - " + this.error + " ##" + System.getProperty("line.separator") + this.inputName + "(" + StringHelper.join((Collection)this.inputArgs, ",") + ")" : this.makeString();
   }

   protected abstract void load(List var1) throws InvalidConfigException;

   protected abstract String makeString();

   protected int readInt(String string, int minValue, int maxValue) throws InvalidConfigException {
      return StringHelper.readInt(string, minValue, maxValue);
   }

   protected double readDouble(String string, double minValue, double maxValue) throws InvalidConfigException {
      return StringHelper.readDouble(string, minValue, maxValue);
   }

   protected double readRarity(String string) throws InvalidConfigException {
      return StringHelper.readDouble(string, 1.0E-6, (double)100.0F);
   }

   protected int readBlockId(String string) throws InvalidConfigException {
      return StringHelper.readBlockId(string);
   }

   protected List readBlockIds(List strings, int start) throws InvalidConfigException {
      List<Integer> blockIds = new ArrayList();

      for(int i = start; i < strings.size(); ++i) {
         blockIds.add(StringHelper.readBlockId((String)strings.get(i)));
      }

      return blockIds;
   }

   protected int readBlockData(String string) throws InvalidConfigException {
      return StringHelper.readBlockData(string);
   }

   protected void assureSize(int size, List args) throws InvalidConfigException {
      if (args.size() < size) {
         throw new InvalidConfigException("Too few arguments supplied");
      }
   }

   protected String makeMaterial(int id, int data) {
      return StringHelper.makeMaterial(id, data);
   }

   protected String makeMaterial(int id) {
      return StringHelper.makeMaterial(id);
   }

   protected String makeMaterial(List ids) {
      String string = "";

      for(int blockId : ids) {
         string = string + ",";
         string = string + this.makeMaterial(blockId);
      }

      return string;
   }
}
