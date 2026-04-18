package com.khorn.terraincontrol.bukkit.structuregens;

import com.khorn.terraincontrol.LocalWorld;
import com.khorn.terraincontrol.bukkit.util.WorldHelper;
import java.util.Random;
import net.minecraft.server.v1_6_R2.IChunkProvider;
import net.minecraft.server.v1_6_R2.StructureGenerator;
import net.minecraft.server.v1_6_R2.StructureStart;
import net.minecraft.server.v1_6_R2.World;
import net.minecraft.server.v1_6_R2.WorldGenMineshaftStart;

public class MineshaftGen extends StructureGenerator {
   public MineshaftGen() {
      super();
   }

   protected boolean a(int chunkX, int chunkZ) {
      Random rand = this.b;
      World worldMC = this.c;
      if (rand.nextInt(80) < Math.max(Math.abs(chunkX), Math.abs(chunkZ))) {
         LocalWorld world = WorldHelper.toLocalWorld(worldMC);
         int biomeId = world.getCalculatedBiomeId(chunkX * 16 + 8, chunkZ * 16 + 8);
         if (rand.nextDouble() * (double)100.0F < world.getSettings().biomeConfigs[biomeId].mineshaftsRarity) {
            return true;
         }
      }

      return false;
   }

   protected StructureStart b(int i, int j) {
      return new WorldGenMineshaftStart(this.c, this.b, i, j);
   }

   public void prepare(World world, int chunkX, int chunkZ, byte[] chunkArray) {
      this.a((IChunkProvider)null, world, chunkX, chunkZ, chunkArray);
   }

   public void place(World world, Random random, int chunkX, int chunkZ) {
      this.a(world, random, chunkX, chunkZ);
   }
}
