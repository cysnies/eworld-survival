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

public class Weakness implements Effect {
   private static final int ID = 8;
   private Random r = new Random();
   private String pn;

   public Weakness(Main main) {
      super();
      this.pn = main.getPn();
   }

   public int getId() {
      return 8;
   }

   public ItemStack setEffectData(ItemStack is, int data) {
      return is;
   }

   public void onAttack(RealDamageEvent e, Player p, int data) {
      this.checkWeakness(e, p, data);
   }

   public void onBow(RealDamageEvent e, Player p, int data) {
      this.checkWeakness(e, p, data);
   }

   public void onAttacked(RealDamageEvent e, Player p, int data, ItemStack is) {
   }

   private void checkWeakness(RealDamageEvent e, Player p, int data) {
      if (this.r.nextInt(10000) < 1000) {
         LivingEntity le = e.getVictim();
         PotionEffect effect = new PotionEffect(PotionEffectType.WEAKNESS, data * 20, 1, false);
         le.addPotionEffect(effect, true);
         p.sendMessage(this.get(265));

         try {
            if (le instanceof Player) {
               ((Player)le).sendMessage(this.get(270));
            }
         } catch (Exception var7) {
         }
      }

   }

   private String get(int id) {
      return UtilFormat.format(this.pn, id);
   }
}
