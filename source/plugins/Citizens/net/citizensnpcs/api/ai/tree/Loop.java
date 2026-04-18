package net.citizensnpcs.api.ai.tree;

public class Loop extends BehaviorGoalAdapter {
   private final Condition condition;
   private final Behavior wrapping;

   public Loop(Behavior wrapping, Condition condition) {
      super();
      this.wrapping = wrapping;
      this.condition = condition;
   }

   public void reset() {
      this.wrapping.reset();
   }

   public BehaviorStatus run() {
      BehaviorStatus status = this.wrapping.run();
      if (status == BehaviorStatus.SUCCESS) {
         this.wrapping.reset();
         if (this.condition.get() && this.wrapping.shouldExecute()) {
            return BehaviorStatus.RUNNING;
         }
      }

      return status;
   }

   public boolean shouldExecute() {
      return this.wrapping.shouldExecute();
   }

   public static Loop createWithCondition(Behavior wrapping, Condition condition) {
      return new Loop(wrapping, condition);
   }
}
