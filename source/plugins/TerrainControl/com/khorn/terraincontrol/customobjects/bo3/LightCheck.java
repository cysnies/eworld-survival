package com.khorn.terraincontrol.customobjects.bo3;

import com.khorn.terraincontrol.LocalWorld;
import com.khorn.terraincontrol.exception.InvalidConfigException;
import java.util.List;

public class LightCheck extends BO3Check {
   public int minLightLevel;
   public int maxLightLevel;

   public LightCheck() {
      super();
   }

   public boolean preventsSpawn(LocalWorld world, int x, int y, int z) {
      int lightLevel = world.getLightLevel(x, y, z);
      return lightLevel < this.minLightLevel || lightLevel > this.maxLightLevel;
   }

   public void load(List args) throws InvalidConfigException {
      this.assureSize(5, args);
      this.x = this.readInt((String)args.get(0), -100, 100);
      this.y = this.readInt((String)args.get(1), -100, 100);
      this.z = this.readInt((String)args.get(2), -100, 100);
      this.minLightLevel = this.readInt((String)args.get(3), 0, 16);
      this.maxLightLevel = this.readInt((String)args.get(4), this.minLightLevel, 16);
   }

   public String makeString() {
      return "LightCheck(" + this.x + "," + this.y + "," + this.z + "," + this.minLightLevel + "," + this.maxLightLevel + ")";
   }

   public BO3Check rotate() {
      LightCheck rotatedCheck = new LightCheck();
      rotatedCheck.x = this.z;
      rotatedCheck.y = this.y;
      rotatedCheck.z = -this.x;
      rotatedCheck.minLightLevel = this.minLightLevel;
      rotatedCheck.maxLightLevel = this.maxLightLevel;
      return rotatedCheck;
   }
}
