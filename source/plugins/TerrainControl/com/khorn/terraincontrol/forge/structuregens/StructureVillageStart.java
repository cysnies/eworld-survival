package com.khorn.terraincontrol.forge.structuregens;

import java.util.List;
import java.util.Random;
import net.minecraft.world.World;
import net.minecraft.world.gen.structure.ComponentVillageRoadPiece;
import net.minecraft.world.gen.structure.StructureComponent;
import net.minecraft.world.gen.structure.StructureStart;
import net.minecraft.world.gen.structure.StructureVillagePieces;

class StructureVillageStart extends StructureStart {
   private boolean hasMoreThanTwoComponents = false;

   public StructureVillageStart(World world, Random random, int chunkX, int chunkZ, int size) {
      super();
      List<StructureComponent> villagePieces = StructureVillagePieces.func_75084_a(random, size);
      VillageStartPiece startPiece = new VillageStartPiece(world, 0, random, (chunkX << 4) + 2, (chunkZ << 4) + 2, villagePieces, size);
      this.field_75075_a.add(startPiece);
      startPiece.func_74861_a(startPiece, this.field_75075_a, random);
      List var8 = startPiece.field_74930_j;
      List var9 = startPiece.field_74932_i;

      while(!var8.isEmpty() || !var9.isEmpty()) {
         if (var8.isEmpty()) {
            int var10 = random.nextInt(var9.size());
            StructureComponent var11 = (StructureComponent)var9.remove(var10);
            var11.func_74861_a(startPiece, this.field_75075_a, random);
         } else {
            int var10 = random.nextInt(var8.size());
            StructureComponent var11 = (StructureComponent)var8.remove(var10);
            var11.func_74861_a(startPiece, this.field_75075_a, random);
         }
      }

      this.func_75072_c();
      int var10 = 0;

      for(Object component : this.field_75075_a) {
         StructureComponent var12 = (StructureComponent)component;
         if (!(var12 instanceof ComponentVillageRoadPiece)) {
            ++var10;
         }
      }

      this.hasMoreThanTwoComponents = var10 > 2;
   }

   public boolean func_75069_d() {
      return this.hasMoreThanTwoComponents;
   }
}
