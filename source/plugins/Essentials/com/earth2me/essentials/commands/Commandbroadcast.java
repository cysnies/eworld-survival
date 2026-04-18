package com.earth2me.essentials.commands;

import com.earth2me.essentials.I18n;
import com.earth2me.essentials.User;
import com.earth2me.essentials.utils.FormatUtil;
import org.bukkit.Server;
import org.bukkit.command.CommandSender;

public class Commandbroadcast extends EssentialsCommand {
   public Commandbroadcast() {
      super("broadcast");
   }

   public void run(Server server, User user, String commandLabel, String[] args) throws Exception {
      this.sendBroadcast(user.getDisplayName(), args);
   }

   public void run(Server server, CommandSender sender, String commandLabel, String[] args) throws Exception {
      this.sendBroadcast(sender.getName(), args);
   }

   private void sendBroadcast(String name, String[] args) throws NotEnoughArgumentsException {
      if (args.length < 1) {
         throw new NotEnoughArgumentsException();
      } else {
         this.ess.broadcastMessage(I18n._("broadcast", FormatUtil.replaceFormat(getFinalArg(args, 0)).replace("\\n", "\n"), name));
      }
   }
}
