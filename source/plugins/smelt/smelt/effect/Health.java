package smelt.effect;

import java.util.UUID;
import lib.nbt.Attributes;
import lib.nbt.Attributes.Attribute;
import lib.nbt.Attributes.AttributeType;
import lib.nbt.Attributes.Operation;
import lib.realDamage.RealDamageEvent;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

public class Health implements Effect {
   private static final UUID HEALTH_UID = UUID.fromString("1a6f9d4f-1397-4ed0-af02-f47703ec58aa");
   private static final int ID = 30;

   public Health() {
      super();
   }

   public int getId() {
      return 30;
   }

   public ItemStack setEffectData(ItemStack is, int data) {
      Attributes at = new Attributes(is);
      Attributes.Attribute a = Attribute.newBuilder().name("none").type(AttributeType.GENERIC_MAX_HEALTH).amount((double)data).operation(Operation.ADD_NUMBER).uuid(HEALTH_UID).build();
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
