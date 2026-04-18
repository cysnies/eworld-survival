package net.citizensnpcs.api.event;

import net.citizensnpcs.api.npc.NPC;
import org.bukkit.block.Block;
import org.bukkit.event.HandlerList;
import org.bukkit.event.entity.EntityCombustByBlockEvent;

public class NPCCombustByBlockEvent extends NPCCombustEvent {
   private final EntityCombustByBlockEvent event;
   private static final HandlerList handlers = new HandlerList();

   public NPCCombustByBlockEvent(EntityCombustByBlockEvent event, NPC npc) {
      super(event, npc);
      this.event = event;
   }

   public Block getCombuster() {
      return this.event.getCombuster();
   }

   public HandlerList getHandlers() {
      return handlers;
   }

   public static HandlerList getHandlerList() {
      return handlers;
   }
}
