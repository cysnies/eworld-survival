package com.lishid.orebfuscator.obfuscation;

import org.bukkit.World;
import org.bukkit.entity.Player;

public class ChunkInfo {
   public boolean useCache;
   public int chunkX;
   public int chunkZ;
   public int chunkMask;
   public int extraMask;
   public int chunkSectionNumber;
   public int extraSectionNumber;
   public boolean canUseCache;
   public int[] chunkSectionToIndexMap = new int[16];
   public int[] extraSectionToIndexMap = new int[16];
   public World world;
   public byte[] data;
   public byte[] buffer;
   public Player player;
   public int startIndex;
   public int size;
   public int blockSize;

   public ChunkInfo() {
      super();
   }
}
