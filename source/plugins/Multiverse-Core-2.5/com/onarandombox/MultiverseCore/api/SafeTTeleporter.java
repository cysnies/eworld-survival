package com.onarandombox.MultiverseCore.api;

import com.onarandombox.MultiverseCore.enums.TeleportResult;
import org.bukkit.Location;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Entity;

public interface SafeTTeleporter {
   Location getSafeLocation(Location var1);

   Location getSafeLocation(Location var1, int var2, int var3);

   TeleportResult safelyTeleport(CommandSender var1, Entity var2, MVDestination var3);

   TeleportResult safelyTeleport(CommandSender var1, Entity var2, Location var3, boolean var4);

   Location getSafeLocation(Entity var1, MVDestination var2);

   Location findPortalBlockNextTo(Location var1);
}
