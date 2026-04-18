package net.citizensnpcs.api.event;

import net.citizensnpcs.api.npc.NPC;
import org.bukkit.event.HandlerList;

public class NPCRemoveEvent extends NPCEvent {
   private static final HandlerList handlers = new HandlerList();

   public NPCRemoveEvent(NPC npc) {
      super(npc);
   }

   public HandlerList getHandlers() {
      return handlers;
   }

   public static HandlerList getHandlerList() {
      return handlers;
   }
}
