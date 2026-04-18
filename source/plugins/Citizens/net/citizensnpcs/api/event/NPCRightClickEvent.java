package net.citizensnpcs.api.event;

import net.citizensnpcs.api.npc.NPC;
import org.bukkit.entity.Player;
import org.bukkit.event.HandlerList;

public class NPCRightClickEvent extends NPCClickEvent {
   private static final HandlerList handlers = new HandlerList();

   public NPCRightClickEvent(NPC npc, Player rightClicker) {
      super(npc, rightClicker);
   }

   public HandlerList getHandlers() {
      return handlers;
   }

   public static HandlerList getHandlerList() {
      return handlers;
   }
}
