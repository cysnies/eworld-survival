package com.khorn.terraincontrol.generator.resourcegens;

import com.khorn.terraincontrol.DefaultMaterial;
import com.khorn.terraincontrol.LocalWorld;
import com.khorn.terraincontrol.TerrainControl;
import com.khorn.terraincontrol.exception.InvalidConfigException;
import com.khorn.terraincontrol.util.MathHelper;
import java.util.List;
import java.util.Random;

public class UndergroundLakeGen extends Resource {
   private int minSize;
   private int maxSize;
   private int minAltitude;
   private int maxAltitude;

   public UndergroundLakeGen() {
      super();
   }

   public void spawn(LocalWorld world, Random rand, boolean villageInChunk, int x, int z) {
      int y = rand.nextInt(this.maxAltitude - this.minAltitude) + this.minAltitude;
      if (y < world.getHighestBlockYAt(x, z)) {
         int size = rand.nextInt(this.maxSize - this.minSize) + this.minSize;
         float mPi = rand.nextFloat() * 3.141593F;
         double x1 = (double)((float)(x + 8) + MathHelper.sin(mPi) * (float)size / 8.0F);
         double x2 = (double)((float)(x + 8) - MathHelper.sin(mPi) * (float)size / 8.0F);
         double z1 = (double)((float)(z + 8) + MathHelper.cos(mPi) * (float)size / 8.0F);
         double z2 = (double)((float)(z + 8) - MathHelper.cos(mPi) * (float)size / 8.0F);
         double y1 = (double)(y + rand.nextInt(3) + 2);
         double y2 = (double)(y + rand.nextInt(3) + 2);

         for(int i = 0; i <= size; ++i) {
            double xAdjusted = x1 + (x2 - x1) * (double)i / (double)size;
            double yAdjusted = y1 + (y2 - y1) * (double)i / (double)size;
            double zAdjusted = z1 + (z2 - z1) * (double)i / (double)size;
            double horizontalSizeMultiplier = rand.nextDouble() * (double)size / (double)16.0F;
            double verticalSizeMultiplier = rand.nextDouble() * (double)size / (double)32.0F;
            double horizontalSize = (double)(MathHelper.sin((float)i * 3.141593F / (float)size) + 1.0F) * horizontalSizeMultiplier + (double)1.0F;
            double verticalSize = (double)(MathHelper.sin((float)i * 3.141593F / (float)size) + 1.0F) * verticalSizeMultiplier + (double)1.0F;

            for(int xLake = (int)(xAdjusted - horizontalSize / (double)2.0F); xLake <= (int)(xAdjusted + horizontalSize / (double)2.0F); ++xLake) {
               for(int yLake = (int)(yAdjusted - verticalSize / (double)2.0F); yLake <= (int)(yAdjusted + verticalSize / (double)2.0F); ++yLake) {
                  for(int zLake = (int)(zAdjusted - horizontalSize / (double)2.0F); zLake <= (int)(zAdjusted + horizontalSize / (double)2.0F); ++zLake) {
                     if (world.getTypeId(xLake, yLake, zLake) != 0) {
                        double xBounds = ((double)xLake + (double)0.5F - xAdjusted) / (horizontalSize / (double)2.0F);
                        double yBounds = ((double)yLake + (double)0.5F - yAdjusted) / (verticalSize / (double)2.0F);
                        double zBounds = ((double)zLake + (double)0.5F - zAdjusted) / (horizontalSize / (double)2.0F);
                        if (!(xBounds * xBounds + yBounds * yBounds + zBounds * zBounds >= (double)1.0F)) {
                           int uBlock = world.getTypeId(xLake, yLake - 1, zLake);
                           if (uBlock != 0) {
                              world.setBlock(xLake, yLake, zLake, DefaultMaterial.WATER.id, 0, false, false, false);
                           } else {
                              world.setBlock(xLake, yLake, zLake, 0, 0, false, false, false);
                           }
                        }
                     }
                  }
               }
            }
         }

      }
   }

   public void load(List args) throws InvalidConfigException {
      this.blockId = DefaultMaterial.WATER.id;
      this.assureSize(6, args);
      this.minSize = this.readInt((String)args.get(0), 1, 25);
      this.maxSize = this.readInt((String)args.get(1), this.minSize, 60);
      this.frequency = this.readInt((String)args.get(2), 1, 100);
      this.rarity = this.readRarity((String)args.get(3));
      this.minAltitude = this.readInt((String)args.get(4), TerrainControl.worldDepth, TerrainControl.worldHeight);
      this.maxAltitude = this.readInt((String)args.get(5), this.minAltitude + 1, TerrainControl.worldHeight);
   }

   public String makeString() {
      return "UnderGroundLake(" + this.minSize + "," + this.maxSize + "," + this.frequency + "," + this.rarity + "," + this.minAltitude + "," + this.maxAltitude + ")";
   }
}
