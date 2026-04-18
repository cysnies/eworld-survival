package com.minecraft001;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import lib.config.ReloadConfigEvent;
import lib.util.UtilConfig;
import lib.util.UtilFormat;
import org.bukkit.Server;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerLoginEvent;
import org.bukkit.event.player.PlayerLoginEvent.Result;

public class Add implements Listener {
   private static final long DELAY = 20L;
   private static final SimpleDateFormat sdf = new SimpleDateFormat("HH:mm");
   private String pn;
   private Server server;
   private Counter counter;
   private int startHour;
   private int startMinute;
   private int interval;
   private int kickHour;
   private int kickMinute;
   private int endHour;
   private int endMinute;
   private boolean start;
   private boolean kick;

   public Add(Main main) {
      super();
      this.pn = main.getPn();
      this.server = main.getServer();
      this.counter = new Counter();
      this.loadConfig(UtilConfig.getConfig(this.pn));
      this.server.getScheduler().scheduleSyncRepeatingTask(main, this.counter, 20L, 20L);
      this.server.getPluginManager().registerEvents(this, main);
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
      priority = EventPriority.MONITOR
   )
   public void onPlayerLogin(PlayerLoginEvent e) {
      if (this.start) {
         e.setKickMessage(this.getKickMsg());
         e.setResult(Result.KICK_OTHER);
      }

   }

   private void loadConfig(FileConfiguration config) {
      this.startHour = config.getInt("time.start.hour");
      this.startMinute = config.getInt("time.start.minute");
      this.interval = config.getInt("time.interval");
      this.kickHour = config.getInt("time.kick.hour");
      this.kickMinute = config.getInt("time.kick.minute");
      this.endHour = config.getInt("time.end.hour");
      this.endMinute = config.getInt("time.end.minute");
   }

   private void check() {
      Calendar now = Calendar.getInstance();
      if (this.start) {
         if (!this.kick) {
            if (System.currentTimeMillis() / 1000L % (long)this.interval == 0L) {
               this.server.broadcastMessage(this.getStartMsg());
            }

            Calendar c1 = Calendar.getInstance();
            c1.set(11, this.kickHour);
            c1.set(12, this.kickMinute);
            c1.set(13, 0);
            if (now.after(c1)) {
               this.kick = true;
               String msg = this.getKickMsg();

               Player[] var7;
               for(Player p : var7 = this.server.getOnlinePlayers()) {
                  p.kickPlayer(msg);
               }
            }
         }
      } else {
         Calendar c1 = Calendar.getInstance();
         c1.set(11, this.startHour);
         c1.set(12, this.startMinute);
         c1.set(13, 0);
         if (now.after(c1)) {
            Calendar c2 = Calendar.getInstance();
            c2.set(11, this.endHour);
            c2.set(12, this.endMinute);
            c2.set(13, 0);
            if (now.before(c2)) {
               this.start = true;
               this.server.broadcastMessage(this.getStartMsg());
            }
         }
      }

   }

   private String getStartMsg() {
      Calendar now = Calendar.getInstance();
      Calendar c1 = Calendar.getInstance();
      c1.set(11, this.kickHour);
      c1.set(12, this.kickMinute);
      c1.set(13, 0);
      Calendar c2 = Calendar.getInstance();
      c2.set(11, this.endHour);
      c2.set(12, this.endMinute);
      c2.set(13, 0);
      int minute = (int)((c1.getTimeInMillis() - now.getTimeInMillis()) / 1000L / 60L);
      int seconds = (int)((c1.getTimeInMillis() - now.getTimeInMillis()) / 1000L % 60L);
      String msg = UtilFormat.format(this.pn, "tip1", new Object[]{sdf.format(c1.getTime()), minute, seconds, sdf.format(c2.getTime())});
      return msg;
   }

   private String getKickMsg() {
      Calendar now = Calendar.getInstance();
      Calendar c2 = Calendar.getInstance();
      c2.set(11, this.endHour);
      c2.set(12, this.endMinute);
      c2.set(13, 0);
      int minute = (int)((c2.getTimeInMillis() - now.getTimeInMillis()) / 1000L / 60L);
      int seconds = (int)((c2.getTimeInMillis() - now.getTimeInMillis()) / 1000L % 60L);
      String msg = UtilFormat.format(this.pn, "tip2", new Object[]{sdf.format(c2.getTime()), minute, seconds});
      return msg;
   }

   class Counter implements Runnable {
      Counter() {
         super();
      }

      public void run() {
         Add.this.check();
      }
   }
}
