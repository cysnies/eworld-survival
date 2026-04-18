package chat;

import friend.FriendManager;
import infos.Infos;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.util.HashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import land.Land;
import landMain.LandMain;
import lib.config.ReloadConfigEvent;
import lib.hashList.HashList;
import lib.util.Util;
import lib.util.UtilConfig;
import lib.util.UtilEco;
import lib.util.UtilFormat;
import lib.util.UtilNames;
import lib.util.UtilPer;
import org.bukkit.Server;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import town.Main;
import town.TownInfo;
import town.TownUser;

public class Channel implements Listener {
   private Server server;
   private Dao dao;
   private String pn;
   private String per_chat_vip;
   private boolean displayName;
   private int defaultChannel;
   private int nearRange;
   private HashMap nameHash;
   private String nearFormat;
   private String worldFormat;
   private String friendFormat;
   private String townFormat;
   private String landFormat;
   private HashMap channelHash;
   private FriendManager friendManager;
   private ConcurrentLinkedQueue chatTaskList;
   private HashMap cost1Hash;
   private HashMap cost2Hash;
   private HashMap worldFormatHash;
   private String logPath;
   private PrintStream log;

   public Channel(Chat main) {
      super();
      this.server = main.getServer();
      this.dao = main.getDao();
      this.pn = main.getPn();
      this.friendManager = main.getFriendManager();
      this.chatTaskList = new ConcurrentLinkedQueue();
      this.logPath = main.getPluginPath() + File.separator + this.pn + File.separator + "log.txt";

      try {
         File file = new File(this.logPath);
         file.createNewFile();
         this.log = new PrintStream(new FileOutputStream(file, true));
      } catch (FileNotFoundException e) {
         e.printStackTrace();
      } catch (IOException e) {
         e.printStackTrace();
      }

      this.loadData();
      this.loadConfig(UtilConfig.getConfig(this.pn));
      main.getPm().registerEvents(this, main);
      Execute execute = new Execute();
      this.server.getScheduler().scheduleSyncRepeatingTask(main, execute, 1L, 1L);
   }

   public void onDisable() {
      this.log.flush();
      this.log.close();
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
      priority = EventPriority.MONITOR,
      ignoreCancelled = true
   )
   public void onAsyncPlayerChat(AsyncPlayerChatEvent e) {
      e.setCancelled(true);
      this.addChatTask(e.getPlayer(), e.getMessage());
   }

   public void chat(Player p, String message, int channel) {
      if (p != null && p.isOnline()) {
         String name = p.getName();
         ChannelUser channelUser = (ChannelUser)this.channelHash.get(name);
         if (channelUser == null) {
            channelUser = this.initChannel(name, this.defaultChannel);
         }

         if (channel == 0) {
            channel = channelUser.getChannel();
         }

         if (!this.nameHash.containsKey(channel)) {
            p.sendMessage(UtilFormat.format(this.pn, "fail", new Object[]{this.get(130)}));
         } else {
            String needPer = "per.chat.channel." + channel;
            if (UtilPer.checkPer(p, needPer)) {
               String vip = "§m";
               int cost;
               if (UtilPer.hasPer(p, this.per_chat_vip)) {
                  vip = "";
                  cost = (Integer)this.cost2Hash.get(channel);
                  if (cost > 0 && UtilEco.get(name) < (double)cost) {
                     p.sendMessage(UtilFormat.format(this.pn, "goldErr", new Object[]{cost, vip}));
                     return;
                  }
               } else {
                  cost = (Integer)this.cost1Hash.get(channel);
                  if (cost > 0 && UtilEco.get(name) < (double)cost) {
                     p.sendMessage(UtilFormat.format(this.pn, "goldErr", new Object[]{cost, vip}));
                     return;
                  }
               }

               if (cost > 0) {
                  UtilEco.del(name, (double)cost);
                  p.sendMessage(UtilFormat.format(this.pn, "delGold2", new Object[]{cost, vip}));
               }

               String worldName = p.getWorld().getName();
               String world = UtilNames.getWorldName(worldName);
               if (this.worldFormatHash.containsKey(worldName)) {
                  world = ((String)this.worldFormatHash.get(worldName)).replace("{0}", world);
               }

               String msg = this.getMsg(channel, p, message, world);
               switch (channel) {
                  case 1:
                     int hear = 0;

                     for(Player tar : p.getWorld().getPlayers()) {
                        int distance = (int)p.getLocation().distance(tar.getLocation());
                        if (!Chat.getBlackList().isBlack(tar.getName(), name) && distance <= this.nearRange) {
                           ++hear;
                           msg = msg.replace("<distance>", String.valueOf(distance));
                           tar.sendMessage(msg);
                        }
                     }

                     if (hear <= 1) {
                        p.sendMessage(this.get(145));
                     }
                     break;
                  case 2:
                     int hear = 0;

                     Player[] var34;
                     for(Player tar : var34 = this.server.getOnlinePlayers()) {
                        if (!Chat.getBlackList().isBlack(tar.getName(), name)) {
                           ++hear;
                           tar.sendMessage(msg);
                        }
                     }

                     if (hear <= 1) {
                        p.sendMessage(this.get(150));
                     }
                     break;
                  case 3:
                     int hear = 0;
                     p.sendMessage(msg);

                     for(String tar : this.friendManager.getFriendList(name)) {
                        Player tarPlayer = this.server.getPlayer(tar);
                        if (tarPlayer != null && !Chat.getBlackList().isBlack(tar, name)) {
                           ++hear;
                           tarPlayer.sendMessage(msg);
                        }
                     }

                     if (hear <= 0) {
                        p.sendMessage(this.get(160));
                     }
                     break;
                  case 4:
                     int hear = 0;
                     TownUser tu = Main.getTownManager().checkInit(p);
                     if (tu.getTownId() == -1L) {
                        p.sendMessage(UtilFormat.format(this.pn, "fail", new Object[]{this.get(205)}));
                     } else {
                        p.sendMessage(msg);
                        TownInfo ti = (TownInfo)Main.getTownManager().getTownHash().get(tu.getTownId());
                        if (ti == null) {
                           return;
                        }

                        for(String tar : ti.getUserHash().keySet()) {
                           Player tarPlayer = this.server.getPlayer(tar);
                           if (!tar.equals(p.getName()) && tarPlayer != null && !Chat.getBlackList().isBlack(tar, name)) {
                              ++hear;
                              tarPlayer.sendMessage(msg);
                           }
                        }

                        if (hear <= 0) {
                           p.sendMessage(this.get(210));
                        }
                     }
                     break;
                  case 5:
                     int hear = 0;
                     Land land = LandMain.getLandManager().getHighestPriorityLand(p.getLocation());
                     if (land == null) {
                        p.sendMessage(UtilFormat.format(this.pn, "fail", new Object[]{this.get(220)}));
                     } else {
                        msg = msg.replace("<land>", land.getName());
                        HashList<Player> list = LandMain.getLandManager().getInHandler().getPlayers(land.getId());
                        if (list != null) {
                           for(Player tar : list) {
                              tar.sendMessage(msg);
                           }

                           hear = list.size() - 1;
                        }

                        if (hear <= 0) {
                           p.sendMessage(this.get(225));
                        }
                     }
               }

               this.log(msg);
            }
         }
      }
   }

   public void toggleChannel(Player p) {
      String name = p.getName();
      ChannelUser user = this.checkInit(name);
      int channel = user.getChannel() + 1;
      if (!this.nameHash.containsKey(channel)) {
         channel = 1;
      }

      user.setChannel(channel);
      this.dao.addOrUpdateChannelUser(user);
      p.sendMessage(UtilFormat.format(this.pn, "joinChannel", new Object[]{this.nameHash.get(channel)}));
   }

   public void toggleChannel(Player p, int id) {
      if (!this.nameHash.containsKey(id)) {
         p.sendMessage(UtilFormat.format(this.pn, "fail", new Object[]{this.get(185)}));
      } else {
         String name = p.getName();
         ChannelUser cu = (ChannelUser)this.channelHash.get(name);
         if (cu != null && cu.getChannel() == id) {
            p.sendMessage(UtilFormat.format(this.pn, "alreadyIn", new Object[]{this.nameHash.get(id)}));
         } else {
            cu = this.checkInit(name);
            cu.setChannel(id);
            this.dao.addOrUpdateChannelUser(cu);
            p.sendMessage(UtilFormat.format(this.pn, "joinChannel", new Object[]{this.nameHash.get(id)}));
         }
      }
   }

   public String getChannelShow(String name) {
      ChannelUser user = this.checkInit(name);
      int channel = user.getChannel();
      String show = (String)this.nameHash.get(channel);
      return show == null ? this.get(170) : show;
   }

   private void log(String msg) {
      msg = "[" + Util.getDate() + "] " + msg.replaceAll("§[0123456789abcdefklmno]", "") + "\n";
      this.log.append(msg);
   }

   private ChannelUser checkInit(String name) {
      ChannelUser cu = (ChannelUser)this.channelHash.get(name);
      if (cu == null) {
         cu = new ChannelUser(name, this.defaultChannel);
         this.channelHash.put(cu.getName(), cu);
         this.dao.addOrUpdateChannelUser(cu);
      }

      return cu;
   }

   private void addChatTask(Player p, String message) {
      try {
         ChatInfo chatInfo = new ChatInfo(p, message);
         this.chatTaskList.add(chatInfo);
      } catch (Exception e) {
         e.printStackTrace();
         System.out.println(">>>>>>>>>>>>异常位置1");
      }

   }

   private void checkChatTask() {
      try {
         ChatInfo chatInfo = (ChatInfo)this.chatTaskList.poll();
         if (chatInfo != null && chatInfo.getP().isOnline()) {
            ChannelUser cu = this.checkInit(chatInfo.getP().getName());
            this.chat(chatInfo.getP(), chatInfo.getMessage(), cu.getChannel());
         }
      } catch (Exception e) {
         e.printStackTrace();
         System.out.println(">>>>>>>>>>>>异常位置2");
      }

   }

   private void loadData() {
      this.channelHash = new HashMap();

      for(ChannelUser channelUser : this.dao.getAllChannelUsers()) {
         this.channelHash.put(channelUser.getName(), channelUser);
      }

   }

   private void loadConfig(YamlConfiguration config) {
      this.per_chat_vip = config.getString("per_chat_vip");
      this.displayName = config.getBoolean("displayName");
      this.defaultChannel = config.getInt("channel.defaultChannel");
      this.nearRange = config.getInt("channel.near.range");
      this.nameHash = new HashMap();
      this.nameHash.put(1, Util.convert(config.getString("channel.near.name")));
      this.nameHash.put(2, Util.convert(config.getString("channel.world.name")));
      this.nameHash.put(3, Util.convert(config.getString("channel.friend.name")));
      this.nameHash.put(4, Util.convert(config.getString("channel.town.name")));
      this.nameHash.put(5, Util.convert(config.getString("channel.land.name")));
      this.nearFormat = Util.convert(config.getString("channel.near.format"));
      this.worldFormat = Util.convert(config.getString("channel.world.format"));
      this.friendFormat = Util.convert(config.getString("channel.friend.format"));
      this.townFormat = Util.convert(config.getString("channel.town.format"));
      this.landFormat = Util.convert(config.getString("channel.land.format"));
      this.cost1Hash = new HashMap();
      this.cost2Hash = new HashMap();
      this.cost1Hash.put(1, config.getInt("costs.c1"));
      this.cost1Hash.put(2, config.getInt("costs.c2"));
      this.cost1Hash.put(3, config.getInt("costs.c3"));
      this.cost1Hash.put(4, config.getInt("costs.c4"));
      this.cost1Hash.put(5, config.getInt("costs.c5"));
      this.cost2Hash.put(1, config.getInt("costs.c1vip"));
      this.cost2Hash.put(2, config.getInt("costs.c2vip"));
      this.cost2Hash.put(3, config.getInt("costs.c3vip"));
      this.cost2Hash.put(4, config.getInt("costs.c4vip"));
      this.cost2Hash.put(5, config.getInt("costs.c5vip"));
      this.worldFormatHash = new HashMap();

      for(String s : config.getStringList("channel.worlds")) {
         this.worldFormatHash.put(s.split(" ")[0], Util.convert(s.split(" ")[1]));
      }

   }

   private String getMsg(int channel, Player p, String msg, String world) {
      String result = null;
      String name;
      if (this.displayName) {
         name = p.getDisplayName();
      } else {
         name = p.getName();
      }

      switch (channel) {
         case 1:
            result = this.nearFormat;
            break;
         case 2:
            result = this.worldFormat;
            break;
         case 3:
            result = this.friendFormat;
            break;
         case 4:
            result = this.townFormat;
            break;
         case 5:
            result = this.landFormat;
      }

      result = result.replace("<name>", name);
      result = result.replace("<world>", world);
      result = result.replace("<msg>", msg);
      result = result.replace("<sex>", Infos.getPlayerInfoManager().getSexXqShow(p));
      return result;
   }

   private ChannelUser initChannel(String name, int channel) {
      ChannelUser channelUser = new ChannelUser(name, channel);
      this.channelHash.put(name, channelUser);
      this.dao.addOrUpdateChannelUser(channelUser);
      return channelUser;
   }

   private String get(int id) {
      return UtilFormat.format(this.pn, id);
   }

   class ChatInfo {
      private Player p;
      private String message;

      public ChatInfo(Player p, String message) {
         super();
         this.p = p;
         this.message = message;
      }

      public Player getP() {
         return this.p;
      }

      public String getMessage() {
         return this.message;
      }
   }

   class Execute implements Runnable {
      Execute() {
         super();
      }

      public void run() {
         Channel.this.checkChatTask();
      }
   }
}
