package flag;

import landMain.LandManager;
import lib.config.ReloadConfigEvent;
import lib.util.UtilConfig;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;

public class BanClearHandler implements Listener {
   private static final String FLAG_BAN_CLEAR = "banClear";
   private String pn;
   private boolean use;
   private String per;
   private String tip;

   public BanClearHandler(LandManager landManager) {
      super();
      this.pn = landManager.getLandMain().getPn();
      this.loadConfig(UtilConfig.getConfig(landManager.getLandMain().getPn()));
      landManager.registerEvents(this);
      landManager.register("banClear", this.tip, this.use, false, true, this.per);
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
      this.use = config.getBoolean("banClear.use");
      this.per = config.getString("banClear.per");
      this.tip = config.getString("banClear.tip");
   }
}
