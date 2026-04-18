package com.earth2me.essentials.commands;

import com.earth2me.essentials.I18n;
import com.earth2me.essentials.Trade;
import com.earth2me.essentials.User;
import org.bukkit.Location;
import org.bukkit.Server;
import org.bukkit.event.player.PlayerTeleportEvent.TeleportCause;

public class Commandtpaccept extends EssentialsCommand {
   public Commandtpaccept() {
      super("tpaccept");
   }

   public void run(Server server, User user, String commandLabel, String[] args) throws Exception {
      User requester = this.ess.getUser(user.getTeleportRequest());
      if (requester != null && requester.isOnline()) {
         if (!user.isTpRequestHere() || (requester.isAuthorized("essentials.tpahere") || requester.isAuthorized("essentials.tpaall")) && (user.getWorld() == requester.getWorld() || !this.ess.getSettings().isWorldTeleportPermissions() || user.isAuthorized("essentials.worlds." + user.getWorld().getName()))) {
            if (user.isTpRequestHere() || requester.isAuthorized("essentials.tpa") && (user.getWorld() == requester.getWorld() || !this.ess.getSettings().isWorldTeleportPermissions() || user.isAuthorized("essentials.worlds." + requester.getWorld().getName()))) {
               if (args.length > 0 && !requester.getName().contains(args[0])) {
                  throw new Exception(I18n._("noPendingRequest"));
               } else {
                  long timeout = this.ess.getSettings().getTpaAcceptCancellation();
                  if (timeout != 0L && (System.currentTimeMillis() - user.getTeleportRequestTime()) / 1000L > timeout) {
                     user.requestTeleport((User)null, false);
                     throw new Exception(I18n._("requestTimedOut"));
                  } else {
                     Trade charge = new Trade(this.getName(), this.ess);
                     user.sendMessage(I18n._("requestAccepted"));
                     requester.sendMessage(I18n._("requestAcceptedFrom", user.getDisplayName()));

                     try {
                        if (user.isTpRequestHere()) {
                           requester.getTeleport().teleportPlayer(user, (Location)user.getTpRequestLocation(), charge, TeleportCause.COMMAND);
                        } else {
                           requester.getTeleport().teleport(user.getBase(), charge, TeleportCause.COMMAND);
                        }
                     } catch (Exception ex) {
                        user.sendMessage(I18n._("pendingTeleportCancelled"));
                        this.ess.showError(requester.getBase(), ex, commandLabel);
                     }

                     user.requestTeleport((User)null, false);
                     throw new NoChargeException();
                  }
               }
            } else {
               throw new Exception(I18n._("noPendingRequest"));
            }
         } else {
            throw new Exception(I18n._("noPendingRequest"));
         }
      } else {
         throw new Exception(I18n._("noPendingRequest"));
      }
   }
}
