package com.fernferret.allpay.multiverse;

import com.earth2me.essentials.api.Economy;
import com.earth2me.essentials.api.NoLoanPermittedException;
import com.earth2me.essentials.api.UserDoesNotExistException;
import org.bukkit.entity.Player;

public class EssentialsBank extends GenericBank {
   public EssentialsBank() {
      super();
   }

   public String getEconUsed() {
      return "Essentials Economy";
   }

   protected boolean setMoneyBalance(Player player, double amount) {
      try {
         Economy.setMoney(player.getName(), amount);
         return true;
      } catch (UserDoesNotExistException var5) {
         this.showError(player, "You don't have an account!");
      } catch (NoLoanPermittedException var6) {
         this.showError(player, "Your bank doesn't allow loans!");
      }

      return false;
   }

   public String getFormattedMoneyAmount(Player player, double amount) {
      return Economy.format(amount);
   }

   public boolean hasMoney(Player player, double money, String message) {
      try {
         return Economy.hasEnough(player.getName(), money);
      } catch (UserDoesNotExistException var6) {
         return false;
      }
   }

   public void takeMoney(Player player, double amount) {
      try {
         Economy.subtract(player.getName(), amount);
         this.showReceipt(player, amount, -1);
      } catch (UserDoesNotExistException var5) {
         this.showError(player, "You don't have an account!");
      } catch (NoLoanPermittedException var6) {
         this.showError(player, "Your bank doesn't allow loans!");
      }

   }

   protected double getMoneyBalance(Player p) {
      try {
         return Economy.getMoney(p.getName());
      } catch (UserDoesNotExistException var3) {
         this.showError(p, "You don't have an account!");
         return (double)0.0F;
      }
   }

   public void giveMoney(Player player, double amount) {
      try {
         Economy.add(player.getName(), amount);
         this.showReceipt(player, amount * (double)-1.0F, -1);
      } catch (UserDoesNotExistException var5) {
         this.showError(player, "You don't have an account!");
      } catch (NoLoanPermittedException var6) {
         this.showError(player, "Your bank doesn't allow loans!");
      }

   }
}
