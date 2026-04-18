package chat;

import java.util.HashMap;
import lib.config.ReloadConfigEvent;
import lib.util.Util;
import lib.util.UtilConfig;
import lib.util.UtilFormat;
import lib.util.UtilPer;
import org.bukkit.Server;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;

public class BlackList implements Listener {
   private Server server;
   private Dao dao;
   private String pn;
   private String per_chat_black;
   private String per_chat_black_info_other;
   private String per_chat_black_unlimit;
   private boolean addBlackTip;
   private boolean delBlackTip;
   private int maxBlackAmount;
   private HashMap blackHash;

   public BlackList(Chat main) {
      super();
      this.server = main.getServer();
      this.dao = main.getDao();
      this.pn = main.getPn();
      this.loadConfig(UtilConfig.getConfig(this.pn));
      main.getPm().registerEvents(this, main);
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

   public boolean addBlack(Player p, String tar) {
      if (!UtilPer.checkPer(p, this.per_chat_black)) {
         return true;
      } else {
         tar = Util.getRealName(p, tar);
         if (tar == null) {
            return false;
         } else {
            String name = p.getName();
            if (this.isBlack(name, tar)) {
               p.sendMessage(UtilFormat.format(this.pn, "fail", new Object[]{this.get(95)}));
               return false;
            } else if (name.equals(tar)) {
               p.sendMessage(UtilFormat.format(this.pn, "fail", new Object[]{this.get(100)}));
               return false;
            } else if (!UtilPer.hasPer(p, this.per_chat_black_unlimit) && this.getBlackAmount(name) >= this.maxBlackAmount) {
               p.sendMessage(UtilFormat.format(this.pn, "blackLimit", new Object[]{this.maxBlackAmount}));
               return true;
            } else {
               BlackUser blackUser = (BlackUser)this.blackHash.get(name);
               if (blackUser == null) {
                  blackUser = new BlackUser(name);
                  this.blackHash.put(name, blackUser);
               }

               blackUser.getBlackList().add(tar);
               this.dao.addOrUpdateBlackUser(blackUser);
               Chat.getShowManager().addBlack(name, tar);
               p.sendMessage(UtilFormat.format(this.pn, "addBlack1", new Object[]{tar}));
               if (this.addBlackTip && this.server.getPlayer(tar) != null) {
                  this.server.getPlayer(tar).sendMessage(UtilFormat.format(this.pn, "addBlack2", new Object[]{name}));
               }

               return false;
            }
         }
      }
   }

   public void removeBlack(Player p, String tar) {
      if (UtilPer.checkPer(p, this.per_chat_black)) {
         tar = Util.getRealName(p, tar);
         if (tar != null) {
            String name = p.getName();
            if (this.blackHash.containsKey(name) && ((BlackUser)this.blackHash.get(name)).getBlackList().has(tar)) {
               BlackUser blackUser = (BlackUser)this.blackHash.get(name);
               blackUser.getBlackList().remove(tar);
               if (blackUser.getBlackList().size() == 0) {
                  this.blackHash.remove(name);
                  this.dao.removeBlackUser(blackUser);
               } else {
                  this.dao.addOrUpdateBlackUser(blackUser);
               }

               Chat.getShowManager().delBlack(name, tar);
               p.sendMessage(UtilFormat.format(this.pn, "delBlack1", new Object[]{tar}));
               if (this.delBlackTip && this.server.getPlayer(tar) != null) {
                  this.server.getPlayer(tar).sendMessage(UtilFormat.format(this.pn, "delBlack2", new Object[]{name}));
               }

            } else {
               p.sendMessage(UtilFormat.format(this.pn, "fail", new Object[]{this.get(105)}));
            }
         }
      }
   }

   public int getBlackAmount(String name) {
      return !this.blackHash.containsKey(name) ? 0 : ((BlackUser)this.blackHash.get(name)).getBlackList().size();
   }

   public boolean isBlack(String name, String tar) {
      return this.blackHash.get(name) != null && ((BlackUser)this.blackHash.get(name)).getBlackList().has(tar);
   }

   public String getPer_chat_black() {
      return this.per_chat_black;
   }

   public String getPer_chat_black_info_other() {
      return this.per_chat_black_info_other;
   }

   public HashMap getBlackHash() {
      return this.blackHash;
   }

   private void loadData() {
      this.blackHash = new HashMap();

      for(BlackUser blackUser : this.dao.getAllBlackUsers()) {
         this.blackHash.put(blackUser.getName(), blackUser);
      }

   }

   private void loadConfig(YamlConfiguration config) {
      this.per_chat_black = config.getString("per_chat_black");
      this.per_chat_black_info_other = config.getString("per_chat_black_info_other");
      this.per_chat_black_unlimit = config.getString("per_chat_black_unlimit");
      this.addBlackTip = config.getBoolean("addBlackTip");
      this.delBlackTip = config.getBoolean("delBlackTip");
      this.maxBlackAmount = config.getInt("maxBlackAmount");
   }

   private String get(int id) {
      return UtilFormat.format(this.pn, id);
   }
}
