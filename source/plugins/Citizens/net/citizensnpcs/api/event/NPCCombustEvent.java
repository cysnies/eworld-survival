package net.citizensnpcs.api.event;

import net.citizensnpcs.api.npc.NPC;
import org.bukkit.event.Cancellable;
import org.bukkit.event.HandlerList;
import org.bukkit.event.entity.EntityCombustEvent;

public class NPCCombustEvent extends NPCEvent implements Cancellable {
   private boolean cancelled;
   private final EntityCombustEvent event;
   private static final HandlerList handlers = new HandlerList();

   public NPCCombustEvent(EntityCombustEvent event, NPC npc) {
      super(npc);
      this.event = event;
   }

   public int getDuration() {
      return this.event.getDuration();
   }

   public HandlerList getHandlers() {
      return handlers;
   }

   public boolean isCancelled() {
      return this.cancelled;
   }

   public void setCancelled(boolean cancelled) {
      this.cancelled = cancelled;
   }

   public void setDuration(int duration) {
      this.event.setDuration(duration);
   }

   public static HandlerList getHandlerList() {
      return handlers;
   }
}
