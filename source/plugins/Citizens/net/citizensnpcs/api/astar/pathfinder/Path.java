package net.citizensnpcs.api.astar.pathfinder;

import com.google.common.collect.Lists;
import java.util.Arrays;
import java.util.List;
import net.citizensnpcs.api.astar.Agent;
import net.citizensnpcs.api.astar.Plan;
import net.citizensnpcs.api.npc.NPC;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.util.Vector;

public class Path implements Plan {
   private int index = 0;
   private final PathEntry[] path;

   Path(Iterable unfiltered) {
      super();
      this.path = this.cull(unfiltered);
   }

   private PathEntry[] cull(Iterable unfiltered) {
      List<PathEntry> path = Lists.newArrayList();

      for(VectorNode node : unfiltered) {
         if (node.callbacks == null) {
            Vector vector = node.location;
            path.add(new PathEntry(vector, node.callbacks));
         }
      }

      return (PathEntry[])path.toArray(new PathEntry[path.size()]);
   }

   public Vector getCurrentVector() {
      return this.path[this.index].vector;
   }

   public boolean isComplete() {
      return this.index >= this.path.length;
   }

   public String toString() {
      return Arrays.toString(this.path);
   }

   public void update(Agent agent) {
      if (!this.isComplete()) {
         PathEntry entry = this.path[this.index];
         if (entry.hasCallbacks()) {
            NPC npc = (NPC)agent;
            Block block = entry.getBlockUsingWorld(npc.getBukkitEntity().getWorld());

            for(PathPoint.PathCallback callback : entry.callbacks) {
               callback.run(npc, block);
            }
         }

         ++this.index;
      }
   }

   private static class PathEntry {
      final Iterable callbacks;
      final Vector vector;

      private PathEntry(Vector vector, List callbacks) {
         super();
         this.vector = vector;
         this.callbacks = callbacks;
      }

      private Block getBlockUsingWorld(World world) {
         return world.getBlockAt(this.vector.getBlockX(), this.vector.getBlockY(), this.vector.getBlockZ());
      }

      private boolean hasCallbacks() {
         return this.callbacks != null;
      }

      public String toString() {
         return this.vector.toString();
      }
   }
}
