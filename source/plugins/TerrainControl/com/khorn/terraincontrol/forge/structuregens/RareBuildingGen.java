package com.khorn.terraincontrol.forge.structuregens;

import com.khorn.terraincontrol.configuration.BiomeConfig;
import com.khorn.terraincontrol.configuration.WorldConfig;
import com.khorn.terraincontrol.forge.Biome;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import net.minecraft.entity.monster.EntityWitch;
import net.minecraft.world.biome.BiomeGenBase;
import net.minecraft.world.biome.SpawnListEntry;
import net.minecraft.world.gen.structure.MapGenStructure;
import net.minecraft.world.gen.structure.StructureStart;

public class RareBuildingGen extends MapGenStructure {
   public List biomeList = new ArrayList();
   private List scatteredFeatureSpawnList;
   private int maxDistanceBetweenScatteredFeatures;
   private int minDistanceBetweenScatteredFeatures;

   public RareBuildingGen(WorldConfig worldConfig) {
      super();

      for(BiomeConfig biomeConfig : worldConfig.biomeConfigs) {
         if (biomeConfig != null && biomeConfig.rareBuildingType != BiomeConfig.RareBuildingType.disabled) {
            this.biomeList.add(((Biome)biomeConfig.Biome).getHandle());
         }
      }

      this.scatteredFeatureSpawnList = new ArrayList();
      this.maxDistanceBetweenScatteredFeatures = worldConfig.maximumDistanceBetweenRareBuildings;
      this.minDistanceBetweenScatteredFeatures = worldConfig.minimumDistanceBetweenRareBuildings - 1;
      this.scatteredFeatureSpawnList.add(new SpawnListEntry(EntityWitch.class, 1, 1, 1));
   }

   protected boolean func_75047_a(int chunkX, int chunkZ) {
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
      Random random = this.field_75039_c.func_72843_D(var5, var6, 14357617);
      var5 *= this.maxDistanceBetweenScatteredFeatures;
      var6 *= this.maxDistanceBetweenScatteredFeatures;
      var5 += random.nextInt(this.maxDistanceBetweenScatteredFeatures - this.minDistanceBetweenScatteredFeatures);
      var6 += random.nextInt(this.maxDistanceBetweenScatteredFeatures - this.minDistanceBetweenScatteredFeatures);
      if (var3 == var5 && var4 == var6) {
         BiomeGenBase biomeAtPosition = this.field_75039_c.func_72959_q().func_76935_a(var3 * 16 + 8, var4 * 16 + 8);

         for(BiomeGenBase biome : this.biomeList) {
            if (biomeAtPosition.field_76756_M == biome.field_76756_M) {
               return true;
            }
         }
      }

      return false;
   }

   protected StructureStart func_75049_b(int chunkX, int chunkZ) {
      return new RareBuildingStart(this.field_75039_c, this.field_75038_b, chunkX, chunkZ);
   }

   public List getScatteredFeatureSpawnList() {
      return this.scatteredFeatureSpawnList;
   }
}
