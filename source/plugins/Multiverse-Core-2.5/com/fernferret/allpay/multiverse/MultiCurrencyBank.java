package com.fernferret.allpay.multiverse;

import me.ashtheking.currency.CurrencyList;
import org.bukkit.entity.Player;

public class MultiCurrencyBank extends GenericBank {
   public MultiCurrencyBank() {
      super();
   }

   public String getEconUsed() {
      return "MultiCurrency";
   }

   protected boolean setMoneyBalance(Player player, double amount) {
      CurrencyList.setValue((String)CurrencyList.maxCurrency(player.getName())[0], player.getName(), amount);
      return true;
   }

   protected boolean hasMoney(Player player, double money, String message) {
      boolean result = CurrencyList.hasEnough(player.getName(), money);
      if (!result) {
         this.userIsTooPoor(player, -1, message);
      }

      return result;
   }

   protected void takeMoney(Player player, double amount) {
      CurrencyList.subtract(player.getName(), amount);
      this.showReceipt(player, amount, -1);
   }

   protected String getFormattedMoneyAmount(Player player, double amount) {
      return this.formatCurrency(amount, (String)CurrencyList.maxCurrency(player.getName())[0], (String)null);
   }

   protected double getMoneyBalance(Player p) {
      return CurrencyList.getValue((String)CurrencyList.maxCurrency(p.getName())[0], p.getName());
   }

   protected void giveMoney(Player player, double amount) {
      CurrencyList.add(player.getName(), amount);
      this.showReceipt(player, amount * (double)-1.0F, -1);
   }
}
