package com.earth2me.essentials.commands;

import com.earth2me.essentials.I18n;
import com.earth2me.essentials.User;
import org.bukkit.Server;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.FoodLevelChangeEvent;

public class Commandfeed extends EssentialsCommand {
   public Commandfeed() {
      super("feed");
   }

   protected void run(Server server, User user, String commandLabel, String[] args) throws Exception {
      if (args.length > 0 && user.isAuthorized("essentials.feed.others")) {
         if (args[0].trim().length() < 2) {
            throw new PlayerNotFoundException();
         } else {
            if (!user.isAuthorized("essentials.feed.cooldown.bypass")) {
               user.healCooldown();
            }

            this.feedOtherPlayers(server, user.getBase(), args[0]);
         }
      } else {
         if (!user.isAuthorized("essentials.feed.cooldown.bypass")) {
            user.healCooldown();
         }

         try {
            this.feedPlayer(user.getBase(), user.getBase());
         } catch (QuietAbortException var6) {
         }

         user.sendMessage(I18n._("feed"));
      }
   }

   protected void run(Server server, CommandSender sender, String commandLabel, String[] args) throws Exception {
      if (args.length < 1) {
         throw new NotEnoughArgumentsException();
      } else {
         this.feedOtherPlayers(server, sender, args[0]);
      }
   }

   private void feedOtherPlayers(Server server, CommandSender sender, String name) throws PlayerNotFoundException {
      boolean skipHidden = sender instanceof Player && !this.ess.getUser(sender).isAuthorized("essentials.vanish.interact");
      boolean foundUser = false;

      for(Player matchPlayer : server.matchPlayer(name)) {
         User player = this.ess.getUser(matchPlayer);
         if (!skipHidden || !player.isHidden()) {
            foundUser = true;

            try {
               this.feedPlayer(sender, matchPlayer);
            } catch (QuietAbortException var11) {
            }
         }
      }

      if (!foundUser) {
         throw new PlayerNotFoundException();
      }
   }

   private void feedPlayer(CommandSender sender, Player player) throws QuietAbortException {
      int amount = 30;
      FoodLevelChangeEvent flce = new FoodLevelChangeEvent(player, 30);
      this.ess.getServer().getPluginManager().callEvent(flce);
      if (flce.isCancelled()) {
         throw new QuietAbortException();
      } else {
         player.setFoodLevel(flce.getFoodLevel() > 20 ? 20 : flce.getFoodLevel());
         player.setSaturation(10.0F);
         if (!sender.equals(player)) {
            sender.sendMessage(I18n._("feedOther", player.getDisplayName()));
         }

      }
   }
}
