package net.citizensnpcs.api.ai;

import com.google.common.collect.Lists;
import java.util.List;
import net.citizensnpcs.api.ai.event.NavigatorCallback;
import net.citizensnpcs.api.astar.pathfinder.BlockExaminer;

public class NavigatorParameters implements Cloneable {
   private AttackStrategy attackStrategy;
   private boolean avoidWater;
   private float baseSpeed = 1.0F;
   private final List callbacks = Lists.newArrayListWithExpectedSize(3);
   private AttackStrategy defaultStrategy;
   private double distanceMargin = (double)2.0F;
   private final List examiners = Lists.newArrayList();
   private float range;
   private float speedModifier = 1.0F;
   private int stationaryTicks = -1;
   private StuckAction stuckAction;
   private boolean useNewPathfinder;

   public NavigatorParameters() {
      super();
   }

   public NavigatorParameters addSingleUseCallback(NavigatorCallback callback) {
      this.callbacks.add(callback);
      return this;
   }

   public AttackStrategy attackStrategy() {
      return this.attackStrategy == null ? this.defaultStrategy : this.attackStrategy;
   }

   public void attackStrategy(AttackStrategy strategy) {
      this.attackStrategy = strategy;
   }

   public boolean avoidWater() {
      return this.avoidWater;
   }

   public NavigatorParameters avoidWater(boolean avoidWater) {
      this.avoidWater = avoidWater;
      return this;
   }

   public float baseSpeed() {
      return this.baseSpeed;
   }

   public NavigatorParameters baseSpeed(float speed) {
      this.baseSpeed = speed;
      return this;
   }

   public Iterable callbacks() {
      return this.callbacks;
   }

   public NavigatorParameters clearExaminers() {
      this.examiners.clear();
      return this;
   }

   public NavigatorParameters clone() {
      try {
         return (NavigatorParameters)super.clone();
      } catch (CloneNotSupportedException var2) {
         return null;
      }
   }

   public AttackStrategy defaultAttackStrategy() {
      return this.defaultStrategy;
   }

   public NavigatorParameters defaultAttackStrategy(AttackStrategy defaultStrategy) {
      this.defaultStrategy = defaultStrategy;
      return this;
   }

   public double distanceMargin() {
      return this.distanceMargin;
   }

   public NavigatorParameters distanceMargin(double newMargin) {
      this.distanceMargin = newMargin;
      return this;
   }

   public NavigatorParameters examiner(BlockExaminer examiner) {
      this.examiners.add(examiner);
      return this;
   }

   public BlockExaminer[] examiners() {
      return (BlockExaminer[])this.examiners.toArray(new BlockExaminer[this.examiners.size()]);
   }

   public float modifiedSpeed(float toModify) {
      return toModify * this.speedModifier();
   }

   public float range() {
      return this.range;
   }

   public NavigatorParameters range(float range) {
      this.range = range;
      return this;
   }

   public float speed() {
      return this.modifiedSpeed(this.baseSpeed);
   }

   /** @deprecated */
   @Deprecated
   public NavigatorParameters speed(float speed) {
      this.baseSpeed = speed;
      return this;
   }

   public float speedModifier() {
      return this.speedModifier;
   }

   public NavigatorParameters speedModifier(float percent) {
      this.speedModifier = percent;
      return this;
   }

   public int stationaryTicks() {
      return this.stationaryTicks;
   }

   public NavigatorParameters stationaryTicks(int ticks) {
      this.stationaryTicks = ticks;
      return this;
   }

   public StuckAction stuckAction() {
      return this.stuckAction;
   }

   public NavigatorParameters stuckAction(StuckAction action) {
      this.stuckAction = action;
      return this;
   }

   public boolean useNewPathfinder() {
      return this.useNewPathfinder;
   }

   public NavigatorParameters useNewPathfinder(boolean use) {
      this.useNewPathfinder = use;
      return this;
   }
}
