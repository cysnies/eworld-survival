package com.khorn.terraincontrol.generator.resourcegens;

import com.khorn.terraincontrol.DefaultMaterial;
import com.khorn.terraincontrol.LocalWorld;
import com.khorn.terraincontrol.TerrainControl;
import com.khorn.terraincontrol.exception.InvalidConfigException;
import java.util.List;
import java.util.Random;

public class SmallLakeGen extends Resource {
   private final boolean[] BooleanBuffer = new boolean[2048];
   public int minAltitude;
   public int maxAltitude;

   public SmallLakeGen() {
      super();
   }

   public void spawn(LocalWorld world, Random rand, boolean villageInChunk, int x, int z) {
      if (!villageInChunk) {
         x -= 8;
         z -= 8;

         int y;
         for(y = rand.nextInt(this.maxAltitude - this.minAltitude) + this.minAltitude; y > 5 && world.isEmpty(x, y, z); --y) {
         }

         if (y > 4) {
            y -= 4;
            synchronized(this.BooleanBuffer) {
               boolean[] BooleanBuffer = new boolean[2048];
               int i = rand.nextInt(4) + 4;

               for(int j = 0; j < i; ++j) {
                  double d1 = rand.nextDouble() * (double)6.0F + (double)3.0F;
                  double d2 = rand.nextDouble() * (double)4.0F + (double)2.0F;
                  double d3 = rand.nextDouble() * (double)6.0F + (double)3.0F;
                  double d4 = rand.nextDouble() * ((double)16.0F - d1 - (double)2.0F) + (double)1.0F + d1 / (double)2.0F;
                  double d5 = rand.nextDouble() * ((double)8.0F - d2 - (double)4.0F) + (double)2.0F + d2 / (double)2.0F;
                  double d6 = rand.nextDouble() * ((double)16.0F - d3 - (double)2.0F) + (double)1.0F + d3 / (double)2.0F;

                  for(int k = 1; k < 15; ++k) {
                     for(int m = 1; m < 15; ++m) {
                        for(int n = 1; n < 7; ++n) {
                           double d7 = ((double)k - d4) / (d1 / (double)2.0F);
                           double d8 = ((double)n - d5) / (d2 / (double)2.0F);
                           double d9 = ((double)m - d6) / (d3 / (double)2.0F);
                           double d10 = d7 * d7 + d8 * d8 + d9 * d9;
                           if (!(d10 >= (double)1.0F)) {
                              BooleanBuffer[(k * 16 + m) * 8 + n] = true;
                           }
                        }
                     }
                  }
               }

               for(int j = 0; j < 16; ++j) {
                  for(int i1 = 0; i1 < 16; ++i1) {
                     for(int i2 = 0; i2 < 8; ++i2) {
                        boolean flag = !BooleanBuffer[(j * 16 + i1) * 8 + i2] && (j < 15 && BooleanBuffer[((j + 1) * 16 + i1) * 8 + i2] || j > 0 && BooleanBuffer[((j - 1) * 16 + i1) * 8 + i2] || i1 < 15 && BooleanBuffer[(j * 16 + i1 + 1) * 8 + i2] || i1 > 0 && BooleanBuffer[(j * 16 + (i1 - 1)) * 8 + i2] || i2 < 7 && BooleanBuffer[(j * 16 + i1) * 8 + i2 + 1] || i2 > 0 && BooleanBuffer[(j * 16 + i1) * 8 + (i2 - 1)]);
                        if (flag) {
                           DefaultMaterial localMaterial = world.getMaterial(x + j, y + i2, z + i1);
                           if (i2 >= 4 && localMaterial.isLiquid()) {
                              return;
                           }

                           if (i2 < 4 && !localMaterial.isSolid() && world.getTypeId(x + j, y + i2, z + i1) != this.blockId) {
                              return;
                           }
                        }
                     }
                  }
               }

               for(int j = 0; j < 16; ++j) {
                  for(int i1 = 0; i1 < 16; ++i1) {
                     for(int i2 = 0; i2 < 4; ++i2) {
                        if (BooleanBuffer[(j * 16 + i1) * 8 + i2]) {
                           world.setBlock(x + j, y + i2, z + i1, this.blockId, this.blockData);
                           BooleanBuffer[(j * 16 + i1) * 8 + i2] = false;
                        }
                     }

                     for(int var43 = 4; var43 < 8; ++var43) {
                        if (BooleanBuffer[(j * 16 + i1) * 8 + var43]) {
                           world.setBlock(x + j, y + var43, z + i1, 0, 0);
                           BooleanBuffer[(j * 16 + i1) * 8 + var43] = false;
                        }
                     }
                  }
               }

            }
         }
      }
   }

   public void load(List args) throws InvalidConfigException {
      this.assureSize(5, args);
      this.blockId = this.readBlockId((String)args.get(0));
      this.blockData = this.readBlockData((String)args.get(0));
      this.frequency = this.readInt((String)args.get(1), 1, 100);
      this.rarity = this.readRarity((String)args.get(2));
      this.minAltitude = this.readInt((String)args.get(3), TerrainControl.worldDepth, TerrainControl.worldHeight);
      this.maxAltitude = this.readInt((String)args.get(4), this.minAltitude + 1, TerrainControl.worldHeight);
   }

   public String makeString() {
      return "SmallLake(" + this.makeMaterial(this.blockId, this.blockData) + "," + this.frequency + "," + this.rarity + "," + this.minAltitude + "," + this.maxAltitude + ")";
   }
}
