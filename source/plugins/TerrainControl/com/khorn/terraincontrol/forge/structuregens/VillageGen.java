package com.khorn.terraincontrol.forge.structuregens;

import com.khorn.terraincontrol.configuration.BiomeConfig;
import com.khorn.terraincontrol.configuration.WorldConfig;
import com.khorn.terraincontrol.forge.Biome;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import net.minecraft.world.gen.structure.MapGenStructure;
import net.minecraft.world.gen.structure.StructureStart;

public class VillageGen extends MapGenStructure {
   public List villageSpawnBiomes;
   private int size;
   private int distance;
   private int minimumDistance;

   public VillageGen(WorldConfig worldConfig) {
      super();
      this.size = worldConfig.villageSize;
      this.distance = worldConfig.villageDistance;
      this.minimumDistance = 8;
      this.villageSpawnBiomes = new ArrayList();

      for(BiomeConfig config : worldConfig.biomeConfigs) {
         if (config != null && config.villageType != BiomeConfig.VillageType.disabled) {
            this.villageSpawnBiomes.add(((Biome)config.Biome).getHandle());
         }
      }

   }

   protected boolean func_75047_a(int chunkX, int chunkZ) {
      int var3 = chunkX;
      int var4 = chunkZ;
      if (chunkX < 0) {
         chunkX -= this.distance - 1;
      }

      if (chunkZ < 0) {
         chunkZ -= this.distance - 1;
      }

      int var5 = chunkX / this.distance;
      int var6 = chunkZ / this.distance;
      Random var7 = this.field_75039_c.func_72843_D(var5, var6, 10387312);
      var5 *= this.distance;
      var6 *= this.distance;
      var5 += var7.nextInt(this.distance - this.minimumDistance);
      var6 += var7.nextInt(this.distance - this.minimumDistance);
      if (var3 == var5 && var4 == var6) {
         boolean canSpawn = this.field_75039_c.func_72959_q().func_76940_a(var3 * 16 + 8, var4 * 16 + 8, 0, this.villageSpawnBiomes);
         if (canSpawn) {
            return true;
         }
      }

      return false;
   }

   protected StructureStart func_75049_b(int chunkX, int chunkZ) {
      return new StructureVillageStart(this.field_75039_c, this.field_75038_b, chunkX, chunkZ, this.size);
   }
}
