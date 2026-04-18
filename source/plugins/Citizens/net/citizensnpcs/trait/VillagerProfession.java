package net.citizensnpcs.trait;

import net.citizensnpcs.api.exception.NPCLoadException;
import net.citizensnpcs.api.trait.Trait;
import net.citizensnpcs.api.util.DataKey;
import org.bukkit.entity.Villager;
import org.bukkit.entity.Villager.Profession;

public class VillagerProfession extends Trait {
   private Villager.Profession profession;

   public VillagerProfession() {
      super("profession");
      this.profession = Profession.FARMER;
   }

   public void load(DataKey key) throws NPCLoadException {
      try {
         this.profession = Profession.valueOf(key.getString(""));
      } catch (IllegalArgumentException var3) {
         throw new NPCLoadException("Invalid profession.");
      }
   }

   public void onSpawn() {
      if (this.npc.getBukkitEntity() instanceof Villager) {
         ((Villager)this.npc.getBukkitEntity()).setProfession(this.profession);
      }

   }

   public void save(DataKey key) {
      key.setString("", this.profession.name());
   }

   public void setProfession(Villager.Profession profession) {
      this.profession = profession;
      if (this.npc.getBukkitEntity() instanceof Villager) {
         ((Villager)this.npc.getBukkitEntity()).setProfession(profession);
      }

   }

   public String toString() {
      return "Profession{" + this.profession + "}";
   }
}
