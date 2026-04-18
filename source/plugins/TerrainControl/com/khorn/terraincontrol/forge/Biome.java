package com.khorn.terraincontrol.forge;

import com.khorn.terraincontrol.LocalBiome;
import com.khorn.terraincontrol.configuration.BiomeConfig;
import net.minecraft.world.biome.BiomeGenBase;

public class Biome implements LocalBiome {
   private BiomeGenCustom biomeBase;

   public Biome(BiomeGenCustom biome) {
      super();
      this.biomeBase = biome;
   }

   public boolean isCustom() {
      return true;
   }

   public int getCustomId() {
      return this.getId();
   }

   public void setEffects(BiomeConfig config) {
      this.biomeBase.setEffects(config);
   }

   public String getName() {
      return this.biomeBase.field_76791_y;
   }

   public BiomeGenBase getHandle() {
      return this.biomeBase;
   }

   public int getId() {
      return this.biomeBase.field_76756_M;
   }

   public float getTemperature() {
      return this.biomeBase.field_76750_F;
   }

   public float getWetness() {
      return this.biomeBase.field_76751_G;
   }

   public float getSurfaceHeight() {
      return this.biomeBase.field_76748_D;
   }

   public float getSurfaceVolatility() {
      return this.biomeBase.field_76749_E;
   }

   public byte getSurfaceBlock() {
      return this.biomeBase.field_76752_A;
   }

   public byte getGroundBlock() {
      return this.biomeBase.field_76753_B;
   }
}
