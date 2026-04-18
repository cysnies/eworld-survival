package uk.org.whoami.authme.commands;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import uk.org.whoami.authme.AuthMe;
import uk.org.whoami.authme.settings.Messages;

public class LoginCommand implements CommandExecutor {
   private AuthMe plugin;
   private Messages m = Messages.getInstance();

   public LoginCommand(AuthMe plugin) {
      super();
      this.plugin = plugin;
   }

   public boolean onCommand(CommandSender sender, Command cmnd, String label, String[] args) {
      if (!(sender instanceof Player)) {
         return true;
      } else {
         Player player = (Player)sender;
         if (args.length == 0) {
            player.sendMessage(this.m._("usage_log"));
            return true;
         } else if (!this.plugin.authmePermissible(player, "authme." + label.toLowerCase())) {
            player.sendMessage(this.m._("no_perm"));
            return true;
         } else {
            this.plugin.management.performLogin(player, args[0], false);
            return true;
         }
      }
   }
}
