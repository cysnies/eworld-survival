package com.earth2me.essentials.commands;

import com.earth2me.essentials.I18n;
import com.earth2me.essentials.User;
import org.bukkit.Location;
import org.bukkit.Server;
import org.bukkit.World;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class Commandnear extends EssentialsCommand {
   public Commandnear() {
      super("near");
   }

   protected void run(Server server, User user, String commandLabel, String[] args) throws Exception {
      long maxRadius = (long)this.ess.getSettings().getChatRadius();
      if (maxRadius == 0L) {
         maxRadius = 200L;
      }

      long radius = maxRadius;
      User otherUser = null;
      if (args.length > 0) {
         try {
            radius = Long.parseLong(args[0]);
         } catch (NumberFormatException var14) {
            try {
               otherUser = this.getPlayer(server, user, args, 0);
            } catch (Exception var13) {
            }
         }

         if (args.length > 1 && otherUser != null) {
            try {
               radius = Long.parseLong(args[1]);
            } catch (NumberFormatException var12) {
            }
         }
      }

      if (radius > maxRadius && !user.isAuthorized("essentials.near.maxexempt")) {
         user.sendMessage(I18n._("radiusTooBig", maxRadius));
         radius = maxRadius;
      }

      if (otherUser == null || !user.isAuthorized("essentials.near.others")) {
         otherUser = user;
      }

      user.sendMessage(I18n._("nearbyPlayers", this.getLocal(server, otherUser, radius)));
   }

   protected void run(Server server, CommandSender sender, String commandLabel, String[] args) throws Exception {
      if (args.length == 0) {
         throw new NotEnoughArgumentsException();
      } else {
         User otherUser = this.getPlayer(server, args, 0, true, false);
         long radius = 200L;
         if (args.length > 1) {
            try {
               radius = Long.parseLong(args[1]);
            } catch (NumberFormatException var9) {
            }
         }

         sender.sendMessage(I18n._("nearbyPlayers", this.getLocal(server, otherUser, radius)));
      }
   }

   private String getLocal(Server server, User user, long radius) {
      Location loc = user.getLocation();
      World world = loc.getWorld();
      StringBuilder output = new StringBuilder();
      long radiusSquared = radius * radius;
      boolean showHidden = user.isAuthorized("essentials.vanish.interact");

      for(Player onlinePlayer : server.getOnlinePlayers()) {
         User player = this.ess.getUser(onlinePlayer);
         if (!player.equals(user) && (!player.isHidden() || showHidden)) {
            Location playerLoc = player.getLocation();
            if (playerLoc.getWorld() == world) {
               long delta = (long)playerLoc.distanceSquared(loc);
               if (delta < radiusSquared) {
                  if (output.length() > 0) {
                     output.append(", ");
                  }

                  output.append(player.getDisplayName()).append("§f(§4").append((long)Math.sqrt((double)delta)).append("m§f)");
               }
            }
         }
      }

      return output.length() > 1 ? output.toString() : I18n._("none");
   }
}
