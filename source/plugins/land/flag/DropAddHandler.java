package flag;

import landMain.LandManager;
import lib.config.ReloadConfigEvent;
import lib.util.UtilConfig;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;

public class DropAddHandler implements Listener {
   private static final String FLAG_DROP_ADD = "dropAdd";
   private String pn;
   private boolean use;
   private String per;
   private String tip;

   public DropAddHandler(LandManager landManager) {
      super();
      this.pn = landManager.getLandMain().getPn();
      this.loadConfig(UtilConfig.getConfig(landManager.getLandMain().getPn()));
      landManager.registerEvents(this);
      landManager.register("dropAdd", this.tip, this.use, false, true, this.per);
   }

   @EventHandler(
      priority = EventPriority.LOW
   )
   public void onReloadConfig(ReloadConfigEvent e) {
      if (e.getCallPlugin().equals(this.pn)) {
         this.loadConfig(e.getConfig());
      }

   }

   private void loadConfig(YamlConfiguration config) {
      this.use = config.getBoolean("dropAdd.use");
      this.per = config.getString("dropAdd.per");
      this.tip = config.getString("dropAdd.tip");
   }
}
