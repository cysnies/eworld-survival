package com.earth2me.essentials.commands;

import com.earth2me.essentials.I18n;
import com.earth2me.essentials.User;
import com.earth2me.essentials.utils.FormatUtil;
import java.util.logging.Level;
import org.bukkit.Server;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class Commandunbanip extends EssentialsCommand {
   public Commandunbanip() {
      super("unbanip");
   }

   public void run(Server server, CommandSender sender, String commandLabel, String[] args) throws Exception {
      if (args.length < 1) {
         throw new NotEnoughArgumentsException();
      } else {
         String ipAddress;
         if (FormatUtil.validIP(args[0])) {
            ipAddress = args[0];
         } else {
            try {
               User player = this.getPlayer(server, args, 0, true, true);
               ipAddress = player.getLastLoginAddress();
            } catch (PlayerNotFoundException var7) {
               ipAddress = args[0];
            }
         }

         if (ipAddress.isEmpty()) {
            throw new PlayerNotFoundException();
         } else {
            this.ess.getServer().unbanIP(ipAddress);
            String senderName = sender instanceof Player ? ((Player)sender).getDisplayName() : "Console";
            server.getLogger().log(Level.INFO, I18n._("playerUnbanIpAddress", senderName, ipAddress));
            this.ess.broadcastMessage("essentials.ban.notify", I18n._("playerUnbanIpAddress", senderName, ipAddress));
         }
      }
   }
}
