package sub;

import java.util.List;
import lib.config.ReloadConfigEvent;
import lib.util.UtilConfig;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;

public class LandManager implements Listener {
   private String pn;
   private List levelList;

   public LandManager(Main main) {
      super();
      this.pn = main.getPn();
      this.loadConfig(UtilConfig.getConfig(this.pn));
      main.getPm().registerEvents(this, main);
   }

   @EventHandler(
      priority = EventPriority.LOW
   )
   public void onReloadConfig(ReloadConfigEvent e) {
      if (e.getCallPlugin().equals(this.pn)) {
         this.loadConfig(e.getConfig());
      }

   }

   @EventHandler(
      priority = EventPriority.LOWEST
   )
   public void onPlayerQuit(PlayerQuitEvent e) {
      this.checkLandLimit(e.getPlayer());
   }

   private void checkLandLimit(Player p) {
      int result = 0;

      for(int i : this.levelList) {
         if (level.Main.getLevelManager().hasLevel(p.getName(), i)) {
            ++result;
         }
      }

      LandLimit ll = Main.getDao().getLandLimit(p.getName());
      if (ll == null) {
         ll = new LandLimit(p.getName(), 0);
      }

      ll.setLimit(result);
      Main.getDao().addOrUpdateLandLimit(ll);
   }

   private void loadConfig(YamlConfiguration config) {
      this.levelList = config.getIntegerList("land.level");
   }
}
