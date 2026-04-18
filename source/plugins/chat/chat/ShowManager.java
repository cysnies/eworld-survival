package chat;

import infos.Infos;
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
   private String pn;
   private int interval;
   private int confirmTimeLimit;
   private int checkFrom;
   private HashMap blackHash;
   private HashMap blackList;

   public ShowManager(Chat chat) {
      super();
      this.pn = chat.getPn();
      this.loadConfig(UtilConfig.getConfig(this.pn));
      chat.getPm().registerEvents(this, chat);
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

   public boolean showBlackList(Player p, String tar, int page) {
      tar = Util.getRealName(p, tar);
      if (tar == null) {
         return false;
      } else if (!p.getName().equals(tar) && !UtilPer.checkPer(p, Chat.getBlackList().getPer_chat_black_info_other())) {
         return false;
      } else if (!UtilSpeed.check(p, this.pn, "show", this.interval)) {
         return false;
      } else {
         HashList<ItemStack> list = (HashList)this.blackList.get(tar);
         int maxPage = 0;
         if (list != null) {
            maxPage = list.getMaxPage(45);
         }

         if (maxPage <= 0) {
            maxPage = 1;
         }

         if (page >= 1 && page <= maxPage) {
            String name = UtilFormat.format(this.pn, "showBlack", new Object[]{tar});
            IconMenu.OptionClickEventHandler handler = new HandlerShowBlack(p, tar, page);
            IconMenu.Info info = UtilIconMenu.register(name, 45, true, handler);
            if (list != null) {
               int index = 0;

               for(ItemStack is : list) {
                  if (index >= 45) {
                     break;
                  }

                  info.setItem(index, is);
                  ++index;
               }
            }

            Inventory inv = Bukkit.getServer().createInventory((InventoryHolder)null, 9, "");

            for(ShowInfo showInfo : this.blackHash.values()) {
               String name1 = showInfo.getName();
               if (name1.equals("add")) {
                  if (!p.getName().equals(tar)) {
                     continue;
                  }
               } else if (name1.equals("pre")) {
                  if (page <= 1) {
                     continue;
                  }
               } else if (name1.equals("next") && page >= maxPage) {
                  continue;
               }

               ItemStack is = UtilItems.getItem(this.pn, showInfo.getType());
               inv.setItem(showInfo.getPos(), is);
            }

            UtilIconMenu.open(p, info, (String)null, inv);
            return true;
         } else {
            p.sendMessage(UtilFormat.format(this.pn, "pageErr", new Object[]{maxPage}));
            return false;
         }
      }
   }

   public boolean showSelectColor(Player p) {
      if (!UtilPer.hasPer(p, Chat.getChatColor().getPer_chat_color())) {
         p.sendMessage(this.get(215));
         return false;
      } else if (!UtilSpeed.check(p, this.pn, "show", this.interval)) {
         return false;
      } else {
         String name = this.get(180);
         IconMenu.OptionClickEventHandler handler = new HandlerSelectColor(p);
         IconMenu.Info info = UtilIconMenu.register(name, 45, true, handler);
         int index = 0;

         for(ItemStack is : Chat.getChatColor().getColorList()) {
            if (index >= 45) {
               break;
            }

            info.setItem(index, is);
            ++index;
         }

         info.setItem(18, Chat.getChatColor().getIsBold(p));
         Inventory inv = Bukkit.getServer().createInventory((InventoryHolder)null, 9, "");
         inv.setItem(4, UtilItems.getItem(this.pn, "color_back"));
         UtilIconMenu.open(p, info, (String)null, inv);
         return true;
      }
   }

   public void addBlack(String name, String tar) {
      HashList<ItemStack> list = (HashList)this.blackList.get(name);
      if (list == null) {
         list = new HashListImpl();
         this.blackList.put(name, list);
      }

      list.add(this.getBlackItem(tar));
   }

   public void delBlack(String name, String tar) {
      HashList<ItemStack> list = (HashList)this.blackList.get(name);
      if (list != null) {
         for(ItemStack is : list) {
            if (this.getName(is).equals(tar)) {
               list.remove(is);
               return;
            }
         }

      }
   }

   private String getName(ItemStack is) {
      try {
         String name = is.getItemMeta().getDisplayName();
         return name.substring(this.checkFrom, name.length());
      } catch (Exception var3) {
         return "";
      }
   }

   private void loadData() {
      this.blackList = new HashMap();
      HashMap<String, BlackUser> hash = Chat.getBlackList().getBlackHash();

      for(String name : hash.keySet()) {
         HashList<ItemStack> list = new HashListImpl();
         this.blackList.put(name, list);

         for(String tar : ((BlackUser)hash.get(name)).getBlackList()) {
            list.add(this.getBlackItem(tar));
         }
      }

   }

   private ItemStack getBlackItem(String tar) {
      ItemStack result = UtilItems.getItem(this.pn, "show").clone();
      ItemMeta im = result.getItemMeta();
      im.setDisplayName(im.getDisplayName().replace("{0}", tar));
      result.setItemMeta(im);
      return result;
   }

   private void loadConfig(YamlConfiguration config) {
      this.interval = config.getInt("item.interval");
      this.confirmTimeLimit = config.getInt("item.confirmTimeLimit");
      this.checkFrom = config.getInt("item.checkFrom");
      String ss = "black";
      this.blackHash = new HashMap();

      for(String s : ((MemorySection)config.get("item." + ss)).getValues(false).keySet()) {
         this.add(this.blackHash, config, ss, s);
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

   private class InputAdd implements InputManager.InputHandler {
      private Player p;

      public InputAdd(Player p) {
         super();
         this.p = p;
      }

      public boolean onInput(String s) {
         return Chat.getBlackList().addBlack(this.p, s);
      }
   }

   private class SessionDel implements IconMenu.Session {
      private Player p;
      private String tar;

      public SessionDel(Player p, String tar) {
         super();
         this.p = p;
         this.tar = tar;
      }

      public void onSelect(IconMenu.Session.Result select) {
         if (select.equals(Result.YES)) {
            Chat.getBlackList().removeBlack(this.p, this.tar);
         }

      }
   }

   private class HandlerShowBlack implements IconMenu.OptionClickEventHandler {
      private Player p;
      private String tar;
      private int page;

      public HandlerShowBlack(Player p, String tar, int page) {
         super();
         this.p = p;
         this.tar = tar;
         this.page = page;
      }

      public void onOptionClick(IconMenu.OptionClickEvent event) {
         int pos = event.getPos();
         if (pos < 45) {
            if (!this.p.getName().equals(this.tar)) {
               return;
            }

            event.setWillClose(true);
            String tar = ShowManager.this.getName(event.getInfo().getInv(this.p).getItem(pos));
            IconMenu.Session session = ShowManager.this.new SessionDel(this.p, tar);
            UtilIconMenu.openSession(this.p, UtilFormat.format(ShowManager.this.pn, "delConfirm", new Object[]{tar}), (List)null, session, ShowManager.this.confirmTimeLimit);
         } else {
            ShowInfo showInfo = (ShowInfo)ShowManager.this.blackHash.get(pos - 45);
            if (showInfo != null) {
               String name = showInfo.getName();
               if (name.equals("add")) {
                  InputAdd inputAdd = ShowManager.this.new InputAdd(this.p);
                  String tip = ShowManager.this.get(175);
                  if (Lib.getInputManager().input(this.p, inputAdd, tip)) {
                     event.setWillClose(true);
                  }
               } else if (name.equals("back")) {
                  if (Infos.getShowManager().showMainMenu(this.p, this.tar)) {
                     event.setWillClose(true);
                  }
               } else if (name.equals("pre")) {
                  if (ShowManager.this.showBlackList(this.p, this.tar, this.page - 1)) {
                     event.setWillClose(true);
                  }
               } else if (name.equals("next") && ShowManager.this.showBlackList(this.p, this.tar, this.page + 1)) {
                  event.setWillClose(true);
               }
            }
         }

      }
   }

   private class HandlerSelectColor implements IconMenu.OptionClickEventHandler {
      private Player p;

      public HandlerSelectColor(Player p) {
         super();
         this.p = p;
      }

      public void onOptionClick(IconMenu.OptionClickEvent event) {
         int pos = event.getPos();
         if (pos == 18) {
            Chat.getChatColor().setBold(this.p);
            event.setWillClose(true);
            Infos.getShowManager().showSetMenu(this.p);
         } else if (pos < 45) {
            String color = Chat.getChatColor().getColor(pos);
            if (Chat.getChatColor().set(this.p, color)) {
               event.setWillClose(true);
               Infos.getShowManager().showSetMenu(this.p);
            }
         } else if (pos - 45 == 4 && Infos.getShowManager().showSetMenu(this.p)) {
            event.setWillClose(true);
         }

      }
   }
}
