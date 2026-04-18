package com.onarandombox.MultiverseCore.listeners;

import org.bukkit.entity.Player;
import org.bukkit.event.player.AsyncPlayerChatEvent;

public class AsyncChatEvent implements ChatEvent {
   private final AsyncPlayerChatEvent event;

   public AsyncChatEvent(AsyncPlayerChatEvent event) {
      super();
      this.event = event;
   }

   public boolean isCancelled() {
      return this.event.isCancelled();
   }

   public String getFormat() {
      return this.event.getFormat();
   }

   public void setFormat(String s) {
      this.event.setFormat(s);
   }

   public Player getPlayer() {
      return this.event.getPlayer();
   }
}
