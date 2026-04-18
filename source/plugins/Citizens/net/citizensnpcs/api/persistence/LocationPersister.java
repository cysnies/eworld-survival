package net.citizensnpcs.api.persistence;

import net.citizensnpcs.api.util.DataKey;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;

public class LocationPersister implements Persister {
   public LocationPersister() {
      super();
   }

   public Location create(DataKey root) {
      if (!root.keyExists("world")) {
         return null;
      } else {
         World world = Bukkit.getWorld(root.getString("world"));
         double x = root.getDouble("x");
         double y = root.getDouble("y");
         double z = root.getDouble("z");
         float yaw = (float)root.getDouble("yaw");
         float pitch = (float)root.getDouble("pitch");
         return (Location)(world == null ? new LazilyLoadedLocation(root.getString("world"), x, y, z, yaw, pitch) : new Location(world, x, y, z, yaw, pitch));
      }
   }

   public void save(Location location, DataKey root) {
      if (location.getWorld() != null) {
         root.setString("world", location.getWorld().getName());
      }

      root.setDouble("x", location.getX());
      root.setDouble("y", location.getY());
      root.setDouble("z", location.getZ());
      root.setDouble("yaw", (double)location.getYaw());
      root.setDouble("pitch", (double)location.getPitch());
   }

   public static class LazilyLoadedLocation extends Location {
      private final String worldName;

      public LazilyLoadedLocation(String world, double x, double y, double z, float yaw, float pitch) {
         super((World)null, x, y, z, yaw, pitch);
         this.worldName = world;
      }

      public World getWorld() {
         if (super.getWorld() == null) {
            super.setWorld(Bukkit.getWorld(this.worldName));
         }

         return super.getWorld();
      }
   }
}
