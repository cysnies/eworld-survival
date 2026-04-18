package net.citizensnpcs.api.ai.tree;

public class ForwardingBehaviorGoalAdapter extends BehaviorGoalAdapter {
   private final Behavior behavior;

   public ForwardingBehaviorGoalAdapter(Behavior behavior) {
      super();
      this.behavior = behavior;
   }

   public void reset() {
      this.behavior.reset();
   }

   public BehaviorStatus run() {
      return this.behavior.run();
   }

   public boolean shouldExecute() {
      return this.behavior.shouldExecute();
   }

   public String toString() {
      return "ForwardingBehaviorGoalAdapter [behavior=" + this.behavior + "]";
   }
}
