package smelt.effect;

import java.util.Random;
import lib.realDamage.RealDamageEvent;
import lib.util.UtilFormat;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import smelt.Main;

public class Hide implements Effect {
   private static final int ID = 36;
   private Random r = new Random();
   private String pn;

   public Hide(Main main) {
      super();
      this.pn = main.getPn();
   }

   public int getId() {
      return 36;
   }

   public ItemStack setEffectData(ItemStack is, int data) {
      return is;
   }

   public void onAttack(RealDamageEvent e, Player p, int data) {
   }

   public void onBow(RealDamageEvent e, Player p, int data) {
   }

   public void onAttacked(RealDamageEvent e, Player p, int data, ItemStack is) {
      if (this.r.nextInt(10000) < 500) {
         PotionEffect effect = new PotionEffect(PotionEffectType.INVISIBILITY, data * 20, 0, false);
         p.addPotionEffect(effect, true);
         p.sendMessage(this.get(290));
      }

   }

   private String get(int id) {
      return UtilFormat.format(this.pn, id);
   }
}
