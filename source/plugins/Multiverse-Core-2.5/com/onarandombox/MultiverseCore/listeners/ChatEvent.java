package com.onarandombox.MultiverseCore.listeners;

import org.bukkit.entity.Player;

public interface ChatEvent {
   boolean isCancelled();

   String getFormat();

   void setFormat(String var1);

   Player getPlayer();
}
