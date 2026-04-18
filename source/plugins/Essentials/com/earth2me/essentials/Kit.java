package com.earth2me.essentials;

import com.earth2me.essentials.commands.NoChargeException;
import com.earth2me.essentials.craftbukkit.InventoryWorkaround;
import com.earth2me.essentials.textreader.IText;
import com.earth2me.essentials.textreader.KeywordReplacer;
import com.earth2me.essentials.textreader.SimpleTextInput;
import com.earth2me.essentials.utils.DateUtil;
import com.earth2me.essentials.utils.NumberUtil;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.logging.Level;
import org.bukkit.Material;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.inventory.ItemStack;

public class Kit {
   public Kit() {
      super();
   }

   public static String listKits(net.ess3.api.IEssentials ess, User user) throws Exception {
      try {
         ConfigurationSection kits = ess.getSettings().getKits();
         StringBuilder list = new StringBuilder();

         for(String kitItem : kits.getKeys(false)) {
            if (user == null) {
               list.append(" ").append(I18n.capitalCase(kitItem));
            } else if (user.isAuthorized("essentials.kits." + kitItem.toLowerCase(Locale.ENGLISH))) {
               String cost = "";
               String name = I18n.capitalCase(kitItem);
               BigDecimal costPrice = (new Trade("kit-" + kitItem.toLowerCase(Locale.ENGLISH), ess)).getCommandCost(user);
               if (costPrice.signum() > 0) {
                  cost = I18n._("kitCost", NumberUtil.displayCurrency(costPrice, ess));
               }

               Map<String, Object> kit = ess.getSettings().getKit(kitItem);
               if (getNextUse(user, kitItem, kit) != 0L) {
                  name = I18n._("kitDelay", name);
               }

               list.append(" ").append(name).append(cost);
            }
         }

         return list.toString().trim();
      } catch (Exception ex) {
         throw new Exception(I18n._("kitError"), ex);
      }
   }

   public static void checkTime(User user, String kitName, Map els) throws Exception {
      Calendar time = new GregorianCalendar();
      long nextUse = getNextUse(user, kitName, els);
      if (nextUse == 0L) {
         user.setKitTimestamp(kitName, time.getTimeInMillis());
      } else if (nextUse < 0L) {
         user.sendMessage(I18n._("kitOnce"));
         throw new NoChargeException();
      } else {
         user.sendMessage(I18n._("kitTimed", DateUtil.formatDateDiff(nextUse)));
         throw new NoChargeException();
      }
   }

   public static long getNextUse(User user, String kitName, Map els) throws Exception {
      if (user.isAuthorized("essentials.kit.exemptdelay")) {
         return 0L;
      } else {
         Calendar time = new GregorianCalendar();
         double delay = (double)0.0F;

         try {
            delay = els.containsKey("delay") ? ((Number)els.get("delay")).doubleValue() : (double)0.0F;
         } catch (Exception var9) {
            throw new Exception(I18n._("kitError2"));
         }

         long lastTime = user.getKitTimestamp(kitName);
         Calendar delayTime = new GregorianCalendar();
         delayTime.setTimeInMillis(lastTime);
         delayTime.add(13, (int)delay);
         delayTime.add(14, (int)(delay * (double)1000.0F % (double)1000.0F));
         if (lastTime != 0L && lastTime <= time.getTimeInMillis()) {
            if (delay < (double)0.0F) {
               return -1L;
            } else {
               return delayTime.before(time) ? 0L : delayTime.getTimeInMillis();
            }
         } else {
            return 0L;
         }
      }
   }

   public static List getItems(net.ess3.api.IEssentials ess, User user, String kitName, Map kit) throws Exception {
      if (kit == null) {
         throw new Exception(I18n._("kitNotFound"));
      } else {
         try {
            List<String> itemList = new ArrayList();
            Object kitItems = kit.get("items");
            if (kitItems instanceof List) {
               for(Object item : (List)kitItems) {
                  if (!(item instanceof String)) {
                     throw new Exception("Invalid kit item: " + item.toString());
                  }

                  itemList.add(item.toString());
               }

               return itemList;
            } else {
               throw new Exception("Invalid item list");
            }
         } catch (Exception e) {
            ess.getLogger().log(Level.WARNING, "Error parsing kit " + kitName + ": " + e.getMessage());
            throw new Exception(I18n._("kitError2"), e);
         }
      }
   }

   public static void expandItems(net.ess3.api.IEssentials ess, User user, List items) throws Exception {
      try {
         IText input = new SimpleTextInput(items);
         IText output = new KeywordReplacer(input, user.getBase(), ess);
         boolean spew = false;
         boolean allowUnsafe = ess.getSettings().allowUnsafeEnchantments();

         for(String kitItem : output.getLines()) {
            if (kitItem.startsWith(ess.getSettings().getCurrencySymbol())) {
               BigDecimal value = new BigDecimal(kitItem.substring(ess.getSettings().getCurrencySymbol().length()).trim());
               Trade t = new Trade(value, ess);
               t.pay(user, Trade.OverflowType.DROP);
            } else {
               String[] parts = kitItem.split(" +");
               ItemStack parseStack = ess.getItemDb().get(parts[0], parts.length > 1 ? Integer.parseInt(parts[1]) : 1);
               if (parseStack.getType() != Material.AIR) {
                  MetaItemStack metaStack = new MetaItemStack(parseStack);
                  if (parts.length > 2) {
                     metaStack.parseStringMeta((CommandSender)null, allowUnsafe, parts, 2, ess);
                  }

                  boolean allowOversizedStacks = user.isAuthorized("essentials.oversizedstacks");
                  Map<Integer, ItemStack> overfilled;
                  if (allowOversizedStacks) {
                     overfilled = InventoryWorkaround.addOversizedItems(user.getInventory(), ess.getSettings().getOversizedStackSize(), metaStack.getItemStack());
                  } else {
                     overfilled = InventoryWorkaround.addItems(user.getInventory(), metaStack.getItemStack());
                  }

                  for(ItemStack itemStack : overfilled.values()) {
                     int spillAmount = itemStack.getAmount();
                     if (!allowOversizedStacks) {
                        itemStack.setAmount(spillAmount < itemStack.getMaxStackSize() ? spillAmount : itemStack.getMaxStackSize());
                     }

                     while(spillAmount > 0) {
                        user.getWorld().dropItemNaturally(user.getLocation(), itemStack);
                        spillAmount -= itemStack.getAmount();
                     }

                     spew = true;
                  }
               }
            }
         }

         user.updateInventory();
         if (spew) {
            user.sendMessage(I18n._("kitInvFull"));
         }

      } catch (Exception e) {
         user.updateInventory();
         ess.getLogger().log(Level.WARNING, e.getMessage());
         throw new Exception(I18n._("kitError2"), e);
      }
   }
}
