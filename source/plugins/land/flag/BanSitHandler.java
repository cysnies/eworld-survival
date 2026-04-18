package flag;

import landMain.LandManager;
import lib.config.ReloadConfigEvent;
import lib.util.UtilConfig;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;

public class BanSitHandler implements Listener {
   private static final String FLAG_BAN_SIT = "banSit";
   private String pn;
   private boolean use;
   private String per;
   private String tip;

   public BanSitHandler(LandManager landManager) {
      super();
      this.pn = landManager.getLandMain().getPn();
      this.loadConfig(UtilConfig.getConfig(this.pn));
      landManager.registerEvents(this);
      landManager.register("banSit", this.tip, this.use, true, false, this.per);
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
      this.use = config.getBoolean("banSit.use");
      this.per = config.getString("banSit.per");
      this.tip = config.getString("banSit.tip");
   }
}
