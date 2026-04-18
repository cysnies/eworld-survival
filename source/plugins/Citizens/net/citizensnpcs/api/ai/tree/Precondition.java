package net.citizensnpcs.api.ai.tree;

public abstract class Precondition extends BehaviorGoalAdapter {
   protected final Condition condition;

   protected Precondition(Condition condition) {
      super();
      this.condition = condition;
   }

   public static Precondition runPrecondition(Condition condition) {
      return new RunPrecondition(condition);
   }

   public static Precondition wrappingPrecondition(Behavior wrapping, Condition condition) {
      return new WrappingPrecondition(wrapping, condition);
   }

   private static class RunPrecondition extends Precondition {
      public RunPrecondition(Condition condition) {
         super(condition);
      }

      public void reset() {
      }

      public BehaviorStatus run() {
         return this.condition.get() ? BehaviorStatus.SUCCESS : BehaviorStatus.FAILURE;
      }

      public boolean shouldExecute() {
         return true;
      }
   }

   private static class WrappingPrecondition extends Precondition {
      private final Behavior wrapping;

      public WrappingPrecondition(Behavior wrapping, Condition condition) {
         super(condition);
         this.wrapping = wrapping;
      }

      public void reset() {
         this.wrapping.reset();
      }

      public BehaviorStatus run() {
         return this.wrapping.run();
      }

      public boolean shouldExecute() {
         return this.condition.get() ? this.wrapping.shouldExecute() : false;
      }
   }
}
