package smelt.effect;

import java.util.Random;
import lib.realDamage.RealDamageEvent;
import lib.util.UtilFormat;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import smelt.Main;

public class Crit implements Effect {
   private static final int ID = 2;
   private Random r = new Random();
   private String pn;

   public Crit(Main main) {
      super();
      this.pn = main.getPn();
   }

   public int getId() {
      return 2;
   }

   public ItemStack setEffectData(ItemStack is, int data) {
      return is;
   }

   public void onAttack(RealDamageEvent e, Player p, int data) {
      this.checkCrit(e, p, data);
   }

   public void onBow(RealDamageEvent e, Player p, int data) {
      this.checkCrit(e, p, data);
   }

   public void onAttacked(RealDamageEvent e, Player p, int data, ItemStack is) {
   }

   private void checkCrit(RealDamageEvent e, Player p, int data) {
      if (this.r.nextInt(100) < data) {
         e.setDamage(e.getDamage() * (double)2.0F);
         p.sendMessage(this.get(185));

         try {
            LivingEntity le = e.getVictim();
            if (le instanceof Player) {
               ((Player)le).sendMessage(this.get(190));
            }
         } catch (Exception var5) {
         }
      }

   }

   private String get(int id) {
      return UtilFormat.format(this.pn, id);
   }
}
