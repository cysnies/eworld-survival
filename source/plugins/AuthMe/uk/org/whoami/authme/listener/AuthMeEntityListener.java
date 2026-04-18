package uk.org.whoami.authme.listener;

import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityInteractEvent;
import org.bukkit.event.entity.EntityRegainHealthEvent;
import org.bukkit.event.entity.EntityTargetEvent;
import org.bukkit.event.entity.FoodLevelChangeEvent;
import uk.org.whoami.authme.AuthMe;
import uk.org.whoami.authme.Utils;
import uk.org.whoami.authme.cache.auth.PlayerCache;
import uk.org.whoami.authme.datasource.DataSource;
import uk.org.whoami.authme.plugin.manager.CombatTagComunicator;
import uk.org.whoami.authme.settings.Settings;

public class AuthMeEntityListener implements Listener {
   private DataSource data;
   public AuthMe instance;

   public AuthMeEntityListener(DataSource data, AuthMe instance) {
      super();
      this.data = data;
      this.instance = instance;
   }

   @EventHandler
   public void onEntityDamage(EntityDamageEvent event) {
      if (!event.isCancelled()) {
         Entity entity = event.getEntity();
         if (entity instanceof Player) {
            if (!Utils.getInstance().isUnrestricted((Player)entity)) {
               Player player = (Player)entity;
               String name = player.getName().toLowerCase();
               if (!CombatTagComunicator.isNPC(player)) {
                  if (!PlayerCache.getInstance().isAuthenticated(name)) {
                     if (this.data.isAuthAvailable(name) || Settings.isForcedRegistrationEnabled) {
                        player.setFireTicks(0);
                        event.setCancelled(true);
                     }
                  }
               }
            }
         }
      }
   }

   @EventHandler
   public void onEntityTarget(EntityTargetEvent event) {
      if (!event.isCancelled()) {
         if (event.getTarget() != null) {
            Entity entity = event.getTarget();
            if (entity instanceof Player) {
               Player player = (Player)entity;
               String name = player.getName().toLowerCase();
               if (!PlayerCache.getInstance().isAuthenticated(name)) {
                  if (this.data.isAuthAvailable(name) || Settings.isForcedRegistrationEnabled) {
                     event.setTarget((Entity)null);
                     event.setCancelled(true);
                  }
               }
            }
         }
      }
   }

   @EventHandler
   public void onFoodLevelChange(FoodLevelChangeEvent event) {
      if (!event.isCancelled()) {
         Entity entity = event.getEntity();
         if (entity instanceof Player) {
            Player player = (Player)entity;
            String name = player.getName().toLowerCase();
            if (!PlayerCache.getInstance().isAuthenticated(name)) {
               if (this.data.isAuthAvailable(name) || Settings.isForcedRegistrationEnabled) {
                  event.setCancelled(true);
               }
            }
         }
      }
   }

   @EventHandler(
      priority = EventPriority.LOWEST
   )
   public void EntityRegainHealthEvent(EntityRegainHealthEvent event) {
      if (!event.isCancelled()) {
         Entity entity = event.getEntity();
         if (entity instanceof Player) {
            Player player = (Player)entity;
            String name = player.getName().toLowerCase();
            if (!PlayerCache.getInstance().isAuthenticated(name)) {
               if (this.data.isAuthAvailable(name) || Settings.isForcedRegistrationEnabled) {
                  event.setCancelled(true);
               }
            }
         }
      }
   }

   @EventHandler(
      priority = EventPriority.MONITOR
   )
   public void onEntityInteract(EntityInteractEvent event) {
      if (!event.isCancelled() && event != null) {
         if (event.getEntity() instanceof Player) {
            Player player = (Player)event.getEntity();
            String name = player.getName().toLowerCase();
            if (!Utils.getInstance().isUnrestricted(player) && !CombatTagComunicator.isNPC(player)) {
               if (!PlayerCache.getInstance().isAuthenticated(player.getName().toLowerCase())) {
                  if (this.data.isAuthAvailable(name) || Settings.isForcedRegistrationEnabled) {
                     event.setCancelled(true);
                  }
               }
            }
         }
      }
   }

   @EventHandler(
      priority = EventPriority.LOWEST
   )
   public void onLowestEntityInteract(EntityInteractEvent event) {
      if (!event.isCancelled() && event != null) {
         if (event.getEntity() instanceof Player) {
            Player player = (Player)event.getEntity();
            String name = player.getName().toLowerCase();
            if (!Utils.getInstance().isUnrestricted(player) && !CombatTagComunicator.isNPC(player)) {
               if (!PlayerCache.getInstance().isAuthenticated(player.getName().toLowerCase())) {
                  if (this.data.isAuthAvailable(name) || Settings.isForcedRegistrationEnabled) {
                     event.setCancelled(true);
                  }
               }
            }
         }
      }
   }
}
