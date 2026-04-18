package com.onarandombox.MultiverseCore.event;

import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

public class MVRespawnEvent extends Event {
   private Player player;
   private Location location;
   private String respawnMethod;
   private static final HandlerList HANDLERS = new HandlerList();

   public MVRespawnEvent(Location spawningAt, Player p, String respawnMethod) {
      super();
      this.player = p;
      this.location = spawningAt;
      this.respawnMethod = respawnMethod;
   }

   public HandlerList getHandlers() {
      return HANDLERS;
   }

   public static HandlerList getHandlerList() {
      return HANDLERS;
   }

   public Player getPlayer() {
      return this.player;
   }

   public String getRespawnMethod() {
      return this.respawnMethod;
   }

   public Location getPlayersRespawnLocation() {
      return this.location;
   }

   public void setRespawnLocation(Location l) {
      this.location = l;
   }
}
