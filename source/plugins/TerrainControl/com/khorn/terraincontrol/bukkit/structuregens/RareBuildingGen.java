package com.khorn.terraincontrol.bukkit.structuregens;

import com.khorn.terraincontrol.bukkit.BukkitBiome;
import com.khorn.terraincontrol.configuration.BiomeConfig;
import com.khorn.terraincontrol.configuration.WorldConfig;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import net.minecraft.server.v1_6_R2.BiomeBase;
import net.minecraft.server.v1_6_R2.BiomeMeta;
import net.minecraft.server.v1_6_R2.EntityWitch;
import net.minecraft.server.v1_6_R2.IChunkProvider;
import net.minecraft.server.v1_6_R2.StructureGenerator;
import net.minecraft.server.v1_6_R2.StructureStart;
import net.minecraft.server.v1_6_R2.World;

public class RareBuildingGen extends StructureGenerator {
   public List biomeList = new ArrayList();
   private List scatteredFeatureSpawnList;
   private int maxDistanceBetweenScatteredFeatures;
   private int minDistanceBetweenScatteredFeatures;

   public RareBuildingGen(WorldConfig worldConfig) {
      super();

      BiomeConfig[] var5;
      for(BiomeConfig biomeConfig : var5 = worldConfig.biomeConfigs) {
         if (biomeConfig != null && biomeConfig.rareBuildingType != BiomeConfig.RareBuildingType.disabled) {
            this.biomeList.add(((BukkitBiome)biomeConfig.Biome).getHandle());
         }
      }

      this.scatteredFeatureSpawnList = new ArrayList();
      this.maxDistanceBetweenScatteredFeatures = worldConfig.maximumDistanceBetweenRareBuildings;
      this.minDistanceBetweenScatteredFeatures = worldConfig.minimumDistanceBetweenRareBuildings - 1;
      this.scatteredFeatureSpawnList.add(new BiomeMeta(EntityWitch.class, 1, 1, 1));
   }

   protected boolean a(int chunkX, int chunkZ) {
      int var3 = chunkX;
      int var4 = chunkZ;
      if (chunkX < 0) {
         chunkX -= this.maxDistanceBetweenScatteredFeatures - 1;
      }

      if (chunkZ < 0) {
         chunkZ -= this.maxDistanceBetweenScatteredFeatures - 1;
      }

      int var5 = chunkX / this.maxDistanceBetweenScatteredFeatures;
      int var6 = chunkZ / this.maxDistanceBetweenScatteredFeatures;
      Random random = this.c.H(var5, var6, 14357617);
      var5 *= this.maxDistanceBetweenScatteredFeatures;
      var6 *= this.maxDistanceBetweenScatteredFeatures;
      var5 += random.nextInt(this.maxDistanceBetweenScatteredFeatures - this.minDistanceBetweenScatteredFeatures);
      var6 += random.nextInt(this.maxDistanceBetweenScatteredFeatures - this.minDistanceBetweenScatteredFeatures);
      if (var3 == var5 && var4 == var6) {
         BiomeBase biomeAtPosition = this.c.getWorldChunkManager().getBiome(var3 * 16 + 8, var4 * 16 + 8);

         for(BiomeBase biome : this.biomeList) {
            if (biomeAtPosition.id == biome.id) {
               return true;
            }
         }
      }

      return false;
   }

   protected StructureStart b(int chunkX, int chunkZ) {
      return new RareBuildingStart(this.c, this.b, chunkX, chunkZ);
   }

   public List getScatteredFeatureSpawnList() {
      return this.scatteredFeatureSpawnList;
   }

   public void prepare(World world, int chunkX, int chunkZ, byte[] chunkArray) {
      this.a((IChunkProvider)null, world, chunkX, chunkZ, chunkArray);
   }

   public void place(World world, Random random, int chunkX, int chunkZ) {
      this.a(world, random, chunkX, chunkZ);
   }
}
