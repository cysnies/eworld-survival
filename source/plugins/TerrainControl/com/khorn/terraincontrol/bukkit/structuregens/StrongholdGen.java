package com.khorn.terraincontrol.bukkit.structuregens;

import com.khorn.terraincontrol.bukkit.BukkitBiome;
import com.khorn.terraincontrol.configuration.BiomeConfig;
import com.khorn.terraincontrol.configuration.WorldConfig;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;
import net.minecraft.server.v1_6_R2.ChunkCoordIntPair;
import net.minecraft.server.v1_6_R2.ChunkPosition;
import net.minecraft.server.v1_6_R2.IChunkProvider;
import net.minecraft.server.v1_6_R2.StructureGenerator;
import net.minecraft.server.v1_6_R2.StructureStart;
import net.minecraft.server.v1_6_R2.World;
import net.minecraft.server.v1_6_R2.WorldGenStrongholdStart;

public class StrongholdGen extends StructureGenerator {
   private List allowedBiomes;
   private boolean ranBiomeCheck;
   private ChunkCoordIntPair[] structureCoords;
   private double distance;
   private int spread;

   public StrongholdGen(WorldConfig worldConfig) {
      super();
      this.distance = worldConfig.strongholdDistance;
      this.structureCoords = new ChunkCoordIntPair[worldConfig.strongholdCount];
      this.spread = worldConfig.strongholdSpread;
      this.allowedBiomes = new ArrayList();

      BiomeConfig[] var5;
      for(BiomeConfig biomeConfig : var5 = worldConfig.biomeConfigs) {
         if (biomeConfig != null && biomeConfig.strongholdsEnabled) {
            this.allowedBiomes.add(((BukkitBiome)biomeConfig.Biome).getHandle());
         }
      }

   }

   protected boolean a(int i, int j) {
      if (!this.ranBiomeCheck) {
         Random random = new Random();
         random.setSeed(this.c.getSeed());
         double d0 = random.nextDouble() * Math.PI * (double)2.0F;
         int k = 1;

         for(int l = 0; l < this.structureCoords.length; ++l) {
            double d1 = ((double)1.25F * (double)k + random.nextDouble()) * this.distance * (double)k;
            int i1 = (int)Math.round(Math.cos(d0) * d1);
            int j1 = (int)Math.round(Math.sin(d0) * d1);
            ArrayList arraylist = new ArrayList();
            Collections.addAll(arraylist, new List[]{this.allowedBiomes});
            ChunkPosition chunkposition = this.c.getWorldChunkManager().a((i1 << 4) + 8, (j1 << 4) + 8, 112, arraylist, random);
            if (chunkposition != null) {
               i1 = chunkposition.x >> 4;
               j1 = chunkposition.z >> 4;
            }

            this.structureCoords[l] = new ChunkCoordIntPair(i1, j1);
            d0 += (Math.PI * 2D) * (double)k / (double)this.spread;
            if (l == this.spread) {
               k += 2 + random.nextInt(5);
               this.spread += 1 + random.nextInt(2);
            }
         }

         this.ranBiomeCheck = true;
      }

      ChunkCoordIntPair[] achunkcoordintpair = this.structureCoords;

      for(ChunkCoordIntPair chunkcoordintpair : achunkcoordintpair) {
         if (i == chunkcoordintpair.x && j == chunkcoordintpair.z) {
            return true;
         }
      }

      return false;
   }

   protected List p_() {
      ArrayList arraylist = new ArrayList();
      ChunkCoordIntPair[] achunkcoordintpair = this.structureCoords;

      for(ChunkCoordIntPair chunkcoordintpair : achunkcoordintpair) {
         if (chunkcoordintpair != null) {
            arraylist.add(chunkcoordintpair.a(64));
         }
      }

      return arraylist;
   }

   protected StructureStart b(int chunkX, int chunkZ) {
      StrongholdStart strongholdStart;
      for(strongholdStart = new StrongholdStart(this.c, this.b, chunkX, chunkZ); strongholdStart.getComponents().isEmpty() || ((WorldGenStrongholdStart)strongholdStart.getComponents().get(0)).b == null; strongholdStart = new StrongholdStart(this.c, this.b, chunkX, chunkZ)) {
      }

      return strongholdStart;
   }

   public void prepare(World world, int chunkX, int chunkZ, byte[] chunkArray) {
      this.a((IChunkProvider)null, world, chunkX, chunkZ, chunkArray);
   }

   public void place(World world, Random random, int chunkX, int chunkZ) {
      this.a(world, random, chunkX, chunkZ);
   }
}
