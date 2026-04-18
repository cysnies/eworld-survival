package uk.org.whoami.authme.commands;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import uk.org.whoami.authme.AuthMe;
import uk.org.whoami.authme.ConsoleLogger;
import uk.org.whoami.authme.Utils;
import uk.org.whoami.authme.cache.auth.PlayerAuth;
import uk.org.whoami.authme.cache.auth.PlayerCache;
import uk.org.whoami.authme.converter.FlatToSql;
import uk.org.whoami.authme.converter.FlatToSqlite;
import uk.org.whoami.authme.converter.RakamakConverter;
import uk.org.whoami.authme.converter.xAuthToFlat;
import uk.org.whoami.authme.datasource.DataSource;
import uk.org.whoami.authme.security.PasswordSecurity;
import uk.org.whoami.authme.settings.Messages;
import uk.org.whoami.authme.settings.Settings;
import uk.org.whoami.authme.settings.Spawn;
import uk.org.whoami.authme.settings.SpoutCfg;

public class AdminCommand implements CommandExecutor {
   public AuthMe plugin;
   private Messages m = Messages.getInstance();
   private SpoutCfg s = SpoutCfg.getInstance();
   public DataSource database;

   public AdminCommand(AuthMe plugin, DataSource database) {
      super();
      this.database = database;
      this.plugin = plugin;
   }

   public boolean onCommand(final CommandSender sender, Command cmnd, String label, final String[] args) {
      if (args.length == 0) {
         sender.sendMessage("Usage: /authme reload - Reload the config");
         sender.sendMessage("/authme register <playername> <password> - Register a player");
         sender.sendMessage("/authme changepassword <playername> <password> - Change player password");
         sender.sendMessage("/authme unregister <playername> - Unregister a player");
         sender.sendMessage("/authme purge <days> - Purge Database");
         sender.sendMessage("/authme version - Get AuthMe version infos");
         sender.sendMessage("/authme lastlogin <playername> - Display Date about the Player's LastLogin");
         return true;
      } else if (sender instanceof ConsoleCommandSender && args[0].equalsIgnoreCase("passpartuToken")) {
         if (args.length > 1) {
            System.out.println("[AuthMe] command usage: authme passpartuToken");
            return true;
         } else {
            if (Utils.getInstance().obtainToken()) {
               System.out.println("[AuthMe] You have 30s for insert this token ingame with /passpartu [token]");
            } else {
               System.out.println("[AuthMe] Security error on passpartu token, redo it. ");
            }

            return true;
         }
      } else if (!this.plugin.authmePermissible(sender, "authme.admin." + args[0].toLowerCase())) {
         sender.sendMessage(this.m._("no_perm"));
         return true;
      } else if (args[0].equalsIgnoreCase("version")) {
         sender.sendMessage("AuthMe Version: " + AuthMe.getInstance().getDescription().getVersion());
         return true;
      } else if (args[0].equalsIgnoreCase("purge")) {
         if (args.length != 2) {
            sender.sendMessage("Usage: /authme purge <DAYS>");
            return true;
         } else {
            try {
               long days = Long.parseLong(args[1]) * 86400000L;
               long until = (new Date()).getTime() - days;
               sender.sendMessage("Deleted " + this.database.purgeDatabase(until) + " user accounts");
               return true;
            } catch (NumberFormatException var31) {
               sender.sendMessage("Usage: /authme purge <DAYS>");
               return true;
            }
         }
      } else {
         if (args[0].equalsIgnoreCase("reload")) {
            this.database.reload();
            File newConfigFile = new File("plugins/AuthMe", "config.yml");
            if (!newConfigFile.exists()) {
               InputStream fis = this.getClass().getResourceAsStream("/config.yml");
               FileOutputStream fos = null;

               try {
                  fos = new FileOutputStream(newConfigFile);
                  byte[] buf = new byte[1024];
                  int i = 0;

                  while((i = fis.read(buf)) != -1) {
                     fos.write(buf, 0, i);
                  }
               } catch (Exception var43) {
                  Logger.getLogger(JavaPlugin.class.getName()).log(Level.SEVERE, "Failed to load config from JAR");
               } finally {
                  try {
                     if (fis != null) {
                        fis.close();
                     }

                     if (fos != null) {
                        fos.close();
                     }
                  } catch (Exception var30) {
                  }

               }
            }

            YamlConfiguration newConfig = YamlConfiguration.loadConfiguration(newConfigFile);
            Settings.reloadConfigOptions(newConfig);
            this.m.reload();
            this.s.reload();
            sender.sendMessage(this.m._("reload"));
         } else if (args[0].equalsIgnoreCase("lastlogin")) {
            if (args.length != 2) {
               sender.sendMessage("Usage: /authme lastlogin <playername>");
               return true;
            }

            try {
               if (this.database.getAuth(args[1].toLowerCase()) != null) {
                  PlayerAuth player = this.database.getAuth(args[1].toLowerCase());
                  long lastLogin = player.getLastLogin();
                  Date d = new Date(lastLogin);
                  long diff = System.currentTimeMillis() - lastLogin;
                  String msg = (int)(diff / 86400000L) + " days " + (int)(diff / 3600000L % 24L) + " hours " + (int)(diff / 60000L % 60L) + " mins " + (int)(diff / 1000L % 60L) + " secs.";
                  String lastIP = player.getIp();
                  sender.sendMessage("[AuthMe] " + args[1].toLowerCase() + " lastlogin : " + d.toString());
                  sender.sendMessage("[AuthMe] The player : " + player.getNickname() + " is unlogged since " + msg);
                  sender.sendMessage("[AuthMe] LastPlayer IP : " + lastIP);
               }
            } catch (NullPointerException var42) {
               sender.sendMessage("This player does not exist");
            }
         } else {
            if (args[0].equalsIgnoreCase("accounts")) {
               if (args.length != 2) {
                  sender.sendMessage("Usage: /authme accounts <playername>");
                  sender.sendMessage("Or: /authme accounts <ip>");
                  return true;
               }

               if (!args[1].contains(".")) {
                  Bukkit.getScheduler().scheduleSyncDelayedTask(this.plugin, new Runnable() {
                     public void run() {
                        PlayerAuth pAuth = null;
                        String message = "[AuthMe] ";

                        try {
                           pAuth = AdminCommand.this.database.getAuth(args[1].toLowerCase());
                        } catch (NullPointerException var7) {
                           sender.sendMessage("[AuthMe] This player is unknown");
                           return;
                        }

                        if (pAuth != null) {
                           List<String> accountList = AdminCommand.this.database.getAllAuthsByName(pAuth);
                           if (!accountList.isEmpty() && accountList != null) {
                              if (accountList.size() == 1) {
                                 sender.sendMessage("[AuthMe] " + args[1] + " is a single account player");
                              } else {
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

                                 sender.sendMessage("[AuthMe] " + args[1] + " has " + accountList.size() + " accounts");
                                 sender.sendMessage(message);
                              }
                           } else {
                              sender.sendMessage("[AuthMe] This player is unknown");
                           }
                        } else {
                           sender.sendMessage("[AuthMe] This player is unknown");
                        }
                     }
                  });
                  return true;
               }

               Bukkit.getScheduler().scheduleSyncDelayedTask(this.plugin, new Runnable() {
                  public void run() {
                     String message = "[AuthMe] ";
                     if (args[1] != null) {
                        List<String> accountList = AdminCommand.this.database.getAllAuthsByIp(args[1]);
                        if (!accountList.isEmpty() && accountList != null) {
                           if (accountList.size() == 1) {
                              sender.sendMessage("[AuthMe] " + args[1] + " is a single account player");
                           } else {
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

                              sender.sendMessage("[AuthMe] " + args[1] + " has " + accountList.size() + " accounts");
                              sender.sendMessage(message);
                           }
                        } else {
                           sender.sendMessage("[AuthMe] Please put a valid IP");
                        }
                     } else {
                        sender.sendMessage("[AuthMe] Please put a valid IP");
                     }
                  }
               });
               return true;
            }

            if (!args[0].equalsIgnoreCase("register") && !args[0].equalsIgnoreCase("reg")) {
               if (args[0].equalsIgnoreCase("convertflattosql")) {
                  try {
                     FlatToSql.FlatToSqlConverter();
                     if (sender instanceof Player) {
                        sender.sendMessage("[AuthMe] FlatFile converted to authme.sql file");
                     }
                  } catch (IOException e) {
                     e.printStackTrace();
                  } catch (NullPointerException ex) {
                     System.out.println(ex.getMessage());
                  }
               } else if (args[0].equalsIgnoreCase("flattosqlite")) {
                  try {
                     String s = FlatToSqlite.convert();
                     if (sender instanceof Player) {
                        sender.sendMessage(s);
                     }
                  } catch (IOException e) {
                     e.printStackTrace();
                  } catch (NullPointerException ex) {
                     System.out.println(ex.getMessage());
                  }
               } else if (args[0].equalsIgnoreCase("xauthimport")) {
                  xAuthToFlat converter = new xAuthToFlat(this.plugin, this.database);
                  if (converter.convert(sender)) {
                     sender.sendMessage("[AuthMe] Successfull convert from xAuth database");
                  } else {
                     sender.sendMessage("[AuthMe] Error while trying to convert from xAuth database");
                  }
               } else {
                  if (args[0].equalsIgnoreCase("getemail")) {
                     if (args.length != 2) {
                        sender.sendMessage("Usage: /authme getemail playername");
                        return true;
                     }

                     String playername = args[1].toLowerCase();
                     PlayerAuth getAuth = PlayerCache.getInstance().getAuth(playername);
                     sender.sendMessage("[AuthMe] " + args[1] + " email : " + getAuth.getEmail());
                     return true;
                  }

                  if (args[0].equalsIgnoreCase("chgemail")) {
                     if (args.length != 3) {
                        sender.sendMessage("Usage: /authme chgemail playername email");
                        return true;
                     }

                     String playername = args[1].toLowerCase();
                     PlayerAuth getAuth = PlayerCache.getInstance().getAuth(playername);
                     getAuth.setEmail(args[2]);
                     if (!this.database.updateEmail(getAuth)) {
                        sender.sendMessage(this.m._("error"));
                        return true;
                     }

                     PlayerCache.getInstance().updatePlayer(getAuth);
                     return true;
                  }

                  if (args[0].equalsIgnoreCase("convertfromrakamak")) {
                     try {
                        RakamakConverter.RakamakConvert();
                        if (sender instanceof Player) {
                           sender.sendMessage("[AuthMe] Rakamak database converted to auths.db");
                        }
                     } catch (IOException e) {
                        e.printStackTrace();
                     } catch (NullPointerException ex) {
                        ConsoleLogger.showError(ex.getMessage());
                     }
                  } else if (args[0].equalsIgnoreCase("setspawn")) {
                     try {
                        if (sender instanceof Player) {
                           if (Spawn.getInstance().setSpawn(((Player)sender).getLocation())) {
                              sender.sendMessage("[AuthMe] Correctly define new spawn");
                           } else {
                              sender.sendMessage("[AuthMe] SetSpawn fail , please retry");
                           }
                        } else {
                           sender.sendMessage("[AuthMe] Please use that command in game");
                        }
                     } catch (NullPointerException ex) {
                        ConsoleLogger.showError(ex.getMessage());
                     }
                  } else if (args[0].equalsIgnoreCase("purgebannedplayers")) {
                     final List<String> bannedPlayers = new ArrayList();

                     for(OfflinePlayer off : this.plugin.getServer().getBannedPlayers()) {
                        bannedPlayers.add(off.getName().toLowerCase());
                     }

                     if (this.database instanceof Thread) {
                        this.database.purgeBanned(bannedPlayers);
                     } else {
                        Bukkit.getScheduler().runTaskAsynchronously(this.plugin, new Runnable() {
                           public void run() {
                              AdminCommand.this.database.purgeBanned(bannedPlayers);
                           }
                        });
                     }
                  } else if (args[0].equalsIgnoreCase("spawn")) {
                     try {
                        if (sender instanceof Player) {
                           if (Spawn.getInstance().getLocation() != null) {
                              ((Player)sender).teleport(Spawn.getInstance().getLocation());
                           } else {
                              sender.sendMessage("[AuthMe] Spawn fail , please try to define the spawn");
                           }
                        } else {
                           sender.sendMessage("[AuthMe] Please use that command in game");
                        }
                     } catch (NullPointerException ex) {
                        ConsoleLogger.showError(ex.getMessage());
                     }
                  } else if (!args[0].equalsIgnoreCase("changepassword") && !args[0].equalsIgnoreCase("cp")) {
                     if (!args[0].equalsIgnoreCase("unregister") && !args[0].equalsIgnoreCase("unreg") && !args[0].equalsIgnoreCase("del")) {
                        sender.sendMessage("Usage: /authme reload|register playername password|changepassword playername password|unregister playername");
                     } else {
                        if (args.length != 2) {
                           sender.sendMessage("Usage: /authme unregister playername");
                           return true;
                        }

                        String name = args[1].toLowerCase();
                        if (!this.database.removeAuth(name)) {
                           sender.sendMessage(this.m._("error"));
                           return true;
                        }

                        PlayerCache.getInstance().removePlayer(name);
                        sender.sendMessage("unregistered");
                        ConsoleLogger.info(args[1] + " unregistered");
                     }
                  } else {
                     if (args.length != 3) {
                        sender.sendMessage("Usage: /authme changepassword playername newpassword");
                        return true;
                     }

                     try {
                        String name = args[1].toLowerCase();
                        String hash = PasswordSecurity.getHash(Settings.getPasswordHash, args[2], name);
                        PlayerAuth auth = null;
                        if (PlayerCache.getInstance().isAuthenticated(name)) {
                           auth = PlayerCache.getInstance().getAuth(name);
                        } else {
                           if (!this.database.isAuthAvailable(name)) {
                              sender.sendMessage(this.m._("unknown_user"));
                              return true;
                           }

                           auth = this.database.getAuth(name);
                        }

                        auth.setHash(hash);
                        if (!this.database.updatePassword(auth)) {
                           sender.sendMessage(this.m._("error"));
                           return true;
                        }

                        sender.sendMessage("pwd_changed");
                        ConsoleLogger.info(args[1] + "'s password changed");
                     } catch (NoSuchAlgorithmException ex) {
                        ConsoleLogger.showError(ex.getMessage());
                        sender.sendMessage(this.m._("error"));
                     }
                  }
               }
            } else {
               if (args.length != 3) {
                  sender.sendMessage("Usage: /authme register playername password");
                  return true;
               }

               try {
                  String name = args[1].toLowerCase();
                  String hash = PasswordSecurity.getHash(Settings.getPasswordHash, args[2], name);
                  if (this.database.isAuthAvailable(name)) {
                     sender.sendMessage(this.m._("user_regged"));
                     return true;
                  }

                  PlayerAuth auth = new PlayerAuth(name, hash, "198.18.0.1", 0L);
                  if (!this.database.saveAuth(auth)) {
                     sender.sendMessage(this.m._("error"));
                     return true;
                  }

                  sender.sendMessage(this.m._("registered"));
                  ConsoleLogger.info(args[1] + " registered");
               } catch (NoSuchAlgorithmException ex) {
                  ConsoleLogger.showError(ex.getMessage());
                  sender.sendMessage(this.m._("error"));
               }
            }
         }

         return true;
      }
   }
}
