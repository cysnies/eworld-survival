package level;

import infos.Infos;
import java.util.HashMap;
import java.util.List;
import lib.IconMenu;
import lib.config.ReloadConfigEvent;
import lib.util.UtilConfig;
import lib.util.UtilFormat;
import lib.util.UtilIconMenu;
import lib.util.UtilItems;
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

public class ShowManager implements Listener {
   private static final String SPEED_SHOW = "show";
   private static final int SIZE = 45;
   private int interval;
   private HashMap mainMenuHash;

   public ShowManager(Main main) {
      super();
      this.loadConfig(UtilConfig.getConfig(Main.getPn()));
      main.getPm().registerEvents(this, main);
      UtilSpeed.register(Main.getPn(), "show");
   }

   @EventHandler(
      priority = EventPriority.LOW
   )
   public void onReloadConfig(ReloadConfigEvent e) {
      if (e.getCallPlugin().equals(Main.getPn())) {
         this.loadConfig(e.getConfig());
      }

   }

   public boolean showHasGet(Player p, int page) {
      if (!UtilSpeed.check(p, Main.getPn(), "show", this.interval)) {
         return false;
      } else {
         int maxPage = Main.getLevelManager().getMaxPage();
         if (page >= 1 && page <= maxPage) {
            int typeId = Main.getLevelManager().getPageType(page);
            String typeName = Main.getLevelManager().getTypeName(typeId);
            String name = UtilFormat.format(Main.getPn(), "hasGet", new Object[]{typeName});
            IconMenu.OptionClickEventHandler handler = new HandlerShowHasGet(p, page);
            IconMenu.Info info = UtilIconMenu.register(name, 45, true, handler);
            int index = 0;

            for(int id : (List)Main.getLevelManager().getTypeListHash().get(typeId)) {
               ItemStack is2 = Main.getLevelManager().getHasItem(p.getName(), id);
               if (is2 != null) {
                  info.setItem(index, is2);
               }

               ++index;
               if (index >= 45) {
                  break;
               }
            }

            Inventory inv = Bukkit.getServer().createInventory((InventoryHolder)null, 9, "");
            inv.setItem(0, UtilItems.getItem(Main.getPn(), "has_tip"));
            inv.setItem(1, UtilItems.getItem(Main.getPn(), "main_level1"));
            inv.setItem(2, UtilItems.getItem(Main.getPn(), "main_level2"));
            inv.setItem(3, UtilItems.getItem(Main.getPn(), "main_level3"));
            inv.setItem(4, UtilItems.getItem(Main.getPn(), "back"));
            inv.setItem(5, UtilItems.getItem(Main.getPn(), "main_level4"));
            inv.setItem(6, UtilItems.getItem(Main.getPn(), "main_level5"));
            inv.setItem(7, UtilItems.getItem(Main.getPn(), "main_level6"));
            UtilIconMenu.open(p, info, (String)null, inv);
            return true;
         } else {
            p.sendMessage(UtilFormat.format(Main.getPn(), "pageErr", new Object[]{maxPage}));
            return false;
         }
      }
   }

   public boolean showSelect(Player p, int page) {
      if (!UtilSpeed.check(p, Main.getPn(), "show", this.interval)) {
         return false;
      } else {
         int maxPage = Main.getLevelManager().getMaxPage();
         if (page >= 1 && page <= maxPage) {
            int typeId = Main.getLevelManager().getPageType(page);
            String typeName = Main.getLevelManager().getTypeName(typeId);
            String name = UtilFormat.format(Main.getPn(), "select", new Object[]{typeName});
            IconMenu.OptionClickEventHandler handler = new HandlerShowSelect(p, page);
            IconMenu.Info info = UtilIconMenu.register(name, 45, true, handler);
            int index = 0;

            for(int id : (List)Main.getLevelManager().getTypeListHash().get(typeId)) {
               ItemStack is = Main.getLevelManager().getSelectItem(p.getName(), id);
               if (is != null) {
                  info.setItem(index, is);
               }

               ++index;
               if (index >= 45) {
                  break;
               }
            }

            Inventory inv = Bukkit.getServer().createInventory((InventoryHolder)null, 9, "");
            inv.setItem(0, UtilItems.getItem(Main.getPn(), "select_tip"));
            inv.setItem(1, UtilItems.getItem(Main.getPn(), "main_level1"));
            inv.setItem(2, UtilItems.getItem(Main.getPn(), "main_level2"));
            inv.setItem(3, UtilItems.getItem(Main.getPn(), "main_level3"));
            inv.setItem(4, UtilItems.getItem(Main.getPn(), "back"));
            inv.setItem(5, UtilItems.getItem(Main.getPn(), "main_level4"));
            inv.setItem(6, UtilItems.getItem(Main.getPn(), "main_level5"));
            inv.setItem(7, UtilItems.getItem(Main.getPn(), "main_level6"));
            UtilIconMenu.open(p, info, (String)null, inv);
            return true;
         } else {
            p.sendMessage(UtilFormat.format(Main.getPn(), "pageErr", new Object[]{maxPage}));
            return false;
         }
      }
   }

   public boolean showHasnotGet(Player p, int page) {
      if (!UtilSpeed.check(p, Main.getPn(), "show", this.interval)) {
         return false;
      } else {
         int maxPage = Main.getLevelManager().getMaxPage();
         if (page >= 1 && page <= maxPage) {
            int typeId = Main.getLevelManager().getPageType(page);
            String typeName = Main.getLevelManager().getTypeName(typeId);
            String name = UtilFormat.format(Main.getPn(), "hasnotGet", new Object[]{typeName});
            IconMenu.OptionClickEventHandler handler = new HandlerShowHasnotGet(p);
            IconMenu.Info info = UtilIconMenu.register(name, 45, true, handler);
            int index = 0;

            for(int id : (List)Main.getLevelManager().getTypeListHash().get(typeId)) {
               ItemStack is2 = Main.getLevelManager().getHasnotItem(p.getName(), id);
               if (is2 != null) {
                  info.setItem(index, is2);
               }

               ++index;
               if (index >= 45) {
                  break;
               }
            }

            Inventory inv = Bukkit.getServer().createInventory((InventoryHolder)null, 9, "");
            inv.setItem(1, UtilItems.getItem(Main.getPn(), "main_level1"));
            inv.setItem(2, UtilItems.getItem(Main.getPn(), "main_level2"));
            inv.setItem(3, UtilItems.getItem(Main.getPn(), "main_level3"));
            inv.setItem(4, UtilItems.getItem(Main.getPn(), "back"));
            inv.setItem(5, UtilItems.getItem(Main.getPn(), "main_level4"));
            inv.setItem(6, UtilItems.getItem(Main.getPn(), "main_level5"));
            inv.setItem(7, UtilItems.getItem(Main.getPn(), "main_level6"));
            UtilIconMenu.open(p, info, (String)null, inv);
            return true;
         } else {
            p.sendMessage(UtilFormat.format(Main.getPn(), "pageErr", new Object[]{maxPage}));
            return false;
         }
      }
   }

   public boolean showMainMenu(Player p) {
      if (!UtilSpeed.check(p, Main.getPn(), "show", this.interval)) {
         return false;
      } else {
         String name = this.get(155);
         IconMenu.OptionClickEventHandler handler = new HandlerShowMainMenu(p);
         IconMenu.Info info = UtilIconMenu.register(name, 45, true, handler);

         for(ShowInfo showInfo : this.mainMenuHash.values()) {
            String name1 = showInfo.getName();
            ItemStack is;
            if (name1.equals("info")) {
               is = Main.getLevelManager().getInfoItem(p.getName());
            } else {
               is = UtilItems.getItem(Main.getPn(), showInfo.getType());
            }

            info.setItem(showInfo.getPos(), is);
         }

         Inventory inv = Bukkit.getServer().createInventory((InventoryHolder)null, 9, "");
         inv.setItem(4, UtilItems.getItem(Main.getPn(), "main_back"));
         UtilIconMenu.open(p, info, (String)null, inv);
         return true;
      }
   }

   private void loadConfig(YamlConfiguration config) {
      this.interval = config.getInt("item.interval");
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
      return UtilFormat.format(Main.getPn(), id);
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

   private class HandlerShowHasGet implements IconMenu.OptionClickEventHandler {
      private Player p;
      private int page;

      public HandlerShowHasGet(Player p, int page) {
         super();
         this.p = p;
         this.page = page;
      }

      public void onOptionClick(IconMenu.OptionClickEvent event) {
         int pos = event.getPos();
         if (pos < 45) {
            int typeId = Main.getLevelManager().getPageType(this.page);
            List<Integer> list = (List)Main.getLevelManager().getTypeListHash().get(typeId);
            if (list != null && pos < list.size()) {
               Main.getLevelManager().toggleEffect(this.p, (Integer)list.get(pos));
            }

            event.setWillClose(true);
            ShowManager.this.showHasGet(this.p, this.page);
         } else {
            switch (pos - 45) {
               case 1:
                  if (ShowManager.this.showHasGet(this.p, 1)) {
                     event.setWillClose(true);
                  }

                  return;
               case 2:
                  if (ShowManager.this.showHasGet(this.p, 2)) {
                     event.setWillClose(true);
                  }

                  return;
               case 3:
                  if (ShowManager.this.showHasGet(this.p, 3)) {
                     event.setWillClose(true);
                  }

                  return;
               case 4:
                  if (ShowManager.this.showMainMenu(this.p)) {
                     event.setWillClose(true);
                  }

                  return;
               case 5:
                  if (ShowManager.this.showHasGet(this.p, 4)) {
                     event.setWillClose(true);
                  }

                  return;
               case 6:
                  if (ShowManager.this.showHasGet(this.p, 5)) {
                     event.setWillClose(true);
                  }

                  return;
               case 7:
                  if (ShowManager.this.showHasGet(this.p, 6)) {
                     event.setWillClose(true);
                  }

                  return;
            }
         }

      }
   }

   private class HandlerShowSelect implements IconMenu.OptionClickEventHandler {
      private Player p;
      private int page;

      public HandlerShowSelect(Player p, int page) {
         super();
         this.p = p;
         this.page = page;
      }

      public void onOptionClick(IconMenu.OptionClickEvent event) {
         int pos = event.getPos();
         if (pos < 45) {
            int typeId = Main.getLevelManager().getPageType(this.page);
            List<Integer> list = (List)Main.getLevelManager().getTypeListHash().get(typeId);
            if (list != null && pos < list.size()) {
               Main.getLevelManager().selectShow(this.p, (Integer)list.get(pos));
            }

            event.setWillClose(true);
            ShowManager.this.showSelect(this.p, this.page);
         } else {
            switch (pos - 45) {
               case 1:
                  if (ShowManager.this.showSelect(this.p, 1)) {
                     event.setWillClose(true);
                  }

                  return;
               case 2:
                  if (ShowManager.this.showSelect(this.p, 2)) {
                     event.setWillClose(true);
                  }

                  return;
               case 3:
                  if (ShowManager.this.showSelect(this.p, 3)) {
                     event.setWillClose(true);
                  }

                  return;
               case 4:
                  if (ShowManager.this.showMainMenu(this.p)) {
                     event.setWillClose(true);
                  }

                  return;
               case 5:
                  if (ShowManager.this.showSelect(this.p, 4)) {
                     event.setWillClose(true);
                  }

                  return;
               case 6:
                  if (ShowManager.this.showSelect(this.p, 5)) {
                     event.setWillClose(true);
                  }

                  return;
               case 7:
                  if (ShowManager.this.showSelect(this.p, 6)) {
                     event.setWillClose(true);
                  }

                  return;
            }
         }

      }
   }

   private class HandlerShowHasnotGet implements IconMenu.OptionClickEventHandler {
      private Player p;

      public HandlerShowHasnotGet(Player p) {
         super();
         this.p = p;
      }

      public void onOptionClick(IconMenu.OptionClickEvent event) {
         int pos = event.getPos();
         switch (pos - 45) {
            case 1:
               if (ShowManager.this.showHasnotGet(this.p, 1)) {
                  event.setWillClose(true);
               }

               return;
            case 2:
               if (ShowManager.this.showHasnotGet(this.p, 2)) {
                  event.setWillClose(true);
               }

               return;
            case 3:
               if (ShowManager.this.showHasnotGet(this.p, 3)) {
                  event.setWillClose(true);
               }

               return;
            case 4:
               if (ShowManager.this.showMainMenu(this.p)) {
                  event.setWillClose(true);
               }

               return;
            case 5:
               if (ShowManager.this.showHasnotGet(this.p, 4)) {
                  event.setWillClose(true);
               }

               return;
            case 6:
               if (ShowManager.this.showHasnotGet(this.p, 5)) {
                  event.setWillClose(true);
               }

               return;
            case 7:
               if (ShowManager.this.showHasnotGet(this.p, 6)) {
                  event.setWillClose(true);
               }

               return;
            default:
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
               if (name.equals("has")) {
                  if (ShowManager.this.showHasGet(this.p, 1)) {
                     event.setWillClose(true);
                  }
               } else if (name.equals("show")) {
                  if (ShowManager.this.showSelect(this.p, 1)) {
                     event.setWillClose(true);
                  }
               } else if (name.equals("hasnot") && ShowManager.this.showHasnotGet(this.p, 1)) {
                  event.setWillClose(true);
               }
            }

         }
      }
   }
}
