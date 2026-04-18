package com.khorn.terraincontrol.bukkit.structuregens;

import java.util.List;
import java.util.Random;
import net.minecraft.server.v1_6_R2.StructurePiece;
import net.minecraft.server.v1_6_R2.StructureStart;
import net.minecraft.server.v1_6_R2.World;
import net.minecraft.server.v1_6_R2.WorldGenVillagePieces;
import net.minecraft.server.v1_6_R2.WorldGenVillageRoadPiece;

public class VillageStart extends StructureStart {
   private boolean hasMoreThanTwoComponents = false;

   public VillageStart(World world, Random random, int chunkX, int chunkZ, int size) {
      super();
      List<StructurePiece> villagePieces = WorldGenVillagePieces.a(random, size);
      VillageStartPiece startPiece = new VillageStartPiece(world, 0, random, (chunkX << 4) + 2, (chunkZ << 4) + 2, villagePieces, size);
      this.a.add(startPiece);
      startPiece.buildComponent(startPiece, this.a, random);
      List<StructurePiece> arraylist1 = startPiece.getPiecesListJ();
      List<StructurePiece> arraylist2 = startPiece.getPiecesListI();

      while(!arraylist1.isEmpty() || !arraylist2.isEmpty()) {
         if (arraylist1.isEmpty()) {
            int componentCount = random.nextInt(arraylist2.size());
            StructurePiece structurepiece = (StructurePiece)arraylist2.remove(componentCount);
            structurepiece.a(startPiece, this.a, random);
         } else {
            int componentCount = random.nextInt(arraylist1.size());
            StructurePiece structurepiece = (StructurePiece)arraylist1.remove(componentCount);
            structurepiece.a(startPiece, this.a, random);
         }
      }

      this.c();
      int componentCount = 0;

      for(Object anA : this.a) {
         StructurePiece structurepiece1 = (StructurePiece)anA;
         if (!(structurepiece1 instanceof WorldGenVillageRoadPiece)) {
            ++componentCount;
         }
      }

      this.hasMoreThanTwoComponents = componentCount > 2;
   }

   public boolean d() {
      return this.hasMoreThanTwoComponents;
   }
}
