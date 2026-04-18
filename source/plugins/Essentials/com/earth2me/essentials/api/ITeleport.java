package com.earth2me.essentials.api;

import com.earth2me.essentials.Trade;
import net.ess3.api.IUser;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerTeleportEvent;

public interface ITeleport {
   void now(Location var1, boolean var2, PlayerTeleportEvent.TeleportCause var3) throws Exception;

   void now(Player var1, boolean var2, PlayerTeleportEvent.TeleportCause var3) throws Exception;

   /** @deprecated */
   @Deprecated
   void teleport(Location var1, Trade var2) throws Exception;

   void teleport(Location var1, Trade var2, PlayerTeleportEvent.TeleportCause var3) throws Exception;

   void teleport(Player var1, Trade var2, PlayerTeleportEvent.TeleportCause var3) throws Exception;

   void teleportPlayer(IUser var1, Location var2, Trade var3, PlayerTeleportEvent.TeleportCause var4) throws Exception;

   void teleportPlayer(IUser var1, Player var2, Trade var3, PlayerTeleportEvent.TeleportCause var4) throws Exception;

   void respawn(Trade var1, PlayerTeleportEvent.TeleportCause var2) throws Exception;

   void warp(IUser var1, String var2, Trade var3, PlayerTeleportEvent.TeleportCause var4) throws Exception;

   void back(Trade var1) throws Exception;

   void back() throws Exception;
}
