package net.citizensnpcs.trait;

import net.citizensnpcs.api.persistence.Persist;
import net.citizensnpcs.api.trait.Trait;
import org.bukkit.util.Vector;

public class Gravity extends Trait implements Toggleable {
   @Persist
   private boolean enabled;

   public Gravity() {
      super("gravity");
   }

   public void gravitate(boolean gravitate) {
      this.enabled = gravitate;
   }

   public void run() {
      if (this.npc.isSpawned() && this.enabled) {
         Vector vector = this.npc.getBukkitEntity().getVelocity();
         vector.setY(Math.max((double)0.0F, vector.getY()));
         this.npc.getBukkitEntity().setVelocity(vector);
      }
   }

   public boolean toggle() {
      return this.enabled = !this.enabled;
   }
}
