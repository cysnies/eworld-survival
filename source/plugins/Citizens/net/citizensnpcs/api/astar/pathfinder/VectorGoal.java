package net.citizensnpcs.api.astar.pathfinder;

import net.citizensnpcs.api.astar.AStarGoal;
import org.bukkit.Location;
import org.bukkit.util.Vector;

public class VectorGoal implements AStarGoal {
   private final Vector goal;
   private final float leeway;

   public VectorGoal(Location dest, float range) {
      this(dest.toVector(), range);
   }

   public VectorGoal(Vector goal, float range) {
      super();
      this.goal = goal.setX(goal.getBlockX()).setY(goal.getBlockY()).setZ(goal.getBlockZ());
      this.leeway = range;
   }

   public float g(VectorNode from, VectorNode to) {
      return from.distance(to);
   }

   public float getInitialCost(VectorNode node) {
      return node.heuristicDistance(this.goal);
   }

   public float h(VectorNode from) {
      return from.heuristicDistance(this.goal);
   }

   public boolean isFinished(VectorNode node) {
      return node.getVector().distanceSquared(this.goal) <= (double)this.leeway;
   }
}
