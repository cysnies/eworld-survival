package net.citizensnpcs.trait;

import net.citizensnpcs.api.persistence.Persist;
import net.citizensnpcs.api.trait.Trait;
import org.bukkit.entity.Creeper;

public class Powered extends Trait implements Toggleable {
   @Persist("")
   private boolean powered;

   public Powered() {
      super("powered");
   }

   public void onSpawn() {
      if (this.npc.getBukkitEntity() instanceof Creeper) {
         ((Creeper)this.npc.getBukkitEntity()).setPowered(this.powered);
      }

   }

   public boolean toggle() {
      this.powered = !this.powered;
      if (this.npc.getBukkitEntity() instanceof Creeper) {
         ((Creeper)this.npc.getBukkitEntity()).setPowered(this.powered);
      }

      return this.powered;
   }

   public String toString() {
      return "Powered{" + this.powered + "}";
   }
}
