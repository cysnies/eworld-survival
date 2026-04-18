package com.khorn.terraincontrol.bukkit.util;

import com.khorn.terraincontrol.LocalWorld;
import com.khorn.terraincontrol.TerrainControl;
import com.khorn.terraincontrol.bukkit.TCPlugin;
import net.minecraft.server.v1_6_R2.World;

public abstract class WorldHelper {
   public WorldHelper() {
      super();
   }

   public static LocalWorld toLocalWorld(World world) {
      return (LocalWorld)((TCPlugin)TerrainControl.getEngine()).worlds.get(world.getWorld().getUID());
   }
}
