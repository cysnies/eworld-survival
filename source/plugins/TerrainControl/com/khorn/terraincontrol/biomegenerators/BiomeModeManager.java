package com.khorn.terraincontrol.biomegenerators;

import com.khorn.terraincontrol.LocalWorld;
import com.khorn.terraincontrol.TerrainControl;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;

public class BiomeModeManager {
   private Map registered = new HashMap();
   public final Class NORMAL = this.register("Normal", NormalBiomeGenerator.class);
   public final Class FROM_IMAGE = this.register("FromImage", FromImageBiomeGenerator.class);
   public final Class OLD_GENERATOR = this.register("OldGenerator", OldBiomeGenerator.class);
   public final Class VANILLA = this.register("Default", VanillaBiomeGenerator.class);

   public BiomeModeManager() {
      super();
   }

   public Class register(String name, Class clazz) {
      this.registered.put(name, clazz);
      return clazz;
   }

   public Class getBiomeManager(String name) {
      for(String key : this.registered.keySet()) {
         if (key.equalsIgnoreCase(name)) {
            return (Class)this.registered.get(key);
         }
      }

      TerrainControl.log(name + " is not a valid biome mode, falling back on Normal.");
      return this.NORMAL;
   }

   public BiomeGenerator create(Class clazz, LocalWorld world, BiomeCache cache) {
      try {
         return (BiomeGenerator)clazz.getConstructor(LocalWorld.class, BiomeCache.class).newInstance(world, cache);
      } catch (Exception e) {
         TerrainControl.log(Level.SEVERE, "Cannot properly reflect biome manager, falling back on BiomeMode:Normal");
         e.printStackTrace();
         return new NormalBiomeGenerator(world, cache);
      }
   }

   public String getName(Class clazz) {
      for(Map.Entry entry : this.registered.entrySet()) {
         if (((Class)entry.getValue()).equals(clazz)) {
            return (String)entry.getKey();
         }
      }

      return null;
   }
}
