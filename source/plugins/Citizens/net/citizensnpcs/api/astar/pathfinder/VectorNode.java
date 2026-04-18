package net.citizensnpcs.api.astar.pathfinder;

import com.google.common.collect.Lists;
import java.util.List;
import net.citizensnpcs.api.astar.AStarNode;
import net.citizensnpcs.api.astar.Plan;
import org.bukkit.Location;
import org.bukkit.util.Vector;

public class VectorNode extends AStarNode implements PathPoint {
   private float blockCost;
   private final BlockSource blockSource;
   List callbacks;
   private final BlockExaminer[] examiners;
   final Vector location;
   private static final float TIEBREAKER = 1.001F;

   public VectorNode(Location location, BlockSource source, BlockExaminer... examiners) {
      this(location.toVector(), source, examiners);
   }

   public VectorNode(Vector location, BlockSource source, BlockExaminer... examiners) {
      super();
      this.blockCost = -1.0F;
      this.location = location.setX(location.getBlockX()).setY(location.getBlockY()).setZ(location.getBlockZ());
      this.blockSource = source;
      this.examiners = examiners == null ? new BlockExaminer[0] : examiners;
   }

   public void addCallback(PathPoint.PathCallback callback) {
      if (this.callbacks == null) {
         this.callbacks = Lists.newArrayList();
      }

      this.callbacks.add(callback);
   }

   public Plan buildPlan() {
      Iterable<VectorNode> parents = this.getParents();
      return new Path(parents);
   }

   public float distance(VectorNode to) {
      return (float)this.location.distance(to.location);
   }

   public boolean equals(Object obj) {
      if (this == obj) {
         return true;
      } else if (obj != null && this.getClass() == obj.getClass()) {
         VectorNode other = (VectorNode)obj;
         if (this.location == null) {
            if (other.location != null) {
               return false;
            }
         } else if (!this.location.equals(other.location)) {
            return false;
         }

         return true;
      } else {
         return false;
      }
   }

   private float getBlockCost() {
      if (this.blockCost == -1.0F) {
         this.blockCost = 0.0F;

         for(BlockExaminer examiner : this.examiners) {
            this.blockCost += examiner.getCost(this.blockSource, this);
         }
      }

      return this.blockCost;
   }

   public Iterable getNeighbours() {
      List<AStarNode> nodes = Lists.newArrayList();

      for(int x = -1; x <= 1; ++x) {
         for(int y = -1; y <= 1; ++y) {
            for(int z = -1; z <= 1; ++z) {
               if (x != 0 || y != 0 || z != 0) {
                  Vector mod = this.location.clone().add(new Vector(x, y, z));
                  if (!mod.equals(this.location)) {
                     VectorNode sub = this.getNewNode(mod);
                     if (this.isPassable(sub)) {
                        nodes.add(sub);
                     }
                  }
               }
            }
         }
      }

      return nodes;
   }

   private VectorNode getNewNode(Vector mod) {
      return new VectorNode(mod, this.blockSource, this.examiners);
   }

   public Vector getVector() {
      return this.location;
   }

   public int hashCode() {
      int prime = 31;
      int result = 31 + (this.location == null ? 0 : this.location.hashCode());
      return result;
   }

   public float heuristicDistance(Vector goal) {
      return (float)(this.location.distance(goal) + (double)this.getBlockCost()) * 1.001F;
   }

   private boolean isPassable(PathPoint mod) {
      for(BlockExaminer examiner : this.examiners) {
         boolean passable = examiner.isPassable(this.blockSource, mod);
         if (!passable) {
            return false;
         }
      }

      return true;
   }
}
