package com.fernferret.allpay.multiverse;

import com.iConomy.iConomy;
import org.bukkit.entity.Player;

public class iConomyBank5X extends GenericBank {
   public iConomyBank5X() {
      super();
   }

   public String getEconUsed() {
      return "iConomy 5";
   }

   protected boolean setMoneyBalance(Player player, double amount) {
      iConomy.getAccount(player.getName()).getHoldings().set(amount);
      return true;
   }

   protected boolean hasMoney(Player player, double money, String message) {
      boolean result = iConomy.getAccount(player.getName()).getHoldings().hasEnough(money);
      if (!result) {
         this.userIsTooPoor(player, -1, message);
      }

      return result;
   }

   protected void takeMoney(Player player, double amount) {
      iConomy.getAccount(player.getName()).getHoldings().subtract(amount);
      this.showReceipt(player, amount, -1);
   }

   protected String getFormattedMoneyAmount(Player player, double amount) {
      return iConomy.format(amount);
   }

   protected double getMoneyBalance(Player p) {
      return iConomy.getAccount(p.getName()).getHoldings().balance();
   }

   protected void giveMoney(Player player, double amount) {
      iConomy.getAccount(player.getName()).getHoldings().add(amount);
      this.showReceipt(player, amount * (double)-1.0F, -1);
   }
}
