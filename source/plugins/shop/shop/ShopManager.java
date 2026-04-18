package shop;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import lib.config.ReloadConfigEvent;
import lib.hashList.HashList;
import lib.hashList.HashListImpl;
import lib.time.TimeEvent;
import lib.util.Util;
import lib.util.UtilConfig;
import lib.util.UtilEco;
import lib.util.UtilFormat;
import lib.util.UtilItems;
import lib.util.UtilNames;
import lib.util.UtilPer;
import lib.util.UtilRewards;
import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import ticket.Ticket;

public class ShopManager implements Listener {
   private static final int pageSize = 36;
   private static final ItemMeta IM = (new ItemStack(1)).getItemMeta();
   private Ticket ticket = (Ticket)Bukkit.getPluginManager().getPlugin("ticket");
   private String pn;
   private Dao dao;
   private String per_shop_admin;
   private String per_shop_use;
   private String per_shop_create;
   private String per_shop_vip;
   private int rateTicket;
   private int check;
   private long last;
   private int min;
   private int tip;
   private int base;
   private int rate;
   private int baseVip;
   private int rateVip;
   private HashList shopList;
   private HashMap shopHash;

   public ShopManager(Main main) {
      super();
      this.pn = main.getPn();
      this.dao = main.getDao();
      this.loadData(main.getDao());
      this.loadConfig(UtilConfig.getConfig(this.pn));
      main.getPm().registerEvents(this, main);
   }

   @EventHandler(
      priority = EventPriority.LOW
   )
   public void onReloadConfig(ReloadConfigEvent e) {
      if (e.getCallPlugin().equals(this.pn)) {
         this.loadConfig(e.getConfig());
      }

   }

   @EventHandler(
      priority = EventPriority.LOWEST
   )
   public void onTime(TimeEvent e) {
      if (TimeEvent.getTime() % (long)this.check == 0L) {
         this.checkDeadLine();
      }

   }

   public List getShop(int page) {
      return (List)(this.shopList.isEmpty() ? new ArrayList() : this.shopList.getPage(page, 36));
   }

   public void sell(Player p, int price, boolean force) {
      if (UtilPer.checkPer(p, this.per_shop_create)) {
         ItemStack is = p.getItemInHand();
         if (is != null && is.getTypeId() != 0) {
            if (price < 1) {
               p.sendMessage(UtilFormat.format(this.pn, "fail", new Object[]{this.get(90)}));
            } else if (price < this.min) {
               p.sendMessage(UtilFormat.format(this.pn, "tip6", new Object[]{this.min}));
            } else {
               String vip;
               int base;
               int rate;
               if (UtilPer.hasPer(p, this.per_shop_vip)) {
                  vip = "";
                  base = this.baseVip;
                  rate = this.rateVip;
               } else {
                  vip = "§m";
                  base = this.base;
                  rate = this.rate;
               }

               int extra = price * rate / 1000;
               int cost = base + extra;
               if (!force && price >= this.tip) {
                  p.sendMessage(UtilFormat.format(this.pn, "tip7", new Object[]{base, extra, rate, vip}));
               } else {
                  if (UtilEco.get(p.getName()) < (double)cost) {
                     p.sendMessage(UtilFormat.format(this.pn, "tip4", new Object[]{base, extra, rate, vip}));
                  } else {
                     UtilEco.del(p.getName(), (double)cost);
                     long now = System.currentTimeMillis();
                     Shop shop = new Shop((String)null, p.getName(), price, now, this.last);
                     this.dao.addOrUpdateShop(shop);
                     ItemStack sellItem = this.getSellItem(is, shop);
                     p.setItemInHand((ItemStack)null);
                     String data = UtilItems.saveItem(sellItem);
                     if (data.length() > 20000) {
                        data = data.substring(0, 20000);
                     }

                     shop.setS(data);
                     this.dao.addOrUpdateShop(shop);
                     this.shopList.add(shop);
                     this.shopHash.put(shop.getId(), shop);
                     p.sendMessage(UtilFormat.format(this.pn, "tip5", new Object[]{base, extra, rate, vip}));
                     p.updateInventory();
                  }

               }
            }
         } else {
            p.sendMessage(UtilFormat.format(this.pn, "fail", new Object[]{this.get(85)}));
         }
      }
   }

   public void goldBuy(Player p, ItemStack is) {
      if (UtilPer.checkPer(p, this.per_shop_use)) {
         long id = this.getId(is);
         if (id != -1L) {
            Shop shop = (Shop)this.shopHash.get(id);
            if (shop != null) {
               if (p.getName().equals(shop.getOwner())) {
                  p.sendMessage(UtilFormat.format(this.pn, "fail", new Object[]{this.get(70)}));
                  return;
               }

               if (UtilEco.get(p.getName()) < (double)shop.getPrice()) {
                  p.sendMessage(UtilFormat.format(this.pn, "lackGold", new Object[]{shop.getPrice()}));
                  return;
               }

               if (!UtilEco.del(p.getName(), (double)shop.getPrice())) {
                  return;
               }

               p.sendMessage(UtilFormat.format(this.pn, "delGold", new Object[]{shop.getPrice()}));
               if (!UtilEco.add(shop.getOwner(), (double)shop.getPrice())) {
                  return;
               }

               Util.sendMsg(shop.getOwner(), UtilFormat.format(this.pn, "addGold", new Object[]{p.getName(), UtilNames.getItemName(shop.getIs()), shop.getPrice()}));
               this.shopList.remove(shop);
               this.shopHash.remove(shop.getId());
               this.dao.removeShop(shop);
               ItemStack item = this.getRealItem(shop.getIs());
               if (item == null) {
                  return;
               }

               HashMap<Integer, ItemStack> itemsHash = new HashMap();
               itemsHash.put(0, item);
               UtilRewards.addRewards(this.pn, (String)null, p.getName(), 0, 0, 0, this.get(75), itemsHash, true);
               p.sendMessage(UtilFormat.format(this.pn, "success", new Object[]{this.get(80)}));
            } else {
               p.sendMessage(UtilFormat.format(this.pn, "fail", new Object[]{this.get(55)}));
            }
         }

      }
   }

   public void ticketBuy(Player p, ItemStack is) {
      if (UtilPer.checkPer(p, this.per_shop_use)) {
         long id = this.getId(is);
         if (id != -1L) {
            Shop shop = (Shop)this.shopHash.get(id);
            if (shop != null) {
               if (p.getName().equals(shop.getOwner())) {
                  p.sendMessage(UtilFormat.format(this.pn, "fail", new Object[]{this.get(70)}));
                  return;
               }

               if (!shop.isTicket()) {
                  p.sendMessage(UtilFormat.format(this.pn, "fail", new Object[]{this.get(150)}));
                  return;
               }

               int price = this.getPriceTicket(shop);
               if (price == -1) {
                  return;
               }

               if (Ticket.getTicket(p.getName()) < price) {
                  p.sendMessage(UtilFormat.format(this.pn, "lackTicket", new Object[]{price}));
                  return;
               }

               if (!this.ticket.del(Bukkit.getConsoleSender(), p.getName(), price, this.pn, this.get(130))) {
                  return;
               }

               if (!this.ticket.add(Bukkit.getConsoleSender(), shop.getOwner(), price, this.pn, this.get(135))) {
                  return;
               }

               Util.sendMsg(shop.getOwner(), UtilFormat.format(this.pn, "addTicket", new Object[]{p.getName(), UtilNames.getItemName(shop.getIs()), price}));
               this.shopList.remove(shop);
               this.shopHash.remove(shop.getId());
               this.dao.removeShop(shop);
               ItemStack item = this.getRealItem(shop.getIs());
               if (item == null) {
                  return;
               }

               HashMap<Integer, ItemStack> itemsHash = new HashMap();
               itemsHash.put(0, item);
               UtilRewards.addRewards(this.pn, (String)null, p.getName(), 0, 0, 0, this.get(75), itemsHash, true);
               p.sendMessage(UtilFormat.format(this.pn, "success", new Object[]{this.get(80)}));
            } else {
               p.sendMessage(UtilFormat.format(this.pn, "fail", new Object[]{this.get(55)}));
            }
         }

      }
   }

   public void getBack(Player p, ItemStack is) {
      long id = this.getId(is);
      if (id != -1L) {
         Shop shop = (Shop)this.shopHash.get(id);
         if (shop != null) {
            if (!UtilPer.hasPer(p, this.per_shop_admin) && !p.getName().equals(shop.getOwner())) {
               p.sendMessage(UtilFormat.format(this.pn, "fail", new Object[]{this.get(50)}));
               return;
            }

            this.shopList.remove(shop);
            this.shopHash.remove(shop.getId());
            this.dao.removeShop(shop);
            this.getBack(shop);
            p.sendMessage(this.get(60));
         } else {
            p.sendMessage(UtilFormat.format(this.pn, "fail", new Object[]{this.get(55)}));
         }
      }

   }

   public int getMaxPage() {
      return this.shopList.isEmpty() ? 1 : this.shopList.getMaxPage(36);
   }

   public int getPrice(ItemStack is) {
      int price = -1;
      long id = this.getId(is);
      if (id != -1L) {
         Shop shop = (Shop)this.shopHash.get(id);
         if (shop != null) {
            price = shop.getPrice();
         }
      }

      return price;
   }

   public int getPriceTicket(ItemStack is) {
      int price = this.getPrice(is);
      if (price == -1) {
         return -1;
      } else {
         int result = price / this.rateTicket;
         if (price % this.rateTicket != 0) {
            ++result;
         }

         return result;
      }
   }

   public int getPriceTicket(Shop shop) {
      int price = shop.getPrice();
      if (price == -1) {
         return -1;
      } else {
         int result = price / this.rateTicket;
         if (price % this.rateTicket != 0) {
            ++result;
         }

         return result;
      }
   }

   public void useTicket(Player p, ItemStack is, boolean ticket) {
      long id = this.getId(is);
      if (id != -1L) {
         Shop shop = (Shop)this.shopHash.get(id);
         if (shop != null) {
            if (!UtilPer.hasPer(p, this.per_shop_admin) && !p.getName().equals(shop.getOwner())) {
               p.sendMessage(UtilFormat.format(this.pn, "fail", new Object[]{this.get(140)}));
               return;
            }

            if (!(shop.isTicket() ^ ticket)) {
               p.sendMessage(UtilFormat.format(this.pn, "fail", new Object[]{this.get(145)}));
               return;
            }

            shop.setTicket(ticket);
            ItemStack result = this.getRealItem(shop.getIs());
            if (result == null) {
               return;
            }

            result = this.getSellItem(result, shop);
            shop.setS(UtilItems.saveItem(result));
            this.dao.addOrUpdateShop(shop);
            if (ticket) {
               p.sendMessage(UtilFormat.format(this.pn, "set1", new Object[]{UtilNames.getItemName(result)}));
            } else {
               p.sendMessage(UtilFormat.format(this.pn, "set2", new Object[]{UtilNames.getItemName(result)}));
            }
         } else {
            p.sendMessage(UtilFormat.format(this.pn, "fail", new Object[]{this.get(55)}));
         }
      }

   }

   public int getRateTicket() {
      return this.rateTicket;
   }

   private long getId(ItemStack is) {
      try {
         List<String> lore = is.getItemMeta().getLore();
         return Long.parseLong(((String)lore.get(lore.size() - 1)).split(" ")[1]);
      } catch (Exception var3) {
         return -1L;
      }
   }

   private void checkDeadLine() {
      long now = System.currentTimeMillis();
      Iterator<Shop> it = this.shopList.iterator();

      while(it.hasNext()) {
         Shop shop = (Shop)it.next();
         if (shop.getStart() + shop.getLast() < now) {
            it.remove();
            this.shopHash.remove(shop.getId());
            this.dao.removeShop(shop);
            if (!this.getBack(shop)) {
               return;
            }

            Util.sendMsg(shop.getOwner(), UtilFormat.format(this.pn, "tipDeadLine", new Object[]{UtilNames.getItemName(shop.getIs())}));
         }
      }

   }

   private boolean getBack(Shop shop) {
      ItemStack is = this.getRealItem(shop.getIs());
      if (is == null) {
         return false;
      } else {
         HashMap<Integer, ItemStack> itemsHash = new HashMap();
         itemsHash.put(0, is);
         UtilRewards.addRewards(this.pn, (String)null, shop.getOwner(), 0, 0, 0, this.get(45), itemsHash, true);
         return true;
      }
   }

   private ItemStack getRealItem(ItemStack is) {
      if (is == null) {
         return null;
      } else {
         ItemStack result = is.clone();

         try {
            ItemMeta im = is.getItemMeta();
            List<String> lore = im.getLore();

            for(int i = 0; i < 5; ++i) {
               lore.remove(lore.size() - 1);
            }

            im.setLore(lore);
            if (UtilItems.isItemMetaEmpty(im)) {
               result.setItemMeta((ItemMeta)null);
            } else {
               result.setItemMeta(im);
            }
         } catch (Exception var6) {
         }

         return result;
      }
   }

   private ItemStack getSellItem(ItemStack is, Shop shop) {
      ItemStack result = is.clone();
      ItemMeta im = is.getItemMeta();
      if (im == null) {
         im = IM.clone();
      }

      List<String> lore = im.getLore();
      if (lore == null) {
         lore = new ArrayList();
      }

      lore.add(this.get(95));
      if (shop.isTicket()) {
         lore.add(UtilFormat.format(this.pn, "price", new Object[]{shop.getPrice()}) + this.get(155));
      } else {
         lore.add(UtilFormat.format(this.pn, "price", new Object[]{shop.getPrice()}));
      }

      lore.add(UtilFormat.format(this.pn, "owner", new Object[]{shop.getOwner()}));
      lore.add(UtilFormat.format(this.pn, "deadline", new Object[]{Util.getDateTime(new Date(shop.getStart()), 0, 0, (int)(shop.getLast() / 60000L))}));
      lore.add(UtilFormat.format(this.pn, "id", new Object[]{shop.getId()}));
      im.setLore(lore);
      result.setItemMeta(im);
      return result;
   }

   private void loadData(Dao dao) {
      this.shopList = new HashListImpl();
      this.shopHash = new HashMap();

      for(Shop shop : dao.getAllShops()) {
         this.shopList.add(shop);
         this.shopHash.put(shop.getId(), shop);
      }

   }

   private void loadConfig(FileConfiguration config) {
      this.per_shop_admin = config.getString("per_shop_admin");
      this.per_shop_use = config.getString("per_shop_use");
      this.per_shop_create = config.getString("per_shop_create");
      this.per_shop_vip = config.getString("per_shop_vip");
      this.rateTicket = config.getInt("rateTicket");
      this.check = config.getInt("check");
      this.last = (long)(config.getInt("last") * 60 * 60 * 1000);
      this.min = config.getInt("min");
      this.tip = config.getInt("tip");
      this.base = config.getInt("tax.base");
      this.rate = config.getInt("tax.rate");
      this.baseVip = config.getInt("tax.baseVip");
      this.rateVip = config.getInt("tax.rateVip");
   }

   private String get(int id) {
      return UtilFormat.format(this.pn, id);
   }
}
