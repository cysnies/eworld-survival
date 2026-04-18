package net.citizensnpcs.api.event;

import net.citizensnpcs.api.npc.NPC;
import org.bukkit.entity.Entity;
import org.bukkit.event.HandlerList;

public class NPCCollisionEvent extends NPCEvent {
   private final Entity entity;
   private static final HandlerList handlers = new HandlerList();

   public NPCCollisionEvent(NPC npc, Entity entity) {
      super(npc);
      this.entity = entity;
   }

   public Entity getCollidedWith() {
      return this.entity;
   }

   public HandlerList getHandlers() {
      return handlers;
   }

   public static HandlerList getHandlerList() {
      return handlers;
   }
}
