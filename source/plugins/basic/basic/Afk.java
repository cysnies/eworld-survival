package basic;

import java.util.HashMap;
import lib.config.ReloadConfigEvent;
import lib.time.TimeEvent;
import lib.util.UtilConfig;
import lib.util.UtilFormat;
import org.bukkit.Location;
import org.bukkit.Server;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;
import sit.Sit;

public class Afk implements Listener {
   private Server server;
   private String pn;
   private Sit sit;
   private HashMap lastLocHash;
   private HashMap lastTimeHash;
   private boolean afk;
   private boolean checkSit;
   private int afkTime;
   private int afkCheckInterval;

   public Afk(Basic basic) {
      super();
      this.server = basic.getServer();
      this.pn = Basic.getPn();
      this.sit = (Sit)this.server.getPluginManager().getPlugin("sit");
      this.lastLocHash = new HashMap();
      this.lastTimeHash = new HashMap();
      this.loadConfig(UtilConfig.getConfig(this.pn));
      basic.getPm().registerEvents(this, basic);
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
   public void onPlayerQuit(PlayerQuitEvent e) {
      this.lastLocHash.remove(e.getPlayer());
      this.lastTimeHash.remove(e.getPlayer());
   }

   @EventHandler(
      priority = EventPriority.LOW
   )
   public void onTime(TimeEvent e) {
      if (this.afk && TimeEvent.getTime() % (long)this.afkCheckInterval == 0L) {
         long now = System.currentTimeMillis();

         Player[] var7;
         for(Player p : var7 = this.server.getOnlinePlayers()) {
            if (this.sit != null && this.checkSit && this.sit.isInSit(p)) {
               this.lastLocHash.put(p, p.getLocation());
               this.lastTimeHash.put(p, now);
            } else if (this.lastTimeHash.containsKey(p)) {
               if (this.isInSameBlock((Location)this.lastLocHash.get(p), p.getLocation())) {
                  if (now - (Long)this.lastTimeHash.get(p) > (long)(this.afkTime * 1000)) {
                     p.kickPlayer(UtilFormat.format(this.pn, "afkKick", new Object[]{this.afkTime}));
                  }
               } else {
                  this.lastLocHash.put(p, p.getLocation());
                  this.lastTimeHash.put(p, now);
               }
            } else {
               this.lastTimeHash.put(p, now);
               this.lastLocHash.put(p, p.getLocation());
            }
         }
      }

   }

   private void loadConfig(FileConfiguration config) {
      this.afk = config.getBoolean("afk");
      this.checkSit = config.getBoolean("checkSit");
      this.afkTime = config.getInt("afkTime");
      this.afkCheckInterval = config.getInt("afkCheckInterval");
   }

   private boolean isInSameBlock(Location l1, Location l2) {
      return l1.getWorld().equals(l2.getWorld()) && l1.getBlockX() == l2.getBlockX() && l1.getBlockY() == l2.getBlockY() && l1.getBlockZ() == l2.getBlockZ();
   }
}
