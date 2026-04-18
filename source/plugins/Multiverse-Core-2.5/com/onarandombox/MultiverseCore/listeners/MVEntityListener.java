package com.onarandombox.MultiverseCore.listeners;

import com.onarandombox.MultiverseCore.MultiverseCore;
import com.onarandombox.MultiverseCore.api.MVWorldManager;
import com.onarandombox.MultiverseCore.api.MultiverseWorld;
import java.util.logging.Level;
import org.bukkit.World;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.bukkit.event.entity.EntityRegainHealthEvent;
import org.bukkit.event.entity.FoodLevelChangeEvent;
import org.bukkit.event.entity.CreatureSpawnEvent.SpawnReason;
import org.bukkit.event.entity.EntityRegainHealthEvent.RegainReason;

public class MVEntityListener implements Listener {
   private MultiverseCore plugin;
   private MVWorldManager worldManager;

   public MVEntityListener(MultiverseCore plugin) {
      super();
      this.plugin = plugin;
      this.worldManager = plugin.getMVWorldManager();
   }

   @EventHandler
   public void foodLevelChange(FoodLevelChangeEvent event) {
      if (!event.isCancelled()) {
         if (event.getEntity() instanceof Player) {
            Player p = (Player)event.getEntity();
            MultiverseWorld w = this.plugin.getMVWorldManager().getMVWorld(p.getWorld().getName());
            if (w != null && !w.getHunger() && event.getFoodLevel() < ((Player)event.getEntity()).getFoodLevel()) {
               event.setCancelled(true);
            }
         }

      }
   }

   @EventHandler
   public void entityRegainHealth(EntityRegainHealthEvent event) {
      if (!event.isCancelled()) {
         EntityRegainHealthEvent.RegainReason reason = event.getRegainReason();
         MultiverseWorld world = this.worldManager.getMVWorld(event.getEntity().getLocation().getWorld());
         if (world != null && reason == RegainReason.REGEN && !world.getAutoHeal()) {
            event.setCancelled(true);
         }

      }
   }

   @EventHandler
   public void creatureSpawn(CreatureSpawnEvent event) {
      if (event.getSpawnReason() != SpawnReason.CUSTOM && event.getSpawnReason() != SpawnReason.SPAWNER_EGG && event.getSpawnReason() != SpawnReason.BREEDING) {
         World world = event.getEntity().getWorld();
         if (!event.isCancelled()) {
            if (this.worldManager.isMVWorld(world.getName())) {
               EntityType type = event.getEntityType();
               if (type != null && type.getName() != null) {
                  MultiverseWorld mvworld = this.worldManager.getMVWorld(world.getName());
                  event.setCancelled(this.plugin.getMVWorldManager().getTheWorldPurger().shouldWeKillThisCreature(mvworld, event.getEntity()));
               } else {
                  this.plugin.log(Level.FINER, "Found a null typed creature.");
               }
            }
         }
      }
   }
}
