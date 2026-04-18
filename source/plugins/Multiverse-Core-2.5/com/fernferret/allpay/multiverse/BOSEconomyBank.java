package com.fernferret.allpay.multiverse;

import cosine.boseconomy.BOSEconomy;
import org.bukkit.entity.Player;

public class BOSEconomyBank extends GenericBank {
   private BOSEconomy plugin;

   public BOSEconomyBank(BOSEconomy plugin) {
      super();
      this.plugin = plugin;
   }

   public String getFormattedMoneyAmount(Player player, double amount) {
      return this.formatCurrency(amount, this.plugin.getMoneyName(), this.plugin.getMoneyNamePlural());
   }

   public boolean hasMoney(Player player, double money, String message) {
      boolean result = (double)this.plugin.getPlayerMoney(player.getName()) >= money;
      if (!result) {
         this.userIsTooPoor(player, -1, message);
      }

      return result;
   }

   public void takeMoney(Player player, double amount) {
      int negativePrice = (int)((double)-1.0F * Math.abs(amount));
      this.plugin.addPlayerMoney(player.getName(), negativePrice, true);
      this.showReceipt(player, amount, -1);
   }

   public String getEconUsed() {
      return "BOSEconomy";
   }

   protected boolean setMoneyBalance(Player player, double amount) {
      this.plugin.setPlayerMoney(player.getName(), (int)amount, true);
      return true;
   }

   protected double getMoneyBalance(Player p) {
      return (double)this.plugin.getPlayerMoney(p.getName());
   }

   protected void giveMoney(Player player, double amount) {
      this.plugin.addPlayerMoney(player.getName(), (int)amount, true);
      this.showReceipt(player, amount * (double)-1.0F, -1);
   }

   protected void transferMoney(Player from, Player to, double amount) {
      if (this.hasMoney(from, amount, "")) {
         this.takeMoney(from, amount);
         this.giveMoney(to, amount);
      }
   }
}
