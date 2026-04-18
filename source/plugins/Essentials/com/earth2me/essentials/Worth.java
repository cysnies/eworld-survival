package com.earth2me.essentials;

import com.earth2me.essentials.commands.NotEnoughArgumentsException;
import java.io.File;
import java.math.BigDecimal;
import java.util.Locale;
import java.util.logging.Logger;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

public class Worth implements IConf {
   private static final Logger logger = Logger.getLogger("Minecraft");
   private final EssentialsConf config;

   public Worth(File dataFolder) {
      super();
      this.config = new EssentialsConf(new File(dataFolder, "worth.yml"));
      this.config.setTemplateName("/worth.yml");
      this.config.load();
   }

   public BigDecimal getPrice(ItemStack itemStack) {
      String itemname = itemStack.getType().toString().toLowerCase(Locale.ENGLISH).replace("_", "");
      BigDecimal result = this.config.getBigDecimal("worth." + itemname + "." + itemStack.getDurability(), BigDecimal.ONE.negate());
      if (result.signum() < 0) {
         result = this.config.getBigDecimal("worth." + itemname + ".0", BigDecimal.ONE.negate());
      }

      if (result.signum() < 0) {
         result = this.config.getBigDecimal("worth." + itemname, BigDecimal.ONE.negate());
      }

      if (result.signum() < 0) {
         result = this.config.getBigDecimal("worth." + itemStack.getTypeId() + "." + itemStack.getDurability(), BigDecimal.ONE.negate());
      }

      if (result.signum() < 0) {
         result = this.config.getBigDecimal("worth." + itemStack.getTypeId() + ".0", BigDecimal.ONE.negate());
      }

      if (result.signum() < 0) {
         result = this.config.getBigDecimal("worth." + itemStack.getTypeId(), BigDecimal.ONE.negate());
      }

      if (result.signum() < 0) {
         result = this.config.getBigDecimal("worth-" + itemStack.getTypeId(), BigDecimal.ONE.negate());
      }

      return result.signum() < 0 ? null : result;
   }

   public int getAmount(IEssentials ess, User user, ItemStack is, String[] args, boolean isBulkSell) throws Exception {
      if (is != null && is.getType() != Material.AIR) {
         int id = is.getTypeId();
         int amount = 0;
         if (args.length > 1) {
            try {
               amount = Integer.parseInt(args[1].replaceAll("[^0-9]", ""));
            } catch (NumberFormatException ex) {
               throw new NotEnoughArgumentsException(ex);
            }

            if (args[1].startsWith("-")) {
               amount = -amount;
            }
         }

         boolean stack = args.length > 1 && args[1].endsWith("s");
         boolean requireStack = ess.getSettings().isTradeInStacks(id);
         if (requireStack && !stack) {
            throw new Exception(I18n._("itemMustBeStacked"));
         } else {
            int max = 0;

            for(ItemStack s : user.getInventory().getContents()) {
               if (s != null && s.isSimilar(is)) {
                  max += s.getAmount();
               }
            }

            if (stack) {
               amount *= is.getType().getMaxStackSize();
            }

            if (amount < 1) {
               amount += max;
            }

            if (requireStack) {
               amount -= amount % is.getType().getMaxStackSize();
            }

            if (amount <= max && amount >= 1) {
               return amount;
            } else if (!isBulkSell) {
               user.sendMessage(I18n._("itemNotEnough2"));
               user.sendMessage(I18n._("itemNotEnough3"));
               throw new Exception(I18n._("itemNotEnough1"));
            } else {
               return amount;
            }
         }
      } else {
         throw new Exception(I18n._("itemSellAir"));
      }
   }

   public void setPrice(ItemStack itemStack, double price) {
      if (itemStack.getType().getData() == null) {
         this.config.setProperty("worth." + itemStack.getType().toString().toLowerCase(Locale.ENGLISH).replace("_", ""), (Object)price);
      } else {
         this.config.setProperty("worth." + itemStack.getType().toString().toLowerCase(Locale.ENGLISH).replace("_", "") + "." + itemStack.getDurability(), (Object)price);
      }

      this.config.removeProperty("worth-" + itemStack.getTypeId());
      this.config.save();
   }

   public void reloadConfig() {
      this.config.load();
   }
}
