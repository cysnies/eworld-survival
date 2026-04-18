package com.fernferret.allpay.multiverse;

import java.util.HashMap;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

public abstract class GenericBank {
   private boolean receipts = true;
   private String prefix;

   public GenericBank() {
      super();
   }

   protected final boolean hasItem(Player player, double amount, int type, String message) {
      boolean hasEnough = player.getInventory().contains(type, (int)amount);
      if (!hasEnough) {
         this.userIsTooPoor(player, type, message);
      }

      return hasEnough;
   }

   protected abstract boolean hasMoney(Player var1, double var2, String var4);

   public final boolean hasEnough(Player player, double amount, int type, String message) {
      if (amount == (double)0.0F) {
         return true;
      } else {
         return type == -1 ? this.hasMoney(player, amount, message) : this.hasItem(player, amount, type, message);
      }
   }

   public final boolean hasEnough(Player player, double amount, int type) {
      return this.hasEnough(player, amount, type, (String)null);
   }

   protected final void takeItem(Player player, double amount, int type) {
      int removed = 0;
      HashMap<Integer, ItemStack> items = player.getInventory().all(type);

      for(int i : items.keySet()) {
         if ((double)removed >= amount) {
            break;
         }

         int diff = (int)(amount - (double)removed);
         int amt = player.getInventory().getItem(i).getAmount();
         if (amt - diff > 0) {
            player.getInventory().getItem(i).setAmount(amt - diff);
            break;
         }

         removed += amt;
         player.getInventory().clear(i);
      }

      this.showReceipt(player, amount, type);
   }

   protected abstract void takeMoney(Player var1, double var2);

   public final void take(Player player, double amount, int type) {
      if (type == -1) {
         this.takeMoney(player, amount);
      } else {
         this.takeItem(player, amount, type);
      }

   }

   public final void give(Player player, double amount, int type) {
      if (type == -1) {
         this.giveMoney(player, amount);
      } else {
         this.giveItem(player, amount, type);
      }

   }

   protected abstract void giveMoney(Player var1, double var2);

   protected final void giveItem(Player player, double amount, int type) {
      ItemStack item = new ItemStack(type, (int)amount);
      player.getInventory().addItem(new ItemStack[]{item});
      this.showReceipt(player, amount * (double)-1.0F, type);
   }

   public final void transfer(Player from, Player to, double amount, int type) {
      if (type == -1) {
         this.transferMoney(from, to, amount);
      } else {
         this.transferItem(from, to, amount, type);
      }

   }

   protected void transferMoney(Player from, Player to, double amount) {
      if (this.hasMoney(from, amount, "")) {
         this.takeMoney(from, amount);
         this.giveMoney(to, amount);
      }
   }

   protected final void transferItem(Player from, Player to, double amount, int type) {
      if (this.hasEnough(from, amount, type)) {
         this.takeItem(from, amount, type);
         this.giveItem(to, amount, type);
      }
   }

   public final String getFormattedItemAmount(double amount, int type) {
      Material m = Material.getMaterial(type);
      return m != null ? amount + " " + m.toString() : "NO ITEM FOUND";
   }

   protected abstract String getFormattedMoneyAmount(Player var1, double var2);

   public final String getFormattedAmount(Player player, double amount, int item) {
      return item == -1 ? this.getFormattedMoneyAmount(player, amount) : this.getFormattedItemAmount(amount, item);
   }

   protected final void userIsTooPoor(Player player, int item, String message) {
      String type = item == -1 ? "funds" : "items";
      if (message == null) {
         message = "";
      } else {
         message = " " + message;
      }

      player.sendMessage(ChatColor.DARK_RED + this.prefix + ChatColor.WHITE + "Sorry but you do not have the required " + type + message);
   }

   protected void showReceipt(Player player, double price, int item) {
      if (this.receipts) {
         if (price > (double)0.0F) {
            player.sendMessage(String.format("%s%s%s%s%s %s", ChatColor.DARK_GREEN, this.prefix, ChatColor.WHITE, "You have been charged", ChatColor.GREEN, this.getFormattedAmount(player, price, item)));
         } else if (price < (double)0.0F) {
            player.sendMessage(String.format("%s%s%s%s %s", ChatColor.DARK_GREEN, this.prefix, this.getFormattedAmount(player, price * (double)-1.0F, item), ChatColor.WHITE, "has been added to your account."));
         }
      }

   }

   protected void showError(Player player, String message) {
      player.sendMessage(ChatColor.DARK_RED + this.prefix + ChatColor.WHITE + message);
   }

   public abstract String getEconUsed();

   public final void setPrefix(String prefix) {
      this.prefix = prefix;
   }

   protected final String formatCurrency(double amount, String currencySingular, String currencyPlural) {
      boolean inFront = false;
      if (currencySingular != null && currencySingular.length() == 1 && currencySingular.matches("[a-zA-Z]")) {
         inFront = true;
      }

      if (currencyPlural != null && amount != (double)1.0F && !inFront) {
         return amount + " " + currencyPlural;
      } else {
         return inFront ? currencySingular + amount : amount + " " + currencySingular;
      }
   }

   public boolean setBalance(Player player, int itemId, double amount) {
      return itemId == -1 ? this.setMoneyBalance(player, amount) : this.setItemAmount(player, itemId, amount);
   }

   protected abstract boolean setMoneyBalance(Player var1, double var2);

   protected final boolean setItemAmount(Player player, int type, double amount) {
      int numberOfItems = this.getItemAmount(player, type);
      if ((double)numberOfItems > amount) {
         this.takeItem(player, (double)numberOfItems - amount, type);
      } else if ((double)numberOfItems < amount) {
         this.giveItem(player, amount - (double)numberOfItems, type);
      }

      return true;
   }

   public double getBalance(Player player, int itemId) {
      return itemId == -1 ? this.getMoneyBalance(player) : (double)this.getItemAmount(player, itemId);
   }

   protected abstract double getMoneyBalance(Player var1);

   /** @deprecated */
   @Deprecated
   protected final int getItemAnount(Player player, int type) {
      return this.getItemAmount(player, type);
   }

   protected final int getItemAmount(Player player, int type) {
      HashMap<Integer, ItemStack> items = player.getInventory().all(type);
      int total = 0;

      for(int i : items.keySet()) {
         total += player.getInventory().getItem(i).getAmount();
      }

      return total;
   }

   public void toggleReceipts(boolean showRecipts) {
      this.receipts = showRecipts;
   }
}
