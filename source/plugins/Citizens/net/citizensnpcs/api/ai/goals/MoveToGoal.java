package net.citizensnpcs.api.ai.goals;

import javax.annotation.Nullable;
import net.citizensnpcs.api.ai.event.CancelReason;
import net.citizensnpcs.api.ai.event.NavigatorCallback;
import net.citizensnpcs.api.ai.tree.BehaviorGoalAdapter;
import net.citizensnpcs.api.ai.tree.BehaviorStatus;
import net.citizensnpcs.api.npc.NPC;
import org.bukkit.Location;

public class MoveToGoal extends BehaviorGoalAdapter {
   private boolean finished;
   private final NPC npc;
   private CancelReason reason;
   private final Location target;

   public MoveToGoal(NPC npc, Location target) {
      super();
      this.npc = npc;
      this.target = target;
   }

   public void reset() {
      this.npc.getNavigator().cancelNavigation();
      this.reason = null;
      this.finished = false;
   }

   public BehaviorStatus run() {
      if (this.finished) {
         return this.reason == null ? BehaviorStatus.SUCCESS : BehaviorStatus.FAILURE;
      } else {
         return BehaviorStatus.RUNNING;
      }
   }

   public boolean shouldExecute() {
      boolean executing = !this.npc.getNavigator().isNavigating() && this.target != null;
      if (executing) {
         this.npc.getNavigator().setTarget(this.target);
         this.npc.getNavigator().getLocalParameters().addSingleUseCallback(new NavigatorCallback() {
            public void onCompletion(@Nullable CancelReason cancelReason) {
               MoveToGoal.this.finished = true;
               MoveToGoal.this.reason = cancelReason;
            }
         });
      }

      return executing;
   }
}
