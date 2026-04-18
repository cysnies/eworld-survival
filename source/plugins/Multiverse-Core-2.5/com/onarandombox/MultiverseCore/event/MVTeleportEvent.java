package com.onarandombox.MultiverseCore.event;

import com.onarandombox.MultiverseCore.api.MVDestination;
import org.bukkit.Location;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

public class MVTeleportEvent extends Event implements Cancellable {
   private Player teleportee;
   private CommandSender teleporter;
   private MVDestination dest;
   private boolean useSafeTeleport;
   private boolean isCancelled;
   private static final HandlerList HANDLERS = new HandlerList();

   public MVTeleportEvent(MVDestination dest, Player teleportee, CommandSender teleporter, boolean safeTeleport) {
      super();
      this.teleportee = teleportee;
      this.teleporter = teleporter;
      this.dest = dest;
      this.useSafeTeleport = safeTeleport;
   }

   public HandlerList getHandlers() {
      return HANDLERS;
   }

   public static HandlerList getHandlerList() {
      return HANDLERS;
   }

   public Player getTeleportee() {
      return this.teleportee;
   }

   public Location getFrom() {
      return this.teleportee.getLocation();
   }

   public CommandSender getTeleporter() {
      return this.teleporter;
   }

   public MVDestination getDestination() {
      return this.dest;
   }

   public boolean isUsingSafeTTeleporter() {
      return this.useSafeTeleport;
   }

   public boolean isCancelled() {
      return this.isCancelled;
   }

   public void setCancelled(boolean cancel) {
      this.isCancelled = cancel;
   }
}
