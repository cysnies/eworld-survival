package com.onarandombox.MultiverseCore.event;

import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

public class MVPlayerTouchedPortalEvent extends Event implements Cancellable {
   private Player p;
   private Location l;
   private boolean isCancelled;
   private boolean canUse = true;
   private static final HandlerList HANDLERS = new HandlerList();

   public MVPlayerTouchedPortalEvent(Player p, Location l) {
      super();
      this.p = p;
      this.l = l;
   }

   public HandlerList getHandlers() {
      return HANDLERS;
   }

   public static HandlerList getHandlerList() {
      return HANDLERS;
   }

   public Location getBlockTouched() {
      return this.l;
   }

   public Player getPlayer() {
      return this.p;
   }

   public boolean canUseThisPortal() {
      return this.canUse;
   }

   public void setCanUseThisPortal(boolean canUse) {
      this.canUse = canUse;
   }

   public boolean isCancelled() {
      return this.isCancelled;
   }

   public void setCancelled(boolean b) {
      this.isCancelled = b;
   }
}
