package uk.org.whoami.authme.commands;

import java.security.NoSuchAlgorithmException;
import me.muizers.Notifications.Notification;
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
import uk.org.whoami.authme.settings.Messages;
import uk.org.whoami.authme.settings.Settings;

public class ChangePasswordCommand implements CommandExecutor {
   private Messages m = Messages.getInstance();
   private DataSource database;
   public AuthMe plugin;

   public ChangePasswordCommand(DataSource database, AuthMe plugin) {
      super();
      this.database = database;
      this.plugin = plugin;
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
         } else if (args.length != 2) {
            player.sendMessage(this.m._("usage_changepassword"));
            return true;
         } else {
            try {
               String hashnew = PasswordSecurity.getHash(Settings.getPasswordHash, args[1], name);
               if (PasswordSecurity.comparePasswordWithHash(args[0], PlayerCache.getInstance().getAuth(name).getHash(), name)) {
                  PlayerAuth auth = PlayerCache.getInstance().getAuth(name);
                  auth.setHash(hashnew);
                  if (!this.database.updatePassword(auth)) {
                     player.sendMessage(this.m._("error"));
                     return true;
                  }

                  PlayerCache.getInstance().updatePlayer(auth);
                  player.sendMessage(this.m._("pwd_changed"));
                  ConsoleLogger.info(player.getName() + " changed his password");
                  if (this.plugin.notifications != null) {
                     this.plugin.notifications.showNotification(new Notification("[AuthMe] " + player.getName() + " change his password!"));
                  }
               } else {
                  player.sendMessage(this.m._("wrong_pwd"));
               }
            } catch (NoSuchAlgorithmException ex) {
               ConsoleLogger.showError(ex.getMessage());
               sender.sendMessage(this.m._("error"));
            }

            return true;
         }
      }
   }
}
