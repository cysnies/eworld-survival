package com.onarandombox.MultiverseCore.utils;

import net.milkbowl.vault.economy.Economy;
import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.server.PluginDisableEvent;
import org.bukkit.event.server.PluginEnableEvent;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.RegisteredServiceProvider;

public class VaultHandler implements Listener {
   private Economy economy;

   public VaultHandler(Plugin plugin) {
      super();
      Bukkit.getPluginManager().registerEvents(new VaultListener(), plugin);
      this.setupVaultEconomy();
   }

   private boolean setupVaultEconomy() {
      if (Bukkit.getPluginManager().getPlugin("Vault") != null) {
         RegisteredServiceProvider<Economy> economyProvider = Bukkit.getServicesManager().getRegistration(Economy.class);
         if (economyProvider != null) {
            CoreLogging.fine("Vault economy enabled.");
            this.economy = (Economy)economyProvider.getProvider();
         } else {
            CoreLogging.finer("Vault economy not detected.");
            this.economy = null;
         }
      } else {
         CoreLogging.finer("Vault was not found.");
         this.economy = null;
      }

      return this.economy != null;
   }

   public Economy getEconomy() {
      return this.economy;
   }

   private class VaultListener implements Listener {
      private VaultListener() {
         super();
      }

      @EventHandler
      private void vaultEnabled(PluginEnableEvent event) {
         if (event.getPlugin() != null && event.getPlugin().getName().equals("Vault")) {
            VaultHandler.this.setupVaultEconomy();
         }

      }

      @EventHandler
      private void vaultDisabled(PluginDisableEvent event) {
         if (event.getPlugin() != null && event.getPlugin().getName().equals("Vault")) {
            CoreLogging.fine("Vault economy disabled");
            VaultHandler.this.setupVaultEconomy();
         }

      }
   }
}
