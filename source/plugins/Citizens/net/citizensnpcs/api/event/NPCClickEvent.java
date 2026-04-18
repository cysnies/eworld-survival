package net.citizensnpcs.api.event;

import net.citizensnpcs.api.npc.NPC;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.HandlerList;

public abstract class NPCClickEvent extends NPCEvent implements Cancellable {
   private boolean cancelled = false;
   private final Player clicker;
   private static final HandlerList handlers = new HandlerList();

   protected NPCClickEvent(NPC npc, Player clicker) {
      super(npc);
      this.clicker = clicker;
   }

   public Player getClicker() {
      return this.clicker;
   }

   public HandlerList getHandlers() {
      return handlers;
   }

   public boolean isCancelled() {
      return this.cancelled;
   }

   public void setCancelled(boolean cancelled) {
      this.cancelled = cancelled;
   }

   public static HandlerList getHandlerList() {
      return handlers;
   }
}
