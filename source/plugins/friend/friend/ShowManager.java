package friend;

import infos.Infos;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import lib.IconMenu;
import lib.InputManager;
import lib.Lib;
import lib.IconMenu.Session.Result;
import lib.config.ReloadConfigEvent;
import lib.hashList.HashList;
import lib.hashList.HashListImpl;
import lib.util.Util;
import lib.util.UtilConfig;
import lib.util.UtilFormat;
import lib.util.UtilIconMenu;
import lib.util.UtilItems;
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
   private Main main;
   private String pn;
   private String per_friend_admin;
   private String per_friend_use;
   private String per_friend_info_other;
   private int interval;
   private int checkFrom;
   private String delName;
   private List delLore;
   private String buyLimitName;
   private List buyLimitLore;
   private int confirmTimeLimit;
   private HashMap itemHash;
   private HashMap mainMenuHash;

   public ShowManager(Main main) {
      super();
      this.main = main;
      this.pn = main.getPn();
      this.loadConfig(UtilConfig.getConfig(this.pn));
      main.getPm().registerEvents(this, main);
      UtilSpeed.register(this.pn, "show");
      this.loadData();
   }

   @EventHandler(
      priority = EventPriority.LOW
   )
   public void onReloadConfig(ReloadConfigEvent e) {
      if (e.getCallPlugin().equals(this.pn)) {
         this.loadConfig(e.getConfig());
      }

   }

   public void addFriend(String name, String tar) {
      ItemStack is = this.getItem(tar);
      if (!this.itemHash.containsKey(name)) {
         this.itemHash.put(name, new HashListImpl());
      }

      HashList<ItemStack> list = (HashList)this.itemHash.get(name);
      list.add(is);
   }

   public void delFriend(String name, String tar) {
      HashList<ItemStack> list = (HashList)this.itemHash.get(name);

      for(ItemStack is : list) {
         if (this.getName(is).equals(tar)) {
            list.remove(is);
            if (list.isEmpty()) {
               this.itemHash.remove(name);
            }

            return;
         }
      }

   }

   public boolean showMainMenu(Player p, String tar, int page) {
      if (!UtilPer.checkPer(p, this.per_friend_use)) {
         return false;
      } else {
         tar = Util.getRealName(p, tar);
         if (tar == null) {
            return false;
         } else if (!p.getName().equals(tar) && !UtilPer.checkPer(p, this.per_friend_info_other)) {
            return false;
         } else if (!UtilSpeed.check(p, this.pn, "show", this.interval)) {
            return false;
         } else {
            HashList<ItemStack> list = (HashList)this.itemHash.get(tar);
            if (list == null) {
               list = new HashListImpl();
               this.itemHash.put(tar, list);
            }

            int maxPage = list.getMaxPage(45);
            if (list.isEmpty()) {
               maxPage = 1;
            }

            if (page >= 1 && page <= maxPage) {
               String name = UtilFormat.format(this.pn, "show1", new Object[]{tar});
               IconMenu.OptionClickEventHandler handler = new HandlerShowMainMenu(p, tar, page);
               IconMenu.Info info = UtilIconMenu.register(name, 45, true, handler);
               int index = 0;
               if (!list.isEmpty()) {
                  for(ItemStack is : list.getPage(page, 45)) {
                     info.setItem(index, is);
                     ++index;
                  }
               }

               Inventory inv = Bukkit.getServer().createInventory((InventoryHolder)null, 9, "");

               for(ShowInfo showInfo : this.mainMenuHash.values()) {
                  if ((!showInfo.getName().equals("add") || p.getName().equals(tar)) && (!showInfo.getName().equals("pre") || page > 1) && (!showInfo.getName().equals("next") || page < maxPage) && (!showInfo.getName().equals("buyLimit") || p.getName().equals(tar))) {
                     inv.setItem(showInfo.getPos(), UtilItems.getItem(this.pn, showInfo.getType()));
                  }
               }

               UtilIconMenu.open(p, info, (String)null, inv);
               return true;
            } else {
               p.sendMessage(UtilFormat.format(this.pn, "show2", new Object[]{maxPage}));
               return false;
            }
         }
      }
   }

   private void add(HashMap hash, YamlConfiguration config, String s, String name) {
      int pos = config.getInt("item." + s + "." + name);
      String type = s + "_" + name;
      ShowInfo showInfo = new ShowInfo(name, pos, type);
      hash.put(pos, showInfo);
   }

   private ItemStack getItem(String tar) {
      ItemStack result = UtilItems.getItem(this.pn, "show").clone();
      ItemMeta im = result.getItemMeta();
      im.setDisplayName(im.getDisplayName().replace("{0}", tar));
      result.setItemMeta(im);
      return result;
   }

   private String getName(ItemStack is) {
      try {
         return is.getItemMeta().getDisplayName().substring(this.checkFrom);
      } catch (Exception var3) {
         return "";
      }
   }

   private void loadConfig(YamlConfiguration config) {
      this.per_friend_admin = config.getString("per_friend_admin");
      this.per_friend_use = config.getString("per_friend_use");
      this.per_friend_info_other = config.getString("per_friend_info_other");
      this.interval = config.getInt("item.interval");
      this.checkFrom = config.getInt("item.checkFrom");
      this.confirmTimeLimit = config.getInt("item.confirmTimeLimit");
      this.delName = Util.convert(config.getString("item.delName"));
      this.delLore = new ArrayList();

      for(String s : config.getStringList("item.delLore")) {
         this.delLore.add(Util.convert(s));
      }

      this.buyLimitName = Util.convert(config.getString("item.buyLimitName"));
      this.buyLimitLore = new ArrayList();

      for(String s : config.getStringList("item.buyLimitLore")) {
         this.buyLimitLore.add(Util.convert(s));
      }

      String ss = "main";
      this.mainMenuHash = new HashMap();

      for(String s : ((MemorySection)config.get("item." + ss)).getValues(false).keySet()) {
         this.add(this.mainMenuHash, config, ss, s);
      }

   }

   private void loadData() {
      this.itemHash = new HashMap();
      HashMap<String, Friend> hash = this.main.getFriendManager().getAllFriends();

      for(String name : hash.keySet()) {
         Friend f = (Friend)hash.get(name);
         HashList<ItemStack> list = new HashListImpl();
         this.itemHash.put(f.getName(), list);

         for(String tar : f.getFriendList()) {
            list.add(this.getItem(tar));
         }
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

   private class InputAdd implements InputManager.InputHandler {
      private Player p;

      public InputAdd(Player p) {
         super();
         this.p = p;
      }

      public boolean onInput(String s) {
         return ShowManager.this.main.getFriendManager().add(this.p, s);
      }
   }

   private class SessionDelete implements IconMenu.Session {
      private Player p;
      private String tar;

      public SessionDelete(Player p, String tar) {
         super();
         this.p = p;
         this.tar = tar;
      }

      public void onSelect(IconMenu.Session.Result select) {
         if (select.equals(Result.YES)) {
            ShowManager.this.main.getFriendManager().remove(this.p, this.tar);
         }

      }
   }

   private class SessionBuyLimit implements IconMenu.Session {
      private Player p;

      public SessionBuyLimit(Player p) {
         super();
         this.p = p;
      }

      public void onSelect(IconMenu.Session.Result select) {
         if (select.equals(Result.YES)) {
            ShowManager.this.main.getFriendManager().buyLimit(this.p);
         }

      }
   }

   private class HandlerShowMainMenu implements IconMenu.OptionClickEventHandler {
      private Player p;
      private String tar;
      private int page;

      public HandlerShowMainMenu(Player p, String tar, int page) {
         super();
         this.p = p;
         this.tar = tar;
         this.page = page;
      }

      public void onOptionClick(IconMenu.OptionClickEvent event) {
         if (event.getPos() < 45) {
            if (!this.p.getName().equals(this.tar) && !UtilPer.hasPer(this.p, ShowManager.this.per_friend_admin)) {
               this.p.sendMessage(UtilFormat.format(ShowManager.this.pn, "fail", new Object[]{ShowManager.this.get(155)}));
            } else {
               event.setWillClose(true);
               String tarName = ShowManager.this.getName(event.getInfo().getInv(this.p).getItem(event.getPos()));
               IconMenu.Session session = ShowManager.this.new SessionDelete(this.p, tarName);
               UtilIconMenu.openSession(this.p, ShowManager.this.delName.replace("{0}", tarName), ShowManager.this.delLore, session, ShowManager.this.confirmTimeLimit);
            }
         } else {
            ShowInfo showInfo = (ShowInfo)ShowManager.this.mainMenuHash.get(event.getPos() - 45);
            if (showInfo != null) {
               String name = showInfo.getName();
               if (name.equals("back")) {
                  if (Infos.getShowManager().showMainMenu(this.p, this.tar)) {
                     event.setWillClose(true);
                  }
               } else if (name.equals("add")) {
                  if (this.p.getName().equals(this.tar)) {
                     InputAdd inputAdd = ShowManager.this.new InputAdd(this.p);
                     String tip = UtilFormat.format(ShowManager.this.pn, "tip", new Object[]{ShowManager.this.get(160)});
                     if (Lib.getInputManager().input(this.p, inputAdd, tip)) {
                        event.setWillClose(true);
                     }
                  }
               } else if (name.equals("pre")) {
                  if (ShowManager.this.showMainMenu(this.p, this.tar, this.page - 1)) {
                     event.setWillClose(true);
                  }
               } else if (name.equals("next")) {
                  if (ShowManager.this.showMainMenu(this.p, this.tar, this.page + 1)) {
                     event.setWillClose(true);
                  }
               } else if (name.equals("buyLimit") && this.p.getName().equals(this.tar)) {
                  IconMenu.Session session = ShowManager.this.new SessionBuyLimit(this.p);
                  UtilIconMenu.openSession(this.p, ShowManager.this.buyLimitName, ShowManager.this.buyLimitLore, session, ShowManager.this.confirmTimeLimit);
               }
            }

         }
      }
   }
}
