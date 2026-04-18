package net.citizensnpcs.api.ai.event;

import net.citizensnpcs.api.ai.Navigator;
import org.bukkit.event.HandlerList;

public class NavigationBeginEvent extends NavigationEvent {
   private static final HandlerList handlers = new HandlerList();

   public NavigationBeginEvent(Navigator navigator) {
      super(navigator);
   }

   public HandlerList getHandlers() {
      return handlers;
   }

   public static HandlerList getHandlerList() {
      return handlers;
   }
}
