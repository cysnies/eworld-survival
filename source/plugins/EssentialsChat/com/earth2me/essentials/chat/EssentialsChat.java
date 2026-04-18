package com.earth2me.essentials.chat;

import com.earth2me.essentials.I18n;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import net.ess3.api.IEssentials;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;

public class EssentialsChat extends JavaPlugin {
   private static final Logger LOGGER = Logger.getLogger("Minecraft");

   public EssentialsChat() {
      super();
   }

   public void onEnable() {
      PluginManager pluginManager = this.getServer().getPluginManager();
      IEssentials ess = (IEssentials)pluginManager.getPlugin("Essentials");
      if (!this.getDescription().getVersion().equals(ess.getDescription().getVersion())) {
         LOGGER.log(Level.WARNING, I18n._("versionMismatchAll", new Object[0]));
      }

      if (!ess.isEnabled()) {
         this.setEnabled(false);
      } else {
         Map<AsyncPlayerChatEvent, ChatStore> chatStore = Collections.synchronizedMap(new HashMap());
         EssentialsChatPlayerListenerLowest playerListenerLowest = new EssentialsChatPlayerListenerLowest(this.getServer(), ess, chatStore);
         EssentialsChatPlayerListenerNormal playerListenerNormal = new EssentialsChatPlayerListenerNormal(this.getServer(), ess, chatStore);
         EssentialsChatPlayerListenerHighest playerListenerHighest = new EssentialsChatPlayerListenerHighest(this.getServer(), ess, chatStore);
         pluginManager.registerEvents(playerListenerLowest, this);
         pluginManager.registerEvents(playerListenerNormal, this);
         pluginManager.registerEvents(playerListenerHighest, this);
      }
   }
}
