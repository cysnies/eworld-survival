package com.onarandombox.MultiverseCore.destination;

import com.onarandombox.MultiverseCore.MultiverseCore;
import com.onarandombox.MultiverseCore.api.MVDestination;
import java.util.Arrays;
import java.util.List;
import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.util.Vector;

public class ExactDestination implements MVDestination {
   private final String coordRegex = "(-?[\\d]+\\.?[\\d]*),(-?[\\d]+\\.?[\\d]*),(-?[\\d]+\\.?[\\d]*)";
   private boolean isValid;
   private Location location;

   public ExactDestination() {
      super();
   }

   public String getIdentifier() {
      return "e";
   }

   public Vector getVelocity() {
      return new Vector(0, 0, 0);
   }

   public boolean isThisType(JavaPlugin plugin, String destination) {
      if (!(plugin instanceof MultiverseCore)) {
         return false;
      } else {
         List<String> parsed = Arrays.asList(destination.split(":"));
         if (parsed.size() != 3 && parsed.size() != 5) {
            return false;
         } else if (!((String)parsed.get(0)).equalsIgnoreCase("e")) {
            return false;
         } else if (!((MultiverseCore)plugin).getMVWorldManager().isMVWorld((String)parsed.get(1))) {
            return false;
         } else if (!((String)parsed.get(2)).matches("(-?[\\d]+\\.?[\\d]*),(-?[\\d]+\\.?[\\d]*),(-?[\\d]+\\.?[\\d]*)")) {
            return false;
         } else if (parsed.size() == 3) {
            return true;
         } else {
            try {
               Float.parseFloat((String)parsed.get(3));
               Float.parseFloat((String)parsed.get(4));
               return true;
            } catch (NumberFormatException var5) {
               return false;
            }
         }
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
         List<String> parsed = Arrays.asList(destination.split(":"));
         if (parsed.size() != 3 && parsed.size() != 5) {
            this.isValid = false;
         } else if (!((String)parsed.get(0)).equalsIgnoreCase(this.getIdentifier())) {
            this.isValid = false;
         } else if (!((MultiverseCore)plugin).getMVWorldManager().isMVWorld((String)parsed.get(1))) {
            this.isValid = false;
         } else {
            this.location = new Location(((MultiverseCore)plugin).getMVWorldManager().getMVWorld((String)parsed.get(1)).getCBWorld(), (double)0.0F, (double)0.0F, (double)0.0F);
            String var10000 = (String)parsed.get(2);
            this.getClass();
            if (!var10000.matches("(-?[\\d]+\\.?[\\d]*),(-?[\\d]+\\.?[\\d]*),(-?[\\d]+\\.?[\\d]*)")) {
               this.isValid = false;
            } else {
               double[] coords = new double[3];
               String[] coordString = ((String)parsed.get(2)).split(",");

               for(int i = 0; i < 3; ++i) {
                  try {
                     coords[i] = Double.parseDouble(coordString[i]);
                  } catch (NumberFormatException var9) {
                     this.isValid = false;
                     return;
                  }
               }

               this.location.setX(coords[0]);
               this.location.setY(coords[1]);
               this.location.setZ(coords[2]);
               if (parsed.size() == 3) {
                  this.isValid = true;
               } else {
                  try {
                     this.location.setPitch(Float.parseFloat((String)parsed.get(3)));
                     this.location.setYaw(Float.parseFloat((String)parsed.get(4)));
                  } catch (NumberFormatException var8) {
                     this.isValid = false;
                     return;
                  }

                  this.isValid = true;
               }
            }
         }
      }
   }

   public String getType() {
      return "Exact";
   }

   public String getName() {
      return "Exact (" + this.location.getX() + ", " + this.location.getY() + ", " + this.location.getZ() + ":" + this.location.getPitch() + ":" + this.location.getYaw() + ")";
   }

   public void setDestination(Location location) {
      if (location != null) {
         this.location = location;
         this.isValid = true;
      }

      this.isValid = false;
   }

   public String toString() {
      return this.isValid ? "e:" + this.location.getWorld().getName() + ":" + this.location.getX() + "," + this.location.getY() + "," + this.location.getZ() + ":" + this.location.getPitch() + ":" + this.location.getYaw() : "i:Invalid Destination";
   }

   public String getRequiredPermission() {
      return "multiverse.access." + this.location.getWorld().getName();
   }

   public boolean useSafeTeleporter() {
      return false;
   }
}
