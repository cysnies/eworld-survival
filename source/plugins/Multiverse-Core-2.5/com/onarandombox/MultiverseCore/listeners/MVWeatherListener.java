package com.onarandombox.MultiverseCore.listeners;

import com.onarandombox.MultiverseCore.MultiverseCore;
import com.onarandombox.MultiverseCore.api.MultiverseWorld;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.weather.ThunderChangeEvent;
import org.bukkit.event.weather.WeatherChangeEvent;

public class MVWeatherListener implements Listener {
   private MultiverseCore plugin;

   public MVWeatherListener(MultiverseCore plugin) {
      super();
      this.plugin = plugin;
   }

   @EventHandler
   public void weatherChange(WeatherChangeEvent event) {
      if (!event.isCancelled()) {
         MultiverseWorld world = this.plugin.getMVWorldManager().getMVWorld(event.getWorld().getName());
         if (world != null) {
            event.setCancelled(event.toWeatherState() && !world.isWeatherEnabled());
         }

      }
   }

   @EventHandler
   public void thunderChange(ThunderChangeEvent event) {
      if (!event.isCancelled()) {
         MultiverseWorld world = this.plugin.getMVWorldManager().getMVWorld(event.getWorld().getName());
         if (world != null) {
            event.setCancelled(event.toThunderState() && !world.isWeatherEnabled());
         }

      }
   }
}
