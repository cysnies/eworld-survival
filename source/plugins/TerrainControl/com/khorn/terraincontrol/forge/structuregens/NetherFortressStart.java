package com.khorn.terraincontrol.forge.structuregens;

import java.util.ArrayList;
import java.util.Random;
import net.minecraft.world.World;
import net.minecraft.world.gen.structure.ComponentNetherBridgeStartPiece;
import net.minecraft.world.gen.structure.StructureComponent;
import net.minecraft.world.gen.structure.StructureStart;

public class NetherFortressStart extends StructureStart {
   public NetherFortressStart(World world, Random random, int chunkX, int chunkZ) {
      super();
      ComponentNetherBridgeStartPiece var5 = new ComponentNetherBridgeStartPiece(random, (chunkX << 4) + 2, (chunkZ << 4) + 2);
      this.field_75075_a.add(var5);
      var5.func_74861_a(var5, this.field_75075_a, random);
      ArrayList list = var5.field_74967_d;

      while(!list.isEmpty()) {
         int var7 = random.nextInt(list.size());
         StructureComponent var8 = (StructureComponent)list.remove(var7);
         var8.func_74861_a(var5, this.field_75075_a, random);
      }

      this.func_75072_c();
      this.func_75070_a(world, random, 48, 70);
   }
}
