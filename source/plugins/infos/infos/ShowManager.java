package infos;

import ad.Ad;
import chat.Chat;
import chat.ChatColor;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import landMain.LandMain;
import lib.IconMenu;
import lib.IconMenu.Session.Result;
import lib.config.ReloadConfigEvent;
import lib.util.Util;
import lib.util.UtilConfig;
import lib.util.UtilFormat;
import lib.util.UtilIconMenu;
import lib.util.UtilItems;
import lib.util.UtilPer;
import lib.util.UtilRewards;
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
import shop.Main;
import ticket.Ticket;

public class ShowManager implements Listener {
   private static final String SPEED_SHOW = "show";
   private static final int SIZE = 45;
   private Infos infos;
   private String pn;
   private int interval;
   private String upgradeName;
   private List upgradeLore;
   private int confirmTimeLimit;
   private String piName;
   private HashMap mainMenuHash;
   private HashMap setMenuHash;
   private HashMap tpMenuHash;
   private HashMap cmdHash;

   public ShowManager(Infos infos) {
      super();
      this.infos = infos;
      this.pn = Infos.getPn();
      this.loadConfig(UtilConfig.getConfig(this.pn));
      infos.getPm().registerEvents(this, infos);
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

   public boolean showMainMenu(Player p, String tar) {
      tar = Util.getRealName(p, tar);
      if (tar == null) {
         return false;
      } else if (!p.getName().equals(tar) && !UtilPer.checkPer(p, Infos.getPlayerInfoManager().getPer_infos_playerInfo_other())) {
         return false;
      } else if (!UtilSpeed.check(p, this.pn, "show", this.interval)) {
         return false;
      } else {
         PlayerInfo pi = Infos.getPlayerInfoManager().getPlayerInfo(tar);
         if (pi == null) {
            p.sendMessage(UtilFormat.format(this.pn, "noInfo", new Object[]{tar}));
            return false;
         } else {
            String name = this.get(150);
            IconMenu.OptionClickEventHandler handler = new HandlerShowMainMenu(p, tar);
            IconMenu.Info info = UtilIconMenu.register(name, 45, true, handler);

            for(ShowInfo showInfo : this.mainMenuHash.values()) {
               String name1 = showInfo.getName();
               ItemStack is;
               if (name1.equals("spawn")) {
                  if (!p.getName().equals(tar)) {
                     continue;
                  }

                  is = this.getSpawnLocItem(p);
               } else if (name1.equals("info")) {
                  is = this.getPlayerInfoItem(tar);
               } else if (name1.equals("serverInfo")) {
                  if (!p.getName().equals(tar)) {
                     continue;
                  }

                  is = this.getServerInfoItem();
               } else if (name1.equals("sit")) {
                  if (!p.getName().equals(tar)) {
                     continue;
                  }

                  is = this.getSitItem(p);
               } else if (name1.equals("serverMsg")) {
                  if (!p.getName().equals(tar)) {
                     continue;
                  }

                  is = this.getServerMsgItem(p);
               } else if (name1.equals("deathMsg")) {
                  if (!p.getName().equals(tar)) {
                     continue;
                  }

                  is = this.getDeathMsgItem(p);
               } else if (name1.equals("addBlack")) {
                  if (p.getName().equals(tar)) {
                     continue;
                  }

                  is = UtilItems.getItem(this.pn, showInfo.getType());
               } else if (!name1.equals("level") && !name1.equals("ad")) {
                  if (name1.equals("ticket")) {
                     if (!p.getName().equals(tar)) {
                        continue;
                     }

                     is = UtilItems.getItem(this.pn, showInfo.getType());
                  } else if (!name1.equals("fasthelp") && !name1.equals("webshop") && !name1.equals("town") && !name1.equals("tip")) {
                     if (name1.equals("chatColor")) {
                        if (!p.getName().equals(tar)) {
                           continue;
                        }

                        is = UtilItems.getItem(this.pn, showInfo.getType()).clone();
                        ItemMeta im = is.getItemMeta();
                        List<String> lore = im.getLore();
                        String bold;
                        if (UtilPer.hasPer(p, ChatColor.getCheck())) {
                           bold = "§l";
                        } else {
                           bold = "";
                        }

                        lore.set(0, ((String)lore.get(0)).replace("{0}", Chat.getChatColor().getColorShow(p.getName())).replace("{1}", bold));
                        im.setLore(lore);
                        is.setItemMeta(im);
                     } else if (name1.equals("chatChannel")) {
                        if (!p.getName().equals(tar)) {
                           continue;
                        }

                        is = UtilItems.getItem(this.pn, showInfo.getType()).clone();
                        ItemMeta im = is.getItemMeta();
                        List<String> lore = im.getLore();
                        lore.set(0, ((String)lore.get(0)).replace("{0}", Chat.getChannel().getChannelShow(p.getName())));
                        im.setLore(lore);
                        is.setItemMeta(im);
                     } else if (name1.equals("up")) {
                        if (!p.getName().equals(tar)) {
                           continue;
                        }

                        is = this.getLevelItem(p);
                     } else {
                        is = UtilItems.getItem(this.pn, showInfo.getType());
                     }
                  } else {
                     if (!p.getName().equals(tar)) {
                        continue;
                     }

                     is = UtilItems.getItem(this.pn, showInfo.getType());
                  }
               } else {
                  if (!p.getName().equals(tar)) {
                     continue;
                  }

                  is = UtilItems.getItem(this.pn, showInfo.getType());
               }

               info.setItem(showInfo.getPos(), is);
            }

            Inventory inv = Bukkit.getServer().createInventory((InventoryHolder)null, 9, "");
            if (p.getName().equals(tar)) {
               inv.setItem(0, UtilItems.getItem(this.pn, "main_set"));
               inv.setItem(4, UtilItems.getItem(this.pn, "main_tp"));
               inv.setItem(8, UtilItems.getItem(this.pn, "main_tip"));
            }

            UtilIconMenu.open(p, info, (String)null, inv);
            return true;
         }
      }
   }

   public boolean showSetMenu(Player p) {
      if (!UtilSpeed.check(p, this.pn, "show", this.interval)) {
         return false;
      } else {
         String name = p.getName();
         PlayerInfo pi = Infos.getPlayerInfoManager().getPlayerInfo(name);
         if (pi == null) {
            p.sendMessage(UtilFormat.format(this.pn, "noInfo", new Object[]{name}));
            return false;
         } else {
            IconMenu.OptionClickEventHandler handler = new HandlerShowSetMenu(p);
            IconMenu.Info info = UtilIconMenu.register(this.get(600), 45, true, handler);

            for(ShowInfo showInfo : this.setMenuHash.values()) {
               String name1 = showInfo.getName();
               ItemStack is;
               if (name1.equals("spawn")) {
                  is = this.getSpawnLocItem(p);
               } else if (name1.equals("sit")) {
                  is = this.getSitItem(p);
               } else if (name1.equals("serverMsg")) {
                  is = this.getServerMsgItem(p);
               } else if (name1.equals("deathMsg")) {
                  is = this.getDeathMsgItem(p);
               } else if (name1.equals("sideMsg")) {
                  is = this.getSideMsgItem(p);
               } else if (name1.equals("moveMsg")) {
                  is = this.getMoveMsgItem(p);
               } else if (name1.equals("see")) {
                  is = this.getSeeItem(p);
               } else if (name1.equals("sex")) {
                  is = Infos.getPlayerInfoManager().getSexItem(p);
               } else if (name1.equals("xq")) {
                  is = Infos.getPlayerInfoManager().getXqItem(p);
               } else if (name1.equals("chatColor")) {
                  is = UtilItems.getItem(this.pn, showInfo.getType()).clone();
                  ItemMeta im = is.getItemMeta();
                  List<String> lore = im.getLore();
                  String bold;
                  if (UtilPer.hasPer(p, ChatColor.getCheck())) {
                     bold = "§l";
                  } else {
                     bold = "";
                  }

                  lore.set(0, ((String)lore.get(0)).replace("{0}", Chat.getChatColor().getColorShow(p.getName())).replace("{1}", bold));
                  im.setLore(lore);
                  is.setItemMeta(im);
               } else if (name1.equals("chatChannel")) {
                  is = UtilItems.getItem(this.pn, showInfo.getType()).clone();
                  ItemMeta im = is.getItemMeta();
                  List<String> lore = im.getLore();
                  lore.set(0, ((String)lore.get(0)).replace("{0}", Chat.getChannel().getChannelShow(p.getName())));
                  im.setLore(lore);
                  is.setItemMeta(im);
               } else if (name1.equals("upgrade")) {
                  is = UtilItems.getItem(this.pn, showInfo.getType());
               } else if (name1.equals("buyJoin")) {
                  is = Infos.getJoin().getStone();
               } else {
                  is = UtilItems.getItem(this.pn, showInfo.getType());
               }

               info.setItem(showInfo.getPos(), is);
            }

            Inventory inv = Bukkit.getServer().createInventory((InventoryHolder)null, 9, "");
            inv.setItem(4, UtilItems.getItem(this.pn, "back"));
            UtilIconMenu.open(p, info, (String)null, inv);
            return true;
         }
      }
   }

   public boolean showTpMenu(Player p) {
      if (!UtilSpeed.check(p, this.pn, "show", this.interval)) {
         return false;
      } else {
         String name = p.getName();
         PlayerInfo pi = Infos.getPlayerInfoManager().getPlayerInfo(name);
         if (pi == null) {
            p.sendMessage(UtilFormat.format(this.pn, "noInfo", new Object[]{name}));
            return false;
         } else {
            IconMenu.OptionClickEventHandler handler = new HandlerShowTpMenu(p);
            IconMenu.Info info = UtilIconMenu.register(this.get(610), 45, true, handler);

            for(ShowInfo showInfo : this.tpMenuHash.values()) {
               ItemStack is = UtilItems.getItem(this.pn, showInfo.getType());
               info.setItem(showInfo.getPos(), is);
            }

            Inventory inv = Bukkit.getServer().createInventory((InventoryHolder)null, 9, "");
            inv.setItem(4, UtilItems.getItem(this.pn, "back"));
            UtilIconMenu.open(p, info, (String)null, inv);
            return true;
         }
      }
   }

   public boolean showSelectXq(Player p) {
      if (!UtilPer.hasPer(p, Infos.getPlayerInfoManager().getXqPer())) {
         p.sendMessage(this.get(470));
         return false;
      } else if (!UtilSpeed.check(p, this.pn, "show", this.interval)) {
         return false;
      } else {
         String name = this.get(235);
         IconMenu.OptionClickEventHandler handler = new HandlerShowSelectXq(p);
         IconMenu.Info info = UtilIconMenu.register(name, 45, true, handler);
         HashMap<Integer, ItemStack> xqHash = Infos.getPlayerInfoManager().getXqHash();
         int size = xqHash.size();
         size = Math.min(45, size);

         for(int i = 0; i < size; ++i) {
            ItemStack is = (ItemStack)xqHash.get(i);
            info.setItem(i, is);
         }

         Inventory inv = Bukkit.getServer().createInventory((InventoryHolder)null, 9, "");
         inv.setItem(4, UtilItems.getItem(this.pn, "xq_back"));
         inv.setItem(8, UtilItems.getItem(this.pn, "xq_del"));
         UtilIconMenu.open(p, info, (String)null, inv);
         return true;
      }
   }

   private boolean handle(Player p, String page, int pos) {
      HashMap<Integer, CmdInfo> hash = (HashMap)this.cmdHash.get(page);
      if (hash != null) {
         CmdInfo ci = (CmdInfo)hash.get(pos);
         if (ci != null) {
            if (ci.isServer()) {
               Bukkit.dispatchCommand(Bukkit.getConsoleSender(), ci.getCmd());
            } else {
               p.chat(ci.getCmd());
            }

            return true;
         }
      }

      return false;
   }

   private ItemStack getSeeItem(Player p) {
      ItemStack is = UtilItems.getItem(this.pn, "set_see").clone();
      ItemMeta im = is.getItemMeta();
      List<String> lore = im.getLore();
      lore.set(0, ((String)lore.get(0)).replace("{0}", Infos.getPlayerInfoManager().getSeeShow(p)));
      im.setLore(lore);
      is.setItemMeta(im);
      return is;
   }

   private ItemStack getSpawnLocItem(Player p) {
      ItemStack is = UtilItems.getItem(this.pn, "set_spawn").clone();
      ItemMeta im = is.getItemMeta();
      List<String> lore = im.getLore();
      lore.set(0, ((String)lore.get(0)).replace("{0}", Infos.getPlayerInfoManager().getSpawnLocShow(p)));
      im.setLore(lore);
      is.setItemMeta(im);
      return is;
   }

   private void loadConfig(YamlConfiguration config) {
      this.interval = config.getInt("item.interval");
      this.confirmTimeLimit = config.getInt("item.confirmTimeLimit");
      this.upgradeName = Util.convert(config.getString("item.upgradeName"));
      this.upgradeLore = new ArrayList();

      for(String s : config.getStringList("item.upgradeLore")) {
         this.upgradeLore.add(Util.convert(s));
      }

      this.piName = Util.convert(config.getString("item.pi.name"));
      String ss = "main";
      this.mainMenuHash = new HashMap();

      for(String s : ((MemorySection)config.get("item." + ss)).getValues(false).keySet()) {
         this.add(this.mainMenuHash, config, ss, s);
      }

      ss = "set";
      this.setMenuHash = new HashMap();

      for(String s : ((MemorySection)config.get("item." + ss)).getValues(false).keySet()) {
         this.add(this.setMenuHash, config, ss, s);
      }

      ss = "tp";
      this.tpMenuHash = new HashMap();

      for(String s : ((MemorySection)config.get("item." + ss)).getValues(false).keySet()) {
         this.add(this.tpMenuHash, config, ss, s);
      }

      this.cmdHash = new HashMap();

      for(String s : config.getStringList("cmd")) {
         String[] sss = s.split(" ");
         String page = sss[0];
         int pos = Integer.parseInt(sss[1]);
         boolean server = sss[2].equalsIgnoreCase("s");
         String cmd = Util.combine(sss, " ", 3, sss.length);
         CmdInfo ci = new CmdInfo(server, cmd);
         if (!this.cmdHash.containsKey(page)) {
            this.cmdHash.put(page, new HashMap());
         }

         ((HashMap)this.cmdHash.get(page)).put(pos, ci);
      }

   }

   private void add(HashMap hash, YamlConfiguration config, String s, String name) {
      int pos = config.getInt("item." + s + "." + name);
      String type = s + "_" + name;
      ShowInfo showInfo = new ShowInfo(name, pos, type);
      hash.put(pos, showInfo);
   }

   private ItemStack getPlayerInfoItem(String name) {
      String show = Infos.getPlayerInfoManager().getShowPlayerInfo(name);
      if (show == null) {
         return null;
      } else {
         ItemStack result = UtilItems.getItem(this.pn, "main_info").clone();
         ItemMeta im = result.getItemMeta();
         im.setDisplayName(this.piName.replace("{0}", name));
         List<String> lore = new ArrayList();

         String[] var9;
         for(String s : var9 = show.split("\n")) {
            lore.add(s);
         }

         im.setLore(lore);
         result.setItemMeta(im);
         return result;
      }
   }

   private ItemStack getServerInfoItem() {
      String show = this.infos.getServerInfoManager().getShowInfo();
      ItemStack result = UtilItems.getItem(this.pn, "main_serverInfo").clone();
      ItemMeta im = result.getItemMeta();
      List<String> lore = new ArrayList();

      String[] var8;
      for(String s : var8 = show.split("\n")) {
         lore.add(s);
      }

      im.setLore(lore);
      result.setItemMeta(im);
      return result;
   }

   private ItemStack getSitItem(Player p) {
      ItemStack result = UtilItems.getItem(this.pn, "set_sit");
      ItemMeta im = result.getItemMeta();
      List<String> lore = im.getLore();
      String msg = this.infos.getSit().getModeShow(p);
      lore.set(0, ((String)lore.get(0)).replace("{0}", msg));
      im.setLore(lore);
      result.setItemMeta(im);
      return result;
   }

   private ItemStack getServerMsgItem(Player p) {
      ItemStack result = UtilItems.getItem(this.pn, "set_serverMsg").clone();
      ItemMeta im = result.getItemMeta();
      List<String> lore = im.getLore();
      String msg = this.infos.getBasic().getServerMsg().getModeShow(p);
      lore.set(0, ((String)lore.get(0)).replace("{0}", msg));
      im.setLore(lore);
      result.setItemMeta(im);
      return result;
   }

   private ItemStack getDeathMsgItem(Player p) {
      ItemStack result = UtilItems.getItem(this.pn, "set_deathMsg").clone();
      ItemMeta im = result.getItemMeta();
      List<String> lore = im.getLore();
      String msg = this.infos.getDeath().getDeathMessage().getModeShow(p);
      lore.set(0, ((String)lore.get(0)).replace("{0}", msg));
      im.setLore(lore);
      result.setItemMeta(im);
      return result;
   }

   private ItemStack getSideMsgItem(Player p) {
      ItemStack result = UtilItems.getItem(this.pn, "set_sideMsg").clone();
      ItemMeta im = result.getItemMeta();
      List<String> lore = im.getLore();
      String tip;
      if (UtilScoreboard.isDisplaySideBar(p)) {
         tip = this.get(165);
      } else {
         tip = this.get(170);
      }

      lore.set(0, ((String)lore.get(0)).replace("{0}", tip));
      im.setLore(lore);
      result.setItemMeta(im);
      return result;
   }

   private ItemStack getMoveMsgItem(Player p) {
      int status = this.infos.getShows().getStatus(p);
      if (status == 0) {
         return null;
      } else {
         ItemStack result = UtilItems.getItem(this.pn, "set_moveMsg").clone();
         ItemMeta im = result.getItemMeta();
         List<String> lore = im.getLore();
         String tip;
         if (status == 1) {
            tip = this.get(170);
         } else {
            tip = this.get(165);
         }

         lore.set(0, ((String)lore.get(0)).replace("{0}", tip));
         im.setLore(lore);
         result.setItemMeta(im);
         return result;
      }
   }

   private ItemStack getLevelItem(Player p) {
      int level = Infos.getPlayerInfoManager().getLevel(p.getName());
      int nextCost = Infos.getPlayerInfoManager().getNextCost(level + 1);
      ItemStack result = UtilItems.getItem(this.pn, "main_up").clone();
      ItemMeta im = result.getItemMeta();
      List<String> lore = im.getLore();
      im.setDisplayName(im.getDisplayName().replace("{0}", String.valueOf(level)));
      if (nextCost != -1) {
         lore.set(0, ((String)lore.get(0)).replace("{0}", String.valueOf(nextCost)));
      } else {
         lore.set(0, this.get(185));
      }

      im.setLore(lore);
      result.setItemMeta(im);
      return result;
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

   private class CmdInfo {
      private boolean server;
      private String cmd;

      public CmdInfo(boolean server, String cmd) {
         super();
         this.server = server;
         this.cmd = cmd;
      }

      public boolean isServer() {
         return this.server;
      }

      public String getCmd() {
         return this.cmd;
      }
   }

   private class SessionUpgrade implements IconMenu.Session {
      private Player p;

      public SessionUpgrade(Player p) {
         super();
         this.p = p;
      }

      public void onSelect(IconMenu.Session.Result select) {
         if (select.equals(Result.YES)) {
            ShowManager.this.infos.getHouse().upgrade(this.p);
         }

      }
   }

   private class SessionAdd implements IconMenu.Session {
      private Player p;
      private String tar;

      public SessionAdd(Player p, String tar) {
         super();
         this.p = p;
         this.tar = tar;
      }

      public void onSelect(IconMenu.Session.Result select) {
         if (select.equals(Result.YES)) {
            Chat.getBlackList().addBlack(this.p, this.tar);
            ShowManager.this.showMainMenu(this.p, this.tar);
         }

      }
   }

   private class SessionUp implements IconMenu.Session {
      private Player p;

      public SessionUp(Player p) {
         super();
         this.p = p;
      }

      public void onSelect(IconMenu.Session.Result select) {
         if (select.equals(Result.YES)) {
            Infos.getPlayerInfoManager().upgrade(this.p);
         }

      }
   }

   private class SessionBuy implements IconMenu.Session {
      private Player p;

      public SessionBuy(Player p) {
         super();
         this.p = p;
      }

      public void onSelect(IconMenu.Session.Result select) {
         if (select.equals(Result.YES)) {
            Infos.getJoin().buy(this.p);
         }

      }
   }

   private class HandlerShowSelectXq implements IconMenu.OptionClickEventHandler {
      private Player p;

      public HandlerShowSelectXq(Player p) {
         super();
         this.p = p;
      }

      public void onOptionClick(IconMenu.OptionClickEvent event) {
         int pos = event.getPos();
         switch (pos - 45) {
            case 4:
               if (ShowManager.this.showSetMenu(this.p)) {
                  event.setWillClose(true);
               }

               return;
            case 5:
            case 6:
            case 7:
            default:
               if (!UtilSpeed.check(this.p, ShowManager.this.pn, PlayerInfoManager.getSpeedXq(), Infos.getPlayerInfoManager().getXqInterval())) {
                  return;
               }

               if (Infos.getPlayerInfoManager().setXq(this.p, pos)) {
                  this.p.sendMessage(UtilFormat.format(ShowManager.this.pn, "success", new Object[]{ShowManager.this.get(245)}));
                  event.setWillClose(true);
               }

               return;
            case 8:
               if (UtilSpeed.check(this.p, ShowManager.this.pn, PlayerInfoManager.getSpeedXq(), Infos.getPlayerInfoManager().getXqInterval())) {
                  if (Infos.getPlayerInfoManager().setXq(this.p, -1)) {
                     this.p.sendMessage(UtilFormat.format(ShowManager.this.pn, "success", new Object[]{ShowManager.this.get(250)}));
                     event.setWillClose(true);
                  }

               }
         }
      }
   }

   private class HandlerShowMainMenu implements IconMenu.OptionClickEventHandler {
      private Player p;
      private String tar;

      public HandlerShowMainMenu(Player p, String tar) {
         super();
         this.p = p;
         this.tar = tar;
      }

      public void onOptionClick(IconMenu.OptionClickEvent event) {
         int pos = event.getPos();
         switch (pos - 45) {
            case 0:
               if (this.p.getName().equals(this.tar) && ShowManager.this.showSetMenu(this.p)) {
                  event.setWillClose(true);
               }

               return;
            case 1:
            case 2:
            case 3:
            default:
               ShowInfo showInfo = (ShowInfo)ShowManager.this.mainMenuHash.get(pos);
               if (showInfo != null) {
                  String name = showInfo.getName();
                  if (name.equalsIgnoreCase("debt")) {
                     this.p.chat("/debt show " + this.tar);
                     event.setWillClose(true);
                  } else if (name.equalsIgnoreCase("webshop")) {
                     if (Main.getShowManager().showMainMenu(this.p, 1)) {
                        event.setWillClose(true);
                     }
                  } else if (name.equalsIgnoreCase("fasthelp")) {
                     fasthelp.Main.getHelpManager().openSession(this.p);
                     event.setWillClose(true);
                  } else if (name.equals("town")) {
                     if (town.Main.getShowManager().showMainMenu(this.p)) {
                        event.setWillClose(true);
                     }
                  } else if (name.equals("friend")) {
                     if (ShowManager.this.infos.getFriend().getShowManager().showMainMenu(this.p, this.tar, 1)) {
                        event.setWillClose(true);
                     }
                  } else if (name.equals("lands1")) {
                     if (LandMain.getLandManager().getLandMain().getShowManager().seeYou(1, this.p, this.tar, 1, 0)) {
                        event.setWillClose(true);
                     }
                  } else if (name.equals("lands2")) {
                     if (LandMain.getLandManager().getLandMain().getShowManager().seeYou(2, this.p, this.tar, 1, 0)) {
                        event.setWillClose(true);
                     }
                  } else if (name.equals("lands3")) {
                     if (LandMain.getLandManager().getLandMain().getShowManager().seeYou(3, this.p, this.tar, 1, 0)) {
                        event.setWillClose(true);
                     }
                  } else if (name.equals("lands4")) {
                     if (LandMain.getLandManager().getLandMain().getShowManager().seeYou(3, this.p, this.tar, 1, 2)) {
                        event.setWillClose(true);
                     }
                  } else if (name.equals("rewards")) {
                     if (UtilRewards.showList(this.p, this.tar, 1)) {
                        event.setWillClose(true);
                     }
                  } else if (name.equals("showBlack")) {
                     if (Chat.getShowManager().showBlackList(this.p, this.tar, 1)) {
                        event.setWillClose(true);
                     }
                  } else if (name.equals("addBlack")) {
                     event.setWillClose(true);
                     IconMenu.Session session = ShowManager.this.new SessionAdd(this.p, this.tar);
                     UtilIconMenu.openSession(this.p, UtilFormat.format(ShowManager.this.pn, "addTip", new Object[]{this.tar}), (List)null, session, ShowManager.this.confirmTimeLimit);
                  } else if (name.equals("ticket")) {
                     if (Ticket.getShowManager().showMainMenu(this.p)) {
                        event.setWillClose(true);
                     }
                  } else if (name.equals("ad")) {
                     if (Ad.getShowManager().showMainMenu(this.p)) {
                        event.setWillClose(true);
                     }
                  } else if (name.equals("level")) {
                     if (level.Main.getShowManager().showMainMenu(this.p)) {
                        event.setWillClose(true);
                     }
                  } else if (name.equals("up")) {
                     event.setWillClose(true);
                     IconMenu.Session sessionUp = ShowManager.this.new SessionUp(this.p);
                     UtilIconMenu.openSession(this.p, ShowManager.this.get(175), (List)null, sessionUp, ShowManager.this.confirmTimeLimit);
                  }
               }

               return;
            case 4:
               if (this.p.getName().equals(this.tar) && ShowManager.this.showTpMenu(this.p)) {
                  event.setWillClose(true);
               }

         }
      }
   }

   private class HandlerShowSetMenu implements IconMenu.OptionClickEventHandler {
      private Player p;

      public HandlerShowSetMenu(Player p) {
         super();
         this.p = p;
      }

      public void onOptionClick(IconMenu.OptionClickEvent event) {
         int pos = event.getPos();
         switch (pos - 45) {
            case 4:
               if (ShowManager.this.showMainMenu(this.p, this.p.getName())) {
                  event.setWillClose(true);
               }

               return;
            default:
               ShowInfo showInfo = (ShowInfo)ShowManager.this.setMenuHash.get(pos);
               if (showInfo != null) {
                  String name = showInfo.getName();
                  if (name.equalsIgnoreCase("bind")) {
                     this.p.sendMessage(ShowManager.this.get(500));
                     event.setWillClose(true);
                  } else if (name.equals("spawn")) {
                     Infos.getPlayerInfoManager().toggleSpawnLoc(this.p);
                     event.setWillClose(true);
                     ShowManager.this.showSetMenu(this.p);
                  } else if (name.equals("sit")) {
                     ShowManager.this.infos.getSit().toggleSit(this.p);
                     event.setWillClose(true);
                     ShowManager.this.showSetMenu(this.p);
                  } else if (name.equals("serverMsg")) {
                     ShowManager.this.infos.getBasic().getServerMsg().toggleServerMsg(this.p);
                     event.setWillClose(true);
                     ShowManager.this.showSetMenu(this.p);
                  } else if (name.equals("deathMsg")) {
                     ShowManager.this.infos.getDeath().getDeathMessage().toggleDeathMsg(this.p);
                     event.setWillClose(true);
                     ShowManager.this.showSetMenu(this.p);
                  } else if (name.equals("sideMsg")) {
                     boolean display = UtilScoreboard.isDisplaySideBar(this.p);
                     UtilScoreboard.setDisplaySideBar(this.p, !display);
                     if (!display) {
                        this.p.sendMessage(UtilFormat.format(ShowManager.this.pn, "success", new Object[]{ShowManager.this.get(155)}));
                     } else {
                        this.p.sendMessage(UtilFormat.format(ShowManager.this.pn, "fail", new Object[]{ShowManager.this.get(160)}));
                     }

                     event.setWillClose(true);
                     ShowManager.this.showSetMenu(this.p);
                  } else if (name.equals("moveMsg")) {
                     if (ShowManager.this.infos.getShows().toggleMove(this.p)) {
                        event.setWillClose(true);
                        ShowManager.this.showSetMenu(this.p);
                     }
                  } else if (name.equals("see")) {
                     if (Infos.getPlayerInfoManager().toggleSee(this.p)) {
                        event.setWillClose(true);
                        ShowManager.this.showSetMenu(this.p);
                     }
                  } else if (name.equals("sex")) {
                     if (!UtilSpeed.check(this.p, ShowManager.this.pn, PlayerInfoManager.getSpeedXq(), Infos.getPlayerInfoManager().getXqInterval())) {
                        return;
                     }

                     Infos.getPlayerInfoManager().toggleSex(this.p);
                     this.p.sendMessage(UtilFormat.format(ShowManager.this.pn, "success", new Object[]{ShowManager.this.get(240)}));
                     event.setWillClose(true);
                     ShowManager.this.showSetMenu(this.p);
                  } else if (name.equals("xq")) {
                     if (ShowManager.this.showSelectXq(this.p)) {
                        event.setWillClose(true);
                        ShowManager.this.showSetMenu(this.p);
                     }
                  } else if (name.equals("chatChannel")) {
                     Chat.getChannel().toggleChannel(this.p);
                     event.setWillClose(true);
                     ShowManager.this.showSetMenu(this.p);
                  } else if (name.equals("chatColor")) {
                     if (Chat.getShowManager().showSelectColor(this.p)) {
                        event.setWillClose(true);
                     }
                  } else if (name.equals("upgrade")) {
                     event.setWillClose(true);
                     IconMenu.Session session = ShowManager.this.new SessionUpgrade(this.p);
                     UtilIconMenu.openSession(this.p, ShowManager.this.upgradeName, ShowManager.this.upgradeLore, session, ShowManager.this.confirmTimeLimit);
                  } else if (name.equals("buyJoin")) {
                     if (event.isLeft()) {
                        IconMenu.Session session = ShowManager.this.new SessionBuy(this.p);
                        UtilIconMenu.openSession(this.p, ShowManager.this.get(435), (List)null, session, ShowManager.this.confirmTimeLimit);
                     } else {
                        if (!event.isRight()) {
                           return;
                        }

                        this.p.sendMessage(ShowManager.this.get(480));
                     }

                     event.setWillClose(true);
                  }
               }

         }
      }
   }

   private class HandlerShowTpMenu implements IconMenu.OptionClickEventHandler {
      private Player p;

      public HandlerShowTpMenu(Player p) {
         super();
         this.p = p;
      }

      public void onOptionClick(IconMenu.OptionClickEvent event) {
         int pos = event.getPos();
         switch (pos - 45) {
            case 4:
               if (ShowManager.this.showMainMenu(this.p, this.p.getName())) {
                  event.setWillClose(true);
               }

               return;
            default:
               if (ShowManager.this.handle(this.p, "tp", pos)) {
                  event.setWillClose(true);
               }

         }
      }
   }
}
