package com.earth2me.essentials.commands;

import com.earth2me.essentials.I18n;
import com.earth2me.essentials.User;
import org.bukkit.Server;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityRegainHealthEvent;
import org.bukkit.event.entity.EntityRegainHealthEvent.RegainReason;
import org.bukkit.potion.PotionEffect;

public class Commandheal extends EssentialsCommand {
   public Commandheal() {
      super("heal");
   }

   public void run(Server server, User user, String commandLabel, String[] args) throws Exception {
      if (args.length > 0 && user.isAuthorized("essentials.heal.others")) {
         if (args[0].trim().length() < 2) {
            throw new PlayerNotFoundException();
         } else {
            if (!user.isAuthorized("essentials.heal.cooldown.bypass")) {
               user.healCooldown();
            }

            this.healOtherPlayers(server, user.getBase(), args[0]);
         }
      } else {
         if (!user.isAuthorized("essentials.heal.cooldown.bypass")) {
            user.healCooldown();
         }

         this.healPlayer(user.getBase());
      }
   }

   public void run(Server server, CommandSender sender, String commandLabel, String[] args) throws Exception {
      if (args.length < 1) {
         throw new NotEnoughArgumentsException();
      } else {
         this.healOtherPlayers(server, sender, args[0]);
      }
   }

   private void healOtherPlayers(Server server, CommandSender sender, String name) throws Exception {
      boolean skipHidden = sender instanceof Player && !this.ess.getUser(sender).isAuthorized("essentials.vanish.interact");
      boolean foundUser = false;

      for(Player matchPlayer : server.matchPlayer(name)) {
         User player = this.ess.getUser(matchPlayer);
         if (!skipHidden || !player.isHidden()) {
            foundUser = true;

            try {
               this.healPlayer(matchPlayer);
               sender.sendMessage(I18n._("healOther", matchPlayer.getDisplayName()));
            } catch (QuietAbortException var11) {
            }
         }
      }

      if (!foundUser) {
         throw new PlayerNotFoundException();
      }
   }

   private void healPlayer(Player player) throws Exception {
      if (player.getHealth() == (double)0.0F) {
         throw new Exception(I18n._("healDead"));
      } else {
         double amount = player.getMaxHealth() - player.getHealth();
         EntityRegainHealthEvent erhe = new EntityRegainHealthEvent(player, amount, RegainReason.CUSTOM);
         this.ess.getServer().getPluginManager().callEvent(erhe);
         if (erhe.isCancelled()) {
            throw new QuietAbortException();
         } else {
            double newAmount = player.getHealth() + erhe.getAmount();
            if (newAmount > player.getMaxHealth()) {
               newAmount = player.getMaxHealth();
            }

            player.setHealth(newAmount);
            player.setFoodLevel(20);
            player.setFireTicks(0);
            player.sendMessage(I18n._("heal"));

            for(PotionEffect effect : player.getActivePotionEffects()) {
               player.removePotionEffect(effect.getType());
            }

         }
      }
   }
}
