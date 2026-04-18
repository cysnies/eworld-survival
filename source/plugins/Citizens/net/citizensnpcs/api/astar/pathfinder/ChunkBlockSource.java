package net.citizensnpcs.api.astar.pathfinder;

import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.World;

public class ChunkBlockSource extends CachingChunkBlockSource {
   public ChunkBlockSource(Location location, float radius) {
      super(location, radius);
   }

   public ChunkBlockSource(World world, int x, int z, float radius) {
      super(world, x, z, radius);
   }

   protected Chunk getChunkObject(int x, int z) {
      return this.world.getChunkAt(x, z);
   }

   protected int getId(Chunk chunk, int x, int y, int z) {
      return chunk.getBlock(x, y, z).getTypeId();
   }

   protected int getLightLevel(Chunk chunk, int x, int y, int z) {
      return chunk.getBlock(x, y, z).getLightLevel();
   }
}
