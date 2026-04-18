package com.khorn.terraincontrol.util;

public class ChunkCoordinate {
   private final int chunkX;
   private final int chunkZ;

   protected ChunkCoordinate(int chunkX, int chunkZ) {
      super();
      this.chunkX = chunkX;
      this.chunkZ = chunkZ;
   }

   public int getChunkX() {
      return this.chunkX;
   }

   public int getChunkZ() {
      return this.chunkZ;
   }

   public int hashCode() {
      return Integer.valueOf(this.chunkX).hashCode() >> 13 ^ Integer.valueOf(this.chunkZ).hashCode();
   }

   public boolean equals(Object otherObject) {
      if (otherObject == null) {
         return false;
      } else if (!(otherObject instanceof ChunkCoordinate)) {
         return false;
      } else {
         ChunkCoordinate otherChunkCoordinate = (ChunkCoordinate)otherObject;
         if (otherChunkCoordinate.chunkX != this.chunkX) {
            return false;
         } else {
            return otherChunkCoordinate.chunkZ == this.chunkZ;
         }
      }
   }

   public static ChunkCoordinate fromBlockCoords(int blockX, int blockZ) {
      return new ChunkCoordinate((int)Math.floor((double)(blockX - 8) / (double)16.0F), (int)Math.floor((double)(blockZ - 8) / (double)16.0F));
   }

   public static ChunkCoordinate fromChunkCoords(int chunkX, int chunkZ) {
      return new ChunkCoordinate(chunkX, chunkZ);
   }
}
