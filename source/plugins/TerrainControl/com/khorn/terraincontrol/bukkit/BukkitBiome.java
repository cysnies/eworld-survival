package com.khorn.terraincontrol.bukkit;

import com.khorn.terraincontrol.DefaultBiome;
import com.khorn.terraincontrol.LocalBiome;
import com.khorn.terraincontrol.configuration.BiomeConfig;
import net.minecraft.server.v1_6_R2.BiomeBase;

public class BukkitBiome implements LocalBiome {
   private BiomeBase biomeBase;
   private boolean isCustom;
   private int customID;
   private float temperature;
   private float humidity;

   public BukkitBiome(BiomeBase biome) {
      super();
      this.biomeBase = biome;
      if (DefaultBiome.getBiome(biome.id) == null) {
         this.isCustom = true;
      }

      this.customID = this.biomeBase.id;
      this.temperature = biome.temperature;
      this.humidity = biome.humidity;
   }

   public boolean isCustom() {
      return this.isCustom;
   }

   public int getCustomId() {
      return this.customID;
   }

   public void setCustomID(int id) {
      this.customID = id;
   }

   public BiomeBase getHandle() {
      return this.biomeBase;
   }

   public void setEffects(BiomeConfig config) {
      ((CustomBiome)this.biomeBase).setEffects(config);
   }

   public String getName() {
      return this.biomeBase.y;
   }

   public int getId() {
      return this.biomeBase.id;
   }

   public float getTemperature() {
      return this.temperature;
   }

   public float getWetness() {
      return this.humidity;
   }

   public float getSurfaceHeight() {
      return this.biomeBase.D;
   }

   public float getSurfaceVolatility() {
      return this.biomeBase.E;
   }

   public byte getSurfaceBlock() {
      return this.biomeBase.A;
   }

   public byte getGroundBlock() {
      return this.biomeBase.B;
   }
}
