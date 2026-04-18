package com.earth2me.essentials.signs;

import com.earth2me.essentials.ChargeException;
import com.earth2me.essentials.I18n;
import com.earth2me.essentials.Trade;
import com.earth2me.essentials.User;
import com.earth2me.essentials.utils.NumberUtil;
import java.math.BigDecimal;
import java.util.Map;
import net.ess3.api.IEssentials;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

public class SignTrade extends EssentialsSign {
   public SignTrade() {
      super("Trade");
   }

   protected boolean onSignCreate(EssentialsSign.ISign sign, User player, String username, IEssentials ess) throws SignException, ChargeException {
      this.validateTrade(sign, 1, false, ess);
      this.validateTrade(sign, 2, true, ess);
      Trade trade = this.getTrade(sign, 2, SignTrade.AmountType.ROUNDED, true, ess);
      Trade charge = this.getTrade(sign, 1, SignTrade.AmountType.ROUNDED, false, ess);
      if (trade.getType() != charge.getType() || trade.getType() == Trade.TradeType.ITEM && !trade.getItemStack().isSimilar(charge.getItemStack())) {
         trade.isAffordableFor(player);
         sign.setLine(3, "§8" + username);
         trade.charge(player);
         Trade.log("Sign", "Trade", "Create", username, trade, username, (Trade)null, sign.getBlock().getLocation(), ess);
         return true;
      } else {
         throw new SignException("You cannot trade for the same item type.");
      }
   }

   protected boolean onSignInteract(EssentialsSign.ISign sign, User player, String username, IEssentials ess) throws SignException, ChargeException {
      if (sign.getLine(3).substring(2).equalsIgnoreCase(username)) {
         Trade store = this.rechargeSign(sign, ess, player);
         Trade stored = null;

         try {
            stored = this.getTrade(sign, 1, SignTrade.AmountType.TOTAL, true, ess);
            this.subtractAmount(sign, 1, stored, ess);
            Map<Integer, ItemStack> withdraw = stored.pay(player, Trade.OverflowType.RETURN);
            if (withdraw == null) {
               Trade.log("Sign", "Trade", "Withdraw", username, store, username, (Trade)null, sign.getBlock().getLocation(), ess);
            } else {
               this.setAmount(sign, 1, BigDecimal.valueOf((long)((ItemStack)withdraw.get(0)).getAmount()), ess);
               Trade.log("Sign", "Trade", "Withdraw", username, stored, username, new Trade((ItemStack)withdraw.get(0), ess), sign.getBlock().getLocation(), ess);
            }
         } catch (SignException e) {
            if (store == null) {
               throw new SignException(I18n._("tradeSignEmptyOwner"), e);
            }
         }

         Trade.log("Sign", "Trade", "Deposit", username, store, username, (Trade)null, sign.getBlock().getLocation(), ess);
      } else {
         Trade charge = this.getTrade(sign, 1, SignTrade.AmountType.COST, false, ess);
         Trade trade = this.getTrade(sign, 2, SignTrade.AmountType.COST, true, ess);
         charge.isAffordableFor(player);
         this.addAmount(sign, 1, charge, ess);
         this.subtractAmount(sign, 2, trade, ess);
         if (!trade.pay(player)) {
            this.subtractAmount(sign, 1, charge, ess);
            this.addAmount(sign, 2, trade, ess);
            throw new ChargeException("Full inventory");
         }

         charge.charge(player);
         Trade.log("Sign", "Trade", "Interact", sign.getLine(3), charge, username, trade, sign.getBlock().getLocation(), ess);
      }

      sign.updateSign();
      return true;
   }

   private Trade rechargeSign(EssentialsSign.ISign sign, IEssentials ess, User player) throws SignException, ChargeException {
      Trade trade = this.getTrade(sign, 2, SignTrade.AmountType.COST, false, ess);
      if (trade.getItemStack() != null && player.getItemInHand() != null && trade.getItemStack().getType() == player.getItemInHand().getType() && trade.getItemStack().getDurability() == player.getItemInHand().getDurability() && trade.getItemStack().getEnchantments().equals(player.getItemInHand().getEnchantments())) {
         int amount = player.getItemInHand().getAmount();
         amount -= amount % trade.getItemStack().getAmount();
         if (amount > 0) {
            ItemStack stack = player.getItemInHand().clone();
            stack.setAmount(amount);
            Trade store = new Trade(stack, ess);
            this.addAmount(sign, 2, store, ess);
            store.charge(player);
            return store;
         }
      }

      return null;
   }

   protected boolean onSignBreak(EssentialsSign.ISign sign, User player, String username, IEssentials ess) throws SignException {
      if ((sign.getLine(3).length() <= 3 || !sign.getLine(3).substring(2).equalsIgnoreCase(username)) && !player.isAuthorized("essentials.signs.trade.override")) {
         return false;
      } else {
         try {
            Trade stored1 = this.getTrade(sign, 1, SignTrade.AmountType.TOTAL, false, ess);
            Trade stored2 = this.getTrade(sign, 2, SignTrade.AmountType.TOTAL, false, ess);
            Map<Integer, ItemStack> withdraw1 = stored1.pay(player, Trade.OverflowType.RETURN);
            Map<Integer, ItemStack> withdraw2 = stored2.pay(player, Trade.OverflowType.RETURN);
            if (withdraw1 == null && withdraw2 == null) {
               Trade.log("Sign", "Trade", "Break", username, stored2, username, stored1, sign.getBlock().getLocation(), ess);
               return true;
            } else {
               this.setAmount(sign, 1, BigDecimal.valueOf(withdraw1 == null ? 0L : (long)((ItemStack)withdraw1.get(0)).getAmount()), ess);
               Trade.log("Sign", "Trade", "Withdraw", username, stored1, username, withdraw1 == null ? null : new Trade((ItemStack)withdraw1.get(0), ess), sign.getBlock().getLocation(), ess);
               this.setAmount(sign, 2, BigDecimal.valueOf(withdraw2 == null ? 0L : (long)((ItemStack)withdraw2.get(0)).getAmount()), ess);
               Trade.log("Sign", "Trade", "Withdraw", username, stored2, username, withdraw2 == null ? null : new Trade((ItemStack)withdraw2.get(0), ess), sign.getBlock().getLocation(), ess);
               sign.updateSign();
               return false;
            }
         } catch (SignException e) {
            if (player.isAuthorized("essentials.signs.trade.override")) {
               return true;
            } else {
               throw e;
            }
         }
      }
   }

   protected final void validateTrade(EssentialsSign.ISign sign, int index, boolean amountNeeded, IEssentials ess) throws SignException {
      String line = sign.getLine(index).trim();
      if (line.isEmpty()) {
         throw new SignException("Empty line");
      } else {
         String[] split = line.split("[ :]+");
         if (split.length == 1 && !amountNeeded) {
            BigDecimal money = this.getMoney(split[0]);
            if (money != null) {
               if (NumberUtil.shortCurrency(money, ess).length() * 2 > 15) {
                  throw new SignException("Line can be too long!");
               }

               sign.setLine(index, NumberUtil.shortCurrency(money, ess) + ":0");
               return;
            }
         }

         if (split.length == 2 && amountNeeded) {
            BigDecimal money = this.getMoney(split[0]);
            BigDecimal amount = this.getBigDecimalPositive(split[1]);
            if (money != null && amount != null) {
               amount = amount.subtract(amount.remainder(money));
               if (amount.compareTo(MINTRANSACTION) >= 0 && money.compareTo(MINTRANSACTION) >= 0) {
                  sign.setLine(index, NumberUtil.shortCurrency(money, ess) + ":" + NumberUtil.shortCurrency(amount, ess).substring(1));
                  return;
               }

               throw new SignException(I18n._("moreThanZero"));
            }
         }

         if (split.length == 2 && !amountNeeded) {
            int amount = this.getIntegerPositive(split[0]);
            if (amount < 1) {
               throw new SignException(I18n._("moreThanZero"));
            } else if (!split[1].equalsIgnoreCase("exp") && !split[1].equalsIgnoreCase("xp") && this.getItemStack(split[1], amount, ess).getType() == Material.AIR) {
               throw new SignException(I18n._("moreThanZero"));
            } else {
               String newline = amount + " " + split[1] + ":0";
               if ((newline + amount).length() > 15) {
                  throw new SignException("Line can be too long!");
               } else {
                  sign.setLine(index, newline);
               }
            }
         } else if (split.length == 3 && amountNeeded) {
            int stackamount = this.getIntegerPositive(split[0]);
            int amount = this.getIntegerPositive(split[2]);
            amount -= amount % stackamount;
            if (amount >= 1 && stackamount >= 1) {
               if (!split[1].equalsIgnoreCase("exp") && !split[1].equalsIgnoreCase("xp") && this.getItemStack(split[1], stackamount, ess).getType() == Material.AIR) {
                  throw new SignException(I18n._("moreThanZero"));
               } else {
                  sign.setLine(index, stackamount + " " + split[1] + ":" + amount);
               }
            } else {
               throw new SignException(I18n._("moreThanZero"));
            }
         } else {
            throw new SignException(I18n._("invalidSignLine", index + 1));
         }
      }
   }

   protected final Trade getTrade(EssentialsSign.ISign sign, int index, AmountType amountType, boolean notEmpty, IEssentials ess) throws SignException {
      String line = sign.getLine(index).trim();
      if (line.isEmpty()) {
         throw new SignException("Empty line");
      } else {
         String[] split = line.split("[ :]+");
         if (split.length == 2) {
            try {
               BigDecimal money = this.getMoney(split[0]);
               BigDecimal amount = notEmpty ? this.getBigDecimalPositive(split[1]) : this.getBigDecimal(split[1]);
               if (money != null && amount != null) {
                  return new Trade(amountType == SignTrade.AmountType.COST ? money : amount, ess);
               }
            } catch (SignException e) {
               throw new SignException(I18n._("tradeSignEmpty"), e);
            }
         }

         if (split.length != 3) {
            throw new SignException(I18n._("invalidSignLine", index + 1));
         } else if (!split[1].equalsIgnoreCase("exp") && !split[1].equalsIgnoreCase("xp")) {
            int stackamount = this.getIntegerPositive(split[0]);
            ItemStack item = this.getItemStack(split[1], stackamount, ess);
            int amount = this.getInteger(split[2]);
            if (amountType == SignTrade.AmountType.ROUNDED) {
               amount -= amount % stackamount;
            }

            if (!notEmpty || amount >= 1 && stackamount >= 1 && item.getType() != Material.AIR && amount >= stackamount) {
               item.setAmount(amountType == SignTrade.AmountType.COST ? stackamount : amount);
               return new Trade(item, ess);
            } else {
               throw new SignException(I18n._("tradeSignEmpty"));
            }
         } else {
            int stackamount = this.getIntegerPositive(split[0]);
            int amount = this.getInteger(split[2]);
            if (amountType == SignTrade.AmountType.ROUNDED) {
               amount -= amount % stackamount;
            }

            if (!notEmpty || amount >= 1 && stackamount >= 1) {
               return new Trade(amountType == SignTrade.AmountType.COST ? stackamount : amount, ess);
            } else {
               throw new SignException(I18n._("tradeSignEmpty"));
            }
         }
      }
   }

   protected final void subtractAmount(EssentialsSign.ISign sign, int index, Trade trade, IEssentials ess) throws SignException {
      BigDecimal money = trade.getMoney();
      if (money != null) {
         this.changeAmount(sign, index, money.negate(), ess);
      }

      ItemStack item = trade.getItemStack();
      if (item != null) {
         this.changeAmount(sign, index, BigDecimal.valueOf((long)(-item.getAmount())), ess);
      }

      Integer exp = trade.getExperience();
      if (exp != null) {
         this.changeAmount(sign, index, BigDecimal.valueOf((long)(-exp)), ess);
      }

   }

   protected final void addAmount(EssentialsSign.ISign sign, int index, Trade trade, IEssentials ess) throws SignException {
      BigDecimal money = trade.getMoney();
      if (money != null) {
         this.changeAmount(sign, index, money, ess);
      }

      ItemStack item = trade.getItemStack();
      if (item != null) {
         this.changeAmount(sign, index, BigDecimal.valueOf((long)item.getAmount()), ess);
      }

      Integer exp = trade.getExperience();
      if (exp != null) {
         this.changeAmount(sign, index, BigDecimal.valueOf((long)exp), ess);
      }

   }

   private void changeAmount(EssentialsSign.ISign sign, int index, BigDecimal value, IEssentials ess) throws SignException {
      String line = sign.getLine(index).trim();
      if (line.isEmpty()) {
         throw new SignException("Empty line");
      } else {
         String[] split = line.split("[ :]+");
         if (split.length == 2) {
            BigDecimal amount = this.getBigDecimal(split[1]).add(value);
            this.setAmount(sign, index, amount, ess);
         } else if (split.length == 3) {
            BigDecimal amount = this.getBigDecimal(split[2]).add(value);
            this.setAmount(sign, index, amount, ess);
         } else {
            throw new SignException(I18n._("invalidSignLine", index + 1));
         }
      }
   }

   private void setAmount(EssentialsSign.ISign sign, int index, BigDecimal value, IEssentials ess) throws SignException {
      String line = sign.getLine(index).trim();
      if (line.isEmpty()) {
         throw new SignException("Empty line");
      } else {
         String[] split = line.split("[ :]+");
         if (split.length == 2) {
            BigDecimal money = this.getMoney(split[0]);
            BigDecimal amount = this.getBigDecimal(split[1]);
            if (money != null && amount != null) {
               String newline = NumberUtil.shortCurrency(money, ess) + ":" + NumberUtil.shortCurrency(value, ess).substring(1);
               if (newline.length() > 15) {
                  throw new SignException("This sign is full: Line too long!");
               }

               sign.setLine(index, newline);
               return;
            }
         }

         if (split.length == 3) {
            if (!split[1].equalsIgnoreCase("exp") && !split[1].equalsIgnoreCase("xp")) {
               int stackamount = this.getIntegerPositive(split[0]);
               this.getItemStack(split[1], stackamount, ess);
               String newline = stackamount + " " + split[1] + ":" + value.intValueExact();
               if (newline.length() > 15) {
                  throw new SignException("This sign is full: Line too long!");
               } else {
                  sign.setLine(index, newline);
               }
            } else {
               int stackamount = this.getIntegerPositive(split[0]);
               String newline = stackamount + " " + split[1] + ":" + value.intValueExact();
               if (newline.length() > 15) {
                  throw new SignException("This sign is full: Line too long!");
               } else {
                  sign.setLine(index, newline);
               }
            }
         } else {
            throw new SignException(I18n._("invalidSignLine", index + 1));
         }
      }
   }

   public static enum AmountType {
      TOTAL,
      ROUNDED,
      COST;

      private AmountType() {
      }
   }
}
