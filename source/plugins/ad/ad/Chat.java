package ad;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Random;
import lib.config.ReloadConfigEvent;
import lib.hashList.ChanceHashList;
import lib.hashList.ChanceHashListImpl;
import lib.time.TimeEvent;
import lib.util.Util;
import lib.util.UtilConfig;
import lib.util.UtilEco;
import lib.util.UtilFormat;
import lib.util.UtilPer;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import ticket.Ticket;

public class Chat implements Listener {
   private static final Random r = new Random();
   private Ticket t;
   private String per_ad_remove;
   private String per_ad_chat_create;
   private int pageSize;
   private int check;
   private int chance;
   private int gold;
   private int ticket;
   private int maxLine;
   private int max;
   private HashMap userHash;
   private ChanceHashList userList;

   public Chat(Ad ad) {
      super();
      this.loadConfig(UtilConfig.getConfig(Ad.getPn()));
      ad.getPm().registerEvents(this, ad);
      this.load();
   }

   @EventHandler(
      priority = EventPriority.LOW
   )
   public void onReloadConfig(ReloadConfigEvent e) {
      if (e.getCallPlugin().equals(Ad.getPn())) {
         this.loadConfig(e.getConfig());
      }

   }

   @EventHandler(
      priority = EventPriority.LOW
   )
   public void onTime(TimeEvent e) {
      if (TimeEvent.getTime() % (long)this.check == 0L && r.nextInt(100) < this.chance && !this.userList.isEmpty()) {
         AdChatUser adChatUser = (AdChatUser)this.userList.getRandom();
         this.show(adChatUser);
      }

   }

   public void create(Player p) {
      if (UtilPer.checkPer(p, this.per_ad_chat_create)) {
         AdChatUser adChatUser = (AdChatUser)this.userHash.get(p.getName());
         if (adChatUser != null) {
            p.sendMessage(UtilFormat.format(Ad.getPn(), "fail", new Object[]{this.get(390)}));
         } else {
            adChatUser = new AdChatUser(p.getName());
            Ad.getDao().addOrUpdateAdChatUser(adChatUser);
            this.userHash.put(p.getName(), adChatUser);
            this.userList.addChance(adChatUser, adChatUser.getCost());
            p.sendMessage(UtilFormat.format(Ad.getPn(), "success", new Object[]{this.get(395)}));
         }
      }
   }

   public void showList(CommandSender sender, int page) {
      int maxPage = this.userList.getMaxPage(this.pageSize);
      if (maxPage <= 0) {
         sender.sendMessage(UtilFormat.format(Ad.getPn(), "fail", new Object[]{this.get(350)}));
      } else {
         if (page < 1) {
            page = 1;
         } else if (page > maxPage) {
            page = maxPage;
         }

         List<AdChatUser> list = this.userList.getPage(page, this.pageSize);
         sender.sendMessage(UtilFormat.format(Ad.getPn(), "listHeader", new Object[]{this.get(355), page, maxPage}));

         for(AdChatUser adChatUser : list) {
            sender.sendMessage(UtilFormat.format(Ad.getPn(), "listItem", new Object[]{UtilFormat.format(Ad.getPn(), "info5", new Object[]{adChatUser.getName(), adChatUser.getCost(), adChatUser.getCount()})}));
         }

         sender.sendMessage(this.get(360));
         sender.sendMessage(this.get(435));
      }
   }

   public void showInfo(CommandSender sender, String tar) {
      tar = Util.getRealName(sender, tar);
      if (tar != null) {
         AdChatUser adChatUser = (AdChatUser)this.userHash.get(tar);
         if (adChatUser == null) {
            sender.sendMessage(UtilFormat.format(Ad.getPn(), "err1", new Object[]{tar}));
         } else {
            sender.sendMessage(UtilFormat.format(Ad.getPn(), "show1", new Object[]{adChatUser.getCount()}));
            sender.sendMessage(UtilFormat.format(Ad.getPn(), "add1", new Object[]{adChatUser.getName()}));

            for(String s : adChatUser.getMsg()) {
               sender.sendMessage(s);
            }

            sender.sendMessage(UtilFormat.format(Ad.getPn(), "add2", new Object[]{adChatUser.getCost()}));
         }
      }
   }

   public void del(CommandSender sender, String tar) {
      if (!(sender instanceof Player) || UtilPer.checkPer((Player)sender, this.per_ad_remove)) {
         tar = Util.getRealName(sender, tar);
         if (tar != null) {
            AdChatUser adChatUser = (AdChatUser)this.userHash.get(tar);
            if (adChatUser == null) {
               sender.sendMessage(UtilFormat.format(Ad.getPn(), "err1", new Object[]{tar}));
            } else {
               adChatUser.setMsg(new ArrayList());
               Ad.getDao().addOrUpdateAdChatUser(adChatUser);
               sender.sendMessage(UtilFormat.format(Ad.getPn(), "end1", new Object[]{tar}));
               String from;
               if (sender instanceof Player) {
                  from = ((Player)sender).getName();
               } else {
                  from = this.get(375);
               }

               Util.sendMsg(tar, UtilFormat.format(Ad.getPn(), "end2", new Object[]{from}));
            }
         }
      }
   }

   public void clear(Player p, int line) {
      if (line < 1) {
         line = 1;
      } else if (line > this.maxLine) {
         line = this.maxLine;
      }

      AdChatUser adChatUser = (AdChatUser)this.userHash.get(p.getName());
      if (adChatUser == null) {
         p.sendMessage(UtilFormat.format(Ad.getPn(), "fail", new Object[]{this.get(400)}));
      } else {
         List<String> msg = adChatUser.getMsg();
         if (msg.size() >= line && !((String)msg.get(line - 1)).isEmpty()) {
            msg.set(line - 1, "");
            Ad.getDao().addOrUpdateAdChatUser(adChatUser);
            p.sendMessage(UtilFormat.format(Ad.getPn(), "success", new Object[]{this.get(405)}));
         } else {
            p.sendMessage(UtilFormat.format(Ad.getPn(), "fail", new Object[]{this.get(410)}));
         }
      }
   }

   public void set(Player p, int line, String content) {
      if (UtilPer.checkPer(p, this.per_ad_chat_create)) {
         AdChatUser adChatUser = (AdChatUser)this.userHash.get(p.getName());
         if (adChatUser == null) {
            p.sendMessage(UtilFormat.format(Ad.getPn(), "fail", new Object[]{this.get(400)}));
         } else {
            if (line < 1) {
               line = 1;
            } else if (line > this.maxLine) {
               line = this.maxLine;
            }

            content = Util.convert(content);
            content = content.substring(0, Math.min(content.length(), this.max));
            if (adChatUser.getMsg().size() < line) {
               int end = line - adChatUser.getMsg().size();

               for(int i = 0; i < end; ++i) {
                  adChatUser.getMsg().add("");
               }
            }

            adChatUser.getMsg().set(line - 1, content);
            Ad.getDao().addOrUpdateAdChatUser(adChatUser);
            p.sendMessage(UtilFormat.format(Ad.getPn(), "success", new Object[]{this.get(415)}));
         }
      }
   }

   public void addGold(Player p, int amount) {
      if (UtilPer.checkPer(p, this.per_ad_chat_create)) {
         AdChatUser adChatUser = (AdChatUser)this.userHash.get(p.getName());
         if (adChatUser == null) {
            p.sendMessage(UtilFormat.format(Ad.getPn(), "fail", new Object[]{this.get(400)}));
         } else {
            int has = (int)UtilEco.get(p.getName());
            if (amount > has) {
               amount = has;
            }

            if (amount <= 0) {
               p.sendMessage(UtilFormat.format(Ad.getPn(), "fail", new Object[]{this.get(420)}));
            } else {
               if (UtilEco.del(p.getName(), (double)amount)) {
                  int add = amount * this.gold;
                  adChatUser.setCost(adChatUser.getCost() + add);
                  this.userList.setChance(adChatUser, adChatUser.getCost());
                  Ad.getDao().addOrUpdateAdChatUser(adChatUser);
                  p.sendMessage(UtilFormat.format(Ad.getPn(), "end3", new Object[]{amount, add, adChatUser.getCost()}));
               }

            }
         }
      }
   }

   public void addTicket(Player p, int amount) {
      if (UtilPer.checkPer(p, this.per_ad_chat_create)) {
         AdChatUser adChatUser = (AdChatUser)this.userHash.get(p.getName());
         if (adChatUser == null) {
            p.sendMessage(UtilFormat.format(Ad.getPn(), "fail", new Object[]{this.get(400)}));
         } else {
            int has = Ticket.getTicket(p.getName());
            if (amount > has) {
               amount = has;
            }

            if (amount <= 0) {
               p.sendMessage(UtilFormat.format(Ad.getPn(), "fail", new Object[]{this.get(425)}));
            } else {
               this.checkInitTicket();
               if (this.t.del(Bukkit.getConsoleSender(), p.getName(), amount, Ad.getPn(), this.get(430))) {
                  int add = amount * this.ticket;
                  adChatUser.setCost(adChatUser.getCost() + add);
                  this.userList.setChance(adChatUser, adChatUser.getCost());
                  Ad.getDao().addOrUpdateAdChatUser(adChatUser);
                  p.sendMessage(UtilFormat.format(Ad.getPn(), "end4", new Object[]{amount, add, adChatUser.getCost()}));
               }

            }
         }
      }
   }

   public int getGold() {
      return this.gold;
   }

   public int getTicket() {
      return this.ticket;
   }

   public int getMaxLine() {
      return this.maxLine;
   }

   private void checkInitTicket() {
      if (this.t == null) {
         this.t = (Ticket)Bukkit.getPluginManager().getPlugin("ticket");
      }

   }

   private void show(AdChatUser adChatUser) {
      adChatUser.setCount(adChatUser.getCount() + 1);
      Ad.getDao().addOrUpdateAdChatUser(adChatUser);
      if (!adChatUser.isEmpty()) {
         Bukkit.broadcastMessage(UtilFormat.format(Ad.getPn(), "add1", new Object[]{adChatUser.getName()}));

         for(String s : adChatUser.getMsg()) {
            Bukkit.broadcastMessage(s);
         }

         Bukkit.broadcastMessage(UtilFormat.format(Ad.getPn(), "add2", new Object[]{adChatUser.getCost()}));
      }
   }

   private void load() {
      this.userHash = new HashMap();
      this.userList = new ChanceHashListImpl();

      for(AdChatUser adChatUser : Ad.getDao().getAllAdChats()) {
         this.userHash.put(adChatUser.getName(), adChatUser);
         this.userList.addChance(adChatUser, adChatUser.getCost());
      }

   }

   private void loadConfig(YamlConfiguration config) {
      this.per_ad_remove = config.getString("per_ad_remove");
      this.per_ad_chat_create = config.getString("per_ad_chat_create");
      this.pageSize = config.getInt("chat.pageSize");
      this.gold = config.getInt("chat.gold");
      this.ticket = config.getInt("chat.ticket");
      this.maxLine = config.getInt("chat.maxLine");
      this.max = config.getInt("chat.max");
      this.check = config.getInt("chat.check");
      this.chance = config.getInt("chat.chance");
   }

   private String get(int id) {
      return UtilFormat.format(Ad.getPn(), id);
   }
}
