package com.onarandombox.MultiverseCore.api;

import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.util.Vector;

public interface MVDestination {
   String getIdentifier();

   boolean isThisType(JavaPlugin var1, String var2);

   Location getLocation(Entity var1);

   Vector getVelocity();

   void setDestination(JavaPlugin var1, String var2);

   boolean isValid();

   String getType();

   String getName();

   String toString();

   String getRequiredPermission();

   boolean useSafeTeleporter();
}
