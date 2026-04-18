package smelt.effect;

import java.util.Random;
import lib.realDamage.RealDamageEvent;
import lib.util.UtilFormat;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import smelt.Main;

public class RecoverAll implements Effect {
   private static final int ID = 90;
   private Random r = new Random();
   private String pn;

   public RecoverAll(Main main) {
      super();
      this.pn = main.getPn();
   }

   public int getId() {
      return 90;
   }

   public ItemStack setEffectData(ItemStack is, int data) {
      return is;
   }

   public void onAttack(RealDamageEvent e, Player p, int data) {
      this.checkRecover(p, data, p.getItemInHand());
   }

   public void onBow(RealDamageEvent e, Player p, int data) {
      this.checkRecover(p, data, p.getItemInHand());
   }

   public void onAttacked(RealDamageEvent e, Player p, int data, ItemStack is) {
      this.checkRecover(p, data, is);
   }

   private void checkRecover(Player p, int data, ItemStack is) {
      if (this.r.nextInt(10000) < 210) {
         is.setDurability((short)Math.max(0, is.getDurability() - data));
         p.sendMessage(this.get(205));
      }

   }

   private String get(int id) {
      return UtilFormat.format(this.pn, id);
   }
}
