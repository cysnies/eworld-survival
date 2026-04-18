package com.khorn.terraincontrol.biomelayers.layers;

import com.khorn.terraincontrol.LocalWorld;
import com.khorn.terraincontrol.biomelayers.ArraysCache;
import com.khorn.terraincontrol.configuration.WorldConfig;
import java.awt.image.BufferedImage;
import java.awt.image.ImageObserver;
import java.io.File;
import java.io.IOException;
import javax.imageio.ImageIO;

public class LayerFromImage extends Layer {
   private int[] biomeMap;
   private int mapHeight;
   private int mapWidth;
   private int fillBiome = 0;
   private int xOffset;
   private int zOffset;
   private WorldConfig.ImageMode imageMode;

   public LayerFromImage(long paramLong, Layer _child, WorldConfig config, LocalWorld world) {
      super(paramLong);
      this.child = _child;
      this.xOffset = config.imageXOffset;
      this.zOffset = config.imageZOffset;
      this.imageMode = config.imageMode;
      this.fillBiome = world.getBiomeIdByName(config.imageFillBiome);

      try {
         File image = new File(config.SettingsDir, config.imageFile);
         BufferedImage map = ImageIO.read(image);
         this.mapHeight = map.getHeight((ImageObserver)null);
         this.mapWidth = map.getWidth((ImageObserver)null);
         int[] colorMap = new int[this.mapHeight * this.mapWidth];
         this.biomeMap = new int[this.mapHeight * this.mapWidth];
         map.getRGB(0, 0, this.mapWidth, this.mapHeight, colorMap, 0, this.mapWidth);

         for(int i = 0; i < colorMap.length; ++i) {
            int color = colorMap[i] & 16777215;
            if (config.biomeColorMap.containsKey(color)) {
               this.biomeMap[i] = (Integer)config.biomeColorMap.get(color);
            } else {
               this.biomeMap[i] = this.fillBiome;
            }
         }
      } catch (IOException ioexception) {
         ioexception.printStackTrace();
      }

   }

   protected int[] GetBiomes(int cacheId, int x, int z, int x_size, int z_size) {
      int[] arrayOfInt1 = ArraysCache.GetArray(cacheId, x_size * z_size);
      int[] arrayOfInt2 = null;
      if (this.child != null) {
         arrayOfInt2 = this.child.GetBiomes(cacheId, x, z, x_size, z_size);
      }

      for(int i = 0; i < z_size; ++i) {
         for(int t = 0; t < x_size; ++t) {
            if (this.imageMode == WorldConfig.ImageMode.Repeat) {
               int Buffer_x = this.mapWidth - 1 - Math.abs((z + i - this.zOffset) % this.mapWidth);
               int Buffer_z = Math.abs((x + t - this.xOffset) % this.mapHeight);
               arrayOfInt1[t + i * x_size] = this.biomeMap[Buffer_x + Buffer_z * this.mapWidth];
            } else {
               int Buffer_x = this.mapWidth - (z + i - this.zOffset);
               int Buffer_z = x + t - this.xOffset;
               if (Buffer_x >= 0 && Buffer_x < this.mapWidth && Buffer_z >= 0 && Buffer_z < this.mapHeight) {
                  arrayOfInt1[t + i * x_size] = this.biomeMap[Buffer_x + Buffer_z * this.mapWidth];
               } else if (arrayOfInt2 != null) {
                  arrayOfInt1[t + i * x_size] = arrayOfInt2[t + i * x_size];
               } else {
                  arrayOfInt1[t + i * x_size] = this.fillBiome;
               }
            }
         }
      }

      return arrayOfInt1;
   }
}
