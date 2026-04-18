package com.earth2me.essentials.commands;

import com.earth2me.essentials.Console;
import com.earth2me.essentials.I18n;
import com.earth2me.essentials.User;
import com.earth2me.essentials.utils.FormatUtil;
import org.bukkit.Server;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class Commandhelpop extends EssentialsCommand {
   public Commandhelpop() {
      super("helpop");
   }

   public void run(Server server, User user, String commandLabel, String[] args) throws Exception {
      user.setDisplayNick();
      this.sendMessage(server, user.getBase(), user.getDisplayName(), args);
   }

   public void run(Server server, CommandSender sender, String commandLabel, String[] args) throws Exception {
      this.sendMessage(server, sender, "Console", args);
   }

   private void sendMessage(Server server, CommandSender sender, String from, String[] args) throws Exception {
      if (args.length < 1) {
         throw new NotEnoughArgumentsException();
      } else {
         String message = I18n._("helpOp", from, FormatUtil.stripFormat(getFinalArg(args, 0)));
         CommandSender cs = Console.getCommandSender(server);
         cs.sendMessage(message);

         for(Player onlinePlayer : server.getOnlinePlayers()) {
            User player = this.ess.getUser(onlinePlayer);
            if (player.isAuthorized("essentials.helpop.receive")) {
               player.sendMessage(message);
            }
         }

      }
   }
}
