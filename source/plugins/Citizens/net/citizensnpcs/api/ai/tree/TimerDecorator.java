package net.citizensnpcs.api.ai.tree;

public class TimerDecorator extends BehaviorGoalAdapter {
   private final int limit;
   private int ticks;
   private final Behavior wrapping;

   private TimerDecorator(Behavior wrapping, int tickLimit) {
      super();
      this.limit = tickLimit;
      this.wrapping = wrapping;
   }

   public void reset() {
      this.ticks = 0;
      this.wrapping.reset();
   }

   public BehaviorStatus run() {
      ++this.ticks;
      return this.ticks >= this.limit ? BehaviorStatus.FAILURE : this.wrapping.run();
   }

   public boolean shouldExecute() {
      return this.wrapping.shouldExecute();
   }

   public static TimerDecorator tickLimiter(Behavior wrapping, int tickLimit) {
      return new TimerDecorator(wrapping, tickLimit);
   }
}
