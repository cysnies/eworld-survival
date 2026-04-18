package net.citizensnpcs.trait;

import net.citizensnpcs.api.CitizensAPI;
import net.citizensnpcs.api.persistence.Persist;
import net.citizensnpcs.api.trait.Trait;
import org.bukkit.entity.Sheep;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.PlayerShearEntityEvent;

public class Sheared extends Trait implements Toggleable {
   @Persist("")
   private boolean sheared;

   public Sheared() {
      super("sheared");
   }

   @EventHandler
   public void onPlayerShearEntityEvent(PlayerShearEntityEvent event) {
      if (this.npc.equals(CitizensAPI.getNPCRegistry().getNPC(event.getEntity()))) {
         event.setCancelled(true);
      }

   }

   public void onSpawn() {
      if (this.npc.getBukkitEntity() instanceof Sheep) {
         ((Sheep)this.npc.getBukkitEntity()).setSheared(this.sheared);
      }

   }

   public boolean toggle() {
      this.sheared = !this.sheared;
      if (this.npc.getBukkitEntity() instanceof Sheep) {
         ((Sheep)this.npc.getBukkitEntity()).setSheared(this.sheared);
      }

      return this.sheared;
   }

   public String toString() {
      return "Sheared{" + this.sheared + "}";
   }
}
