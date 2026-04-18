package com.onarandombox.MultiverseCore.event;

import java.util.List;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

public class MVConfigReloadEvent extends Event {
   private List configsLoaded;
   private static final HandlerList HANDLERS = new HandlerList();

   public MVConfigReloadEvent(List configsLoaded) {
      super();
      this.configsLoaded = configsLoaded;
   }

   public HandlerList getHandlers() {
      return HANDLERS;
   }

   public static HandlerList getHandlerList() {
      return HANDLERS;
   }

   public void addConfig(String config) {
      this.configsLoaded.add(config);
   }

   public List getAllConfigsLoaded() {
      return this.configsLoaded;
   }
}
