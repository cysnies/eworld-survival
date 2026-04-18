package net.citizensnpcs.api.ai.tree;

import net.citizensnpcs.api.ai.Goal;
import net.citizensnpcs.api.ai.GoalSelector;

public abstract class BehaviorGoalAdapter implements Goal, Behavior {
   public BehaviorGoalAdapter() {
      super();
   }

   public void run(GoalSelector selector) {
      BehaviorStatus status = this.run();
      if (status == BehaviorStatus.RESET_AND_REMOVE) {
         selector.finishAndRemove();
      }

      if (status == BehaviorStatus.FAILURE || status == BehaviorStatus.SUCCESS) {
         selector.finish();
      }

   }

   public boolean shouldExecute(GoalSelector selector) {
      return this.shouldExecute();
   }
}
