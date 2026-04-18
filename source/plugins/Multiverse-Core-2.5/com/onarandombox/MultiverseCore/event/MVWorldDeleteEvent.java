package com.onarandombox.MultiverseCore.event;

import com.onarandombox.MultiverseCore.api.MultiverseWorld;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

public class MVWorldDeleteEvent extends Event implements Cancellable {
   private boolean cancelled = false;
   private final MultiverseWorld world;
   private final boolean removeFromConfig;
   private static final HandlerList HANDLERS = new HandlerList();

   public MVWorldDeleteEvent(MultiverseWorld world, boolean removeFromConfig) {
      super();
      if (world == null) {
         throw new IllegalArgumentException("world can't be null!");
      } else {
         this.world = world;
         this.removeFromConfig = removeFromConfig;
      }
   }

   public HandlerList getHandlers() {
      return HANDLERS;
   }

   public static HandlerList getHandlerList() {
      return HANDLERS;
   }

   public boolean isCancelled() {
      return this.cancelled;
   }

   public void setCancelled(boolean cancel) {
      this.cancelled = cancel;
   }

   public MultiverseWorld getWorld() {
      return this.world;
   }

   public boolean removeWorldFromConfig() {
      return this.removeFromConfig;
   }
}
