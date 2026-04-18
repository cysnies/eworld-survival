package com.onarandombox.MultiverseCore.listeners;

import com.onarandombox.MultiverseCore.MultiverseCore;
import java.util.logging.Level;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.PlayerChatEvent;

public class MVPlayerChatListener extends MVChatListener {
   public MVPlayerChatListener(MultiverseCore plugin, MVPlayerListener playerListener) {
      super(plugin, playerListener);
      plugin.log(Level.FINE, "Registered PlayerChatEvent listener.");
   }

   @EventHandler
   public void playerChat(PlayerChatEvent event) {
      this.playerChat(new NormalChatEvent(event));
   }
}
