package buscript.multiverse;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.server.PluginDisableEvent;
import org.bukkit.event.server.PluginEnableEvent;

public class VaultListener implements Listener {
   private Buscript buscript;

   public VaultListener(Buscript buscript) {
      super();
      this.buscript = buscript;
   }

   @EventHandler
   public void pluginEnable(PluginEnableEvent event) {
      if (event.getPlugin().getName().equals("Vault")) {
         this.buscript.setupVault();
      }

   }

   @EventHandler
   public void pluginDisable(PluginDisableEvent event) {
      if (event.getPlugin().getName().equals("Vault")) {
         this.buscript.disableVault();
      }

   }
}
