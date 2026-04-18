package com.earth2me.essentials.commands;

import com.earth2me.essentials.I18n;
import com.earth2me.essentials.User;
import org.bukkit.Server;
import org.bukkit.command.CommandSender;

public class Commandvanish extends EssentialsToggleCommand {
   public Commandvanish() {
      super("vanish", "essentials.vanish.others");
   }

   protected void run(Server server, CommandSender sender, String commandLabel, String[] args) throws Exception {
      this.toggleOtherPlayers(server, sender, args);
   }

   protected void run(Server server, User user, String commandLabel, String[] args) throws Exception {
      if (args.length == 1) {
         Boolean toggle = this.matchToggleArgument(args[0]);
         if (toggle == null && user.isAuthorized(this.othersPermission)) {
            this.toggleOtherPlayers(server, user.getBase(), args);
         } else {
            this.togglePlayer(user.getBase(), user, toggle);
         }
      } else if (args.length == 2 && user.isAuthorized(this.othersPermission)) {
         this.toggleOtherPlayers(server, user.getBase(), args);
      } else {
         this.togglePlayer(user.getBase(), user, (Boolean)null);
      }

   }

   void togglePlayer(CommandSender sender, User user, Boolean enabled) throws NotEnoughArgumentsException {
      if (enabled == null) {
         enabled = !user.isVanished();
      }

      user.setVanished(enabled);
      user.sendMessage(I18n._("vanish", user.getDisplayName(), enabled ? I18n._("enabled") : I18n._("disabled")));
      if (enabled) {
         user.sendMessage(I18n._("vanished"));
      }

      if (!sender.equals(user.getBase())) {
         sender.sendMessage(I18n._("vanish", user.getDisplayName(), enabled ? I18n._("enabled") : I18n._("disabled")));
      }

   }
}
