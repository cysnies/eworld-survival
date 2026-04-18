package com.earth2me.essentials.chat;

import com.earth2me.essentials.ChargeException;
import com.earth2me.essentials.Trade;
import com.earth2me.essentials.User;
import java.util.Map;
import java.util.logging.Logger;
import net.ess3.api.IEssentials;
import org.bukkit.Server;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;

public abstract class EssentialsChatPlayer implements Listener {
   protected transient IEssentials ess;
   protected static final Logger logger = Logger.getLogger("Minecraft");
   protected final transient Server server;
   protected final transient Map chatStorage;

   public EssentialsChatPlayer(Server server, IEssentials ess, Map chatStorage) {
      super();
      this.ess = ess;
      this.server = server;
      this.chatStorage = chatStorage;
   }

   public void onPlayerChat(AsyncPlayerChatEvent event) {
   }

   public boolean isAborted(AsyncPlayerChatEvent event) {
      return event.isCancelled();
   }

   public String getChatType(String message) {
      switch (message.charAt(0)) {
         case '!':
            return "shout";
         case '?':
            return "question";
         default:
            return "";
      }
   }

   public ChatStore getChatStore(AsyncPlayerChatEvent event) {
      return (ChatStore)this.chatStorage.get(event);
   }

   public void setChatStore(AsyncPlayerChatEvent event, ChatStore chatStore) {
      this.chatStorage.put(event, chatStore);
   }

   public ChatStore delChatStore(AsyncPlayerChatEvent event) {
      return (ChatStore)this.chatStorage.remove(event);
   }

   protected void charge(User user, Trade charge) throws ChargeException {
      charge.charge(user);
   }

   protected boolean charge(AsyncPlayerChatEvent event, ChatStore chatStore) {
      try {
         this.charge(chatStore.getUser(), chatStore.getCharge());
         return true;
      } catch (ChargeException e) {
         this.ess.showError(chatStore.getUser().getBase(), e, chatStore.getLongType());
         event.setCancelled(true);
         return false;
      }
   }
}
