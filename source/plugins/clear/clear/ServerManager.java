package clear;

import lib.config.ReloadConfigEvent;
import lib.hashList.HashList;
import lib.hashList.HashListImpl;
import lib.util.Util;
import lib.util.UtilConfig;
import lib.util.UtilFormat;
import org.bukkit.Server;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.scheduler.BukkitScheduler;

public class ServerManager implements Listener {
   private Main main;
   private Server server;
   private BukkitScheduler scheduler;
   private String pn;
   private int checkInterval;
   private boolean broadcast;
   private AutoTpsTip autoTpsTip;
   private HashList levelList;

   public ServerManager(Main main) {
      super();
      this.main = main;
      this.server = main.getServer();
      this.scheduler = main.getServer().getScheduler();
      this.pn = main.getPn();
      this.loadConfig(UtilConfig.getConfig(this.pn));
      main.getServer().getPluginManager().registerEvents(this, main);
      this.autoTpsTip = new AutoTpsTip();
      this.scheduler.scheduleSyncDelayedTask(main, this.autoTpsTip, (long)(this.checkInterval * 20));
   }

   @EventHandler(
      priority = EventPriority.LOW
   )
   public void onReloadConfig(ReloadConfigEvent e) {
      if (e.getCallPlugin().equals(this.pn)) {
         this.loadConfig(e.getConfig());
      }

   }

   public int getServerStatus() {
      double tps = (double)((int)Util.getTps());
      if (tps != (double)-1.0F) {
         for(Level level : this.levelList) {
            if (tps >= level.getThreshold()) {
               return this.levelList.indexOf(level);
            }
         }
      }

      return 3;
   }

   private void loadConfig(YamlConfiguration config) {
      this.checkInterval = config.getInt("tps.checkInterval");
      this.broadcast = config.getBoolean("tps.broadcast");
      this.levelList = new HashListImpl();

      String[] var9;
      for(String s : var9 = new String[]{"good", "fine", "bad", "unknown"}) {
         double threshold = config.getDouble("tps.levels." + s + ".threshold", (double)0.0F);
         String status = Util.convert(config.getString("tps.levels." + s + ".status"));
         String show = Util.convert(config.getString("tps.levels." + s + ".show"));
         Level level = new Level(threshold, status, show);
         this.levelList.add(level);
      }

   }

   class Level {
      private double threshold;
      private String status;
      private String show;

      public Level(double threshold, String status, String show) {
         super();
         this.threshold = threshold;
         this.status = status;
         this.show = show;
      }

      public double getThreshold() {
         return this.threshold;
      }

      public String getStatus() {
         return this.status;
      }

      public String getShow() {
         return this.show;
      }
   }

   class AutoTpsTip implements Runnable {
      private int preStatus = 3;

      AutoTpsTip() {
         super();
      }

      public void run() {
         int nowStatus = ServerManager.this.getServerStatus();
         if (ServerManager.this.broadcast) {
            if (this.preStatus == 3) {
               this.preStatus = nowStatus;
            } else if (nowStatus != this.preStatus) {
               String pre = ((Level)ServerManager.this.levelList.get(this.preStatus)).getStatus();
               String now = ((Level)ServerManager.this.levelList.get(nowStatus)).getStatus();
               String tip = ((Level)ServerManager.this.levelList.get(nowStatus)).getShow();
               ServerManager.this.server.broadcastMessage(UtilFormat.format(ServerManager.this.pn, "tpsChange", new Object[]{pre, now}));
               if (!tip.isEmpty()) {
                  ServerManager.this.server.broadcastMessage(tip);
               }
            }

            this.preStatus = nowStatus;
         }

         ServerManager.this.scheduler.scheduleSyncDelayedTask(ServerManager.this.main, ServerManager.this.autoTpsTip, (long)(ServerManager.this.checkInterval * 20));
      }
   }
}
