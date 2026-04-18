package net.citizensnpcs.trait;

import net.citizensnpcs.api.CitizensAPI;
import net.citizensnpcs.api.persistence.Persist;
import net.citizensnpcs.api.trait.Trait;
import org.bukkit.entity.Pig;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.PlayerInteractEntityEvent;

public class Saddle extends Trait implements Toggleable {
   private boolean pig;
   @Persist("")
   private boolean saddle;

   public Saddle() {
      super("saddle");
   }

   @EventHandler
   public void onPlayerInteractEntity(PlayerInteractEntityEvent event) {
      if (this.pig && this.npc.equals(CitizensAPI.getNPCRegistry().getNPC(event.getRightClicked()))) {
         event.setCancelled(true);
      }

   }

   public void onSpawn() {
      if (this.npc.getBukkitEntity() instanceof Pig) {
         ((Pig)this.npc.getBukkitEntity()).setSaddle(this.saddle);
         this.pig = true;
      } else {
         this.pig = false;
      }

   }

   public boolean toggle() {
      this.saddle = !this.saddle;
      if (this.pig) {
         ((Pig)this.npc.getBukkitEntity()).setSaddle(this.saddle);
      }

      return this.saddle;
   }

   public String toString() {
      return "Saddle{" + this.saddle + "}";
   }
}
