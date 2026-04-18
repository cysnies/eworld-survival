package com.fernferret.allpay.multiverse;

import com.nijiko.coelho.iConomy.iConomy;
import org.bukkit.entity.Player;

public class iConomyBank4X extends GenericBank {
   public iConomyBank4X() {
      super();
   }

   public String getEconUsed() {
      return "iConomy 4";
   }

   protected boolean setMoneyBalance(Player player, double amount) {
      iConomy.getBank().getAccount(player.getName()).setBalance(amount);
      return true;
   }

   protected boolean hasMoney(Player player, double money, String message) {
      boolean result = iConomy.getBank().getAccount(player.getName()).hasEnough(money);
      if (!result) {
         this.userIsTooPoor(player, -1, message);
      }

      return result;
   }

   protected void takeMoney(Player player, double amount) {
      iConomy.getBank().getAccount(player.getName()).subtract(amount);
      this.showReceipt(player, amount, -1);
   }

   protected String getFormattedMoneyAmount(Player player, double amount) {
      return this.formatCurrency(amount, iConomy.getBank().getCurrency(), (String)null);
   }

   protected double getMoneyBalance(Player p) {
      return iConomy.getBank().getAccount(p.getName()).getBalance();
   }

   protected void giveMoney(Player player, double amount) {
      iConomy.getBank().getAccount(player.getName()).add(amount);
      this.showReceipt(player, amount * (double)-1.0F, -1);
   }
}
