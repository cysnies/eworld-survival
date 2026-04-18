package uk.org.whoami.authme.task;

import org.bukkit.GameMode;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import uk.org.whoami.authme.ConsoleLogger;
import uk.org.whoami.authme.cache.auth.PlayerCache;
import uk.org.whoami.authme.cache.backup.FileCache;
import uk.org.whoami.authme.cache.limbo.LimboCache;
import uk.org.whoami.authme.cache.limbo.LimboPlayer;
import uk.org.whoami.authme.listener.AuthMePlayerListener;
import uk.org.whoami.authme.settings.Messages;

public class TimeoutTask implements Runnable {
   private JavaPlugin plugin;
   private String name;
   private Messages m = Messages.getInstance();
   private FileCache playerCache = new FileCache();

   public TimeoutTask(JavaPlugin plugin, String name) {
      super();
      this.plugin = plugin;
      this.name = name;
   }

   public String getName() {
      return this.name;
   }

   public void run() {
      if (!PlayerCache.getInstance().isAuthenticated(this.name)) {
         Player[] var4;
         for(Player player : var4 = this.plugin.getServer().getOnlinePlayers()) {
            if (player.getName().toLowerCase().equals(this.name)) {
               if (LimboCache.getInstance().hasLimboPlayer(this.name)) {
                  LimboPlayer inv = LimboCache.getInstance().getLimboPlayer(this.name);
                  player.getServer().getScheduler().cancelTask(inv.getTimeoutTaskId());
                  if (this.playerCache.doesCacheExist(this.name)) {
                     this.playerCache.removeCache(this.name);
                  }
               }

               int gm = (Integer)AuthMePlayerListener.gameMode.get(this.name);
               player.setGameMode(GameMode.getByValue(gm));
               ConsoleLogger.info("Set " + player.getName() + " to gamemode: " + GameMode.getByValue(gm).name());
               player.kickPlayer(this.m._("timeout"));
               break;
            }
         }

      }
   }
}
