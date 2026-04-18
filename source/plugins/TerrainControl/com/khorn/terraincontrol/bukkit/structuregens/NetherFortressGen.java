package com.khorn.terraincontrol.bukkit.structuregens;

import com.khorn.terraincontrol.LocalWorld;
import com.khorn.terraincontrol.bukkit.util.WorldHelper;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import net.minecraft.server.v1_6_R2.BiomeMeta;
import net.minecraft.server.v1_6_R2.EntityBlaze;
import net.minecraft.server.v1_6_R2.EntityMagmaCube;
import net.minecraft.server.v1_6_R2.EntityPigZombie;
import net.minecraft.server.v1_6_R2.EntitySkeleton;
import net.minecraft.server.v1_6_R2.IChunkProvider;
import net.minecraft.server.v1_6_R2.StructureGenerator;
import net.minecraft.server.v1_6_R2.StructureStart;
import net.minecraft.server.v1_6_R2.World;

public class NetherFortressGen extends StructureGenerator {
   public List spawnList = new ArrayList();

   public NetherFortressGen() {
      super();
      this.spawnList.add(new BiomeMeta(EntityBlaze.class, 10, 2, 3));
      this.spawnList.add(new BiomeMeta(EntityPigZombie.class, 5, 4, 4));
      this.spawnList.add(new BiomeMeta(EntitySkeleton.class, 10, 4, 4));
      this.spawnList.add(new BiomeMeta(EntityMagmaCube.class, 3, 4, 4));
   }

   public List a() {
      return this.spawnList;
   }

   protected boolean a(int chunkX, int chunkZ) {
      Random rand = this.b;
      World worldObj = this.c;
      int var3 = chunkX >> 4;
      int var4 = chunkZ >> 4;
      rand.setSeed((long)(var3 ^ var4 << 4) ^ worldObj.getSeed());
      rand.nextInt();
      if (rand.nextInt(3) != 0) {
         return false;
      } else if (chunkX != (var3 << 4) + 4 + rand.nextInt(8)) {
         return false;
      } else {
         LocalWorld world = WorldHelper.toLocalWorld(worldObj);
         int biomeId = world.getCalculatedBiomeId(chunkX * 16 + 8, chunkZ * 16 + 8);
         if (!world.getSettings().biomeConfigs[biomeId].netherFortressesEnabled) {
            return false;
         } else {
            return chunkZ == (var4 << 4) + 4 + rand.nextInt(8);
         }
      }
   }

   protected StructureStart b(int i, int j) {
      return new NetherFortressStart(this.c, this.b, i, j);
   }

   public void prepare(World world, int chunkX, int chunkZ, byte[] chunkArray) {
      this.a((IChunkProvider)null, world, chunkX, chunkZ, chunkArray);
   }

   public void place(World world, Random random, int chunkX, int chunkZ) {
      this.a(world, random, chunkX, chunkZ);
   }
}
