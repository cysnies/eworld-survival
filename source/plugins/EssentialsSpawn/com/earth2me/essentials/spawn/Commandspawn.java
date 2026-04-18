package com.earth2me.essentials.spawn;

import com.earth2me.essentials.I18n;
import com.earth2me.essentials.Trade;
import com.earth2me.essentials.User;
import com.earth2me.essentials.commands.EssentialsCommand;
import com.earth2me.essentials.commands.NoChargeException;
import com.earth2me.essentials.commands.NotEnoughArgumentsException;
import org.bukkit.Location;
import org.bukkit.Server;
import org.bukkit.command.CommandSender;
import org.bukkit.event.player.PlayerTeleportEvent.TeleportCause;

public class Commandspawn extends EssentialsCommand {
   public Commandspawn() {
      super("spawn");
   }

   public void run(Server server, User user, String commandLabel, String[] args) throws Exception {
      Trade charge = new Trade(this.getName(), this.ess);
      charge.isAffordableFor(user);
      if (args.length > 0 && user.isAuthorized("essentials.spawn.others")) {
         User otherUser = this.getPlayer(server, user, args, 0);
         this.respawn(user, otherUser, charge);
         if (!otherUser.equals(user)) {
            otherUser.sendMessage(I18n._("teleportAtoB", new Object[]{user.getDisplayName(), "spawn"}));
            user.sendMessage(I18n._("teleporting", new Object[0]));
         }
      } else {
         this.respawn(user, user, charge);
      }

      throw new NoChargeException();
   }

   protected void run(Server server, CommandSender sender, String commandLabel, String[] args) throws Exception {
      if (args.length < 1) {
         throw new NotEnoughArgumentsException();
      } else {
         User user = this.getPlayer(server, args, 0, true, false);
         this.respawn((User)null, user, (Trade)null);
         user.sendMessage(I18n._("teleportAtoB", new Object[]{"Console", "spawn"}));
         sender.sendMessage(I18n._("teleporting", new Object[0]));
      }
   }

   private void respawn(User teleportOwner, User teleportee, Trade charge) throws Exception {
      SpawnStorage spawns = (SpawnStorage)this.module;
      Location spawn = spawns.getSpawn(teleportee.getGroup());
      if (teleportOwner == null) {
         teleportee.getTeleport().now(spawn, false, TeleportCause.COMMAND);
      } else {
         teleportOwner.getTeleport().teleportPlayer(teleportee, spawn, charge, TeleportCause.COMMAND);
      }

   }
}
