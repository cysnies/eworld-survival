package com.onarandombox.MultiverseCore.listeners;

import com.onarandombox.MultiverseCore.MultiverseCore;
import java.util.logging.Level;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.AsyncPlayerChatEvent;

public class MVAsyncPlayerChatListener extends MVChatListener {
   public MVAsyncPlayerChatListener(MultiverseCore plugin, MVPlayerListener playerListener) {
      super(plugin, playerListener);
      plugin.log(Level.FINE, "Created AsyncPlayerChatEvent listener.");
   }

   @EventHandler
   public void playerChat(AsyncPlayerChatEvent event) {
      this.playerChat(new AsyncChatEvent(event));
   }
}
