package net.citizensnpcs.trait;

import net.citizensnpcs.api.persistence.Persist;
import net.citizensnpcs.api.trait.Trait;
import net.citizensnpcs.api.util.Messaging;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Ageable;

public class Age extends Trait implements Toggleable {
   @Persist
   private int age = 0;
   private Ageable ageable;
   @Persist
   private boolean locked = true;

   public Age() {
      super("age");
   }

   public void describe(CommandSender sender) {
      Messaging.sendTr(sender, "citizens.traits.age-description", this.npc.getName(), this.age, this.locked);
   }

   private boolean isAgeable() {
      return this.ageable != null;
   }

   public void onSpawn() {
      if (this.npc.getBukkitEntity() instanceof Ageable) {
         Ageable entity = (Ageable)this.npc.getBukkitEntity();
         entity.setAge(this.age);
         entity.setAgeLock(this.locked);
         this.ageable = entity;
      } else {
         this.ageable = null;
      }

   }

   public void run() {
      if (!this.locked && this.isAgeable()) {
         this.age = this.ageable.getAge();
      }

   }

   public void setAge(int age) {
      this.age = age;
      if (this.isAgeable()) {
         this.ageable.setAge(age);
      }

   }

   public boolean toggle() {
      this.locked = !this.locked;
      if (this.isAgeable()) {
         this.ageable.setAgeLock(this.locked);
      }

      return this.locked;
   }

   public String toString() {
      return "Age{age=" + this.age + ",locked=" + this.locked + "}";
   }
}
