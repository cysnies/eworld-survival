package com.onarandombox.MultiverseCore.listeners;

import com.fernferret.allpay.multiverse.AllPay;
import com.onarandombox.MultiverseCore.MultiverseCore;
import java.util.Arrays;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.server.PluginDisableEvent;
import org.bukkit.event.server.PluginEnableEvent;

public class MVPluginListener implements Listener {
   private MultiverseCore plugin;

   public MVPluginListener(MultiverseCore plugin) {
      super();
      this.plugin = plugin;
   }

   @EventHandler(
      priority = EventPriority.MONITOR
   )
   public void pluginEnable(PluginEnableEvent event) {
      if (this.plugin.getVaultHandler().getEconomy() == null) {
         if (Arrays.asList(AllPay.getValidEconPlugins()).contains(event.getPlugin().getDescription().getName())) {
            this.plugin.setBank(this.plugin.getBanker().loadEconPlugin());
         }

      }
   }

   @EventHandler(
      priority = EventPriority.MONITOR
   )
   public void pluginDisable(PluginDisableEvent event) {
   }
}
