package com.khorn.terraincontrol.bukkit;

import com.khorn.terraincontrol.bukkit.commands.BaseCommand;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.lang.reflect.Field;
import net.minecraft.server.v1_6_R2.ChunkProviderServer;
import net.minecraft.server.v1_6_R2.ChunkRegionLoader;
import net.minecraft.server.v1_6_R2.NBTCompressedStreamTools;
import net.minecraft.server.v1_6_R2.NBTTagCompound;
import net.minecraft.server.v1_6_R2.RegionFile;
import org.bukkit.command.CommandSender;
import org.bukkit.craftbukkit.v1_6_R2.CraftWorld;

public class BiomeReplace implements Runnable {
   private CraftWorld world;
   private CommandSender sender;
   private byte BiomeIdFrom;
   private byte BiomeIdTo;
   private transient File[] regionFiles = null;
   private static boolean isWorking = false;

   public BiomeReplace(CraftWorld _world, int biomeIDFrom, int biomeIDTo, CommandSender _sender) {
      super();
      this.world = _world;
      this.sender = _sender;
      this.BiomeIdFrom = (byte)biomeIDFrom;
      this.BiomeIdTo = (byte)biomeIDTo;
   }

   public void run() {
      if (isWorking) {
         this.sender.sendMessage(BaseCommand.ERROR_COLOR + "Another instance of biome replace is running");
      } else {
         isWorking = true;
         File regionFolder = new File(this.world.getWorldFolder(), "region");
         if (!regionFolder.exists() || !regionFolder.isDirectory()) {
            regionFolder = new File(this.world.getWorldFolder(), "DIM-1" + File.separator + "region");
            if (!regionFolder.exists() || !regionFolder.isDirectory()) {
               regionFolder = new File(this.world.getWorldFolder(), "DIM1" + File.separator + "region");
               if (!regionFolder.exists() || !regionFolder.isDirectory()) {
                  this.sender.sendMessage(BaseCommand.ERROR_COLOR + "Could not validate folder for world's region files.");
                  return;
               }
            }
         }

         this.regionFiles = regionFolder.listFiles(new MCAFileFilter(".MCA"));
         if (this.regionFiles != null && this.regionFiles.length != 0) {
            ChunkProviderServer chunkProviderServer = (ChunkProviderServer)this.world.getHandle().chunkProvider;
            int chunksRewritten = 0;

            try {
               this.sender.sendMessage(BaseCommand.MESSAGE_COLOR + "Lock chunk load");
               Field chunkLoaderField = ChunkProviderServer.class.getDeclaredField("e");
               chunkLoaderField.setAccessible(true);
               ChunkRegionLoader chunkLoader = (ChunkRegionLoader)chunkLoaderField.get(chunkProviderServer);
               Field chunkLoaderLockField = ChunkRegionLoader.class.getDeclaredField("c");
               chunkLoaderLockField.setAccessible(true);
               Object chunkLoaderLock = chunkLoaderLockField.get(chunkLoader);
               synchronized(chunkLoaderLock) {
                  this.sender.sendMessage(BaseCommand.MESSAGE_COLOR + "Unload all chunks");
                  chunkProviderServer.a();
                  chunkProviderServer.unloadChunks();
                  this.sender.sendMessage(BaseCommand.MESSAGE_COLOR + "Start replace...");
                  long time = System.currentTimeMillis();

                  for(int i = 0; i < this.regionFiles.length; ++i) {
                     RegionFile file = new RegionFile(this.regionFiles[i]);
                     long time2 = System.currentTimeMillis();
                     if (time2 < time) {
                        time = time2;
                     }

                     if (time2 > time + 500L) {
                        this.sender.sendMessage(BaseCommand.MESSAGE_COLOR + Integer.toString(i * 100 / this.regionFiles.length) + "%");
                        time = time2;
                     }

                     for(int x = 0; x < 32; ++x) {
                        for(int z = 0; z < 32; ++z) {
                           if (file.c(x, z)) {
                              DataInputStream localDataInputStream = file.a(x, z);
                              NBTTagCompound localNBTTagCompound1 = NBTCompressedStreamTools.a(localDataInputStream);
                              localDataInputStream.close();
                              NBTTagCompound chunkTag = localNBTTagCompound1.getCompound("Level");
                              if (chunkTag.hasKey("Biomes")) {
                                 byte[] biomeArray = chunkTag.getByteArray("Biomes");
                                 boolean needSave = false;

                                 for(int t = 0; t < biomeArray.length; ++t) {
                                    if (this.BiomeIdFrom == -1 || biomeArray[t] == this.BiomeIdFrom) {
                                       biomeArray[t] = this.BiomeIdTo;
                                       needSave = true;
                                    }
                                 }

                                 if (needSave) {
                                    chunkTag.setByteArray("Biomes", biomeArray);
                                    ++chunksRewritten;
                                    DataOutputStream localDataOutputStream = file.b(x, z);
                                    NBTCompressedStreamTools.a(localNBTTagCompound1, localDataOutputStream);
                                    localDataOutputStream.close();
                                 }
                              }
                           }
                        }
                     }
                  }
               }
            } catch (NoSuchFieldException e) {
               e.printStackTrace();
            } catch (IllegalAccessException e) {
               e.printStackTrace();
            } catch (IOException e) {
               e.printStackTrace();
            }

            this.sender.sendMessage(BaseCommand.MESSAGE_COLOR + "Done. " + chunksRewritten + " chunks rewritten.");
            isWorking = false;
         } else {
            this.sender.sendMessage(BaseCommand.ERROR_COLOR + "Could not find any region files.");
         }
      }
   }

   private static class MCAFileFilter implements FileFilter {
      String ext;

      public MCAFileFilter(String extension) {
         super();
         this.ext = extension.toLowerCase();
      }

      public boolean accept(File file) {
         return file.exists() && file.isFile() && file.getName().toLowerCase().endsWith(this.ext);
      }
   }
}
