package com.earth2me.essentials.commands;

import com.earth2me.essentials.I18n;
import com.earth2me.essentials.User;
import java.util.logging.Level;
import org.bukkit.OfflinePlayer;
import org.bukkit.Server;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class Commandunban extends EssentialsCommand {
   public Commandunban() {
      super("unban");
   }

   public void run(Server server, CommandSender sender, String commandLabel, String[] args) throws Exception {
      if (args.length < 1) {
         throw new NotEnoughArgumentsException();
      } else {
         String name;
         try {
            User user = this.getPlayer(server, args, 0, true, true);
            name = user.getName();
            user.setBanned(false);
            user.setBanTimeout(0L);
         } catch (NoSuchFieldException e) {
            OfflinePlayer player = server.getOfflinePlayer(args[0]);
            name = player.getName();
            if (!player.isBanned()) {
               throw new Exception(I18n._("playerNotFound"), e);
            }

            player.setBanned(false);
         }

         String senderName = sender instanceof Player ? ((Player)sender).getDisplayName() : "Console";
         server.getLogger().log(Level.INFO, I18n._("playerUnbanned", senderName, name));
         this.ess.broadcastMessage("essentials.ban.notify", I18n._("playerUnbanned", senderName, name));
      }
   }
}
