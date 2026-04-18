package net.citizensnpcs.trait;

import net.citizensnpcs.api.persistence.Persist;
import net.citizensnpcs.api.trait.Trait;
import org.bukkit.entity.Zombie;

public class ZombieModifier extends Trait {
   @Persist
   private boolean baby;
   @Persist
   private boolean villager;
   private boolean zombie;

   public ZombieModifier() {
      super("zombiemodifier");
   }

   public void onSpawn() {
      if (this.npc.getBukkitEntity() instanceof Zombie) {
         ((Zombie)this.npc.getBukkitEntity()).setVillager(this.villager);
         ((Zombie)this.npc.getBukkitEntity()).setBaby(this.baby);
         this.zombie = true;
      } else {
         this.zombie = false;
      }

   }

   public boolean toggleBaby() {
      this.baby = !this.baby;
      if (this.zombie) {
         ((Zombie)this.npc.getBukkitEntity()).setBaby(this.baby);
      }

      return this.baby;
   }

   public boolean toggleVillager() {
      this.villager = !this.villager;
      if (this.zombie) {
         ((Zombie)this.npc.getBukkitEntity()).setVillager(this.villager);
      }

      return this.villager;
   }
}
