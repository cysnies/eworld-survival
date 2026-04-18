package net.citizensnpcs.api.astar;

import com.google.common.base.Supplier;
import com.google.common.collect.Maps;
import java.util.Map;
import java.util.PriorityQueue;
import java.util.Queue;

public class SimpleAStarStorage implements AStarStorage {
   private final Map closed = Maps.newHashMap();
   private final Map open = Maps.newHashMap();
   private final Queue queue = new PriorityQueue();
   public static final Supplier FACTORY = new Supplier() {
      public AStarStorage get() {
         return new SimpleAStarStorage();
      }
   };

   public SimpleAStarStorage() {
      super();
   }

   public void close(AStarNode node) {
      this.open.remove(node);
      this.closed.put(node, node.f);
   }

   public AStarNode getBestNode() {
      return (AStarNode)this.queue.peek();
   }

   public void open(AStarNode node) {
      this.queue.offer(node);
      this.open.put(node, node.f);
      this.closed.remove(node);
   }

   public AStarNode removeBestNode() {
      return (AStarNode)this.queue.poll();
   }

   public boolean shouldExamine(AStarNode neighbour) {
      Float openF = (Float)this.open.get(neighbour);
      if (openF != null && openF > neighbour.f) {
         this.open.remove(neighbour);
         openF = null;
      }

      Float closedF = (Float)this.closed.get(neighbour);
      if (closedF != null && closedF > neighbour.f) {
         this.closed.remove(neighbour);
         closedF = null;
      }

      return closedF == null && openF == null;
   }

   public String toString() {
      return "SimpleAStarStorage [closed=" + this.closed + ", open=" + this.open + "]";
   }
}
