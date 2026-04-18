package net.citizensnpcs.api.ai.tree;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;

public class Sequence extends Composite {
   private Behavior executing;
   private int executingIndex;
   private final boolean retryChildren;

   private Sequence(boolean retryChildren, Behavior... behaviors) {
      this(retryChildren, (Collection)Arrays.asList(behaviors));
   }

   private Sequence(boolean retryChildren, Collection behaviors) {
      super(behaviors);
      this.retryChildren = retryChildren;
   }

   private BehaviorStatus getContinuationStatus() {
      this.resetCurrent();
      if (this.retryChildren) {
         return ++this.executingIndex >= this.getBehaviors().size() ? BehaviorStatus.FAILURE : BehaviorStatus.RUNNING;
      } else {
         return BehaviorStatus.FAILURE;
      }
   }

   public void reset() {
      super.reset();
      this.resetCurrent();
      this.executingIndex = 0;
   }

   private void resetCurrent() {
      this.stopExecution(this.executing);
      this.executing = null;
   }

   public BehaviorStatus run() {
      this.tickParallel();
      List<Behavior> behaviors = this.getBehaviors();
      if (this.executing == null) {
         BehaviorStatus next = this.selectNext(behaviors);
         if (next != BehaviorStatus.RUNNING) {
            this.resetCurrent();
            return next;
         }
      }

      BehaviorStatus status = this.executing.run();
      switch (status) {
         case RUNNING:
            return BehaviorStatus.RUNNING;
         case FAILURE:
            return this.getContinuationStatus();
         case RESET_AND_REMOVE:
            behaviors.remove(this.executingIndex);
            return this.selectNext(behaviors);
         case SUCCESS:
            this.resetCurrent();
            ++this.executingIndex;
            return this.selectNext(behaviors);
         default:
            throw new IllegalStateException();
      }
   }

   private BehaviorStatus selectNext(List behaviors) {
      if (this.executingIndex >= behaviors.size()) {
         return BehaviorStatus.SUCCESS;
      } else {
         while((this.executing = (Behavior)behaviors.get(this.executingIndex)) instanceof ParallelBehavior) {
            this.addParallel(this.executing);
            if (++this.executingIndex >= behaviors.size()) {
               return BehaviorStatus.SUCCESS;
            }
         }

         if (!this.executing.shouldExecute()) {
            return this.getContinuationStatus();
         } else {
            this.prepareForExecution(this.executing);
            return BehaviorStatus.RUNNING;
         }
      }
   }

   public String toString() {
      return "Sequence [executing=" + this.executing + ", executingIndex=" + this.executingIndex + ", retryChildren=" + this.retryChildren + ", getBehaviors()=" + this.getBehaviors() + "]";
   }

   public static Sequence createRetryingSequence(Behavior... behaviors) {
      return createRetryingSequence((Collection)Arrays.asList(behaviors));
   }

   public static Sequence createRetryingSequence(Collection behaviors) {
      return new Sequence(true, behaviors);
   }

   public static Sequence createSequence(Behavior... behaviors) {
      return createSequence((Collection)Arrays.asList(behaviors));
   }

   public static Sequence createSequence(Collection behaviors) {
      return new Sequence(false, behaviors);
   }
}
