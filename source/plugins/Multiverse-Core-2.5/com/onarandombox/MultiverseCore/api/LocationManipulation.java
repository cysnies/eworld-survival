package com.onarandombox.MultiverseCore.api;

import org.bukkit.Location;
import org.bukkit.entity.Vehicle;
import org.bukkit.util.Vector;

public interface LocationManipulation {
   String locationToString(Location var1);

   Location getBlockLocation(Location var1);

   Location stringToLocation(String var1);

   String strCoords(Location var1);

   String strCoordsRaw(Location var1);

   String getDirection(Location var1);

   float getYaw(String var1);

   float getSpeed(Vector var1);

   Vector getTranslatedVector(Vector var1, String var2);

   Location getNextBlock(Vehicle var1);
}
