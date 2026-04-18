package com.onarandombox.MultiverseCore.destination;

import com.onarandombox.MultiverseCore.api.MVDestination;
import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.util.Vector;

public class PlayerDestination implements MVDestination {
   private String player;
   private boolean isValid;
   private JavaPlugin plugin;

   public PlayerDestination() {
      super();
   }

   public String getIdentifier() {
      return "pl";
   }

   public boolean isThisType(JavaPlugin plugin, String destination) {
      String[] items = destination.split(":");
      if (items.length != 2) {
         return false;
      } else {
         return items[0].equalsIgnoreCase("pl");
      }
   }

   public Location getLocation(Entity e) {
      Player p = this.plugin.getServer().getPlayer(this.player);
      Player plLoc = null;
      if (e instanceof Player) {
         plLoc = (Player)e;
      } else if (e.getPassenger() instanceof Player) {
         plLoc = (Player)e.getPassenger();
      }

      return p != null && plLoc != null ? p.getLocation() : null;
   }

   public boolean isValid() {
      return this.isValid;
   }

   public void setDestination(JavaPlugin plugin, String destination) {
      String[] items = destination.split(":");
      if (items.length != 2) {
         this.isValid = false;
      }

      if (!items[0].equalsIgnoreCase("pl")) {
         this.isValid = false;
      }

      this.isValid = true;
      this.player = items[1];
      this.plugin = plugin;
   }

   public String getType() {
      return "Player";
   }

   public String getName() {
      return this.player;
   }

   public String toString() {
      return "pl:" + this.player;
   }

   public String getRequiredPermission() {
      return "";
   }

   public Vector getVelocity() {
      return new Vector(0, 0, 0);
   }

   public boolean useSafeTeleporter() {
      return true;
   }
}
