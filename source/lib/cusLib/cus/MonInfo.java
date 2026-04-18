package cus;

import java.io.Serializable;
import java.util.UUID;

public class MonInfo implements Serializable {
   private static final long serialVersionUID = 1L;
   private long point;
   private UUID uid;
   private String world;
   private int chunkX;
   private int chunkZ;

   public MonInfo(long point, UUID uid, String world, int chunkX, int chunkZ) {
      super();
      this.point = point;
      this.uid = uid;
      this.world = world;
      this.chunkX = chunkX;
      this.chunkZ = chunkZ;
   }

   public long getPoint() {
      return this.point;
   }

   public void setPoint(long point) {
      this.point = point;
   }

   public UUID getUid() {
      return this.uid;
   }

   public void setUid(UUID uid) {
      this.uid = uid;
   }

   public String getWorld() {
      return this.world;
   }

   public void setWorld(String world) {
      this.world = world;
   }

   public int getChunkX() {
      return this.chunkX;
   }

   public void setChunkX(int chunkX) {
      this.chunkX = chunkX;
   }

   public int getChunkZ() {
      return this.chunkZ;
   }

   public void setChunkZ(int chunkZ) {
      this.chunkZ = chunkZ;
   }
}
