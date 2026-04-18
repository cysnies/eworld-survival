package smelt.effect;

import java.util.Random;
import lib.realDamage.RealDamageEvent;
import lib.util.UtilFormat;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import smelt.Main;

public class InstantRecover implements Effect {
   private static final int ID = 20;
   private Random r = new Random();
   private String pn;

   public InstantRecover(Main main) {
      super();
      this.pn = main.getPn();
   }

   public int getId() {
      return 20;
   }

   public ItemStack setEffectData(ItemStack is, int data) {
      return is;
   }

   public void onAttack(RealDamageEvent e, Player p, int data) {
   }

   public void onBow(RealDamageEvent e, Player p, int data) {
   }

   public void onAttacked(RealDamageEvent e, Player p, int data, ItemStack is) {
      if (this.r.nextInt(10000) < 250) {
         p.setHealth(Math.min(p.getMaxHealth(), p.getHealth() + (double)data));
         p.sendMessage(this.get(195));
      }

   }

   private String get(int id) {
      return UtilFormat.format(this.pn, id);
   }
}
