package com.khorn.terraincontrol.forge.structuregens;

import com.khorn.terraincontrol.configuration.BiomeConfig;
import com.khorn.terraincontrol.configuration.WorldConfig;
import com.khorn.terraincontrol.forge.Biome;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;
import net.minecraft.world.ChunkCoordIntPair;
import net.minecraft.world.ChunkPosition;
import net.minecraft.world.gen.structure.ComponentStrongholdStairs2;
import net.minecraft.world.gen.structure.MapGenStructure;
import net.minecraft.world.gen.structure.StructureStart;

public class StrongholdGen extends MapGenStructure {
   private List allowedBiomeGenBases;
   private boolean ranBiomeCheck;
   private ChunkCoordIntPair[] structureCoords;
   private double distance;
   private int spread;

   public StrongholdGen(WorldConfig worldConfig) {
      super();
      this.distance = worldConfig.strongholdDistance;
      this.structureCoords = new ChunkCoordIntPair[worldConfig.strongholdCount];
      this.spread = worldConfig.strongholdSpread;
      this.allowedBiomeGenBases = new ArrayList();

      for(BiomeConfig biomeConfig : worldConfig.biomeConfigs) {
         if (biomeConfig != null && biomeConfig.strongholdsEnabled) {
            this.allowedBiomeGenBases.add(((Biome)biomeConfig.Biome).getHandle());
         }
      }

   }

   protected boolean func_75047_a(int par1, int par2) {
      if (!this.ranBiomeCheck) {
         Random random = new Random();
         random.setSeed(this.field_75039_c.func_72905_C());
         double randomNumBetween0and2PI = random.nextDouble() * Math.PI * (double)2.0F;
         int var6 = 1;

         for(int i = 0; i < this.structureCoords.length; ++i) {
            double var8 = ((double)1.25F * (double)var6 + random.nextDouble()) * this.distance * (double)var6;
            int var10 = (int)Math.round(Math.cos(randomNumBetween0and2PI) * var8);
            int var11 = (int)Math.round(Math.sin(randomNumBetween0and2PI) * var8);
            ArrayList var12 = new ArrayList();
            Collections.addAll(var12, new List[]{this.allowedBiomeGenBases});
            ChunkPosition var13 = this.field_75039_c.func_72959_q().func_76941_a((var10 << 4) + 8, (var11 << 4) + 8, 112, var12, random);
            if (var13 != null) {
               var10 = var13.field_76930_a >> 4;
               var11 = var13.field_76929_c >> 4;
            }

            this.structureCoords[i] = new ChunkCoordIntPair(var10, var11);
            randomNumBetween0and2PI += (Math.PI * 2D) * (double)var6 / (double)this.spread;
            if (i == this.spread) {
               var6 += 2 + random.nextInt(5);
               this.spread += 1 + random.nextInt(2);
            }
         }

         this.ranBiomeCheck = true;
      }

      ChunkCoordIntPair[] structureCoords = this.structureCoords;

      for(ChunkCoordIntPair structureCoord : structureCoords) {
         if (par1 == structureCoord.field_77276_a && par2 == structureCoord.field_77275_b) {
            return true;
         }
      }

      return false;
   }

   protected List func_75052_o_() {
      ArrayList<ChunkPosition> chunkPositions = new ArrayList();

      for(ChunkCoordIntPair structureCoord : this.structureCoords) {
         if (structureCoord != null) {
            chunkPositions.add(structureCoord.func_77271_a(64));
         }
      }

      return chunkPositions;
   }

   protected StructureStart func_75049_b(int par1, int par2) {
      StrongholdStart start;
      for(start = new StrongholdStart(this.field_75039_c, this.field_75038_b, par1, par2); start.func_75073_b().isEmpty() || ((ComponentStrongholdStairs2)start.func_75073_b().get(0)).field_75025_b == null; start = new StrongholdStart(this.field_75039_c, this.field_75038_b, par1, par2)) {
      }

      return start;
   }
}
