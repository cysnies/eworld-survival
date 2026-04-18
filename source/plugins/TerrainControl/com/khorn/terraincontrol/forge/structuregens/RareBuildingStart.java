package com.khorn.terraincontrol.forge.structuregens;

import com.khorn.terraincontrol.LocalWorld;
import com.khorn.terraincontrol.configuration.BiomeConfig;
import com.khorn.terraincontrol.forge.util.WorldHelper;
import java.util.Random;
import net.minecraft.world.World;
import net.minecraft.world.gen.structure.ComponentScatteredFeatureDesertPyramid;
import net.minecraft.world.gen.structure.ComponentScatteredFeatureJunglePyramid;
import net.minecraft.world.gen.structure.ComponentScatteredFeatureSwampHut;
import net.minecraft.world.gen.structure.StructureComponent;
import net.minecraft.world.gen.structure.StructureStart;

public class RareBuildingStart extends StructureStart {
   public RareBuildingStart(World world, Random random, int chunkX, int chunkZ) {
      super();
      LocalWorld localWorld = WorldHelper.toLocalWorld(world);
      BiomeConfig biomeConfig = localWorld.getSettings().biomeConfigs[localWorld.getCalculatedBiomeId(chunkX * 16 + 8, chunkZ * 16 + 8)];
      StructureComponent building;
      switch (biomeConfig.rareBuildingType) {
         case desertPyramid:
            building = new ComponentScatteredFeatureDesertPyramid(random, chunkX * 16, chunkZ * 16);
            break;
         case jungleTemple:
            building = new ComponentScatteredFeatureJunglePyramid(random, chunkX * 16, chunkZ * 16);
            break;
         case swampHut:
            building = new ComponentScatteredFeatureSwampHut(random, chunkX * 16, chunkZ * 16);
            break;
         case disabled:
         default:
            building = null;
      }

      if (building != null) {
         this.field_75075_a.add(building);
      }

      this.func_75072_c();
   }
}
