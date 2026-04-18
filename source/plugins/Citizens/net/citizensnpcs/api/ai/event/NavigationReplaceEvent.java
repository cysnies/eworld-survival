package net.citizensnpcs.api.ai.event;

import net.citizensnpcs.api.ai.Navigator;
import org.bukkit.event.HandlerList;

public class NavigationReplaceEvent extends NavigationCancelEvent {
   private static final HandlerList handlers = new HandlerList();

   public NavigationReplaceEvent(Navigator navigator) {
      super(navigator, CancelReason.REPLACE);
   }

   public HandlerList getHandlers() {
      return handlers;
   }

   public static HandlerList getHandlerList() {
      return handlers;
   }
}
