package com.khorn.terraincontrol.customobjects.bo3;

import com.khorn.terraincontrol.LocalWorld;

public abstract class BO3Check extends BO3Function {
   public int x;
   public int y;
   public int z;

   public BO3Check() {
      super();
   }

   public abstract boolean preventsSpawn(LocalWorld var1, int var2, int var3, int var4);

   public abstract BO3Check rotate();
}
