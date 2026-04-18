package com.fernferret.allpay.multiverse;

import org.bukkit.entity.Player;

public class ItemBank extends GenericBank {
   public ItemBank() {
      super();
   }

   protected String getFormattedMoneyAmount(Player player, double amount) {
      return "";
   }

   protected boolean hasMoney(Player player, double money, String message) {
      return true;
   }

   public void showReceipt(Player player, double price, int item) {
      if (item != -1) {
         super.showReceipt(player, price, item);
      }

   }

   protected void takeMoney(Player player, double amount) {
   }

   public String getEconUsed() {
      return "Simple Item Economy";
   }

   protected boolean setMoneyBalance(Player player, double amount) {
      return false;
   }

   protected double getMoneyBalance(Player p) {
      return (double)0.0F;
   }

   public void giveMoney(Player player, double amount) {
   }
}
