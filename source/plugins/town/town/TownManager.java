package town;

import infos.Infos;
import infos.PlayerInfo;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Random;
import land.Land;
import land.Pos;
import land.Range;
import landHandler.AdminHandler;
import landMain.LandMain;
import landMain.LandManager;
import level.LevelManager;
import level.LevelNameSetEvent;
import lib.config.ReloadConfigEvent;
import lib.hashList.HashList;
import lib.hashList.HashListImpl;
import lib.tab.Tab;
import lib.time.TimeEvent;
import lib.util.Util;
import lib.util.UtilConfig;
import lib.util.UtilEco;
import lib.util.UtilFormat;
import lib.util.UtilItems;
import lib.util.UtilPer;
import lib.util.UtilTab;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Server;
import org.bukkit.World;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

public class TownManager implements Listener {
   private static final String CHECK_SAFE = "per.town.safe";
   private static final String TOWN = "town";
   private Random r = new Random();
   private String pn;
   private Dao dao;
   private Server server;
   private String nowSizeFile;
   private int townBase;
   private int townFix;
   private int minGive;
   private int pageSize;
   private int safeLockLevel;
   private int maxLevel;
   private int setNameCost;
   private int maxNameLength;
   private int healLevel;
   private int fix;
   private int add;
   private int diyId;
   private String diyLeader;
   private String diyStaff;
   private String preLeader;
   private String preStaff;
   private String statusOnline;
   private String statusOffline;
   private int size;
   private String world;
   private int level;
   private int ask;
   private int yMin;
   private int yMax;
   private String name;
   private int showId;
   private int showSmallId;
   private int showHeight;
   private int spawnHeight;
   private int spawnRadius;
   private int spawnId;
   private HashMap addFlagsHash;
   private HashMap posNameHash;
   private ItemStack delItem;
   private TownInfo activeTown;
   private HashMap townHash;
   private HashMap townPosHash;
   private HashMap userHash;
   private HashMap playerHash;
   private HashList rankList;
   private LevelManager levelManager;

   public TownManager(Main main) {
      super();
      this.levelManager = main.getLevelManager();
      this.pn = Main.getPn();
      this.dao = Main.getDao();
      this.server = main.getServer();
      this.nowSizeFile = main.getPluginPath() + File.separator + Main.getPn() + File.separator + "nowSize.yml";
      this.loadConfig(UtilConfig.getConfig(Main.getPn()));
      this.server.getPluginManager().registerEvents(this, main);
      this.loadData();
      if (this.activeTown == null) {
         this.createNewTown();
      }

      UtilTab.register("town");
      this.playerHash = UtilTab.getMode("town");
      this.fixAllRanks();
   }

   @EventHandler(
      priority = EventPriority.NORMAL
   )
   public void onPlayerRespawn(PlayerRespawnEvent e) {
      if (Infos.getPlayerInfoManager().getSpawnLoc(e.getPlayer()) == 2) {
         TownUser townUser = this.checkInit(e.getPlayer());
         if (townUser.getTownId() != -1L) {
            TownInfo townInfo = (TownInfo)this.townHash.get(townUser.getTownId());
            if (townInfo != null) {
               Land land = LandMain.getLandManager().getLand(townInfo.getLandId());
               if (land != null) {
                  Location spawn = LandMain.getLandManager().getTpHandler().getSpawnLoc(String.valueOf(land.getId()));
                  if (spawn != null) {
                     spawn.getChunk().load(true);
                     e.setRespawnLocation(spawn);
                  }

               }
            }
         }
      }
   }

   @EventHandler(
      priority = EventPriority.LOW
   )
   public void onReloadConfig(ReloadConfigEvent e) {
      if (e.getCallPlugin().equals(Main.getPn())) {
         this.loadConfig(e.getConfig());
      }

   }

   @EventHandler(
      priority = EventPriority.LOWEST
   )
   public void onPlayerJoin(PlayerJoinEvent e) {
      if (!this.levelManager.hasLevel(e.getPlayer().getName(), this.diyId)) {
         this.levelManager.addLevel(Bukkit.getConsoleSender(), e.getPlayer().getName(), this.diyId);
      }

      this.checkInit(e.getPlayer());
      this.initTab(e.getPlayer());
   }

   @EventHandler(
      priority = EventPriority.LOWEST
   )
   public void onPlayerQuit(PlayerQuitEvent e) {
      this.checkTabLeave(e.getPlayer());
   }

   @EventHandler(
      priority = EventPriority.HIGHEST,
      ignoreCancelled = false
   )
   public void onBlockBreak(BlockBreakEvent e) {
      if (e.isCancelled() && e.getBlock().getWorld().getName().equals(this.world)) {
         e.getPlayer().sendMessage(this.get(70));
      }

   }

   @EventHandler(
      priority = EventPriority.HIGHEST,
      ignoreCancelled = false
   )
   public void onBlockPlace(BlockPlaceEvent e) {
      if (e.isCancelled() && e.getBlock().getWorld().getName().equals(this.world)) {
         e.getPlayer().sendMessage(this.get(70));
      }

   }

   @EventHandler(
      priority = EventPriority.HIGHEST,
      ignoreCancelled = true
   )
   public void onEntityDamageByEntity(EntityDamageByEntityEvent e) {
      if (e.getEntity() instanceof Player) {
         Player damager;
         if (e.getDamager() instanceof Projectile) {
            LivingEntity shooter = ((Projectile)e.getDamager()).getShooter();
            if (shooter == null || !(shooter instanceof Player)) {
               return;
            }

            damager = (Player)shooter;
         } else {
            if (!(e.getDamager() instanceof Player)) {
               return;
            }

            damager = (Player)e.getDamager();
         }

         Player victim = (Player)e.getEntity();
         TownUser tu1 = this.checkInit(victim);
         TownUser tu2 = this.checkInit(damager);
         if (tu1.getTownId() == tu2.getTownId() && tu1.getTownId() != -1L && this.isSafe(victim)) {
            e.setCancelled(true);
            damager.sendMessage(this.get(250));
         }

      }
   }

   @EventHandler(
      priority = EventPriority.HIGHEST
   )
   public void onLevelNameSet(LevelNameSetEvent e) {
      if (e.getLevel().getId() == this.diyId) {
         String result = this.get(255);
         Player p = e.getP();
         TownUser tu = this.checkInit(p);
         long townId = tu.getTownId();
         if (townId != -1L) {
            TownInfo ti = (TownInfo)this.townHash.get(townId);
            if (ti != null && ti.getName() != null) {
               if (tu.getPos() == 0) {
                  result = this.diyStaff.replace("{0}", ti.getName());
               } else {
                  result = this.diyLeader.replace("{0}", ti.getName());
               }
            }
         }

         result = result.substring(0, Math.min(16, result.length()));
         e.setShow(result);
      }

   }

   @EventHandler(
      priority = EventPriority.LOWEST
   )
   public void onTime(TimeEvent e) {
      if (TimeEvent.getTime() % 6L == 0L) {
         HashMap<TownInfo, Integer> resultHash = new HashMap();

         Player[] var6;
         for(Player p : var6 = Bukkit.getOnlinePlayers()) {
            if (this.r.nextInt(10) == 5) {
               TownUser tu = this.checkInit(p);
               TownInfo ti = (TownInfo)this.townHash.get(tu.getTownId());
               if (ti != null) {
                  if (!resultHash.containsKey(ti)) {
                     resultHash.put(ti, 0);
                  }

                  int add = 2;
                  PlayerInfo pi = Infos.getPlayerInfoManager().getPlayerInfo(p.getName());
                  if (pi != null) {
                     if (pi.isActive()) {
                        add += 2;
                     }

                     if (pi.isAlive()) {
                        ++add;
                     }
                  }

                  resultHash.put(ti, (Integer)resultHash.get(ti) + add);
                  if (!ti.getGiveHash().containsKey(p.getName())) {
                     ti.getGiveHash().put(p.getName(), 0);
                  }

                  ti.getGiveHash().put(p.getName(), (Integer)ti.getGiveHash().get(p.getName()) + add);
               }
            }
         }

         for(TownInfo ti : resultHash.keySet()) {
            int add = (Integer)resultHash.get(ti);
            this.addExp(ti, add, (String)null);
         }
      }

   }

   public int getActive(long id) {
      try {
         int result = 0;
         TownInfo townInfo = (TownInfo)this.townHash.get(id);

         for(String name : townInfo.getUserHash().keySet()) {
            PlayerInfo pi = Infos.getPlayerInfoManager().getPlayerInfo(name);
            if (pi != null && pi.isActive()) {
               ++result;
            }
         }

         return result;
      } catch (Exception var8) {
         return -1;
      }
   }

   public int getAlive(long id) {
      try {
         int result = 0;
         TownInfo townInfo = (TownInfo)this.townHash.get(id);

         for(String name : townInfo.getUserHash().keySet()) {
            PlayerInfo pi = Infos.getPlayerInfoManager().getPlayerInfo(name);
            if (pi != null && pi.isAlive()) {
               ++result;
            }
         }

         return result;
      } catch (Exception var8) {
         return -1;
      }
   }

   public void spawn(Player p) {
      TownUser townUser = this.checkInit(p);
      if (townUser.getTownId() == -1L) {
         p.sendMessage(UtilFormat.format(this.pn, "fail", new Object[]{this.get(55)}));
      } else {
         TownInfo townInfo = (TownInfo)this.townHash.get(townUser.getTownId());
         if (townInfo == null) {
            p.sendMessage(UtilFormat.format(this.pn, "fail", new Object[]{this.get(60)}));
         } else {
            Land land = LandMain.getLandManager().getLand(townInfo.getLandId());
            if (land == null) {
               p.sendMessage(UtilFormat.format(this.pn, "fail", new Object[]{this.get(65)}));
            } else {
               LandMain.getLandManager().getTpHandler().tp(p, String.valueOf(land.getId()));
            }
         }
      }
   }

   public void getPos(Player p) {
      TownUser townUser = this.checkInit(p);
      if (townUser.getPos() == 1) {
         p.sendMessage(UtilFormat.format(this.pn, "fail", new Object[]{this.get(75)}));
      } else {
         TownInfo townInfo = (TownInfo)this.townHash.get(townUser.getTownId());
         if (townInfo == null) {
            p.sendMessage(UtilFormat.format(this.pn, "fail", new Object[]{this.get(130)}));
         } else if (townInfo.isActive()) {
            p.sendMessage(UtilFormat.format(this.pn, "fail", new Object[]{this.get(80)}));
         } else if (townInfo.isSafeLock()) {
            p.sendMessage(UtilFormat.format(this.pn, "fail", new Object[]{this.get(275)}));
         } else {
            for(String name : townInfo.getUserHash().keySet()) {
               if ((Integer)townInfo.getUserHash().get(name) == 1) {
                  PlayerInfo pi = Infos.getPlayerInfoManager().getPlayerInfo(name);
                  if (pi != null && pi.isAlive()) {
                     p.sendMessage(UtilFormat.format(this.pn, "fail", new Object[]{this.get(85)}));
                     return;
                  }
                  break;
               }
            }

            PlayerInfo pi = Infos.getPlayerInfoManager().getPlayerInfo(p.getName());
            if (pi != null) {
               if (!pi.isActive()) {
                  p.sendMessage(UtilFormat.format(this.pn, "fail", new Object[]{this.get(90)}));
                  return;
               }

               for(String name : townInfo.getUserHash().keySet()) {
                  if (name.equals(p.getName())) {
                     townInfo.getUserHash().put(name, 1);
                  } else if ((Integer)townInfo.getUserHash().get(name) == 1) {
                     townInfo.getUserHash().put(name, 0);
                     TownUser tu = (TownUser)this.userHash.get(name);
                     if (tu != null) {
                        tu.setPos(0);
                        this.dao.addOrUpdateTownUser(tu);

                        for(String s : townInfo.getUserHash().keySet()) {
                           this.updateTab(s, name, false);
                        }
                     }
                  }
               }

               this.dao.addOrUpdateTownInfo(townInfo);
               townUser.setPos(1);
               this.dao.addOrUpdateTownUser(townUser);

               for(String s : townInfo.getUserHash().keySet()) {
                  this.updateTab(s, p.getName(), false);
               }

               p.sendMessage(UtilFormat.format(this.pn, "success", new Object[]{this.get(95)}));
               String msg = UtilFormat.format(this.pn, "getSuccess", new Object[]{p.getName()});

               for(String name : townInfo.getUserHash().keySet()) {
                  if (!name.equals(p.getName())) {
                     Player tarP = this.server.getPlayerExact(name);
                     if (tarP != null) {
                        tarP.sendMessage(msg);
                     }
                  }
               }
            }

         }
      }
   }

   public ItemStack getAskInfoItem(Player p) {
      ItemStack is = UtilItems.getItem(this.pn, "main_ask").clone();
      ItemMeta im = is.getItemMeta();
      List<String> lore = new ArrayList();
      TownUser tu = this.checkInit(p);
      TownInfo ti = (TownInfo)this.townHash.get(tu.getTownId());
      if (ti != null) {
         for(String s : ti.getAskHash().keySet()) {
            String ss = UtilFormat.format(this.pn, "ask1", new Object[]{s});
            lore.add(ss);
         }
      }

      im.setLore(lore);
      is.setItemMeta(im);
      return is;
   }

   public ItemStack getTownTipItem() {
      ItemStack is = UtilItems.getItem(this.pn, "main_tip").clone();
      ItemMeta im = is.getItemMeta();
      List<String> lore = im.getLore();
      lore.set(1, ((String)lore.get(1)).replace("{0}", String.valueOf(this.healLevel)));
      im.setLore(lore);
      is.setItemMeta(im);
      return is;
   }

   public ItemStack getTownSetNameItem() {
      ItemStack is = UtilItems.getItem(this.pn, "main_setName").clone();
      ItemMeta im = is.getItemMeta();
      List<String> lore = im.getLore();
      lore.set(2, ((String)lore.get(2)).replace("{0}", String.valueOf(this.setNameCost)));
      im.setLore(lore);
      is.setItemMeta(im);
      return is;
   }

   public ItemStack getTownGiveItem() {
      ItemStack is = UtilItems.getItem(this.pn, "main_give").clone();
      ItemMeta im = is.getItemMeta();
      List<String> lore = im.getLore();
      lore.set(1, ((String)lore.get(1)).replace("{0}", String.valueOf(this.minGive)));
      im.setLore(lore);
      is.setItemMeta(im);
      return is;
   }

   public ItemStack getTownSafeLockItem(Player p) {
      ItemStack is = UtilItems.getItem(this.pn, "main_safeLock").clone();
      ItemMeta im = is.getItemMeta();
      List<String> lore = im.getLore();
      lore.set(5, ((String)lore.get(5)).replace("{0}", String.valueOf(this.safeLockLevel)));
      String status = this.get(110);
      TownUser tu = this.checkInit(p);
      TownInfo ti = (TownInfo)this.townHash.get(tu.getTownId());
      if (ti != null) {
         if (ti.isSafeLock()) {
            status = this.get(240);
         } else {
            status = this.get(245);
         }
      }

      lore.set(6, ((String)lore.get(6)).replace("{0}", status));
      im.setLore(lore);
      is.setItemMeta(im);
      return is;
   }

   public ItemStack getTownInfoItem(Player p) {
      ItemStack is = UtilItems.getItem(this.pn, "main_info").clone();
      ItemMeta im = is.getItemMeta();
      List<String> lore = im.getLore();
      TownUser tu = this.checkInit(p);
      TownInfo ti = (TownInfo)this.townHash.get(tu.getTownId());
      String owner = this.get(110);
      long townId = -1L;
      String townName = this.get(110);
      String landName = this.get(110);
      int now = -1;
      int active = -1;
      int alive = -1;
      int level = 0;
      int nowExp = 0;
      int nextExp = 0;
      if (ti != null) {
         nowExp = ti.getExp();
         level = ti.getLevel();
         nextExp = this.getNextExp(level);

         for(String name : ti.getUserHash().keySet()) {
            if ((Integer)ti.getUserHash().get(name) == 1) {
               owner = name;
               break;
            }
         }

         Land land = LandMain.getLandManager().getLand(ti.getLandId());
         townId = ti.getId();
         if (ti.getName() != null) {
            townName = ti.getName();
         }

         landName = land.getName();
         now = ti.getUserHash().size();
         active = this.getActive(ti.getId());
         alive = this.getAlive(ti.getId());
      }

      lore.set(0, ((String)lore.get(0)).replace("{0}", String.valueOf(townId)));
      lore.set(0, ((String)lore.get(0)).replace("{1}", townName));
      lore.set(0, ((String)lore.get(0)).replace("{2}", owner));
      lore.set(1, ((String)lore.get(1)).replace("{0}", String.valueOf(level)));
      lore.set(1, ((String)lore.get(1)).replace("{1}", String.valueOf(nowExp)).replace("{2}", String.valueOf(nextExp)));
      lore.set(2, ((String)lore.get(2)).replace("{0}", landName));
      lore.set(2, ((String)lore.get(2)).replace("{1}", String.valueOf(now)).replace("{2}", String.valueOf(this.getMaxPlayers(level))));
      lore.set(2, ((String)lore.get(2)).replace("{3}", String.valueOf(active)).replace("{4}", String.valueOf(alive)));
      if (ti != null) {
         boolean check = true;

         for(String s : ti.getUserHash().keySet()) {
            int give = 0;
            if (ti.getGiveHash().containsKey(s)) {
               give = (Integer)ti.getGiveHash().get(s);
            }

            String actives = this.get(120);
            String alives = this.get(120);
            PlayerInfo pi = Infos.getPlayerInfoManager().getPlayerInfo(s);
            if (pi != null) {
               if (pi.isActive()) {
                  actives = this.get(115);
               }

               if (pi.isAlive()) {
                  alives = this.get(115);
               }
            }

            String info = UtilFormat.format(this.pn, "townUser", new Object[]{s, give, actives, alives});
            if (check) {
               lore.add(info);
            } else {
               lore.set(lore.size() - 1, (String)lore.get(lore.size() - 1) + " " + info);
            }

            check = !check;
         }
      }

      im.setLore(lore);
      is.setItemMeta(im);
      return is;
   }

   public boolean join(Player p, String s) {
      try {
         TownUser tu = this.checkInit(p);
         if (tu.getTownId() != -1L) {
            p.sendMessage(UtilFormat.format(this.pn, "fail", new Object[]{this.get(180)}));
            return true;
         } else {
            long id = Long.parseLong(s);
            TownInfo ti = (TownInfo)this.townHash.get(id);
            if (ti == null) {
               p.sendMessage(UtilFormat.format(this.pn, "fail", new Object[]{this.get(175)}));
               return false;
            } else if (ti.getUserHash().size() > 0) {
               if (ti.getUserHash().size() >= this.getMaxPlayers(ti.getLevel())) {
                  p.sendMessage(UtilFormat.format(this.pn, "fail", new Object[]{this.get(185)}));
                  return false;
               } else if (ti.getAskHash().size() >= this.ask) {
                  p.sendMessage(UtilFormat.format(this.pn, "fail", new Object[]{this.get(190)}));
                  return false;
               } else if (ti.getAskHash().containsKey(p.getName())) {
                  p.sendMessage(UtilFormat.format(this.pn, "fail", new Object[]{this.get(200)}));
                  return false;
               } else {
                  ti.getAskHash().put(p.getName(), 0);
                  this.dao.addOrUpdateTownInfo(ti);
                  this.initTab(p);
                  p.sendMessage(UtilFormat.format(this.pn, "success", new Object[]{this.get(195)}));
                  return true;
               }
            } else {
               tu.setPos(0);
               tu.setTownId(ti.getId());
               this.dao.addOrUpdateTownUser(tu);
               ti.getUserHash().put(p.getName(), 0);
               this.dao.addOrUpdateTownInfo(ti);
               this.addLandPer(p.getName(), ti.getLandId());

               for(String name : ti.getUserHash().keySet()) {
                  this.updateTab(name, p.getName(), false);
               }

               p.sendMessage(UtilFormat.format(this.pn, "success", new Object[]{this.get(235)}));
               return true;
            }
         }
      } catch (NumberFormatException var9) {
         p.sendMessage(UtilFormat.format(this.pn, "fail", new Object[]{this.get(35)}));
         return false;
      }
   }

   public boolean del(Player p, String s) {
      TownUser tu = this.checkInit(p);
      if (tu.getTownId() == -1L) {
         p.sendMessage(UtilFormat.format(this.pn, "fail", new Object[]{this.get(130)}));
         return true;
      } else if (tu.getPos() == 0) {
         p.sendMessage(UtilFormat.format(this.pn, "fail", new Object[]{this.get(135)}));
         return true;
      } else {
         String ss = Util.getRealName(p, s);
         if (ss != null) {
            s = ss;
         }

         if (p.getName().equals(s)) {
            p.sendMessage(UtilFormat.format(this.pn, "fail", new Object[]{this.get(145)}));
            return true;
         } else {
            TownInfo ti = (TownInfo)this.townHash.get(tu.getTownId());
            if (ti == null) {
               return true;
            } else if (!ti.getUserHash().containsKey(s)) {
               p.sendMessage(UtilFormat.format(this.pn, "fail", new Object[]{this.get(140)}));
               return false;
            } else {
               TownUser tu2 = (TownUser)this.userHash.get(s);
               if (tu2 != null) {
                  tu2.setTownId(-1L);
                  tu2.setPos(0);
                  this.dao.addOrUpdateTownUser(tu2);
               }

               ti.getUserHash().remove(s);
               this.dao.addOrUpdateTownInfo(ti);
               this.delLandPer(s, ti.getLandId());
               p.sendMessage(UtilFormat.format(this.pn, "kick1", new Object[]{s}));
               Player tarP = this.server.getPlayerExact(s);
               if (tarP != null) {
                  tarP.sendMessage(UtilFormat.format(this.pn, "kick2", new Object[]{p.getName()}));
                  this.checkTabLeave(tarP);
               }

               for(String sss : ti.getUserHash().keySet()) {
                  Player pp = this.server.getPlayerExact(sss);
                  if (pp != null) {
                     pp.sendMessage(UtilFormat.format(this.pn, "kick3", new Object[]{p.getName(), s}));
                  }
               }

               return true;
            }
         }
      }
   }

   public boolean yes(Player p, String s) {
      TownUser tu = this.checkInit(p);
      if (tu.getTownId() == -1L) {
         p.sendMessage(UtilFormat.format(this.pn, "fail", new Object[]{this.get(130)}));
         return true;
      } else if (tu.getPos() == 0) {
         p.sendMessage(UtilFormat.format(this.pn, "fail", new Object[]{this.get(205)}));
         return true;
      } else {
         s = Util.getRealName(p, s);
         if (s == null) {
            return false;
         } else {
            TownInfo ti = (TownInfo)this.townHash.get(tu.getTownId());
            if (ti == null) {
               return true;
            } else if (!ti.getAskHash().containsKey(s)) {
               p.sendMessage(UtilFormat.format(this.pn, "fail", new Object[]{this.get(210)}));
               return false;
            } else {
               ti.getAskHash().remove(s);
               if (ti.getUserHash().size() >= this.getMaxPlayers(ti.getLevel())) {
                  p.sendMessage(UtilFormat.format(this.pn, "fail1", new Object[]{s}));
                  return true;
               } else {
                  TownUser tuTar = (TownUser)this.userHash.get(s);
                  if (tuTar == null) {
                     return false;
                  } else if (tuTar.getTownId() != -1L) {
                     p.sendMessage(UtilFormat.format(this.pn, "fail2", new Object[]{s}));
                     return true;
                  } else {
                     ti.getUserHash().put(s, 0);
                     tuTar.setPos(0);
                     tuTar.setTownId(ti.getId());
                     this.dao.addOrUpdateTownUser(tuTar);
                     this.dao.addOrUpdateTownInfo(ti);
                     this.addLandPer(s, ti.getLandId());
                     this.initTab(p);
                     p.sendMessage(UtilFormat.format(this.pn, "success1", new Object[]{s}));
                     Player tarP = this.server.getPlayerExact(s);
                     if (tarP != null && tarP.isOnline()) {
                        tarP.sendMessage(UtilFormat.format(this.pn, "success", new Object[]{this.get(220)}));
                     }

                     String tip = UtilFormat.format(this.pn, "success2", new Object[]{s});

                     for(String ss : ti.getUserHash().keySet()) {
                        if (!ss.equals(p.getName()) && !ss.equals(s)) {
                           Player pp = this.server.getPlayerExact(ss);
                           if (pp != null && pp.isOnline()) {
                              pp.sendMessage(tip);
                           }
                        }
                     }

                     return false;
                  }
               }
            }
         }
      }
   }

   public void yesAll(Player p) {
      TownUser tu = this.checkInit(p);
      if (tu.getTownId() == -1L) {
         p.sendMessage(UtilFormat.format(this.pn, "fail", new Object[]{this.get(130)}));
      } else if (tu.getPos() == 0) {
         p.sendMessage(UtilFormat.format(this.pn, "fail", new Object[]{this.get(205)}));
      } else {
         TownInfo ti = (TownInfo)this.townHash.get(tu.getTownId());
         if (ti != null) {
            if (ti.getAskHash().size() <= 0) {
               p.sendMessage(UtilFormat.format(this.pn, "fail", new Object[]{this.get(215)}));
            } else {
               for(int i = 0; i < this.ask && ti.getAskHash().size() > 0; ++i) {
                  this.yes(p, (String)ti.getAskHash().keySet().iterator().next());
               }

            }
         }
      }
   }

   public boolean no(Player p, String s) {
      TownUser tu = this.checkInit(p);
      if (tu.getTownId() == -1L) {
         p.sendMessage(UtilFormat.format(this.pn, "fail", new Object[]{this.get(130)}));
         return true;
      } else if (tu.getPos() == 0) {
         p.sendMessage(UtilFormat.format(this.pn, "fail", new Object[]{this.get(205)}));
         return true;
      } else {
         String ss = Util.getRealName((CommandSender)null, s);
         if (ss != null) {
            s = ss;
         }

         TownInfo ti = (TownInfo)this.townHash.get(tu.getTownId());
         if (ti == null) {
            return true;
         } else if (s != null && !s.trim().isEmpty() && ti.getAskHash().containsKey(s)) {
            ti.getAskHash().remove(s);
            this.dao.addOrUpdateTownInfo(ti);
            p.sendMessage(UtilFormat.format(this.pn, "deny1", new Object[]{s}));
            Player tarP = this.server.getPlayerExact(s);
            if (tarP != null && tarP.isOnline()) {
               tarP.sendMessage(UtilFormat.format(this.pn, "deny2", new Object[]{ti.getLandId()}));
            }

            return false;
         } else {
            p.sendMessage(UtilFormat.format(this.pn, "fail", new Object[]{this.get(210)}));
            return false;
         }
      }
   }

   public void noAll(Player p) {
      TownUser tu = this.checkInit(p);
      if (tu.getTownId() == -1L) {
         p.sendMessage(UtilFormat.format(this.pn, "fail", new Object[]{this.get(130)}));
      } else if (tu.getPos() == 0) {
         p.sendMessage(UtilFormat.format(this.pn, "fail", new Object[]{this.get(205)}));
      } else {
         TownInfo ti = (TownInfo)this.townHash.get(tu.getTownId());
         if (ti != null) {
            if (ti.getAskHash().size() <= 0) {
               p.sendMessage(UtilFormat.format(this.pn, "fail", new Object[]{this.get(215)}));
            } else {
               for(int i = 0; i < this.ask && ti.getAskHash().size() > 0; ++i) {
                  this.no(p, (String)ti.getAskHash().keySet().iterator().next());
               }

            }
         }
      }
   }

   public void quit(Player p) {
      TownUser tu = this.checkInit(p);
      if (tu.getTownId() == -1L) {
         p.sendMessage(UtilFormat.format(this.pn, "fail", new Object[]{this.get(130)}));
      } else {
         TownInfo ti = (TownInfo)this.townHash.get(tu.getTownId());
         if (ti != null) {
            if (ti.isSafeLock() && tu.getPos() == 1) {
               p.sendMessage(UtilFormat.format(this.pn, "fail", new Object[]{this.get(280)}));
               return;
            }

            ti.getUserHash().remove(p.getName());
            this.dao.addOrUpdateTownInfo(ti);
         }

         tu.setPos(0);
         tu.setTownId(-1L);
         this.dao.addOrUpdateTownUser(tu);
         this.delLandPer(p.getName(), ti.getLandId());
         this.checkTabLeave(p);
         p.sendMessage(UtilFormat.format(this.pn, "fail", new Object[]{this.get(165)}));
         String msg = UtilFormat.format(this.pn, "quitTip", new Object[]{p.getName()});

         for(String name : ti.getUserHash().keySet()) {
            Player tarP = this.server.getPlayerExact(name);
            if (tarP != null && tarP.isOnline()) {
               tarP.sendMessage(msg);
            }
         }

      }
   }

   public ItemStack getDelItem() {
      return this.delItem;
   }

   public String getAskListShow(Player p) {
      TownUser tu = this.checkInit(p);
      TownInfo ti = (TownInfo)this.townHash.get(tu.getTownId());
      if (ti == null) {
         return "";
      } else {
         String result = "";

         for(String s : ti.getAskHash().keySet()) {
            if (!result.isEmpty()) {
               result = result + ",";
            }

            result = result + s;
         }

         return result;
      }
   }

   public TownUser checkInit(Player p) {
      TownUser townUser = (TownUser)this.userHash.get(p.getName());
      if (townUser == null) {
         townUser = new TownUser(p.getName(), this.getNextTownId());
         this.userHash.put(p.getName(), townUser);
         this.dao.addOrUpdateTownUser(townUser);
         p.sendMessage(this.get(50));
         TownInfo townInfo = (TownInfo)this.townHash.get(townUser.getTownId());
         if (townInfo != null) {
            townInfo.getUserHash().put(p.getName(), 0);
            this.dao.addOrUpdateTownInfo(townInfo);
            String tip = UtilFormat.format(this.pn, "joinTip", new Object[]{p.getName()});

            for(String name : townInfo.getUserHash().keySet()) {
               Player tarP = this.server.getPlayerExact(name);
               if (tarP != null && tarP.isOnline() && !tarP.getName().equals(p.getName())) {
                  tarP.sendMessage(tip);
               }
            }

            this.addLandPer(p.getName(), townInfo.getLandId());
         }

         this.initTab(p);
      }

      return townUser;
   }

   public HashMap getTownHash() {
      return this.townHash;
   }

   public boolean isSafe(Player p) {
      return !UtilPer.hasPer(p, "per.town.safe");
   }

   public String isSafeShow(Player p) {
      return this.isSafe(p) ? this.get(240) : this.get(245);
   }

   public void toggleSafe(Player p) {
      this.setSafe(p, !this.isSafe(p));
      p.sendMessage(UtilFormat.format(this.pn, "setSafe", new Object[]{this.isSafeShow(p)}));
   }

   public void setSafe(Player p, boolean safe) {
      if (safe) {
         UtilPer.remove(p, "per.town.safe");
      } else {
         UtilPer.add(p, "per.town.safe");
      }

   }

   public boolean isInSameTown(String name, String tar) {
      TownUser tu1 = (TownUser)this.userHash.get(name);
      TownUser tu2 = (TownUser)this.userHash.get(tar);
      return tu1 != null && tu2 != null && tu1.getTownId() == tu2.getTownId() && tu1.getTownId() != -1L;
   }

   public int getHealLevel() {
      return this.healLevel;
   }

   public boolean setName(Player p, String name) {
      TownUser tu = this.checkInit(p);
      if (tu.getTownId() == -1L) {
         p.sendMessage(UtilFormat.format(this.pn, "fail", new Object[]{this.get(130)}));
         return true;
      } else if (tu.getPos() == 0) {
         p.sendMessage(UtilFormat.format(this.pn, "fail", new Object[]{this.get(205)}));
         return true;
      } else {
         name = Util.convert(name.trim());
         if (name.length() > this.maxNameLength) {
            p.sendMessage(UtilFormat.format(this.pn, "maxNameLength", new Object[]{this.maxNameLength}));
            return false;
         } else {
            TownInfo ti = (TownInfo)this.townHash.get(tu.getTownId());
            if (ti == null) {
               return true;
            } else if (name.length() > ti.getLevel()) {
               p.sendMessage(UtilFormat.format(this.pn, "maxNameLength2", new Object[]{ti.getLevel()}));
               return false;
            } else if (name.isEmpty()) {
               p.sendMessage(UtilFormat.format(this.pn, "fail", new Object[]{this.get(265)}));
               return false;
            } else if (ti.getExp() < this.setNameCost) {
               p.sendMessage(UtilFormat.format(this.pn, "setNameCost", new Object[]{this.setNameCost}));
               return true;
            } else {
               this.delExp(ti, this.setNameCost, UtilFormat.format(this.pn, "delExp", new Object[]{this.setNameCost, this.get(300)}));
               ti.setName(name);
               this.dao.addOrUpdateTownInfo(ti);
               p.sendMessage(UtilFormat.format(this.pn, "set2", new Object[]{name}));
               this.sendTip(ti, UtilFormat.format(this.pn, "tip2", new Object[]{p.getName(), name}));
               return true;
            }
         }
      }
   }

   public void safeLock(Player p, boolean open) {
      TownUser tu = this.checkInit(p);
      if (tu.getTownId() == -1L) {
         p.sendMessage(UtilFormat.format(this.pn, "fail", new Object[]{this.get(130)}));
      } else if (tu.getPos() == 0) {
         p.sendMessage(UtilFormat.format(this.pn, "fail", new Object[]{this.get(205)}));
      } else {
         TownInfo ti = (TownInfo)this.townHash.get(tu.getTownId());
         if (ti != null) {
            if (ti.getLevel() < this.safeLockLevel) {
               p.sendMessage(UtilFormat.format(this.pn, "tip3", new Object[]{this.safeLockLevel}));
            } else if (!(ti.isSafeLock() ^ open)) {
               p.sendMessage(UtilFormat.format(this.pn, "fail", new Object[]{this.get(285)}));
            } else {
               ti.setSafeLock(open);
               this.dao.addOrUpdateTownInfo(ti);
               String status;
               if (open) {
                  status = this.get(240);
               } else {
                  status = this.get(245);
               }

               p.sendMessage(UtilFormat.format(this.pn, "set3", new Object[]{status}));
               this.sendTip(ti, UtilFormat.format(this.pn, "tip4", new Object[]{p.getName(), status}));
            }
         }
      }
   }

   public void showRank(CommandSender sender, int page) {
      if (this.rankList.isEmpty()) {
         sender.sendMessage(UtilFormat.format(this.pn, "fail", new Object[]{this.get(315)}));
      } else {
         int maxPage = this.rankList.getMaxPage(this.pageSize);
         if (page < 1) {
            page = 1;
         } else if (page > maxPage) {
            page = maxPage;
         }

         List<TownInfo> list = this.rankList.getPage(page, this.pageSize);
         sender.sendMessage(UtilFormat.format(this.pn, "listHeader", new Object[]{this.get(320), page, maxPage}));
         int rank = (page - 1) * this.pageSize + 1;

         for(TownInfo ti : list) {
            String townName;
            if (ti.getName() != null) {
               townName = ti.getName();
            } else {
               townName = this.get(110);
            }

            sender.sendMessage(UtilFormat.format(this.pn, "listItem", new Object[]{UtilFormat.format(this.pn, "tip6", new Object[]{rank++, ti.getId(), townName, ti.getLevel(), ti.getUserHash().size(), this.getMaxPlayers(ti.getLevel())})}));
         }

         sender.sendMessage(this.get(325));
      }
   }

   public boolean give(Player p, String s) {
      TownUser tu = this.checkInit(p);
      if (tu.getTownId() == -1L) {
         p.sendMessage(UtilFormat.format(this.pn, "fail", new Object[]{this.get(130)}));
         return true;
      } else {
         TownInfo ti = (TownInfo)this.townHash.get(tu.getTownId());
         if (ti == null) {
            return true;
         } else {
            try {
               int amount = Integer.parseInt(s);
               if (amount < this.minGive) {
                  p.sendMessage(UtilFormat.format(this.pn, "tip7", new Object[]{this.minGive}));
                  return false;
               } else {
                  int give = Math.min(amount, (int)UtilEco.get(p.getName()));
                  if (give < this.minGive) {
                     p.sendMessage(UtilFormat.format(this.pn, "fail", new Object[]{this.get(330)}));
                     return true;
                  } else {
                     if (UtilEco.del(p.getName(), (double)give)) {
                        if (!ti.getGiveHash().containsKey(p.getName())) {
                           ti.getGiveHash().put(p.getName(), 0);
                        }

                        ti.getGiveHash().put(p.getName(), (Integer)ti.getGiveHash().get(p.getName()) + give);
                        p.sendMessage(UtilFormat.format(this.pn, "tip8", new Object[]{give, give}));
                        this.addExp(ti, give, (String)null);
                     }

                     return true;
                  }
               }
            } catch (NumberFormatException var7) {
               p.sendMessage(UtilFormat.format(this.pn, "fail", new Object[]{this.get(35)}));
               return false;
            }
         }
      }
   }

   private int getMaxPlayers(int level) {
      return this.townBase + this.townFix * level;
   }

   private void fixAllRanks() {
      Util.sendConsoleMessage(UtilFormat.format(this.pn, "tip", new Object[]{this.get(290)}));
      this.rankList = new HashListImpl();

      for(TownInfo ti : this.townHash.values()) {
         this.fixRank(ti);
      }

      Util.sendConsoleMessage(UtilFormat.format(this.pn, "tip", new Object[]{this.get(295)}));
   }

   private void fixRank(TownInfo ti) {
      this.rankList.remove(ti);
      if (this.rankList.isEmpty()) {
         this.rankList.add(ti);
      } else {
         TownInfo ti2 = (TownInfo)this.rankList.get(0);
         int start = 0;
         int end = this.rankList.size() - 1;

         while(start < end - 1) {
            int mid = (start + end) / 2;
            ti2 = (TownInfo)this.rankList.get(mid);
            if (this.compare(ti, ti2)) {
               end = mid;
            } else {
               start = mid;
            }
         }

         if (start == end) {
            if (this.compare(ti, ti2)) {
               this.rankList.add(ti, start);
            } else {
               this.rankList.add(ti, start + 1);
            }
         } else {
            ti2 = (TownInfo)this.rankList.get(end);
            if (this.compare(ti, ti2)) {
               ti2 = (TownInfo)this.rankList.get(start);
               if (this.compare(ti, ti2)) {
                  this.rankList.add(ti, start);
               } else {
                  this.rankList.add(ti, start + 1);
               }
            } else {
               this.rankList.add(ti, end + 1);
            }
         }

      }
   }

   private void addExp(TownInfo ti, int exp, String reason) {
      if (exp > 0) {
         int level = ti.getLevel();
         int nextExp = this.getNextExp(level);
         ti.setExp(ti.getExp() + exp);
         if (level < this.maxLevel && ti.getExp() >= nextExp) {
            ti.setExp(ti.getExp() - nextExp);
            ti.setLevel(ti.getLevel() + 1);
            this.sendTip(ti, UtilFormat.format(this.pn, "tip5", new Object[]{ti.getLevel()}));
         }

         this.dao.addOrUpdateTownInfo(ti);
         this.fixRank(ti);
         if (reason != null) {
            this.sendTip(ti, reason);
         }

      }
   }

   private void delExp(TownInfo ti, int exp, String reason) {
      if (exp > 0) {
         ti.setExp(ti.getExp() - exp);
         if (ti.getExp() < 0) {
            ti.setExp(0);
         }

         this.dao.addOrUpdateTownInfo(ti);
         this.fixRank(ti);
         if (reason != null) {
            this.sendTip(ti, reason);
         }

      }
   }

   private boolean compare(TownInfo ti1, TownInfo ti2) {
      if (ti1.getLevel() > ti2.getLevel()) {
         return true;
      } else if (ti1.getLevel() < ti2.getLevel()) {
         return false;
      } else {
         return ti1.getExp() >= ti2.getExp();
      }
   }

   private void sendTip(TownInfo ti, String msg) {
      for(String name : ti.getUserHash().keySet()) {
         Util.sendMsg(name, msg);
      }

   }

   private int getNextExp(int level) {
      return this.fix + level * this.add;
   }

   private void initTab(Player p) {
      Tab.Mode mode = new Tab.Mode();
      this.playerHash.put(p, mode);
      TownUser tu = this.checkInit(p);
      long townId = tu.getTownId();
      if (townId != -1L) {
         TownInfo ti = (TownInfo)this.townHash.get(townId);
         if (ti != null) {
            HashMap<String, Integer> hash = ti.getUserHash();
            this.updateTab(p.getName(), p.getName(), false);

            for(String name : hash.keySet()) {
               if (!p.getName().equals(name)) {
                  this.updateTab(p.getName(), name, false);
                  this.updateTab(name, p.getName(), false);
               }
            }
         }
      }

   }

   private void checkTabLeave(Player p) {
      this.playerHash.remove(p);
      TownUser tu = this.checkInit(p);
      long townId = tu.getTownId();
      if (townId != -1L) {
         TownInfo ti = (TownInfo)this.townHash.get(townId);
         if (ti != null) {
            HashMap<String, Integer> hash = ti.getUserHash();

            for(String name : hash.keySet()) {
               if (!p.getName().equals(name)) {
                  this.updateTab(name, p.getName(), true);
               }
            }
         }
      }

   }

   private void updateTab(String name, String tar, boolean forceOffline) {
      Player p = Bukkit.getPlayerExact(name);
      if (p != null && p.isOnline()) {
         Tab.Mode mode = (Tab.Mode)this.playerHash.get(p);
         if (mode != null) {
            TownUser tu = (TownUser)this.userHash.get(tar);
            if (tu != null) {
               if (tu.getTownId() == -1L) {
                  mode.remove(mode.getPos(tar));
               } else {
                  int pos = tu.getPos();
                  String tabName = this.getTabName(tar, pos, forceOffline);
                  if (mode.getShow(tar) == null) {
                     mode.add(tar, tabName);
                  } else {
                     mode.set(tar, tabName);
                  }
               }
            }
         }
      }

   }

   private String getTabName(String name, int pos, boolean forceOffline) {
      String pre;
      if (pos == 0) {
         pre = this.preStaff;
      } else {
         pre = this.preLeader;
      }

      Player p = Bukkit.getPlayerExact(name);
      String status;
      if (!forceOffline && p != null && p.isOnline()) {
         status = this.statusOnline;
      } else {
         status = this.statusOffline;
      }

      String result = pre + status + name;
      return result.substring(0, Math.min(16, result.length()));
   }

   private void addLandPer(String name, long landId) {
      Land land = LandMain.getLandManager().getLand(landId);
      if (land != null) {
         String adminFlag = AdminHandler.getFlagAdmin();
         if (!land.hasFlag(adminFlag)) {
            LandMain.getLandManager().getFlagHandler().addFlag(land, adminFlag, 0);
         }

         HashList<String> pers = (HashList)land.getPers().get(adminFlag);
         if (pers == null) {
            pers = new HashListImpl();
            land.getPers().put(adminFlag, pers);
         }

         pers.add(name);
         LandMain.getLandManager().addLand(land);
      }

   }

   private void delLandPer(String name, long landId) {
      Land land = LandMain.getLandManager().getLand(landId);
      if (land != null) {
         String adminFlag = AdminHandler.getFlagAdmin();
         if (!land.hasFlag(adminFlag)) {
            LandMain.getLandManager().getFlagHandler().addFlag(land, adminFlag, 0);
         }

         HashList<String> pers = (HashList)land.getPers().get(adminFlag);
         if (pers == null) {
            pers = new HashListImpl();
            land.getPers().put(adminFlag, pers);
         }

         pers.remove(name);
         LandMain.getLandManager().addLand(land);
      }

   }

   private long getNextTownId() {
      long result = this.activeTown.getId();
      if (this.getTownerAmount(this.activeTown) + 1 >= this.getMaxPlayers(0)) {
         this.activeTown.setActive(false);
         this.dao.addOrUpdateTownInfo(this.activeTown);
         this.createNewTown();
      }

      return result;
   }

   private void createNewTown() {
      int nowSize = this.getNowSize();
      boolean success = false;
      int x = nowSize;
      int z = nowSize;

      for(int xx = 0; xx <= nowSize; ++xx) {
         if (!this.isExsitTown(xx, nowSize)) {
            x = xx;
            success = true;
            break;
         }
      }

      if (!success) {
         for(int zz = 0; zz <= nowSize; ++zz) {
            if (!this.isExsitTown(nowSize, zz)) {
               z = zz;
               success = true;
               break;
            }
         }
      }

      if (!success) {
         this.setNowSize(nowSize + 1);
         this.createNewTown();
      } else {
         long landId = this.getNewLandId(x, z);
         TownInfo townInfo = new TownInfo(landId, x, z);
         this.dao.addOrUpdateTownInfo(townInfo);
         this.townHash.put(townInfo.getId(), townInfo);
         HashMap<Integer, TownInfo> hash = (HashMap)this.townPosHash.get(townInfo.getX());
         if (hash == null) {
            hash = new HashMap();
            this.townPosHash.put(townInfo.getX(), hash);
         }

         hash.put(townInfo.getZ(), townInfo);
         this.activeTown = townInfo;
      }
   }

   private long getNewLandId(int x, int z) {
      Pos p1 = new Pos(this.world, x * this.size, this.yMin, z * this.size);
      Pos p2 = new Pos(this.world, x * this.size + this.size - 1, this.yMax, z * this.size + this.size - 1);
      Range range = new Range(p1, p2);
      int index = 1;

      String name;
      do {
         name = this.name.replace("{0}", "" + index);
         ++index;
      } while(LandMain.getLandManager().getLand(name) != null);

      Land land = LandManager.createLand(1, true, name, Land.getSystem(), range, this.level);

      for(String flag : this.addFlagsHash.keySet()) {
         LandMain.getLandManager().getFlagHandler().addFlag(land, flag, (Integer)this.addFlagsHash.get(flag));
      }

      this.createProtect(land);
      this.createShow(land);
      return land.getId();
   }

   private void createProtect(Land land) {
      Range range = land.getRange();
      Pos center = range.getCenter();
      center.setY(this.spawnHeight);
      LandMain.getLandManager().getTpHandler().setTp(land, Pos.toLoc(center));
      World w = this.server.getWorld(center.getWorld());
      if (w != null) {
         for(int x = -this.spawnRadius; x <= this.spawnRadius; ++x) {
            for(int z = -this.spawnRadius; z <= this.spawnRadius; ++z) {
               for(int y = 0; y <= 3; ++y) {
                  Location l = new Location(w, (double)(x + center.getX()), (double)(y + this.spawnHeight - 1), (double)(z + center.getZ()));
                  l.getBlock().setType(Material.AIR);
               }
            }
         }
      }

      if (w != null) {
         for(int x = -this.spawnRadius; x <= this.spawnRadius; ++x) {
            for(int z = -this.spawnRadius; z <= this.spawnRadius; ++z) {
               Location l = new Location(w, (double)(x + center.getX()), (double)(this.spawnHeight - 1), (double)(z + center.getZ()));
               l.getBlock().setType(Material.getMaterial(this.spawnId));
            }
         }
      }

   }

   private void createShow(Land land) {
      Range range = land.getRange();
      World w = this.server.getWorld(range.getP1().getWorld());
      if (w != null) {
         range.fit();

         for(int x = range.getP1().getX(); x <= range.getP2().getX(); ++x) {
            Location l = new Location(w, (double)x, (double)this.showHeight, (double)range.getP1().getZ());
            l.getBlock().setTypeIdAndData(this.showId, (byte)this.showSmallId, true);
            l = new Location(w, (double)x, (double)this.showHeight, (double)range.getP2().getZ());
            l.getBlock().setTypeIdAndData(this.showId, (byte)this.showSmallId, true);
         }

         for(int z = range.getP1().getZ(); z <= range.getP2().getZ(); ++z) {
            Location l = new Location(w, (double)range.getP1().getX(), (double)this.showHeight, (double)z);
            l.getBlock().setTypeIdAndData(this.showId, (byte)this.showSmallId, true);
            l = new Location(w, (double)range.getP2().getX(), (double)this.showHeight, (double)z);
            l.getBlock().setTypeIdAndData(this.showId, (byte)this.showSmallId, true);
         }
      }

   }

   private boolean isExsitTown(int x, int z) {
      return this.townPosHash.containsKey(x) && ((HashMap)this.townPosHash.get(x)).containsKey(z);
   }

   private int getNowSize() {
      YamlConfiguration config = new YamlConfiguration();

      try {
         config.load(this.nowSizeFile);
         return config.getInt("size");
      } catch (FileNotFoundException e) {
         e.printStackTrace();
      } catch (IOException e) {
         e.printStackTrace();
      } catch (InvalidConfigurationException e) {
         e.printStackTrace();
      }

      this.setNowSize(0);
      return 0;
   }

   private void setNowSize(int size) {
      YamlConfiguration config = new YamlConfiguration();

      try {
         config.set("size", size);
         config.save(this.nowSizeFile);
      } catch (FileNotFoundException e) {
         e.printStackTrace();
      } catch (IOException e) {
         e.printStackTrace();
      }

   }

   private int getTownerAmount(TownInfo townInfo) {
      return townInfo.getUserHash().size();
   }

   private void loadData() {
      this.townHash = new HashMap();
      this.townPosHash = new HashMap();

      for(TownInfo townInfo : this.dao.getAllTownInfos()) {
         this.townHash.put(townInfo.getId(), townInfo);
         HashMap<Integer, TownInfo> hash = (HashMap)this.townPosHash.get(townInfo.getX());
         if (hash == null) {
            hash = new HashMap();
            this.townPosHash.put(townInfo.getX(), hash);
         }

         hash.put(townInfo.getZ(), townInfo);
         if (townInfo.isActive()) {
            this.activeTown = townInfo;
         }
      }

      this.userHash = new HashMap();

      for(TownUser townUser : this.dao.getAllTownUsers()) {
         this.userHash.put(townUser.getName(), townUser);
      }

   }

   private void loadConfig(YamlConfiguration config) {
      this.townBase = config.getInt("town.players.base");
      this.townFix = config.getInt("town.players.fix");
      this.minGive = config.getInt("town.minGive");
      this.pageSize = config.getInt("town.pageSize");
      this.safeLockLevel = config.getInt("town.safeLockLevel");
      this.maxLevel = config.getInt("town.maxLevel");
      this.setNameCost = config.getInt("town.setNameCost");
      this.maxNameLength = config.getInt("town.maxNameLength");
      this.healLevel = config.getInt("town.healLevel");
      this.fix = config.getInt("town.fix");
      this.add = config.getInt("town.add");
      this.diyId = config.getInt("town.diy.id");
      this.diyLeader = Util.convert(config.getString("town.diy.leader"));
      this.diyStaff = Util.convert(config.getString("town.diy.staff"));
      this.preLeader = Util.convert(config.getString("town.pre.leader"));
      this.preStaff = Util.convert(config.getString("town.pre.staff"));
      this.statusOnline = Util.convert(config.getString("town.status.online"));
      this.statusOffline = Util.convert(config.getString("town.status.offline"));
      this.size = config.getInt("town.size");
      this.world = config.getString("town.world");
      this.level = config.getInt("town.level");
      this.ask = config.getInt("town.ask");
      this.yMin = config.getInt("town.range.min");
      this.yMax = config.getInt("town.range.max");
      this.name = config.getString("town.name");
      this.spawnHeight = config.getInt("town.spawn.height");
      this.spawnRadius = config.getInt("town.spawn.radius");
      this.spawnId = config.getInt("town.spawn.id");
      String ss = config.getString("town.show.id");
      if (ss.indexOf(":") != -1) {
         this.showId = Integer.parseInt(ss.split(":")[0]);
         this.showSmallId = Integer.parseInt(ss.split(":")[1]);
      } else {
         this.showId = Integer.parseInt(ss);
      }

      this.showHeight = config.getInt("town.show.height");
      this.addFlagsHash = new HashMap();

      for(String s : config.getStringList("town.addFlags")) {
         this.addFlagsHash.put(s.split(":")[0], Integer.parseInt(s.split(":")[1]));
      }

      this.posNameHash = new HashMap();

      for(String s : config.getStringList("pos")) {
         int pos = Integer.parseInt(s.split(" ")[0]);
         String name = s.split(" ")[1];
         this.posNameHash.put(pos, name);
      }

      this.delItem = UtilItems.getItem(this.pn, "main_del");
   }

   private String get(int id) {
      return UtilFormat.format(Main.getPn(), id);
   }
}
