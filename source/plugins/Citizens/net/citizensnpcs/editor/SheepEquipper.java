package net.citizensnpcs.editor;

import net.citizensnpcs.api.npc.NPC;
import net.citizensnpcs.api.util.Messaging;
import net.citizensnpcs.trait.Sheared;
import net.citizensnpcs.trait.WoolColor;
import org.bukkit.DyeColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.entity.Sheep;
import org.bukkit.inventory.ItemStack;

public class SheepEquipper implements Equipper {
   public SheepEquipper() {
      super();
   }

   public void equip(Player equipper, NPC toEquip) {
      ItemStack hand = equipper.getItemInHand();
      Sheep sheep = (Sheep)toEquip.getBukkitEntity();
      if (hand.getType() == Material.SHEARS) {
         Messaging.sendTr(equipper, ((Sheared)toEquip.getTrait(Sheared.class)).toggle() ? "citizens.editors.equipment.sheared-set" : "citizens.editors.equipment.sheared-stopped", toEquip.getName());
      } else if (hand.getType() == Material.INK_SACK) {
         if (sheep.getColor() == DyeColor.getByWoolData((byte)(15 - hand.getData().getData()))) {
            return;
         }

         DyeColor color = DyeColor.getByWoolData((byte)(15 - hand.getData().getData()));
         ((WoolColor)toEquip.getTrait(WoolColor.class)).setColor(color);
         Messaging.sendTr(equipper, "citizens.editors.equipment.sheep-coloured", toEquip.getName(), color.name().toLowerCase().replace("_", " "));
         hand.setAmount(hand.getAmount() - 1);
      } else {
         ((WoolColor)toEquip.getTrait(WoolColor.class)).setColor(DyeColor.WHITE);
         Messaging.sendTr(equipper, "citizens.editors.equipment.sheep-coloured", toEquip.getName(), "white");
      }

      equipper.setItemInHand(hand);
   }
}
