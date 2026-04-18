package com.earth2me.essentials.commands;

import com.earth2me.essentials.I18n;
import com.earth2me.essentials.User;
import org.bukkit.Server;
import org.bukkit.event.player.PlayerTeleportEvent.TeleportCause;

public class Commandtpohere extends EssentialsCommand {
   public Commandtpohere() {
      super("tpohere");
   }

   public void run(Server server, User user, String commandLabel, String[] args) throws Exception {
      if (args.length < 1) {
         throw new NotEnoughArgumentsException();
      } else {
         User player = this.getPlayer(server, user, args, 0);
         if (user.getWorld() != player.getWorld() && this.ess.getSettings().isWorldTeleportPermissions() && !user.isAuthorized("essentials.worlds." + user.getWorld().getName())) {
            throw new Exception(I18n._("noPerm", "essentials.worlds." + user.getWorld().getName()));
         } else {
            player.getTeleport().now(user.getBase(), false, TeleportCause.COMMAND);
            user.sendMessage(I18n._("teleporting"));
         }
      }
   }
}
