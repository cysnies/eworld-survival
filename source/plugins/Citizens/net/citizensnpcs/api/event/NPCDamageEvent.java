package net.citizensnpcs.api.event;

import net.citizensnpcs.api.npc.NPC;
import org.bukkit.event.Cancellable;
import org.bukkit.event.HandlerList;
import org.bukkit.event.entity.EntityDamageEvent;

public class NPCDamageEvent extends NPCEvent implements Cancellable {
   private final EntityDamageEvent event;
   private static final HandlerList handlers = new HandlerList();

   public NPCDamageEvent(NPC npc, EntityDamageEvent event) {
      super(npc);
      this.event = event;
   }

   public EntityDamageEvent.DamageCause getCause() {
      return this.event.getCause();
   }

   public double getDamage() {
      return this.event.getDamage();
   }

   public HandlerList getHandlers() {
      return handlers;
   }

   public boolean isCancelled() {
      return this.event.isCancelled();
   }

   public void setCancelled(boolean cancel) {
      this.event.setCancelled(cancel);
   }

   public void setDamage(int damage) {
      this.event.setDamage((double)damage);
   }

   public static HandlerList getHandlerList() {
      return handlers;
   }
}
