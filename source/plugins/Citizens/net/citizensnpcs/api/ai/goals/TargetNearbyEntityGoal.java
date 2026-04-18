package net.citizensnpcs.api.ai.goals;

import java.util.Collection;
import java.util.EnumSet;
import java.util.Set;
import javax.annotation.Nullable;
import net.citizensnpcs.api.ai.event.CancelReason;
import net.citizensnpcs.api.ai.event.NavigatorCallback;
import net.citizensnpcs.api.ai.tree.BehaviorGoalAdapter;
import net.citizensnpcs.api.ai.tree.BehaviorStatus;
import net.citizensnpcs.api.npc.NPC;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;

public class TargetNearbyEntityGoal extends BehaviorGoalAdapter {
   private final boolean aggressive;
   private boolean finished;
   private final NPC npc;
   private final double radius;
   private CancelReason reason;
   private Entity target;
   private final Set targets;

   private TargetNearbyEntityGoal(NPC npc, Set targets, boolean aggressive, double radius) {
      super();
      this.npc = npc;
      this.targets = targets;
      this.aggressive = aggressive;
      this.radius = radius;
   }

   public void reset() {
      this.npc.getNavigator().cancelNavigation();
      this.target = null;
      this.finished = false;
      this.reason = null;
   }

   public BehaviorStatus run() {
      if (this.finished) {
         return this.reason == null ? BehaviorStatus.SUCCESS : BehaviorStatus.FAILURE;
      } else {
         return BehaviorStatus.RUNNING;
      }
   }

   public boolean shouldExecute() {
      if (this.targets.size() != 0 && this.npc.isSpawned()) {
         Collection<Entity> nearby = this.npc.getBukkitEntity().getNearbyEntities(this.radius, this.radius, this.radius);
         this.target = null;

         for(Entity entity : nearby) {
            if (this.targets.contains(entity.getType())) {
               this.target = entity;
               break;
            }
         }

         if (this.target != null) {
            this.npc.getNavigator().setTarget(this.target, this.aggressive);
            this.npc.getNavigator().getLocalParameters().addSingleUseCallback(new NavigatorCallback() {
               public void onCompletion(@Nullable CancelReason cancelReason) {
                  TargetNearbyEntityGoal.this.reason = cancelReason;
                  TargetNearbyEntityGoal.this.finished = true;
               }
            });
            return true;
         } else {
            return false;
         }
      } else {
         return false;
      }
   }

   public static Builder builder(NPC npc) {
      return new Builder(npc);
   }

   public static class Builder {
      private boolean aggressive;
      private final NPC npc;
      private double radius = (double)10.0F;
      private Set targetTypes = EnumSet.noneOf(EntityType.class);

      public Builder(NPC npc) {
         super();
         this.npc = npc;
      }

      public Builder aggressive(boolean aggressive) {
         this.aggressive = aggressive;
         return this;
      }

      public TargetNearbyEntityGoal build() {
         return new TargetNearbyEntityGoal(this.npc, this.targetTypes, this.aggressive, this.radius);
      }

      public Builder radius(double radius) {
         this.radius = radius;
         return this;
      }

      public Builder targets(Set targetTypes) {
         this.targetTypes = targetTypes;
         return this;
      }
   }
}
