package com.khorn.terraincontrol.bukkit.util;

import com.khorn.terraincontrol.bukkit.TCPlugin;
import com.khorn.terraincontrol.util.MetricsHelper;
import java.io.IOException;
import org.bukkit.Bukkit;

public class BukkitMetricsHelper extends MetricsHelper {
   private final TCPlugin plugin;

   public BukkitMetricsHelper(TCPlugin plugin) {
      super();
      this.plugin = plugin;
      Bukkit.getScheduler().runTaskLater(plugin, new Runnable() {
         public void run() {
            BukkitMetricsHelper.this.startMetrics();
         }
      }, 100L);
   }

   private void startMetrics() {
      this.calculateBiomeModes(this.plugin.worlds.values());

      try {
         Metrics metrics = new Metrics(this.plugin);
         Metrics.Graph usedBiomeModesGraph = metrics.createGraph("Biome modes used");
         usedBiomeModesGraph.addPlotter(new Metrics.Plotter("Normal") {
            public int getValue() {
               return BukkitMetricsHelper.this.normalMode;
            }
         });
         usedBiomeModesGraph.addPlotter(new Metrics.Plotter("FromImage") {
            public int getValue() {
               return BukkitMetricsHelper.this.fromImageMode;
            }
         });
         usedBiomeModesGraph.addPlotter(new Metrics.Plotter("Default") {
            public int getValue() {
               return BukkitMetricsHelper.this.vanillaMode;
            }
         });
         usedBiomeModesGraph.addPlotter(new Metrics.Plotter("OldGenerator") {
            public int getValue() {
               return BukkitMetricsHelper.this.oldBiomeMode;
            }
         });
         usedBiomeModesGraph.addPlotter(new Metrics.Plotter("Custom / Unknown") {
            public int getValue() {
               return BukkitMetricsHelper.this.customMode;
            }
         });
         metrics.start();
      } catch (IOException var3) {
      }

   }
}
