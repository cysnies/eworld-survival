package com.earth2me.essentials.commands;

import com.earth2me.essentials.User;
import org.bukkit.Server;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public abstract class EssentialsToggleCommand extends EssentialsCommand {
   String othersPermission;

   public EssentialsToggleCommand(String command, String othersPermission) {
      super(command);
      this.othersPermission = othersPermission;
   }

   protected Boolean matchToggleArgument(String arg) {
      if (!arg.equalsIgnoreCase("on") && !arg.startsWith("ena") && !arg.equalsIgnoreCase("1")) {
         return !arg.equalsIgnoreCase("off") && !arg.startsWith("dis") && !arg.equalsIgnoreCase("0") ? null : false;
      } else {
         return true;
      }
   }

   protected void toggleOtherPlayers(Server server, CommandSender sender, String[] args) throws PlayerNotFoundException, NotEnoughArgumentsException {
      if (args.length >= 1 && args[0].trim().length() >= 2) {
         boolean skipHidden = sender instanceof Player && !this.ess.getUser(sender).isAuthorized("essentials.vanish.interact");
         boolean foundUser = false;

         for(Player matchPlayer : server.matchPlayer(args[0])) {
            User player = this.ess.getUser(matchPlayer);
            if (!skipHidden || !player.isHidden()) {
               foundUser = true;
               if (args.length > 1) {
                  Boolean toggle = this.matchToggleArgument(args[1]);
                  if (toggle) {
                     this.togglePlayer(sender, player, true);
                  } else {
                     this.togglePlayer(sender, player, false);
                  }
               } else {
                  this.togglePlayer(sender, player, (Boolean)null);
               }
            }
         }

         if (!foundUser) {
            throw new PlayerNotFoundException();
         }
      } else {
         throw new PlayerNotFoundException();
      }
   }

   abstract void togglePlayer(CommandSender var1, User var2, Boolean var3) throws NotEnoughArgumentsException;
}
