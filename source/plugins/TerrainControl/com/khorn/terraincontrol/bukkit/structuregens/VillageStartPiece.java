package com.khorn.terraincontrol.bukkit.structuregens;

import com.khorn.terraincontrol.LocalWorld;
import com.khorn.terraincontrol.TerrainControl;
import com.khorn.terraincontrol.bukkit.util.WorldHelper;
import com.khorn.terraincontrol.configuration.BiomeConfig;
import java.lang.reflect.Field;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;
import net.minecraft.server.v1_6_R2.BiomeBase;
import net.minecraft.server.v1_6_R2.World;
import net.minecraft.server.v1_6_R2.WorldChunkManager;
import net.minecraft.server.v1_6_R2.WorldGenVillageStartPiece;

public class VillageStartPiece extends WorldGenVillageStartPiece {
   public final WorldChunkManager worldChunkManager;

   public VillageStartPiece(World world, int i, Random random, int blockX, int blockZ, List arraylist, int size) {
      super(world.getWorldChunkManager(), i, random, blockX, blockZ, arraylist, size);
      this.worldChunkManager = world.getWorldChunkManager();
      BiomeBase currentBiomeGenBase = this.worldChunkManager.getBiome(blockX, blockZ);
      LocalWorld worldTC = WorldHelper.toLocalWorld(world);
      BiomeConfig config = worldTC.getSettings().biomeConfigs[currentBiomeGenBase.id];
      this.setSandstoneVillage(config.villageType == BiomeConfig.VillageType.sandstone);
      this.k = this;
   }

   private void setSandstoneVillage(boolean sandstoneVillage) {
      Field[] var5;
      for(Field field : var5 = WorldGenVillageStartPiece.class.getFields()) {
         if (field.getType().toString().equals("boolean")) {
            try {
               field.setAccessible(true);
               field.setBoolean(this, sandstoneVillage);
               break;
            } catch (Exception e) {
               TerrainControl.log("Cannot make village a sandstone village!");
               e.printStackTrace();
            }
         }
      }

   }

   public WorldChunkManager d() {
      return this.worldChunkManager;
   }

   public void buildComponent(VillageStartPiece startPiece, LinkedList list, Random random) {
      this.a(startPiece, list, random);
   }

   public List getPiecesListJ() {
      return this.j;
   }

   public List getPiecesListI() {
      return this.i;
   }
}
