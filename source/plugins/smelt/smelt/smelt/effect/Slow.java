package smelt.effect;

import java.util.Random;
import lib.realDamage.RealDamageEvent;
import lib.util.UtilFormat;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import smelt.Main;

public class Slow implements Effect {
   private static final int ID = 7;
   private Random r = new Random();
   private String pn;

   public Slow(Main main) {
      super();
      this.pn = main.getPn();
   }

   public int getId() {
      return 7;
   }

   public ItemStack setEffectData(ItemStack is, int data) {
      return is;
   }

   public void onAttack(RealDamageEvent e, Player p, int data) {
      this.checkBlindness(e, p, data);
   }

   public void onBow(RealDamageEvent e, Player p, int data) {
      this.checkBlindness(e, p, data);
   }

   public void onAttacked(RealDamageEvent e, Player p, int data, ItemStack is) {
   }

   private void checkBlindness(RealDamageEvent e, Player p, int data) {
      if (this.r.nextInt(10000) < 1000) {
         LivingEntity le = e.getVictim();
         PotionEffect effect = new PotionEffect(PotionEffectType.SLOW, data * 20, 2, false);
         le.addPotionEffect(effect, true);
         p.sendMessage(this.get(255));

         try {
            if (le instanceof Player) {
               ((Player)le).sendMessage(this.get(260));
            }
         } catch (Exception var7) {
         }
      }

   }

   private String get(int id) {
      return UtilFormat.format(this.pn, id);
   }
}
