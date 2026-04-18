package net.citizensnpcs.api.event;

import net.citizensnpcs.api.npc.NPC;
import org.bukkit.entity.Entity;
import org.bukkit.event.Cancellable;
import org.bukkit.event.HandlerList;
import org.bukkit.event.entity.EntityTargetEvent;

public class EntityTargetNPCEvent extends NPCEvent implements Cancellable {
   private boolean cancelled;
   private final EntityTargetEvent event;
   private static final HandlerList handlers = new HandlerList();

   public EntityTargetNPCEvent(EntityTargetEvent event, NPC npc) {
      super(npc);
      this.event = event;
   }

   public Entity getEntity() {
      return this.event.getEntity();
   }

   public HandlerList getHandlers() {
      return handlers;
   }

   public EntityTargetEvent.TargetReason getReason() {
      return this.event.getReason();
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
