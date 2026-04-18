package com.onarandombox.MultiverseCore.listeners;

import com.onarandombox.MultiverseCore.MultiverseCore;
import com.onarandombox.MultiverseCore.api.MVWorldManager;
import com.onarandombox.MultiverseCore.api.MultiverseWorld;
import org.bukkit.event.Listener;

public abstract class MVChatListener implements Listener {
   private final MultiverseCore plugin;
   private final MVWorldManager worldManager;
   private final MVPlayerListener playerListener;

   public MVChatListener(MultiverseCore plugin, MVPlayerListener playerListener) {
      super();
      this.plugin = plugin;
      this.worldManager = plugin.getMVWorldManager();
      this.playerListener = playerListener;
   }

   public void playerChat(ChatEvent event) {
      if (!event.isCancelled()) {
         if (this.plugin.getMVConfig().getPrefixChat()) {
            String world = (String)this.playerListener.getPlayerWorld().get(event.getPlayer().getName());
            if (world == null) {
               world = event.getPlayer().getWorld().getName();
               this.playerListener.getPlayerWorld().put(event.getPlayer().getName(), world);
            }

            String prefix = "";
            if (!this.worldManager.isMVWorld(world)) {
               return;
            }

            MultiverseWorld mvworld = this.worldManager.getMVWorld(world);
            if (mvworld.isHidden()) {
               return;
            }

            prefix = mvworld.getColoredWorldString();
            String format = event.getFormat();
            event.setFormat("[" + prefix + "]" + format);
         }

      }
   }
}
