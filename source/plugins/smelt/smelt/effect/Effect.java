package smelt.effect;

import lib.realDamage.RealDamageEvent;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

public interface Effect {
   int getId();

   void onAttack(RealDamageEvent var1, Player var2, int var3);

   void onBow(RealDamageEvent var1, Player var2, int var3);

   void onAttacked(RealDamageEvent var1, Player var2, int var3, ItemStack var4);

   ItemStack setEffectData(ItemStack var1, int var2);
}
