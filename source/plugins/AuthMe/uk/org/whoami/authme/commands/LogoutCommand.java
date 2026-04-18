package uk.org.whoami.authme.commands;

import me.muizers.Notifications.Notification;
import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitScheduler;
import org.bukkit.scheduler.BukkitTask;
import uk.org.whoami.authme.AuthMe;
import uk.org.whoami.authme.ConsoleLogger;
import uk.org.whoami.authme.Utils;
import uk.org.whoami.authme.cache.auth.PlayerAuth;
import uk.org.whoami.authme.cache.auth.PlayerCache;
import uk.org.whoami.authme.cache.backup.DataFileCache;
import uk.org.whoami.authme.cache.backup.FileCache;
import uk.org.whoami.authme.cache.limbo.LimboCache;
import uk.org.whoami.authme.datasource.DataSource;
import uk.org.whoami.authme.events.AuthMeTeleportEvent;
import uk.org.whoami.authme.settings.Messages;
import uk.org.whoami.authme.settings.PlayersLogs;
import uk.org.whoami.authme.settings.Settings;
import uk.org.whoami.authme.settings.Spawn;
import uk.org.whoami.authme.task.MessageTask;
import uk.org.whoami.authme.task.TimeoutTask;

public class LogoutCommand implements CommandExecutor {
   private Messages m = Messages.getInstance();
   private PlayersLogs pllog = PlayersLogs.getInstance();
   private AuthMe plugin;
   private DataSource database;
   private Utils utils = Utils.getInstance();
   private FileCache playerBackup = new FileCache();

   public LogoutCommand(AuthMe plugin, DataSource database) {
      super();
      this.plugin = plugin;
      this.database = database;
   }

   public boolean onCommand(CommandSender sender, Command cmnd, String label, String[] args) {
      if (!(sender instanceof Player)) {
         return true;
      } else if (!this.plugin.authmePermissible(sender, "authme." + label.toLowerCase())) {
         sender.sendMessage(this.m._("no_perm"));
         return true;
      } else {
         Player player = (Player)sender;
         String name = player.getName().toLowerCase();
         if (!PlayerCache.getInstance().isAuthenticated(name)) {
            player.sendMessage(this.m._("not_logged_in"));
            return true;
         } else {
            PlayerAuth auth = PlayerCache.getInstance().getAuth(name);
            auth.setIp("198.18.0.1");
            this.database.updateSession(auth);
            PlayerCache.getInstance().removePlayer(name);
            LimboCache.getInstance().addLimboPlayer(player, this.utils.removeAll(player));
            LimboCache.getInstance().addLimboPlayer(player);
            if (Settings.protectInventoryBeforeLogInEnabled) {
               player.getInventory().setArmorContents(new ItemStack[4]);
               player.getInventory().setContents(new ItemStack[36]);
               DataFileCache playerData = new DataFileCache(player.getInventory().getContents(), player.getInventory().getArmorContents());
               this.playerBackup.createCache(name, playerData, LimboCache.getInstance().getLimboPlayer(name).getGroup(), LimboCache.getInstance().getLimboPlayer(name).getOperator(), LimboCache.getInstance().getLimboPlayer(name).isFlying());
            }

            if (Settings.isTeleportToSpawnEnabled) {
               Location spawnLoc = player.getWorld().getSpawnLocation();
               if (this.plugin.essentialsSpawn != null) {
                  spawnLoc = this.plugin.essentialsSpawn;
               }

               if (Spawn.getInstance().getLocation() != null) {
                  spawnLoc = Spawn.getInstance().getLocation();
               }

               AuthMeTeleportEvent tpEvent = new AuthMeTeleportEvent(player, spawnLoc);
               this.plugin.getServer().getPluginManager().callEvent(tpEvent);
               if (!tpEvent.isCancelled()) {
                  if (!tpEvent.getTo().getWorld().getChunkAt(tpEvent.getTo()).isLoaded()) {
                     tpEvent.getTo().getWorld().getChunkAt(tpEvent.getTo()).load();
                  }

                  player.teleport(tpEvent.getTo());
               }
            }

            int delay = Settings.getRegistrationTimeout * 20;
            int interval = Settings.getWarnMessageInterval;
            BukkitScheduler sched = sender.getServer().getScheduler();
            if (delay != 0) {
               BukkitTask id = sched.runTaskLater(this.plugin, new TimeoutTask(this.plugin, name), (long)delay);
               LimboCache.getInstance().getLimboPlayer(name).setTimeoutTaskId(id.getTaskId());
            }

            BukkitTask msgT = sched.runTask(this.plugin, new MessageTask(this.plugin, name, this.m._("login_msg"), interval));
            LimboCache.getInstance().getLimboPlayer(name).setMessageTaskId(msgT.getTaskId());

            try {
               if (PlayersLogs.players.contains(player.getName())) {
                  PlayersLogs.players.remove(player.getName());
                  this.pllog.save();
               }
            } catch (NullPointerException var13) {
            }

            player.sendMessage(this.m._("logout"));
            ConsoleLogger.info(player.getDisplayName() + " logged out");
            if (this.plugin.notifications != null) {
               this.plugin.notifications.showNotification(new Notification("[AuthMe] " + player.getName() + " logged out!"));
            }

            return true;
         }
      }
   }
}
