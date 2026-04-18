package com.khorn.terraincontrol.forge.structuregens;

import com.khorn.terraincontrol.LocalWorld;
import com.khorn.terraincontrol.forge.util.WorldHelper;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.entity.monster.EntityBlaze;
import net.minecraft.entity.monster.EntityMagmaCube;
import net.minecraft.entity.monster.EntityPigZombie;
import net.minecraft.entity.monster.EntitySkeleton;
import net.minecraft.world.biome.SpawnListEntry;
import net.minecraft.world.gen.structure.MapGenStructure;
import net.minecraft.world.gen.structure.StructureStart;

public class NetherFortressGen extends MapGenStructure {
   public List spawnList = new ArrayList();

   public NetherFortressGen() {
      super();
      this.spawnList.add(new SpawnListEntry(EntityBlaze.class, 10, 2, 3));
      this.spawnList.add(new SpawnListEntry(EntityPigZombie.class, 5, 4, 4));
      this.spawnList.add(new SpawnListEntry(EntitySkeleton.class, 10, 4, 4));
      this.spawnList.add(new SpawnListEntry(EntityMagmaCube.class, 3, 4, 4));
   }

   public List getSpawnList() {
      return this.spawnList;
   }

   protected boolean func_75047_a(int chunkX, int chunkZ) {
      int var3 = chunkX >> 4;
      int var4 = chunkZ >> 4;
      this.field_75038_b.setSeed((long)(var3 ^ var4 << 4) ^ this.field_75039_c.func_72905_C());
      this.field_75038_b.nextInt();
      if (this.field_75038_b.nextInt(3) != 0) {
         return false;
      } else if (chunkX != (var3 << 4) + 4 + this.field_75038_b.nextInt(8)) {
         return false;
      } else {
         LocalWorld world = WorldHelper.toLocalWorld(this.field_75039_c);
         int biomeId = world.getCalculatedBiomeId(chunkX * 16 + 8, chunkZ * 16 + 8);
         if (!world.getSettings().biomeConfigs[biomeId].netherFortressesEnabled) {
            return false;
         } else {
            return chunkZ == (var4 << 4) + 4 + this.field_75038_b.nextInt(8);
         }
      }
   }

   protected StructureStart func_75049_b(int chunkX, int chunkZ) {
      return new NetherFortressStart(this.field_75039_c, this.field_75038_b, chunkX, chunkZ);
   }
}
