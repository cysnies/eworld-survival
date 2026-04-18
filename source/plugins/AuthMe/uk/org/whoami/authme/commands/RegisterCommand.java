package uk.org.whoami.authme.commands;

import java.security.NoSuchAlgorithmException;
import java.util.Date;
import me.muizers.Notifications.Notification;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitTask;
import uk.org.whoami.authme.AuthMe;
import uk.org.whoami.authme.ConsoleLogger;
import uk.org.whoami.authme.Utils;
import uk.org.whoami.authme.cache.auth.PlayerAuth;
import uk.org.whoami.authme.cache.auth.PlayerCache;
import uk.org.whoami.authme.cache.limbo.LimboCache;
import uk.org.whoami.authme.cache.limbo.LimboPlayer;
import uk.org.whoami.authme.datasource.DataSource;
import uk.org.whoami.authme.events.RegisterTeleportEvent;
import uk.org.whoami.authme.security.PasswordSecurity;
import uk.org.whoami.authme.security.RandomString;
import uk.org.whoami.authme.settings.Messages;
import uk.org.whoami.authme.settings.PlayersLogs;
import uk.org.whoami.authme.settings.Settings;
import uk.org.whoami.authme.settings.Spawn;
import uk.org.whoami.authme.task.MessageTask;
import uk.org.whoami.authme.task.TimeoutTask;

public class RegisterCommand implements CommandExecutor {
   private Messages m = Messages.getInstance();
   private PlayersLogs pllog = PlayersLogs.getInstance();
   private DataSource database;
   public boolean isFirstTimeJoin;
   public PlayerAuth auth;
   public AuthMe plugin;

   public RegisterCommand(DataSource database, AuthMe plugin) {
      super();
      this.database = database;
      this.isFirstTimeJoin = false;
      this.plugin = plugin;
   }

   public boolean onCommand(CommandSender sender, Command cmnd, String label, String[] args) {
      if (!(sender instanceof Player)) {
         return true;
      } else if (!this.plugin.authmePermissible(sender, "authme." + label.toLowerCase())) {
         sender.sendMessage(this.m._("no_perm"));
         return true;
      } else {
         final Player player = (Player)sender;
         final String name = player.getName().toLowerCase();
         final String ipA = player.getAddress().getAddress().getHostAddress();
         if (Settings.bungee && this.plugin.realIp.containsKey(name)) {
            ipA = (String)this.plugin.realIp.get(name);
         }

         String ip = ipA;
         if (PlayerCache.getInstance().isAuthenticated(name)) {
            player.sendMessage(this.m._("logged_in"));
            return true;
         } else if (!Settings.isRegistrationEnabled) {
            player.sendMessage(this.m._("reg_disabled"));
            return true;
         } else if (this.database.isAuthAvailable(player.getName().toLowerCase())) {
            player.sendMessage(this.m._("user_regged"));
            if (this.pllog.getStringList("players").contains(player.getName())) {
               this.pllog.getStringList("players").remove(player.getName());
            }

            return true;
         } else if (Settings.getmaxRegPerIp > 0 && !this.plugin.authmePermissible(sender, "authme.allow2accounts") && this.database.getAllAuthsByIp(ipA).size() >= Settings.getmaxRegPerIp) {
            player.sendMessage(this.m._("max_reg"));
            return true;
         } else {
            if (Settings.emailRegistration && !Settings.getmailAccount.isEmpty()) {
               if (!args[0].contains("@")) {
                  player.sendMessage(this.m._("usage_reg"));
                  return true;
               }

               if (Settings.doubleEmailCheck) {
                  if (args.length < 2) {
                     player.sendMessage(this.m._("usage_reg"));
                     return true;
                  }

                  if (!args[0].equals(args[1])) {
                     player.sendMessage(this.m._("usage_reg"));
                     return true;
                  }
               }

               final String email = args[0];
               if (Settings.getmaxRegPerEmail > 0 && !this.plugin.authmePermissible(sender, "authme.allow2accounts") && this.database.getAllAuthsByEmail(email).size() >= Settings.getmaxRegPerEmail) {
                  player.sendMessage(this.m._("max_reg"));
                  return true;
               }

               RandomString rand = new RandomString(Settings.getRecoveryPassLength);
               final String thePass = rand.nextString();
               if (!thePass.isEmpty()) {
                  Bukkit.getScheduler().runTaskAsynchronously(this.plugin, new Runnable() {
                     public void run() {
                        if (PasswordSecurity.userSalt.containsKey(name)) {
                           try {
                              String hashnew = PasswordSecurity.getHash(Settings.getPasswordHash, thePass, name);
                              PlayerAuth fAuth = new PlayerAuth(name, hashnew, (String)PasswordSecurity.userSalt.get(name), ipA, (new Date()).getTime(), (int)player.getLocation().getX(), (int)player.getLocation().getY(), (int)player.getLocation().getZ(), player.getLocation().getWorld().getName(), email);
                              RegisterCommand.this.database.saveAuth(fAuth);
                              RegisterCommand.this.database.updateEmail(fAuth);
                              RegisterCommand.this.database.updateSession(fAuth);
                              RegisterCommand.this.plugin.mail.main(fAuth, thePass);
                           } catch (NoSuchAlgorithmException e) {
                              ConsoleLogger.showError(e.getMessage());
                           }
                        } else {
                           try {
                              String hashnew = PasswordSecurity.getHash(Settings.getPasswordHash, thePass, name);
                              PlayerAuth fAuth = new PlayerAuth(name, hashnew, ipA, (new Date()).getTime(), (int)player.getLocation().getX(), (int)player.getLocation().getY(), (int)player.getLocation().getZ(), player.getLocation().getWorld().getName(), email);
                              RegisterCommand.this.database.saveAuth(fAuth);
                              RegisterCommand.this.database.updateEmail(fAuth);
                              RegisterCommand.this.database.updateSession(fAuth);
                              RegisterCommand.this.plugin.mail.main(fAuth, thePass);
                           } catch (NoSuchAlgorithmException e) {
                              ConsoleLogger.showError(e.getMessage());
                           }
                        }

                     }
                  });
                  if (!Settings.getRegisteredGroup.isEmpty()) {
                     Utils.getInstance().setGroup(player, Utils.groupType.REGISTERED);
                  }

                  player.sendMessage(this.m._("vb_nonActiv"));
                  String msg = this.m._("login_msg");
                  int time = Settings.getRegistrationTimeout * 20;
                  int msgInterval = Settings.getWarnMessageInterval;
                  if (time != 0) {
                     Bukkit.getScheduler().cancelTask(LimboCache.getInstance().getLimboPlayer(name).getTimeoutTaskId());
                     BukkitTask id = Bukkit.getScheduler().runTaskLater(this.plugin, new TimeoutTask(this.plugin, name), (long)time);
                     LimboCache.getInstance().getLimboPlayer(name).setTimeoutTaskId(id.getTaskId());
                  }

                  Bukkit.getScheduler().cancelTask(LimboCache.getInstance().getLimboPlayer(name).getMessageTaskId());
                  BukkitTask nwMsg = Bukkit.getScheduler().runTask(this.plugin, new MessageTask(this.plugin, name, msg, msgInterval));
                  LimboCache.getInstance().getLimboPlayer(name).setMessageTaskId(nwMsg.getTaskId());
                  LimboCache.getInstance().deleteLimboPlayer(name);
                  if (Settings.isTeleportToSpawnEnabled) {
                     World world = player.getWorld();
                     Location loca = world.getSpawnLocation();
                     if (this.plugin.mv != null) {
                        try {
                           loca = this.plugin.mv.getMVWorldManager().getMVWorld(world).getSpawnLocation();
                        } catch (NullPointerException var19) {
                        } catch (ClassCastException var20) {
                        } catch (NoClassDefFoundError var21) {
                        }
                     }

                     if (this.plugin.essentialsSpawn != null) {
                        loca = this.plugin.essentialsSpawn;
                     }

                     if (Spawn.getInstance().getLocation() != null) {
                        loca = Spawn.getInstance().getLocation();
                     }

                     RegisterTeleportEvent tpEvent = new RegisterTeleportEvent(player, loca);
                     this.plugin.getServer().getPluginManager().callEvent(tpEvent);
                     if (!tpEvent.isCancelled()) {
                        if (!tpEvent.getTo().getWorld().getChunkAt(tpEvent.getTo()).isLoaded()) {
                           tpEvent.getTo().getWorld().getChunkAt(tpEvent.getTo()).load();
                        }

                        player.teleport(tpEvent.getTo());
                     }
                  }

                  this.isFirstTimeJoin = true;
                  if (player.getGameMode() != GameMode.CREATIVE) {
                     player.setAllowFlight(false);
                  }

                  player.setFlying(false);
                  player.saveData();
                  if (!Settings.noConsoleSpam) {
                     ConsoleLogger.info(player.getName() + " registered " + player.getAddress().getAddress().getHostAddress());
                  }

                  if (this.plugin.notifications != null) {
                     this.plugin.notifications.showNotification(new Notification("[AuthMe] " + player.getName() + " has registered!"));
                  }

                  return true;
               }
            }

            if (args.length != 0 && (!Settings.getEnablePasswordVerifier || args.length >= 2)) {
               if (args[0].length() >= Settings.getPasswordMinLen && args[0].length() <= Settings.passwordMaxLength) {
                  try {
                     String hash;
                     if (Settings.getEnablePasswordVerifier) {
                        if (!args[0].equals(args[1])) {
                           player.sendMessage(this.m._("password_error"));
                           return true;
                        }

                        hash = PasswordSecurity.getHash(Settings.getPasswordHash, args[0], name);
                     } else {
                        hash = PasswordSecurity.getHash(Settings.getPasswordHash, args[0], name);
                     }

                     if (Settings.getMySQLColumnSalt.isEmpty()) {
                        this.auth = new PlayerAuth(name, hash, ip, (new Date()).getTime());
                     } else {
                        this.auth = new PlayerAuth(name, hash, (String)PasswordSecurity.userSalt.get(name), ip, (new Date()).getTime());
                     }

                     if (!this.database.saveAuth(this.auth)) {
                        player.sendMessage(this.m._("error"));
                        return true;
                     }

                     PlayerCache.getInstance().addPlayer(this.auth);
                     LimboPlayer limbo = LimboCache.getInstance().getLimboPlayer(name);
                     if (limbo != null) {
                        player.setGameMode(GameMode.getByValue(limbo.getGameMode()));
                        if (Settings.isTeleportToSpawnEnabled) {
                           World world = player.getWorld();
                           Location loca = world.getSpawnLocation();
                           if (this.plugin.mv != null) {
                              try {
                                 loca = this.plugin.mv.getMVWorldManager().getMVWorld(world).getSpawnLocation();
                              } catch (NullPointerException var22) {
                              } catch (ClassCastException var23) {
                              } catch (NoClassDefFoundError var24) {
                              }
                           }

                           if (this.plugin.essentialsSpawn != null) {
                              loca = this.plugin.essentialsSpawn;
                           }

                           if (Spawn.getInstance().getLocation() != null) {
                              loca = Spawn.getInstance().getLocation();
                           }

                           RegisterTeleportEvent tpEvent = new RegisterTeleportEvent(player, loca);
                           this.plugin.getServer().getPluginManager().callEvent(tpEvent);
                           if (!tpEvent.isCancelled()) {
                              if (!tpEvent.getTo().getWorld().getChunkAt(tpEvent.getTo()).isLoaded()) {
                                 tpEvent.getTo().getWorld().getChunkAt(tpEvent.getTo()).load();
                              }

                              player.teleport(tpEvent.getTo());
                           }
                        }

                        sender.getServer().getScheduler().cancelTask(limbo.getTimeoutTaskId());
                        sender.getServer().getScheduler().cancelTask(limbo.getMessageTaskId());
                        LimboCache.getInstance().deleteLimboPlayer(name);
                     }

                     if (!Settings.getRegisteredGroup.isEmpty()) {
                        Utils.getInstance().setGroup(player, Utils.groupType.REGISTERED);
                     }

                     player.sendMessage(this.m._("registered"));
                     if (!Settings.getmailAccount.isEmpty()) {
                        player.sendMessage(this.m._("add_email"));
                     }

                     this.isFirstTimeJoin = true;
                     if (player.getGameMode() != GameMode.CREATIVE) {
                        player.setAllowFlight(false);
                     }

                     player.setFlying(false);
                     player.saveData();
                     if (!Settings.noConsoleSpam) {
                        ConsoleLogger.info(player.getName() + " registered " + player.getAddress().getAddress().getHostAddress());
                     }

                     if (this.plugin.notifications != null) {
                        this.plugin.notifications.showNotification(new Notification("[AuthMe] " + player.getName() + " has registered!"));
                     }
                  } catch (NoSuchAlgorithmException ex) {
                     ConsoleLogger.showError(ex.getMessage());
                     sender.sendMessage(this.m._("error"));
                  }

                  return true;
               } else {
                  player.sendMessage(this.m._("pass_len"));
                  return true;
               }
            } else {
               player.sendMessage(this.m._("usage_reg"));
               return true;
            }
         }
      }
   }
}
