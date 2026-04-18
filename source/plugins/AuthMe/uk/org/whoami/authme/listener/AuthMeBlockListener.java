package uk.org.whoami.authme.listener;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import uk.org.whoami.authme.AuthMe;
import uk.org.whoami.authme.Utils;
import uk.org.whoami.authme.cache.auth.PlayerCache;
import uk.org.whoami.authme.datasource.DataSource;
import uk.org.whoami.authme.settings.Settings;

public class AuthMeBlockListener implements Listener {
   private DataSource data;
   public AuthMe instance;

   public AuthMeBlockListener(DataSource data, AuthMe instance) {
      super();
      this.data = data;
      this.instance = instance;
   }

   @EventHandler
   public void onBlockPlace(BlockPlaceEvent event) {
      if (!event.isCancelled() && event.getPlayer() != null) {
         Player player = event.getPlayer();
         String name = player.getName().toLowerCase();
         if (!Utils.getInstance().isUnrestricted(player)) {
            if (!PlayerCache.getInstance().isAuthenticated(name)) {
               if (this.data.isAuthAvailable(name) || Settings.isForcedRegistrationEnabled) {
                  event.setCancelled(true);
               }
            }
         }
      }
   }

   @EventHandler
   public void onBlockBreak(BlockBreakEvent event) {
      if (!event.isCancelled() && event.getPlayer() != null) {
         Player player = event.getPlayer();
         String name = player.getName().toLowerCase();
         if (!Utils.getInstance().isUnrestricted(player)) {
            if (!PlayerCache.getInstance().isAuthenticated(player.getName().toLowerCase())) {
               if (this.data.isAuthAvailable(name) || Settings.isForcedRegistrationEnabled) {
                  event.setCancelled(true);
               }
            }
         }
      }
   }
}
