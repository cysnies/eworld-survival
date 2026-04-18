package com.khorn.terraincontrol.bukkit.structuregens;

import com.khorn.terraincontrol.bukkit.BukkitBiome;
import com.khorn.terraincontrol.configuration.BiomeConfig;
import com.khorn.terraincontrol.configuration.WorldConfig;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import net.minecraft.server.v1_6_R2.IChunkProvider;
import net.minecraft.server.v1_6_R2.StructureGenerator;
import net.minecraft.server.v1_6_R2.StructureStart;
import net.minecraft.server.v1_6_R2.World;

public class VillageGen extends StructureGenerator {
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

      BiomeConfig[] var5;
      for(BiomeConfig config : var5 = worldConfig.biomeConfigs) {
         if (config != null && config.villageType != BiomeConfig.VillageType.disabled) {
            this.villageSpawnBiomes.add(((BukkitBiome)config.Biome).getHandle());
         }
      }

   }

   protected boolean a(int chunkX, int chunkZ) {
      int k = chunkX;
      int l = chunkZ;
      if (chunkX < 0) {
         chunkX -= this.distance - 1;
      }

      if (chunkZ < 0) {
         chunkZ -= this.distance - 1;
      }

      int i1 = chunkX / this.distance;
      int j1 = chunkZ / this.distance;
      Random random = this.c.H(i1, j1, 10387312);
      i1 *= this.distance;
      j1 *= this.distance;
      i1 += random.nextInt(this.distance - this.minimumDistance);
      j1 += random.nextInt(this.distance - this.minimumDistance);
      if (k == i1 && l == j1) {
         boolean flag = this.c.getWorldChunkManager().a(k * 16 + 8, l * 16 + 8, 0, this.villageSpawnBiomes);
         if (flag) {
            return true;
         }
      }

      return false;
   }

   protected StructureStart b(int chunkX, int chunkZ) {
      return new VillageStart(this.c, this.b, chunkX, chunkZ, this.size);
   }

   public void prepare(World world, int chunkX, int chunkZ, byte[] chunkArray) {
      this.a((IChunkProvider)null, world, chunkX, chunkZ, chunkArray);
   }

   public boolean place(World world, Random random, int chunkX, int chunkZ) {
      return this.a(world, random, chunkX, chunkZ);
   }
}
