package com.khorn.terraincontrol.bukkit.structuregens;

import java.util.LinkedList;
import java.util.List;
import java.util.Random;
import net.minecraft.server.v1_6_R2.StructurePiece;
import net.minecraft.server.v1_6_R2.StructureStart;
import net.minecraft.server.v1_6_R2.World;
import net.minecraft.server.v1_6_R2.WorldGenStrongholdPieces;
import net.minecraft.server.v1_6_R2.WorldGenStrongholdStart;

public class StrongholdStart extends StructureStart {
   public StrongholdStart(World world, Random random, int i, int j) {
      super();
      WorldGenStrongholdPieces.a();
      WorldGenStrongholdStart worldgenstrongholdstart = new WorldGenStrongholdStart(0, random, (i << 4) + 2, (j << 4) + 2);
      this.a.add(worldgenstrongholdstart);
      worldgenstrongholdstart.a(worldgenstrongholdstart, this.a, random);
      List arraylist = worldgenstrongholdstart.c;

      while(!arraylist.isEmpty()) {
         int k = random.nextInt(arraylist.size());
         StructurePiece structurepiece = (StructurePiece)arraylist.remove(k);
         structurepiece.a(worldgenstrongholdstart, this.a, random);
      }

      this.c();
      this.a(world, random, 10);
   }

   public LinkedList getComponents() {
      return this.b();
   }
}
