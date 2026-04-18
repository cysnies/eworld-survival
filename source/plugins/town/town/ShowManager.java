package town;

import infos.Infos;
import java.util.HashMap;
import java.util.List;
import lib.IconMenu;
import lib.InputManager;
import lib.Lib;
import lib.IconMenu.Session.Result;
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
import org.bukkit.inventory.meta.ItemMeta;

public class ShowManager implements Listener {
   private static final String SPEED_SHOW = "show";
   private static final int SIZE = 45;
   private String pn = Main.getPn();
   private int interval;
   private int confirmTimeLimit;
   private HashMap mainMenuHash;

   public ShowManager(Main main) {
      super();
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

   public boolean showMainMenu(Player p) {
      if (!UtilSpeed.check(p, this.pn, "show", this.interval)) {
         return false;
      } else {
         String name = this.get(100);
         IconMenu.OptionClickEventHandler handler = new HandlerShowMainMenu(p);
         IconMenu.Info info = UtilIconMenu.register(name, 45, true, handler);

         for(ShowInfo showInfo : this.mainMenuHash.values()) {
            String name1 = showInfo.getName();
            ItemStack is;
            if (name1.equals("ask")) {
               is = Main.getTownManager().getAskInfoItem(p);
            } else if (name1.equals("setName")) {
               is = Main.getTownManager().getTownSetNameItem();
            } else if (name1.equals("info")) {
               is = Main.getTownManager().getTownInfoItem(p);
            } else if (name1.equals("del")) {
               is = Main.getTownManager().getDelItem();
            } else if (name1.equals("safe")) {
               is = this.getSafeItem(p);
            } else if (name1.equals("give")) {
               is = Main.getTownManager().getTownGiveItem();
            } else if (name1.equals("safeLock")) {
               is = Main.getTownManager().getTownSafeLockItem(p);
            } else if (name1.equals("tip")) {
               is = Main.getTownManager().getTownTipItem();
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

   private ItemStack getSafeItem(Player p) {
      ItemStack is = UtilItems.getItem(this.pn, "main_safe").clone();
      ItemMeta im = is.getItemMeta();
      List<String> lore = im.getLore();
      lore.set(0, ((String)lore.get(0)).replace("{0}", Main.getTownManager().isSafeShow(p)));
      im.setLore(lore);
      is.setItemMeta(im);
      return is;
   }

   private void loadConfig(YamlConfiguration config) {
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

   private class InputSetName implements InputManager.InputHandler {
      private Player p;

      public InputSetName(Player p) {
         super();
         this.p = p;
      }

      public boolean onInput(String s) {
         return Main.getTownManager().setName(this.p, s);
      }
   }

   private class InputGive implements InputManager.InputHandler {
      private Player p;

      public InputGive(Player p) {
         super();
         this.p = p;
      }

      public boolean onInput(String s) {
         return Main.getTownManager().give(this.p, s);
      }
   }

   private class InputJoin implements InputManager.InputHandler {
      private Player p;

      public InputJoin(Player p) {
         super();
         this.p = p;
      }

      public boolean onInput(String s) {
         return Main.getTownManager().join(this.p, s);
      }
   }

   private class InputDel implements InputManager.InputHandler {
      private Player p;

      public InputDel(Player p) {
         super();
         this.p = p;
      }

      public boolean onInput(String s) {
         return Main.getTownManager().del(this.p, s);
      }
   }

   private class InputYes implements InputManager.InputHandler {
      private Player p;

      public InputYes(Player p) {
         super();
         this.p = p;
      }

      public boolean onInput(String s) {
         return Main.getTownManager().yes(this.p, s);
      }
   }

   private class InputNo implements InputManager.InputHandler {
      private Player p;

      public InputNo(Player p) {
         super();
         this.p = p;
      }

      public boolean onInput(String s) {
         return Main.getTownManager().no(this.p, s);
      }
   }

   private class SessionGetPos implements IconMenu.Session {
      private Player p;

      public SessionGetPos(Player p) {
         super();
         this.p = p;
      }

      public void onSelect(IconMenu.Session.Result select) {
         if (select.equals(Result.YES)) {
            Main.getTownManager().getPos(this.p);
         }

      }
   }

   private class SessionSafeLock implements IconMenu.Session {
      private Player p;

      public SessionSafeLock(Player p) {
         super();
         this.p = p;
      }

      public void onSelect(IconMenu.Session.Result select) {
         if (select.equals(Result.YES)) {
            Main.getTownManager().safeLock(this.p, true);
         } else if (select.equals(Result.NO)) {
            Main.getTownManager().safeLock(this.p, false);
         }

      }
   }

   private class SessionQuit implements IconMenu.Session {
      private Player p;

      public SessionQuit(Player p) {
         super();
         this.p = p;
      }

      public void onSelect(IconMenu.Session.Result select) {
         if (select.equals(Result.YES)) {
            Main.getTownManager().quit(this.p);
         }

      }
   }

   private class SessionYesAll implements IconMenu.Session {
      private Player p;

      public SessionYesAll(Player p) {
         super();
         this.p = p;
      }

      public void onSelect(IconMenu.Session.Result select) {
         if (select.equals(Result.YES)) {
            Main.getTownManager().yesAll(this.p);
         }

      }
   }

   private class SessionNoAll implements IconMenu.Session {
      private Player p;

      public SessionNoAll(Player p) {
         super();
         this.p = p;
      }

      public void onSelect(IconMenu.Session.Result select) {
         if (select.equals(Result.YES)) {
            Main.getTownManager().noAll(this.p);
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
         switch (pos - 45) {
            case 4:
               if (Infos.getShowManager().showMainMenu(this.p, this.p.getName())) {
                  event.setWillClose(true);
               }

               return;
            default:
               ShowInfo showInfo = (ShowInfo)ShowManager.this.mainMenuHash.get(pos);
               if (showInfo != null) {
                  String name = showInfo.getName();
                  if (name.equals("tp")) {
                     event.setWillClose(true);
                     Main.getTownManager().spawn(this.p);
                  } else if (name.equals("setName")) {
                     InputManager.InputHandler inputHandler = ShowManager.this.new InputSetName(this.p);
                     String tip = ShowManager.this.get(260);
                     if (Lib.getInputManager().input(this.p, inputHandler, tip)) {
                        event.setWillClose(true);
                     }
                  } else if (name.equals("give")) {
                     InputManager.InputHandler inputHandler = ShowManager.this.new InputGive(this.p);
                     String tip = ShowManager.this.get(335);
                     if (Lib.getInputManager().input(this.p, inputHandler, tip)) {
                        event.setWillClose(true);
                     }
                  } else if (name.equals("rank")) {
                     event.setWillClose(true);
                     this.p.chat("/town rank");
                  } else if (name.equals("get")) {
                     event.setWillClose(true);
                     IconMenu.Session session = ShowManager.this.new SessionGetPos(this.p);
                     UtilIconMenu.openSession(this.p, ShowManager.this.get(105), (List)null, session, ShowManager.this.confirmTimeLimit);
                  } else if (name.equals("safeLock")) {
                     event.setWillClose(true);
                     IconMenu.Session session = ShowManager.this.new SessionSafeLock(this.p);
                     UtilIconMenu.openSession(this.p, ShowManager.this.get(270), (List)null, session, ShowManager.this.confirmTimeLimit);
                  } else if (name.equals("join")) {
                     InputManager.InputHandler inputHandler = ShowManager.this.new InputJoin(this.p);
                     String tip = ShowManager.this.get(170);
                     if (Lib.getInputManager().input(this.p, inputHandler, tip)) {
                        event.setWillClose(true);
                     }
                  } else if (name.equals("del")) {
                     InputManager.InputHandler inputHandler = ShowManager.this.new InputDel(this.p);
                     String tip = ShowManager.this.get(150);
                     if (Lib.getInputManager().input(this.p, inputHandler, tip)) {
                        event.setWillClose(true);
                     }
                  } else if (name.equals("quit")) {
                     event.setWillClose(true);
                     IconMenu.Session session = ShowManager.this.new SessionQuit(this.p);
                     UtilIconMenu.openSession(this.p, ShowManager.this.get(160), (List)null, session, ShowManager.this.confirmTimeLimit);
                  } else if (name.equals("yesOne")) {
                     InputManager.InputHandler inputHandler = ShowManager.this.new InputYes(this.p);
                     String tip = UtilFormat.format(ShowManager.this.pn, "ask2", new Object[]{Main.getTownManager().getAskListShow(this.p)});
                     if (Lib.getInputManager().input(this.p, inputHandler, tip)) {
                        event.setWillClose(true);
                     }
                  } else if (name.equals("noOne")) {
                     InputManager.InputHandler inputHandler = ShowManager.this.new InputNo(this.p);
                     String tip = UtilFormat.format(ShowManager.this.pn, "ask2", new Object[]{Main.getTownManager().getAskListShow(this.p)});
                     if (Lib.getInputManager().input(this.p, inputHandler, tip)) {
                        event.setWillClose(true);
                     }
                  } else if (name.equals("yesAll")) {
                     event.setWillClose(true);
                     IconMenu.Session session = ShowManager.this.new SessionYesAll(this.p);
                     UtilIconMenu.openSession(this.p, ShowManager.this.get(225), (List)null, session, ShowManager.this.confirmTimeLimit);
                  } else if (name.equals("noAll")) {
                     event.setWillClose(true);
                     IconMenu.Session session = ShowManager.this.new SessionNoAll(this.p);
                     UtilIconMenu.openSession(this.p, ShowManager.this.get(230), (List)null, session, ShowManager.this.confirmTimeLimit);
                  } else if (name.equals("safe")) {
                     event.setWillClose(true);
                     Main.getTownManager().toggleSafe(this.p);
                     ShowManager.this.showMainMenu(this.p);
                  }
               }

         }
      }
   }
}
