package net.citizensnpcs.api.event;

import net.citizensnpcs.api.npc.NPC;
import org.bukkit.event.Cancellable;
import org.bukkit.event.HandlerList;

public class NPCDespawnEvent extends NPCEvent implements Cancellable {
   private boolean cancelled;
   private final DespawnReason reason;
   private static final HandlerList handlers = new HandlerList();

   public NPCDespawnEvent(NPC npc, DespawnReason reason) {
      super(npc);
      this.reason = reason;
   }

   public HandlerList getHandlers() {
      return handlers;
   }

   public DespawnReason getReason() {
      return this.reason;
   }

   public boolean isCancelled() {
      return this.cancelled;
   }

   public void setCancelled(boolean cancel) {
      this.cancelled = cancel;
   }

   public static HandlerList getHandlerList() {
      return handlers;
   }
}
