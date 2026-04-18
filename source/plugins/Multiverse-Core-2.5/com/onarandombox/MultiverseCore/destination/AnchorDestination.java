package com.onarandombox.MultiverseCore.destination;

import com.onarandombox.MultiverseCore.MultiverseCore;
import com.onarandombox.MultiverseCore.api.MVDestination;
import java.util.Arrays;
import java.util.List;
import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.util.Vector;

public class AnchorDestination implements MVDestination {
   private boolean isValid;
   private Location location;
   private MultiverseCore plugin;
   private String name;

   public AnchorDestination() {
      super();
   }

   public String getIdentifier() {
      return "a";
   }

   public Vector getVelocity() {
      return new Vector(0, 0, 0);
   }

   public boolean isThisType(JavaPlugin plugin, String destination) {
      if (!(plugin instanceof MultiverseCore)) {
         return false;
      } else {
         this.plugin = (MultiverseCore)plugin;
         List<String> parsed = Arrays.asList(destination.split(":"));
         return parsed.size() != 2 ? false : ((String)parsed.get(0)).equalsIgnoreCase("a");
      }
   }

   public Location getLocation(Entity e) {
      return this.location;
   }

   public boolean isValid() {
      return this.isValid;
   }

   public void setDestination(JavaPlugin plugin, String destination) {
      if (plugin instanceof MultiverseCore) {
         this.plugin = (MultiverseCore)plugin;
         List<String> parsed = Arrays.asList(destination.split(":"));
         if (parsed.size() != 2) {
            this.isValid = false;
         } else {
            this.name = (String)parsed.get(1);
            this.location = this.plugin.getAnchorManager().getAnchorLocation((String)parsed.get(1));
            if (this.location == null) {
               this.isValid = false;
            } else {
               if (!((String)parsed.get(0)).equalsIgnoreCase(this.getIdentifier())) {
                  this.isValid = false;
               }

               this.isValid = true;
            }
         }
      }
   }

   public String getType() {
      return "Anchor";
   }

   public String getName() {
      return "Anchor: " + this.name;
   }

   public String toString() {
      return this.isValid ? "a:" + this.name : "i:Invalid Destination";
   }

   public String getRequiredPermission() {
      return "multiverse.access." + this.location.getWorld().getName();
   }

   public boolean useSafeTeleporter() {
      return false;
   }
}
