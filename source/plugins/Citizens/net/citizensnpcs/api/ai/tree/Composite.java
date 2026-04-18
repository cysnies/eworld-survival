package net.citizensnpcs.api.ai.tree;

import com.google.common.collect.Lists;
import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import net.citizensnpcs.api.CitizensAPI;
import org.bukkit.event.HandlerList;

public abstract class Composite extends BehaviorGoalAdapter {
   private final List behaviors;
   private final Collection parallelExecuting;

   public Composite(Behavior... behaviors) {
      this((Collection)Arrays.asList(behaviors));
   }

   public Composite(Collection behaviors) {
      super();
      this.parallelExecuting = Lists.newArrayListWithCapacity(0);
      this.behaviors = Lists.newArrayList(behaviors);
      boolean foundNonParallel = false;

      for(Behavior behavior : behaviors) {
         if (!(behavior instanceof ParallelBehavior)) {
            foundNonParallel = true;
            break;
         }
      }

      if (!foundNonParallel) {
         throw new IllegalStateException("must have at least one non-parallel node");
      }
   }

   public void addBehavior(Behavior behavior) {
      this.behaviors.add(behavior);
   }

   protected void addParallel(Behavior behavior) {
      if (behavior.shouldExecute() && !this.parallelExecuting.contains(behavior)) {
         this.parallelExecuting.add(behavior);
         this.prepareForExecution(behavior);
      }

   }

   protected List getBehaviors() {
      return this.behaviors;
   }

   protected void prepareForExecution(Behavior behavior) {
      if (behavior != null) {
         CitizensAPI.registerEvents(behavior);
      }
   }

   public void removeBehavior(Behavior behavior) {
      this.behaviors.remove(behavior);
   }

   public void reset() {
      if (this.parallelExecuting.size() > 0) {
         for(Behavior behavior : this.parallelExecuting) {
            behavior.reset();
         }

         this.parallelExecuting.clear();
      }

   }

   public boolean shouldExecute() {
      return this.behaviors.size() > 0;
   }

   protected void stopExecution(Behavior behavior) {
      if (behavior != null) {
         HandlerList.unregisterAll(behavior);
         behavior.reset();
      }
   }

   protected void tickParallel() {
      Iterator<Behavior> itr = this.parallelExecuting.iterator();

      while(itr.hasNext()) {
         Behavior behavior = (Behavior)itr.next();
         BehaviorStatus status = behavior.run();
         switch (status) {
            case RESET_AND_REMOVE:
               this.behaviors.remove(behavior);
            case FAILURE:
            case SUCCESS:
               itr.remove();
               this.stopExecution(behavior);
         }
      }

   }
}
