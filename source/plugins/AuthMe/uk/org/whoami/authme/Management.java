package uk.org.whoami.authme;

import java.security.NoSuchAlgorithmException;
import java.util.Date;
import java.util.List;
import me.muizers.Notifications.Notification;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.plugin.PluginManager;
import uk.org.whoami.authme.api.API;
import uk.org.whoami.authme.cache.auth.PlayerAuth;
import uk.org.whoami.authme.cache.auth.PlayerCache;
import uk.org.whoami.authme.cache.backup.FileCache;
import uk.org.whoami.authme.cache.limbo.LimboCache;
import uk.org.whoami.authme.cache.limbo.LimboPlayer;
import uk.org.whoami.authme.datasource.DataSource;
import uk.org.whoami.authme.events.AuthMeTeleportEvent;
import uk.org.whoami.authme.events.LoginEvent;
import uk.org.whoami.authme.events.RestoreInventoryEvent;
import uk.org.whoami.authme.events.SpawnTeleportEvent;
import uk.org.whoami.authme.listener.AuthMePlayerListener;
import uk.org.whoami.authme.security.PasswordSecurity;
import uk.org.whoami.authme.security.RandomString;
import uk.org.whoami.authme.settings.Messages;
import uk.org.whoami.authme.settings.PlayersLogs;
import uk.org.whoami.authme.settings.Settings;
import uk.org.whoami.authme.settings.Spawn;

public class Management {
   private Messages m = Messages.getInstance();
   private PlayersLogs pllog = PlayersLogs.getInstance();
   private Utils utils = Utils.getInstance();
   private FileCache playerCache = new FileCache();
   private DataSource database;
   public AuthMe plugin;
   public static RandomString rdm;
   public PluginManager pm;

   static {
      rdm = new RandomString(Settings.captchaLength);
   }

   public Management(DataSource database, AuthMe plugin) {
      super();
      this.database = database;
      this.plugin = plugin;
      this.pm = plugin.getServer().getPluginManager();
   }

   public void performLogin(final Player player, final String password, final boolean passpartu) {
      final String name = player.getName().toLowerCase();
      Bukkit.getScheduler().runTaskAsynchronously(this.plugin, new Runnable() {
         public void run() {
            String ip = player.getAddress().getAddress().getHostAddress();
            if (Settings.bungee && Management.this.plugin.realIp.containsKey(name)) {
               ip = (String)Management.this.plugin.realIp.get(name);
            }

            World world = player.getWorld();
            final Location spawnLoc = world.getSpawnLocation();
            if (Management.this.plugin.mv != null) {
               try {
                  spawnLoc = Management.this.plugin.mv.getMVWorldManager().getMVWorld(world).getSpawnLocation();
               } catch (NullPointerException var16) {
               } catch (ClassCastException var17) {
               } catch (NoClassDefFoundError var18) {
               }
            }

            if (Management.this.plugin.essentialsSpawn != null) {
               spawnLoc = Management.this.plugin.essentialsSpawn;
            }

            if (Spawn.getInstance().getLocation() != null) {
               spawnLoc = Spawn.getInstance().getLocation();
            }

            if (PlayerCache.getInstance().isAuthenticated(name)) {
               player.sendMessage(Management.this.m._("logged_in"));
            } else if (!Management.this.database.isAuthAvailable(player.getName().toLowerCase())) {
               player.sendMessage(Management.this.m._("user_unknown"));
            } else {
               PlayerAuth pAuth = Management.this.database.getAuth(name);
               if (pAuth == null) {
                  player.sendMessage(Management.this.m._("user_unknown"));
               } else if (!Settings.getMySQLColumnGroup.isEmpty() && pAuth.getGroupId() == Settings.getNonActivatedGroup) {
                  player.sendMessage(Management.this.m._("vb_nonActiv"));
               } else {
                  String hash = pAuth.getHash();
                  String email = pAuth.getEmail();

                  try {
                     if (!passpartu) {
                        if (Settings.useCaptcha) {
                           if (!Management.this.plugin.captcha.containsKey(name)) {
                              Management.this.plugin.captcha.put(name, 1);
                           } else {
                              int i = (Integer)Management.this.plugin.captcha.get(name) + 1;
                              Management.this.plugin.captcha.remove(name);
                              Management.this.plugin.captcha.put(name, i);
                           }

                           if (Management.this.plugin.captcha.containsKey(name) && (Integer)Management.this.plugin.captcha.get(name) > Settings.maxLoginTry) {
                              player.sendMessage(Management.this.m._("need_captcha"));
                              Management.this.plugin.cap.put(name, Management.rdm.nextString());
                              player.sendMessage("Type : /captcha " + (String)Management.this.plugin.cap.get(name));
                              return;
                           }

                           if (Management.this.plugin.captcha.containsKey(name) && (Integer)Management.this.plugin.captcha.get(name) > Settings.maxLoginTry) {
                              try {
                                 Management.this.plugin.captcha.remove(name);
                                 Management.this.plugin.cap.remove(name);
                              } catch (NullPointerException var15) {
                              }
                           }
                        }

                        if (PasswordSecurity.comparePasswordWithHash(password, hash, name) && player.isOnline()) {
                           PlayerAuth auth = new PlayerAuth(name, hash, ip, (new Date()).getTime(), email);
                           Management.this.database.updateSession(auth);
                           final LimboPlayer limbo = LimboCache.getInstance().getLimboPlayer(name);
                           final PlayerAuth getAuth = Management.this.database.getAuth(name);
                           if (limbo != null) {
                              Bukkit.getScheduler().scheduleSyncDelayedTask(Management.this.plugin, new Runnable() {
                                 public void run() {
                                    player.setOp(limbo.getOperator());
                                    if (player.getGameMode() != GameMode.CREATIVE) {
                                       player.setAllowFlight(limbo.isFlying());
                                    }

                                    player.setFlying(limbo.isFlying());
                                 }
                              });
                              Management.this.utils.addNormal(player, limbo.getGroup());
                              if (Settings.isTeleportToSpawnEnabled && !Settings.isForceSpawnLocOnJoinEnabled && Settings.getForcedWorlds.contains(player.getWorld().getName())) {
                                 if (Settings.isSaveQuitLocationEnabled && getAuth.getQuitLocY() != 0) {
                                    Bukkit.getScheduler().scheduleSyncDelayedTask(Management.this.plugin, new Runnable() {
                                       public void run() {
                                          Management.this.utils.packCoords(getAuth.getQuitLocX(), getAuth.getQuitLocY(), getAuth.getQuitLocZ(), getAuth.getWorld(), player);
                                       }
                                    });
                                 } else {
                                    Bukkit.getScheduler().scheduleSyncDelayedTask(Management.this.plugin, new Runnable() {
                                       public void run() {
                                          AuthMeTeleportEvent tpEvent = new AuthMeTeleportEvent(player, limbo.getLoc());
                                          Management.this.pm.callEvent(tpEvent);
                                          Location fLoc = tpEvent.getTo();
                                          if (!tpEvent.isCancelled()) {
                                             if (!fLoc.getChunk().isLoaded()) {
                                                fLoc.getChunk().load();
                                             }

                                             player.teleport(fLoc);
                                          }

                                       }
                                    });
                                 }
                              } else if (Settings.isForceSpawnLocOnJoinEnabled && Settings.getForcedWorlds.contains(player.getWorld().getName())) {
                                 Bukkit.getScheduler().scheduleSyncDelayedTask(Management.this.plugin, new Runnable() {
                                    public void run() {
                                       SpawnTeleportEvent tpEvent = new SpawnTeleportEvent(player, player.getLocation(), spawnLoc, true);
                                       Management.this.pm.callEvent(tpEvent);
                                       if (!tpEvent.isCancelled()) {
                                          Location fLoc = tpEvent.getTo();
                                          if (!fLoc.getChunk().isLoaded()) {
                                             fLoc.getChunk().load();
                                          }

                                          player.teleport(fLoc);
                                       }

                                    }
                                 });
                              } else if (Settings.isSaveQuitLocationEnabled && getAuth.getQuitLocY() != 0) {
                                 Bukkit.getScheduler().scheduleSyncDelayedTask(Management.this.plugin, new Runnable() {
                                    public void run() {
                                       Management.this.utils.packCoords(getAuth.getQuitLocX(), getAuth.getQuitLocY(), getAuth.getQuitLocZ(), getAuth.getWorld(), player);
                                    }
                                 });
                              } else {
                                 Bukkit.getScheduler().scheduleSyncDelayedTask(Management.this.plugin, new Runnable() {
                                    public void run() {
                                       AuthMeTeleportEvent tpEvent = new AuthMeTeleportEvent(player, limbo.getLoc());
                                       Management.this.pm.callEvent(tpEvent);
                                       Location fLoc = tpEvent.getTo();
                                       if (!tpEvent.isCancelled()) {
                                          if (!fLoc.getChunk().isLoaded()) {
                                             fLoc.getChunk().load();
                                          }

                                          player.teleport(fLoc);
                                       }

                                    }
                                 });
                              }

                              if (!Settings.forceOnlyAfterLogin) {
                                 Bukkit.getScheduler().scheduleSyncDelayedTask(Management.this.plugin, new Runnable() {
                                    public void run() {
                                       player.setGameMode(GameMode.getByValue(limbo.getGameMode()));
                                    }
                                 });
                              } else {
                                 Bukkit.getScheduler().scheduleSyncDelayedTask(Management.this.plugin, new Runnable() {
                                    public void run() {
                                       player.setGameMode(GameMode.SURVIVAL);
                                    }
                                 });
                              }

                              if (Settings.protectInventoryBeforeLogInEnabled && player.hasPlayedBefore()) {
                                 Bukkit.getScheduler().scheduleSyncDelayedTask(Management.this.plugin, new Runnable() {
                                    public void run() {
                                       RestoreInventoryEvent event = new RestoreInventoryEvent(player, limbo.getInventory(), limbo.getArmour());
                                       Bukkit.getServer().getPluginManager().callEvent(event);
                                       if (!event.isCancelled()) {
                                          API.setPlayerInventory(player, limbo.getInventory(), limbo.getArmour());
                                       }

                                    }
                                 });
                              }

                              player.getServer().getScheduler().cancelTask(limbo.getTimeoutTaskId());
                              player.getServer().getScheduler().cancelTask(limbo.getMessageTaskId());
                              LimboCache.getInstance().deleteLimboPlayer(name);
                              if (Management.this.playerCache.doesCacheExist(name)) {
                                 Management.this.playerCache.removeCache(name);
                              }
                           }

                           if (Settings.isPermissionCheckEnabled && AuthMe.permission.playerInGroup(player, Settings.unRegisteredGroup) && !Settings.unRegisteredGroup.isEmpty()) {
                              AuthMe.permission.playerRemoveGroup(player.getWorld(), player.getName(), Settings.unRegisteredGroup);
                              AuthMe.permission.playerAddGroup(player.getWorld(), player.getName(), Settings.getRegisteredGroup);
                           }

                           try {
                              if (!PlayersLogs.players.contains(player.getName())) {
                                 PlayersLogs.players.add(player.getName());
                              }

                              Management.this.pllog.save();
                           } catch (NullPointerException var14) {
                           }

                           Bukkit.getScheduler().scheduleSyncDelayedTask(Management.this.plugin, new Runnable() {
                              public void run() {
                                 Bukkit.getServer().getPluginManager().callEvent(new LoginEvent(player, true));
                              }
                           });
                           if (Settings.useCaptcha) {
                              if (Management.this.plugin.captcha.containsKey(name)) {
                                 Management.this.plugin.captcha.remove(name);
                              }

                              if (Management.this.plugin.cap.containsKey(name)) {
                                 Management.this.plugin.cap.containsKey(name);
                              }
                           }

                           player.setNoDamageTicks(0);
                           player.sendMessage(Management.this.m._("login"));
                           Management.this.displayOtherAccounts(auth);
                           if (!Settings.noConsoleSpam) {
                              ConsoleLogger.info(player.getName() + " logged in!");
                           }

                           if (Management.this.plugin.notifications != null) {
                              Management.this.plugin.notifications.showNotification(new Notification("[AuthMe] " + player.getName() + " logged in!"));
                           }

                           PlayerCache.getInstance().addPlayer(auth);
                           Bukkit.getScheduler().scheduleSyncDelayedTask(Management.this.plugin, new Runnable() {
                              public void run() {
                                 player.saveData();
                              }
                           });
                        } else if (player.isOnline()) {
                           if (!Settings.noConsoleSpam) {
                              ConsoleLogger.info(player.getName() + " used the wrong password");
                           }

                           if (!Settings.isKickOnWrongPasswordEnabled) {
                              player.sendMessage(Management.this.m._("wrong_pwd"));
                              return;
                           }

                           try {
                              final int gm = (Integer)AuthMePlayerListener.gameMode.get(name);
                              Bukkit.getScheduler().scheduleSyncDelayedTask(Management.this.plugin, new Runnable() {
                                 public void run() {
                                    player.setGameMode(GameMode.getByValue(gm));
                                 }
                              });
                           } catch (NullPointerException var13) {
                           }

                           Bukkit.getScheduler().scheduleSyncDelayedTask(Management.this.plugin, new Runnable() {
                              public void run() {
                                 player.kickPlayer(Management.this.m._("wrong_pwd"));
                              }
                           });
                        } else {
                           ConsoleLogger.showError("Player " + name + " wasn't online during login process , aborded... ");
                        }
                     } else {
                        PlayerAuth auth = new PlayerAuth(name, hash, ip, (new Date()).getTime(), email);
                        Management.this.database.updateSession(auth);
                        final LimboPlayer limbo = LimboCache.getInstance().getLimboPlayer(name);
                        if (limbo != null) {
                           Bukkit.getScheduler().scheduleSyncDelayedTask(Management.this.plugin, new Runnable() {
                              public void run() {
                                 player.setOp(limbo.getOperator());
                                 if (player.getGameMode() != GameMode.CREATIVE) {
                                    player.setAllowFlight(limbo.isFlying());
                                 }

                                 player.setFlying(limbo.isFlying());
                              }
                           });
                           Management.this.utils.addNormal(player, limbo.getGroup());
                           if (Settings.isTeleportToSpawnEnabled && !Settings.isForceSpawnLocOnJoinEnabled && Settings.getForcedWorlds.contains(player.getWorld().getName())) {
                              if (Settings.isSaveQuitLocationEnabled && Management.this.database.getAuth(name).getQuitLocY() != 0) {
                                 String worldname = Management.this.database.getAuth(name).getWorld();
                                 World theWorld;
                                 if (worldname.equals("unavailableworld")) {
                                    theWorld = player.getWorld();
                                 } else {
                                    theWorld = Bukkit.getWorld(worldname);
                                 }

                                 if (theWorld == null) {
                                    theWorld = player.getWorld();
                                 }

                                 final Location quitLoc = new Location(theWorld, (double)Management.this.database.getAuth(name).getQuitLocX() + (double)0.5F, (double)Management.this.database.getAuth(name).getQuitLocY() + (double)0.5F, (double)Management.this.database.getAuth(name).getQuitLocZ() + (double)0.5F);
                                 Bukkit.getScheduler().scheduleSyncDelayedTask(Management.this.plugin, new Runnable() {
                                    public void run() {
                                       AuthMeTeleportEvent tpEvent = new AuthMeTeleportEvent(player, quitLoc);
                                       Management.this.pm.callEvent(tpEvent);
                                       Location fLoc = tpEvent.getTo();
                                       if (!tpEvent.isCancelled()) {
                                          if (!fLoc.getChunk().isLoaded()) {
                                             fLoc.getChunk().load();
                                          }

                                          player.teleport(fLoc);
                                       }

                                    }
                                 });
                              } else {
                                 Bukkit.getScheduler().scheduleSyncDelayedTask(Management.this.plugin, new Runnable() {
                                    public void run() {
                                       AuthMeTeleportEvent tpEvent = new AuthMeTeleportEvent(player, limbo.getLoc());
                                       Management.this.pm.callEvent(tpEvent);
                                       Location fLoc = tpEvent.getTo();
                                       if (!tpEvent.isCancelled()) {
                                          if (!fLoc.getChunk().isLoaded()) {
                                             fLoc.getChunk().load();
                                          }

                                          player.teleport(fLoc);
                                       }

                                    }
                                 });
                              }
                           } else if (Settings.isForceSpawnLocOnJoinEnabled && Settings.getForcedWorlds.contains(player.getWorld().getName())) {
                              Bukkit.getScheduler().scheduleSyncDelayedTask(Management.this.plugin, new Runnable() {
                                 public void run() {
                                    SpawnTeleportEvent tpEvent = new SpawnTeleportEvent(player, player.getLocation(), spawnLoc, true);
                                    Management.this.pm.callEvent(tpEvent);
                                    if (!tpEvent.isCancelled()) {
                                       Location fLoc = tpEvent.getTo();
                                       if (!fLoc.getChunk().isLoaded()) {
                                          fLoc.getChunk().load();
                                       }

                                       player.teleport(fLoc);
                                    }

                                 }
                              });
                           } else if (Settings.isSaveQuitLocationEnabled && Management.this.database.getAuth(name).getQuitLocY() != 0) {
                              String worldname = Management.this.database.getAuth(name).getWorld();
                              World theWorld;
                              if (worldname.equals("unavailableworld")) {
                                 theWorld = player.getWorld();
                              } else {
                                 theWorld = Bukkit.getWorld(worldname);
                              }

                              if (theWorld == null) {
                                 theWorld = player.getWorld();
                              }

                              final Location quitLoc = new Location(theWorld, (double)Management.this.database.getAuth(name).getQuitLocX() + (double)0.5F, (double)Management.this.database.getAuth(name).getQuitLocY() + (double)0.5F, (double)Management.this.database.getAuth(name).getQuitLocZ() + (double)0.5F);
                              Bukkit.getScheduler().scheduleSyncDelayedTask(Management.this.plugin, new Runnable() {
                                 public void run() {
                                    AuthMeTeleportEvent tpEvent = new AuthMeTeleportEvent(player, quitLoc);
                                    Management.this.pm.callEvent(tpEvent);
                                    Location fLoc = tpEvent.getTo();
                                    if (!tpEvent.isCancelled()) {
                                       if (!fLoc.getChunk().isLoaded()) {
                                          fLoc.getChunk().load();
                                       }

                                       player.teleport(fLoc);
                                    }

                                 }
                              });
                           } else {
                              Bukkit.getScheduler().scheduleSyncDelayedTask(Management.this.plugin, new Runnable() {
                                 public void run() {
                                    AuthMeTeleportEvent tpEvent = new AuthMeTeleportEvent(player, limbo.getLoc());
                                    Management.this.pm.callEvent(tpEvent);
                                    Location fLoc = tpEvent.getTo();
                                    if (!tpEvent.isCancelled()) {
                                       if (!fLoc.getChunk().isLoaded()) {
                                          fLoc.getChunk().load();
                                       }

                                       player.teleport(fLoc);
                                    }

                                 }
                              });
                           }

                           if (!Settings.forceOnlyAfterLogin) {
                              Bukkit.getScheduler().scheduleSyncDelayedTask(Management.this.plugin, new Runnable() {
                                 public void run() {
                                    player.setGameMode(GameMode.getByValue(limbo.getGameMode()));
                                 }
                              });
                           } else {
                              Bukkit.getScheduler().scheduleSyncDelayedTask(Management.this.plugin, new Runnable() {
                                 public void run() {
                                    player.setGameMode(GameMode.SURVIVAL);
                                 }
                              });
                           }

                           if (Settings.protectInventoryBeforeLogInEnabled && player.hasPlayedBefore()) {
                              Bukkit.getScheduler().scheduleSyncDelayedTask(Management.this.plugin, new Runnable() {
                                 public void run() {
                                    RestoreInventoryEvent event = new RestoreInventoryEvent(player, limbo.getInventory(), limbo.getArmour());
                                    Bukkit.getServer().getPluginManager().callEvent(event);
                                    if (!event.isCancelled()) {
                                       API.setPlayerInventory(player, limbo.getInventory(), limbo.getArmour());
                                    }

                                 }
                              });
                           }

                           player.getServer().getScheduler().cancelTask(limbo.getTimeoutTaskId());
                           player.getServer().getScheduler().cancelTask(limbo.getMessageTaskId());
                           LimboCache.getInstance().deleteLimboPlayer(name);
                           if (Management.this.playerCache.doesCacheExist(name)) {
                              Management.this.playerCache.removeCache(name);
                           }
                        }

                        if (Settings.isPermissionCheckEnabled && AuthMe.permission.playerInGroup(player, Settings.unRegisteredGroup) && !Settings.unRegisteredGroup.isEmpty()) {
                           AuthMe.permission.playerRemoveGroup(player.getWorld(), player.getName(), Settings.unRegisteredGroup);
                           AuthMe.permission.playerAddGroup(player.getWorld(), player.getName(), Settings.getRegisteredGroup);
                        }

                        try {
                           if (!PlayersLogs.players.contains(player.getName())) {
                              PlayersLogs.players.add(player.getName());
                           }

                           Management.this.pllog.save();
                        } catch (NullPointerException var12) {
                        }

                        Bukkit.getScheduler().scheduleSyncDelayedTask(Management.this.plugin, new Runnable() {
                           public void run() {
                              Bukkit.getServer().getPluginManager().callEvent(new LoginEvent(player, true));
                           }
                        });
                        if (Settings.useCaptcha) {
                           if (Management.this.plugin.captcha.containsKey(name)) {
                              Management.this.plugin.captcha.remove(name);
                           }

                           if (Management.this.plugin.cap.containsKey(name)) {
                              Management.this.plugin.cap.containsKey(name);
                           }
                        }

                        player.setNoDamageTicks(0);
                        player.sendMessage(Management.this.m._("login"));
                        Management.this.displayOtherAccounts(auth);
                        if (!Settings.noConsoleSpam) {
                           ConsoleLogger.info(player.getName() + " logged in!");
                        }

                        if (Management.this.plugin.notifications != null) {
                           Management.this.plugin.notifications.showNotification(new Notification("[AuthMe] " + player.getName() + " logged in!"));
                        }

                        PlayerCache.getInstance().addPlayer(auth);
                        Bukkit.getScheduler().scheduleSyncDelayedTask(Management.this.plugin, new Runnable() {
                           public void run() {
                              player.saveData();
                           }
                        });
                     }

                  } catch (NoSuchAlgorithmException ex) {
                     ConsoleLogger.showError(ex.getMessage());
                     player.sendMessage(Management.this.m._("error"));
                  }
               }
            }
         }
      });
   }

   private void displayOtherAccounts(PlayerAuth auth) {
      if (Settings.displayOtherAccounts) {
         if (auth != null) {
            if (!this.database.getAllAuthsByName(auth).isEmpty() && this.database.getAllAuthsByName(auth) != null) {
               if (this.database.getAllAuthsByName(auth).size() != 1) {
                  List<String> accountList = this.database.getAllAuthsByName(auth);
                  String message = "[AuthMe] ";
                  int i = 0;

                  for(String account : accountList) {
                     ++i;
                     message = message + account;
                     if (i != accountList.size()) {
                        message = message + ", ";
                     } else {
                        message = message + ".";
                     }
                  }

                  Player[] var8;
                  for(Player player : var8 = AuthMe.getInstance().getServer().getOnlinePlayers()) {
                     if (this.plugin.authmePermissible(player, "authme.seeOtherAccounts")) {
                        player.sendMessage("[AuthMe] The player " + auth.getNickname() + " has " + accountList.size() + " accounts");
                        player.sendMessage(message);
                     }
                  }

               }
            }
         }
      }
   }
}
