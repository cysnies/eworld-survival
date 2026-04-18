package shop;

import infos.Infos;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import lib.IconMenu;
import lib.IconMenu.Session.Result;
import lib.config.ReloadConfigEvent;
import lib.util.UtilConfig;
import lib.util.UtilFormat;
import lib.util.UtilIconMenu;
import lib.util.UtilItems;
import lib.util.UtilNames;
import lib.util.UtilPer;
import lib.util.UtilSpeed;
import org.bukkit.Bukkit;
import org.bukkit.configuration.MemorySection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

public class ShowManager implements Listener {
   private static final String SPEED_SHOW = "show";
   private static final int SIZE = 45;
   private String pn;
   private String per_shop_use;
   private int confirmTimeLimit;
   private int interval;
   private HashMap mainMenuHash;

   public ShowManager(Main main) {
      super();
      this.pn = main.getPn();
      this.loadConfig(UtilConfig.getConfig(this.pn));
      main.getPm().registerEvents(this, main);
      UtilSpeed.register(this.pn, "show");
   }

   @EventHandler(
      priority = EventPriority.LOW
   )
   public void onReloadConfig(ReloadConfigEvent e) {
      if (e.getCallPlugin().equals(this.pn)) {
         this.loadConfig(e.getConfig());
      }

   }

   public boolean showMainMenu(Player p, int page) {
      if (!UtilPer.checkPer(p, this.per_shop_use)) {
         return false;
      } else if (!UtilSpeed.check(p, this.pn, "show", this.interval)) {
         return false;
      } else {
         int maxPage = Main.getShopManager().getMaxPage();
         if (page >= 1 && page <= maxPage) {
            String name = this.get(65);
            IconMenu.OptionClickEventHandler handler = new HandlerShowMainMenu(p, page);
            IconMenu.Info info = UtilIconMenu.register(name, 45, true, handler);

            for(ShowInfo showInfo : this.mainMenuHash.values()) {
               if (showInfo.getName().equals("info")) {
                  ItemStack is = UtilItems.getItem(this.pn, showInfo.getType()).clone();
                  ItemMeta im = is.getItemMeta();
                  List<String> lore = im.getLore();
                  lore.set(lore.size() - 1, ((String)lore.get(lore.size() - 1)).replace("{0}", String.valueOf(Main.getShopManager().getRateTicket())));
                  im.setLore(lore);
                  is.setItemMeta(im);
                  info.setItem(showInfo.getPos(), is);
               } else {
                  info.setItem(showInfo.getPos(), UtilItems.getItem(this.pn, showInfo.getType()));
               }
            }

            int index = 9;

            for(Shop shop : Main.getShopManager().getShop(page)) {
               info.setItem(index, shop.getShowIs());
               ++index;
            }

            Inventory inv = Bukkit.getServer().createInventory((InventoryHolder)null, 9, "");
            ItemStack first = UtilItems.getItem(this.pn, "main_first");
            ItemMeta im = first.getItemMeta();
            List<String> lore = im.getLore();
            lore.set(0, ((String)lore.get(0)).replace("{0}", String.valueOf(page)));
            lore.set(1, ((String)lore.get(1)).replace("{1}", String.valueOf(maxPage)));
            im.setLore(lore);
            first.setItemMeta(im);
            inv.setItem(0, first);
            if (page - 10 > 0) {
               inv.setItem(1, UtilItems.getItem(this.pn, "main_pre10"));
            }

            if (page - 5 > 0) {
               inv.setItem(2, UtilItems.getItem(this.pn, "main_pre5"));
            }

            if (page - 1 > 0) {
               inv.setItem(3, UtilItems.getItem(this.pn, "main_pre1"));
            }

            inv.setItem(4, UtilItems.getItem(this.pn, "main_back"));
            if (page + 1 <= maxPage) {
               inv.setItem(5, UtilItems.getItem(this.pn, "main_next1"));
            }

            if (page + 5 <= maxPage) {
               inv.setItem(6, UtilItems.getItem(this.pn, "main_next5"));
            }

            if (page + 10 <= maxPage) {
               inv.setItem(7, UtilItems.getItem(this.pn, "main_next10"));
            }

            inv.setItem(8, UtilItems.getItem(this.pn, "main_end"));
            UtilIconMenu.open(p, info, (String)null, inv);
            return true;
         } else {
            p.sendMessage(UtilFormat.format(this.pn, "pageErr", new Object[]{maxPage}));
            return false;
         }
      }
   }

   private void add(HashMap hash, YamlConfiguration config, String s, String name) {
      int pos = config.getInt("item." + s + "." + name);
      String type = s + "_" + name;
      ShowInfo showInfo = new ShowInfo(name, pos, type);
      hash.put(pos, showInfo);
   }

   private void loadConfig(YamlConfiguration config) {
      this.per_shop_use = config.getString("per_shop_use");
      this.confirmTimeLimit = config.getInt("item.confirmTimeLimit");
      this.interval = config.getInt("interval");
      String ss = "main";
      this.mainMenuHash = new HashMap();

      for(String s : ((MemorySection)config.get("item." + ss)).getValues(false).keySet()) {
         this.add(this.mainMenuHash, config, ss, s);
      }

   }

   private String get(int id) {
      return UtilFormat.format(this.pn, id);
   }

   public class ShowInfo {
      private String name;
      private int pos;
      private String type;

      public ShowInfo(String name, int pos, String type) {
         super();
         this.name = name;
         this.pos = pos;
         this.type = type;
      }

      public String getName() {
         return this.name;
      }

      public int getPos() {
         return this.pos;
      }

      public String getType() {
         return this.type;
      }
   }

   private class SessionBuy implements IconMenu.Session {
      private Player p;
      private ItemStack is;

      public SessionBuy(Player p, ItemStack is) {
         super();
         this.p = p;
         this.is = is;
      }

      public void onSelect(IconMenu.Session.Result select) {
         if (select.equals(Result.YES)) {
            Main.getShopManager().goldBuy(this.p, this.is);
         }

      }
   }

   private class SessionTicketBuy implements IconMenu.Session {
      private Player p;
      private ItemStack is;

      public SessionTicketBuy(Player p, ItemStack is) {
         super();
         this.p = p;
         this.is = is;
      }

      public void onSelect(IconMenu.Session.Result select) {
         if (select.equals(Result.YES)) {
            Main.getShopManager().ticketBuy(this.p, this.is);
         }

      }
   }

   private class SessionGetback implements IconMenu.Session {
      private Player p;
      private ItemStack is;

      public SessionGetback(Player p, ItemStack is) {
         super();
         this.p = p;
         this.is = is;
      }

      public void onSelect(IconMenu.Session.Result select) {
         if (select.equals(Result.YES)) {
            Main.getShopManager().getBack(this.p, this.is);
         }

      }
   }

   private class SessionUseTicket implements IconMenu.Session {
      private Player p;
      private ItemStack is;

      public SessionUseTicket(Player p, ItemStack is) {
         super();
         this.p = p;
         this.is = is;
      }

      public void onSelect(IconMenu.Session.Result select) {
         if (select.equals(Result.YES)) {
            Main.getShopManager().useTicket(this.p, this.is, true);
         } else if (select.equals(Result.NO)) {
            Main.getShopManager().useTicket(this.p, this.is, false);
         }

      }
   }

   private class HandlerShowMainMenu implements IconMenu.OptionClickEventHandler {
      private Player p;
      private int page;

      public HandlerShowMainMenu(Player p, int page) {
         super();
         this.p = p;
         this.page = page;
      }

      public void onOptionClick(IconMenu.OptionClickEvent event) {
         if (event.getPos() >= 9) {
            switch (event.getPos() - 45) {
               case 0:
                  if (ShowManager.this.showMainMenu(this.p, 1)) {
                     event.setWillClose(true);
                  }

                  return;
               case 1:
                  if (ShowManager.this.showMainMenu(this.p, this.page - 10)) {
                     event.setWillClose(true);
                  }

                  return;
               case 2:
                  if (ShowManager.this.showMainMenu(this.p, this.page - 5)) {
                     event.setWillClose(true);
                  }

                  return;
               case 3:
                  if (ShowManager.this.showMainMenu(this.p, this.page - 1)) {
                     event.setWillClose(true);
                  }

                  return;
               case 4:
                  if (Infos.getShowManager().showMainMenu(this.p, this.p.getName())) {
                     event.setWillClose(true);
                  }

                  return;
               case 5:
                  if (ShowManager.this.showMainMenu(this.p, this.page + 1)) {
                     event.setWillClose(true);
                  }

                  return;
               case 6:
                  if (ShowManager.this.showMainMenu(this.p, this.page + 5)) {
                     event.setWillClose(true);
                  }

                  return;
               case 7:
                  if (ShowManager.this.showMainMenu(this.p, this.page + 10)) {
                     event.setWillClose(true);
                  }

                  return;
               case 8:
                  if (ShowManager.this.showMainMenu(this.p, Main.getShopManager().getMaxPage())) {
                     event.setWillClose(true);
                  }

                  return;
               default:
                  if (event.isLeft()) {
                     event.setWillClose(true);
                     ItemStack is = event.getInfo().getInv(this.p).getItem(event.getPos());
                     if (event.isShift()) {
                        IconMenu.Session session = ShowManager.this.new SessionTicketBuy(this.p, is);
                        List<String> lore = new ArrayList();
                        lore.add(UtilFormat.format(ShowManager.this.pn, "tip8", new Object[]{Main.getShopManager().getPriceTicket(is)}));
                        UtilIconMenu.openSession(this.p, UtilFormat.format(ShowManager.this.pn, "tip1", new Object[]{UtilNames.getItemName(is)}), lore, session, ShowManager.this.confirmTimeLimit);
                     } else {
                        IconMenu.Session session = ShowManager.this.new SessionBuy(this.p, is);
                        List<String> lore = new ArrayList();
                        lore.add(UtilFormat.format(ShowManager.this.pn, "tip2", new Object[]{Main.getShopManager().getPrice(is)}));
                        UtilIconMenu.openSession(this.p, UtilFormat.format(ShowManager.this.pn, "tip1", new Object[]{UtilNames.getItemName(is)}), lore, session, ShowManager.this.confirmTimeLimit);
                     }
                  } else {
                     event.setWillClose(true);
                     ItemStack is = event.getInfo().getInv(this.p).getItem(event.getPos());
                     if (event.isShift()) {
                        IconMenu.Session session = ShowManager.this.new SessionUseTicket(this.p, is);
                        UtilIconMenu.openSession(this.p, UtilFormat.format(ShowManager.this.pn, "tip9", new Object[]{UtilNames.getItemName(is)}), (List)null, session, ShowManager.this.confirmTimeLimit);
                     } else {
                        IconMenu.Session session = ShowManager.this.new SessionGetback(this.p, is);
                        UtilIconMenu.openSession(this.p, UtilFormat.format(ShowManager.this.pn, "tip3", new Object[]{UtilNames.getItemName(is)}), (List)null, session, ShowManager.this.confirmTimeLimit);
                     }
                  }

            }
         }
      }
   }
}
