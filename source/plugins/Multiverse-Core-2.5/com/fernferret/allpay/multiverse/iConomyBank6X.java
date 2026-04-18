package com.fernferret.allpay.multiverse;

import com.iCo6.iConomy;
import com.iCo6.system.Accounts;
import org.bukkit.entity.Player;

public class iConomyBank6X extends GenericBank {
   private Accounts accounts = new Accounts();

   public iConomyBank6X() {
      super();
   }

   public String getEconUsed() {
      return "iConomy 6";
   }

   protected boolean setMoneyBalance(Player player, double amount) {
      this.accounts.get(player.getName()).getHoldings().setBalance(amount);
      return true;
   }

   protected boolean hasMoney(Player player, double money, String message) {
      boolean result = this.accounts.get(player.getName()).getHoldings().hasEnough(money);
      if (!result) {
         this.userIsTooPoor(player, -1, message);
      }

      return result;
   }

   protected void takeMoney(Player player, double amount) {
      this.accounts.get(player.getName()).getHoldings().subtract(amount);
      this.showReceipt(player, amount, -1);
   }

   protected String getFormattedMoneyAmount(Player player, double amount) {
      return iConomy.format(amount);
   }

   protected double getMoneyBalance(Player p) {
      return this.accounts.get(p.getName()).getHoldings().getBalance();
   }

   protected void giveMoney(Player player, double amount) {
      this.accounts.get(player.getName()).getHoldings().add(amount);
      this.showReceipt(player, amount * (double)-1.0F, -1);
   }
}
