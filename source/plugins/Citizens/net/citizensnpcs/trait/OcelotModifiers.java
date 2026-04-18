package net.citizensnpcs.trait;

import net.citizensnpcs.api.persistence.Persist;
import net.citizensnpcs.api.trait.Trait;
import org.bukkit.entity.Ocelot;
import org.bukkit.entity.Ocelot.Type;

public class OcelotModifiers extends Trait {
   @Persist("sitting")
   private boolean sitting;
   @Persist("type")
   private Ocelot.Type type;

   public OcelotModifiers() {
      super("ocelotmodifiers");
      this.type = Type.WILD_OCELOT;
   }

   public void onSpawn() {
      this.updateModifiers();
   }

   public void setSitting(boolean sit) {
      this.sitting = sit;
      this.updateModifiers();
   }

   public void setType(Ocelot.Type type) {
      this.type = type;
      this.updateModifiers();
   }

   private void updateModifiers() {
      if (this.npc.getBukkitEntity() instanceof Ocelot) {
         Ocelot ocelot = (Ocelot)this.npc.getBukkitEntity();
         ocelot.setCatType(this.type);
         ocelot.setSitting(this.sitting);
      }

   }
}
