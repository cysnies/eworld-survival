package com.earth2me.essentials.commands;

import com.earth2me.essentials.I18n;
import com.earth2me.essentials.User;
import java.util.HashSet;
import org.bukkit.Server;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.LightningStrike;
import org.bukkit.entity.Player;

public class Commandlightning extends EssentialsCommand {
   public Commandlightning() {
      super("lightning");
   }

   public void run(Server server, CommandSender sender, String commandLabel, String[] args) throws Exception {
      User user = null;
      if (sender instanceof Player) {
         user = this.ess.getUser((Player)sender);
         if (args.length < 1 || user != null && !user.isAuthorized("essentials.lightning.others")) {
            user.getWorld().strikeLightning(user.getTargetBlock((HashSet)null, 600).getLocation());
            return;
         }
      }

      int power = 5;
      if (args.length > 1) {
         try {
            power = Integer.parseInt(args[1]);
         } catch (NumberFormatException var12) {
         }
      }

      if (args[0].trim().length() < 2) {
         throw new PlayerNotFoundException();
      } else {
         for(Player matchPlayer : server.matchPlayer(args[0])) {
            User matchUser = this.ess.getUser(matchPlayer);
            sender.sendMessage(I18n._("lightningUse", matchPlayer.getDisplayName()));
            LightningStrike strike = matchPlayer.getWorld().strikeLightningEffect(matchPlayer.getLocation());
            if (!matchUser.isGodModeEnabled()) {
               matchPlayer.damage((double)power, strike);
            }

            if (this.ess.getSettings().warnOnSmite()) {
               matchUser.sendMessage(I18n._("lightningSmited"));
            }
         }

      }
   }
}
