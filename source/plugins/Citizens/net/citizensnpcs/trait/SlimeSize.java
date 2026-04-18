package net.citizensnpcs.trait;

import net.citizensnpcs.api.persistence.Persist;
import net.citizensnpcs.api.trait.Trait;
import net.citizensnpcs.api.util.Messaging;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Slime;

public class SlimeSize extends Trait {
   @Persist
   private int size = 3;
   private boolean slime;

   public SlimeSize() {
      super("slimesize");
   }

   public void describe(CommandSender sender) {
      Messaging.sendTr(sender, "citizens.commands.npc.size.description", this.npc.getName(), this.size);
   }

   public void onSpawn() {
      if (!(this.npc.getBukkitEntity() instanceof Slime)) {
         this.slime = false;
      } else {
         ((Slime)this.npc.getBukkitEntity()).setSize(this.size);
         this.slime = true;
      }
   }

   public void setSize(int size) {
      this.size = size;
      if (this.slime) {
         ((Slime)this.npc.getBukkitEntity()).setSize(size);
      }

   }
}
