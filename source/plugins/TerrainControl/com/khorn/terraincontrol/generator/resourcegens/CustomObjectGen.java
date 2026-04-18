package com.khorn.terraincontrol.generator.resourcegens;

import com.khorn.terraincontrol.LocalWorld;
import com.khorn.terraincontrol.TerrainControl;
import com.khorn.terraincontrol.configuration.BiomeConfig;
import com.khorn.terraincontrol.customobjects.CustomObject;
import com.khorn.terraincontrol.exception.InvalidConfigException;
import com.khorn.terraincontrol.util.StringHelper;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Random;

public class CustomObjectGen extends Resource {
   private List objects;
   private List objectNames;

   public CustomObjectGen() {
      super();
   }

   public void load(List args) throws InvalidConfigException {
      if (args.size() == 0 || args.size() == 1 && ((String)args.get(0)).trim().equals("")) {
         args = new ArrayList();
         args.add("UseWorld");
      }

      this.objects = new ArrayList();
      this.objectNames = new ArrayList();

      for(String arg : args) {
         CustomObject object = TerrainControl.getCustomObjectManager().getObjectFromString(arg, ((BiomeConfig)this.getHolder()).worldConfig);
         if (object == null || !object.canSpawnAsObject()) {
            throw new InvalidConfigException("No custom object found with the name " + arg);
         }

         this.objects.add(object);
         this.objectNames.add(arg);
      }

   }

   public void spawn(LocalWorld world, Random random, boolean villageInChunk, int x, int z) {
   }

   protected void spawnInChunk(LocalWorld world, Random random, boolean villageInChunk, int chunkX, int chunkZ) {
      for(CustomObject object : this.objects) {
         object.process(world, random, chunkX, chunkZ);
      }

   }

   public String makeString() {
      return "CustomObject(" + StringHelper.join((Collection)this.objectNames, ",") + ")";
   }
}
