package com.khorn.terraincontrol.forge.structuregens;

import com.khorn.terraincontrol.LocalWorld;
import com.khorn.terraincontrol.TerrainControl;
import com.khorn.terraincontrol.configuration.BiomeConfig;
import com.khorn.terraincontrol.forge.util.WorldHelper;
import java.lang.reflect.Field;
import java.util.List;
import java.util.Random;
import net.minecraft.world.World;
import net.minecraft.world.biome.BiomeGenBase;
import net.minecraft.world.biome.WorldChunkManager;
import net.minecraft.world.gen.structure.ComponentVillageStartPiece;

public class VillageStartPiece extends ComponentVillageStartPiece {
   public final WorldChunkManager worldChunkManager;

   public VillageStartPiece(World world, int par2, Random par3Random, int par4, int par5, List par6ArrayList, int size) {
      super(world.func_72959_q(), par2, par3Random, par4, par5, par6ArrayList, size);
      this.worldChunkManager = world.func_72959_q();
      BiomeGenBase currentBiomeGenBase = this.worldChunkManager.func_76935_a(par4, par5);
      LocalWorld worldTC = WorldHelper.toLocalWorld(world);
      BiomeConfig config = worldTC.getSettings().biomeConfigs[currentBiomeGenBase.field_76756_M];
      this.setSandstoneVillage(config.villageType == BiomeConfig.VillageType.sandstone);
      this.field_74897_k = this;
   }

   private void setSandstoneVillage(boolean sandstoneVillage) {
      for(Field field : ComponentVillageStartPiece.class.getFields()) {
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

   public WorldChunkManager func_74925_d() {
      return this.worldChunkManager;
   }
}
