package com.lishid.orebfuscator.obfuscation;

import com.lishid.orebfuscator.Orebfuscator;
import com.lishid.orebfuscator.OrebfuscatorConfig;
import com.lishid.orebfuscator.cache.ObfuscatedCachedChunk;
import com.lishid.orebfuscator.internal.IPacket51;
import com.lishid.orebfuscator.internal.IPacket56;
import com.lishid.orebfuscator.internal.InternalAccessor;
import java.io.File;
import java.util.ArrayList;
import java.util.zip.Deflater;
import org.bukkit.World.Environment;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;

public class Calculations {
   public static final ThreadLocal buffer = new ThreadLocal() {
      protected byte[] initialValue() {
         return new byte[65536];
      }
   };
   public static final ThreadLocal localDeflater = new ThreadLocal() {
      protected Deflater initialValue() {
         return new Deflater(1);
      }
   };
   private static byte[] cacheMap = new byte[256];

   static {
      buildCacheMap();
   }

   public Calculations() {
      super();
   }

   public static void Obfuscate(Object packet, Player player) {
      IPacket51 packet51 = InternalAccessor.Instance.newPacket51();
      packet51.setPacket(packet);
      Obfuscate(packet51, player);
   }

   public static void Obfuscate(IPacket56 packet, Player player) {
      if (packet.getFieldData(packet.getOutputBuffer()) == null) {
         ChunkInfo[] infos = getInfo(packet, player);

         for(int chunkNum = 0; chunkNum < infos.length; ++chunkNum) {
            ChunkInfo info = infos[chunkNum];
            info.buffer = (byte[])buffer.get();
            ComputeChunkInfoAndObfuscate(info, (byte[])packet.getFieldData(packet.getBuildBuffer()));
         }

      }
   }

   public static void Obfuscate(IPacket51 packet, Player player) {
      Obfuscate(packet, player, true);
   }

   public static void Obfuscate(IPacket51 packet, Player player, boolean needCompression) {
      ChunkInfo info = getInfo(packet, player);
      info.buffer = (byte[])buffer.get();
      if (info.chunkMask != 0 || info.extraMask != 0) {
         if (info.buffer != null && info.buffer.length != 0) {
            ComputeChunkInfoAndObfuscate(info, packet.getBuffer());
            if (needCompression) {
               Deflater deflater = (Deflater)localDeflater.get();
               packet.compress(deflater);
            }

         }
      }
   }

   public static ChunkInfo[] getInfo(IPacket56 packet, Player player) {
      ChunkInfo[] infos = new ChunkInfo[packet.getPacketChunkNumber()];
      int dataStartIndex = 0;
      int[] x = packet.getX();
      int[] z = packet.getZ();
      byte[][] inflatedBuffers = (byte[][])packet.getFieldData(packet.getInflatedBuffers());
      int[] chunkMask = packet.getChunkMask();
      int[] extraMask = packet.getExtraMask();
      byte[] buildBuffer = (byte[])packet.getFieldData(packet.getBuildBuffer());
      if (buildBuffer.length == 0) {
         int finalBufferSize = 0;

         for(int i = 0; i < inflatedBuffers.length; ++i) {
            finalBufferSize += inflatedBuffers[i].length;
         }

         buildBuffer = new byte[finalBufferSize];
         int bufferLocation = 0;

         for(int i = 0; i < inflatedBuffers.length; ++i) {
            System.arraycopy(inflatedBuffers[i], 0, buildBuffer, bufferLocation, inflatedBuffers[i].length);
            bufferLocation += inflatedBuffers[i].length;
         }

         packet.setFieldData(packet.getBuildBuffer(), buildBuffer);
      }

      for(int chunkNum = 0; chunkNum < packet.getPacketChunkNumber(); ++chunkNum) {
         ChunkInfo info = new ChunkInfo();
         infos[chunkNum] = info;
         info.world = player.getWorld();
         info.player = player;
         info.chunkX = x[chunkNum];
         info.chunkZ = z[chunkNum];
         info.chunkMask = chunkMask[chunkNum];
         info.extraMask = extraMask[chunkNum];
         info.data = buildBuffer;
         info.startIndex = dataStartIndex;
         info.size = inflatedBuffers[chunkNum].length;
         dataStartIndex += info.size;
      }

      return infos;
   }

   public static ChunkInfo getInfo(IPacket51 packet, Player player) {
      ChunkInfo info = new ChunkInfo();
      info.world = player.getWorld();
      info.player = player;
      info.chunkX = packet.getX();
      info.chunkZ = packet.getZ();
      info.chunkMask = packet.getChunkMask();
      info.extraMask = packet.getExtraMask();
      info.data = packet.getBuffer();
      info.startIndex = 0;
      return info;
   }

   public static void ComputeChunkInfoAndObfuscate(ChunkInfo info, byte[] original) {
      for(int i = 0; i < 16; ++i) {
         if ((info.chunkMask & 1 << i) > 0) {
            info.chunkSectionToIndexMap[i] = info.chunkSectionNumber++;
         } else {
            info.chunkSectionToIndexMap[i] = -1;
         }

         if ((info.extraMask & 1 << i) > 0) {
            info.extraSectionToIndexMap[i] = info.extraSectionNumber++;
         }
      }

      info.size = 2048 * (5 * info.chunkSectionNumber + info.extraSectionNumber) + 256;
      info.blockSize = 4096 * info.chunkSectionNumber;
      if (info.startIndex + info.blockSize <= info.data.length) {
         if (!OrebfuscatorConfig.isWorldDisabled(info.world.getName()) && OrebfuscatorConfig.obfuscateForPlayer(info.player) && OrebfuscatorConfig.Enabled) {
            byte[] obfuscated = Obfuscate(info, original);
            System.arraycopy(obfuscated, 0, original, info.startIndex, info.blockSize);
         }

      }
   }

   public static byte[] Obfuscate(ChunkInfo info, byte[] original) {
      boolean isNether = info.world.getEnvironment() == Environment.NETHER;
      ObfuscatedCachedChunk cache = null;
      long hash = 0L;
      ArrayList<Block> proximityBlocks = new ArrayList();
      info.useCache = false;
      int initialRadius = OrebfuscatorConfig.InitialRadius;
      if (info.blockSize > info.buffer.length) {
         info.buffer = new byte[info.blockSize];
         buffer.set(info.buffer);
      }

      System.arraycopy(info.data, info.startIndex, info.buffer, 0, info.blockSize);
      if (OrebfuscatorConfig.UseCache) {
         PrepareBufferForCaching(info.buffer, info.blockSize);
         File cacheFolder = new File(OrebfuscatorConfig.getCacheFolder(), info.world.getName());
         cache = new ObfuscatedCachedChunk(cacheFolder, info.chunkX, info.chunkZ);
         info.useCache = true;
         hash = CalculationsUtil.Hash(info.buffer, info.blockSize);
         cache.Read();
         long storedHash = cache.getHash();
         int[] proximityList = cache.proximityList;
         if (storedHash == hash && cache.data != null) {
            if (proximityList != null) {
               for(int i = 0; i < proximityList.length; i += 3) {
                  Block b = CalculationsUtil.getBlockAt(info.player.getWorld(), proximityList[i], proximityList[i + 1], proximityList[i + 2]);
                  proximityBlocks.add(b);
               }
            }

            ProximityHider.AddProximityBlocks(info.player, proximityBlocks);
            RepaintChunkToBuffer(cache.data, info.data, info.startIndex, info.blockSize);
            return cache.data;
         }
      }

      int randomIncrement = 0;
      int randomIncrement2 = 0;
      int ramdomCave = 0;
      boolean obfuscate = false;
      boolean specialObfuscate = false;
      int engineMode = OrebfuscatorConfig.EngineMode;
      int maxChance = OrebfuscatorConfig.AirGeneratorMaxChance;
      int randomBlocksLength = OrebfuscatorConfig.getRandomBlocks(false, isNether).length;
      boolean randomAlternate = false;
      int dataIndexModifier = 0;
      int startX = info.chunkX << 4;
      int startZ = info.chunkZ << 4;

      for(int i = 0; i < 16; ++i) {
         if ((info.chunkMask & 1 << i) != 0) {
            int indexDataStart = dataIndexModifier * 4096;
            int tempIndex = 0;
            OrebfuscatorConfig.shuffleRandomBlocks();

            for(int y = 0; y < 16; ++y) {
               for(int z = 0; z < 16; ++z) {
                  int incrementMax = (maxChance + OrebfuscatorConfig.random(maxChance)) / 2;

                  for(int x = 0; x < 16; ++x) {
                     int index = indexDataStart + tempIndex;
                     byte data = info.data[info.startIndex + index];
                     obfuscate = false;
                     specialObfuscate = false;
                     if (OrebfuscatorConfig.isObfuscated(data, isNether)) {
                        if (initialRadius == 0) {
                           if (OrebfuscatorConfig.UseProximityHider && OrebfuscatorConfig.isProximityObfuscated(data)) {
                              if (!areAjacentBlocksTransparent(info, data, startX + x, (i << 4) + y, startZ + z, 1)) {
                                 obfuscate = true;
                              }
                           } else {
                              obfuscate = true;
                           }
                        } else if (!areAjacentBlocksTransparent(info, data, startX + x, (i << 4) + y, startZ + z, initialRadius)) {
                           obfuscate = true;
                        }
                     }

                     if (!obfuscate && OrebfuscatorConfig.UseProximityHider && OrebfuscatorConfig.isProximityObfuscated(data) && (i << 4) + y <= OrebfuscatorConfig.ProximityHiderEnd) {
                        proximityBlocks.add(CalculationsUtil.getBlockAt(info.player.getWorld(), startX + x, (i << 4) + y, startZ + z));
                        obfuscate = true;
                        if (OrebfuscatorConfig.UseSpecialBlockForProximityHider) {
                           specialObfuscate = true;
                        }
                     }

                     if (obfuscate) {
                        if (specialObfuscate) {
                           info.buffer[index] = (byte)OrebfuscatorConfig.ProximityHiderID;
                        } else {
                           randomIncrement2 = OrebfuscatorConfig.random(incrementMax);
                           if (engineMode == 1) {
                              info.buffer[index] = (byte)(isNether ? 87 : 1);
                           } else if (engineMode == 2) {
                              if (randomBlocksLength > 1) {
                                 randomIncrement = CalculationsUtil.increment(randomIncrement, randomBlocksLength);
                              }

                              info.buffer[index] = OrebfuscatorConfig.getRandomBlock(randomIncrement, randomAlternate, isNether);
                              randomAlternate = !randomAlternate;
                           }

                           if (OrebfuscatorConfig.AntiTexturePackAndFreecam) {
                              if (randomIncrement2 == 0) {
                                 ramdomCave = 1 + OrebfuscatorConfig.random(3);
                              }

                              if (ramdomCave > 0) {
                                 info.buffer[index] = 0;
                                 --ramdomCave;
                              }
                           }
                        }
                     }

                     if (!obfuscate && OrebfuscatorConfig.DarknessHideBlocks && OrebfuscatorConfig.isDarknessObfuscated(data) && !areAjacentBlocksBright(info, startX + x, (i << 4) + y, startZ + z, 1)) {
                        info.buffer[index] = 0;
                     }

                     ++tempIndex;
                  }
               }
            }

            ++dataIndexModifier;
         }
      }

      ProximityHider.AddProximityBlocks(info.player, proximityBlocks);
      if (info.useCache) {
         int[] proximityList = new int[proximityBlocks.size() * 3];

         for(int i = 0; i < proximityBlocks.size(); ++i) {
            Block b = (Block)proximityBlocks.get(i);
            if (b != null) {
               proximityList[i * 3] = b.getX();
               proximityList[i * 3 + 1] = b.getY();
               proximityList[i * 3 + 2] = b.getZ();
            }
         }

         cache.Write(hash, info.buffer, proximityList);
      }

      if (cache != null) {
         cache.free();
      }

      if (OrebfuscatorConfig.UseCache) {
         RepaintChunkToBuffer(info.buffer, info.data, info.startIndex, info.blockSize);
      }

      return info.buffer;
   }

   public static void buildCacheMap() {
      for(int i = 0; i < 256; ++i) {
         cacheMap[i] = (byte)i;
         if (OrebfuscatorConfig.isBlockTransparent((short)i) && !isBlockSpecialObfuscated((byte)i)) {
            cacheMap[i] = 0;
         }
      }

   }

   private static void PrepareBufferForCaching(byte[] data, int length) {
      for(int i = 0; i < length; ++i) {
         data[i] = cacheMap[(data[i] + 256) % 256];
      }

   }

   private static boolean isBlockSpecialObfuscated(byte id) {
      if (OrebfuscatorConfig.DarknessHideBlocks && OrebfuscatorConfig.isDarknessObfuscated(id)) {
         return true;
      } else {
         return OrebfuscatorConfig.UseProximityHider && OrebfuscatorConfig.isProximityObfuscated(id);
      }
   }

   private static void RepaintChunkToBuffer(byte[] data, byte[] original, int start, int length) {
      for(int i = 0; i < length; ++i) {
         if (data[i] == 0 && original[start + i] != 0 && OrebfuscatorConfig.isBlockTransparent(original[start + i]) && !isBlockSpecialObfuscated(original[start + i])) {
            data[i] = original[start + i];
         }
      }

   }

   public static boolean areAjacentBlocksTransparent(ChunkInfo info, byte currentBlockID, int x, int y, int z, int countdown) {
      byte id = 0;
      boolean foundID = false;
      if (y < info.world.getMaxHeight() && y >= 0) {
         int section = info.chunkSectionToIndexMap[y >> 4];
         if ((info.chunkMask & 1 << (y >> 4)) > 0 && x >> 4 == info.chunkX && z >> 4 == info.chunkZ) {
            int cX = x % 16 < 0 ? x % 16 + 16 : x % 16;
            int cZ = z % 16 < 0 ? z % 16 + 16 : z % 16;
            int index = section * 4096 + (y % 16 << 8) + (cZ << 4) + cX;

            try {
               id = info.data[info.startIndex + index];
               foundID = true;
            } catch (Exception e) {
               Orebfuscator.log((Throwable)e);
            }
         }

         if (!foundID) {
            if (CalculationsUtil.isChunkLoaded(info.world, x >> 4, z >> 4)) {
               id = (byte)info.world.getBlockTypeIdAt(x, y, z);
            } else {
               id = 1;
               info.useCache = false;
            }
         }

         if (id != currentBlockID && OrebfuscatorConfig.isBlockTransparent(id)) {
            return true;
         } else if (countdown == 0) {
            return false;
         } else if (areAjacentBlocksTransparent(info, currentBlockID, x, y + 1, z, countdown - 1)) {
            return true;
         } else if (areAjacentBlocksTransparent(info, currentBlockID, x, y - 1, z, countdown - 1)) {
            return true;
         } else if (areAjacentBlocksTransparent(info, currentBlockID, x + 1, y, z, countdown - 1)) {
            return true;
         } else if (areAjacentBlocksTransparent(info, currentBlockID, x - 1, y, z, countdown - 1)) {
            return true;
         } else if (areAjacentBlocksTransparent(info, currentBlockID, x, y, z + 1, countdown - 1)) {
            return true;
         } else {
            return areAjacentBlocksTransparent(info, currentBlockID, x, y, z - 1, countdown - 1);
         }
      } else {
         return true;
      }
   }

   public static boolean areAjacentBlocksBright(ChunkInfo info, int x, int y, int z, int countdown) {
      if (CalculationsUtil.isChunkLoaded(info.world, x >> 4, z >> 4)) {
         if (info.world.getBlockAt(x, y, z).getLightLevel() > 0) {
            return true;
         } else if (countdown == 0) {
            return false;
         } else if (areAjacentBlocksBright(info, x, y + 1, z, countdown - 1)) {
            return true;
         } else if (areAjacentBlocksBright(info, x, y - 1, z, countdown - 1)) {
            return true;
         } else if (areAjacentBlocksBright(info, x + 1, y, z, countdown - 1)) {
            return true;
         } else if (areAjacentBlocksBright(info, x - 1, y, z, countdown - 1)) {
            return true;
         } else if (areAjacentBlocksBright(info, x, y, z + 1, countdown - 1)) {
            return true;
         } else {
            return areAjacentBlocksBright(info, x, y, z - 1, countdown - 1);
         }
      } else {
         return true;
      }
   }
}
