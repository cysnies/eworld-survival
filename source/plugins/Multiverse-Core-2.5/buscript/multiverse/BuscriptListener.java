package buscript.multiverse;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.server.PluginDisableEvent;

public class BuscriptListener implements Listener {
   private Buscript buscript;

   public BuscriptListener(Buscript buscript) {
      super();
      this.buscript = buscript;
   }

   @EventHandler
   public void pluginDisable(PluginDisableEvent event) {
      if (event.getPlugin().equals(this.buscript.getPlugin())) {
         this.buscript.runTasks = false;
         this.buscript.saveData();
      }

   }
}
