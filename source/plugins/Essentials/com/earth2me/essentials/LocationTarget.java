package com.earth2me.essentials;

import org.bukkit.Location;

public class LocationTarget implements ITarget {
   private final Location location;

   LocationTarget(Location location) {
      super();
      this.location = location;
   }

   public Location getLocation() {
      return this.location;
   }
}
