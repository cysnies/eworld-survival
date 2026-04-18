package infos;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.HashMap;
import lib.config.ReloadConfigEvent;
import lib.time.TimeEvent;
import lib.util.Util;
import lib.util.UtilConfig;
import lib.util.UtilFormat;
import lib.util.UtilItems;
import lib.util.UtilNames;
import lib.util.UtilPer;
import lib.util.UtilRewards;
import org.bukkit.Server;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;

public class Gift implements Listener {
   private Infos infos;
   private Dao dao;
   private Server server;
   private String pn;
   private String per_infos_admin;
   private String per_infos_gift;
   private HashMap giftHash;
   private HashMap userHash;
   private HashMap timeHash;
   private String savePath;
   private YamlConfiguration saveConfig;

   public Gift(Infos infos) {
      super();
      this.infos = infos;
      this.dao = infos.getDao();
      this.server = infos.getServer();
      this.pn = Infos.getPn();
      this.savePath = infos.getPluginPath() + File.separator + this.pn + File.separator + "time.yml";
      this.loadConfig(UtilConfig.getConfig(this.pn));
      infos.getPm().registerEvents(this, infos);
      this.loadData();
   }

   public void onCommand(CommandSender sender, String[] args) {
      try {
         int length = args.length;
         Player p = null;
         if (sender instanceof Player) {
            p = (Player)sender;
         }

         if (length != 1 || !args[0].equalsIgnoreCase("?")) {
            if (length == 0) {
               if (p == null) {
                  sender.sendMessage(UtilFormat.format(this.pn, "fail", new Object[]{this.get(125)}));
               } else {
                  this.info(p, p.getName());
               }

               return;
            }

            if (length == 2) {
               if (args[0].equalsIgnoreCase("reset")) {
                  if (args[1].indexOf(",") == -1) {
                     this.reset(sender, Integer.parseInt(args[1]));
                  } else {
                     String[] var8;
                     for(String s : var8 = args[1].split(",")) {
                        this.reset(sender, Integer.parseInt(s));
                     }
                  }

                  return;
               }

               if (args[0].equalsIgnoreCase("info")) {
                  if (p == null) {
                     sender.sendMessage(UtilFormat.format(this.pn, "fail", new Object[]{this.get(125)}));
                  } else {
                     this.info(p, args[1]);
                  }

                  return;
               }
            } else if (length == 3) {
               if (args[0].equalsIgnoreCase("give")) {
                  this.give(sender, args[1], args[2]);
                  return;
               }

               if (args[0].equalsIgnoreCase("reset")) {
                  if (args[2].indexOf(",") == -1) {
                     this.reset(sender, args[1], Integer.parseInt(args[2]));
                  } else {
                     String[] var13;
                     for(String s : var13 = args[2].split(",")) {
                        this.reset(sender, args[1], Integer.parseInt(s));
                     }
                  }

                  return;
               }
            }
         }

         sender.sendMessage(UtilFormat.format(this.pn, "cmdHelpHeader", new Object[]{this.get(300)}));
         if (p == null || UtilPer.hasPer(p, this.per_infos_admin)) {
            sender.sendMessage(UtilFormat.format(this.pn, "cmdHelpItem", new Object[]{this.get(305), this.get(310)}));
            sender.sendMessage(UtilFormat.format(this.pn, "cmdHelpItem", new Object[]{this.get(370), this.get(375)}));
            sender.sendMessage(UtilFormat.format(this.pn, "cmdHelpItem", new Object[]{this.get(380), this.get(385)}));
            sender.sendMessage(UtilFormat.format(this.pn, "cmdHelpItem", new Object[]{this.get(350), this.get(355)}));
            sender.sendMessage(UtilFormat.format(this.pn, "cmdHelpItem", new Object[]{this.get(360), this.get(365)}));
         }
      } catch (NumberFormatException var9) {
         sender.sendMessage(UtilFormat.format(this.pn, "fail", new Object[]{this.get(35)}));
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

   @EventHandler(
      priority = EventPriority.LOW
   )
   public void onTime(TimeEvent e) {
      if (TimeEvent.getTime() % 60L == 0L) {
         this.check();
      }

   }

   private void loadConfig(FileConfiguration config) {
      this.per_infos_admin = config.getString("per_infos_admin");
      this.per_infos_gift = config.getString("per_infos_gift");
      this.giftHash = new HashMap();

      for(String s : config.getStringList("gift")) {
         int id = Integer.parseInt(s.split(" ")[0]);
         int times = Integer.parseInt(s.split(" ")[1]);
         int interval = Integer.parseInt(s.split(" ")[2]);
         int cost = Integer.parseInt(s.split(" ")[3]);
         boolean add = Boolean.parseBoolean(s.split(" ")[4]);
         String tip = Util.convert(s.split(" ")[5]);
         this.giftHash.put(id, new GiftInfo(id, times, interval, cost, add, tip));
      }

      this.saveConfig = new YamlConfiguration();

      try {
         this.saveConfig.load(this.savePath);
         this.timeHash = new HashMap();

         for(GiftInfo gi : this.giftHash.values()) {
            if (this.saveConfig.contains("" + gi.getId())) {
               this.timeHash.put(gi.getId(), this.saveConfig.getInt("" + gi.getId()));
            }
         }
      } catch (FileNotFoundException e) {
         e.printStackTrace();
      } catch (IOException e) {
         e.printStackTrace();
      } catch (InvalidConfigurationException e) {
         e.printStackTrace();
      }

      for(GiftInfo gi : this.giftHash.values()) {
         int id = gi.getId();
         if (!this.timeHash.containsKey(id)) {
            this.setTime(id, gi.getInterval(), false);
         }
      }

      this.save();
   }

   private void loadData() {
      this.userHash = new HashMap();

      for(GiftUser giftUser : this.infos.getDao().getAllGiftUsers()) {
         this.userHash.put(giftUser.getName(), giftUser);
      }

   }

   private void info(Player p, String tar) {
      tar = Util.getRealName(p, tar);
      if (tar != null) {
         if (p.getName().equals(tar) || UtilPer.checkPer(p, this.per_infos_admin)) {
            p.sendMessage(UtilFormat.format(this.pn, "info1", new Object[]{tar}));
            GiftUser gu = this.checkInit(tar);

            for(GiftInfo gi : this.giftHash.values()) {
               int id = gi.getId();
               int time = this.getTime(id);
               int hour = time / 60;
               int minute = time % 60;
               String add;
               if (gi.isAdd()) {
                  add = this.get(391);
               } else {
                  add = this.get(392);
               }

               String get;
               if (gu.getGiftHash().containsKey(id)) {
                  get = this.get(389);
               } else {
                  get = this.get(390);
               }

               String msg = UtilFormat.format(this.pn, "info2", new Object[]{gi.getId(), hour, minute, gi.getCost(), add, get, gi.getTip()});
               p.sendMessage(msg);
            }

         }
      }
   }

   private void give(CommandSender sender, String tar, String s) {
      if (!(sender instanceof Player) || UtilPer.checkPer((Player)sender, this.per_infos_admin)) {
         Player tarP = this.server.getPlayerExact(tar);
         if (tarP == null) {
            sender.sendMessage(UtilFormat.format(this.pn, "fail", new Object[]{this.get(340)}));
         } else {
            tar = tarP.getName();

            int id;
            try {
               id = Integer.parseInt(s);
            } catch (NumberFormatException var14) {
               sender.sendMessage(UtilFormat.format(this.pn, "fail", new Object[]{this.get(35)}));
               return;
            }

            GiftUser giftUser = this.checkInit(tar);
            if (giftUser.getGiftHash().containsKey(id)) {
               sender.sendMessage(UtilFormat.format(this.pn, "fail", new Object[]{this.get(330)}));
               tarP.sendMessage(UtilFormat.format(this.pn, "fail", new Object[]{this.get(335)}));
            } else {
               GiftInfo giftInfo = this.checkExsit(sender, id);
               PlayerInfo pi = Infos.getPlayerInfoManager().checkInit(tar);
               if (!UtilPer.hasPer(tarP, this.per_infos_gift) && pi.getPower() < giftInfo.getCost()) {
                  tarP.sendMessage(UtilFormat.format(this.pn, "needPower", new Object[]{giftInfo.getCost()}));
               } else {
                  if (!UtilPer.hasPer(tarP, this.per_infos_gift)) {
                     pi.setPower(pi.getPower() - giftInfo.getCost());
                     tarP.sendMessage(UtilFormat.format(this.pn, "costPower", new Object[]{giftInfo.getCost()}));
                  }

                  giftUser.getGiftHash().put(id, true);
                  String tip = this.get(315) + id;
                  HashMap<Integer, ItemStack> itemsHash = new HashMap();

                  for(int i = 0; i < giftInfo.getTimes(); ++i) {
                     ItemStack is = UtilItems.getItem(this.pn, "gift" + id);
                     itemsHash.put(i, is);
                  }

                  if (giftInfo.isAdd()) {
                     PlayerInventory inv = tarP.getInventory();

                     for(ItemStack is : itemsHash.values()) {
                        inv.addItem(new ItemStack[]{is});
                        tarP.sendMessage(UtilFormat.format(this.pn, "getGift", new Object[]{UtilNames.getItemName(is), is.getAmount()}));
                     }

                     tarP.updateInventory();
                  } else {
                     UtilRewards.addRewards(this.pn, (String)null, tar, 0, 0, 0, tip, itemsHash, true);
                     tarP.sendMessage(UtilFormat.format(this.pn, "gift2", new Object[]{id}));
                  }

                  this.dao.addOrUpdateGiftUser(giftUser);
                  sender.sendMessage(UtilFormat.format(this.pn, "gift1", new Object[]{id, tar}));
               }
            }
         }
      }
   }

   private void reset(CommandSender sender, String tar, int id) {
      Player p = null;
      if (sender instanceof Player) {
         p = (Player)sender;
      }

      if (p == null || UtilPer.checkPer(p, this.per_infos_admin)) {
         tar = Util.getRealName(sender, tar);
         if (tar != null) {
            GiftInfo gi = this.checkExsit(sender, id);
            if (gi != null) {
               boolean result = this.reset(tar, id);
               if (result) {
                  Util.sendMsg(tar, UtilFormat.format(this.pn, "resetTip4", new Object[]{id}));
                  sender.sendMessage(UtilFormat.format(this.pn, "resetTip3", new Object[]{tar, id}));
               } else {
                  Util.sendMsg(tar, UtilFormat.format(this.pn, "resetTip5", new Object[]{id}));
                  sender.sendMessage(UtilFormat.format(this.pn, "resetTip6", new Object[]{tar, id}));
               }

            }
         }
      }
   }

   private GiftInfo checkExsit(CommandSender sender, int id) {
      if (this.giftHash.containsKey(id)) {
         return (GiftInfo)this.giftHash.get(id);
      } else {
         if (sender != null) {
            sender.sendMessage(UtilFormat.format(this.pn, "notExsit", new Object[]{id}));
         }

         return null;
      }
   }

   private void reset(CommandSender sender, int id) {
      Player p = null;
      if (sender instanceof Player) {
         p = (Player)sender;
      }

      if (p == null || UtilPer.checkPer(p, this.per_infos_admin)) {
         GiftInfo giftInfo = (GiftInfo)this.giftHash.get(id);
         if (giftInfo == null) {
            sender.sendMessage(UtilFormat.format(this.pn, "fail", new Object[]{this.get(325)}));
         } else {
            this.reset(id);
            sender.sendMessage(UtilFormat.format(this.pn, "resetTip2", new Object[]{id}));
         }
      }
   }

   private GiftUser checkInit(String name) {
      GiftUser giftUser = (GiftUser)this.userHash.get(name);
      if (giftUser == null) {
         giftUser = new GiftUser(name);
         this.userHash.put(name, giftUser);
         this.dao.addOrUpdateGiftUser(giftUser);
      }

      return giftUser;
   }

   private void check() {
      for(GiftInfo gi : this.giftHash.values()) {
         int id = gi.getId();
         if (gi != null) {
            int left = this.getTime(id);
            --left;
            this.setTime(id, left, false);
            if (left <= 0) {
               this.reset(id);
            }
         }
      }

      this.save();
   }

   private void reset(int id) {
      GiftInfo gi = (GiftInfo)this.giftHash.get(id);
      if (gi != null) {
         this.setTime(id, gi.getInterval(), true);

         for(GiftUser gu : this.userHash.values()) {
            gu.getGiftHash().remove(id);
         }

         this.dao.addOrUpdateGiftUsers(this.userHash.values());
         this.server.broadcastMessage(UtilFormat.format(this.pn, "resetTip", new Object[]{id}));
      }

   }

   private boolean reset(String tar, int id) {
      GiftUser gu = this.checkInit(tar);
      if (gu != null && gu.getGiftHash().containsKey(id)) {
         gu.getGiftHash().remove(id);
         this.dao.addOrUpdateGiftUser(gu);
         return true;
      } else {
         return false;
      }
   }

   private int getTime(int id) {
      return this.timeHash.containsKey(id) ? (Integer)this.timeHash.get(id) : -1;
   }

   private void setTime(int id, int time, boolean save) {
      if (this.giftHash.containsKey(id)) {
         this.timeHash.put(id, time);
         this.saveConfig.set("" + id, time);
         if (save) {
            this.save();
         }
      }

   }

   private void save() {
      try {
         this.saveConfig.save(this.savePath);
      } catch (IOException e) {
         e.printStackTrace();
      }

   }

   private String get(int id) {
      return UtilFormat.format(this.pn, id);
   }

   private class GiftInfo {
      private int id;
      private int times;
      private int interval;
      private int cost;
      private boolean add;
      private String tip;

      public GiftInfo(int id, int times, int interval, int cost, boolean add, String tip) {
         super();
         this.id = id;
         this.times = times;
         this.interval = interval;
         this.cost = cost;
         this.add = add;
         this.tip = tip;
      }

      public int getId() {
         return this.id;
      }

      public int getTimes() {
         return this.times;
      }

      public int getCost() {
         return this.cost;
      }

      public int getInterval() {
         return this.interval;
      }

      public boolean isAdd() {
         return this.add;
      }

      public String getTip() {
         return this.tip;
      }
   }
}
