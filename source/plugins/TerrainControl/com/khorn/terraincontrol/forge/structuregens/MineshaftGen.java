package com.khorn.terraincontrol.forge.structuregens;

import com.khorn.terraincontrol.LocalWorld;
import com.khorn.terraincontrol.forge.util.WorldHelper;
import net.minecraft.world.gen.structure.MapGenStructure;
import net.minecraft.world.gen.structure.StructureMineshaftStart;
import net.minecraft.world.gen.structure.StructureStart;

public class MineshaftGen extends MapGenStructure {
   public MineshaftGen() {
      super();
   }

   protected boolean func_75047_a(int chunkX, int chunkZ) {
      if (this.field_75038_b.nextInt(80) < Math.max(Math.abs(chunkX), Math.abs(chunkZ))) {
         LocalWorld world = WorldHelper.toLocalWorld(this.field_75039_c);
         int biomeId = world.getCalculatedBiomeId(chunkX * 16 + 8, chunkZ * 16 + 8);
         if (this.field_75038_b.nextDouble() * (double)100.0F < world.getSettings().biomeConfigs[biomeId].mineshaftsRarity) {
            return true;
         }
      }

      return false;
   }

   protected StructureStart func_75049_b(int par1, int par2) {
      return new StructureMineshaftStart(this.field_75039_c, this.field_75038_b, par1, par2);
   }
}
