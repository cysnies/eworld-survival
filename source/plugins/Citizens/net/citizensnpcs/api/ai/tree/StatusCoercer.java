package net.citizensnpcs.api.ai.tree;

import com.google.common.base.Supplier;

public class StatusCoercer extends BehaviorGoalAdapter {
   private final Supplier to;
   private final Behavior wrapping;

   private StatusCoercer(Behavior wrapping, Supplier to) {
      super();
      this.wrapping = wrapping;
      this.to = to;
   }

   public void reset() {
      this.wrapping.reset();
   }

   public BehaviorStatus run() {
      return (BehaviorStatus)this.to.get();
   }

   public boolean shouldExecute() {
      return this.wrapping.shouldExecute();
   }

   public static StatusCoercer coercing(Behavior wrapping, Supplier to) {
      return new StatusCoercer(wrapping, to);
   }
}
