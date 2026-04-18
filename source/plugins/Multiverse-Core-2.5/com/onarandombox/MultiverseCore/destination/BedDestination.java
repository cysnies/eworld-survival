package com.onarandombox.MultiverseCore.destination;

import com.onarandombox.MultiverseCore.MultiverseCore;
import com.onarandombox.MultiverseCore.api.MVDestination;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.util.Vector;

public class BedDestination implements MVDestination {
   private boolean isValid;
   private Location knownBedLoc;
   private MultiverseCore plugin;

   public BedDestination() {
      super();
   }

   public String getIdentifier() {
      return "b";
   }

   public boolean isThisType(JavaPlugin plugin, String destination) {
      String[] split = destination.split(":");
      this.isValid = split.length >= 1 && split.length <= 2 && split[0].equals(this.getIdentifier());
      return this.isValid;
   }

   public Location getLocation(Entity entity) {
      if (entity instanceof Player) {
         this.knownBedLoc = this.plugin.getBlockSafety().getSafeBedSpawn(((Player)entity).getBedSpawnLocation());
         if (this.knownBedLoc == null) {
            ((Player)entity).sendMessage("Your bed was " + ChatColor.RED + "invalid or blocked" + ChatColor.RESET + ". Sorry.");
         }

         return this.knownBedLoc;
      } else {
         return null;
      }
   }

   public Vector getVelocity() {
      return new Vector();
   }

   public void setDestination(JavaPlugin plugin, String destination) {
      this.plugin = (MultiverseCore)plugin;
   }

   public boolean isValid() {
      return this.isValid;
   }

   public String getType() {
      return "Bed";
   }

   public String getName() {
      return "Bed";
   }

   public String getRequiredPermission() {
      return this.knownBedLoc != null ? "multiverse.access." + this.knownBedLoc.getWorld().getName() : "";
   }

   public boolean useSafeTeleporter() {
      return false;
   }

   public String toString() {
      return "b:playerbed";
   }
}
