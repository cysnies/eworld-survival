package com.onarandombox.MultiverseCore.utils;

import java.text.DecimalFormat;
import java.util.Collections;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Vehicle;
import org.bukkit.util.Vector;

/** @deprecated */
@Deprecated
public class LocationManipulation {
   private static Map orientationInts = new HashMap();

   private LocationManipulation() {
      super();
   }

   public static String locationToString(Location location) {
      return location == null ? "" : String.format(Locale.ENGLISH, "%s:%.2f,%.2f,%.2f:%.2f:%.2f", location.getWorld().getName(), location.getX(), location.getY(), location.getZ(), location.getYaw(), location.getPitch());
   }

   public static Location getBlockLocation(Location l) {
      l.setX((double)l.getBlockX());
      l.setY((double)l.getBlockY());
      l.setZ((double)l.getBlockZ());
      return l;
   }

   public static Location stringToLocation(String locationString) {
      if (locationString == null) {
         return null;
      } else {
         String[] split = locationString.split(":");
         if (split.length >= 2 && split.length <= 4) {
            String[] xyzsplit = split[1].split(",");
            if (xyzsplit.length != 3) {
               return null;
            } else {
               World w = Bukkit.getWorld(split[0]);
               if (w == null) {
                  return null;
               } else {
                  try {
                     float pitch = 0.0F;
                     float yaw = 0.0F;
                     if (split.length >= 3) {
                        yaw = (float)Double.parseDouble(split[2]);
                     }

                     if (split.length == 4) {
                        pitch = (float)Double.parseDouble(split[3]);
                     }

                     return new Location(w, Double.parseDouble(xyzsplit[0]), Double.parseDouble(xyzsplit[1]), Double.parseDouble(xyzsplit[2]), yaw, pitch);
                  } catch (NumberFormatException var6) {
                     return null;
                  }
               }
            }
         } else {
            return null;
         }
      }
   }

   public static String strCoords(Location l) {
      String result = "";
      DecimalFormat df = new DecimalFormat();
      df.setMinimumFractionDigits(0);
      df.setMaximumFractionDigits(2);
      result = result + ChatColor.WHITE + "X: " + ChatColor.AQUA + df.format(l.getX()) + " ";
      result = result + ChatColor.WHITE + "Y: " + ChatColor.AQUA + df.format(l.getY()) + " ";
      result = result + ChatColor.WHITE + "Z: " + ChatColor.AQUA + df.format(l.getZ()) + " ";
      result = result + ChatColor.WHITE + "P: " + ChatColor.GOLD + df.format((double)l.getPitch()) + " ";
      result = result + ChatColor.WHITE + "Y: " + ChatColor.GOLD + df.format((double)l.getYaw()) + " ";
      return result;
   }

   public static String strCoordsRaw(Location l) {
      if (l == null) {
         return "null";
      } else {
         String result = "";
         DecimalFormat df = new DecimalFormat();
         df.setMinimumFractionDigits(0);
         df.setMaximumFractionDigits(2);
         result = result + "X: " + df.format(l.getX()) + " ";
         result = result + "Y: " + df.format(l.getY()) + " ";
         result = result + "Z: " + df.format(l.getZ()) + " ";
         result = result + "P: " + df.format((double)l.getPitch()) + " ";
         result = result + "Y: " + df.format((double)l.getYaw()) + " ";
         return result;
      }
   }

   public static String getDirection(Location location) {
      double r = (double)(location.getYaw() % 360.0F + 180.0F);
      String dir;
      if (r < (double)22.5F) {
         dir = "n";
      } else if (r < (double)67.5F) {
         dir = "ne";
      } else if (r < (double)112.5F) {
         dir = "e";
      } else if (r < (double)157.5F) {
         dir = "se";
      } else if (r < (double)202.5F) {
         dir = "s";
      } else if (r < (double)247.5F) {
         dir = "sw";
      } else if (r < (double)292.5F) {
         dir = "w";
      } else if (r < (double)337.5F) {
         dir = "nw";
      } else {
         dir = "n";
      }

      return dir;
   }

   public static float getYaw(String orientation) {
      if (orientation == null) {
         return 0.0F;
      } else {
         return orientationInts.containsKey(orientation.toLowerCase()) ? (float)(Integer)orientationInts.get(orientation.toLowerCase()) : 0.0F;
      }
   }

   public static float getSpeed(Vector v) {
      return (float)Math.sqrt(v.getX() * v.getX() + v.getZ() * v.getZ());
   }

   public static Vector getTranslatedVector(Vector v, String direction) {
      if (direction == null) {
         return v;
      } else {
         float speed = getSpeed(v);
         float halfSpeed = (float)((double)speed / (double)2.0F);
         if (direction.equalsIgnoreCase("n")) {
            return new Vector(0.0F, 0.0F, -1.0F * speed);
         } else if (direction.equalsIgnoreCase("ne")) {
            return new Vector(halfSpeed, 0.0F, -1.0F * halfSpeed);
         } else if (direction.equalsIgnoreCase("e")) {
            return new Vector(speed, 0.0F, 0.0F);
         } else if (direction.equalsIgnoreCase("se")) {
            return new Vector(halfSpeed, 0.0F, halfSpeed);
         } else if (direction.equalsIgnoreCase("s")) {
            return new Vector(0.0F, 0.0F, speed);
         } else if (direction.equalsIgnoreCase("sw")) {
            return new Vector(-1.0F * halfSpeed, 0.0F, halfSpeed);
         } else if (direction.equalsIgnoreCase("w")) {
            return new Vector(-1.0F * speed, 0.0F, 0.0F);
         } else {
            return direction.equalsIgnoreCase("nw") ? new Vector(-1.0F * halfSpeed, 0.0F, -1.0F * halfSpeed) : v;
         }
      }
   }

   public static Location getNextBlock(Vehicle v) {
      Vector vector = v.getVelocity();
      Location location = v.getLocation();
      int x = vector.getX() < (double)0.0F ? (vector.getX() == (double)0.0F ? 0 : -1) : 1;
      int z = vector.getZ() < (double)0.0F ? (vector.getZ() == (double)0.0F ? 0 : -1) : 1;
      return location.add((double)x, (double)0.0F, (double)z);
   }

   static {
      orientationInts.put("n", 180);
      orientationInts.put("ne", 225);
      orientationInts.put("e", 270);
      orientationInts.put("se", 315);
      orientationInts.put("s", 0);
      orientationInts.put("sw", 45);
      orientationInts.put("w", 90);
      orientationInts.put("nw", 135);
      orientationInts = Collections.unmodifiableMap(orientationInts);
   }
}
