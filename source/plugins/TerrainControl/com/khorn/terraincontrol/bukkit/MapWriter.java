package com.khorn.terraincontrol.bukkit;

import com.khorn.terraincontrol.TerrainControl;
import com.khorn.terraincontrol.bukkit.commands.BaseCommand;
import com.khorn.terraincontrol.configuration.BiomeConfig;
import com.sun.imageio.plugins.png.PNGImageWriter;
import com.sun.imageio.plugins.png.PNGImageWriterSpi;
import java.awt.Color;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileOutputStream;
import java.util.logging.Level;
import javax.imageio.stream.FileCacheImageOutputStream;
import javax.imageio.stream.ImageOutputStream;
import net.minecraft.server.v1_6_R2.BiomeBase;
import net.minecraft.server.v1_6_R2.World;
import org.bukkit.command.CommandSender;

public class MapWriter implements Runnable {
   public static final int[] defaultColors = new int[]{3355647, 10066176, 16763955, 3355392, 65280, 30464, 10079334, 52428, 0, 0, 16777215, 6750207, 13421772, 13408614, 16724940, 16751001, 16776960, 10053120, 39168, 13056, 6710784};
   public static boolean isWorking = false;
   private TCPlugin plugin;
   private World world;
   private int size;
   private CommandSender sender;
   private Angle angle;
   private int offsetX;
   private int offsetZ;
   private String label;
   // $FF: synthetic field
   private static int[] $SWITCH_TABLE$com$khorn$terraincontrol$bukkit$MapWriter$Angle;

   public MapWriter(TCPlugin _plugin, World _world, int _size, Angle _angle, CommandSender _sender, int _offsetX, int _offsetZ, String _label) {
      super();
      this.plugin = _plugin;
      this.world = _world;
      this.size = _size;
      this.sender = _sender;
      this.angle = _angle;
      this.offsetX = _offsetX;
      this.offsetZ = _offsetZ;
      this.label = _label;
   }

   public void run() {
      if (isWorking) {
         this.sender.sendMessage(BaseCommand.ERROR_COLOR + "Another instance of map writer is running");
      } else {
         isWorking = true;
         int height = this.size;
         int width = this.size;

         try {
            int[] colors = defaultColors;
            BukkitWorld bukkitWorld = (BukkitWorld)this.plugin.worlds.get(this.world.getDataManager().getUUID());
            if (bukkitWorld != null) {
               colors = new int[bukkitWorld.getSettings().biomeConfigs.length];

               BiomeConfig[] var8;
               for(BiomeConfig biomeConfig : var8 = bukkitWorld.getSettings().biomeConfigs) {
                  if (biomeConfig != null) {
                     try {
                        int color = Integer.decode(biomeConfig.BiomeColor);
                        if (color <= 16777215) {
                           colors[biomeConfig.Biome.getId()] = color;
                        }
                     } catch (NumberFormatException var20) {
                        TerrainControl.log(Level.WARNING, "Wrong color in " + biomeConfig.Biome.getName());
                        this.sender.sendMessage(BaseCommand.ERROR_COLOR + "Wrong color in " + biomeConfig.Biome.getName());
                     }
                  }
               }
            }

            this.sender.sendMessage(BaseCommand.MESSAGE_COLOR + "Generating map...");
            float[] tempArray = new float[256];
            BiomeBase[] BiomeBuffer = new BiomeBase[256];
            long time = System.currentTimeMillis();
            BufferedImage biomeImage = new BufferedImage(height * 16, width * 16, 1);
            BufferedImage tempImage = new BufferedImage(height * 16, width * 16, 1);
            int image_x = 0;
            int image_y = 0;

            for(int x = -height / 2; x < height / 2; ++x) {
               for(int z = -width / 2; z < width / 2; ++z) {
                  long time2 = System.currentTimeMillis();
                  if (time2 < time) {
                     time = time2;
                  }

                  if (time2 > time + 2000L) {
                     this.sender.sendMessage(BaseCommand.MESSAGE_COLOR + (x + height / 2) * 100 / height + "%");
                     time = time2;
                  }

                  BiomeBuffer = this.world.getWorldChunkManager().getBiomeBlock(BiomeBuffer, this.offsetX + x * 16, this.offsetZ + z * 16, 16, 16);
                  tempArray = this.world.getWorldChunkManager().getTemperatures(tempArray, this.offsetX + x * 16, this.offsetZ + z * 16, 16, 16);

                  for(int x1 = 0; x1 < 16; ++x1) {
                     for(int z1 = 0; z1 < 16; ++z1) {
                        switch (this.angle) {
                           case d0:
                              image_x = (x + height / 2) * 16 + x1;
                              image_y = (z + width / 2) * 16 + z1;
                              break;
                           case d90:
                              image_x = width * 16 - ((z + width / 2) * 16 + z1 + 1);
                              image_y = (x + height / 2) * 16 + x1;
                              break;
                           case d180:
                              image_x = height * 16 - ((x + height / 2) * 16 + x1 + 1);
                              image_y = width * 16 - ((z + width / 2) * 16 + z1 + 1);
                              break;
                           case d270:
                              image_x = (z + width / 2) * 16 + z1;
                              image_y = height * 16 - ((x + height / 2) * 16 + x1 + 1);
                        }

                        biomeImage.setRGB(image_x, image_y, colors[BiomeBuffer[x1 + 16 * z1].id]);
                        Color tempColor = Color.getHSBColor(0.7F - tempArray[x1 + 16 * z1] * 0.7F, 0.9F, tempArray[x1 + 16 * z1] * 0.7F + 0.3F);
                        tempImage.setRGB(image_x, image_y, tempColor.getRGB());
                     }
                  }
               }
            }

            this.sender.sendMessage(BaseCommand.MESSAGE_COLOR + "Writing images...");
            PNGImageWriter PngEncoder = new PNGImageWriter(new PNGImageWriterSpi());
            FileOutputStream fileOutput = new FileOutputStream(this.label + this.world.worldData.getName() + "_biome.png", false);
            ImageOutputStream imageOutput = new FileCacheImageOutputStream(fileOutput, (File)null);
            PngEncoder.setOutput(imageOutput);
            PngEncoder.write(biomeImage);
            imageOutput.close();
            fileOutput.close();
            fileOutput = new FileOutputStream(this.label + this.world.worldData.getName() + "_temperature.png", false);
            imageOutput = new FileCacheImageOutputStream(fileOutput, (File)null);
            PngEncoder.setOutput(imageOutput);
            PngEncoder.write(tempImage);
            imageOutput.close();
            fileOutput.close();
            PngEncoder.dispose();
            this.sender.sendMessage(BaseCommand.MESSAGE_COLOR + "Done");
         } catch (Exception e1) {
            e1.printStackTrace();
         }

         isWorking = false;
      }
   }

   // $FF: synthetic method
   static int[] $SWITCH_TABLE$com$khorn$terraincontrol$bukkit$MapWriter$Angle() {
      int[] var10000 = $SWITCH_TABLE$com$khorn$terraincontrol$bukkit$MapWriter$Angle;
      if (var10000 != null) {
         return var10000;
      } else {
         int[] var0 = new int[MapWriter.Angle.values().length];

         try {
            var0[MapWriter.Angle.d0.ordinal()] = 1;
         } catch (NoSuchFieldError var4) {
         }

         try {
            var0[MapWriter.Angle.d180.ordinal()] = 3;
         } catch (NoSuchFieldError var3) {
         }

         try {
            var0[MapWriter.Angle.d270.ordinal()] = 4;
         } catch (NoSuchFieldError var2) {
         }

         try {
            var0[MapWriter.Angle.d90.ordinal()] = 2;
         } catch (NoSuchFieldError var1) {
         }

         $SWITCH_TABLE$com$khorn$terraincontrol$bukkit$MapWriter$Angle = var0;
         return var0;
      }
   }

   public static enum Angle {
      d0,
      d90,
      d180,
      d270;

      private Angle() {
      }
   }
}
