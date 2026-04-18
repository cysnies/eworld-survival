package com.onarandombox.MultiverseCore.listeners;

import com.onarandombox.MultiverseCore.MultiverseCore;
import com.onarandombox.MultiverseCore.api.MultiverseWorld;
import com.onarandombox.MultiverseCore.enums.AllowedPortalType;
import org.bukkit.Material;
import org.bukkit.PortalType;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityCreatePortalEvent;
import org.bukkit.event.world.PortalCreateEvent;

public class MVPortalListener implements Listener {
   private MultiverseCore plugin;

   public MVPortalListener(MultiverseCore core) {
      super();
      this.plugin = core;
   }

   @EventHandler
   public void entityPortalCreate(EntityCreatePortalEvent event) {
      if (!event.isCancelled() && event.getBlocks().size() != 0) {
         MultiverseWorld world = this.plugin.getMVWorldManager().getMVWorld(((BlockState)event.getBlocks().get(0)).getWorld());
         if (cancelPortalEvent(world, event.getPortalType())) {
            event.setCancelled(true);
         }

      }
   }

   @EventHandler
   public void portalForm(PortalCreateEvent event) {
      if (!event.isCancelled() && event.getBlocks().size() != 0) {
         for(Block b : event.getBlocks()) {
            if (b.getType() == Material.PORTAL) {
               MultiverseWorld world = this.plugin.getMVWorldManager().getMVWorld(b.getWorld());
               if (cancelPortalEvent(world, PortalType.NETHER)) {
                  event.setCancelled(true);
                  return;
               }
            }
         }

         MultiverseWorld world = this.plugin.getMVWorldManager().getMVWorld(((Block)event.getBlocks().get(0)).getWorld());
         if (cancelPortalEvent(world, PortalType.ENDER)) {
            event.setCancelled(true);
         }

      }
   }

   private static boolean cancelPortalEvent(MultiverseWorld world, PortalType type) {
      if (world.getAllowedPortals() == AllowedPortalType.NONE) {
         return true;
      } else {
         return world.getAllowedPortals() != AllowedPortalType.ALL && type != world.getAllowedPortals().getActualPortalType();
      }
   }
}
