package com.fernferret.allpay.multiverse;

import fr.crafter.tickleman.RealEconomy.RealEconomy;
import org.bukkit.entity.Player;

public class RealEconomyBank extends GenericBank {
   private RealEconomy plugin;

   public RealEconomyBank(RealEconomy plugin) {
      super();
      this.plugin = plugin;
   }

   protected String getFormattedMoneyAmount(Player player, double amount) {
      return this.formatCurrency(amount, this.plugin.getCurrency(), (String)null);
   }

   protected boolean hasMoney(Player player, double money, String message) {
      boolean result = this.plugin.getBalance(player.getName()) >= money;
      if (!result) {
         this.userIsTooPoor(player, -1, message);
      }

      return result;
   }

   protected void takeMoney(Player player, double amount) {
      double totalmoney = this.plugin.getBalance(player.getName());
      this.plugin.setBalance(player.getName(), totalmoney - amount);
      this.showReceipt(player, amount, -1);
   }

   public String getEconUsed() {
      return "RealEconomy";
   }

   protected boolean setMoneyBalance(Player player, double amount) {
      this.plugin.setBalance(player.getName(), amount);
      return true;
   }

   protected double getMoneyBalance(Player p) {
      return this.plugin.getBalance(p.getName());
   }

   protected void giveMoney(Player player, double amount) {
      double totalmoney = this.plugin.getBalance(player.getName());
      this.plugin.setBalance(player.getName(), totalmoney + amount);
      this.showReceipt(player, amount * (double)-1.0F, -1);
   }
}
