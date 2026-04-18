package com.khorn.terraincontrol.customobjects.bo3;

import com.khorn.terraincontrol.configuration.ConfigFunction;

public abstract class BO3Function extends ConfigFunction {
   public BO3Function() {
      super();
   }

   public Class getHolderType() {
      return BO3Config.class;
   }

   public abstract BO3Function rotate();
}
