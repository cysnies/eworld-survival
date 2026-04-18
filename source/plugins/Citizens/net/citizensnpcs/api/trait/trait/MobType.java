package net.citizensnpcs.api.trait.trait;

import net.citizensnpcs.api.trait.Trait;
import net.citizensnpcs.api.util.DataKey;
import org.bukkit.entity.EntityType;

public class MobType extends Trait {
   private EntityType type;

   public MobType() {
      super("type");
      this.type = EntityType.PLAYER;
   }

   public EntityType getType() {
      return this.type;
   }

   public void load(DataKey key) {
      this.type = EntityType.fromName(key.getString(""));
      if (this.type == null) {
         this.type = EntityType.PLAYER;
      }

   }

   public void onSpawn() {
      this.type = this.npc.getBukkitEntity().getType();
   }

   public void save(DataKey key) {
      key.setString("", this.type.getName());
   }

   public void setType(EntityType type) {
      this.type = type;
   }

   public String toString() {
      return "MobType{" + this.type + "}";
   }
}
