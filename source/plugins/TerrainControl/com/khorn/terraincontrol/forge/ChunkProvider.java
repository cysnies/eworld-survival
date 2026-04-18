package com.khorn.terraincontrol.forge;

import com.khorn.terraincontrol.configuration.WorldConfig;
import com.khorn.terraincontrol.generator.ChunkProviderTC;
import com.khorn.terraincontrol.generator.ObjectSpawner;
import java.util.List;
import net.minecraft.block.BlockSand;
import net.minecraft.entity.EnumCreatureType;
import net.minecraft.util.IProgressUpdate;
import net.minecraft.world.ChunkPosition;
import net.minecraft.world.World;
import net.minecraft.world.biome.BiomeGenBase;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.chunk.IChunkProvider;
import net.minecraft.world.chunk.storage.ExtendedBlockStorage;

public class ChunkProvider implements IChunkProvider {
   private SingleWorld world;
   private World worldHandle;
   private boolean TestMode = false;
   private ChunkProviderTC generator;
   private ObjectSpawner spawner;

   public ChunkProvider(SingleWorld _world) {
      super();
      this.world = _world;
      this.worldHandle = _world.getWorld();
      this.TestMode = this.world.getSettings().ModeTerrain == WorldConfig.TerrainMode.TerrainTest;
      this.generator = new ChunkProviderTC(this.world.getSettings(), this.world);
      this.spawner = new ObjectSpawner(this.world.getSettings(), this.world);
   }

   public boolean func_73149_a(int i, int i1) {
      return true;
   }

   public Chunk func_73154_d(int chunkX, int chunkZ) {
      Chunk chunk = new Chunk(this.worldHandle, chunkX, chunkZ);
      byte[] BlockArray = this.generator.generate(chunkX, chunkZ);
      ExtendedBlockStorage[] sections = chunk.func_76587_i();
      int i1 = BlockArray.length / 256;

      for(int blockX = 0; blockX < 16; ++blockX) {
         for(int blockZ = 0; blockZ < 16; ++blockZ) {
            for(int blockY = 0; blockY < i1; ++blockY) {
               int block = BlockArray[blockX << this.world.getHeightBits() + 4 | blockZ << this.world.getHeightBits() | blockY];
               if (block != 0) {
                  int sectionId = blockY >> 4;
                  if (sections[sectionId] == null) {
                     sections[sectionId] = new ExtendedBlockStorage(sectionId << 4, !chunk.field_76637_e.field_73011_w.field_76576_e);
                  }

                  sections[sectionId].func_76655_a(blockX, blockY & 15, blockZ, block & 255);
               }
            }
         }
      }

      this.world.FillChunkForBiomes(chunk, chunkX, chunkZ);
      chunk.func_76603_b();
      return chunk;
   }

   public Chunk func_73158_c(int i, int i1) {
      return this.func_73154_d(i, i1);
   }

   public void func_73153_a(IChunkProvider ChunkProvider, int x, int z) {
      if (!this.TestMode) {
         BlockSand.field_72192_a = true;
         this.world.LoadChunk(x, z);
         this.spawner.populate(x, z);
         BlockSand.field_72192_a = false;
      }
   }

   public boolean func_73151_a(boolean b, IProgressUpdate il) {
      return true;
   }

   public boolean func_73156_b() {
      return false;
   }

   public boolean func_73157_c() {
      return true;
   }

   public String func_73148_d() {
      return "TerrainControlLevelSource";
   }

   public List func_73155_a(EnumCreatureType paramaca, int paramInt1, int paramInt2, int paramInt3) {
      BiomeGenBase Biome = this.worldHandle.func_72807_a(paramInt1, paramInt3);
      return Biome == null ? null : Biome.func_76747_a(paramaca);
   }

   public ChunkPosition func_73150_a(World world, String s, int x, int y, int z) {
      return "Stronghold".equals(s) && this.world.strongholdGen != null ? this.world.strongholdGen.func_75050_a(world, x, y, z) : null;
   }

   public int func_73152_e() {
      return 0;
   }

   public void func_82695_e(int chunkX, int chunkZ) {
      if (this.world.mineshaftGen != null) {
         this.world.mineshaftGen.func_75036_a(this, this.world.getWorld(), chunkX, chunkZ, (byte[])null);
      }

      if (this.world.villageGen != null) {
         this.world.villageGen.func_75036_a(this, this.world.getWorld(), chunkX, chunkZ, (byte[])null);
      }

      if (this.world.strongholdGen != null) {
         this.world.strongholdGen.func_75036_a(this, this.world.getWorld(), chunkX, chunkZ, (byte[])null);
      }

      if (this.world.rareBuildingGen != null) {
         this.world.rareBuildingGen.func_75036_a(this, this.world.getWorld(), chunkX, chunkZ, (byte[])null);
      }

      if (this.world.netherFortressGen != null) {
         this.world.netherFortressGen.func_75036_a(this, this.world.getWorld(), chunkX, chunkZ, (byte[])null);
      }

   }

   public void func_104112_b() {
   }
}
