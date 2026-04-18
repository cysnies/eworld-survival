package smelt.effect;

import java.util.HashMap;
import java.util.UUID;
import lib.nbt.Attributes;
import lib.nbt.Attributes.Attribute;
import lib.nbt.Attributes.AttributeType;
import lib.nbt.Attributes.Operation;
import lib.realDamage.RealDamageEvent;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

public class Attack implements Effect {
   private static final UUID ATTACK_UID = UUID.fromString("1a6f9d4f-1397-4ed0-af02-f47703ec58ae");
   private static final int ID = 1;
   private static final HashMap HURT = new HashMap();

   static {
      HURT.put(267, 6);
      HURT.put(283, 4);
      HURT.put(276, 7);
   }

   public Attack() {
      super();
   }

   public int getId() {
      return 1;
   }

   public ItemStack setEffectData(ItemStack is, int data) {
      if (is.getTypeId() == 261) {
         return is;
      } else {
         Attributes at = new Attributes(is);
         int add = 0;
         if (HURT.containsKey(is.getTypeId())) {
            add = (Integer)HURT.get(is.getTypeId());
         }

         Attributes.Attribute a = Attribute.newBuilder().name("none").type(AttributeType.GENERIC_ATTACK_DAMAGE).amount((double)(data + add)).operation(Operation.ADD_NUMBER).uuid(ATTACK_UID).build();
         at.remove(a);
         at.add(a);
         return at.getStack();
      }
   }

   public void onAttack(RealDamageEvent e, Player p, int data) {
   }

   public void onBow(RealDamageEvent e, Player p, int data) {
      e.setDamage(e.getDamage() + (double)data);
   }

   public void onAttacked(RealDamageEvent e, Player p, int data, ItemStack is) {
   }
}
