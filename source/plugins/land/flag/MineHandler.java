package flag;

import java.util.Calendar;
import java.util.Date;
import java.util.Random;
import land.Land;
import land.Range;
import landMain.LandManager;
import lib.config.ReloadConfigEvent;
import lib.hashList.ChanceHashList;
import lib.hashList.ChanceHashListImpl;
import lib.util.UtilConfig;
import lib.util.UtilFormat;
import lib.util.UtilPer;
import org.bukkit.Server;
import org.bukkit.World;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;

public class MineHandler implements Listener {
   private static final String FLAG_MINE = "mine";
   private Random r = new Random();
   private LandManager landManager;
   private Server server;
   private String pn;
   private Check check;
   private String per_land_admin;
   private boolean use;
   private String per;
   private String tip;
   private int checkInterval;
   private int preTip;
   private double minStone;
   private ChanceHashList oreList;

   public MineHandler(LandManager landManager) {
      super();
      this.landManager = landManager;
      this.server = landManager.getServer();
      this.pn = landManager.getLandMain().getPn();
      this.loadConfig(UtilConfig.getConfig(this.pn));
      landManager.registerEvents(this);
      landManager.register("mine", this.tip, this.use, false, true, this.per);
      this.check = new Check();
      this.server.getScheduler().scheduleSyncDelayedTask(landManager.getLandMain(), this.check, (long)(this.checkInterval * 20));
   }

   public void onCommand(CommandSender sender, Command cmd, String label, String[] args) {
      Player p = null;
      if (sender instanceof Player) {
         p = (Player)sender;
      }

      if (p == null || UtilPer.checkPer(p, this.per_land_admin)) {
         int length = args.length;
         if (length != 1 || !args[0].equals("?")) {
            if (length == 1) {
               if (args[0].equalsIgnoreCase("refreshAll")) {
                  this.refreshAll(sender);
                  return;
               }
            } else if (length == 2 && args[0].equalsIgnoreCase("refresh")) {
               this.refresh(sender, args[1]);
               return;
            }
         }

         sender.sendMessage(UtilFormat.format(this.pn, "cmdHelpHeader", new Object[]{this.get(1500)}));
         sender.sendMessage(UtilFormat.format((String)null, "cmdHelpItem", new Object[]{this.get(1505), this.get(1510)}));
         sender.sendMessage(UtilFormat.format((String)null, "cmdHelpItem", new Object[]{this.get(1515), this.get(1520)}));
      }
   }

   @EventHandler(
      priority = EventPriority.LOW
   )
   public void reloadConfig(ReloadConfigEvent e) {
      if (e.getCallPlugin().equals(this.pn)) {
         this.loadConfig(e.getConfig());
      }

   }

   private void loadConfig(YamlConfiguration config) {
      this.per_land_admin = config.getString("per_land_admin");
      this.use = config.getBoolean("mine.use");
      this.per = config.getString("mine.per");
      this.tip = config.getString("mine.tip");
      this.oreList = new ChanceHashListImpl();

      for(String s : config.getStringList("mine.ore")) {
         int id = Integer.parseInt(s.split(" ")[0]);
         int chance = Integer.parseInt(s.split(" ")[1]);
         this.oreList.addChance(id, chance);
      }

      this.checkInterval = config.getInt("mine.checkInterval");
      this.preTip = config.getInt("mine.preTip");
      this.minStone = config.getDouble("mine.minStone");
   }

   private void refresh(CommandSender sender, String name) {
      if (!(sender instanceof Player) || UtilPer.checkPer((Player)sender, this.per_land_admin)) {
         Land land = this.landManager.getLand(sender, name);
         if (land != null) {
            this.refresh(sender, land);
         }
      }
   }

   private void refreshAll(CommandSender sender) {
      if (!(sender instanceof Player) || UtilPer.checkPer((Player)sender, this.per_land_admin)) {
         for(Land land : this.landManager.getAllLands()) {
            if (land.hasFlag("mine")) {
               this.refresh(sender, land);
            }
         }

      }
   }

   private void refresh(CommandSender sender, Land land) {
      if (sender != null) {
         sender.sendMessage(UtilFormat.format(this.pn, "landMineRefresh", new Object[]{land.getName()}));
      }

      if (!land.hasFlag("mine")) {
         if (sender != null) {
            sender.sendMessage(UtilFormat.format((String)null, "fail", new Object[]{this.get(290)}));
         }

      } else if (land.getFlag("mine") / 100 >= 1 && land.getFlag("mine") / 100 <= 9 && land.getFlag("mine") % 100 >= 0 && land.getFlag("mine") % 100 <= 23) {
         int mineLevel = land.getFlag("mine") / 100;
         ChanceHashList<Integer> result = new ChanceHashListImpl();

         for(int id : this.oreList) {
            if (id == 1) {
               result.addChance(id, (int)((double)this.oreList.getChance(this.oreList.indexOf(id)) * ((double)1.0F - ((double)1.0F - this.minStone) * (double)mineLevel / (double)9.0F)));
            } else {
               double multi = (double)(this.r.nextInt(100 + mineLevel * 10) / 100);
               if (multi < (double)0.5F) {
                  multi = (double)0.5F;
               }

               result.addChance(id, (int)((double)this.oreList.getChance(this.oreList.indexOf(id)) * multi));
            }
         }

         World w = this.server.getWorld(land.getRange().getP1().getWorld());
         Range rangeCopy = land.getRange();
         rangeCopy.fit();
         int xMin = rangeCopy.getP1().getX();
         int xMax = rangeCopy.getP2().getX();
         int yMin = rangeCopy.getP1().getY();
         int yMax = rangeCopy.getP2().getY();
         int zMin = rangeCopy.getP1().getZ();
         int zMax = rangeCopy.getP2().getZ();

         for(int x = xMin; x <= xMax; ++x) {
            for(int y = yMin; y <= yMax; ++y) {
               for(int z = zMin; z <= zMax; ++z) {
                  w.getBlockAt(x, y, z).setTypeId((Integer)result.getRandom());
               }
            }
         }

         if (sender != null) {
            sender.sendMessage(UtilFormat.format(this.pn, "landMineRefresh2", new Object[]{land.getName()}));
         }

      } else {
         if (sender != null) {
            sender.sendMessage(UtilFormat.format((String)null, "fail", new Object[]{this.get(295)}));
         }

      }
   }

   private String get(int id) {
      return UtilFormat.format(this.pn, id);
   }

   class Check implements Runnable {
      long lastCheckTime = System.currentTimeMillis();

      public Check() {
         super();
      }

      public void run() {
         for(Land land : MineHandler.this.landManager.getAllLands()) {
            if (land.hasFlag("mine")) {
               int hour = land.getFlag("mine") % 100;
               if (hour >= 0 && hour <= 23) {
                  this.checkRefresh(land, hour);
               }
            }
         }

         this.lastCheckTime = System.currentTimeMillis();
         MineHandler.this.server.getScheduler().scheduleSyncDelayedTask(MineHandler.this.landManager.getLandMain(), MineHandler.this.check, (long)(MineHandler.this.checkInterval * 20));
      }

      private void checkRefresh(Land land, int hour) {
         Calendar tarCalendar = Calendar.getInstance();
         tarCalendar.setTime(new Date());
         tarCalendar.set(11, hour);
         tarCalendar.set(12, 0);
         tarCalendar.set(13, 0);
         Calendar preCalendar = Calendar.getInstance();
         preCalendar.setTime(new Date(this.lastCheckTime));
         Calendar nowCalendar = Calendar.getInstance();
         nowCalendar.setTime(new Date());
         if (preCalendar.compareTo(tarCalendar) < 0 && nowCalendar.compareTo(tarCalendar) > 0) {
            String msg = UtilFormat.format(MineHandler.this.pn, "landMineTip2", new Object[]{land.getName(), land.getId(), land.getRange().getP1().getWorld(), land.getRange().getXCenter(), land.getRange().getYCenter(), land.getRange().getZCenter()});
            MineHandler.this.server.broadcastMessage(msg);
            MineHandler.this.refresh((CommandSender)null, (Land)land);
         } else {
            tarCalendar.setTimeInMillis(tarCalendar.getTimeInMillis() - (long)(MineHandler.this.preTip * 60 * 1000));
            if (preCalendar.compareTo(tarCalendar) < 0 && nowCalendar.compareTo(tarCalendar) > 0) {
               MineHandler.this.server.broadcastMessage(UtilFormat.format(MineHandler.this.pn, "landMineTip", new Object[]{land.getName(), land.getId(), land.getRange().getP1().getWorld(), land.getRange().getXCenter(), land.getRange().getYCenter(), land.getRange().getZCenter(), MineHandler.this.preTip}));
            }

         }
      }
   }
}
