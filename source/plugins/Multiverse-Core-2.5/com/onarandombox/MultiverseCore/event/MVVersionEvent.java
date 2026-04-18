package com.onarandombox.MultiverseCore.event;

import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

public class MVVersionEvent extends Event {
   private final StringBuilder versionInfoBuilder;
   private static final HandlerList HANDLERS = new HandlerList();

   public MVVersionEvent(String versionInfo) {
      super();
      this.versionInfoBuilder = new StringBuilder(versionInfo);
   }

   public HandlerList getHandlers() {
      return HANDLERS;
   }

   public static HandlerList getHandlerList() {
      return HANDLERS;
   }

   public String getVersionInfo() {
      return this.versionInfoBuilder.toString();
   }

   public void appendVersionInfo(String moreVersionInfo) {
      this.versionInfoBuilder.append(moreVersionInfo);
   }
}
