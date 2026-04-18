package com.khorn.terraincontrol.customobjects;

import com.khorn.terraincontrol.LocalBiome;
import com.khorn.terraincontrol.LocalWorld;
import java.util.Map;
import java.util.Random;

public interface CustomObject {
   void onEnable(Map var1);

   String getName();

   boolean canSpawnAsTree();

   boolean canSpawnAsObject();

   boolean canRotateRandomly();

   boolean spawnForced(LocalWorld var1, Random var2, Rotation var3, int var4, int var5, int var6);

   boolean canSpawnAt(LocalWorld var1, Rotation var2, int var3, int var4, int var5);

   boolean spawnAsTree(LocalWorld var1, Random var2, int var3, int var4);

   boolean process(LocalWorld var1, Random var2, int var3, int var4);

   CustomObject applySettings(Map var1);

   boolean hasPreferenceToSpawnIn(LocalBiome var1);
}
