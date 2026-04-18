package net.citizensnpcs.api.event;

import net.citizensnpcs.api.npc.NPC;
import org.bukkit.entity.Entity;
import org.bukkit.event.HandlerList;
import org.bukkit.event.entity.EntityCombustByEntityEvent;

public class NPCCombustByEntityEvent extends NPCCombustEvent {
   private final EntityCombustByEntityEvent event;
   private static final HandlerList handlers = new HandlerList();

   public NPCCombustByEntityEvent(EntityCombustByEntityEvent event, NPC npc) {
      super(event, npc);
      this.event = event;
   }

   public Entity getCombuster() {
      return this.event.getCombuster();
   }

   public HandlerList getHandlers() {
      return handlers;
   }

   public static HandlerList getHandlerList() {
      return handlers;
   }
}
