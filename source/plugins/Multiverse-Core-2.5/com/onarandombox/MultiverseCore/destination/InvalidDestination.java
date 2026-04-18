package com.onarandombox.MultiverseCore.destination;

import com.onarandombox.MultiverseCore.api.MVDestination;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.util.Vector;

public class InvalidDestination implements MVDestination {
   public InvalidDestination() {
      super();
   }

   public String getIdentifier() {
      return "i";
   }

   public boolean isThisType(JavaPlugin plugin, String destination) {
      return false;
   }

   public Location getLocation(Entity e) {
      return null;
   }

   public boolean isValid() {
      return false;
   }

   public void setDestination(JavaPlugin plugin, String destination) {
   }

   public String getType() {
      return ChatColor.RED + "Invalid Destination";
   }

   public String getName() {
      return ChatColor.RED + "Invalid Destination";
   }

   public String toString() {
      return "i:Invalid Destination";
   }

   public String getRequiredPermission() {
      return null;
   }

   public Vector getVelocity() {
      return new Vector(0, 0, 0);
   }

   public boolean useSafeTeleporter() {
      return false;
   }
}
