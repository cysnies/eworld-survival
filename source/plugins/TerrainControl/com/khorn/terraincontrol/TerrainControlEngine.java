package com.khorn.terraincontrol;

import java.io.File;
import java.util.logging.Level;

public interface TerrainControlEngine {
   LocalWorld getWorld(String var1);

   void log(Level var1, String... var2);

   File getGlobalObjectsDirectory();

   boolean isValidBlockId(int var1);
}
