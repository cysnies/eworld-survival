package net.citizensnpcs.api.event;

import net.citizensnpcs.api.npc.NPC;
import org.bukkit.entity.LivingEntity;
import org.bukkit.event.HandlerList;

public class NPCVehicleExitEvent extends NPCEvent {
   private final LivingEntity entity;
   private static final HandlerList handlers = new HandlerList();

   public NPCVehicleExitEvent(NPC npc, LivingEntity entity) {
      super(npc);
      this.entity = entity;
   }

   public LivingEntity getExited() {
      return this.entity;
   }

   public HandlerList getHandlers() {
      return handlers;
   }

   public static HandlerList getHandlerList() {
      return handlers;
   }
}
