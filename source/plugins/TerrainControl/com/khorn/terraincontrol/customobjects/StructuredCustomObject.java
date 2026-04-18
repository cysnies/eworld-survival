package com.khorn.terraincontrol.customobjects;

import java.util.Random;

public interface StructuredCustomObject extends CustomObject {
   boolean hasBranches();

   Branch[] getBranches(Rotation var1);

   CustomObjectCoordinate makeCustomObjectCoordinate(Random var1, int var2, int var3);

   int getMaxBranchDepth();

   CustomObjectCoordinate.SpawnHeight getSpawnHeight();
}
