package uk.org.whoami.authme.listener;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.Date;
import java.util.HashMap;
import java.util.regex.PatternSyntaxException;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.SignChangeEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryOpenEvent;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerBedEnterEvent;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerKickEvent;
import org.bukkit.event.player.PlayerLoginEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerPickupItemEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.event.player.PlayerLoginEvent.Result;
import org.bukkit.scheduler.BukkitScheduler;
import org.bukkit.scheduler.BukkitTask;
import uk.org.whoami.authme.AuthMe;
import uk.org.whoami.authme.ConsoleLogger;
import uk.org.whoami.authme.Utils;
import uk.org.whoami.authme.api.API;
import uk.org.whoami.authme.cache.auth.PlayerAuth;
import uk.org.whoami.authme.cache.auth.PlayerCache;
import uk.org.whoami.authme.cache.backup.DataFileCache;
import uk.org.whoami.authme.cache.backup.FileCache;
import uk.org.whoami.authme.cache.limbo.LimboCache;
import uk.org.whoami.authme.cache.limbo.LimboPlayer;
import uk.org.whoami.authme.datasource.DataSource;
import uk.org.whoami.authme.events.AuthMeTeleportEvent;
import uk.org.whoami.authme.events.ProtectInventoryEvent;
import uk.org.whoami.authme.events.RestoreInventoryEvent;
import uk.org.whoami.authme.events.SessionEvent;
import uk.org.whoami.authme.events.SpawnTeleportEvent;
import uk.org.whoami.authme.plugin.manager.CombatTagComunicator;
import uk.org.whoami.authme.settings.Messages;
import uk.org.whoami.authme.settings.PlayersLogs;
import uk.org.whoami.authme.settings.Settings;
import uk.org.whoami.authme.settings.Spawn;
import uk.org.whoami.authme.task.MessageTask;
import uk.org.whoami.authme.task.TimeoutTask;

public class AuthMePlayerListener implements Listener {
   public static int gm = 0;
   public static HashMap gameMode = new HashMap();
   private Utils utils = Utils.getInstance();
   private Messages m = Messages.getInstance();
   public AuthMe plugin;
   private DataSource data;
   private FileCache playerBackup = new FileCache();

   public AuthMePlayerListener(AuthMe plugin, DataSource data) {
      super();
      this.plugin = plugin;
      this.data = data;
   }

   @EventHandler(
      priority = EventPriority.LOWEST
   )
   public void onPlayerCommandPreprocess(PlayerCommandPreprocessEvent event) {
      if (!event.isCancelled() && event.getPlayer() != null) {
         Player player = event.getPlayer();
         String name = player.getName().toLowerCase();
         if (!Utils.getInstance().isUnrestricted(player)) {
            if (!PlayerCache.getInstance().isAuthenticated(name)) {
               if (this.data.isAuthAvailable(name) || Settings.isForcedRegistrationEnabled) {
                  String msg = event.getMessage();
                  if (!msg.equalsIgnoreCase("/worldedit cui")) {
                     String cmd = msg.split(" ")[0];
                     if (!cmd.equalsIgnoreCase("/login") && !cmd.equalsIgnoreCase("/register") && !cmd.equalsIgnoreCase("/passpartu") && !cmd.equalsIgnoreCase("/l") && !cmd.equalsIgnoreCase("/reg") && !cmd.equalsIgnoreCase("/email") && !cmd.equalsIgnoreCase("/captcha")) {
                        if (!Settings.useEssentialsMotd || !cmd.equalsIgnoreCase("/motd")) {
                           if (!Settings.allowCommands.contains(cmd)) {
                              event.setMessage("/notloggedin");
                              event.setCancelled(true);
                           }
                        }
                     }
                  }
               }
            }
         }
      }
   }

   @EventHandler(
      priority = EventPriority.NORMAL
   )
   public void onPlayerNormalChat(AsyncPlayerChatEvent event) {
      if (!event.isCancelled() && event.getPlayer() != null) {
         final Player player = event.getPlayer();
         final String name = player.getName().toLowerCase();
         if (!Utils.getInstance().isUnrestricted(player)) {
            if (!PlayerCache.getInstance().isAuthenticated(name)) {
               String cmd = event.getMessage().split(" ")[0];
               if (!Settings.isChatAllowed && !Settings.allowCommands.contains(cmd)) {
                  event.setCancelled(true);
               } else {
                  if (!event.isAsynchronous()) {
                     if (this.data.isAuthAvailable(name)) {
                        player.sendMessage(this.m._("login_msg"));
                     } else {
                        if (!Settings.isForcedRegistrationEnabled) {
                           return;
                        }

                        if (Settings.emailRegistration) {
                           player.sendMessage(this.m._("reg_email_msg"));
                        } else {
                           player.sendMessage(this.m._("reg_msg"));
                        }
                     }
                  } else {
                     Bukkit.getScheduler().scheduleSyncDelayedTask(this.plugin, new Runnable() {
                        public void run() {
                           if (AuthMePlayerListener.this.data.isAuthAvailable(name)) {
                              player.sendMessage(AuthMePlayerListener.this.m._("login_msg"));
                           } else if (Settings.isForcedRegistrationEnabled) {
                              if (Settings.emailRegistration) {
                                 player.sendMessage(AuthMePlayerListener.this.m._("reg_email_msg"));
                              } else {
                                 player.sendMessage(AuthMePlayerListener.this.m._("reg_msg"));
                              }
                           }

                        }
                     });
                  }

               }
            }
         }
      }
   }

   @EventHandler(
      priority = EventPriority.HIGH
   )
   public void onPlayerHighChat(AsyncPlayerChatEvent event) {
      if (!event.isCancelled() && event.getPlayer() != null) {
         final Player player = event.getPlayer();
         final String name = player.getName().toLowerCase();
         if (!Utils.getInstance().isUnrestricted(player)) {
            if (!PlayerCache.getInstance().isAuthenticated(name)) {
               String cmd = event.getMessage().split(" ")[0];
               if (!Settings.isChatAllowed && !Settings.allowCommands.contains(cmd)) {
                  event.setCancelled(true);
               } else {
                  if (!event.isAsynchronous()) {
                     if (this.data.isAuthAvailable(name)) {
                        player.sendMessage(this.m._("login_msg"));
                     } else {
                        if (!Settings.isForcedRegistrationEnabled) {
                           return;
                        }

                        if (Settings.emailRegistration) {
                           player.sendMessage(this.m._("reg_email_msg"));
                        } else {
                           player.sendMessage(this.m._("reg_msg"));
                        }
                     }
                  } else {
                     Bukkit.getScheduler().scheduleSyncDelayedTask(this.plugin, new Runnable() {
                        public void run() {
                           if (AuthMePlayerListener.this.data.isAuthAvailable(name)) {
                              player.sendMessage(AuthMePlayerListener.this.m._("login_msg"));
                           } else if (Settings.isForcedRegistrationEnabled) {
                              if (Settings.emailRegistration) {
                                 player.sendMessage(AuthMePlayerListener.this.m._("reg_email_msg"));
                              } else {
                                 player.sendMessage(AuthMePlayerListener.this.m._("reg_msg"));
                              }
                           }

                        }
                     });
                  }

               }
            }
         }
      }
   }

   @EventHandler(
      priority = EventPriority.MONITOR
   )
   public void onPlayerChat(AsyncPlayerChatEvent event) {
      if (!event.isCancelled() && event.getPlayer() != null) {
         final Player player = event.getPlayer();
         final String name = player.getName().toLowerCase();
         if (!Utils.getInstance().isUnrestricted(player)) {
            if (!PlayerCache.getInstance().isAuthenticated(name)) {
               String cmd = event.getMessage().split(" ")[0];
               if (!Settings.isChatAllowed && !Settings.allowCommands.contains(cmd)) {
                  event.setCancelled(true);
               } else {
                  if (!event.isAsynchronous()) {
                     if (this.data.isAuthAvailable(name)) {
                        player.sendMessage(this.m._("login_msg"));
                     } else {
                        if (!Settings.isForcedRegistrationEnabled) {
                           return;
                        }

                        if (Settings.emailRegistration) {
                           player.sendMessage(this.m._("reg_email_msg"));
                        } else {
                           player.sendMessage(this.m._("reg_msg"));
                        }
                     }
                  } else {
                     Bukkit.getScheduler().scheduleSyncDelayedTask(this.plugin, new Runnable() {
                        public void run() {
                           if (AuthMePlayerListener.this.data.isAuthAvailable(name)) {
                              player.sendMessage(AuthMePlayerListener.this.m._("login_msg"));
                           } else if (Settings.isForcedRegistrationEnabled) {
                              if (Settings.emailRegistration) {
                                 player.sendMessage(AuthMePlayerListener.this.m._("reg_email_msg"));
                              } else {
                                 player.sendMessage(AuthMePlayerListener.this.m._("reg_msg"));
                              }
                           }

                        }
                     });
                  }

               }
            }
         }
      }
   }

   @EventHandler(
      priority = EventPriority.HIGHEST
   )
   public void onPlayerHighestChat(AsyncPlayerChatEvent event) {
      if (!event.isCancelled() && event.getPlayer() != null) {
         final Player player = event.getPlayer();
         final String name = player.getName().toLowerCase();
         if (!Utils.getInstance().isUnrestricted(player)) {
            if (!PlayerCache.getInstance().isAuthenticated(name)) {
               String cmd = event.getMessage().split(" ")[0];
               if (!Settings.isChatAllowed && !Settings.allowCommands.contains(cmd)) {
                  event.setCancelled(true);
               } else {
                  if (!event.isAsynchronous()) {
                     if (this.data.isAuthAvailable(name)) {
                        player.sendMessage(this.m._("login_msg"));
                     } else {
                        if (!Settings.isForcedRegistrationEnabled) {
                           return;
                        }

                        if (Settings.emailRegistration) {
                           player.sendMessage(this.m._("reg_email_msg"));
                        } else {
                           player.sendMessage(this.m._("reg_msg"));
                        }
                     }
                  } else {
                     Bukkit.getScheduler().scheduleSyncDelayedTask(this.plugin, new Runnable() {
                        public void run() {
                           if (AuthMePlayerListener.this.data.isAuthAvailable(name)) {
                              player.sendMessage(AuthMePlayerListener.this.m._("login_msg"));
                           } else if (Settings.isForcedRegistrationEnabled) {
                              if (Settings.emailRegistration) {
                                 player.sendMessage(AuthMePlayerListener.this.m._("reg_email_msg"));
                              } else {
                                 player.sendMessage(AuthMePlayerListener.this.m._("reg_msg"));
                              }
                           }

                        }
                     });
                  }

               }
            }
         }
      }
   }

   @EventHandler(
      priority = EventPriority.LOWEST
   )
   public void onPlayerEarlyChat(AsyncPlayerChatEvent event) {
      if (!event.isCancelled() && event.getPlayer() != null) {
         final Player player = event.getPlayer();
         final String name = player.getName().toLowerCase();
         if (!Utils.getInstance().isUnrestricted(player)) {
            if (!PlayerCache.getInstance().isAuthenticated(name)) {
               String cmd = event.getMessage().split(" ")[0];
               if (!Settings.isChatAllowed && !Settings.allowCommands.contains(cmd)) {
                  event.setCancelled(true);
               } else {
                  if (!event.isAsynchronous()) {
                     if (this.data.isAuthAvailable(name)) {
                        player.sendMessage(this.m._("login_msg"));
                     } else {
                        if (!Settings.isForcedRegistrationEnabled) {
                           return;
                        }

                        if (Settings.emailRegistration) {
                           player.sendMessage(this.m._("reg_email_msg"));
                        } else {
                           player.sendMessage(this.m._("reg_msg"));
                        }
                     }
                  } else {
                     Bukkit.getScheduler().scheduleSyncDelayedTask(this.plugin, new Runnable() {
                        public void run() {
                           if (AuthMePlayerListener.this.data.isAuthAvailable(name)) {
                              player.sendMessage(AuthMePlayerListener.this.m._("login_msg"));
                           } else if (Settings.isForcedRegistrationEnabled) {
                              if (Settings.emailRegistration) {
                                 player.sendMessage(AuthMePlayerListener.this.m._("reg_email_msg"));
                              } else {
                                 player.sendMessage(AuthMePlayerListener.this.m._("reg_msg"));
                              }
                           }

                        }
                     });
                  }

               }
            }
         }
      }
   }

   @EventHandler(
      priority = EventPriority.LOW
   )
   public void onPlayerLowChat(AsyncPlayerChatEvent event) {
      if (!event.isCancelled() && event.getPlayer() != null) {
         final Player player = event.getPlayer();
         final String name = player.getName().toLowerCase();
         if (!Utils.getInstance().isUnrestricted(player)) {
            if (!PlayerCache.getInstance().isAuthenticated(name)) {
               String cmd = event.getMessage().split(" ")[0];
               if (!Settings.isChatAllowed && !Settings.allowCommands.contains(cmd)) {
                  event.setCancelled(true);
               } else {
                  if (!event.isAsynchronous()) {
                     if (this.data.isAuthAvailable(name)) {
                        player.sendMessage(this.m._("login_msg"));
                     } else {
                        if (!Settings.isForcedRegistrationEnabled) {
                           return;
                        }

                        if (Settings.emailRegistration) {
                           player.sendMessage(this.m._("reg_email_msg"));
                        } else {
                           player.sendMessage(this.m._("reg_msg"));
                        }
                     }
                  } else {
                     Bukkit.getScheduler().scheduleSyncDelayedTask(this.plugin, new Runnable() {
                        public void run() {
                           if (AuthMePlayerListener.this.data.isAuthAvailable(name)) {
                              player.sendMessage(AuthMePlayerListener.this.m._("login_msg"));
                           } else if (Settings.isForcedRegistrationEnabled) {
                              if (Settings.emailRegistration) {
                                 player.sendMessage(AuthMePlayerListener.this.m._("reg_email_msg"));
                              } else {
                                 player.sendMessage(AuthMePlayerListener.this.m._("reg_msg"));
                              }
                           }

                        }
                     });
                  }

               }
            }
         }
      }
   }

   @EventHandler(
      priority = EventPriority.MONITOR
   )
   public void onPlayerMove(PlayerMoveEvent event) {
      if (!event.isCancelled() && event.getPlayer() != null) {
         Player player = event.getPlayer();
         String name = player.getName().toLowerCase();
         if (!this.plugin.getCitizensCommunicator().isNPC(player, this.plugin) && !Utils.getInstance().isUnrestricted(player) && !CombatTagComunicator.isNPC(player)) {
            if (!PlayerCache.getInstance().isAuthenticated(name)) {
               if (Settings.isForcedRegistrationEnabled) {
                  if (!Settings.isMovementAllowed) {
                     event.setTo(event.getFrom());
                  } else if (Settings.getMovementRadius != 0) {
                     int radius = Settings.getMovementRadius;
                     Location spawn = player.getWorld().getSpawnLocation();
                     if (this.plugin.mv != null) {
                        try {
                           spawn = this.plugin.mv.getMVWorldManager().getMVWorld(player.getWorld()).getSpawnLocation();
                        } catch (NullPointerException var7) {
                        } catch (ClassCastException var8) {
                        } catch (NoClassDefFoundError var9) {
                        }
                     }

                     if (this.plugin.essentialsSpawn != null) {
                        spawn = this.plugin.essentialsSpawn;
                     }

                     if (Spawn.getInstance().getLocation() != null && Spawn.getInstance().getLocation().getWorld().equals(player.getWorld())) {
                        spawn = Spawn.getInstance().getLocation();
                     }

                     if (!event.getPlayer().getWorld().equals(spawn.getWorld())) {
                        event.getPlayer().teleport(spawn);
                     } else if (spawn.distance(player.getLocation()) > (double)radius) {
                        event.getPlayer().teleport(spawn);
                     }
                  }
               }
            }
         }
      }
   }

   @EventHandler(
      priority = EventPriority.HIGHEST
   )
   public void onPlayerLogin(PlayerLoginEvent event) {
      Player player = event.getPlayer();
      String name = player.getName().toLowerCase();
      if (!this.plugin.getCitizensCommunicator().isNPC(player, this.plugin) && !Utils.getInstance().isUnrestricted(player) && !CombatTagComunicator.isNPC(player)) {
         if (player.isOnline() && Settings.isForceSingleSessionEnabled) {
            event.disallow(Result.KICK_OTHER, this.m._("same_nick"));
         } else {
            if (this.data.isAuthAvailable(name) && !LimboCache.getInstance().hasLimboPlayer(name)) {
               if (!Settings.isSessionsEnabled) {
                  LimboCache.getInstance().addLimboPlayer(player, this.utils.removeAll(player));
               } else if (PlayerCache.getInstance().isAuthenticated(name)) {
                  if (!Settings.sessionExpireOnIpChange && LimboCache.getInstance().hasLimboPlayer(player.getName().toLowerCase())) {
                     LimboCache.getInstance().deleteLimboPlayer(name);
                  }

                  LimboCache.getInstance().addLimboPlayer(player, this.utils.removeAll(player));
               }
            }

            if (player.isOnline() && Settings.isForceSingleSessionEnabled) {
               LimboPlayer limbo = LimboCache.getInstance().getLimboPlayer(player.getName().toLowerCase());
               event.disallow(Result.KICK_OTHER, this.m._("same_nick"));
               if (PlayerCache.getInstance().isAuthenticated(player.getName().toLowerCase())) {
                  this.utils.addNormal(player, limbo.getGroup());
                  LimboCache.getInstance().deleteLimboPlayer(player.getName().toLowerCase());
               }

            } else {
               int min = Settings.getMinNickLength;
               int max = Settings.getMaxNickLength;
               String regex = Settings.getNickRegex;
               if (name.length() <= max && name.length() >= min) {
                  try {
                     if (!player.getName().matches(regex) || name.equals("Player")) {
                        try {
                           event.disallow(Result.KICK_OTHER, this.m._("regex").replaceAll("REG_EX", regex));
                        } catch (StringIndexOutOfBoundsException var10) {
                           event.disallow(Result.KICK_OTHER, "allowed char : " + regex);
                        }

                        return;
                     }
                  } catch (PatternSyntaxException var11) {
                     if (regex != null && !regex.isEmpty()) {
                        try {
                           event.disallow(Result.KICK_OTHER, this.m._("regex").replaceAll("REG_EX", regex));
                        } catch (StringIndexOutOfBoundsException var9) {
                           event.disallow(Result.KICK_OTHER, "allowed char : " + regex);
                        }

                        return;
                     }

                     event.disallow(Result.KICK_OTHER, "Your nickname do not match");
                     return;
                  }

                  if (Settings.isKickNonRegisteredEnabled && !this.data.isAuthAvailable(name)) {
                     event.disallow(Result.KICK_OTHER, this.m._("reg_only"));
                  } else if (event.getResult() == Result.KICK_FULL) {
                     if (!player.isBanned()) {
                        if (!this.plugin.authmePermissible(player, "authme.vip")) {
                           event.disallow(Result.KICK_FULL, this.m._("kick_fullserver"));
                        } else if (this.plugin.getServer().getOnlinePlayers().length > this.plugin.getServer().getMaxPlayers()) {
                           event.allow();
                        } else {
                           Player pl = this.plugin.generateKickPlayer(this.plugin.getServer().getOnlinePlayers());
                           if (pl != null) {
                              pl.kickPlayer(this.m._("kick_forvip"));
                              event.allow();
                           } else {
                              ConsoleLogger.info("The player " + player.getName() + " wants to join, but the server is full");
                              event.disallow(Result.KICK_FULL, this.m._("kick_fullserver"));
                           }
                        }
                     }
                  }
               } else {
                  event.disallow(Result.KICK_OTHER, this.m._("name_len"));
               }
            }
         }
      }
   }

   @EventHandler(
      priority = EventPriority.LOWEST
   )
   public void onPlayerLowestJoin(PlayerJoinEvent event) {
      if (event.getPlayer() != null) {
         final Player player = event.getPlayer();
         if (!this.plugin.getCitizensCommunicator().isNPC(player, this.plugin) && !Utils.getInstance().isUnrestricted(player) && !CombatTagComunicator.isNPC(player)) {
            if (Settings.bungee) {
               final ByteArrayOutputStream b = new ByteArrayOutputStream();
               DataOutputStream out = new DataOutputStream(b);

               try {
                  out.writeUTF("IP");
               } catch (IOException var6) {
               }

               Bukkit.getServer().getScheduler().scheduleSyncDelayedTask(this.plugin, new Runnable() {
                  public void run() {
                     player.sendPluginMessage(AuthMePlayerListener.this.plugin, "BungeeCord", b.toByteArray());
                  }
               });
            }

         }
      }
   }

   @EventHandler(
      priority = EventPriority.HIGHEST
   )
   public void onPlayerJoin(final PlayerJoinEvent event) {
      if (event.getPlayer() != null) {
         Player player = event.getPlayer();
         World world = player.getWorld();
         Location spawnLoc = world.getSpawnLocation();
         if (this.plugin.mv != null) {
            try {
               spawnLoc = this.plugin.mv.getMVWorldManager().getMVWorld(player.getWorld()).getSpawnLocation();
            } catch (NullPointerException var18) {
            } catch (ClassCastException var19) {
            } catch (NoClassDefFoundError var20) {
            }
         }

         if (this.plugin.essentialsSpawn != null) {
            spawnLoc = this.plugin.essentialsSpawn;
         }

         if (Spawn.getInstance().getLocation() != null) {
            spawnLoc = Spawn.getInstance().getLocation();
         }

         gm = player.getGameMode().getValue();
         String name = player.getName().toLowerCase();
         gameMode.put(name, gm);
         BukkitScheduler sched = this.plugin.getServer().getScheduler();
         if (!this.plugin.getCitizensCommunicator().isNPC(player, this.plugin) && !Utils.getInstance().isUnrestricted(player) && !CombatTagComunicator.isNPC(player)) {
            if (this.plugin.ess != null && Settings.disableSocialSpy) {
               this.plugin.ess.getUser(player.getName()).setSocialSpyEnabled(false);
            }

            String ip = player.getAddress().getAddress().getHostAddress();
            if (Settings.bungee && this.plugin.realIp.containsKey(name)) {
               ip = (String)this.plugin.realIp.get(name);
            }

            if (Settings.isAllowRestrictedIp && !Settings.getRestrictedIp(name, ip)) {
               int gM = (Integer)gameMode.get(name);
               player.setGameMode(GameMode.getByValue(gM));
               player.kickPlayer("You are not the Owner of this account, please try another name!");
               if (Settings.banUnsafeIp) {
                  this.plugin.getServer().banIP(ip);
               }

            } else {
               if (this.data.isAuthAvailable(name)) {
                  if (Settings.isSessionsEnabled) {
                     PlayerAuth auth = this.data.getAuth(name);
                     long timeout = (long)(Settings.getSessionTimeout * '\uea60');
                     long lastLogin = auth.getLastLogin();
                     long cur = (new Date()).getTime();
                     if ((cur - lastLogin < timeout || timeout == 0L) && !auth.getIp().equals("198.18.0.1")) {
                        if (auth.getNickname().equalsIgnoreCase(name) && auth.getIp().equals(ip)) {
                           this.plugin.getServer().getPluginManager().callEvent(new SessionEvent(auth, true));
                           if (PlayerCache.getInstance().getAuth(name) != null) {
                              PlayerCache.getInstance().updatePlayer(auth);
                           } else {
                              PlayerCache.getInstance().addPlayer(auth);
                           }

                           player.sendMessage(this.m._("valid_session"));
                           return;
                        }

                        if (!Settings.sessionExpireOnIpChange) {
                           int gM = (Integer)gameMode.get(name);
                           player.setGameMode(GameMode.getByValue(gM));
                           player.kickPlayer(this.m._("unvalid_session"));
                           return;
                        }

                        if (!auth.getNickname().equalsIgnoreCase(name)) {
                           int gM = (Integer)gameMode.get(name);
                           player.setGameMode(GameMode.getByValue(gM));
                           player.kickPlayer(this.m._("unvalid_session"));
                           return;
                        }

                        if (Settings.isForceSurvivalModeEnabled && !Settings.forceOnlyAfterLogin) {
                           sched.scheduleSyncDelayedTask(this.plugin, new Runnable() {
                              public void run() {
                                 event.getPlayer().setGameMode(GameMode.SURVIVAL);
                              }
                           });
                        }

                        PlayerCache.getInstance().removePlayer(name);
                        LimboCache.getInstance().addLimboPlayer(player, this.utils.removeAll(player));
                     } else {
                        PlayerCache.getInstance().removePlayer(name);
                        LimboCache.getInstance().addLimboPlayer(player, this.utils.removeAll(player));
                     }
                  }

                  if (Settings.isForceSurvivalModeEnabled && !Settings.forceOnlyAfterLogin) {
                     event.getPlayer().setGameMode(GameMode.SURVIVAL);
                  }

                  LimboCache.getInstance().updateLimboPlayer(player);
                  DataFileCache dataFile = new DataFileCache(LimboCache.getInstance().getLimboPlayer(name).getInventory(), LimboCache.getInstance().getLimboPlayer(name).getArmour());
                  this.playerBackup.createCache(name, dataFile, LimboCache.getInstance().getLimboPlayer(name).getGroup(), LimboCache.getInstance().getLimboPlayer(name).getOperator(), LimboCache.getInstance().getLimboPlayer(name).isFlying());
               } else {
                  if (Settings.isForceSurvivalModeEnabled && !Settings.forceOnlyAfterLogin) {
                     event.getPlayer().setGameMode(GameMode.SURVIVAL);
                  }

                  if (!Settings.unRegisteredGroup.isEmpty()) {
                     this.utils.setGroup(player, Utils.groupType.UNREGISTERED);
                  }

                  if (!Settings.isForcedRegistrationEnabled) {
                     return;
                  }
               }

               if (Settings.protectInventoryBeforeLogInEnabled) {
                  try {
                     LimboPlayer limbo = LimboCache.getInstance().getLimboPlayer(player.getName().toLowerCase());
                     ProtectInventoryEvent ev = new ProtectInventoryEvent(player, limbo.getInventory(), limbo.getArmour(), 36, 4);
                     this.plugin.getServer().getPluginManager().callEvent(ev);
                     if (ev.isCancelled() && !Settings.noConsoleSpam) {
                        ConsoleLogger.info("ProtectInventoryEvent has been cancelled for " + player.getName() + " ...");
                     }
                  } catch (NullPointerException var17) {
                  }
               }

               if (Settings.isTeleportToSpawnEnabled || Settings.isForceSpawnLocOnJoinEnabled && Settings.getForcedWorlds.contains(player.getWorld().getName())) {
                  SpawnTeleportEvent tpEvent = new SpawnTeleportEvent(player, player.getLocation(), spawnLoc, PlayerCache.getInstance().isAuthenticated(name));
                  this.plugin.getServer().getPluginManager().callEvent(tpEvent);
                  if (!tpEvent.isCancelled()) {
                     if (!tpEvent.getTo().getWorld().getChunkAt(tpEvent.getTo()).isLoaded()) {
                        tpEvent.getTo().getWorld().getChunkAt(tpEvent.getTo()).load();
                     }

                     player.teleport(tpEvent.getTo());
                  }
               }

               this.placePlayerSafely(player, spawnLoc);
               String msg = "";
               if (Settings.emailRegistration) {
                  msg = this.data.isAuthAvailable(name) ? this.m._("login_msg") : this.m._("reg_email_msg");
               } else {
                  msg = this.data.isAuthAvailable(name) ? this.m._("login_msg") : this.m._("reg_msg");
               }

               int time = Settings.getRegistrationTimeout * 20;
               int msgInterval = Settings.getWarnMessageInterval;
               if (time != 0) {
                  BukkitTask id = sched.runTaskLater(this.plugin, new TimeoutTask(this.plugin, name), (long)time);
                  if (!LimboCache.getInstance().hasLimboPlayer(name)) {
                     LimboCache.getInstance().addLimboPlayer(player);
                  }

                  LimboCache.getInstance().getLimboPlayer(name).setTimeoutTaskId(id.getTaskId());
               }

               if (!LimboCache.getInstance().hasLimboPlayer(name)) {
                  LimboCache.getInstance().addLimboPlayer(player);
               }

               if (player.isOp()) {
                  player.setOp(false);
               }

               player.setAllowFlight(true);
               player.setFlying(true);
               BukkitTask msgT = sched.runTask(this.plugin, new MessageTask(this.plugin, name, msg, msgInterval));
               LimboCache.getInstance().getLimboPlayer(name).setMessageTaskId(msgT.getTaskId());
               player.setNoDamageTicks(Settings.getRegistrationTimeout * 20);
               if (Settings.useEssentialsMotd) {
                  player.performCommand("motd");
               }

            }
         }
      }
   }

   private void placePlayerSafely(Player player, Location spawnLoc) {
      if (!Settings.isTeleportToSpawnEnabled && (!Settings.isForceSpawnLocOnJoinEnabled || !Settings.getForcedWorlds.contains(player.getWorld().getName()))) {
         Block b = player.getLocation().getBlock();
         if (b.getType() == Material.PORTAL || b.getType() == Material.ENDER_PORTAL || b.getType() == Material.LAVA || b.getType() == Material.STATIONARY_LAVA) {
            player.sendMessage(this.m._("unsafe_spawn"));
            player.teleport(spawnLoc);
         }
      }
   }

   @EventHandler(
      priority = EventPriority.MONITOR
   )
   public void onPlayerQuit(PlayerQuitEvent event) {
      if (event.getPlayer() != null) {
         Player player = event.getPlayer();
         String name = player.getName().toLowerCase();
         Location loc = player.getLocation();
         if (loc.getY() % (double)1.0F != (double)0.0F) {
            loc.add((double)0.0F, (double)0.5F, (double)0.0F);
         }

         if (!this.plugin.getCitizensCommunicator().isNPC(player, this.plugin) && !Utils.getInstance().isUnrestricted(player) && !CombatTagComunicator.isNPC(player)) {
            if (PlayerCache.getInstance().isAuthenticated(name) && !player.isDead() && Settings.isSaveQuitLocationEnabled && this.data.isAuthAvailable(name)) {
               final PlayerAuth auth = new PlayerAuth(event.getPlayer().getName().toLowerCase(), loc.getBlockX(), loc.getBlockY(), loc.getBlockZ(), loc.getWorld().getName());

               try {
                  if (this.data instanceof Thread) {
                     this.data.updateQuitLoc(auth);
                  } else {
                     Bukkit.getScheduler().runTaskAsynchronously(this.plugin, new Runnable() {
                        public void run() {
                           AuthMePlayerListener.this.data.updateQuitLoc(auth);
                        }
                     });
                  }
               } catch (NullPointerException var8) {
               }
            }

            if (LimboCache.getInstance().hasLimboPlayer(name)) {
               LimboPlayer limbo = LimboCache.getInstance().getLimboPlayer(name);
               if (Settings.protectInventoryBeforeLogInEnabled && player.hasPlayedBefore()) {
                  RestoreInventoryEvent ev = new RestoreInventoryEvent(player, limbo.getInventory(), limbo.getArmour());
                  this.plugin.getServer().getPluginManager().callEvent(ev);
                  if (!ev.isCancelled()) {
                     API.setPlayerInventory(player, limbo.getInventory(), limbo.getArmour());
                  }
               }

               this.utils.addNormal(player, limbo.getGroup());
               player.setOp(limbo.getOperator());
               if (player.getGameMode() != GameMode.CREATIVE) {
                  player.setAllowFlight(limbo.isFlying());
               }

               player.setFlying(limbo.isFlying());
               this.plugin.getServer().getScheduler().cancelTask(limbo.getTimeoutTaskId());
               LimboCache.getInstance().deleteLimboPlayer(name);
               if (this.playerBackup.doesCacheExist(name)) {
                  this.playerBackup.removeCache(name);
               }
            }

            try {
               PlayerCache.getInstance().removePlayer(name);
               PlayersLogs.players.remove(player.getName());
               PlayersLogs.getInstance().save();
               player.getVehicle().eject();
            } catch (NullPointerException var7) {
            }

            if (gameMode.containsKey(name)) {
               gameMode.remove(name);
            }

            this.plugin.premium.remove(player.getName());
            player.saveData();
         }
      }
   }

   @EventHandler(
      priority = EventPriority.MONITOR
   )
   public void onPlayerKick(PlayerKickEvent event) {
      if (event.getPlayer() != null) {
         if (!event.isCancelled()) {
            Player player = event.getPlayer();
            Location loc = player.getLocation();
            if (loc.getY() % (double)1.0F != (double)0.0F) {
               loc.add((double)0.0F, (double)0.5F, (double)0.0F);
            }

            if (!this.plugin.getCitizensCommunicator().isNPC(player, this.plugin) && !Utils.getInstance().isUnrestricted(player) && !CombatTagComunicator.isNPC(player)) {
               if (Settings.isForceSingleSessionEnabled && event.getReason().contains("You logged in from another location")) {
                  event.setCancelled(true);
               } else {
                  String name = player.getName().toLowerCase();
                  if (PlayerCache.getInstance().isAuthenticated(name) && !player.isDead() && Settings.isSaveQuitLocationEnabled && this.data.isAuthAvailable(name)) {
                     final PlayerAuth auth = new PlayerAuth(event.getPlayer().getName().toLowerCase(), loc.getBlockX(), loc.getBlockY(), loc.getBlockZ(), loc.getWorld().getName());

                     try {
                        if (this.data instanceof Thread) {
                           this.data.updateQuitLoc(auth);
                        } else {
                           Bukkit.getScheduler().runTaskAsynchronously(this.plugin, new Runnable() {
                              public void run() {
                                 AuthMePlayerListener.this.data.updateQuitLoc(auth);
                              }
                           });
                        }
                     } catch (NullPointerException var10) {
                     }
                  }

                  if (LimboCache.getInstance().hasLimboPlayer(name)) {
                     LimboPlayer limbo = LimboCache.getInstance().getLimboPlayer(name);
                     if (Settings.protectInventoryBeforeLogInEnabled) {
                        try {
                           RestoreInventoryEvent ev = new RestoreInventoryEvent(player, limbo.getInventory(), limbo.getArmour());
                           this.plugin.getServer().getPluginManager().callEvent(ev);
                           if (!ev.isCancelled()) {
                              API.setPlayerInventory(player, ev.getInventory(), ev.getArmor());
                           }
                        } catch (NullPointerException var9) {
                           ConsoleLogger.showError("Problem while restore " + name + "inventory after a kick");
                        }
                     }

                     try {
                        AuthMeTeleportEvent tpEvent = new AuthMeTeleportEvent(player, limbo.getLoc());
                        this.plugin.getServer().getPluginManager().callEvent(tpEvent);
                        if (!tpEvent.isCancelled()) {
                           if (!tpEvent.getTo().getWorld().getChunkAt(tpEvent.getTo()).isLoaded()) {
                              tpEvent.getTo().getWorld().getChunkAt(tpEvent.getTo()).load();
                           }

                           player.teleport(tpEvent.getTo());
                        }
                     } catch (NullPointerException var8) {
                     }

                     this.utils.addNormal(player, limbo.getGroup());
                     player.setOp(limbo.getOperator());
                     if (player.getGameMode() != GameMode.CREATIVE) {
                        player.setAllowFlight(limbo.isFlying());
                     }

                     player.setFlying(limbo.isFlying());
                     this.plugin.getServer().getScheduler().cancelTask(limbo.getTimeoutTaskId());
                     LimboCache.getInstance().deleteLimboPlayer(name);
                     if (this.playerBackup.doesCacheExist(name)) {
                        this.playerBackup.removeCache(name);
                     }
                  }

                  try {
                     PlayerCache.getInstance().removePlayer(name);
                     PlayersLogs.players.remove(player.getName());
                     PlayersLogs.getInstance().save();
                     if (gameMode.containsKey(name)) {
                        gameMode.remove(name);
                     }

                     player.getVehicle().eject();
                     player.saveData();
                     this.plugin.premium.remove(player.getName());
                  } catch (NullPointerException var7) {
                  }

               }
            }
         }
      }
   }

   @EventHandler(
      priority = EventPriority.MONITOR
   )
   public void onPlayerPickupItem(PlayerPickupItemEvent event) {
      if (!event.isCancelled() && event.getPlayer() != null) {
         Player player = event.getPlayer();
         String name = player.getName().toLowerCase();
         if (!Utils.getInstance().isUnrestricted(player)) {
            if (!PlayerCache.getInstance().isAuthenticated(player.getName().toLowerCase())) {
               if (this.data.isAuthAvailable(name) || Settings.isForcedRegistrationEnabled) {
                  event.setCancelled(true);
               }
            }
         }
      }
   }

   @EventHandler(
      priority = EventPriority.LOWEST
   )
   public void onPlayerInteract(PlayerInteractEvent event) {
      if (!event.isCancelled() && event.getPlayer() != null) {
         Player player = event.getPlayer();
         String name = player.getName().toLowerCase();
         if (!Utils.getInstance().isUnrestricted(player)) {
            if (!PlayerCache.getInstance().isAuthenticated(player.getName().toLowerCase())) {
               if (this.data.isAuthAvailable(name) || Settings.isForcedRegistrationEnabled) {
                  if (event.getClickedBlock() != null && event.getClickedBlock().getType() != Material.AIR) {
                     event.setUseInteractedBlock(org.bukkit.event.Event.Result.DENY);
                  }

                  event.setUseItemInHand(org.bukkit.event.Event.Result.DENY);
                  event.setCancelled(true);
               }
            }
         }
      }
   }

   @EventHandler(
      priority = EventPriority.HIGHEST
   )
   public void onPlayerInventoryOpen(InventoryOpenEvent event) {
      if (!event.isCancelled() && event.getPlayer() != null) {
         if (event.getPlayer() instanceof Player) {
            Player player = (Player)event.getPlayer();
            String name = player.getName().toLowerCase();
            if (!Utils.getInstance().isUnrestricted(player)) {
               if (!PlayerCache.getInstance().isAuthenticated(player.getName().toLowerCase())) {
                  if (this.data.isAuthAvailable(name) || Settings.isForcedRegistrationEnabled) {
                     event.setCancelled(true);
                  }
               }
            }
         }
      }
   }

   @EventHandler(
      priority = EventPriority.HIGHEST
   )
   public void onPlayerInventoryClick(InventoryClickEvent event) {
      if (!event.isCancelled() && event.getWhoClicked() != null) {
         if (event.getWhoClicked() instanceof Player) {
            Player player = (Player)event.getWhoClicked();
            String name = player.getName().toLowerCase();
            if (!Utils.getInstance().isUnrestricted(player)) {
               if (!PlayerCache.getInstance().isAuthenticated(player.getName().toLowerCase())) {
                  if (this.data.isAuthAvailable(name) || Settings.isForcedRegistrationEnabled) {
                     event.setResult(org.bukkit.event.Event.Result.DENY);
                     event.setCancelled(true);
                  }
               }
            }
         }
      }
   }

   @EventHandler(
      priority = EventPriority.LOWEST
   )
   public void onPlayerInteractEntity(PlayerInteractEntityEvent event) {
      if (!event.isCancelled() && event.getPlayer() != null) {
         Player player = event.getPlayer();
         String name = player.getName().toLowerCase();
         if (!this.plugin.getCitizensCommunicator().isNPC(player, this.plugin) && !Utils.getInstance().isUnrestricted(player) && !CombatTagComunicator.isNPC(player)) {
            if (!PlayerCache.getInstance().isAuthenticated(player.getName().toLowerCase())) {
               if (this.data.isAuthAvailable(name) || Settings.isForcedRegistrationEnabled) {
                  event.setCancelled(true);
               }
            }
         }
      }
   }

   @EventHandler(
      priority = EventPriority.LOWEST
   )
   public void onPlayerDropItem(PlayerDropItemEvent event) {
      if (!event.isCancelled() && event.getPlayer() != null) {
         Player player = event.getPlayer();
         String name = player.getName().toLowerCase();
         if (!Utils.getInstance().isUnrestricted(player) && !CombatTagComunicator.isNPC(player)) {
            if (!PlayerCache.getInstance().isAuthenticated(player.getName().toLowerCase())) {
               if (this.data.isAuthAvailable(name) || Settings.isForcedRegistrationEnabled) {
                  event.setCancelled(true);
               }
            }
         }
      }
   }

   @EventHandler(
      priority = EventPriority.LOWEST
   )
   public void onPlayerBedEnter(PlayerBedEnterEvent event) {
      if (!event.isCancelled() && event.getPlayer() != null) {
         Player player = event.getPlayer();
         String name = player.getName().toLowerCase();
         if (!Utils.getInstance().isUnrestricted(player)) {
            if (!PlayerCache.getInstance().isAuthenticated(player.getName().toLowerCase())) {
               if (this.data.isAuthAvailable(name) || Settings.isForcedRegistrationEnabled) {
                  event.setCancelled(true);
               }
            }
         }
      }
   }

   @EventHandler(
      priority = EventPriority.LOWEST
   )
   public void onSignChange(SignChangeEvent event) {
      if (!event.isCancelled() && event.getPlayer() != null && event != null) {
         Player player = event.getPlayer();
         String name = player.getName().toLowerCase();
         if (!Utils.getInstance().isUnrestricted(player)) {
            if (!PlayerCache.getInstance().isAuthenticated(name)) {
               if (this.data.isAuthAvailable(name) || Settings.isForcedRegistrationEnabled) {
                  event.setCancelled(true);
               }
            }
         }
      }
   }

   @EventHandler(
      priority = EventPriority.HIGHEST
   )
   public void onPlayerRespawn(PlayerRespawnEvent event) {
      if (event.getPlayer() != null && event != null) {
         Player player = event.getPlayer();
         String name = player.getName().toLowerCase();
         if (!Utils.getInstance().isUnrestricted(player) && !CombatTagComunicator.isNPC(player)) {
            if (!PlayerCache.getInstance().isAuthenticated(name)) {
               if (this.data.isAuthAvailable(name) || Settings.isForcedRegistrationEnabled) {
                  if (Settings.isTeleportToSpawnEnabled || Settings.isForceSpawnLocOnJoinEnabled) {
                     Location spawn = player.getWorld().getSpawnLocation();
                     if (this.plugin.mv != null) {
                        try {
                           spawn = this.plugin.mv.getMVWorldManager().getMVWorld(player.getWorld()).getSpawnLocation();
                        } catch (NullPointerException var6) {
                        } catch (ClassCastException var7) {
                        } catch (NoClassDefFoundError var8) {
                        }
                     }

                     if (this.plugin.essentialsSpawn != null) {
                        spawn = this.plugin.essentialsSpawn;
                     }

                     if (Spawn.getInstance().getLocation() != null && Spawn.getInstance().getLocation().getWorld().equals(player.getWorld())) {
                        spawn = Spawn.getInstance().getLocation();
                     }

                     event.setRespawnLocation(spawn);
                  }
               }
            }
         }
      }
   }
}
