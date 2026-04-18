package net.citizensnpcs.api.ai.tree;

import com.google.common.base.Function;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Random;
import javax.annotation.Nullable;

public class Selector extends Composite {
   private Behavior executing;
   private boolean retryChildren;
   private final Function selectionFunction;
   private static final Random RANDOM = new Random();
   private static final Function RANDOM_SELECTION = new Function() {
      public Behavior apply(@Nullable List behaviors) {
         return (Behavior)behaviors.get(Selector.RANDOM.nextInt(behaviors.size()));
      }
   };

   private Selector(Function selectionFunction, boolean retryChildren, Collection behaviors) {
      super(behaviors);
      this.retryChildren = false;
      this.selectionFunction = selectionFunction;
      this.retryChildren = retryChildren;
   }

   public Behavior getNextBehavior() {
      Behavior behavior = null;

      while((behavior = (Behavior)this.selectionFunction.apply(this.getBehaviors())) instanceof ParallelBehavior) {
         this.addParallel(behavior);
      }

      return behavior;
   }

   public void reset() {
      super.reset();
      if (this.executing != null) {
         this.stopExecution(this.executing);
      }

      this.executing = null;
   }

   public BehaviorStatus run() {
      this.tickParallel();
      BehaviorStatus status = null;
      if (this.executing == null) {
         this.executing = this.getNextBehavior();
         if (this.executing == null) {
            return BehaviorStatus.FAILURE;
         }

         if (this.executing.shouldExecute()) {
            this.prepareForExecution(this.executing);
         } else {
            status = BehaviorStatus.FAILURE;
         }
      }

      if (status == null) {
         status = this.executing.run();
      }

      if (status == BehaviorStatus.FAILURE) {
         if (this.retryChildren) {
            this.stopExecution(this.executing);
            this.executing = null;
            return BehaviorStatus.RUNNING;
         }
      } else if (status == BehaviorStatus.RESET_AND_REMOVE) {
         this.getBehaviors().remove(this.executing);
         this.stopExecution(this.executing);
         this.executing = null;
         return BehaviorStatus.SUCCESS;
      }

      return status;
   }

   public String toString() {
      return "Selector [executing=" + this.executing + ", retryChildren=" + this.retryChildren + ", selectionFunction=" + this.selectionFunction + ", getBehaviors()=" + this.getBehaviors() + "]";
   }

   public static Builder selecting(Behavior... behaviors) {
      return selecting((Collection)Arrays.asList(behaviors));
   }

   public static Builder selecting(Collection behaviors) {
      return new Builder(behaviors);
   }

   public static class Builder {
      private final Collection behaviors;
      private boolean retryChildren;
      private Function selectionFunction;

      private Builder(Collection behaviors) {
         super();
         this.selectionFunction = Selector.RANDOM_SELECTION;
         this.behaviors = behaviors;
      }

      public Selector build() {
         return new Selector(this.selectionFunction, this.retryChildren, this.behaviors);
      }

      public Builder retryChildren() {
         this.retryChildren = true;
         return this;
      }

      public Builder retryChildren(boolean b) {
         this.retryChildren = b;
         return this;
      }

      public Builder selectionFunction(Function function) {
         this.selectionFunction = function;
         return this;
      }
   }
}
