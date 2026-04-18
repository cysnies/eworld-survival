package com.fernferret.allpay.multiverse;

import ca.agnate.EconXP.EconXP;
import org.bukkit.entity.Player;

public class EconXPBank extends GenericBank {
   private EconXP plugin;

   public EconXPBank(EconXP plugin) {
      super();
      this.plugin = plugin;
   }

   public String getFormattedMoneyAmount(Player player, double amount) {
      return this.formatCurrency(amount, "XP", "XP");
   }

   public boolean hasMoney(Player player, double money, String message) {
      boolean result = (double)this.plugin.getExp(player) >= money;
      if (!result) {
         this.userIsTooPoor(player, -1, message);
      }

      return result;
   }

   public void takeMoney(Player player, double amount) {
      this.plugin.removeExp(player, (int)amount);
      this.showReceipt(player, amount, -1);
   }

   public String getEconUsed() {
      return "EconXP";
   }

   protected boolean setMoneyBalance(Player player, double amount) {
      this.plugin.setExp(player, (int)amount);
      return true;
   }

   protected double getMoneyBalance(Player p) {
      return (double)this.plugin.getExp(p);
   }

   protected void giveMoney(Player player, double amount) {
      this.plugin.addExp(player, (int)amount);
      this.showReceipt(player, amount * (double)-1.0F, -1);
   }
}
