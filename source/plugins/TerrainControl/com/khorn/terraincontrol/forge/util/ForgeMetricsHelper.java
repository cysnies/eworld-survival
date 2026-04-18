package com.khorn.terraincontrol.forge.util;

import com.khorn.terraincontrol.LocalWorld;
import com.khorn.terraincontrol.forge.TCPlugin;
import com.khorn.terraincontrol.util.MetricsHelper;
import cpw.mods.fml.common.FMLCommonHandler;
import cpw.mods.fml.common.ModContainer;
import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;

public class ForgeMetricsHelper extends MetricsHelper {
   private final ModContainer container;
   private final TCPlugin plugin;

   public ForgeMetricsHelper(TCPlugin plugin) {
      super();
      this.plugin = plugin;
      this.container = FMLCommonHandler.instance().findContainerFor(plugin);
      this.startMetrics();
   }

   private void startMetrics() {
      Iterable<LocalWorld> loadedWorlds;
      if (this.plugin.getWorld() == null) {
         loadedWorlds = Collections.emptyList();
      } else {
         loadedWorlds = Arrays.asList(this.plugin.getWorld());
      }

      this.calculateBiomeModes(loadedWorlds);

      try {
         Metrics metrics = new Metrics(this.container.getModId(), this.container.getVersion());
         Metrics.Graph usedBiomeModesGraph = metrics.createGraph("Biome modes used");
         usedBiomeModesGraph.addPlotter(new Metrics.Plotter("Normal") {
            public int getValue() {
               return ForgeMetricsHelper.this.normalMode;
            }
         });
         usedBiomeModesGraph.addPlotter(new Metrics.Plotter("FromImage") {
            public int getValue() {
               return ForgeMetricsHelper.this.fromImageMode;
            }
         });
         usedBiomeModesGraph.addPlotter(new Metrics.Plotter("Default") {
            public int getValue() {
               return ForgeMetricsHelper.this.vanillaMode;
            }
         });
         usedBiomeModesGraph.addPlotter(new Metrics.Plotter("OldGenerator") {
            public int getValue() {
               return ForgeMetricsHelper.this.oldBiomeMode;
            }
         });
         usedBiomeModesGraph.addPlotter(new Metrics.Plotter("Custom / Unknown") {
            public int getValue() {
               return ForgeMetricsHelper.this.customMode;
            }
         });
         metrics.start();
      } catch (IOException var4) {
      }

   }
}
