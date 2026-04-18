package net.citizensnpcs.trait;

import net.citizensnpcs.api.persistence.Persist;
import net.citizensnpcs.api.trait.Trait;
import org.bukkit.Location;
import org.bukkit.World;

public class CurrentLocation extends Trait {
   @Persist(
      value = "",
      required = true
   )
   private Location location = new Location((World)null, (double)0.0F, (double)0.0F, (double)0.0F);

   public CurrentLocation() {
      super("location");
   }

   public Location getLocation() {
      return this.location.getWorld() == null ? null : this.location;
   }

   public void run() {
      if (this.npc.isSpawned()) {
         this.location = this.npc.getBukkitEntity().getLocation(this.location);
      }
   }

   public void setLocation(Location loc) {
      this.location = loc.clone();
   }

   public String toString() {
      return "CurrentLocation{" + this.location + "}";
   }
}
