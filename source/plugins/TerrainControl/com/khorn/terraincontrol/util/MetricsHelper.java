package com.khorn.terraincontrol.util;

import com.khorn.terraincontrol.LocalWorld;
import com.khorn.terraincontrol.biomegenerators.BiomeGenerator;
import com.khorn.terraincontrol.biomegenerators.FromImageBiomeGenerator;
import com.khorn.terraincontrol.biomegenerators.NormalBiomeGenerator;
import com.khorn.terraincontrol.biomegenerators.OldBiomeGenerator;
import com.khorn.terraincontrol.biomegenerators.VanillaBiomeGenerator;
import com.khorn.terraincontrol.configuration.WorldConfig;

public abstract class MetricsHelper {
   protected int normalMode = 0;
   protected int fromImageMode = 0;
   protected int vanillaMode = 0;
   protected int oldBiomeMode = 0;
   protected int customMode = 0;

   public MetricsHelper() {
      super();
   }

   protected void calculateBiomeModes(Iterable worlds) {
      for(LocalWorld world : worlds) {
         WorldConfig config = world.getSettings();
         if (config != null) {
            Class<? extends BiomeGenerator> clazz = config.biomeMode;
            if (clazz.equals(NormalBiomeGenerator.class)) {
               ++this.normalMode;
            } else if (clazz.equals(FromImageBiomeGenerator.class)) {
               ++this.fromImageMode;
            } else if (clazz.equals(VanillaBiomeGenerator.class)) {
               ++this.vanillaMode;
            } else if (clazz.equals(OldBiomeGenerator.class)) {
               ++this.oldBiomeMode;
            } else {
               ++this.customMode;
            }
         }
      }

   }
}
