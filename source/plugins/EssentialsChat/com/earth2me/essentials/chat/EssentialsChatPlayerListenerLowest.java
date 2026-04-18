package com.earth2me.essentials.chat;

import com.earth2me.essentials.User;
import com.earth2me.essentials.utils.FormatUtil;
import java.util.Locale;
import java.util.Map;
import net.ess3.api.IEssentials;
import org.bukkit.Server;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.scoreboard.Team;

public class EssentialsChatPlayerListenerLowest extends EssentialsChatPlayer {
   public EssentialsChatPlayerListenerLowest(Server server, IEssentials ess, Map chatStorage) {
      super(server, ess, chatStorage);
   }

   @EventHandler(
      priority = EventPriority.LOWEST
   )
   public void onPlayerChat(AsyncPlayerChatEvent event) {
      if (!this.isAborted(event)) {
         User user = this.ess.getUser(event.getPlayer());
         if (user == null) {
            event.setCancelled(true);
         } else {
            ChatStore chatStore = new ChatStore(this.ess, user, this.getChatType(event.getMessage()));
            this.setChatStore(event, chatStore);
            event.setMessage(FormatUtil.formatMessage(user, "essentials.chat", event.getMessage()));
            String group = user.getGroup();
            String world = user.getWorld().getName();
            Team team = user.getScoreboard().getPlayerTeam(user.getBase());
            String format = this.ess.getSettings().getChatFormat(group);
            format = format.replace("{0}", group);
            format = format.replace("{1}", world);
            format = format.replace("{2}", world.substring(0, 1).toUpperCase(Locale.ENGLISH));
            format = format.replace("{3}", team == null ? "" : team.getPrefix());
            format = format.replace("{4}", team == null ? "" : team.getSuffix());
            format = format.replace("{5}", team == null ? "" : team.getDisplayName());
            synchronized(format) {
               event.setFormat(format);
            }
         }
      }
   }
}
