package com.khorn.terraincontrol.forge.util;

import com.khorn.terraincontrol.LocalWorld;
import com.khorn.terraincontrol.TerrainControl;
import java.util.NoSuchElementException;
import net.minecraft.world.World;
import net.minecraft.world.WorldType;

public abstract class WorldHelper {
   public WorldHelper() {
      super();
   }

   public static int getNextWorldTypeID() {
      for(int i = 0; i < WorldType.field_77139_a.length; ++i) {
         if (WorldType.field_77139_a[i] == null) {
            return i;
         }
      }

      throw new NoSuchElementException("No more WorldType indexes available.");
   }

   public static LocalWorld toLocalWorld(World world) {
      String worldName = world.func_72860_G().func_75760_g();
      return TerrainControl.getWorld(worldName);
   }
}
