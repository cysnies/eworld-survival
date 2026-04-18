package net.citizensnpcs.api.event;

import org.bukkit.event.HandlerList;

public class CitizensEnableEvent extends CitizensEvent {
   private static final HandlerList handlers = new HandlerList();

   public CitizensEnableEvent() {
      super();
   }

   public HandlerList getHandlers() {
      return handlers;
   }

   public static HandlerList getHandlerList() {
      return handlers;
   }
}
