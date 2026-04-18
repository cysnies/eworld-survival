package net.citizensnpcs.util;

import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;

public class Pose {
   private final String name;
   private final float pitch;
   private final float yaw;

   public Pose(String name, float pitch, float yaw) {
      super();
      this.yaw = yaw;
      this.pitch = pitch;
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
         Pose op = (Pose)object;
         return (new EqualsBuilder()).append(this.name, op.getName()).isEquals();
      }
   }

   public String getName() {
      return this.name;
   }

   public float getPitch() {
      return this.pitch;
   }

   public float getYaw() {
      return this.yaw;
   }

   public int hashCode() {
      return (new HashCodeBuilder(13, 21)).append(this.name).toHashCode();
   }

   public String stringValue() {
      return this.name + ";" + this.pitch + ";" + this.yaw;
   }

   public String toString() {
      return "Name: " + this.name + " Pitch: " + this.pitch + " Yaw: " + this.yaw;
   }
}
