package com.onarandombox.MultiverseCore.listeners;

import com.onarandombox.MultiverseCore.MultiverseCore;
import com.onarandombox.MultiverseCore.api.MVWorldManager;
import com.onarandombox.MultiverseCore.api.MultiverseWorld;
import com.onarandombox.MultiverseCore.event.MVRespawnEvent;
import com.onarandombox.MultiverseCore.utils.CoreLogging;
import com.onarandombox.MultiverseCore.utils.PermissionTools;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerChangedWorldEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerPortalEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.event.player.PlayerTeleportEvent;

public class MVPlayerListener implements Listener {
   private final MultiverseCore plugin;
   private final MVWorldManager worldManager;
   private final PermissionTools pt;
   private final Map playerWorld = new ConcurrentHashMap();

   public MVPlayerListener(MultiverseCore plugin) {
      super();
      this.plugin = plugin;
      this.worldManager = plugin.getMVWorldManager();
      this.pt = new PermissionTools(plugin);
   }

   public Map getPlayerWorld() {
      return this.playerWorld;
   }

   @EventHandler(
      priority = EventPriority.LOW
   )
   public void playerRespawn(PlayerRespawnEvent event) {
      World world = event.getPlayer().getWorld();
      MultiverseWorld mvWorld = this.worldManager.getMVWorld(world.getName());
      if (mvWorld != null) {
         if (mvWorld.getBedRespawn() && event.isBedSpawn()) {
            this.plugin.log(Level.FINE, "Spawning " + event.getPlayer().getName() + " at their bed");
         } else {
            MultiverseWorld respawnWorld = null;
            if (this.worldManager.isMVWorld(mvWorld.getRespawnToWorld())) {
               respawnWorld = this.worldManager.getMVWorld(mvWorld.getRespawnToWorld());
            }

            if (respawnWorld != null) {
               world = respawnWorld.getCBWorld();
            }

            Location respawnLocation = this.getMostAccurateRespawnLocation(world);
            MVRespawnEvent respawnEvent = new MVRespawnEvent(respawnLocation, event.getPlayer(), "compatability");
            this.plugin.getServer().getPluginManager().callEvent(respawnEvent);
            event.setRespawnLocation(respawnEvent.getPlayersRespawnLocation());
         }
      }
   }

   private Location getMostAccurateRespawnLocation(World w) {
      MultiverseWorld mvw = this.worldManager.getMVWorld(w.getName());
      return mvw != null ? mvw.getSpawnLocation() : w.getSpawnLocation();
   }

   @EventHandler
   public void playerJoin(PlayerJoinEvent event) {
      Player p = event.getPlayer();
      if (!p.hasPlayedBefore()) {
         this.plugin.log(Level.FINER, "Player joined for the FIRST time!");
         if (this.plugin.getMVConfig().getFirstSpawnOverride()) {
            this.plugin.log(Level.FINE, "Moving NEW player to(firstspawnoverride): " + this.worldManager.getFirstSpawnWorld().getSpawnLocation());
            this.sendPlayerToDefaultWorld(p);
         }

      } else {
         this.plugin.log(Level.FINER, "Player joined AGAIN!");
         if (this.plugin.getMVConfig().getEnforceAccess() && !this.plugin.getMVPerms().hasPermission(p, "multiverse.access." + p.getWorld().getName(), false)) {
            p.sendMessage("[MV] - Sorry you can't be in this world anymore!");
            this.sendPlayerToDefaultWorld(p);
         }

         this.handleGameMode(event.getPlayer(), event.getPlayer().getWorld());
         this.playerWorld.put(p.getName(), p.getWorld().getName());
      }
   }

   @EventHandler(
      priority = EventPriority.MONITOR
   )
   public void playerChangedWorld(PlayerChangedWorldEvent event) {
      this.handleGameMode(event.getPlayer(), event.getPlayer().getWorld());
      this.playerWorld.put(event.getPlayer().getName(), event.getPlayer().getWorld().getName());
   }

   @EventHandler
   public void playerQuit(PlayerQuitEvent event) {
      this.plugin.removePlayerSession(event.getPlayer());
   }

   @EventHandler(
      priority = EventPriority.HIGHEST
   )
   public void playerTeleport(PlayerTeleportEvent event) {
      this.plugin.log(Level.FINER, "Got teleport event for player '" + event.getPlayer().getName() + "' with cause '" + event.getCause() + "'");
      if (!event.isCancelled()) {
         Player teleportee = event.getPlayer();
         CommandSender teleporter = null;
         String teleporterName = MultiverseCore.getPlayerTeleporter(teleportee.getName());
         if (teleporterName != null) {
            if (teleporterName.equals("CONSOLE")) {
               this.plugin.log(Level.FINER, "We know the teleporter is the console! Magical!");
               teleporter = this.plugin.getServer().getConsoleSender();
            } else {
               teleporter = this.plugin.getServer().getPlayer(teleporterName);
            }
         }

         this.plugin.log(Level.FINER, "Inferred sender '" + teleporter + "' from name '" + teleporterName + "', fetched from name '" + teleportee.getName() + "'");
         MultiverseWorld fromWorld = this.worldManager.getMVWorld(event.getFrom().getWorld().getName());
         MultiverseWorld toWorld = this.worldManager.getMVWorld(event.getTo().getWorld().getName());
         if (fromWorld != null && toWorld != null) {
            if (event.getFrom().getWorld().equals(event.getTo().getWorld())) {
               this.plugin.log(Level.FINER, "Player '" + teleportee.getName() + "' is teleporting to the same world.");
               this.stateSuccess(teleportee.getName(), toWorld.getAlias());
            } else {
               event.setCancelled(!this.pt.playerHasMoneyToEnter(fromWorld, toWorld, teleporter, teleportee, true));
               if (event.isCancelled() && teleporter != null) {
                  this.plugin.log(Level.FINE, "Player '" + teleportee.getName() + "' was DENIED ACCESS to '" + toWorld.getAlias() + "' because '" + teleporter.getName() + "' don't have the FUNDS required to enter it.");
               } else {
                  if (this.plugin.getMVConfig().getEnforceAccess()) {
                     event.setCancelled(!this.pt.playerCanGoFromTo(fromWorld, toWorld, teleporter, teleportee));
                     if (event.isCancelled() && teleporter != null) {
                        this.plugin.log(Level.FINE, "Player '" + teleportee.getName() + "' was DENIED ACCESS to '" + toWorld.getAlias() + "' because '" + teleporter.getName() + "' don't have: multiverse.access." + event.getTo().getWorld().getName());
                        return;
                     }
                  } else {
                     this.plugin.log(Level.FINE, "Player '" + teleportee.getName() + "' was allowed to go to '" + toWorld.getAlias() + "' because enforceaccess is off.");
                  }

                  if (toWorld.getPlayerLimit() > -1 && toWorld.getCBWorld().getPlayers().size() >= toWorld.getPlayerLimit() && !this.pt.playerCanBypassPlayerLimit(toWorld, teleporter, teleportee)) {
                     this.plugin.log(Level.FINE, "Player '" + teleportee.getName() + "' was DENIED ACCESS to '" + toWorld.getAlias() + "' because the world is full and '" + teleporter.getName() + "' doesn't have: mv.bypass.playerlimit." + event.getTo().getWorld().getName());
                     event.setCancelled(true);
                  } else {
                     this.stateSuccess(teleportee.getName(), toWorld.getAlias());
                  }
               }
            }
         }
      }
   }

   private void stateSuccess(String playerName, String worldName) {
      this.plugin.log(Level.FINE, "MV-Core is allowing Player '" + playerName + "' to go to '" + worldName + "'.");
   }

   @EventHandler(
      priority = EventPriority.LOWEST
   )
   public void playerPortalCheck(PlayerPortalEvent event) {
      if (!event.isCancelled() && event.getFrom() != null) {
         if (event.getFrom().getWorld().getBlockAt(event.getFrom()).getType() != Material.PORTAL) {
            Location newloc = this.plugin.getSafeTTeleporter().findPortalBlockNextTo(event.getFrom());
            if (newloc != null) {
               event.setFrom(newloc);
            }
         }

         if (event.getTo() != null) {
            ;
         }
      }
   }

   @EventHandler(
      priority = EventPriority.HIGH
   )
   public void playerPortal(PlayerPortalEvent event) {
      if (!event.isCancelled() && event.getFrom() != null) {
         if (event.getTo() != null) {
            MultiverseWorld fromWorld = this.worldManager.getMVWorld(event.getFrom().getWorld().getName());
            MultiverseWorld toWorld = this.worldManager.getMVWorld(event.getTo().getWorld().getName());
            if (event.getFrom().getWorld().equals(event.getTo().getWorld())) {
               this.plugin.log(Level.FINER, "Player '" + event.getPlayer().getName() + "' is portaling to the same world.");
            } else {
               event.setCancelled(!this.pt.playerHasMoneyToEnter(fromWorld, toWorld, event.getPlayer(), event.getPlayer(), true));
               if (event.isCancelled()) {
                  this.plugin.log(Level.FINE, "Player '" + event.getPlayer().getName() + "' was DENIED ACCESS to '" + event.getTo().getWorld().getName() + "' because they don't have the FUNDS required to enter.");
               } else {
                  if (this.plugin.getMVConfig().getEnforceAccess()) {
                     event.setCancelled(!this.pt.playerCanGoFromTo(fromWorld, toWorld, event.getPlayer(), event.getPlayer()));
                     if (event.isCancelled()) {
                        this.plugin.log(Level.FINE, "Player '" + event.getPlayer().getName() + "' was DENIED ACCESS to '" + event.getTo().getWorld().getName() + "' because they don't have: multiverse.access." + event.getTo().getWorld().getName());
                     }
                  } else {
                     this.plugin.log(Level.FINE, "Player '" + event.getPlayer().getName() + "' was allowed to go to '" + event.getTo().getWorld().getName() + "' because enforceaccess is off.");
                  }

                  if (!this.plugin.getMVConfig().isUsingDefaultPortalSearch()) {
                     event.getPortalTravelAgent().setSearchRadius(this.plugin.getMVConfig().getPortalSearchRadius());
                  }

               }
            }
         }
      }
   }

   private void sendPlayerToDefaultWorld(final Player player) {
      this.plugin.getServer().getScheduler().scheduleSyncDelayedTask(this.plugin, new Runnable() {
         public void run() {
            player.teleport(MVPlayerListener.this.plugin.getMVWorldManager().getFirstSpawnWorld().getSpawnLocation());
         }
      }, 1L);
   }

   private void handleGameMode(Player player, World world) {
      MultiverseWorld mvWorld = this.worldManager.getMVWorld(world.getName());
      if (mvWorld != null) {
         this.handleGameMode(player, mvWorld);
      } else {
         this.plugin.log(Level.FINER, "Not handling gamemode for world '" + world.getName() + "' not managed by Multiverse.");
      }

   }

   public void handleGameMode(final Player player, final MultiverseWorld world) {
      if (!this.pt.playerCanIgnoreGameModeRestriction(world, player)) {
         this.plugin.getServer().getScheduler().scheduleSyncDelayedTask(this.plugin, new Runnable() {
            public void run() {
               if (player.getWorld() == world.getCBWorld()) {
                  CoreLogging.fine("Handling gamemode for player: %s, Changing to %s", player.getName(), world.getGameMode().toString());
                  CoreLogging.finest("From World: %s", player.getWorld());
                  CoreLogging.finest("To World: %s", world);
                  player.setGameMode(world.getGameMode());
               } else {
                  CoreLogging.fine("The gamemode was NOT changed for player '%s' because he is now in world '%s' instead of world '%s'", player.getName(), player.getWorld().getName(), world.getName());
               }

            }
         }, 1L);
      } else {
         this.plugin.log(Level.FINE, "Player: " + player.getName() + " is IMMUNE to gamemode changes!");
      }

   }
}
