package net.citizensnpcs.util;

import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.bukkit.Location;

public class Anchor {
   private final Location location;
   private final String name;

   public Anchor(String name, Location location) {
      super();
      this.location = location;
      this.name = name;
   }

   public boolean equals(Object object) {
      if (object == null) {
         return false;
      } else if (object == this) {
         return true;
      } else if (object.getClass() != this.getClass()) {
         return false;
      } else {
         Anchor op = (Anchor)object;
         return (new EqualsBuilder()).append(this.name, op.getName()).isEquals();
      }
   }

   public Location getLocation() {
      return this.location;
   }

   public String getName() {
      return this.name;
   }

   public int hashCode() {
      return (new HashCodeBuilder(13, 21)).append(this.name).toHashCode();
   }

   public String stringValue() {
      return this.name + ";" + this.location.getWorld().getName() + ";" + this.location.getX() + ";" + this.location.getY() + ";" + this.location.getZ();
   }

   public String toString() {
      return "Name: " + this.name + " World: " + this.location.getWorld().getName() + " Location: " + this.location.getBlockX() + "," + this.location.getBlockY() + "," + this.location.getBlockZ();
   }
}
