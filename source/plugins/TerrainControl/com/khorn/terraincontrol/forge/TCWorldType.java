package com.khorn.terraincontrol.forge;

import com.khorn.terraincontrol.TerrainControl;
import com.khorn.terraincontrol.biomegenerators.BiomeGenerator;
import com.khorn.terraincontrol.configuration.WorldConfig;
import com.khorn.terraincontrol.forge.util.ForgeMetricsHelper;
import com.khorn.terraincontrol.forge.util.WorldHelper;
import java.io.File;
import net.minecraft.client.multiplayer.WorldClient;
import net.minecraft.world.World;
import net.minecraft.world.WorldType;
import net.minecraft.world.biome.WorldChunkManager;
import net.minecraft.world.chunk.IChunkProvider;

public class TCWorldType extends WorldType {
   public SingleWorld worldTC;
   private TCPlugin plugin;
   private String field_77133_f;

   public TCWorldType(TCPlugin plugin, String paramString) {
      super(WorldHelper.getNextWorldTypeID(), paramString);
      this.plugin = plugin;
      this.field_77133_f = paramString;
   }

   public String func_77128_b() {
      return this.field_77133_f;
   }

   public WorldChunkManager getChunkManager(World world) {
      boolean standAloneServer = false;

      try {
         if (world instanceof WorldClient) {
            return super.getChunkManager(world);
         }
      } catch (NoClassDefFoundError var8) {
         standAloneServer = true;
      }

      SingleWorld.restoreBiomes();
      File worldDirectory = new File(this.plugin.terrainControlDirectory, "worlds" + File.separator + world.func_72860_G().func_75760_g());
      if (!worldDirectory.exists()) {
         System.out.println("TerrainControl: settings does not exist, creating defaults");
         if (!worldDirectory.mkdirs()) {
            System.out.println("TerrainControl: cant create folder " + worldDirectory.getAbsolutePath());
         }
      }

      this.worldTC = new SingleWorld(world.func_72860_G().func_75760_g());
      WorldConfig config = new WorldConfig(worldDirectory, this.worldTC, false);
      this.worldTC.Init(world, config);
      WorldChunkManager chunkManager = null;
      Class<? extends BiomeGenerator> biomeManagerClass = this.worldTC.getSettings().biomeMode;
      if (biomeManagerClass == TerrainControl.getBiomeModeManager().VANILLA) {
         chunkManager = super.getChunkManager(world);
      } else {
         chunkManager = new TCWorldChunkManager(this.worldTC);
         BiomeGenerator biomeManager = TerrainControl.getBiomeModeManager().create(biomeManagerClass, this.worldTC, new BiomeCacheWrapper(chunkManager));
         ((TCWorldChunkManager)chunkManager).setBiomeManager(biomeManager);
         this.worldTC.setBiomeManager(biomeManager);
      }

      if (standAloneServer) {
         new ForgeMetricsHelper(this.plugin);
      }

      return chunkManager;
   }

   public IChunkProvider getChunkGenerator(World world, String generatorOptions) {
      return (IChunkProvider)(this.worldTC.getSettings().ModeTerrain != WorldConfig.TerrainMode.Default ? this.worldTC.getChunkGenerator() : super.getChunkGenerator(world, generatorOptions));
   }

   public int getMinimumSpawnHeight(World world) {
      return WorldHelper.toLocalWorld(world).getSettings().waterLevelMax;
   }
}
