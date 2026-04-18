package net.citizensnpcs.api.event;

import net.citizensnpcs.api.npc.NPC;
import net.citizensnpcs.api.trait.Trait;
import org.bukkit.event.HandlerList;

public class NPCAddTraitEvent extends NPCTraitEvent {
   private static final HandlerList handlers = new HandlerList();

   public NPCAddTraitEvent(NPC npc, Trait trait) {
      super(npc, trait);
   }

   public HandlerList getHandlers() {
      return handlers;
   }

   public static HandlerList getHandlerList() {
      return handlers;
   }
}
