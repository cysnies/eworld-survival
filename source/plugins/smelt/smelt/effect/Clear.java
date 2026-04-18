package smelt.effect;

import java.util.Random;
import lib.realDamage.RealDamageEvent;
import lib.util.UtilFormat;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffectType;
import smelt.Main;

public class Clear implements Effect {
   private static final int ID = 32;
   private Random r = new Random();
   private String pn;

   public Clear(Main main) {
      super();
      this.pn = main.getPn();
   }

   public int getId() {
      return 32;
   }

   public ItemStack setEffectData(ItemStack is, int data) {
      return is;
   }

   public void onAttack(RealDamageEvent e, Player p, int data) {
   }

   public void onBow(RealDamageEvent e, Player p, int data) {
   }

   public void onAttacked(RealDamageEvent e, Player p, int data, ItemStack is) {
      if (this.r.nextInt(100) < data) {
         p.removePotionEffect(PotionEffectType.BLINDNESS);
         p.removePotionEffect(PotionEffectType.CONFUSION);
         p.removePotionEffect(PotionEffectType.HARM);
         p.removePotionEffect(PotionEffectType.HUNGER);
         p.removePotionEffect(PotionEffectType.POISON);
         p.removePotionEffect(PotionEffectType.SLOW);
         p.removePotionEffect(PotionEffectType.SLOW_DIGGING);
         p.removePotionEffect(PotionEffectType.WEAKNESS);
         p.removePotionEffect(PotionEffectType.WITHER);
         p.sendMessage(this.get(230));
      }

   }

   private String get(int id) {
      return UtilFormat.format(this.pn, id);
   }
}
