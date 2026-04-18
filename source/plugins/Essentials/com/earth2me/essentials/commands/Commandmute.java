package com.earth2me.essentials.commands;

import com.earth2me.essentials.I18n;
import com.earth2me.essentials.OfflinePlayer;
import com.earth2me.essentials.User;
import com.earth2me.essentials.utils.DateUtil;
import org.bukkit.Server;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class Commandmute extends EssentialsCommand {
   public Commandmute() {
      super("mute");
   }

   public void run(Server server, CommandSender sender, String commandLabel, String[] args) throws Exception {
      boolean nomatch = false;
      if (args.length < 1) {
         throw new NotEnoughArgumentsException();
      } else {
         User user;
         try {
            user = this.getPlayer(server, args, 0, true, true);
         } catch (NoSuchFieldException var11) {
            nomatch = true;
            user = this.ess.getUser(new OfflinePlayer(args[0], this.ess));
         }

         if (!user.isOnline()) {
            if (sender instanceof Player && !this.ess.getUser(sender).isAuthorized("essentials.mute.offline")) {
               throw new Exception(I18n._("muteExempt"));
            }
         } else if (user.isAuthorized("essentials.mute.exempt") && sender instanceof Player) {
            throw new Exception(I18n._("muteExempt"));
         }

         long muteTimestamp = 0L;
         if (args.length > 1) {
            String time = getFinalArg(args, 1);
            muteTimestamp = DateUtil.parseDateDiff(time, true);
            user.setMuted(true);
         } else {
            user.setMuted(!user.getMuted());
         }

         user.setMuteTimeout(muteTimestamp);
         boolean muted = user.getMuted();
         String muteTime = DateUtil.formatDateDiff(muteTimestamp);
         if (nomatch) {
            sender.sendMessage(I18n._("userUnknown", user.getName()));
         }

         if (muted) {
            if (muteTimestamp > 0L) {
               sender.sendMessage(I18n._("mutedPlayerFor", user.getDisplayName(), muteTime));
               user.sendMessage(I18n._("playerMutedFor", muteTime));
            } else {
               sender.sendMessage(I18n._("mutedPlayer", user.getDisplayName()));
               user.sendMessage(I18n._("playerMuted"));
            }

            this.ess.broadcastMessage("essentials.mute.notify", I18n._("muteNotify", sender.getName(), user.getName(), muteTime));
         } else {
            sender.sendMessage(I18n._("unmutedPlayer", user.getDisplayName()));
            user.sendMessage(I18n._("playerUnmuted"));
         }

      }
   }
}
