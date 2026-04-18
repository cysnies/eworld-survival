package friend;

import java.util.HashMap;
import lib.config.ReloadConfigEvent;
import lib.hashList.HashList;
import lib.hashList.HashListImpl;
import lib.tab.Tab;
import lib.types.InvalidTypeException;
import lib.util.Util;
import lib.util.UtilConfig;
import lib.util.UtilCosts;
import lib.util.UtilFormat;
import lib.util.UtilPer;
import lib.util.UtilTab;
import org.bukkit.Bukkit;
import org.bukkit.Server;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

public class FriendManager implements Listener {
   private static final String LIB = "lib";
   private static final String FRIEND = "friend";
   private Main main;
   private String pn;
   private Server server;
   private Dao dao;
   private HashMap friendHash;
   private String per_friend_use;
   private String per_friend_info_other;
   private int defaultLimit;
   private boolean addTip;
   private boolean removeTip;
   private int buyLimitAmount;
   private boolean tipShowName;
   private String pre;
   private String online;
   private String offline;
   private HashMap playerHash;

   public FriendManager(Main main) {
      super();
      this.main = main;
      this.pn = main.getPn();
      this.server = main.getServer();
      this.dao = main.getDao();
      this.loadData(main.getDao());
      this.loadConfig(UtilConfig.getConfig(this.pn));
      main.getPm().registerEvents(this, main);
      UtilTab.register("friend");
      this.playerHash = UtilTab.getMode("friend");
   }

   @EventHandler(
      priority = EventPriority.LOW
   )
   public void reloadConfig(ReloadConfigEvent e) {
      if (e.getCallPlugin().equals(this.pn)) {
         this.loadConfig(e.getConfig());
      }

   }

   @EventHandler(
      priority = EventPriority.LOW
   )
   public void onPlayerJoin(PlayerJoinEvent e) {
      this.checkInitPlayer(e.getPlayer().getName());
      this.initTab(e.getPlayer());
      this.tipOnline(e.getPlayer());
   }

   @EventHandler(
      priority = EventPriority.LOW
   )
   public void onPlayerQuit(PlayerQuitEvent e) {
      this.tipOffline(e.getPlayer());
      this.checkTabLeave(e.getPlayer());
   }

   public boolean add(Player p, String tar) {
      if (!UtilPer.checkPer(p, this.per_friend_use)) {
         return true;
      } else {
         tar = Util.getRealName(p, tar);
         if (tar == null) {
            return false;
         } else if (this.isFriend(p.getName(), tar)) {
            p.sendMessage(UtilFormat.format(this.pn, "isAlreadyFriend", new Object[]{tar}));
            return false;
         } else {
            Friend friend = (Friend)this.friendHash.get(p.getName());
            if (friend == null) {
               friend = this.checkInitPlayer(p.getName());
            }

            if (p.getName().equals(tar)) {
               p.sendMessage(UtilFormat.format("lib", "fail", new Object[]{this.get(125)}));
               return false;
            } else if (friend.getFriendList().size() >= friend.getLimits()) {
               p.sendMessage(UtilFormat.format(this.pn, "reachLimit", new Object[]{friend.getFriendList().size(), friend.getLimits()}));
               return true;
            } else {
               friend.getFriendList().add(tar);
               this.dao.addOrUpdateFriend(friend);
               this.main.getShowManager().addFriend(p.getName(), tar);
               this.updateTab(p, tar, false, false);
               p.sendMessage(UtilFormat.format(this.pn, "addFriend", new Object[]{tar}));
               if (this.addTip) {
                  Player tarP = this.server.getPlayer(tar);
                  if (tarP != null && tarP.isOnline()) {
                     tarP.sendMessage(UtilFormat.format(this.pn, "addFriend2", new Object[]{p.getName()}));
                  }
               }

               return false;
            }
         }
      }
   }

   public void remove(Player p, String tar) {
      if (UtilPer.checkPer(p, this.per_friend_use)) {
         tar = Util.getRealName(p, tar);
         if (tar != null) {
            if (!this.isFriend(p.getName(), tar)) {
               p.sendMessage(UtilFormat.format(this.pn, "isNotFriend", new Object[]{tar}));
            } else {
               Friend friend = (Friend)this.friendHash.get(p.getName());
               if (friend == null) {
                  friend = this.checkInitPlayer(p.getName());
               }

               friend.getFriendList().remove(tar);
               this.dao.addOrUpdateFriend(friend);
               this.main.getShowManager().delFriend(p.getName(), tar);
               this.updateTab(p, tar, false, true);
               p.sendMessage(UtilFormat.format(this.pn, "removeFriend", new Object[]{tar}));
               if (this.removeTip) {
                  Player tarP = this.server.getPlayer(tar);
                  if (tarP != null && tarP.isOnline()) {
                     tarP.sendMessage(UtilFormat.format(this.pn, "removeFriend2", new Object[]{p.getName()}));
                  }
               }

            }
         }
      }
   }

   public void showList(CommandSender sender, String tar) {
      if (!(sender instanceof Player) || UtilPer.checkPer((Player)sender, this.per_friend_use)) {
         tar = Util.getRealName(sender, tar);
         if (tar != null) {
            if (sender instanceof Player) {
               Player p = (Player)sender;
               if (!p.getName().equals(tar) && !UtilPer.hasPer(p, this.per_friend_info_other)) {
                  sender.sendMessage(UtilFormat.format(this.pn, "fail", new Object[]{this.get(165)}));
                  return;
               }
            }

            Friend friend = (Friend)this.friendHash.get(tar);
            if (friend == null) {
               friend = this.checkInitPlayer(tar);
            }

            int now = friend.getFriendList().size();
            int max = friend.getLimits();
            sender.sendMessage(UtilFormat.format(this.pn, "showFriend", new Object[]{tar, now, max}));
            String result = "";
            boolean first = true;

            for(String s : friend.getFriendList()) {
               if (first) {
                  first = false;
               } else {
                  result = result + ",";
               }

               result = result + s;
            }

            sender.sendMessage(result);
         }
      }
   }

   public void showTipStatus(Player p) {
      if (UtilPer.checkPer(p, this.per_friend_use)) {
         Friend friend = (Friend)this.friendHash.get(p.getName());
         if (friend == null) {
            friend = this.checkInitPlayer(p.getName());
         }

         String status;
         if (friend.isTip()) {
            status = this.get(85);
         } else {
            status = this.get(90);
         }

         p.sendMessage(UtilFormat.format(this.pn, "showStatus", new Object[]{status}));
      }
   }

   public void triggerTip(Player p, String s) {
      if (UtilPer.checkPer(p, this.per_friend_use)) {
         Friend friend = (Friend)this.friendHash.get(p.getName());
         if (friend == null) {
            friend = this.checkInitPlayer(p.getName());
         }

         if (s.equalsIgnoreCase("on")) {
            if (friend.isTip()) {
               p.sendMessage(UtilFormat.format("lib", "fail", new Object[]{this.get(105)}));
            } else {
               friend.setTip(true);
               this.dao.addOrUpdateFriend(friend);
               p.sendMessage(UtilFormat.format("lib", "success", new Object[]{this.get(115)}));
            }
         } else if (s.equalsIgnoreCase("off")) {
            if (!friend.isTip()) {
               p.sendMessage(UtilFormat.format("lib", "fail", new Object[]{this.get(110)}));
            } else {
               friend.setTip(false);
               this.dao.addOrUpdateFriend(friend);
               p.sendMessage(UtilFormat.format("lib", "success", new Object[]{this.get(120)}));
            }
         } else {
            p.sendMessage(UtilFormat.format("lib", "fail", new Object[]{this.get(100)}));
         }

      }
   }

   public void buyLimit(Player p) {
      try {
         if (!UtilPer.checkPer(p, this.per_friend_use)) {
            return;
         }

         if (!UtilCosts.cost(p, this.pn, "buyCost", false)) {
            return;
         }

         Friend friend = (Friend)this.friendHash.get(p.getName());
         if (friend == null) {
            friend = this.checkInitPlayer(p.getName());
         }

         friend.setLimits(friend.getLimits() + this.buyLimitAmount);
         this.dao.addOrUpdateFriend(friend);
         p.sendMessage(UtilFormat.format(this.pn, "buyLimit", new Object[]{this.buyLimitAmount, friend.getLimits()}));
      } catch (InvalidTypeException e) {
         e.printStackTrace();
      }

   }

   public boolean hasFriend(String name) {
      Friend friend = (Friend)this.friendHash.get(name);
      if (friend == null) {
         friend = this.checkInitPlayer(name);
      }

      return !friend.getFriendList().isEmpty();
   }

   public HashList getFriendList(String name) {
      Friend friend = (Friend)this.friendHash.get(name);
      if (friend == null) {
         friend = this.checkInitPlayer(name);
      }

      return friend.getFriendList();
   }

   public boolean isFriend(String name, String tar) {
      Friend friend = (Friend)this.friendHash.get(name);
      if (friend == null) {
         friend = this.checkInitPlayer(name);
      }

      return friend.getFriendList().has(tar);
   }

   public int getBuyLimitAmount() {
      return this.buyLimitAmount;
   }

   public HashMap getAllFriends() {
      return this.friendHash;
   }

   private void initTab(Player p) {
      Tab.Mode mode = new Tab.Mode();
      this.playerHash.put(p, mode);
      Friend f = this.checkInitPlayer(p.getName());

      for(String name : f.getFriendList()) {
         this.updateTab(p, name, false, false);
      }

      Player[] var7;
      for(Player tar : var7 = Bukkit.getOnlinePlayers()) {
         if (!p.getName().equals(tar.getName())) {
            Friend tarF = this.checkInitPlayer(tar.getName());
            if (tarF.getFriendList().has(p.getName())) {
               this.updateTab(tar, p.getName(), false, false);
            }
         }
      }

   }

   private void updateTab(Player p, String tar, boolean forceOffline, boolean delete) {
      Tab.Mode mode = (Tab.Mode)this.playerHash.get(p);
      if (mode != null) {
         String tabName = this.getTabName(tar, forceOffline);
         if (delete) {
            mode.remove(mode.getPos(tar));
         } else if (mode.getShow(tar) == null) {
            mode.add(tar, tabName);
         } else {
            mode.set(tar, tabName);
         }
      }

   }

   private String getTabName(String name, boolean forceOffline) {
      Player p = Bukkit.getPlayerExact(name);
      String status;
      if (!forceOffline && p != null && p.isOnline()) {
         status = this.online;
      } else {
         status = this.offline;
      }

      String result = this.pre + status + name;
      return result.substring(0, Math.min(16, result.length()));
   }

   private void checkTabLeave(Player p) {
      this.playerHash.remove(p);

      Player[] var5;
      for(Player tar : var5 = Bukkit.getOnlinePlayers()) {
         if (!p.getName().equals(tar.getName())) {
            Friend tarF = this.checkInitPlayer(tar.getName());
            if (tarF.getFriendList().has(p.getName())) {
               this.updateTab(tar, p.getName(), true, false);
            }
         }
      }

   }

   private void loadData(Dao dao) {
      this.friendHash = new HashMap();

      for(Friend friend : dao.getAllFriends()) {
         this.friendHash.put(friend.getName(), friend);
      }

   }

   private void loadConfig(FileConfiguration config) {
      this.per_friend_use = config.getString("per_friend_use");
      this.per_friend_info_other = config.getString("per_friend_info_other");
      this.defaultLimit = config.getInt("defaultLimit");
      this.addTip = config.getBoolean("tip.add");
      this.removeTip = config.getBoolean("tip.remove");
      this.buyLimitAmount = config.getInt("buyLimitAmount");
      this.tipShowName = config.getBoolean("tipShowName");
      this.pre = Util.convert(config.getString("pre"));
      this.online = Util.convert(config.getString("online"));
      this.offline = Util.convert(config.getString("offline"));
   }

   private void tipOnline(Player p) {
      String show;
      if (this.tipShowName) {
         show = p.getDisplayName();
      } else {
         show = p.getName();
      }

      String msg = UtilFormat.format(this.pn, "onlineTip", new Object[]{show});
      String name = p.getName();

      Player[] var8;
      for(Player pp : var8 = this.server.getOnlinePlayers()) {
         try {
            if (((Friend)this.friendHash.get(pp.getName())).getFriendList().has(name) && ((Friend)this.friendHash.get(pp.getName())).isTip()) {
               pp.sendMessage(msg);
            }
         } catch (Exception var10) {
         }
      }

   }

   private void tipOffline(Player p) {
      String show;
      if (this.tipShowName) {
         show = p.getDisplayName();
      } else {
         show = p.getName();
      }

      String msg = UtilFormat.format(this.pn, "offlineTip", new Object[]{show});
      String name = p.getName();

      Player[] var8;
      for(Player pp : var8 = this.server.getOnlinePlayers()) {
         try {
            if (((Friend)this.friendHash.get(pp.getName())).getFriendList().has(name) && ((Friend)this.friendHash.get(pp.getName())).isTip()) {
               pp.sendMessage(msg);
            }
         } catch (Exception var10) {
         }
      }

   }

   private Friend checkInitPlayer(String name) {
      if (!this.friendHash.containsKey(name)) {
         Friend friend = new Friend(name, true, this.defaultLimit, new HashListImpl());
         this.friendHash.put(name, friend);
         this.dao.addOrUpdateFriend(friend);
      }

      return (Friend)this.friendHash.get(name);
   }

   private String get(int id) {
      return UtilFormat.format(this.pn, id);
   }
}
