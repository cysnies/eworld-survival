package com.onarandombox.MultiverseCore.destination;

import com.onarandombox.MultiverseCore.MultiverseCore;
import com.onarandombox.MultiverseCore.api.MVDestination;
import java.util.Arrays;
import java.util.List;
import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.util.Vector;

public class CannonDestination implements MVDestination {
   private final String coordRegex = "(-?[\\d]+\\.?[\\d]*),(-?[\\d]+\\.?[\\d]*),(-?[\\d]+\\.?[\\d]*)";
   private boolean isValid;
   private Location location;
   private double speed;
   private static final int SPLIT_SIZE = 6;

   public CannonDestination() {
      super();
   }

   public Vector getVelocity() {
      double pitchRadians = Math.toRadians((double)this.location.getPitch());
      double yawRadians = Math.toRadians((double)this.location.getYaw());
      double x = Math.sin(yawRadians) * this.speed * (double)-1.0F;
      double y = Math.sin(pitchRadians) * this.speed * (double)-1.0F;
      double z = Math.cos(yawRadians) * this.speed;
      x = Math.cos(pitchRadians) * x;
      z = Math.cos(pitchRadians) * z;
      return new Vector(x, y, z);
   }

   public String getIdentifier() {
      return "ca";
   }

   public boolean isThisType(JavaPlugin plugin, String destination) {
      if (!(plugin instanceof MultiverseCore)) {
         return false;
      } else {
         List<String> parsed = Arrays.asList(destination.split(":"));
         if (parsed.size() != 6) {
            return false;
         } else if (!((String)parsed.get(0)).equalsIgnoreCase("ca")) {
            return false;
         } else if (!((MultiverseCore)plugin).getMVWorldManager().isMVWorld((String)parsed.get(1))) {
            return false;
         } else if (!((String)parsed.get(2)).matches("(-?[\\d]+\\.?[\\d]*),(-?[\\d]+\\.?[\\d]*),(-?[\\d]+\\.?[\\d]*)")) {
            return false;
         } else {
            try {
               Float.parseFloat((String)parsed.get(3));
               Float.parseFloat((String)parsed.get(4));
               Float.parseFloat((String)parsed.get(5));
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
         if (parsed.size() != 6) {
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

               try {
                  this.location.setPitch(Float.parseFloat((String)parsed.get(3)));
                  this.location.setYaw(Float.parseFloat((String)parsed.get(4)));
                  this.speed = (double)Math.abs(Float.parseFloat((String)parsed.get(5)));
               } catch (NumberFormatException var8) {
                  this.isValid = false;
                  return;
               }

               this.isValid = true;
            }
         }
      }
   }

   public String getType() {
      return "Cannon!";
   }

   public String getName() {
      return "Cannon (" + this.location.getX() + ", " + this.location.getY() + ", " + this.location.getZ() + ":" + this.location.getPitch() + ":" + this.location.getYaw() + ":" + this.speed + ")";
   }

   public void setDestination(Location location, double speed) {
      if (location != null) {
         this.location = location;
         this.speed = Math.abs(speed);
         this.isValid = true;
      }

      this.isValid = false;
   }

   public String getRequiredPermission() {
      return "multiverse.access." + this.location.getWorld().getName();
   }

   public boolean useSafeTeleporter() {
      return false;
   }

   public String toString() {
      return this.isValid ? "ca:" + this.location.getWorld().getName() + ":" + this.location.getX() + "," + this.location.getY() + "," + this.location.getZ() + ":" + this.location.getPitch() + ":" + this.location.getYaw() + ":" + this.speed : "i:Invalid Destination";
   }
}
