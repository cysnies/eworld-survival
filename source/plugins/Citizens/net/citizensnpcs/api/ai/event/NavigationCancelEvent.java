package net.citizensnpcs.api.ai.event;

import net.citizensnpcs.api.ai.Navigator;
import org.bukkit.event.HandlerList;

public class NavigationCancelEvent extends NavigationCompleteEvent {
   private final CancelReason reason;
   private static final HandlerList handlers = new HandlerList();

   public NavigationCancelEvent(Navigator navigator, CancelReason reason) {
      super(navigator);
      this.reason = reason;
   }

   public CancelReason getCancelReason() {
      return this.reason;
   }

   public HandlerList getHandlers() {
      return handlers;
   }

   public static HandlerList getHandlerList() {
      return handlers;
   }
}
