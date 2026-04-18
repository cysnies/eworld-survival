package need;

import java.util.HashMap;
import lib.config.ReloadConfigEvent;
import lib.util.Util;
import lib.util.UtilConfig;
import lib.util.UtilEco;
import lib.util.UtilFormat;
import lib.util.UtilItems;
import lib.util.UtilNames;
import lib.util.UtilPer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

public class Sell implements Listener {
   private static final String LIB = "lib";
   private String pn;
   private String per_need_sell;
   private boolean enableSell;
   private HashMap infoHash;

   public Sell(Main main) {
      super();
      this.pn = main.getPn();
      this.loadConfig(UtilConfig.getConfig(this.pn));
      main.getPm().registerEvents(this, main);
   }

   public void onCommand(CommandSender sender, Command cmd, String label, String[] args) {
      try {
         int length = args.length;
         Player p = null;
         if (sender instanceof Player) {
            p = (Player)sender;
         }

         if (p == null) {
            sender.sendMessage(UtilFormat.format("lib", "fail", new Object[]{this.get(40)}));
            return;
         }

         if (!this.enableSell) {
            sender.sendMessage(UtilFormat.format("lib", "fail", new Object[]{this.get(525)}));
            return;
         }

         if (length != 1 || !args[0].equalsIgnoreCase("?")) {
            if (length == 0) {
               if (!UtilPer.checkPer(p, this.per_need_sell)) {
                  return;
               }

               ItemStack is = p.getItemInHand();
               if (is != null && is.getTypeId() != 0) {
                  int id = is.getTypeId();
                  double price = (double)0.0F;
                  if (this.infoHash.containsKey(id)) {
                     price = Util.getDouble((Double)this.infoHash.get(id), 2);
                  }

                  p.sendMessage(UtilFormat.format(this.pn, "itemInfo", new Object[]{id, UtilNames.getItemName(id, is.getDurability()), price}));
                  return;
               }

               sender.sendMessage(UtilFormat.format("lib", "fail", new Object[]{this.get(520)}));
               return;
            }

            if (length == 1) {
               int amount = Integer.parseInt(args[0]);
               this.sell(p, amount);
               return;
            }
         }

         sender.sendMessage(UtilFormat.format("lib", "cmdHelpHeader", new Object[]{this.get(530)}));
         if (UtilPer.hasPer(p, this.per_need_sell)) {
            sender.sendMessage(UtilFormat.format("lib", "cmdHelpItem", new Object[]{this.get(535), this.get(540)}));
         }
      } catch (NumberFormatException var11) {
         sender.sendMessage(UtilFormat.format("lib", "fail", new Object[]{this.get(415)}));
      }

   }

   @EventHandler(
      priority = EventPriority.LOW
   )
   public void onReloadConfig(ReloadConfigEvent e) {
      if (e.getCallPlugin().equals(this.pn)) {
         this.loadConfig(e.getConfig());
      }

   }

   private void loadConfig(FileConfiguration config) {
      this.per_need_sell = config.getString("per_need_sell");
      this.enableSell = config.getBoolean("enableSell");
      this.infoHash = new HashMap();

      for(String s : config.getStringList("info")) {
         int id = Integer.parseInt(s.split(" ")[0]);
         double price = Double.parseDouble(s.split(" ")[1]);
         this.infoHash.put(id, price);
      }

   }

   private void sell(Player p, int amount) {
      if (UtilPer.checkPer(p, this.per_need_sell)) {
         Inventory inv = p.getInventory();
         ItemStack is = p.getItemInHand();
         if (is != null && is.getTypeId() != 0) {
            int id = is.getTypeId();
            int has = UtilItems.getAmount(inv, id);
            if (amount > has) {
               amount = has;
            }

            if (amount <= 0) {
               p.sendMessage(UtilFormat.format("lib", "fail", new Object[]{this.get(515)}));
            } else if (!this.infoHash.containsKey(id)) {
               p.sendMessage(UtilFormat.format("lib", "fail", new Object[]{this.get(500)}));
            } else {
               double price = (Double)this.infoHash.get(id);
               if (price <= (double)0.0F) {
                  p.sendMessage(UtilFormat.format("lib", "fail", new Object[]{this.get(505)}));
               } else {
                  double get = Util.getDouble((double)amount * price, 2);
                  if (!UtilEco.add(p.getName(), get)) {
                     p.sendMessage(UtilFormat.format("lib", "fail", new Object[]{this.get(510)}));
                  } else {
                     UtilItems.removeItem(inv, id, amount, true);
                     p.updateInventory();
                     p.sendMessage(UtilFormat.format(this.pn, "sellTip", new Object[]{amount, UtilNames.getItemName(id, is.getDurability()), id, get}));
                  }
               }
            }
         } else {
            p.sendMessage(UtilFormat.format("lib", "fail", new Object[]{this.get(520)}));
         }
      }
   }

   private String get(int id) {
      return UtilFormat.format(this.pn, id);
   }
}
