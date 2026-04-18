package net.citizensnpcs.api.event;

import net.citizensnpcs.api.npc.NPC;
import org.bukkit.event.Cancellable;
import org.bukkit.event.HandlerList;
import org.bukkit.util.Vector;

public class NPCPushEvent extends NPCEvent implements Cancellable {
   private boolean cancelled;
   private Vector collisionVector;
   private static final HandlerList handlers = new HandlerList();

   public NPCPushEvent(NPC npc, Vector vector) {
      super(npc);
      this.collisionVector = vector;
   }

   public Vector getCollisionVector() {
      return this.collisionVector;
   }

   public HandlerList getHandlers() {
      return handlers;
   }

   public boolean isCancelled() {
      return this.cancelled;
   }

   public void setCancelled(boolean cancel) {
      this.cancelled = cancel;
   }

   public void setCollisionVector(Vector vector) {
      this.collisionVector = vector;
   }

   public static HandlerList getHandlerList() {
      return handlers;
   }
}
