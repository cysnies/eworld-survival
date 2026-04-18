package com.khorn.terraincontrol.bukkit.structuregens;

import com.khorn.terraincontrol.LocalWorld;
import com.khorn.terraincontrol.bukkit.util.WorldHelper;
import com.khorn.terraincontrol.configuration.BiomeConfig;
import java.util.Random;
import net.minecraft.server.v1_6_R2.StructurePiece;
import net.minecraft.server.v1_6_R2.StructureStart;
import net.minecraft.server.v1_6_R2.World;
import net.minecraft.server.v1_6_R2.WorldGenJungleTemple;
import net.minecraft.server.v1_6_R2.WorldGenPyramidPiece;
import net.minecraft.server.v1_6_R2.WorldGenWitchHut;

public class RareBuildingStart extends StructureStart {
   // $FF: synthetic field
   private static int[] $SWITCH_TABLE$com$khorn$terraincontrol$configuration$BiomeConfig$RareBuildingType;

   public RareBuildingStart(World world, Random random, int chunkX, int chunkZ) {
      super();
      LocalWorld localWorld = WorldHelper.toLocalWorld(world);
      BiomeConfig biomeConfig = localWorld.getSettings().biomeConfigs[localWorld.getCalculatedBiomeId(chunkX * 16 + 8, chunkZ * 16 + 8)];
      StructurePiece building;
      switch (biomeConfig.rareBuildingType) {
         case disabled:
         default:
            building = null;
            break;
         case desertPyramid:
            building = new WorldGenPyramidPiece(random, chunkX * 16, chunkZ * 16);
            break;
         case jungleTemple:
            building = new WorldGenJungleTemple(random, chunkX * 16, chunkZ * 16);
            break;
         case swampHut:
            building = new WorldGenWitchHut(random, chunkX * 16, chunkZ * 16);
      }

      if (building != null) {
         this.a.add(building);
      }

      this.c();
   }

   // $FF: synthetic method
   static int[] $SWITCH_TABLE$com$khorn$terraincontrol$configuration$BiomeConfig$RareBuildingType() {
      int[] var10000 = $SWITCH_TABLE$com$khorn$terraincontrol$configuration$BiomeConfig$RareBuildingType;
      if (var10000 != null) {
         return var10000;
      } else {
         int[] var0 = new int[BiomeConfig.RareBuildingType.values().length];

         try {
            var0[BiomeConfig.RareBuildingType.desertPyramid.ordinal()] = 2;
         } catch (NoSuchFieldError var4) {
         }

         try {
            var0[BiomeConfig.RareBuildingType.disabled.ordinal()] = 1;
         } catch (NoSuchFieldError var3) {
         }

         try {
            var0[BiomeConfig.RareBuildingType.jungleTemple.ordinal()] = 3;
         } catch (NoSuchFieldError var2) {
         }

         try {
            var0[BiomeConfig.RareBuildingType.swampHut.ordinal()] = 4;
         } catch (NoSuchFieldError var1) {
         }

         $SWITCH_TABLE$com$khorn$terraincontrol$configuration$BiomeConfig$RareBuildingType = var0;
         return var0;
      }
   }
}
