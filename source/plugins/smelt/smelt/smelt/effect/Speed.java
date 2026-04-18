package smelt.effect;

import java.util.UUID;
import lib.nbt.Attributes;
import lib.nbt.Attributes.Attribute;
import lib.nbt.Attributes.AttributeType;
import lib.nbt.Attributes.Operation;
import lib.realDamage.RealDamageEvent;
import lib.util.Util;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

public class Speed implements Effect {
   private static final UUID SPEED_UID = UUID.fromString("1a6f9d4f-1397-4ed0-af02-f47703ec58a8");
   private static final int ID = 50;

   public Speed() {
      super();
   }

   public int getId() {
      return 50;
   }

   public ItemStack setEffectData(ItemStack is, int data) {
      Attributes at = new Attributes(is);
      double add = Util.getDouble((double)data / (double)100.0F, 2);
      Attributes.Attribute a = Attribute.newBuilder().name("none").type(AttributeType.GENERIC_MOVEMENT_SPEED).amount(add).operation(Operation.ADD_PERCENTAGE).uuid(SPEED_UID).build();
      at.remove(a);
      at.add(a);
      return at.getStack();
   }

   public void onAttack(RealDamageEvent e, Player p, int data) {
   }

   public void onBow(RealDamageEvent e, Player p, int data) {
   }

   public void onAttacked(RealDamageEvent e, Player p, int data, ItemStack is) {
   }
}
