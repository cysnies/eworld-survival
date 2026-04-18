package net.citizensnpcs.api.ai.tree;

public class Empty extends BehaviorGoalAdapter {
   public static Empty INSTANCE = new Empty();

   private Empty() {
      super();
   }

   public void reset() {
   }

   public BehaviorStatus run() {
      return null;
   }

   public boolean shouldExecute() {
      return false;
   }
}
