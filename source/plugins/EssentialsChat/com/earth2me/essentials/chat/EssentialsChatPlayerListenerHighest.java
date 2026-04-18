package com.earth2me.essentials.chat;

import java.util.Map;
import net.ess3.api.IEssentials;
import org.bukkit.Server;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.player.AsyncPlayerChatEvent;

public class EssentialsChatPlayerListenerHighest extends EssentialsChatPlayer {
   public EssentialsChatPlayerListenerHighest(Server server, IEssentials ess, Map chatStorage) {
      super(server, ess, chatStorage);
   }

   @EventHandler(
      priority = EventPriority.HIGHEST
   )
   public void onPlayerChat(AsyncPlayerChatEvent event) {
      ChatStore chatStore = this.delChatStore(event);
      if (!this.isAborted(event) && chatStore != null) {
         this.charge(event, chatStore);
      }
   }
}
