package net.citizensnpcs.api.astar;

import com.google.common.collect.Lists;
import java.util.Collections;
import java.util.List;

public abstract class AStarNode implements Comparable {
   float f;
   float g;
   float h;
   AStarNode parent;
   List parents;

   public AStarNode() {
      super();
   }

   public abstract Plan buildPlan();

   public int compareTo(AStarNode other) {
      return Float.compare(this.f, other.f);
   }

   public abstract boolean equals(Object var1);

   public abstract Iterable getNeighbours();

   protected AStarNode getParent() {
      return this.parent;
   }

   protected Iterable getParents() {
      if (this.parents != null) {
         return this.parents;
      } else {
         this.parents = Lists.newArrayList();

         for(AStarNode start = this; start != null; start = start.parent) {
            this.parents.add(start);
         }

         Collections.reverse(this.parents);
         return this.parents;
      }
   }

   protected float getPathCost() {
      return this.f;
   }

   public abstract int hashCode();
}
