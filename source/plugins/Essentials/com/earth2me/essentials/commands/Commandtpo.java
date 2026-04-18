package com.earth2me.essentials.commands;

import com.earth2me.essentials.I18n;
import com.earth2me.essentials.User;
import org.bukkit.Server;
import org.bukkit.event.player.PlayerTeleportEvent.TeleportCause;

public class Commandtpo extends EssentialsCommand {
   public Commandtpo() {
      super("tpo");
   }

   public void run(Server server, User user, String commandLabel, String[] args) throws Exception {
      switch (args.length) {
         case 0:
            throw new NotEnoughArgumentsException();
         case 1:
            User player = this.getPlayer(server, user, args, 0);
            if (user.getWorld() != player.getWorld() && this.ess.getSettings().isWorldTeleportPermissions() && !user.isAuthorized("essentials.worlds." + player.getWorld().getName())) {
               throw new Exception(I18n._("noPerm", "essentials.worlds." + player.getWorld().getName()));
            }

            user.sendMessage(I18n._("teleporting"));
            user.getTeleport().now(player.getBase(), false, TeleportCause.COMMAND);
            break;
         default:
            if (!user.isAuthorized("essentials.tp.others")) {
               throw new Exception(I18n._("noPerm", "essentials.tp.others"));
            }

            user.sendMessage(I18n._("teleporting"));
            User target = this.getPlayer(server, user, args, 0);
            User toPlayer = this.getPlayer(server, user, args, 1);
            if (target.getWorld() != toPlayer.getWorld() && this.ess.getSettings().isWorldTeleportPermissions() && !user.isAuthorized("essentials.worlds." + toPlayer.getWorld().getName())) {
               throw new Exception(I18n._("noPerm", "essentials.worlds." + toPlayer.getWorld().getName()));
            }

            target.getTeleport().now(toPlayer.getBase(), false, TeleportCause.COMMAND);
            target.sendMessage(I18n._("teleportAtoB", user.getDisplayName(), toPlayer.getDisplayName()));
      }

   }
}
