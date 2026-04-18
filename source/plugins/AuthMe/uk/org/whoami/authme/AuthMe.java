package uk.org.whoami.authme;

import com.Acrobot.ChestShop.ChestShop;
import com.earth2me.essentials.Essentials;
import com.onarandombox.MultiverseCore.MultiverseCore;
import java.io.File;
import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Random;
import me.muizers.Notifications.Notifications;
import net.citizensnpcs.Citizens;
import net.milkbowl.vault.permission.Permission;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Server;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;
import uk.org.whoami.authme.api.API;
import uk.org.whoami.authme.cache.auth.PlayerAuth;
import uk.org.whoami.authme.cache.auth.PlayerCache;
import uk.org.whoami.authme.cache.backup.FileCache;
import uk.org.whoami.authme.cache.limbo.LimboCache;
import uk.org.whoami.authme.cache.limbo.LimboPlayer;
import uk.org.whoami.authme.commands.AdminCommand;
import uk.org.whoami.authme.commands.CaptchaCommand;
import uk.org.whoami.authme.commands.ChangePasswordCommand;
import uk.org.whoami.authme.commands.EmailCommand;
import uk.org.whoami.authme.commands.LoginCommand;
import uk.org.whoami.authme.commands.LogoutCommand;
import uk.org.whoami.authme.commands.PasspartuCommand;
import uk.org.whoami.authme.commands.RegisterCommand;
import uk.org.whoami.authme.commands.UnregisterCommand;
import uk.org.whoami.authme.datasource.CacheDataSource;
import uk.org.whoami.authme.datasource.DataSource;
import uk.org.whoami.authme.datasource.FileDataSource;
import uk.org.whoami.authme.datasource.MiniConnectionPoolManager;
import uk.org.whoami.authme.datasource.MySQLDataSource;
import uk.org.whoami.authme.datasource.SqliteDataSource;
import uk.org.whoami.authme.listener.AuthMeBlockListener;
import uk.org.whoami.authme.listener.AuthMeChestShopListener;
import uk.org.whoami.authme.listener.AuthMeEntityListener;
import uk.org.whoami.authme.listener.AuthMePlayerListener;
import uk.org.whoami.authme.listener.AuthMeSpoutListener;
import uk.org.whoami.authme.plugin.manager.BungeeCordMessage;
import uk.org.whoami.authme.plugin.manager.CitizensCommunicator;
import uk.org.whoami.authme.plugin.manager.CombatTagComunicator;
import uk.org.whoami.authme.plugin.manager.EssSpawn;
import uk.org.whoami.authme.settings.Messages;
import uk.org.whoami.authme.settings.PlayersLogs;
import uk.org.whoami.authme.settings.Settings;
import uk.org.whoami.authme.threads.FlatFileThread;
import uk.org.whoami.authme.threads.MySQLThread;
import uk.org.whoami.authme.threads.SQLiteThread;

public class AuthMe extends JavaPlugin {
   public DataSource database = null;
   private Settings settings;
   private Messages m;
   private PlayersLogs pllog;
   public static Server server;
   public static Plugin authme;
   public static Permission permission;
   private static AuthMe instance;
   private Utils utils = Utils.getInstance();
   private JavaPlugin plugin;
   private FileCache playerBackup = new FileCache();
   public CitizensCommunicator citizens;
   public SendMailSSL mail = null;
   public int CitizensVersion = 0;
   public int CombatTag = 0;
   public double ChestShop = (double)0.0F;
   public boolean BungeeCord = false;
   public Essentials ess;
   public Notifications notifications;
   public API api;
   public Management management;
   public HashMap captcha = new HashMap();
   public HashMap cap = new HashMap();
   public HashMap realIp = new HashMap();
   public List premium = new ArrayList();
   public MultiverseCore mv = null;
   public Location essentialsSpawn;
   public Thread databaseThread = null;
   // $FF: synthetic field
   private static int[] $SWITCH_TABLE$uk$org$whoami$authme$datasource$DataSource$DataSourceType;

   public AuthMe() {
      super();
   }

   public void onEnable() {
      instance = this;
      authme = instance;
      this.citizens = new CitizensCommunicator(this);
      this.settings = new Settings(this);
      this.settings.loadConfigOptions();
      this.setMessages(Messages.getInstance());
      this.pllog = PlayersLogs.getInstance();
      server = this.getServer();
      if (Settings.removePassword) {
         Bukkit.getLogger().setFilter(new ConsoleFilter());
      }

      if (!Settings.getmailAccount.isEmpty() && !Settings.getmailPassword.isEmpty()) {
         this.mail = new SendMailSSL(this);
      }

      this.citizensVersion();
      this.combatTag();
      this.checkNotifications();
      this.checkMultiverse();
      this.checkChestShop();
      this.checkEssentials();
      if (Settings.isBackupActivated && Settings.isBackupOnStart) {
         Boolean Backup = (new PerformBackup(this)).DoBackup();
         if (Backup) {
            ConsoleLogger.info("Backup Complete");
         } else {
            ConsoleLogger.showError("Error while making Backup");
         }
      }

      switch (Settings.getDataSource) {
         case MYSQL:
            if (Settings.useMultiThreading) {
               MySQLThread sqlThread = new MySQLThread();
               sqlThread.run();
               this.database = sqlThread;
               this.databaseThread = sqlThread;
            } else {
               try {
                  this.database = new MySQLDataSource();
               } catch (ClassNotFoundException ex) {
                  ConsoleLogger.showError(ex.getMessage());
                  if (Settings.isStopEnabled) {
                     ConsoleLogger.showError("Can't use MySQL... Please input correct MySQL informations ! SHUTDOWN...");
                     server.shutdown();
                  }

                  if (!Settings.isStopEnabled) {
                     this.getServer().getPluginManager().disablePlugin(this);
                  }

                  return;
               } catch (SQLException ex) {
                  ConsoleLogger.showError(ex.getMessage());
                  if (Settings.isStopEnabled) {
                     ConsoleLogger.showError("Can't use MySQL... Please input correct MySQL informations ! SHUTDOWN...");
                     server.shutdown();
                  }

                  if (!Settings.isStopEnabled) {
                     this.getServer().getPluginManager().disablePlugin(this);
                  }

                  return;
               } catch (MiniConnectionPoolManager.TimeoutException ex) {
                  ConsoleLogger.showError(ex.getMessage());
                  if (Settings.isStopEnabled) {
                     ConsoleLogger.showError("Can't use MySQL... Please input correct MySQL informations ! SHUTDOWN...");
                     server.shutdown();
                  }

                  if (!Settings.isStopEnabled) {
                     this.getServer().getPluginManager().disablePlugin(this);
                  }

                  return;
               }
            }
            break;
         case FILE:
            if (Settings.useMultiThreading) {
               FlatFileThread fileThread = new FlatFileThread();
               fileThread.run();
               this.database = fileThread;
               this.databaseThread = fileThread;
            } else {
               try {
                  this.database = new FileDataSource();
               } catch (IOException ex) {
                  ConsoleLogger.showError(ex.getMessage());
                  if (Settings.isStopEnabled) {
                     ConsoleLogger.showError("Can't use FLAT FILE... SHUTDOWN...");
                     server.shutdown();
                  }

                  if (!Settings.isStopEnabled) {
                     this.getServer().getPluginManager().disablePlugin(this);
                  }

                  return;
               }
            }
            break;
         case SQLITE:
            if (Settings.useMultiThreading) {
               SQLiteThread sqliteThread = new SQLiteThread();
               sqliteThread.run();
               this.database = sqliteThread;
               this.databaseThread = sqliteThread;
            } else {
               try {
                  this.database = new SqliteDataSource();
               } catch (ClassNotFoundException ex) {
                  ConsoleLogger.showError(ex.getMessage());
                  if (Settings.isStopEnabled) {
                     ConsoleLogger.showError("Can't use SQLITE... ! SHUTDOWN...");
                     server.shutdown();
                  }

                  if (!Settings.isStopEnabled) {
                     this.getServer().getPluginManager().disablePlugin(this);
                  }

                  return;
               } catch (SQLException ex) {
                  ConsoleLogger.showError(ex.getMessage());
                  if (Settings.isStopEnabled) {
                     ConsoleLogger.showError("Can't use SQLITE... ! SHUTDOWN...");
                     server.shutdown();
                  }

                  if (!Settings.isStopEnabled) {
                     this.getServer().getPluginManager().disablePlugin(this);
                  }

                  return;
               }
            }
      }

      if (Settings.isCachingEnabled) {
         this.database = new CacheDataSource(this, this.database);
      }

      this.api = new API(this, this.database);
      this.management = new Management(this.database, this);
      PluginManager pm = this.getServer().getPluginManager();
      if (pm.isPluginEnabled("Spout")) {
         pm.registerEvents(new AuthMeSpoutListener(this.database), this);
         ConsoleLogger.info("Successfully hook with Spout!");
      }

      pm.registerEvents(new AuthMePlayerListener(this, this.database), this);
      pm.registerEvents(new AuthMeBlockListener(this.database, this), this);
      pm.registerEvents(new AuthMeEntityListener(this.database, this), this);
      if (this.ChestShop != (double)0.0F) {
         pm.registerEvents(new AuthMeChestShopListener(this.database, this), this);
         ConsoleLogger.info("Successfully hook with ChestShop!");
      }

      if (Settings.bungee) {
         Bukkit.getMessenger().registerOutgoingPluginChannel(this, "BungeeCord");
         Bukkit.getMessenger().registerIncomingPluginChannel(this, "BungeeCord", new BungeeCordMessage(this));
      }

      if (pm.getPlugin("Vault") != null) {
         RegisteredServiceProvider<Permission> permissionProvider = this.getServer().getServicesManager().getRegistration(Permission.class);
         if (permissionProvider != null) {
            permission = (Permission)permissionProvider.getProvider();
            ConsoleLogger.info("Vault plugin detected, hook with " + permission.getName() + " system");
         } else {
            ConsoleLogger.showError("Vault plugin is detected but not the permissions plugin!");
         }
      }

      this.getCommand("authme").setExecutor(new AdminCommand(this, this.database));
      this.getCommand("register").setExecutor(new RegisterCommand(this.database, this));
      this.getCommand("login").setExecutor(new LoginCommand(this));
      this.getCommand("changepassword").setExecutor(new ChangePasswordCommand(this.database, this));
      this.getCommand("logout").setExecutor(new LogoutCommand(this, this.database));
      this.getCommand("unregister").setExecutor(new UnregisterCommand(this, this.database));
      this.getCommand("passpartu").setExecutor(new PasspartuCommand(this));
      this.getCommand("email").setExecutor(new EmailCommand(this, this.database));
      this.getCommand("captcha").setExecutor(new CaptchaCommand(this));
      if (!Settings.isForceSingleSessionEnabled) {
         ConsoleLogger.showError("ATTENTION by disabling ForceSingleSession, your server protection is set to low");
      }

      if (Settings.reloadSupport) {
         try {
            if (!(new File(this.getDataFolder() + File.separator + "players.yml")).exists()) {
               this.pllog = new PlayersLogs();
            }

            this.onReload();
            if (server.getOnlinePlayers().length < 1) {
               try {
                  PlayersLogs.players.clear();
                  this.pllog.save();
               } catch (NullPointerException var3) {
               }
            }
         } catch (NullPointerException var4) {
         }
      }

      ConsoleLogger.info("Authme " + this.getDescription().getVersion() + " enabled");
   }

   private void checkChestShop() {
      if (!Settings.chestshop) {
         this.ChestShop = (double)0.0F;
      } else {
         if (this.getServer().getPluginManager().isPluginEnabled("ChestShop")) {
            try {
               String ver = com.Acrobot.ChestShop.ChestShop.getVersion();

               try {
                  double version = Double.valueOf(ver.split(" ")[0]);
                  if (version >= (double)3.5F) {
                     this.ChestShop = version;
                  } else {
                     ConsoleLogger.showError("Please Update your ChestShop version!");
                  }
               } catch (NumberFormatException var6) {
                  try {
                     double version = Double.valueOf(ver.split("t")[0]);
                     if (version >= (double)3.5F) {
                        this.ChestShop = version;
                     } else {
                        ConsoleLogger.showError("Please Update your ChestShop version!");
                     }
                  } catch (NumberFormatException var5) {
                  }
               }
            } catch (NullPointerException var7) {
            } catch (NoClassDefFoundError var8) {
            } catch (ClassCastException var9) {
            }
         }

      }
   }

   private void checkMultiverse() {
      if (!Settings.multiverse) {
         this.mv = null;
      } else {
         if (this.getServer().getPluginManager().getPlugin("Multiverse-Core") != null && this.getServer().getPluginManager().getPlugin("Multiverse-Core").isEnabled()) {
            try {
               this.mv = (MultiverseCore)this.getServer().getPluginManager().getPlugin("Multiverse-Core");
               ConsoleLogger.info("Hook with Multiverse-Core for SpawnLocations");
            } catch (NullPointerException var2) {
               this.mv = null;
            } catch (ClassCastException var3) {
               this.mv = null;
            } catch (NoClassDefFoundError var4) {
               this.mv = null;
            }
         }

      }
   }

   private void checkEssentials() {
      if (this.getServer().getPluginManager().getPlugin("Essentials") != null && this.getServer().getPluginManager().getPlugin("Essentials").isEnabled()) {
         try {
            this.ess = (Essentials)this.getServer().getPluginManager().getPlugin("Essentials");
            ConsoleLogger.info("Hook with Essentials plugin");
         } catch (NullPointerException var2) {
            this.ess = null;
         } catch (ClassCastException var3) {
            this.ess = null;
         } catch (NoClassDefFoundError var4) {
            this.ess = null;
         }
      }

      if (this.getServer().getPluginManager().getPlugin("EssentialsSpawn") != null && this.getServer().getPluginManager().getPlugin("EssentialsSpawn").isEnabled()) {
         this.essentialsSpawn = (new EssSpawn()).getLocation();
         ConsoleLogger.info("Hook with EssentialsSpawn plugin");
      }

   }

   private void checkNotifications() {
      if (!Settings.notifications) {
         this.notifications = null;
      } else {
         if (this.getServer().getPluginManager().getPlugin("Notifications") != null && this.getServer().getPluginManager().getPlugin("Notifications").isEnabled()) {
            this.notifications = (Notifications)this.getServer().getPluginManager().getPlugin("Notifications");
            ConsoleLogger.info("Successfully hook with Notifications");
         } else {
            this.notifications = null;
         }

      }
   }

   private void combatTag() {
      if (this.getServer().getPluginManager().getPlugin("CombatTag") != null && this.getServer().getPluginManager().getPlugin("CombatTag").isEnabled()) {
         this.CombatTag = 1;
      } else {
         this.CombatTag = 0;
      }

   }

   private void citizensVersion() {
      if (this.getServer().getPluginManager().getPlugin("Citizens") != null && this.getServer().getPluginManager().getPlugin("Citizens").isEnabled()) {
         Citizens cit = (Citizens)this.getServer().getPluginManager().getPlugin("Citizens");
         String ver = cit.getDescription().getVersion();
         String[] args = ver.split("\\.");
         if (args[0].contains("1")) {
            this.CitizensVersion = 1;
         } else {
            this.CitizensVersion = 2;
         }
      } else {
         this.CitizensVersion = 0;
      }

   }

   public void onDisable() {
      if (Bukkit.getOnlinePlayers() != null) {
         Player[] var4;
         for(Player player : var4 = Bukkit.getOnlinePlayers()) {
            this.savePlayer(player);
         }
      }

      this.pllog.save();
      if (this.database != null) {
         this.database.close();
      }

      if (this.databaseThread != null) {
         this.databaseThread.interrupt();
      }

      if (Settings.isBackupActivated && Settings.isBackupOnStop) {
         Boolean Backup = (new PerformBackup(this)).DoBackup();
         if (Backup) {
            ConsoleLogger.info("Backup Complete");
         } else {
            ConsoleLogger.showError("Error while making Backup");
         }
      }

      ConsoleLogger.info("Authme " + this.getDescription().getVersion() + " disabled");
   }

   private void onReload() {
      try {
         if (Bukkit.getServer().getOnlinePlayers() != null && !PlayersLogs.players.isEmpty()) {
            Player[] var4;
            for(Player player : var4 = Bukkit.getServer().getOnlinePlayers()) {
               if (PlayersLogs.players.contains(player.getName())) {
                  String name = player.getName().toLowerCase();
                  PlayerAuth pAuth = this.database.getAuth(name);
                  if (pAuth == null) {
                     break;
                  }

                  PlayerAuth auth = new PlayerAuth(name, pAuth.getHash(), pAuth.getIp(), (new Date()).getTime());
                  this.database.updateSession(auth);
                  PlayerCache.getInstance().addPlayer(auth);
               }
            }
         }

      } catch (NullPointerException var8) {
      }
   }

   public static AuthMe getInstance() {
      return instance;
   }

   public void savePlayer(Player player) throws IllegalStateException, NullPointerException {
      try {
         if (this.citizens.isNPC(player, this) || Utils.getInstance().isUnrestricted(player) || CombatTagComunicator.isNPC(player)) {
            return;
         }
      } catch (Exception var5) {
      }

      try {
         String name = player.getName().toLowerCase();
         if (PlayerCache.getInstance().isAuthenticated(name) && !player.isDead() && Settings.isSaveQuitLocationEnabled) {
            final PlayerAuth auth = new PlayerAuth(player.getName().toLowerCase(), (int)player.getLocation().getX(), (int)player.getLocation().getY(), (int)player.getLocation().getZ(), player.getWorld().getName());
            Bukkit.getScheduler().runTaskAsynchronously(this.plugin, new Runnable() {
               public void run() {
                  AuthMe.this.database.updateQuitLoc(auth);
               }
            });
         }

         if (LimboCache.getInstance().hasLimboPlayer(name)) {
            LimboPlayer limbo = LimboCache.getInstance().getLimboPlayer(name);
            if (Settings.protectInventoryBeforeLogInEnabled) {
               player.getInventory().setArmorContents(limbo.getArmour());
               player.getInventory().setContents(limbo.getInventory());
            }

            if (!limbo.getLoc().getChunk().isLoaded()) {
               limbo.getLoc().getChunk().load();
            }

            player.teleport(limbo.getLoc());
            this.utils.addNormal(player, limbo.getGroup());
            player.setOp(limbo.getOperator());
            this.plugin.getServer().getScheduler().cancelTask(limbo.getTimeoutTaskId());
            LimboCache.getInstance().deleteLimboPlayer(name);
            if (this.playerBackup.doesCacheExist(name)) {
               this.playerBackup.removeCache(name);
            }
         }

         PlayerCache.getInstance().removePlayer(name);
         player.saveData();
      } catch (Exception var4) {
      }

   }

   public CitizensCommunicator getCitizensCommunicator() {
      return this.citizens;
   }

   public void setMessages(Messages m) {
      this.m = m;
   }

   public Messages getMessages() {
      return this.m;
   }

   public Player generateKickPlayer(Player[] players) {
      Player player = null;

      for(int i = 0; i <= players.length; ++i) {
         Random rdm = new Random();
         int a = rdm.nextInt(players.length);
         if (!this.authmePermissible(players[a], "authme.vip")) {
            player = players[a];
            break;
         }
      }

      if (player == null) {
         for(Player p : players) {
            if (!this.authmePermissible(p, "authme.vip")) {
               player = p;
               break;
            }
         }
      }

      return player;
   }

   public boolean authmePermissible(Player player, String perm) {
      if (player.hasPermission(perm)) {
         return true;
      } else {
         return permission != null ? permission.playerHas(player, perm) : false;
      }
   }

   public boolean authmePermissible(CommandSender sender, String perm) {
      if (sender.hasPermission(perm)) {
         return true;
      } else {
         return permission != null ? permission.has(sender, perm) : false;
      }
   }

   // $FF: synthetic method
   static int[] $SWITCH_TABLE$uk$org$whoami$authme$datasource$DataSource$DataSourceType() {
      int[] var10000 = $SWITCH_TABLE$uk$org$whoami$authme$datasource$DataSource$DataSourceType;
      if (var10000 != null) {
         return var10000;
      } else {
         int[] var0 = new int[DataSource.DataSourceType.values().length];

         try {
            var0[DataSource.DataSourceType.FILE.ordinal()] = 2;
         } catch (NoSuchFieldError var3) {
         }

         try {
            var0[DataSource.DataSourceType.MYSQL.ordinal()] = 1;
         } catch (NoSuchFieldError var2) {
         }

         try {
            var0[DataSource.DataSourceType.SQLITE.ordinal()] = 3;
         } catch (NoSuchFieldError var1) {
         }

         $SWITCH_TABLE$uk$org$whoami$authme$datasource$DataSource$DataSourceType = var0;
         return var0;
      }
   }
}
