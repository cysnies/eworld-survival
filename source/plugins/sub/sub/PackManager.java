package sub;

import lib.config.ReloadConfigEvent;
import lib.util.UtilConfig;
import lib.util.UtilPer;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

public class PackManager implements Listener {
   private static final String PACK_PER = "per.sub.pack.vip";
   private String pn;

   public PackManager(Main main) {
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
   public void onPlayerJoin(PlayerJoinEvent e) {
      this.check(e.getPlayer());
      PackLimit packLimit = Main.getDao().getPackLimit(e.getPlayer().getName());
      int limit = packLimit.getLimit();

      for(int i = 1; i <= 5; ++i) {
         if (i <= limit) {
            UtilPer.add(e.getPlayer(), "per.pack.open." + i);
         } else {
            UtilPer.remove(e.getPlayer(), "per.pack.open." + i);
         }
      }

   }

   @EventHandler(
      priority = EventPriority.LOWEST
   )
   public void onPlayerQuit(PlayerQuitEvent e) {
      this.check(e.getPlayer());
   }

   private void check(Player p) {
      PackLimit packLimit = Main.getDao().getPackLimit(p.getName());
      if (packLimit == null) {
         packLimit = new PackLimit(p.getName(), 0);
      }

      if (UtilPer.hasPer(p, "per.sub.pack.vip")) {
         packLimit.setLimit(3);
      } else {
         packLimit.setLimit(1);
      }

      Main.getDao().addOrUpdatePackLimit(packLimit);
   }

   private void loadConfig(YamlConfiguration config) {
   }
}
