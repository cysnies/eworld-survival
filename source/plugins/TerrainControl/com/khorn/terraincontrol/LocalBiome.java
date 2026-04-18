package com.khorn.terraincontrol;

import com.khorn.terraincontrol.configuration.BiomeConfig;

public interface LocalBiome {
   boolean isCustom();

   void setEffects(BiomeConfig var1);

   String getName();

   int getId();

   int getCustomId();

   float getTemperature();

   float getWetness();

   float getSurfaceHeight();

   float getSurfaceVolatility();

   byte getSurfaceBlock();

   byte getGroundBlock();
}
