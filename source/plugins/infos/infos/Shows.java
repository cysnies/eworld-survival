package infos;

import java.util.HashMap;
import lib.config.ReloadConfigEvent;
import lib.util.UtilConfig;
import lib.util.UtilPer;
import net.minecraft.server.v1_6_R2.Packet63WorldParticles;
import org.bukkit.Location;
import org.bukkit.Server;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.craftbukkit.v1_6_R2.entity.CraftPlayer;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;

public class Shows implements Listener {
   private static final String CHECK_PER = "per.fix.shows.checkPer";
   private Infos infos;
   private String pn;
   private Server server;
   private HashMap lastHash;
   private Check check;
   private SpecInfo move;
   private String movePer;
   private int moveInterval;
   private int moveRange;

   public Shows(Infos infos) {
      super();
      this.infos = infos;
      this.pn = Infos.getPn();
      this.server = infos.getServer();
      this.lastHash = new HashMap();
      this.loadConfig(UtilConfig.getConfig(this.pn));
      infos.getPm().registerEvents(this, infos);
      this.check = new Check((Check)null);
      this.server.getScheduler().scheduleSyncDelayedTask(infos, this.check, (long)this.moveInterval);
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
      priority = EventPriority.LOWEST
   )
   public void onPlayerQuit(PlayerQuitEvent e) {
      this.lastHash.remove(e.getPlayer());
   }

   public int getStatus(Player p) {
      if (!UtilPer.hasPer(p, this.movePer)) {
         return 0;
      } else {
         return UtilPer.hasPer(p, "per.fix.shows.checkPer") ? 1 : 2;
      }
   }

   public boolean toggleMove(Player p) {
      if (!UtilPer.checkPer(p, this.movePer)) {
         return false;
      } else {
         if (UtilPer.hasPer(p, "per.fix.shows.checkPer")) {
            UtilPer.remove(p, "per.fix.shows.checkPer");
         } else {
            UtilPer.add(p, "per.fix.shows.checkPer");
         }

         return true;
      }
   }

   private void loadConfig(YamlConfiguration config) {
      String s = "move";
      String name = config.getString("spec." + s + ".name");
      float offset = (float)config.getDouble("spec." + s + ".offset");
      float speed = (float)config.getDouble("spec." + s + ".speed");
      int count = config.getInt("spec." + s + ".count");
      this.move = new SpecInfo(name, offset, speed, count);
      this.movePer = config.getString("spec." + s + ".per");
      this.moveInterval = config.getInt("spec." + s + ".interval");
      this.moveRange = config.getInt("spec." + s + ".range");
   }

   private void checkMove() {
      Player[] var4;
      for(Player p : var4 = this.server.getOnlinePlayers()) {
         if (UtilPer.hasPer(p, this.movePer) && !UtilPer.hasPer(p, "per.fix.shows.checkPer")) {
            Location l = p.getLocation();
            if (this.lastHash.containsKey(p)) {
               Location last = (Location)this.lastHash.get(p);
               if (!last.getWorld().equals(l.getWorld()) || last.distance(l) > 0.3) {
                  Packet63WorldParticles packet = new Packet63WorldParticles(this.move.getName(), (float)l.getX(), (float)l.getY(), (float)l.getZ(), this.move.getOffset(), this.move.getOffset(), this.move.getOffset(), this.move.getSpeed(), this.move.getCount());
                  ((CraftPlayer)p).getHandle().playerConnection.sendPacket(packet);

                  for(Entity e : p.getNearbyEntities((double)this.moveRange, (double)this.moveRange, (double)this.moveRange)) {
                     if (e instanceof Player) {
                        Player tar = (Player)e;
                        if (!tar.getName().equals(p.getName())) {
                           ((CraftPlayer)tar).getHandle().playerConnection.sendPacket(packet);
                        }
                     }
                  }
               }
            }

            this.lastHash.put(p, l);
         }
      }

   }

   private class Check implements Runnable {
      private Check() {
         super();
      }

      public void run() {
         Shows.this.server.getScheduler().scheduleSyncDelayedTask(Shows.this.infos, Shows.this.check, (long)Shows.this.moveInterval);
         Shows.this.checkMove();
      }

      // $FF: synthetic method
      Check(Check var2) {
         this();
      }
   }

   private class SpecInfo {
      private String name;
      private float offset;
      private float speed;
      private int count;

      public SpecInfo(String name, float offset, float speed, int count) {
         super();
         this.name = name;
         this.offset = offset;
         this.speed = speed;
         this.count = count;
      }

      public String getName() {
         return this.name;
      }

      public float getOffset() {
         return this.offset;
      }

      public float getSpeed() {
         return this.speed;
      }

      public int getCount() {
         return this.count;
      }
   }
}
