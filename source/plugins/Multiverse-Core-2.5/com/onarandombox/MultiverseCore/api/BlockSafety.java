package com.onarandombox.MultiverseCore.api;

import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Minecart;
import org.bukkit.entity.Vehicle;

public interface BlockSafety {
   boolean isBlockAboveAir(Location var1);

   boolean playerCanSpawnHereSafely(World var1, double var2, double var4, double var6);

   boolean playerCanSpawnHereSafely(Location var1);

   Location getSafeBedSpawn(Location var1);

   Location getTopBlock(Location var1);

   Location getBottomBlock(Location var1);

   boolean isEntitiyOnTrack(Location var1);

   boolean canSpawnCartSafely(Minecart var1);

   boolean canSpawnVehicleSafely(Vehicle var1);
}
