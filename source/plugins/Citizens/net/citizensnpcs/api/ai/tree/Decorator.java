package net.citizensnpcs.api.ai.tree;

import com.google.common.base.Function;
import com.google.common.base.Predicate;
import com.google.common.collect.Lists;
import java.util.Collection;
import java.util.Collections;

public class Decorator extends BehaviorGoalAdapter {
   private final Collection resetCallbacks;
   private final Collection runCallbacks;
   private final Collection shouldExecutePredicates;
   private final Collection statusTransformers;
   private final Behavior wrapping;

   private Decorator(Behavior toWrap, Collection runCallbacks, Collection statusTransformers, Collection shouldExecutePredicates, Collection resetCallbacks) {
      super();
      this.wrapping = toWrap;
      this.runCallbacks = runCallbacks;
      this.statusTransformers = statusTransformers;
      this.shouldExecutePredicates = shouldExecutePredicates;
      this.resetCallbacks = resetCallbacks;
   }

   public void reset() {
      for(Runnable runnable : this.resetCallbacks) {
         runnable.run();
      }

      this.wrapping.reset();
   }

   public BehaviorStatus run() {
      for(Runnable runnable : this.runCallbacks) {
         runnable.run();
      }

      BehaviorStatus status = this.wrapping.run();

      for(Function transformer : this.statusTransformers) {
         status = (BehaviorStatus)transformer.apply(status);
      }

      return status;
   }

   public boolean shouldExecute() {
      boolean shouldExecute = this.wrapping.shouldExecute();

      for(Predicate transformer : this.shouldExecutePredicates) {
         shouldExecute = transformer.apply(shouldExecute);
      }

      return shouldExecute;
   }

   public static Builder wrapping(Behavior toWrap) {
      return new Builder(toWrap);
   }

   public static class Builder {
      private Collection resetCallbacks;
      private Collection runCallbacks;
      private Collection shouldExecutePredicates;
      private Collection statusTransformers;
      private final Behavior toWrap;

      private Builder(Behavior toWrap) {
         super();
         this.resetCallbacks = Collections.emptyList();
         this.runCallbacks = Collections.emptyList();
         this.shouldExecutePredicates = Collections.emptyList();
         this.statusTransformers = Collections.emptyList();
         this.toWrap = toWrap;
      }

      public Decorator build() {
         return new Decorator(this.toWrap, this.runCallbacks, this.statusTransformers, this.shouldExecutePredicates, this.resetCallbacks);
      }

      public Builder withPreRunCallback(Runnable callback) {
         if (this.runCallbacks == Collections.EMPTY_LIST) {
            this.runCallbacks = Lists.newArrayList();
         }

         this.runCallbacks.add(callback);
         return this;
      }

      public Builder withResetCallback(Runnable callback) {
         if (this.resetCallbacks == Collections.EMPTY_LIST) {
            this.resetCallbacks = Lists.newArrayList();
         }

         this.resetCallbacks.add(callback);
         return this;
      }

      public Builder withShouldExecutePredicate(Predicate predicate) {
         if (this.shouldExecutePredicates == Collections.EMPTY_LIST) {
            this.shouldExecutePredicates = Lists.newArrayList();
         }

         this.shouldExecutePredicates.add(predicate);
         return this;
      }

      public Builder withStatusTransformer(Function transformer) {
         if (this.statusTransformers == Collections.EMPTY_LIST) {
            this.statusTransformers = Lists.newArrayList();
         }

         this.statusTransformers.add(transformer);
         return this;
      }
   }
}
