package net.citizensnpcs.trait;

import net.citizensnpcs.api.persistence.Persist;
import net.citizensnpcs.api.trait.Trait;
import org.bukkit.DyeColor;
import org.bukkit.entity.Wolf;

public class WolfModifiers extends Trait {
   @Persist("angry")
   private boolean angry;
   @Persist("collarColor")
   private DyeColor collarColor;
   @Persist("sitting")
   private boolean sitting;
   @Persist("tamed")
   private boolean tamed;

   public WolfModifiers() {
      super("wolfmodifiers");
      this.collarColor = DyeColor.RED;
   }

   public void onSpawn() {
      this.updateModifiers();
   }

   public void setAngry(boolean angry) {
      this.angry = angry;
      this.updateModifiers();
   }

   public void setCollarColor(DyeColor color) {
      this.collarColor = color;
      this.updateModifiers();
   }

   public void setSitting(boolean sitting) {
      this.sitting = sitting;
      this.updateModifiers();
   }

   public void setTamed(boolean tamed) {
      this.tamed = tamed;
      this.updateModifiers();
   }

   private void updateModifiers() {
      if (this.npc.getBukkitEntity() instanceof Wolf) {
         Wolf wolf = (Wolf)this.npc.getBukkitEntity();
         wolf.setCollarColor(this.collarColor);
         wolf.setSitting(this.sitting);
         wolf.setAngry(this.angry);
         wolf.setTamed(this.tamed);
      }

   }
}
