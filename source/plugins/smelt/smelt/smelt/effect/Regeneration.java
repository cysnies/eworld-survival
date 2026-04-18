package smelt.effect;

import java.util.Random;
import lib.realDamage.RealDamageEvent;
import lib.util.UtilFormat;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import smelt.Main;

public class Regeneration implements Effect {
   private static final int ID = 34;
   private Random r = new Random();
   private String pn;

   public Regeneration(Main main) {
      super();
      this.pn = main.getPn();
   }

   public int getId() {
      return 34;
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
         PotionEffect effect = new PotionEffect(PotionEffectType.REGENERATION, data * 20, 2, false);
         p.addPotionEffect(effect, true);
         p.sendMessage(this.get(280));
      }

   }

   private String get(int id) {
      return UtilFormat.format(this.pn, id);
   }
}
