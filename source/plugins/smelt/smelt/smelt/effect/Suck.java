package smelt.effect;

import java.util.Random;
import lib.realDamage.RealDamageEvent;
import lib.util.UtilFormat;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import smelt.Main;

public class Suck implements Effect {
   private static final int ID = 9;
   private Random r = new Random();
   private String pn;

   public Suck(Main main) {
      super();
      this.pn = main.getPn();
   }

   public int getId() {
      return 9;
   }

   public ItemStack setEffectData(ItemStack is, int data) {
      return is;
   }

   public void onAttack(RealDamageEvent e, Player p, int data) {
      this.checkSuck(e, p, data);
   }

   public void onBow(RealDamageEvent e, Player p, int data) {
      this.checkSuck(e, p, data);
   }

   public void onAttacked(RealDamageEvent e, Player p, int data, ItemStack is) {
   }

   private void checkSuck(RealDamageEvent e, Player p, int data) {
      if (this.r.nextInt(100) < data) {
         p.setHealth(Math.min(p.getMaxHealth(), p.getHealth() + e.getDamage()));
         p.sendMessage(this.get(200));
      }

   }

   private String get(int id) {
      return UtilFormat.format(this.pn, id);
   }
}
