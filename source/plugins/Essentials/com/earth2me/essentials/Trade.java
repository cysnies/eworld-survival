package com.earth2me.essentials;

import com.earth2me.essentials.craftbukkit.InventoryWorkaround;
import com.earth2me.essentials.craftbukkit.SetExpFix;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.math.BigDecimal;
import java.text.DateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.bukkit.Location;
import org.bukkit.entity.Item;
import org.bukkit.inventory.ItemStack;

public class Trade {
   private final transient String command;
   private final transient Trade fallbackTrade;
   private final transient BigDecimal money;
   private final transient ItemStack itemStack;
   private final transient Integer exp;
   private final transient IEssentials ess;
   private static FileWriter fw = null;

   public Trade(String command, net.ess3.api.IEssentials ess) {
      this(command, (Trade)null, (BigDecimal)null, (ItemStack)null, (Integer)null, ess);
   }

   public Trade(String command, Trade fallback, net.ess3.api.IEssentials ess) {
      this(command, fallback, (BigDecimal)null, (ItemStack)null, (Integer)null, ess);
   }

   /** @deprecated */
   @Deprecated
   public Trade(double money, IEssentials ess) {
      this((String)null, (Trade)null, BigDecimal.valueOf(money), (ItemStack)null, (Integer)null, ess);
   }

   public Trade(BigDecimal money, net.ess3.api.IEssentials ess) {
      this((String)null, (Trade)null, money, (ItemStack)null, (Integer)null, ess);
   }

   public Trade(ItemStack items, net.ess3.api.IEssentials ess) {
      this((String)null, (Trade)null, (BigDecimal)null, items, (Integer)null, ess);
   }

   public Trade(int exp, net.ess3.api.IEssentials ess) {
      this((String)null, (Trade)null, (BigDecimal)null, (ItemStack)null, exp, ess);
   }

   private Trade(String command, Trade fallback, BigDecimal money, ItemStack item, Integer exp, IEssentials ess) {
      super();
      this.command = command;
      this.fallbackTrade = fallback;
      this.money = money;
      this.itemStack = item;
      this.exp = exp;
      this.ess = ess;
   }

   public void isAffordableFor(net.ess3.api.IUser user) throws ChargeException {
      if (this.ess.getSettings().isDebug()) {
         this.ess.getLogger().log(Level.INFO, "checking if " + user.getName() + " can afford charge.");
      }

      if (this.getMoney() != null && this.getMoney().signum() > 0 && !user.canAfford(this.getMoney())) {
         throw new ChargeException(I18n._("notEnoughMoney"));
      } else if (this.getItemStack() != null && !user.getBase().getInventory().containsAtLeast(this.itemStack, this.itemStack.getAmount())) {
         throw new ChargeException(I18n._("missingItems", this.getItemStack().getAmount(), this.ess.getItemDb().name(this.getItemStack())));
      } else {
         BigDecimal money;
         if (this.command != null && !this.command.isEmpty() && (money = this.getCommandCost(user)).signum() > 0 && !user.canAfford(money)) {
            throw new ChargeException(I18n._("notEnoughMoney"));
         } else if (this.exp != null && this.exp > 0 && SetExpFix.getTotalExperience(user.getBase()) < this.exp) {
            throw new ChargeException(I18n._("notEnoughExperience"));
         }
      }
   }

   public boolean pay(net.ess3.api.IUser user) {
      return this.pay(user, Trade.OverflowType.ABORT) == null;
   }

   public Map pay(net.ess3.api.IUser user, OverflowType type) {
      if (this.getMoney() != null && this.getMoney().signum() > 0) {
         if (this.ess.getSettings().isDebug()) {
            this.ess.getLogger().log(Level.INFO, "paying user " + user.getName() + " via trade " + this.getMoney().toPlainString());
         }

         user.giveMoney(this.getMoney());
      }

      if (this.getItemStack() != null) {
         Map<Integer, ItemStack> overFlow = InventoryWorkaround.addAllItems(user.getBase().getInventory(), this.getItemStack());
         if (overFlow != null) {
            switch (type) {
               case ABORT:
                  if (this.ess.getSettings().isDebug()) {
                     this.ess.getLogger().log(Level.INFO, "abort paying " + user.getName() + " itemstack " + this.getItemStack().toString() + " due to lack of inventory space ");
                  }

                  return overFlow;
               case RETURN:
                  Map<Integer, ItemStack> returnStack = InventoryWorkaround.addItems(user.getBase().getInventory(), this.getItemStack());
                  user.getBase().updateInventory();
                  if (this.ess.getSettings().isDebug()) {
                     this.ess.getLogger().log(Level.INFO, "paying " + user.getName() + " partial itemstack " + this.getItemStack().toString() + " with overflow " + ((ItemStack)returnStack.get(0)).toString());
                  }

                  return returnStack;
               case DROP:
                  Map<Integer, ItemStack> leftOver = InventoryWorkaround.addItems(user.getBase().getInventory(), this.getItemStack());
                  Location loc = user.getBase().getLocation();

                  for(ItemStack loStack : leftOver.values()) {
                     int maxStackSize = loStack.getType().getMaxStackSize();
                     int stacks = loStack.getAmount() / maxStackSize;
                     int leftover = loStack.getAmount() % maxStackSize;
                     Item[] itemStacks = new Item[stacks + (leftover > 0 ? 1 : 0)];

                     for(int i = 0; i < stacks; ++i) {
                        ItemStack stack = loStack.clone();
                        stack.setAmount(maxStackSize);
                        itemStacks[i] = loc.getWorld().dropItem(loc, stack);
                     }

                     if (leftover > 0) {
                        ItemStack stack = loStack.clone();
                        stack.setAmount(leftover);
                        itemStacks[stacks] = loc.getWorld().dropItem(loc, stack);
                     }
                  }

                  if (this.ess.getSettings().isDebug()) {
                     this.ess.getLogger().log(Level.INFO, "paying " + user.getName() + " partial itemstack " + this.getItemStack().toString() + " and dropping overflow " + ((ItemStack)leftOver.get(0)).toString());
                  }
            }
         } else if (this.ess.getSettings().isDebug()) {
            this.ess.getLogger().log(Level.INFO, "paying " + user.getName() + " itemstack " + this.getItemStack().toString());
         }

         user.getBase().updateInventory();
      }

      if (this.getExperience() != null) {
         SetExpFix.setTotalExperience(user.getBase(), SetExpFix.getTotalExperience(user.getBase()) + this.getExperience());
      }

      return null;
   }

   public void charge(net.ess3.api.IUser user) throws ChargeException {
      if (this.ess.getSettings().isDebug()) {
         this.ess.getLogger().log(Level.INFO, "attempting to charge user " + user.getName());
      }

      if (this.getMoney() != null) {
         if (this.ess.getSettings().isDebug()) {
            this.ess.getLogger().log(Level.INFO, "charging user " + user.getName() + " money " + this.getMoney().toPlainString());
         }

         if (!user.canAfford(this.getMoney()) && this.getMoney().signum() > 0) {
            throw new ChargeException(I18n._("notEnoughMoney"));
         }

         user.takeMoney(this.getMoney());
      }

      if (this.getItemStack() != null) {
         if (this.ess.getSettings().isDebug()) {
            this.ess.getLogger().log(Level.INFO, "charging user " + user.getName() + " itemstack " + this.getItemStack().toString());
         }

         if (!user.getBase().getInventory().containsAtLeast(this.getItemStack(), this.getItemStack().getAmount())) {
            throw new ChargeException(I18n._("missingItems", this.getItemStack().getAmount(), this.getItemStack().getType().toString().toLowerCase(Locale.ENGLISH).replace("_", " ")));
         }

         user.getBase().getInventory().removeItem(new ItemStack[]{this.getItemStack()});
         user.getBase().updateInventory();
      }

      if (this.command != null) {
         BigDecimal cost = this.getCommandCost(user);
         if (!user.canAfford(cost) && cost.signum() > 0) {
            throw new ChargeException(I18n._("notEnoughMoney"));
         }

         user.takeMoney(cost);
      }

      if (this.getExperience() != null) {
         if (this.ess.getSettings().isDebug()) {
            this.ess.getLogger().log(Level.INFO, "charging user " + user.getName() + " exp " + this.getExperience());
         }

         int experience = SetExpFix.getTotalExperience(user.getBase());
         if (experience < this.getExperience() && this.getExperience() > 0) {
            throw new ChargeException(I18n._("notEnoughExperience"));
         }

         SetExpFix.setTotalExperience(user.getBase(), experience - this.getExperience());
      }

      if (this.ess.getSettings().isDebug()) {
         this.ess.getLogger().log(Level.INFO, "charge user " + user.getName() + " completed");
      }

   }

   public BigDecimal getMoney() {
      return this.money;
   }

   public ItemStack getItemStack() {
      return this.itemStack;
   }

   public Integer getExperience() {
      return this.exp;
   }

   public TradeType getType() {
      if (this.getExperience() != null) {
         return Trade.TradeType.EXP;
      } else {
         return this.getItemStack() != null ? Trade.TradeType.ITEM : Trade.TradeType.MONEY;
      }
   }

   public BigDecimal getCommandCost(net.ess3.api.IUser user) {
      BigDecimal cost = BigDecimal.ZERO;
      if (this.command != null && !this.command.isEmpty()) {
         cost = this.ess.getSettings().getCommandCost(this.command.charAt(0) == '/' ? this.command.substring(1) : this.command);
         if (cost.signum() == 0 && this.fallbackTrade != null) {
            cost = this.fallbackTrade.getCommandCost(user);
         }

         if (this.ess.getSettings().isDebug()) {
            this.ess.getLogger().log(Level.INFO, "calculated command (" + this.command + ") cost for " + user.getName() + " as " + cost);
         }
      }

      return cost.signum() == 0 || !user.isAuthorized("essentials.nocommandcost.all") && !user.isAuthorized("essentials.nocommandcost." + this.command) ? cost : BigDecimal.ZERO;
   }

   public static void log(String type, String subtype, String event, String sender, Trade charge, String receiver, Trade pay, Location loc, net.ess3.api.IEssentials ess) {
      if ((loc != null || ess.getSettings().isEcoLogUpdateEnabled()) && (loc == null || ess.getSettings().isEcoLogEnabled())) {
         if (fw == null) {
            try {
               fw = new FileWriter(new File(ess.getDataFolder(), "trade.log"), true);
            } catch (IOException ex) {
               Logger.getLogger("Minecraft").log(Level.SEVERE, (String)null, ex);
            }
         }

         StringBuilder sb = new StringBuilder();
         sb.append(type).append(",").append(subtype).append(",").append(event).append(",\"");
         sb.append(DateFormat.getDateTimeInstance(0, 0).format(new Date()));
         sb.append("\",\"");
         if (sender != null) {
            sb.append(sender);
         }

         sb.append("\",");
         if (charge == null) {
            sb.append("\"\",\"\",\"\"");
         } else {
            if (charge.getItemStack() != null) {
               sb.append(charge.getItemStack().getAmount()).append(",");
               sb.append(charge.getItemStack().getType().toString()).append(",");
               sb.append(charge.getItemStack().getDurability());
            }

            if (charge.getMoney() != null) {
               sb.append(charge.getMoney()).append(",");
               sb.append("money").append(",");
               sb.append(ess.getSettings().getCurrencySymbol());
            }

            if (charge.getExperience() != null) {
               sb.append(charge.getExperience()).append(",");
               sb.append("exp").append(",");
               sb.append("\"\"");
            }
         }

         sb.append(",\"");
         if (receiver != null) {
            sb.append(receiver);
         }

         sb.append("\",");
         if (pay == null) {
            sb.append("\"\",\"\",\"\"");
         } else {
            if (pay.getItemStack() != null) {
               sb.append(pay.getItemStack().getAmount()).append(",");
               sb.append(pay.getItemStack().getType().toString()).append(",");
               sb.append(pay.getItemStack().getDurability());
            }

            if (pay.getMoney() != null) {
               sb.append(pay.getMoney()).append(",");
               sb.append("money").append(",");
               sb.append(ess.getSettings().getCurrencySymbol());
            }

            if (pay.getExperience() != null) {
               sb.append(pay.getExperience()).append(",");
               sb.append("exp").append(",");
               sb.append("\"\"");
            }
         }

         if (loc == null) {
            sb.append(",\"\",\"\",\"\",\"\"");
         } else {
            sb.append(",\"");
            sb.append(loc.getWorld().getName()).append("\",");
            sb.append(loc.getBlockX()).append(",");
            sb.append(loc.getBlockY()).append(",");
            sb.append(loc.getBlockZ()).append(",");
         }

         sb.append("\n");

         try {
            fw.write(sb.toString());
            fw.flush();
         } catch (IOException ex) {
            Logger.getLogger("Minecraft").log(Level.SEVERE, (String)null, ex);
         }

      }
   }

   public static void closeLog() {
      if (fw != null) {
         try {
            fw.close();
         } catch (IOException ex) {
            Logger.getLogger("Minecraft").log(Level.SEVERE, (String)null, ex);
         }

         fw = null;
      }

   }

   public static enum TradeType {
      MONEY,
      EXP,
      ITEM;

      private TradeType() {
      }
   }

   public static enum OverflowType {
      ABORT,
      DROP,
      RETURN;

      private OverflowType() {
      }
   }
}
