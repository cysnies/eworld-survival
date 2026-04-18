package uk.org.whoami.authme.commands;

import java.security.NoSuchAlgorithmException;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import uk.org.whoami.authme.AuthMe;
import uk.org.whoami.authme.ConsoleLogger;
import uk.org.whoami.authme.cache.auth.PlayerAuth;
import uk.org.whoami.authme.cache.auth.PlayerCache;
import uk.org.whoami.authme.datasource.DataSource;
import uk.org.whoami.authme.security.PasswordSecurity;
import uk.org.whoami.authme.security.RandomString;
import uk.org.whoami.authme.settings.Messages;
import uk.org.whoami.authme.settings.Settings;

public class EmailCommand implements CommandExecutor {
   public AuthMe plugin;
   private DataSource data;
   private Messages m = Messages.getInstance();

   public EmailCommand(AuthMe plugin, DataSource data) {
      super();
      this.plugin = plugin;
      this.data = data;
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
         if (args.length == 0) {
            player.sendMessage(this.m._("usage_email_add"));
            player.sendMessage(this.m._("usage_email_change"));
            player.sendMessage(this.m._("usage_email_recovery"));
            return true;
         } else {
            if (args[0].equalsIgnoreCase("add")) {
               if (args.length != 3) {
                  player.sendMessage(this.m._("usage_email_add"));
                  return true;
               }

               if (args[1].equals(args[2]) && PlayerCache.getInstance().isAuthenticated(name)) {
                  PlayerAuth auth = PlayerCache.getInstance().getAuth(name);
                  if (auth.getEmail() == null || !auth.getEmail().contains("your@email.com")) {
                     player.sendMessage("usage_email_change");
                     return true;
                  }

                  if (!args[1].contains("@")) {
                     player.sendMessage(this.m._("email_invalid"));
                     return true;
                  }

                  auth.setEmail(args[1]);
                  if (!this.data.updateEmail(auth)) {
                     player.sendMessage(this.m._("error"));
                     return true;
                  }

                  PlayerCache.getInstance().updatePlayer(auth);
                  player.sendMessage(this.m._("email_added"));
                  player.sendMessage(auth.getEmail());
               } else if (PlayerCache.getInstance().isAuthenticated(name)) {
                  player.sendMessage(this.m._("email_confirm"));
               } else if (!this.data.isAuthAvailable(name)) {
                  player.sendMessage(this.m._("login_msg"));
               } else {
                  player.sendMessage(this.m._("reg_email_msg"));
               }
            } else if (args[0].equalsIgnoreCase("change") && args.length == 3) {
               if (PlayerCache.getInstance().isAuthenticated(name)) {
                  PlayerAuth auth = PlayerCache.getInstance().getAuth(name);
                  if (auth.getEmail() == null || auth.getEmail().equals("your@email.com")) {
                     player.sendMessage(this.m._("usage_email_add"));
                     return true;
                  }

                  if (!args[1].equals(auth.getEmail())) {
                     player.sendMessage(this.m._("old_email_invalid"));
                     return true;
                  }

                  if (!args[2].contains("@")) {
                     player.sendMessage(this.m._("new_email_invalid"));
                     return true;
                  }

                  auth.setEmail(args[2]);
                  if (!this.data.updateEmail(auth)) {
                     player.sendMessage(this.m._("bad_database_email"));
                     return true;
                  }

                  PlayerCache.getInstance().updatePlayer(auth);
                  player.sendMessage(this.m._("email_changed"));
                  player.sendMessage(this.m._("email_defined") + auth.getEmail());
               } else if (PlayerCache.getInstance().isAuthenticated(name)) {
                  player.sendMessage(this.m._("email_confirm"));
               } else if (!this.data.isAuthAvailable(name)) {
                  player.sendMessage(this.m._("login_msg"));
               } else {
                  player.sendMessage(this.m._("reg_email_msg"));
               }
            }

            if (args[0].equalsIgnoreCase("recovery")) {
               if (args.length != 2) {
                  player.sendMessage(this.m._("usage_email_recovery"));
                  return true;
               }

               if (this.plugin.mail == null) {
                  player.sendMessage(this.m._("error"));
                  ConsoleLogger.info("Missed mail.jar in lib folder");
                  return true;
               }

               if (this.data.isAuthAvailable(name)) {
                  if (PlayerCache.getInstance().isAuthenticated(name)) {
                     player.sendMessage(this.m._("logged_in"));
                     return true;
                  }

                  try {
                     RandomString rand = new RandomString(Settings.getRecoveryPassLength);
                     String thePass = rand.nextString();
                     final String hashnew = PasswordSecurity.getHash(Settings.getPasswordHash, thePass, name);
                     final PlayerAuth auth = null;
                     if (PlayerCache.getInstance().isAuthenticated(name)) {
                        auth = PlayerCache.getInstance().getAuth(name);
                     } else {
                        if (!this.data.isAuthAvailable(name)) {
                           sender.sendMessage(this.m._("unknown_user"));
                           return true;
                        }

                        auth = this.data.getAuth(name);
                     }

                     if (Settings.getmailAccount.equals("") || Settings.getmailAccount.isEmpty()) {
                        player.sendMessage(this.m._("error"));
                        return true;
                     }

                     if (!args[1].equalsIgnoreCase(auth.getEmail())) {
                        player.sendMessage(this.m._("email_invalid"));
                        return true;
                     }

                     if (this.data instanceof Thread) {
                        auth.setHash(hashnew);
                        this.data.updatePassword(auth);
                     } else {
                        Bukkit.getScheduler().runTaskAsynchronously(this.plugin, new Runnable() {
                           public void run() {
                              auth.setHash(hashnew);
                              EmailCommand.this.data.updatePassword(auth);
                           }
                        });
                     }

                     this.plugin.mail.main(auth, thePass);
                     player.sendMessage(this.m._("email_send"));
                  } catch (NoSuchAlgorithmException ex) {
                     ConsoleLogger.showError(ex.getMessage());
                     sender.sendMessage(this.m._("error"));
                  } catch (NoClassDefFoundError ncdfe) {
                     ConsoleLogger.showError(ncdfe.getMessage());
                     sender.sendMessage(this.m._("error"));
                  }
               } else {
                  player.sendMessage(this.m._("reg_email_msg"));
               }
            }

            return true;
         }
      }
   }
}
