package net.citizensnpcs.api.event;

import net.citizensnpcs.api.npc.NPC;
import org.bukkit.block.Block;
import org.bukkit.event.HandlerList;
import org.bukkit.event.entity.EntityDamageByBlockEvent;

public class NPCDamageByBlockEvent extends NPCDamageEvent {
   private final Block damager;
   private static final HandlerList handlers = new HandlerList();

   public NPCDamageByBlockEvent(NPC npc, EntityDamageByBlockEvent event) {
      super(npc, event);
      this.damager = event.getDamager();
   }

   public Block getDamager() {
      return this.damager;
   }

   public HandlerList getHandlers() {
      return handlers;
   }

   public static HandlerList getHandlerList() {
      return handlers;
   }
}
