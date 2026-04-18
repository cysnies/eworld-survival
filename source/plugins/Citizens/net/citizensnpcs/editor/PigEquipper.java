package net.citizensnpcs.editor;

import net.citizensnpcs.api.npc.NPC;
import net.citizensnpcs.api.util.Messaging;
import net.citizensnpcs.trait.Saddle;
import org.bukkit.Material;
import org.bukkit.entity.Pig;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

public class PigEquipper implements Equipper {
   public PigEquipper() {
      super();
   }

   public void equip(Player equipper, NPC toEquip) {
      ItemStack hand = equipper.getItemInHand();
      Pig pig = (Pig)toEquip.getBukkitEntity();
      if (hand.getType() == Material.SADDLE) {
         if (!pig.hasSaddle()) {
            ((Saddle)toEquip.getTrait(Saddle.class)).toggle();
            hand.setAmount(0);
            Messaging.sendTr(equipper, "citizens.editors.equipment.saddled-set", toEquip.getName());
         }
      } else if (pig.hasSaddle()) {
         equipper.getWorld().dropItemNaturally(pig.getLocation(), new ItemStack(Material.SADDLE, 1));
         ((Saddle)toEquip.getTrait(Saddle.class)).toggle();
         Messaging.sendTr(equipper, "citizens.editors.equipment.saddled-stopped", toEquip.getName());
      }

      equipper.setItemInHand(hand);
   }
}
