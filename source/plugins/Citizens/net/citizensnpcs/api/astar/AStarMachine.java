package net.citizensnpcs.api.astar;

import com.google.common.base.Preconditions;
import com.google.common.base.Supplier;

public class AStarMachine {
   private Supplier storageSupplier;

   private AStarMachine(Supplier storage) {
      super();
      this.storageSupplier = storage;
   }

   private void f(AStarGoal goal, AStarNode node, AStarNode neighbour) {
      float g = node.g + goal.g(node, neighbour);
      float h = goal.h(neighbour);
      neighbour.f = g + h;
      neighbour.g = g;
      neighbour.h = h;
   }

   private AStarStorage getInitialisedStorage(AStarGoal goal, AStarNode start) {
      AStarStorage storage = (AStarStorage)this.storageSupplier.get();
      storage.open(start);
      start.f = goal.getInitialCost(start);
      return storage;
   }

   public AStarState getStateFor(AStarGoal goal, AStarNode start) {
      return new AStarState(goal, start, this.getInitialisedStorage(goal, start));
   }

   public Plan run(AStarState state) {
      return this.run(state, -1);
   }

   public Plan run(AStarState state, int maxIterations) {
      return this.run(state.storage, state.goal, state.start, maxIterations);
   }

   private Plan run(AStarStorage storage, AStarGoal goal, AStarNode start, int maxIterations) {
      Preconditions.checkNotNull(goal);
      Preconditions.checkNotNull(start);
      Preconditions.checkNotNull(storage);
      int iterations = 0;

      do {
         N node = (N)storage.removeBestNode();
         if (node == null) {
            return null;
         }

         if (goal.isFinished(node)) {
            return node.buildPlan();
         }

         storage.close(node);

         for(AStarNode neighbour : node.getNeighbours()) {
            this.f(goal, node, neighbour);
            if (storage.shouldExamine(neighbour)) {
               storage.open(neighbour);
               neighbour.parent = node;
            }
         }
      } while(maxIterations < 0 || iterations++ < maxIterations);

      return null;
   }

   public Plan runFully(AStarGoal goal, AStarNode start) {
      return this.runFully(goal, start, -1);
   }

   public Plan runFully(AStarGoal goal, AStarNode start, int iterations) {
      return this.run(this.getInitialisedStorage(goal, start), goal, start, iterations);
   }

   public void setStorageSupplier(Supplier newSupplier) {
      this.storageSupplier = newSupplier;
   }

   public static AStarMachine createWithDefaultStorage() {
      return createWithStorage(SimpleAStarStorage.FACTORY);
   }

   public static AStarMachine createWithStorage(Supplier storageSupplier) {
      return new AStarMachine(storageSupplier);
   }

   public class AStarState {
      private final AStarGoal goal;
      private final AStarNode start;
      private final AStarStorage storage;

      private AStarState(AStarGoal goal, AStarNode start, AStarStorage storage) {
         super();
         this.goal = goal;
         this.start = start;
         this.storage = storage;
      }

      public AStarNode getBestNode() {
         return this.storage.getBestNode();
      }
   }
}
