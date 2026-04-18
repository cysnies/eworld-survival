package fr.neatmonster.nocheatplus.checks.moving;

import fr.neatmonster.nocheatplus.utilities.PlayerLocation;
import org.bukkit.Location;

public class LocUtil {
   public LocUtil() {
      super();
   }

   public static final Location clone(Location loc) {
      return new Location(loc.getWorld(), loc.getX(), loc.getY(), loc.getZ(), loc.getYaw(), loc.getPitch());
   }

   public static final Location clone(Location loc, float yaw, float pitch) {
      return new Location(loc.getWorld(), loc.getX(), loc.getY(), loc.getZ(), yaw, pitch);
   }

   public static final Location clone(Location setBack, Location ref) {
      return setBack == null ? clone(ref) : clone(setBack, ref.getYaw(), ref.getPitch());
   }

   public static final Location clone(Location setBack, PlayerLocation ref) {
      return setBack == null ? ref.getLocation() : clone(setBack, ref.getYaw(), ref.getPitch());
   }

   public static final void set(Location setBack, Location loc) {
      setBack.setWorld(loc.getWorld());
      setBack.setX(loc.getX());
      setBack.setY(loc.getY());
      setBack.setZ(loc.getZ());
      setBack.setYaw(loc.getYaw());
      setBack.setPitch(loc.getPitch());
   }

   public static final void set(Location setBack, PlayerLocation loc) {
      setBack.setWorld(loc.getWorld());
      setBack.setX(loc.getX());
      setBack.setY(loc.getY());
      setBack.setZ(loc.getZ());
      setBack.setYaw(loc.getYaw());
      setBack.setPitch(loc.getPitch());
   }
}
