package jb;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import lib.IconMenu;
import lib.util.Util;
import lib.util.UtilConfig;
import lib.util.UtilFormat;
import lib.util.UtilIconMenu;
import lib.util.UtilItems;
import lib.util.UtilPer;
import lib.util.UtilSpeed;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

public class Jb implements Listener {
   private static final int SIZE = 45;
   private static String SPEED = "jb";
   private Main main;
   private static String pn;
   private static String savePath;
   private static String per_jb_op;
   private int speed;
   private int limit;
   private int cost;
   private int delay;
   private static HashMap infoHash;
   private static final int MAX_SIZE = 20;

   public Jb(Main main) {
      super();
      this.main = main;
      pn = main.getPn();
      savePath = main.getPluginPath() + File.separator + pn + File.separator + "data";
      (new File(savePath)).mkdirs();
      this.loadConfig(UtilConfig.getConfig(pn));
      Bukkit.getPluginManager().registerEvents(this, main);
      this.loadData();
      UtilSpeed.register(pn, SPEED);
   }

   @EventHandler(
      priority = EventPriority.MONITOR
   )
   public void onPlayerJoin(PlayerJoinEvent e) {
      Show show = new Show(e.getPlayer());
      Bukkit.getScheduler().scheduleSyncDelayedTask(this.main, show, (long)this.delay);
   }

   public int getCost() {
      return this.cost;
   }

   public void create(Player p, String msg) {
      if (UtilSpeed.check(p, pn, SPEED, this.speed)) {
         Info info = (Info)infoHash.get(p.getName());
         if (info != null && info.status == 0) {
            p.sendMessage(get(285));
         } else {
            this.delete(p, false);
            info = new Info(p.getName(), msg, Jb.Pos.getPos(p.getLocation()));
            if (info.save()) {
               if (this.cost > 0) {
                  Util.addDebt(p.getName(), this.cost, get(215));
               }

               p.sendMessage(get(220));
            }

         }
      }
   }

   public void pos(Player p) {
      if (UtilSpeed.check(p, pn, SPEED, this.speed)) {
         Info info = (Info)infoHash.get(p.getName());
         if (info == null) {
            p.sendMessage(get(230));
         } else if (info.status == 1) {
            p.sendMessage(get(270));
         } else {
            info.setPos(Jb.Pos.getPos(p.getLocation()));
            info.save();
            p.sendMessage(UtilFormat.format(pn, "success", new Object[]{get(250)}));
            this.tip(p.getName(), get(245));
         }
      }
   }

   public void delete(Player p, boolean tip) {
      Info info = (Info)infoHash.get(p.getName());
      if (info == null) {
         if (tip) {
            p.sendMessage(UtilFormat.format(pn, "fail", new Object[]{get(200)}));
         }

      } else {
         infoHash.remove(p.getName());
         File file = new File(savePath + File.separator + p.getName() + ".yml");
         if (file.exists() && !file.delete()) {
            file.deleteOnExit();
         }

         p.sendMessage(UtilFormat.format(pn, "success", new Object[]{get(205)}));
      }
   }

   public void info(Player p, String tar) {
      tar = Util.getRealName(p, tar);
      if (tar != null) {
         if (p.getName().equals(tar) || UtilPer.checkPer(p, per_jb_op)) {
            if (UtilSpeed.check(p, pn, SPEED, this.speed)) {
               Info info = (Info)infoHash.get(tar);
               if (info == null) {
                  if (p.getName().equals(tar)) {
                     p.sendMessage(get(230));
                  } else {
                     p.sendMessage(get(260));
                  }

               } else {
                  for(String s : this.getShowList(info, false)) {
                     p.sendMessage(s);
                  }

                  if (p.getName().equals(tar)) {
                     p.sendMessage(get(315));
                     p.sendMessage(get(330));
                  }

                  if (UtilPer.hasPer(p, per_jb_op)) {
                     p.sendMessage(get(320));
                     p.sendMessage(get(335));
                  }

               }
            }
         }
      }
   }

   public void end(Player p, String tar) {
      if (UtilPer.checkPer(p, per_jb_op)) {
         tar = Util.getRealName(p, tar);
         if (tar != null) {
            Info info = (Info)infoHash.get(tar);
            if (info == null) {
               p.sendMessage(get(260));
            } else if (info.status == 1) {
               p.sendMessage(get(265));
            } else {
               info.status = 1;
               info.save();
               p.sendMessage(UtilFormat.format(pn, "overTip2", new Object[]{tar}));
               Util.sendMsg(tar, UtilFormat.format(pn, "overTip1", new Object[]{p.getName()}));
            }
         }
      }
   }

   public void tp(Player p, String tar) {
      if (UtilPer.checkPer(p, per_jb_op)) {
         tar = Util.getRealName(p, tar);
         if (tar != null) {
            Info info = (Info)infoHash.get(tar);
            if (info == null) {
               p.sendMessage(get(260));
            } else {
               Location l = info.pos.toLoc();
               if (l == null) {
                  p.sendMessage(UtilFormat.format(pn, "fail", new Object[]{get(275)}));
               } else {
                  p.teleport(l);
                  p.sendMessage(UtilFormat.format(pn, "success", new Object[]{get(280)}));
               }
            }
         }
      }
   }

   public void add(Player p, String msg) {
      if (UtilSpeed.check(p, pn, SPEED, this.speed)) {
         Info info = (Info)infoHash.get(p.getName());
         if (info == null) {
            p.sendMessage(get(230));
         } else if (info.status == 1) {
            p.sendMessage(get(270));
         } else {
            String result = UtilFormat.format(pn, "add", new Object[]{msg});
            info.getRes().add(result);

            while(info.getRes().size() > this.limit) {
               info.getRes().remove(0);
            }

            info.save();
            p.sendMessage(UtilFormat.format(pn, "success", new Object[]{get(290)}));
            this.tip(p.getName(), UtilFormat.format(pn, "add2", new Object[]{msg}));
         }
      }
   }

   public void re(Player p, String tar, String msg) {
      if (UtilPer.checkPer(p, per_jb_op)) {
         tar = Util.getRealName(p, tar);
         if (tar != null) {
            Info info = (Info)infoHash.get(tar);
            if (info == null) {
               p.sendMessage(get(260));
            } else {
               String result = UtilFormat.format(pn, "re", new Object[]{p.getName(), msg});
               info.getRes().add(result);

               while(info.getRes().size() > this.limit) {
                  info.getRes().remove(0);
               }

               info.save();
               p.sendMessage(UtilFormat.format(pn, "success", new Object[]{get(295)}));
               Util.sendMsg(tar, UtilFormat.format(pn, "re2", new Object[]{p.getName(), msg}));
            }
         }
      }
   }

   public void list(Player p) {
      if (UtilPer.checkPer(p, per_jb_op)) {
         String name = get(175);
         IconMenu.OptionClickEventHandler handler = new HandlerShowList(p);
         IconMenu.Info info = UtilIconMenu.register(name, 45, true, handler);
         int index = 0;

         for(Info in : infoHash.values()) {
            if (in.status == 0) {
               info.setItem(index, this.getJbItem(in));
               ++index;
               if (index >= 45) {
                  break;
               }
            }
         }

         if (index == 0) {
            p.sendMessage(get(300));
            UtilIconMenu.unregister(info);
         } else {
            UtilIconMenu.open(p, info, (String)null, (Inventory)null);
         }

      }
   }

   private ItemStack getJbItem(Info info) {
      ItemStack is = UtilItems.getItem(pn, "main_info").clone();
      ItemMeta im = is.getItemMeta();
      im.setDisplayName(info.name);
      List<String> lore = this.getShowList(info, true);
      im.setLore(lore);
      is.setItemMeta(im);
      return is;
   }

   private List getShowList(Info info, boolean lines) {
      List<String> list = new ArrayList();
      String result;
      if (info.status == 0) {
         result = get(235);
      } else {
         result = get(240);
      }

      list.add(get(325));
      String s0 = UtilFormat.format(pn, "show", new Object[]{result, info.name, info.msg});
      if (lines) {
         String s2 = s0;

         while(true) {
            list.add(s2.substring(0, Math.min(20, s2.length())));
            if (s2.length() <= 20) {
               break;
            }

            s2 = s2.substring(20);
         }
      } else {
         list.add(s0);
      }

      for(String s : info.res) {
         if (lines) {
            String s2 = s;

            while(true) {
               list.add(s2.substring(0, Math.min(20, s2.length())));
               if (s2.length() <= 20) {
                  break;
               }

               s2 = s2.substring(20);
            }
         } else {
            list.add(s);
         }
      }

      list.add(get(325));
      return list;
   }

   private void tip(String name, String reason) {
      String msg = UtilFormat.format(pn, "newTip", new Object[]{name, reason});
      int notice = 0;

      Player[] var8;
      for(Player p : var8 = Bukkit.getOnlinePlayers()) {
         if (UtilPer.hasPer(p, per_jb_op)) {
            ++notice;
            p.sendMessage(msg);
         }
      }

      if (notice == 0) {
         Util.sendMsg(name, get(255));
      } else {
         Util.sendMsg(name, UtilFormat.format(pn, "notice", new Object[]{notice}));
      }

   }

   private static void sendTip(Player p) {
      p.sendMessage(get(305));
      if (UtilPer.hasPer(p, per_jb_op)) {
         for(Info info : infoHash.values()) {
            if (info.status == 0) {
               p.sendMessage(get(310));
               break;
            }
         }
      }

   }

   private void loadData() {
      infoHash = new HashMap();

      File[] var4;
      for(File file : var4 = (new File(savePath)).listFiles()) {
         if (file.isFile() && file.getName().endsWith(".yml")) {
            String name = file.getName().substring(0, file.getName().length() - 4);
            YamlConfiguration config = new YamlConfiguration();

            try {
               config.load(file);
               Info info = Jb.Info.load(config);
               if (info != null) {
                  infoHash.put(name, info);
                  continue;
               }
            } catch (FileNotFoundException var8) {
               continue;
            } catch (IOException var9) {
               continue;
            } catch (InvalidConfigurationException var10) {
            }

            if (!file.delete()) {
               file.deleteOnExit();
            }
         }
      }

   }

   private void loadConfig(YamlConfiguration config) {
      per_jb_op = config.getString("per_jb_op");
      this.speed = config.getInt("speed");
      this.limit = config.getInt("limit");
      this.cost = config.getInt("cost");
      this.delay = config.getInt("delay");
   }

   private static String get(int id) {
      return UtilFormat.format(pn, id);
   }

   private static class Pos {
      String world;
      int x;
      int y;
      int z;

      public Pos(String world, int x, int y, int z) {
         super();
         this.world = world;
         this.x = x;
         this.y = y;
         this.z = z;
      }

      public static Pos getPos(Location l) {
         return new Pos(l.getWorld().getName(), l.getBlockX(), l.getBlockY(), l.getBlockZ());
      }

      public Location toLoc() {
         World w = Bukkit.getWorld(this.world);
         if (w == null) {
            return null;
         } else {
            Location l = new Location(w, (double)this.x, (double)this.y, (double)this.z);
            return l;
         }
      }
   }

   private static class Info {
      String name;
      String msg;
      int status;
      Pos pos;
      List res;

      public Info() {
         super();
      }

      public Info(String name, String msg, Pos pos) {
         super();
         this.name = name;
         this.msg = msg;
         this.pos = pos;
         this.res = new ArrayList();
      }

      public void setName(String name) {
         this.name = name;
      }

      public void setMsg(String msg) {
         this.msg = msg;
      }

      public void setStatus(int status) {
         this.status = status;
      }

      public void setPos(Pos pos) {
         this.pos = pos;
      }

      public List getRes() {
         return this.res;
      }

      public void setRes(List res) {
         this.res = res;
      }

      public static Info load(YamlConfiguration config) {
         try {
            Info info = new Info();
            String name = config.getString("name");
            String msg = config.getString("msg");
            int status = config.getInt("status");
            String world = config.getString("pos.world");
            int x = config.getInt("pos.x");
            int y = config.getInt("pos.y");
            int z = config.getInt("pos.z");
            Pos pos = new Pos(world, x, y, z);
            List<String> res = config.getStringList("res");
            info.setName(name);
            info.setMsg(msg);
            info.setStatus(status);
            info.setPos(pos);
            info.setRes(res);
            return info;
         } catch (Exception var11) {
            return null;
         }
      }

      public boolean save() {
         File file = new File(Jb.savePath + File.separator + this.name + ".yml");

         try {
            file.createNewFile();
            YamlConfiguration config = new YamlConfiguration();
            config.set("name", this.name);
            config.set("msg", this.msg);
            config.set("status", this.status);
            config.set("pos.world", this.pos.world);
            config.set("pos.x", this.pos.x);
            config.set("pos.y", this.pos.y);
            config.set("pos.z", this.pos.z);
            config.set("res", this.res);
            config.save(file);
            Jb.infoHash.put(this.name, this);
            return true;
         } catch (IOException var3) {
            Util.sendConsoleMessage(UtilFormat.format(Jb.pn, "fail", new Object[]{Jb.get(210)}));
            return false;
         }
      }
   }

   private static class Show implements Runnable {
      private Player p;

      public Show(Player p) {
         super();
         this.p = p;
      }

      public void run() {
         if (this.p.isOnline()) {
            Jb.sendTip(this.p);
         }

      }
   }

   private class HandlerShowList implements IconMenu.OptionClickEventHandler {
      private Player p;

      public HandlerShowList(Player p) {
         super();
         this.p = p;
      }

      public void onOptionClick(IconMenu.OptionClickEvent event) {
         int pos = event.getPos();

         try {
            ItemStack is = event.getInfo().getInv(this.p).getItem(pos);
            if (is != null) {
               event.setWillClose(true);
               String tar = is.getItemMeta().getDisplayName();
               if (event.isRight()) {
                  Jb.this.end(this.p, tar);
               } else {
                  Jb.this.tp(this.p, tar);
               }
            }
         } catch (Exception var5) {
         }

      }
   }
}
