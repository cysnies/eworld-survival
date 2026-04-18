package uk.org.whoami.authme.commands;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import uk.org.whoami.authme.AuthMe;
import uk.org.whoami.authme.Utils;
import uk.org.whoami.authme.cache.auth.PlayerCache;
import uk.org.whoami.authme.settings.Messages;

public class PasspartuCommand implements CommandExecutor {
   private Utils utils = new Utils();
   public AuthMe plugin;
   private Messages m;

   public PasspartuCommand(AuthMe plugin) {
      super();
      this.plugin = plugin;
   }

   public boolean onCommand(CommandSender sender, Command cmnd, String label, String[] args) {
      if (!this.plugin.authmePermissible(sender, "authme.admin." + args[0].toLowerCase())) {
         sender.sendMessage(this.m._("no_perm"));
         return true;
      } else if (PlayerCache.getInstance().isAuthenticated(sender.getName().toLowerCase())) {
         return true;
      } else if (sender instanceof Player && args.length == 1) {
         if (this.utils.readToken(args[0])) {
            this.plugin.management.performLogin((Player)sender, "dontneed", true);
            return true;
         } else {
            sender.sendMessage("Time is expired or Token is Wrong!");
            return true;
         }
      } else {
         sender.sendMessage("usage: /passpartu token");
         return true;
      }
   }
}
