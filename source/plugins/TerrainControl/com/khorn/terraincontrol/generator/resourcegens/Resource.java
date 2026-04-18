package com.khorn.terraincontrol.generator.resourcegens;

import com.khorn.terraincontrol.LocalWorld;
import com.khorn.terraincontrol.TerrainControl;
import com.khorn.terraincontrol.configuration.BiomeConfig;
import com.khorn.terraincontrol.configuration.ConfigFunction;
import com.khorn.terraincontrol.exception.InvalidConfigException;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.logging.Level;

public abstract class Resource extends ConfigFunction {
   protected int blockId = -1;
   protected int blockData = -1;
   protected int frequency;
   protected double rarity;

   public Resource() {
      super();
   }

   public Class getHolderType() {
      return BiomeConfig.class;
   }

   public abstract void spawn(LocalWorld var1, Random var2, boolean var3, int var4, int var5);

   public final void process(LocalWorld world, Random random, boolean villageInChunk, int chunkX, int chunkZ) {
      if (this.isValid()) {
         if (TerrainControl.fireResourceProcessEvent(this, world, random, villageInChunk, chunkX, chunkZ)) {
            this.spawnInChunk(world, random, villageInChunk, chunkX, chunkZ);
         }
      }
   }

   protected void spawnInChunk(LocalWorld world, Random random, boolean villageInChunk, int chunkX, int chunkZ) {
      for(int t = 0; t < this.frequency; ++t) {
         if (!(random.nextDouble() * (double)100.0F > this.rarity)) {
            int x = chunkX * 16 + random.nextInt(16) + 8;
            int z = chunkZ * 16 + random.nextInt(16) + 8;
            this.spawn(world, random, false, x, z);
         }
      }

   }

   public static Resource createResource(BiomeConfig config, Class clazz, Object... args) {
      List<String> stringArgs = new ArrayList(args.length);

      for(Object arg : args) {
         stringArgs.add("" + arg);
      }

      Resource resource;
      try {
         resource = (Resource)clazz.newInstance();
      } catch (InstantiationException var9) {
         return null;
      } catch (IllegalAccessException var10) {
         return null;
      }

      resource.setHolder(config);

      try {
         resource.load(stringArgs);
         resource.setValid(true);
         return resource;
      } catch (InvalidConfigException e) {
         TerrainControl.log(Level.SEVERE, "Invalid default resource! Please report! " + clazz.getName() + ": " + e.getMessage());
         e.printStackTrace();
         throw new RuntimeException(e);
      }
   }

   public int getBlockId() {
      return this.blockId;
   }
}
