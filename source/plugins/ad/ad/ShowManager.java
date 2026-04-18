package ad;

import infos.Infos;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Random;
import lib.IconMenu;
import lib.InputManager;
import lib.Lib;
import lib.IconMenu.Session.Result;
import lib.config.ReloadConfigEvent;
import lib.hashList.ChanceHashList;
import lib.hashList.ChanceHashListImpl;
import lib.util.Util;
import lib.util.UtilConfig;
import lib.util.UtilEco;
import lib.util.UtilFormat;
import lib.util.UtilIconMenu;
import lib.util.UtilItems;
import lib.util.UtilPer;
import lib.util.UtilScoreboard;
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
   private static final long DAY = 86400000L;
   private Random r = new Random();
   private Ad ad;
   private String pn;
   private CheckTimer checkTimer;
   private String per_ad_use;
   private int clearInterval;
   private int cost;
   private int set;
   private int max;
   private int day;
   private int last;
   private int check;
   private int chance;
   private int interval;
   private int confirmTimeLimit;
   private HashMap mainMenuHash;
   private HashMap userHash;
   private ChanceHashList userList;

   public ShowManager(Ad ad) {
      super();
      this.ad = ad;
      this.pn = Ad.getPn();
      this.loadConfig(UtilConfig.getConfig(this.pn));
      ad.getPm().registerEvents(this, ad);
      UtilSpeed.register(this.pn, "show");
      this.loadData();
      this.checkTimer = new CheckTimer((CheckTimer)null);
      Bukkit.getScheduler().scheduleSyncDelayedTask(ad, this.checkTimer, (long)(this.check * 20));
      Bukkit.getScheduler().scheduleSyncRepeatingTask(ad, new Runnable() {
         public void run() {
            ShowManager.this.clear();
         }
      }, (long)(this.clearInterval * 20), (long)(this.clearInterval * 20));
   }

   @EventHandler(
      priority = EventPriority.LOW
   )
   public void onReloadConfig(ReloadConfigEvent e) {
      if (e.getCallPlugin().equals(this.pn)) {
         this.loadConfig(e.getConfig());
      }

   }

   public boolean showMainMenu(Player p) {
      if (!UtilPer.checkPer(p, this.per_ad_use)) {
         return false;
      } else if (!UtilSpeed.check(p, this.pn, "show", this.interval)) {
         return false;
      } else {
         String name = this.get(40);
         IconMenu.OptionClickEventHandler handler = new HandlerShowMainMenu(p);
         IconMenu.Info info = UtilIconMenu.register(name, 45, true, handler);
         AdUser adUser = (AdUser)this.userHash.get(p.getName());

         for(ShowInfo showInfo : this.mainMenuHash.values()) {
            String name1 = showInfo.getName();
            ItemStack is;
            if (name1.equals("info")) {
               is = UtilItems.getItem(this.pn, "main_info").clone();
               ItemMeta im = is.getItemMeta();
               List<String> lore = im.getLore();
               String s;
               if (adUser == null) {
                  s = this.get(60);
               } else {
                  s = adUser.getMsg();
               }

               lore.set(0, ((String)lore.get(0)).replace("{0}", s));
               if (adUser == null) {
                  s = this.get(60);
               } else {
                  s = String.valueOf(adUser.getPrice());
               }

               lore.set(1, ((String)lore.get(1)).replace("{0}", s));
               if (adUser == null) {
                  s = this.get(60);
               } else {
                  s = Util.getDateTime(new Date(adUser.getStart()), this.day, 0, 0);
               }

               lore.set(2, ((String)lore.get(2)).replace("{0}", s));
               im.setLore(lore);
               is.setItemMeta(im);
            } else if (name1.equals("add")) {
               is = UtilItems.getItem(this.pn, "main_add").clone();
               ItemMeta im = is.getItemMeta();
               List<String> lore = im.getLore();
               lore.set(0, ((String)lore.get(0)).replace("{0}", String.valueOf(this.cost)));
               im.setLore(lore);
               is.setItemMeta(im);
            } else if (name1.equals("set")) {
               is = UtilItems.getItem(this.pn, "main_set").clone();
               ItemMeta im = is.getItemMeta();
               List<String> lore = im.getLore();
               lore.set(0, ((String)lore.get(0)).replace("{0}", String.valueOf(this.set)));
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

   private boolean addAd(Player p, String s) {
      s = s.replace("&", "§");
      if (!UtilPer.checkPer(p, this.per_ad_use)) {
         return true;
      } else {
         AdUser adUser = (AdUser)this.userHash.get(p.getName());
         if (adUser != null) {
            p.sendMessage(UtilFormat.format(this.pn, "fail", new Object[]{this.get(45)}));
            return true;
         } else if (UtilEco.get(p.getName()) < (double)this.cost) {
            p.sendMessage(UtilFormat.format(this.pn, "fail", new Object[]{this.get(50)}));
            return true;
         } else if (!s.isEmpty() && s.length() <= 32) {
            UtilEco.del(p.getName(), (double)this.cost);
            p.sendMessage(UtilFormat.format(this.pn, "delGold", new Object[]{this.cost}));
            adUser = new AdUser(p.getName(), s, this.cost, System.currentTimeMillis());
            this.userHash.put(p.getName(), adUser);
            this.userList.add(adUser);
            Ad.getDao().addOrUpdateAdUser(adUser);
            p.sendMessage(UtilFormat.format(this.pn, "success", new Object[]{this.get(65)}));
            p.sendMessage(UtilFormat.format(this.pn, "showAd", new Object[]{s}));
            return true;
         } else {
            p.sendMessage(UtilFormat.format(this.pn, "fail", new Object[]{this.get(55)}));
            return false;
         }
      }
   }

   private boolean setAd(Player p, String s) {
      s = s.replace("&", "§");
      if (!UtilPer.checkPer(p, this.per_ad_use)) {
         return true;
      } else {
         AdUser adUser = (AdUser)this.userHash.get(p.getName());
         if (adUser == null) {
            p.sendMessage(UtilFormat.format(this.pn, "fail", new Object[]{this.get(80)}));
            return true;
         } else if (UtilEco.get(p.getName()) < (double)this.set) {
            p.sendMessage(UtilFormat.format(this.pn, "fail", new Object[]{this.get(50)}));
            return true;
         } else if (!s.isEmpty() && s.length() <= 32) {
            UtilEco.del(p.getName(), (double)this.set);
            p.sendMessage(UtilFormat.format(this.pn, "delGold", new Object[]{this.set}));
            adUser.setMsg(s);
            Ad.getDao().addOrUpdateAdUser(adUser);
            p.sendMessage(UtilFormat.format(this.pn, "success", new Object[]{this.get(85)}));
            p.sendMessage(UtilFormat.format(this.pn, "showAd", new Object[]{s}));
            return true;
         } else {
            p.sendMessage(UtilFormat.format(this.pn, "fail", new Object[]{this.get(55)}));
            return false;
         }
      }
   }

   private boolean upgradeAd(Player p, int amount) {
      if (!UtilPer.checkPer(p, this.per_ad_use)) {
         return true;
      } else {
         AdUser adUser = (AdUser)this.userHash.get(p.getName());
         if (adUser == null) {
            p.sendMessage(UtilFormat.format(this.pn, "fail", new Object[]{this.get(80)}));
            return true;
         } else if (amount < 1) {
            p.sendMessage(UtilFormat.format(this.pn, "fail", new Object[]{this.get(95)}));
            return false;
         } else if (adUser.getPrice() + amount > this.max) {
            p.sendMessage(UtilFormat.format(this.pn, "maxCost", new Object[]{this.max}));
            return false;
         } else {
            UtilEco.del(p.getName(), (double)amount);
            p.sendMessage(UtilFormat.format(this.pn, "delGold", new Object[]{amount}));
            adUser.setPrice(adUser.getPrice() + amount);
            this.userList.setChance(adUser, adUser.getPrice());
            Ad.getDao().addOrUpdateAdUser(adUser);
            p.sendMessage(UtilFormat.format(this.pn, "success", new Object[]{this.get(100)}));
            return true;
         }
      }
   }

   private void removeAd(Player p) {
      if (UtilPer.checkPer(p, this.per_ad_use)) {
         AdUser adUser = (AdUser)this.userHash.get(p.getName());
         if (adUser == null) {
            p.sendMessage(UtilFormat.format(this.pn, "fail", new Object[]{this.get(80)}));
         } else {
            this.userHash.remove(p.getName());
            this.userList.remove(adUser);
            Ad.getDao().removeUser(adUser);
            p.sendMessage(UtilFormat.format(this.pn, "success", new Object[]{this.get(110)}));
         }
      }
   }

   public void removeAd(String tar) {
      AdUser au = (AdUser)this.userHash.remove(tar);
      if (au != null) {
         this.userList.remove(au);
         Ad.getDao().removeUser(au);
      }

   }

   private void check() {
      if (this.r.nextInt(100) < this.chance && !this.userList.isEmpty()) {
         AdUser adUser = (AdUser)this.userList.getRandom();
         UtilScoreboard.show(adUser.getMsg(), adUser.getName(), this.last);
      }

   }

   private void clear() {
      long now = System.currentTimeMillis();
      Iterator<AdUser> it = this.userHash.values().iterator();

      while(it.hasNext()) {
         AdUser adUser = (AdUser)it.next();
         if (now - adUser.getStart() >= 86400000L * (long)this.day) {
            it.remove();
            this.userList.remove(adUser);
            Ad.getDao().removeUser(adUser);
            Player tar = Bukkit.getServer().getPlayerExact(adUser.getName());
            if (tar != null && tar.isOnline()) {
               tar.sendMessage(UtilFormat.format(this.pn, "fail", new Object[]{this.get(115)}));
            }
         }
      }

   }

   private void loadData() {
      this.userHash = new HashMap();
      this.userList = new ChanceHashListImpl();

      for(AdUser adUser : Ad.getDao().getAllAds()) {
         this.userHash.put(adUser.getName(), adUser);
         this.userList.addChance(adUser, adUser.getPrice());
      }

   }

   private void loadConfig(YamlConfiguration config) {
      this.per_ad_use = config.getString("per_ad_use");
      this.clearInterval = config.getInt("clearInterval");
      this.cost = config.getInt("cost");
      this.set = config.getInt("set");
      this.max = config.getInt("max");
      this.day = config.getInt("day");
      this.last = config.getInt("last");
      this.check = config.getInt("check");
      this.chance = config.getInt("chance");
      this.interval = config.getInt("item.interval");
      this.confirmTimeLimit = config.getInt("item.confirmTimeLimit");
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

   private class CheckTimer implements Runnable {
      private CheckTimer() {
         super();
      }

      public void run() {
         Bukkit.getScheduler().scheduleSyncDelayedTask(ShowManager.this.ad, ShowManager.this.checkTimer, (long)(ShowManager.this.check * 20));
         ShowManager.this.check();
      }

      // $FF: synthetic method
      CheckTimer(CheckTimer var2) {
         this();
      }
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

   private class InputAdd implements InputManager.InputHandler {
      private Player p;

      public InputAdd(Player p) {
         super();
         this.p = p;
      }

      public boolean onInput(String s) {
         return ShowManager.this.addAd(this.p, s);
      }
   }

   private class InputSet implements InputManager.InputHandler {
      private Player p;

      public InputSet(Player p) {
         super();
         this.p = p;
      }

      public boolean onInput(String s) {
         return ShowManager.this.setAd(this.p, s);
      }
   }

   private class InputUpgrade implements InputManager.InputHandler {
      private Player p;

      public InputUpgrade(Player p) {
         super();
         this.p = p;
      }

      public boolean onInput(String s) {
         try {
            return ShowManager.this.upgradeAd(this.p, Integer.parseInt(s));
         } catch (NumberFormatException var3) {
            this.p.sendMessage(UtilFormat.format(ShowManager.this.pn, "fail", new Object[]{ShowManager.this.get(35)}));
            return false;
         }
      }
   }

   private class SessionRemove implements IconMenu.Session {
      private Player p;

      public SessionRemove(Player p) {
         super();
         this.p = p;
      }

      public void onSelect(IconMenu.Session.Result select) {
         if (select.equals(Result.YES)) {
            ShowManager.this.removeAd(this.p);
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
               if (name.equals("add")) {
                  InputAdd inputAdd = ShowManager.this.new InputAdd(this.p);
                  if (Lib.getInputManager().input(this.p, inputAdd, ShowManager.this.get(70))) {
                     event.setWillClose(true);
                  }
               } else if (name.equals("set")) {
                  InputSet inputSet = ShowManager.this.new InputSet(this.p);
                  if (Lib.getInputManager().input(this.p, inputSet, ShowManager.this.get(75))) {
                     event.setWillClose(true);
                  }
               } else if (name.equals("upgrade")) {
                  InputUpgrade inputUpgrade = ShowManager.this.new InputUpgrade(this.p);
                  if (Lib.getInputManager().input(this.p, inputUpgrade, ShowManager.this.get(90))) {
                     event.setWillClose(true);
                  }
               } else if (name.equals("remove")) {
                  event.setWillClose(true);
                  IconMenu.Session session = ShowManager.this.new SessionRemove(this.p);
                  UtilIconMenu.openSession(this.p, ShowManager.this.get(105), (List)null, session, ShowManager.this.confirmTimeLimit);
               }
            }
         }

      }
   }
}
