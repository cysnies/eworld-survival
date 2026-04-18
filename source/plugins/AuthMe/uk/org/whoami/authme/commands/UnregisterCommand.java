package uk.org.whoami.authme.commands;

import java.security.NoSuchAlgorithmException;
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
import uk.org.whoami.authme.cache.auth.PlayerCache;
import uk.org.whoami.authme.cache.backup.FileCache;
import uk.org.whoami.authme.cache.limbo.LimboCache;
import uk.org.whoami.authme.datasource.DataSource;
import uk.org.whoami.authme.events.SpawnTeleportEvent;
import uk.org.whoami.authme.security.PasswordSecurity;
import uk.org.whoami.authme.settings.Messages;
import uk.org.whoami.authme.settings.PlayersLogs;
import uk.org.whoami.authme.settings.Settings;
import uk.org.whoami.authme.settings.Spawn;
import uk.org.whoami.authme.task.MessageTask;
import uk.org.whoami.authme.task.TimeoutTask;

public class UnregisterCommand implements CommandExecutor {
   private Messages m = Messages.getInstance();
   private PlayersLogs pllog = PlayersLogs.getInstance();
   public AuthMe plugin;
   private DataSource database;
   private FileCache playerCache = new FileCache();

   public UnregisterCommand(AuthMe plugin, DataSource database) {
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
         } else if (args.length != 1) {
            player.sendMessage(this.m._("usage_unreg"));
            return true;
         } else {
            try {
               if (PasswordSecurity.comparePasswordWithHash(args[0], PlayerCache.getInstance().getAuth(name).getHash(), name)) {
                  if (!this.database.removeAuth(name)) {
                     player.sendMessage("error");
                     return true;
                  }

                  if (Settings.isForcedRegistrationEnabled) {
                     player.getInventory().setArmorContents(new ItemStack[4]);
                     player.getInventory().setContents(new ItemStack[36]);
                     player.saveData();
                     PlayerCache.getInstance().removePlayer(player.getName().toLowerCase());
                     LimboCache.getInstance().addLimboPlayer(player);
                     int delay = Settings.getRegistrationTimeout * 20;
                     int interval = Settings.getWarnMessageInterval;
                     BukkitScheduler sched = sender.getServer().getScheduler();
                     if (delay != 0) {
                        BukkitTask id = sched.runTaskLater(this.plugin, new TimeoutTask(this.plugin, name), (long)delay);
                        LimboCache.getInstance().getLimboPlayer(name).setTimeoutTaskId(id.getTaskId());
                     }

                     sched.scheduleSyncDelayedTask(this.plugin, new MessageTask(this.plugin, name, this.m._("reg_msg"), interval));
                     if (!Settings.unRegisteredGroup.isEmpty()) {
                        Utils.getInstance().setGroup(player, Utils.groupType.UNREGISTERED);
                     }

                     player.sendMessage("unregistered");
                     ConsoleLogger.info(player.getDisplayName() + " unregistered himself");
                     if (this.plugin.notifications != null) {
                        this.plugin.notifications.showNotification(new Notification("[AuthMe] " + player.getName() + " unregistered himself!"));
                     }

                     return true;
                  }

                  if (!Settings.unRegisteredGroup.isEmpty()) {
                     Utils.getInstance().setGroup(player, Utils.groupType.UNREGISTERED);
                  }

                  PlayerCache.getInstance().removePlayer(player.getName().toLowerCase());
                  if (this.playerCache.doesCacheExist(name)) {
                     this.playerCache.removeCache(name);
                  }

                  if (PlayersLogs.players.contains(player.getName())) {
                     PlayersLogs.players.remove(player.getName());
                     this.pllog.save();
                  }

                  player.sendMessage("unregistered");
                  ConsoleLogger.info(player.getDisplayName() + " unregistered himself");
                  if (this.plugin.notifications != null) {
                     this.plugin.notifications.showNotification(new Notification("[AuthMe] " + player.getName() + " unregistered himself!"));
                  }

                  if (Settings.isTeleportToSpawnEnabled) {
                     Location spawn = player.getWorld().getSpawnLocation();
                     if (this.plugin.mv != null) {
                        try {
                           spawn = this.plugin.mv.getMVWorldManager().getMVWorld(player.getWorld()).getSpawnLocation();
                        } catch (NullPointerException var11) {
                        } catch (ClassCastException var12) {
                        } catch (NoClassDefFoundError var13) {
                        }
                     }

                     if (this.plugin.essentialsSpawn != null) {
                        spawn = this.plugin.essentialsSpawn;
                     }

                     if (Spawn.getInstance().getLocation() != null) {
                        spawn = Spawn.getInstance().getLocation();
                     }

                     SpawnTeleportEvent tpEvent = new SpawnTeleportEvent(player, player.getLocation(), spawn, false);
                     this.plugin.getServer().getPluginManager().callEvent(tpEvent);
                     if (!tpEvent.isCancelled()) {
                        if (!tpEvent.getTo().getWorld().getChunkAt(tpEvent.getTo()).isLoaded()) {
                           tpEvent.getTo().getWorld().getChunkAt(tpEvent.getTo()).load();
                        }

                        player.teleport(tpEvent.getTo());
                     }
                  }

                  return true;
               }

               player.sendMessage(this.m._("wrong_pwd"));
            } catch (NoSuchAlgorithmException ex) {
               ConsoleLogger.showError(ex.getMessage());
               sender.sendMessage("Internal Error please read the server log");
            }

            return true;
         }
      }
   }
}
