package net.citizensnpcs.editor;

import net.citizensnpcs.api.npc.NPC;
import net.citizensnpcs.api.trait.trait.Equipment;
import net.citizensnpcs.api.util.Messaging;
import org.bukkit.Material;
import org.bukkit.entity.Enderman;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.material.MaterialData;

public class EndermanEquipper implements Equipper {
   public EndermanEquipper() {
      super();
   }

   public void equip(Player equipper, NPC npc) {
      ItemStack hand = equipper.getItemInHand();
      if (!hand.getType().isBlock()) {
         Messaging.sendErrorTr(equipper, "citizens.editors.equipment.invalid-block");
      } else {
         MaterialData carried = ((Enderman)npc.getBukkitEntity()).getCarriedMaterial();
         if (carried.getItemType() == Material.AIR) {
            if (hand.getType() == Material.AIR) {
               Messaging.sendErrorTr(equipper, "citizens.editors.equipment.invalid-block");
               return;
            }
         } else {
            equipper.getWorld().dropItemNaturally(npc.getBukkitEntity().getLocation(), carried.toItemStack(1));
            ((Enderman)npc.getBukkitEntity()).setCarriedMaterial(hand.getData());
         }

         ItemStack set = hand.clone();
         if (set.getType() != Material.AIR) {
            set.setAmount(1);
            hand.setAmount(hand.getAmount() - 1);
            equipper.setItemInHand(hand);
         }

         ((Equipment)npc.getTrait(Equipment.class)).set(0, set);
      }
   }
}
