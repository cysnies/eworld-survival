package com.earth2me.essentials.commands;

import com.earth2me.essentials.I18n;
import com.earth2me.essentials.User;
import com.earth2me.essentials.utils.NumberUtil;
import java.math.BigDecimal;
import java.util.List;
import java.util.Locale;
import org.bukkit.Server;
import org.bukkit.command.CommandSender;
import org.bukkit.inventory.ItemStack;

public class Commandworth extends EssentialsCommand {
   public Commandworth() {
      super("worth");
   }

   public void run(Server server, User user, String commandLabel, String[] args) throws Exception {
      BigDecimal totalWorth = BigDecimal.ZERO;
      String type = "";
      List<ItemStack> is = this.ess.getItemDb().getMatching(user, args);
      int count = 0;
      boolean isBulk = is.size() > 1;

      for(ItemStack stack : is) {
         try {
            if (stack.getAmount() > 0) {
               totalWorth = totalWorth.add(this.itemWorth(user.getBase(), user, stack, args));
               stack = stack.clone();
               ++count;

               for(ItemStack zeroStack : is) {
                  if (zeroStack.isSimilar(stack)) {
                     zeroStack.setAmount(0);
                  }
               }
            }
         } catch (Exception e) {
            if (!isBulk) {
               throw e;
            }
         }
      }

      if (count > 1) {
         if (args.length > 0 && args[0].equalsIgnoreCase("blocks")) {
            user.sendMessage(I18n._("totalSellableBlocks", type, NumberUtil.displayCurrency(totalWorth, this.ess)));
         } else {
            user.sendMessage(I18n._("totalSellableAll", type, NumberUtil.displayCurrency(totalWorth, this.ess)));
         }
      }

   }

   public void run(Server server, CommandSender sender, String commandLabel, String[] args) throws Exception {
      String type = "";
      if (args.length < 1) {
         throw new NotEnoughArgumentsException();
      } else {
         ItemStack stack = this.ess.getItemDb().get(args[0]);
         this.itemWorth(sender, (User)null, stack, args);
      }
   }

   private BigDecimal itemWorth(CommandSender sender, User user, ItemStack is, String[] args) throws Exception {
      int amount = 1;
      if (user == null) {
         if (args.length > 1) {
            try {
               amount = Integer.parseInt(args[1].replaceAll("[^0-9]", ""));
            } catch (NumberFormatException ex) {
               throw new NotEnoughArgumentsException(ex);
            }
         }
      } else {
         amount = this.ess.getWorth().getAmount(this.ess, user, is, args, true);
      }

      BigDecimal worth = this.ess.getWorth().getPrice(is);
      if (worth == null) {
         throw new Exception(I18n._("itemCannotBeSold"));
      } else {
         if (amount < 0) {
            amount = 0;
         }

         BigDecimal result = worth.multiply(BigDecimal.valueOf((long)amount));
         sender.sendMessage(is.getDurability() != 0 ? I18n._("worthMeta", is.getType().toString().toLowerCase(Locale.ENGLISH).replace("_", ""), is.getDurability(), NumberUtil.displayCurrency(result, this.ess), amount, NumberUtil.displayCurrency(worth, this.ess)) : I18n._("worth", is.getType().toString().toLowerCase(Locale.ENGLISH).replace("_", ""), NumberUtil.displayCurrency(result, this.ess), amount, NumberUtil.displayCurrency(worth, this.ess)));
         return result;
      }
   }
}
