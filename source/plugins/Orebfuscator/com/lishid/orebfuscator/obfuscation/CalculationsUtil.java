package com.lishid.orebfuscator.obfuscation;

import java.util.zip.CRC32;
import org.bukkit.World;
import org.bukkit.block.Block;

public class CalculationsUtil {
   public CalculationsUtil() {
      super();
   }

   public static boolean isChunkLoaded(World world, int x, int z) {
      return world.isChunkLoaded(x, z);
   }

   public static Block getBlockAt(World world, int x, int y, int z) {
      return isChunkLoaded(world, x >> 4, z >> 4) ? world.getBlockAt(x, y, z) : null;
   }

   public static long Hash(byte[] data, int length) {
      CRC32 crc = new CRC32();
      crc.reset();
      crc.update(data, 0, length);
      return crc.getValue();
   }

   public static int increment(int current, int max) {
      return (current + 1) % max;
   }
}
