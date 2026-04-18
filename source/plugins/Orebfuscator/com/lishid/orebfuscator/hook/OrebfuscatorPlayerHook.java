package com.lishid.orebfuscator.hook;

import com.lishid.orebfuscator.Orebfuscator;
import com.lishid.orebfuscator.internal.IPlayerHook;
import com.lishid.orebfuscator.internal.InternalAccessor;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

public class OrebfuscatorPlayerHook implements Listener {
   private static IPlayerHook playerHook;

   public OrebfuscatorPlayerHook() {
      super();
   }

   private static IPlayerHook getPlayerHook() {
      if (playerHook == null) {
         playerHook = InternalAccessor.Instance.newPlayerHook();
      }

      return playerHook;
   }

   @EventHandler(
      priority = EventPriority.NORMAL
   )
   public void onPlayerJoin(PlayerJoinEvent event) {
      IPlayerHook playerHook = getPlayerHook();
      if (!Orebfuscator.usePL) {
         playerHook.HookNM(event.getPlayer());
      }

      playerHook.HookChunkQueue(event.getPlayer());
   }
}
