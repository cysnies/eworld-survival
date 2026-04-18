package com.khorn.terraincontrol.bukkit;

import com.khorn.terraincontrol.configuration.BiomeConfig;
import net.minecraft.server.v1_6_R2.BiomeBase;

public class NullBiome extends BukkitBiome {
   private String name;

   public NullBiome(String _name) {
      super(BiomeBase.OCEAN);
      this.name = _name;
   }

   public boolean isCustom() {
      return true;
   }

   public int getId() {
      return 255;
   }

   public String getName() {
      return this.name;
   }

   public void setEffects(BiomeConfig config) {
   }
}
