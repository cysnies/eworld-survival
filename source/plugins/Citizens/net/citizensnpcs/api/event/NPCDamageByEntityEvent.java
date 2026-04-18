package net.citizensnpcs.api.event;

import net.citizensnpcs.api.npc.NPC;
import org.bukkit.entity.Entity;
import org.bukkit.event.HandlerList;
import org.bukkit.event.entity.EntityDamageByEntityEvent;

public class NPCDamageByEntityEvent extends NPCDamageEvent {
   private final Entity damager;
   private static final HandlerList handlers = new HandlerList();

   public NPCDamageByEntityEvent(NPC npc, EntityDamageByEntityEvent event) {
      super(npc, event);
      this.damager = event.getDamager();
   }

   public Entity getDamager() {
      return this.damager;
   }

   public HandlerList getHandlers() {
      return handlers;
   }

   public static HandlerList getHandlerList() {
      return handlers;
   }
}
