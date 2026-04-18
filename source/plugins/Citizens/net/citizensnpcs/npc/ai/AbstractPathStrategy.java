package net.citizensnpcs.npc.ai;

import net.citizensnpcs.api.ai.TargetType;
import net.citizensnpcs.api.ai.event.CancelReason;

public abstract class AbstractPathStrategy implements PathStrategy {
   private CancelReason cancelReason;
   private final TargetType type;

   protected AbstractPathStrategy(TargetType type) {
      super();
      this.type = type;
   }

   public void clearCancelReason() {
      this.cancelReason = null;
   }

   public CancelReason getCancelReason() {
      return this.cancelReason;
   }

   public TargetType getTargetType() {
      return this.type;
   }

   protected void setCancelReason(CancelReason reason) {
      this.cancelReason = reason;
   }
}
