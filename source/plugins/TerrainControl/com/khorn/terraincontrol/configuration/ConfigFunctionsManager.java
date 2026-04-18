package com.khorn.terraincontrol.configuration;

import com.khorn.terraincontrol.TerrainControl;
import com.khorn.terraincontrol.exception.InvalidConfigException;
import com.khorn.terraincontrol.generator.resourcegens.AboveWaterGen;
import com.khorn.terraincontrol.generator.resourcegens.CactusGen;
import com.khorn.terraincontrol.generator.resourcegens.CustomObjectGen;
import com.khorn.terraincontrol.generator.resourcegens.CustomStructureGen;
import com.khorn.terraincontrol.generator.resourcegens.DungeonGen;
import com.khorn.terraincontrol.generator.resourcegens.GrassGen;
import com.khorn.terraincontrol.generator.resourcegens.LiquidGen;
import com.khorn.terraincontrol.generator.resourcegens.OreGen;
import com.khorn.terraincontrol.generator.resourcegens.PlantGen;
import com.khorn.terraincontrol.generator.resourcegens.ReedGen;
import com.khorn.terraincontrol.generator.resourcegens.SaplingGen;
import com.khorn.terraincontrol.generator.resourcegens.SmallLakeGen;
import com.khorn.terraincontrol.generator.resourcegens.TreeGen;
import com.khorn.terraincontrol.generator.resourcegens.UnderWaterOreGen;
import com.khorn.terraincontrol.generator.resourcegens.UndergroundLakeGen;
import com.khorn.terraincontrol.generator.resourcegens.VeinGen;
import com.khorn.terraincontrol.generator.resourcegens.VinesGen;
import com.khorn.terraincontrol.generator.resourcegens.WellGen;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;

public class ConfigFunctionsManager {
   private Map configFunctions = new HashMap();

   public ConfigFunctionsManager() {
      super();
      this.registerConfigFunction("AboveWaterRes", AboveWaterGen.class);
      this.registerConfigFunction("Cactus", CactusGen.class);
      this.registerConfigFunction("CustomObject", CustomObjectGen.class);
      this.registerConfigFunction("CustomStructure", CustomStructureGen.class);
      this.registerConfigFunction("Dungeon", DungeonGen.class);
      this.registerConfigFunction("Grass", GrassGen.class);
      this.registerConfigFunction("Liquid", LiquidGen.class);
      this.registerConfigFunction("Ore", OreGen.class);
      this.registerConfigFunction("Plant", PlantGen.class);
      this.registerConfigFunction("Reed", ReedGen.class);
      this.registerConfigFunction("Sapling", SaplingGen.class);
      this.registerConfigFunction("SmallLake", SmallLakeGen.class);
      this.registerConfigFunction("Tree", TreeGen.class);
      this.registerConfigFunction("UndergroundLake", UndergroundLakeGen.class);
      this.registerConfigFunction("UnderWaterOre", UnderWaterOreGen.class);
      this.registerConfigFunction("Vein", VeinGen.class);
      this.registerConfigFunction("Vines", VinesGen.class);
      this.registerConfigFunction("Well", WellGen.class);
   }

   public void registerConfigFunction(String name, Class value) {
      this.configFunctions.put(name.toLowerCase(), value);
   }

   public ConfigFunction getConfigFunction(String name, Object holder, String locationOfResource, List args) {
      if (!this.configFunctions.containsKey(name.toLowerCase())) {
         TerrainControl.log("Invalid resource " + name + " in " + locationOfResource + ": resource type not found!");
         return null;
      } else {
         Class<? extends ConfigFunction<?>> clazz = (Class)this.configFunctions.get(name.toLowerCase());

         ConfigFunction<?> configFunction;
         try {
            configFunction = (ConfigFunction)clazz.newInstance();
         } catch (InstantiationException e) {
            TerrainControl.log(Level.WARNING, "Reflection error (Instantiation) while loading the resources: " + e.getMessage());
            e.printStackTrace();
            return null;
         } catch (IllegalAccessException e) {
            TerrainControl.log(Level.WARNING, "Reflection error (IllegalAccess) while loading the resources: " + e.getMessage());
            e.printStackTrace();
            return null;
         }

         boolean matchingTypes;
         try {
            matchingTypes = holder.getClass().isAssignableFrom((Class)clazz.getMethod("getHolderType").invoke(configFunction));
         } catch (Exception e) {
            TerrainControl.log(Level.WARNING, "Reflection error (" + e.getClass().getSimpleName() + ") while loading the resources: " + e.getMessage());
            e.printStackTrace();
            return null;
         }

         if (!matchingTypes) {
            TerrainControl.log(Level.WARNING, "Invalid resource " + name + " in " + locationOfResource + ": cannot be placed in this config file!");
            return null;
         } else {
            configFunction.setHolder(holder);

            try {
               configFunction.read(name, args);
            } catch (InvalidConfigException e) {
               TerrainControl.log(Level.WARNING, "Invalid resource " + name + " in " + locationOfResource + ": " + e.getMessage());
            }

            return configFunction;
         }
      }
   }
}
