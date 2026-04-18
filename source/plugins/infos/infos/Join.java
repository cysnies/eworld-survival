package infos;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import lib.config.ReloadConfigEvent;
import lib.util.Util;
import lib.util.UtilConfig;
import lib.util.UtilEco;
import lib.util.UtilFormat;
import lib.util.UtilItems;
import lib.util.UtilPer;
import lib.util.UtilRewards;
import org.bukkit.Server;
import org.bukkit.configuration.MemorySection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

public class Join implements Listener {
   private Infos infos;
   private Dao dao;
   private Server server;
   private String pn;
   private int itemId;
   private String check;
   private String joiner;
   private List lore;
   private int cost;
   private JoinInfo free;
   private int max;
   private List cmd;
   private int beJoinRewards;
   private List joinInfoList;
   private ItemStack stone;
   private HashMap userHash;

   public Join(Infos infos) {
      super();
      this.infos = infos;
      this.dao = infos.getDao();
      this.server = infos.getServer();
      this.pn = Infos.getPn();
      this.loadConfig(UtilConfig.getConfig(this.pn));
      infos.getPm().registerEvents(this, infos);
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

   @EventHandler(
      priority = EventPriority.HIGH
   )
   public void onPlayerJoin(PlayerJoinEvent e) {
      PlayerInfo pi = Infos.getPlayerInfoManager().getPlayerInfo(e.getPlayer().getName());
      if (pi != null) {
         JoinUser joinUser = this.checkInit(e.getPlayer().getName());
         if (!joinUser.isFree() && pi.getPlaceNum() >= this.free.placeAmount && pi.getMineNum() >= this.free.mineAmount && pi.getKillMonsterNum() >= this.free.monsterAmount && pi.getOnlineTime() >= this.free.onlineAmount) {
            joinUser.setFree(true);
            this.dao.addOrUpdateJoinUser(joinUser);
            e.getPlayer().sendMessage(UtilFormat.format(this.pn, "success", new Object[]{this.get(445)}));
            this.giveStone(e.getPlayer());
         }

         String tar = joinUser.getJoinUser();
         if (tar != null) {
            for(JoinInfo joinInfo : this.joinInfoList) {
               if (!UtilPer.hasPer(e.getPlayer().getName(), joinInfo.per) && pi.getBreakNum() >= joinInfo.breakAmount && pi.getPlaceNum() >= joinInfo.placeAmount && pi.getMineNum() >= joinInfo.mineAmount && pi.getKillMonsterNum() >= joinInfo.monsterAmount && pi.getOnlineTime() >= joinInfo.onlineAmount) {
                  this.execute(tar, joinInfo.rewards);
                  UtilPer.add(e.getPlayer().getName(), joinInfo.per);
               }
            }
         }

      }
   }

   @EventHandler(
      priority = EventPriority.HIGHEST,
      ignoreCancelled = true
   )
   public void onPlayerInteract(PlayerInteractEvent e) {
      if (e.getAction().equals(Action.RIGHT_CLICK_BLOCK) && e.hasItem()) {
         ItemStack is = e.getItem();
         if (is.getTypeId() == this.itemId) {
            ItemMeta im = is.getItemMeta();
            if (im != null) {
               List<String> lore = im.getLore();
               if (lore != null && lore.size() >= 2 && ((String)lore.get(0)).equals(this.check)) {
                  try {
                     String joiner = ((String)lore.get(1)).split(" ")[1];
                     e.setCancelled(true);
                     if (this.join(e.getPlayer().getName(), joiner)) {
                        if (is.getAmount() > 1) {
                           is.setAmount(is.getAmount() - 1);
                        } else {
                           e.getPlayer().setItemInHand((ItemStack)null);
                        }

                        e.getPlayer().updateInventory();
                     }
                  } catch (Exception var6) {
                  }
               }
            }
         }
      }

   }

   public String getJoin(String name) {
      JoinUser joinUser = this.checkInit(name);
      return joinUser.getJoinUser();
   }

   public int getJoinAmount(String name) {
      JoinUser joinUser = this.checkInit(name);
      return joinUser.getAmount();
   }

   public ItemStack getStone() {
      return this.stone;
   }

   public int getMax() {
      return this.max;
   }

   public void buy(Player p) {
      JoinUser joinUser = this.checkInit(p.getName());
      if (joinUser.getAmount() >= this.max) {
         p.sendMessage(UtilFormat.format(this.pn, "fail", new Object[]{this.get(415)}));
      } else if (UtilEco.get(p.getName()) < (double)this.cost) {
         p.sendMessage(UtilFormat.format(this.pn, "fail", new Object[]{this.get(420)}));
      } else {
         UtilEco.del(p.getName(), (double)this.cost);
         p.sendMessage(UtilFormat.format(this.pn, "delGold", new Object[]{this.cost}));
         this.giveStone(p);
      }
   }

   public void giveStone(Player p) {
      String tip = this.get(425);
      HashMap<Integer, ItemStack> itemsHash = new HashMap();
      ItemStack stone = new ItemStack(this.itemId);
      ItemMeta im = stone.getItemMeta();
      im.setDisplayName(this.get(450));
      List<String> lore = new ArrayList();
      lore.add(this.check);
      lore.add(this.joiner + p.getName());

      for(String s : this.lore) {
         lore.add(s);
      }

      im.setLore(lore);
      stone.setItemMeta(im);
      itemsHash.put(0, stone);
      UtilRewards.addRewards(this.pn, (String)null, p.getName(), 0, 0, 0, tip, itemsHash, true);
      p.sendMessage(UtilFormat.format(this.pn, "success", new Object[]{this.get(430)}));
   }

   private boolean join(String name, String tar) {
      Player p = this.server.getPlayerExact(name);
      if (p != null && p.isOnline()) {
         name = p.getName();
         tar = Util.getRealName(p, tar);
         if (tar == null) {
            return false;
         } else {
            JoinUser joinUser1 = this.checkInit(name);
            if (joinUser1.getJoinUser() != null) {
               p.sendMessage(UtilFormat.format(this.pn, "fail", new Object[]{this.get(400)}));
               return false;
            } else if (this.getJoinAmount(tar) >= this.max) {
               p.sendMessage(UtilFormat.format(this.pn, "fail", new Object[]{this.get(410)}));
               return false;
            } else if (name.equals(tar)) {
               p.sendMessage(UtilFormat.format(this.pn, "fail", new Object[]{this.get(440)}));
               return false;
            } else {
               joinUser1.setJoinUser(tar);
               JoinUser joinUser2 = this.checkInit(tar);
               joinUser2.setAmount(joinUser2.getAmount() + 1);
               this.dao.addOrUpdateJoinUser(joinUser1);
               this.dao.addOrUpdateJoinUser(joinUser2);
               this.execute(name, this.beJoinRewards);
               p.sendMessage(UtilFormat.format(this.pn, "join1", new Object[]{this.beJoinRewards}));
               Player tarP = this.server.getPlayerExact(tar);
               if (tarP != null && tarP.isOnline()) {
                  tarP.sendMessage(UtilFormat.format(this.pn, "join2", new Object[]{name}));
               }

               return true;
            }
         }
      } else {
         return false;
      }
   }

   private void execute(String name, int amount) {
      for(String s : this.cmd) {
         this.server.dispatchCommand(this.server.getConsoleSender(), s.replace("{0}", name).replace("{1}", "" + amount));
      }

   }

   private JoinUser checkInit(String name) {
      JoinUser joinUser = (JoinUser)this.userHash.get(name);
      if (joinUser == null) {
         joinUser = new JoinUser(name);
         this.userHash.put(name, joinUser);
         this.dao.addOrUpdateJoinUser(joinUser);
      }

      return joinUser;
   }

   private void loadConfig(YamlConfiguration config) {
      this.itemId = config.getInt("join.itemId");
      this.check = Util.convert(config.getString("join.check"));
      this.joiner = Util.convert(config.getString("join.joiner"));
      this.lore = new ArrayList();

      for(String s : config.getStringList("join.lore")) {
         this.lore.add(Util.convert(s));
      }

      this.cost = config.getInt("join.cost");
      int placeAmount = config.getInt("join.free.place");
      int mineAmount = config.getInt("join.free.mine");
      int monsterAmount = config.getInt("join.free.monster");
      int onlineAmount = config.getInt("join.free.online");
      this.free = new JoinInfo("", 0, placeAmount, mineAmount, monsterAmount, onlineAmount, 0);
      this.max = config.getInt("join.max");
      this.cmd = config.getStringList("join.cmd");
      this.beJoinRewards = config.getInt("join.rewards.beJoin");
      this.joinInfoList = new ArrayList();
      MemorySection ms = (MemorySection)config.get("join.rewards.join");

      for(String key : ms.getValues(false).keySet()) {
         String per = ms.getString(key + ".per");
         int breakAmount = ms.getInt(key + ".con.break");
         placeAmount = ms.getInt(key + ".con.place");
         mineAmount = ms.getInt(key + ".con.mine");
         monsterAmount = ms.getInt(key + ".con.monster");
         onlineAmount = ms.getInt(key + ".con.online");
         int rewards = ms.getInt(key + ".rewards");
         JoinInfo joinInfo = new JoinInfo(per, breakAmount, placeAmount, mineAmount, monsterAmount, onlineAmount, rewards);
         this.joinInfoList.add(joinInfo);
      }

      this.stone = UtilItems.getItem(this.pn, "set_buyJoin");
      ItemMeta im = this.stone.getItemMeta();
      List<String> lore = im.getLore();
      lore.set(lore.size() - 1, ((String)lore.get(lore.size() - 1)).replace("{0}", "" + this.cost));
      im.setLore(lore);
      this.stone.setItemMeta(im);
   }

   private void loadData() {
      this.userHash = new HashMap();

      for(JoinUser joinUser : this.infos.getDao().getAllJoinUsers()) {
         this.userHash.put(joinUser.getName(), joinUser);
      }

   }

   private String get(int id) {
      return UtilFormat.format(this.pn, id);
   }

   private class JoinInfo {
      String per;
      int breakAmount;
      int placeAmount;
      int mineAmount;
      int monsterAmount;
      int onlineAmount;
      int rewards;

      public JoinInfo(String per, int breakAmount, int placeAmount, int mineAmount, int monsterAmount, int onlineAmount, int rewards) {
         super();
         this.per = per;
         this.breakAmount = breakAmount;
         this.placeAmount = placeAmount;
         this.mineAmount = mineAmount;
         this.monsterAmount = monsterAmount;
         this.onlineAmount = onlineAmount;
         this.rewards = rewards;
      }
   }
}
