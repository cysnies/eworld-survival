package net.citizensnpcs.api.ai.tree;

public class IfElse extends BehaviorGoalAdapter {
   private final Condition condition;
   private Behavior executing;
   private final Behavior ifBehavior;
   private final Behavior elseBehavior;

   public IfElse(Condition condition, Behavior ifBehavior, Behavior elseBehavior) {
      super();
      this.condition = condition;
      this.ifBehavior = ifBehavior;
      this.elseBehavior = elseBehavior;
   }

   public void reset() {
      if (this.executing != null) {
         this.executing.reset();
         this.executing = null;
      }

   }

   public BehaviorStatus run() {
      return this.executing.run();
   }

   public boolean shouldExecute() {
      boolean cond = this.condition.get();
      if (cond) {
         this.executing = this.ifBehavior;
      } else {
         this.executing = this.elseBehavior;
      }

      if (this.executing != null && this.executing.shouldExecute()) {
         return true;
      } else {
         this.executing = null;
         return false;
      }
   }

   public static IfElse create(Condition condition, Behavior ifBehavior, Behavior elseBehavior) {
      return new IfElse(condition, ifBehavior, elseBehavior);
   }
}
