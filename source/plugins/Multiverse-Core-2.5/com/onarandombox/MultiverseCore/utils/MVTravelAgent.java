package com.onarandombox.MultiverseCore.utils;

import com.onarandombox.MultiverseCore.MultiverseCore;
import com.onarandombox.MultiverseCore.api.MVDestination;
import com.onarandombox.MultiverseCore.destination.CannonDestination;
import java.util.logging.Level;
import org.bukkit.Location;
import org.bukkit.TravelAgent;
import org.bukkit.entity.Player;

public class MVTravelAgent implements TravelAgent {
   private MVDestination destination;
   private MultiverseCore core;
   private Player player;

   public MVTravelAgent(MultiverseCore multiverseCore, MVDestination d, Player p) {
      super();
      this.destination = d;
      this.core = multiverseCore;
      this.player = p;
   }

   public TravelAgent setSearchRadius(int radius) {
      return this;
   }

   public int getSearchRadius() {
      return 0;
   }

   public TravelAgent setCreationRadius(int radius) {
      return this;
   }

   public int getCreationRadius() {
      return 0;
   }

   public boolean getCanCreatePortal() {
      return false;
   }

   public void setCanCreatePortal(boolean create) {
   }

   public Location findOrCreate(Location location) {
      return this.getSafeLocation();
   }

   public Location findPortal(Location location) {
      return this.getSafeLocation();
   }

   public boolean createPortal(Location location) {
      return false;
   }

   private Location getSafeLocation() {
      if (this.destination instanceof CannonDestination) {
         this.core.log(Level.FINE, "Using Stock TP method. This cannon will have 0 velocity");
      }

      com.onarandombox.MultiverseCore.api.SafeTTeleporter teleporter = this.core.getSafeTTeleporter();
      Location newLoc = this.destination.getLocation(this.player);
      if (this.destination.useSafeTeleporter()) {
         newLoc = teleporter.getSafeLocation(this.player, this.destination);
      }

      return newLoc == null ? this.player.getLocation() : newLoc;
   }
}
