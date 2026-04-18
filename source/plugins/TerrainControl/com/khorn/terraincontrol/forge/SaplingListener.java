package com.khorn.terraincontrol.forge;

import com.khorn.terraincontrol.LocalWorld;
import com.khorn.terraincontrol.configuration.BiomeConfig;
import com.khorn.terraincontrol.forge.util.WorldHelper;
import com.khorn.terraincontrol.generator.resourcegens.SaplingGen;
import com.khorn.terraincontrol.generator.resourcegens.SaplingType;
import java.util.Random;
import net.minecraft.block.Block;
import net.minecraft.block.BlockSapling;
import net.minecraft.world.World;
import net.minecraftforge.event.ForgeSubscribe;
import net.minecraftforge.event.Event.Result;
import net.minecraftforge.event.entity.player.BonemealEvent;
import net.minecraftforge.event.terraingen.SaplingGrowTreeEvent;

public class SaplingListener {
   public SaplingListener() {
      super();
   }

   @ForgeSubscribe
   public void onSaplingGrow(SaplingGrowTreeEvent event) {
      int x = event.x;
      int y = event.y;
      int z = event.z;
      World world = event.world;
      LocalWorld localWorld = WorldHelper.toLocalWorld(world);
      if (localWorld != null) {
         int blockId = world.func_72798_a(x, y, z);
         BlockSapling saplingBlock = (BlockSapling)Block.field_71987_y;
         if (blockId == saplingBlock.field_71990_ca) {
            int blockData = world.func_72805_g(x, y, z) & 3;
            SaplingType treeToGrow = null;
            boolean hugeJungleTreeHasGrown = false;
            int jungleOffsetX = 0;
            int jungleOffsetZ = 0;
            if (blockData == 1) {
               treeToGrow = SaplingType.Redwood;
            } else if (blockData == 2) {
               treeToGrow = SaplingType.Birch;
            } else if (blockData == 3) {
               for(jungleOffsetX = 0; jungleOffsetX >= -1; --jungleOffsetX) {
                  for(jungleOffsetZ = 0; jungleOffsetZ >= -1; --jungleOffsetZ) {
                     if (saplingBlock.func_72268_e(world, x + jungleOffsetX, y, z + jungleOffsetZ, 3) && saplingBlock.func_72268_e(world, x + jungleOffsetX + 1, y, z + jungleOffsetZ, 3) && saplingBlock.func_72268_e(world, x + jungleOffsetX, y, z + jungleOffsetZ + 1, 3) && saplingBlock.func_72268_e(world, x + jungleOffsetX + 1, y, z + jungleOffsetZ + 1, 3)) {
                        treeToGrow = SaplingType.BigJungle;
                        hugeJungleTreeHasGrown = true;
                        break;
                     }
                  }

                  if (treeToGrow != null) {
                     break;
                  }
               }

               if (treeToGrow == null) {
                  jungleOffsetZ = 0;
                  jungleOffsetX = 0;
                  treeToGrow = SaplingType.SmallJungle;
               }
            } else {
               treeToGrow = SaplingType.Oak;
            }

            SaplingGen saplingGen = this.getSaplingGen(localWorld, treeToGrow, x, z);
            if (saplingGen != null) {
               event.setResult(Result.DENY);
               if (hugeJungleTreeHasGrown) {
                  world.func_94571_i(x + jungleOffsetX, y, z + jungleOffsetZ);
                  world.func_94571_i(x + jungleOffsetX + 1, y, z + jungleOffsetZ);
                  world.func_94571_i(x + jungleOffsetX, y, z + jungleOffsetZ + 1);
                  world.func_94571_i(x + jungleOffsetX + 1, y, z + jungleOffsetZ + 1);
               } else {
                  world.func_94571_i(x, y, z);
               }

               boolean saplingGrown = false;

               for(int i = 0; i < 10; ++i) {
                  if (saplingGen.growSapling(localWorld, new Random(), x + jungleOffsetX, y, z + jungleOffsetZ)) {
                     saplingGrown = true;
                     break;
                  }
               }

               if (!saplingGrown) {
                  if (hugeJungleTreeHasGrown) {
                     world.func_72832_d(x + jungleOffsetX, y, z + jungleOffsetZ, blockId, blockData, 4);
                     world.func_72832_d(x + jungleOffsetX + 1, y, z + jungleOffsetZ, blockId, blockData, 4);
                     world.func_72832_d(x + jungleOffsetX, y, z + jungleOffsetZ + 1, blockId, blockData, 4);
                     world.func_72832_d(x + jungleOffsetX + 1, y, z + jungleOffsetZ + 1, blockId, blockData, 4);
                  } else {
                     world.func_72832_d(x, y, z, blockId, blockData, 4);
                  }
               }

            }
         }
      }
   }

   @ForgeSubscribe
   public void onBonemealUse(BonemealEvent event) {
      LocalWorld localWorld = WorldHelper.toLocalWorld(event.world);
      if (localWorld != null) {
         SaplingGen gen = null;
         if (event.ID == Block.field_72103_ag.field_71990_ca) {
            gen = this.getSaplingGen(localWorld, SaplingType.RedMushroom, event.X, event.Z);
         } else if (event.ID == Block.field_72109_af.field_71990_ca) {
            gen = this.getSaplingGen(localWorld, SaplingType.BrownMushroom, event.X, event.Z);
         }

         if (gen != null) {
            event.setResult(Result.ALLOW);
            event.world.func_94571_i(event.X, event.Y, event.Z);
            boolean mushroomGrown = false;
            Random random = new Random();

            for(int i = 0; i < 10; ++i) {
               if (gen.growSapling(localWorld, random, event.X, event.Y, event.Z)) {
                  mushroomGrown = true;
                  break;
               }
            }

            if (!mushroomGrown) {
               event.world.func_72832_d(event.X, event.Y, event.Z, event.ID, 0, 2);
            }

         }
      }
   }

   public SaplingGen getSaplingGen(LocalWorld world, SaplingType type, int x, int z) {
      BiomeConfig biomeConfig = world.getSettings().biomeConfigs[world.getBiomeId(x, z)];
      return biomeConfig == null ? null : biomeConfig.getSaplingGen(type);
   }
}
