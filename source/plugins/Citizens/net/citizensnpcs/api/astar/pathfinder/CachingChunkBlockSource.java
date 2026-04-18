package net.citizensnpcs.api.astar.pathfinder;

import org.bukkit.Location;
import org.bukkit.World;

public abstract class CachingChunkBlockSource extends BlockSource {
   private final Object[][] chunks;
   private final int chunkX;
   private final int chunkZ;
   int t;
   protected final World world;

   protected CachingChunkBlockSource(Location location, float radius) {
      this(location.getWorld(), location.getBlockX(), location.getBlockZ(), radius);
   }

   protected CachingChunkBlockSource(World world, int x, int z, float radius) {
      this(world, (int)((float)x - radius), (int)((float)z - radius), (int)((float)x + radius), (int)((float)z + radius));
   }

   protected CachingChunkBlockSource(World world, int minX, int minZ, int maxX, int maxZ) {
      super();
      this.world = world;
      this.chunkX = minX >> 4;
      this.chunkZ = minZ >> 4;
      int maxChunkX = maxX >> 4;
      int maxChunkZ = maxZ >> 4;
      this.chunks = new Object[maxChunkX - this.chunkX + 1][maxChunkZ - this.chunkZ + 1];

      for(int x = this.chunkX; x < maxChunkX; ++x) {
         for(int z = this.chunkZ; z < maxChunkZ; ++z) {
            this.chunks[x - this.chunkX][z - this.chunkZ] = this.getChunkObject(x, z);
         }
      }

   }

   public int getBlockTypeIdAt(int x, int y, int z) {
      T chunk = (T)this.getSpecific(x, z);
      return chunk != null ? this.getId(chunk, x & 15, y, z & 15) : this.world.getBlockTypeIdAt(x, y, z);
   }

   protected abstract Object getChunkObject(int var1, int var2);

   protected abstract int getId(Object var1, int var2, int var3, int var4);

   protected abstract int getLightLevel(Object var1, int var2, int var3, int var4);

   private Object getSpecific(int x, int z) {
      int xx = (x >> 4) - this.chunkX;
      int zz = (z >> 4) - this.chunkZ;
      if (xx >= 0 && xx < this.chunks.length) {
         Object[] inner = this.chunks[xx];
         if (zz >= 0 && zz < inner.length) {
            return inner[zz];
         }
      }

      return null;
   }

   public World getWorld() {
      return this.world;
   }
}
