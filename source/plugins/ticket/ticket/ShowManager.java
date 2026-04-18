package ticket;

import infos.Infos;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import lib.IconMenu;
import lib.InputManager;
import lib.Lib;
import lib.IconMenu.Session.Result;
import lib.config.ReloadConfigEvent;
import lib.time.TimeEvent;
import lib.util.UtilConfig;
import lib.util.UtilEco;
import lib.util.UtilFormat;
import lib.util.UtilIconMenu;
import lib.util.UtilItems;
import lib.util.UtilRewards;
import lib.util.UtilSpeed;
import org.bukkit.Bukkit;
import org.bukkit.configuration.InvalidConfigurationException;
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
   private Ticket ticket;
   private String pn;
   private int fixYear;
   private int interval;
   private int confirmTimeLimit;
   private int rate;
   private String goldPath;
   private int goldAdd;
   private int goldInterval;
   private int goldUpdate;
   private int goldAmount;
   private int goldRate;
   private int currentDay;
   private int leftGold;
   private HashMap mainMenuHash;

   public ShowManager(Ticket ticket) {
      super();
      this.ticket = ticket;
      this.pn = ticket.getPn();
      this.goldPath = ticket.getPluginPath() + File.separator + this.pn + File.separator + "gold.yml";
      this.loadGold();
      this.loadConfig(UtilConfig.getConfig(this.pn));
      ticket.getPm().registerEvents(this, ticket);
      UtilSpeed.register(this.pn, "show");
   }

   @EventHandler(
      priority = EventPriority.LOW
   )
   public void onReloadConfig(ReloadConfigEvent e) {
      if (e.getCallPlugin().equals(this.pn)) {
         this.loadGold();
         this.loadConfig(e.getConfig());
      }

   }

   @EventHandler
   public void onTime(TimeEvent e) {
      if (TimeEvent.getTime() % (long)this.goldInterval == 0L) {
         int nowDay = (int)(System.currentTimeMillis() / (long)this.goldUpdate);
         if (nowDay != this.currentDay) {
            this.currentDay = nowDay;
            int add = this.getMulti() * this.goldAmount;
            if (Bukkit.getOnlinePlayers().length >= this.goldAdd) {
               this.leftGold += add;
            } else {
               this.leftGold = add;
            }

            this.saveGold();
         }
      }

   }

   public boolean showMainMenu(Player p) {
      if (!UtilSpeed.check(p, this.pn, "show", this.interval)) {
         return false;
      } else {
         String name = this.get(400);
         IconMenu.OptionClickEventHandler handler = new HandlerShowMainMenu(p);
         IconMenu.Info info = UtilIconMenu.register(name, 45, true, handler);

         for(ShowInfo showInfo : this.mainMenuHash.values()) {
            String name1 = showInfo.getName();
            ItemStack is;
            if (name1.equals("info")) {
               is = UtilItems.getItem(this.pn, "main_info").clone();
               ItemMeta im = is.getItemMeta();
               TicketUser tu = Ticket.checkInit(p.getName());
               int ticket = tu.getTicket();
               im.setDisplayName(im.getDisplayName().replace("{0}", "" + ticket));
               is.setItemMeta(im);
            } else if (name1.equals("stone")) {
               is = UtilItems.getItem(this.pn, "main_stone").clone();
               ItemMeta im = is.getItemMeta();
               List<String> lore = im.getLore();
               lore.set(1, ((String)lore.get(1)).replace("{0}", String.valueOf(this.rate)));
               im.setLore(lore);
               is.setItemMeta(im);
            } else if (name1.equals("buyGold")) {
               is = UtilItems.getItem(this.pn, "main_buyGold").clone();
               ItemMeta im = is.getItemMeta();
               List<String> lore = im.getLore();
               lore.set(0, ((String)lore.get(0)).replace("{0}", String.valueOf(this.goldRate)));
               int hour = this.goldUpdate / 3600000;
               lore.set(1, ((String)lore.get(1)).replace("{0}", String.valueOf(hour)).replace("{1}", String.valueOf(this.goldAmount)));
               lore.set(2, ((String)lore.get(2)).replace("{0}", String.valueOf(this.goldAdd)));
               lore.set(3, ((String)lore.get(3)).replace("{0}", String.valueOf(this.leftGold)));
               im.setLore(lore);
               is.setItemMeta(im);
            } else {
               is = UtilItems.getItem(this.pn, showInfo.getType());
            }

            info.setItem(showInfo.getPos(), is);
         }

         Inventory inv = Bukkit.getServer().createInventory((InventoryHolder)null, 9, "");
         inv.setItem(4, UtilItems.getItem(this.pn, "main_back"));
         UtilIconMenu.open(p, info, (String)null, inv);
         return true;
      }
   }

   private int getMulti() {
      Calendar c = Calendar.getInstance();
      c.add(1, this.fixYear);
      int day = c.get(7);
      return day != 7 && day != 1 ? 1 : 2;
   }

   private void buyGold(Player p, int amount) {
      if (amount <= 0) {
         p.sendMessage(UtilFormat.format(this.pn, "fail", new Object[]{this.get(515)}));
      } else if (Ticket.getTicket(p.getName()) <= 0) {
         p.sendMessage(UtilFormat.format(this.pn, "fail", new Object[]{this.get(510)}));
      } else if (this.leftGold < this.goldRate) {
         p.sendMessage(UtilFormat.format(this.pn, "fail", new Object[]{this.get(520)}));
      } else {
         int has = Ticket.getTicket(p.getName());
         amount = Math.min(has, amount);
         int get = Math.min(amount * this.goldRate, this.leftGold);
         int pay = get / this.goldRate;
         get = pay * this.goldRate;
         if (pay > 0) {
            if (this.ticket.del(Bukkit.getConsoleSender(), p.getName(), pay, this.pn, this.get(525))) {
               UtilEco.add(p.getName(), (double)get);
               this.leftGold -= get;
               this.saveGold();
               p.sendMessage(UtilFormat.format(this.pn, "buyGold", new Object[]{pay, get}));
            }
         }
      }
   }

   private void loadGold() {
      YamlConfiguration config = new YamlConfiguration();

      try {
         config.load(this.goldPath);
         this.currentDay = config.getInt("day");
         this.leftGold = config.getInt("left");
      } catch (FileNotFoundException e) {
         e.printStackTrace();
      } catch (IOException e) {
         e.printStackTrace();
      } catch (InvalidConfigurationException e) {
         e.printStackTrace();
      }

   }

   private void saveGold() {
      YamlConfiguration config = new YamlConfiguration();

      try {
         config.set("day", this.currentDay);
         config.set("left", this.leftGold);
         config.save(this.goldPath);
      } catch (FileNotFoundException e) {
         e.printStackTrace();
      } catch (IOException e) {
         e.printStackTrace();
      }

   }

   private void makeStone(Player p, int amount) {
      int ticket = Ticket.getTicket(p.getName());
      if (amount < 1) {
         p.sendMessage(UtilFormat.format(this.pn, "fail", new Object[]{this.get(485)}));
      } else if (amount > ticket) {
         p.sendMessage(UtilFormat.format(this.pn, "fail", new Object[]{this.get(490)}));
      } else {
         int has = (int)UtilEco.get(p.getName());
         if (has < this.rate) {
            p.sendMessage(UtilFormat.format(this.pn, "rateErr", new Object[]{this.rate}));
         } else {
            UtilEco.del(p.getName(), (double)this.rate);
            this.ticket.del(Bukkit.getConsoleSender(), p.getName(), amount, this.pn, this.get(495));
            ItemStack stone = UtilItems.getItem(this.pn, "ticketStone");
            ItemMeta im = stone.getItemMeta();
            List<String> lore = im.getLore();
            lore.set(1, ((String)lore.get(1)).replace("<amount>", String.valueOf(amount)));
            lore.set(2, ((String)lore.get(2)).replace("<amount>", String.valueOf(amount)));
            im.setLore(lore);
            stone.setItemMeta(im);
            HashMap<Integer, ItemStack> itemsHash = new HashMap();
            itemsHash.put(0, stone);
            String tip = this.get(480);
            UtilRewards.addRewards(this.pn, (String)null, p.getName(), 0, 0, 0, tip, itemsHash, true);
            p.sendMessage(UtilFormat.format(this.pn, "makeSuccess", new Object[]{amount}));
         }
      }
   }

   private void loadConfig(YamlConfiguration config) {
      this.fixYear = config.getInt("fixYear");
      this.interval = config.getInt("item.interval");
      this.confirmTimeLimit = config.getInt("item.confirmTimeLimit");
      this.rate = config.getInt("item.rate");
      this.goldAdd = config.getInt("goldAdd");
      this.goldInterval = config.getInt("goldInterval");
      this.goldUpdate = config.getInt("goldUpdate");
      this.goldAmount = config.getInt("goldAmount");
      this.goldRate = config.getInt("goldRate");
      String ss = "main";
      this.mainMenuHash = new HashMap();

      for(String s : ((MemorySection)config.get("item." + ss)).getValues(false).keySet()) {
         this.add(this.mainMenuHash, config, ss, s);
      }

   }

   private void add(HashMap hash, YamlConfiguration config, String s, String name) {
      int pos = config.getInt("item." + s + "." + name);
      String type = s + "_" + name;
      ShowInfo showInfo = new ShowInfo(name, pos, type);
      hash.put(pos, showInfo);
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

   private class InputInfo implements InputManager.InputHandler {
      private Player p;

      public InputInfo(Player p) {
         super();
         this.p = p;
      }

      public boolean onInput(String s) {
         Ticket.getCode().info(this.p, s);
         return true;
      }
   }

   private class InputUse implements InputManager.InputHandler {
      private Player p;

      public InputUse(Player p) {
         super();
         this.p = p;
      }

      public boolean onInput(String s) {
         Ticket.getCode().use(this.p, s);
         return true;
      }
   }

   private class InputStone implements InputManager.InputHandler {
      private Player p;

      public InputStone(Player p) {
         super();
         this.p = p;
      }

      public boolean onInput(String s) {
         try {
            ShowManager.this.makeStone(this.p, Integer.parseInt(s));
         } catch (NumberFormatException var3) {
            this.p.sendMessage(UtilFormat.format(ShowManager.this.pn, "fail", new Object[]{ShowManager.this.get(100)}));
         }

         return true;
      }
   }

   private class InputBuyGold implements InputManager.InputHandler {
      private Player p;

      public InputBuyGold(Player p) {
         super();
         this.p = p;
      }

      public boolean onInput(String s) {
         try {
            ShowManager.this.buyGold(this.p, Integer.parseInt(s));
         } catch (NumberFormatException var3) {
            this.p.sendMessage(UtilFormat.format(ShowManager.this.pn, "fail", new Object[]{ShowManager.this.get(100)}));
         }

         return true;
      }
   }

   private class SessionBuy implements IconMenu.Session {
      private Player p;
      private int id;

      public SessionBuy(Player p, int id) {
         super();
         this.p = p;
         this.id = id;
      }

      public void onSelect(IconMenu.Session.Result select) {
         if (select.equals(Result.YES)) {
            ShowManager.this.ticket.buy(this.p, this.id);
         }

      }
   }

   private class SessionBig implements IconMenu.Session {
      private Player p;

      public SessionBig(Player p) {
         super();
         this.p = p;
      }

      public void onSelect(IconMenu.Session.Result select) {
         if (select.equals(Result.YES)) {
            ShowManager.this.ticket.buyBig(this.p);
         }

      }
   }

   private class HandlerShowMainMenu implements IconMenu.OptionClickEventHandler {
      private Player p;

      public HandlerShowMainMenu(Player p) {
         super();
         this.p = p;
      }

      public void onOptionClick(IconMenu.OptionClickEvent event) {
         int pos = event.getPos();
         if (pos - 45 == 4) {
            if (Infos.getShowManager().showMainMenu(this.p, this.p.getName())) {
               event.setWillClose(true);
            }
         } else {
            ShowInfo showInfo = (ShowInfo)ShowManager.this.mainMenuHash.get(pos);
            if (showInfo != null) {
               String name = showInfo.getName();
               if (name.equals("stone")) {
                  InputStone inputStone = ShowManager.this.new InputStone(this.p);
                  int ticket = Ticket.getTicket(this.p.getName());
                  String tip = UtilFormat.format(ShowManager.this.pn, "inputAmount", new Object[]{ticket});
                  if (Lib.getInputManager().input(this.p, inputStone, tip)) {
                     event.setWillClose(true);
                  }
               } else if (name.equals("buy1")) {
                  event.setWillClose(true);
                  this.p.sendMessage(ShowManager.this.get(475));
               } else if (name.equals("buy2")) {
                  event.setWillClose(true);
                  this.p.sendMessage(ShowManager.this.get(440));
               } else if (name.equals("codeInfo")) {
                  InputInfo inputInfo = ShowManager.this.new InputInfo(this.p);
                  String tip = ShowManager.this.get(405);
                  if (Lib.getInputManager().input(this.p, inputInfo, tip)) {
                     event.setWillClose(true);
                  }
               } else if (name.equals("codeUse")) {
                  InputUse inputUse = ShowManager.this.new InputUse(this.p);
                  String tip = ShowManager.this.get(410);
                  if (Lib.getInputManager().input(this.p, inputUse, tip)) {
                     event.setWillClose(true);
                  }
               } else if (name.equals("vip1")) {
                  event.setWillClose(true);
                  IconMenu.Session session = ShowManager.this.new SessionBuy(this.p, 1);
                  UtilIconMenu.openSession(this.p, ShowManager.this.get(415), (List)null, session, ShowManager.this.confirmTimeLimit);
               } else if (name.equals("vip2")) {
                  event.setWillClose(true);
                  IconMenu.Session session = ShowManager.this.new SessionBuy(this.p, 2);
                  UtilIconMenu.openSession(this.p, ShowManager.this.get(416), (List)null, session, ShowManager.this.confirmTimeLimit);
               } else if (name.equals("vip3")) {
                  event.setWillClose(true);
                  IconMenu.Session session = ShowManager.this.new SessionBuy(this.p, 3);
                  UtilIconMenu.openSession(this.p, ShowManager.this.get(417), (List)null, session, ShowManager.this.confirmTimeLimit);
               } else if (name.equals("vip4")) {
                  event.setWillClose(true);
                  IconMenu.Session session = ShowManager.this.new SessionBuy(this.p, 4);
                  UtilIconMenu.openSession(this.p, ShowManager.this.get(418), (List)null, session, ShowManager.this.confirmTimeLimit);
               } else if (name.equals("vip5")) {
                  event.setWillClose(true);
                  IconMenu.Session session = ShowManager.this.new SessionBuy(this.p, 5);
                  UtilIconMenu.openSession(this.p, ShowManager.this.get(419), (List)null, session, ShowManager.this.confirmTimeLimit);
               } else if (name.equals("free")) {
                  event.setWillClose(true);
                  IconMenu.Session session = ShowManager.this.new SessionBuy(this.p, 6);
                  UtilIconMenu.openSession(this.p, ShowManager.this.get(445), (List)null, session, ShowManager.this.confirmTimeLimit);
               } else if (name.equals("big")) {
                  event.setWillClose(true);
                  IconMenu.Session session = ShowManager.this.new SessionBig(this.p);
                  UtilIconMenu.openSession(this.p, ShowManager.this.get(455), (List)null, session, ShowManager.this.confirmTimeLimit);
               } else if (name.equals("vip11")) {
                  event.setWillClose(true);
                  IconMenu.Session session = ShowManager.this.new SessionBuy(this.p, 11);
                  UtilIconMenu.openSession(this.p, ShowManager.this.get(501), (List)null, session, ShowManager.this.confirmTimeLimit);
               } else if (name.equals("vip12")) {
                  event.setWillClose(true);
                  IconMenu.Session session = ShowManager.this.new SessionBuy(this.p, 12);
                  UtilIconMenu.openSession(this.p, ShowManager.this.get(502), (List)null, session, ShowManager.this.confirmTimeLimit);
               } else if (name.equals("vip13")) {
                  event.setWillClose(true);
                  IconMenu.Session session = ShowManager.this.new SessionBuy(this.p, 13);
                  UtilIconMenu.openSession(this.p, ShowManager.this.get(503), (List)null, session, ShowManager.this.confirmTimeLimit);
               } else if (name.equals("vip14")) {
                  event.setWillClose(true);
                  IconMenu.Session session = ShowManager.this.new SessionBuy(this.p, 14);
                  UtilIconMenu.openSession(this.p, ShowManager.this.get(504), (List)null, session, ShowManager.this.confirmTimeLimit);
               } else if (name.equals("vip15")) {
                  event.setWillClose(true);
                  IconMenu.Session session = ShowManager.this.new SessionBuy(this.p, 15);
                  UtilIconMenu.openSession(this.p, ShowManager.this.get(505), (List)null, session, ShowManager.this.confirmTimeLimit);
               } else if (name.equals("buyGold")) {
                  InputBuyGold input = ShowManager.this.new InputBuyGold(this.p);
                  String tip = UtilFormat.format(ShowManager.this.pn, "buyGoldTip", new Object[]{ShowManager.this.goldRate});
                  if (Lib.getInputManager().input(this.p, input, tip)) {
                     event.setWillClose(true);
                  }
               }
            }
         }

      }
   }
}
