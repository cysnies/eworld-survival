package uk.org.whoami.authme.listener;

import com.Acrobot.ChestShop.Events.PreTransactionEvent;
import com.Acrobot.ChestShop.Events.PreTransactionEvent.TransactionOutcome;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import uk.org.whoami.authme.AuthMe;
import uk.org.whoami.authme.Utils;
import uk.org.whoami.authme.cache.auth.PlayerCache;
import uk.org.whoami.authme.datasource.DataSource;
import uk.org.whoami.authme.settings.Settings;

public class AuthMeChestShopListener implements Listener {
   public DataSource database;
   public AuthMe plugin;

   public AuthMeChestShopListener(DataSource database, AuthMe plugin) {
      super();
      this.database = database;
      this.plugin = plugin;
   }

   @EventHandler(
      priority = EventPriority.LOWEST
   )
   public void onPreTransaction(PreTransactionEvent event) {
      if (!event.isCancelled() && event.getClient() != null && event != null) {
         Player player = event.getClient();
         String name = player.getName().toLowerCase();
         if (!Utils.getInstance().isUnrestricted(player)) {
            if (!PlayerCache.getInstance().isAuthenticated(name)) {
               if (this.database.isAuthAvailable(name) || Settings.isForcedRegistrationEnabled) {
                  event.setCancelled(TransactionOutcome.OTHER);
               }
            }
         }
      }
   }
}
