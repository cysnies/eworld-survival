package uk.org.whoami.authme.task;

import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitScheduler;
import org.bukkit.scheduler.BukkitTask;
import uk.org.whoami.authme.AuthMe;
import uk.org.whoami.authme.cache.auth.PlayerCache;
import uk.org.whoami.authme.cache.limbo.LimboCache;

public class MessageTask implements Runnable {
   private AuthMe plugin;
   private String name;
   private String msg;
   private int interval;

   public MessageTask(AuthMe plugin, String name, String msg, int interval) {
      super();
      this.plugin = plugin;
      this.name = name;
      this.msg = msg;
      this.interval = interval;
   }

   public void run() {
      if (!PlayerCache.getInstance().isAuthenticated(this.name)) {
         Player[] var4;
         for(Player player : var4 = this.plugin.getServer().getOnlinePlayers()) {
            if (player.getName().toLowerCase().equals(this.name)) {
               player.sendMessage(this.msg);
               BukkitScheduler sched = this.plugin.getServer().getScheduler();
               BukkitTask late = sched.runTaskLater(this.plugin, this, (long)(this.interval * 20));
               if (LimboCache.getInstance().hasLimboPlayer(this.name)) {
                  LimboCache.getInstance().getLimboPlayer(this.name).setMessageTaskId(late.getTaskId());
               }
            }
         }

      }
   }
}
