package com.lishid.orebfuscator.listeners;

import com.lishid.orebfuscator.OrebfuscatorConfig;
import com.lishid.orebfuscator.hithack.BlockHitManager;
import com.lishid.orebfuscator.obfuscation.BlockUpdate;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockDamageEvent;
import org.bukkit.event.block.BlockPhysicsEvent;
import org.bukkit.event.block.BlockPistonExtendEvent;
import org.bukkit.event.block.BlockPistonRetractEvent;

public class OrebfuscatorBlockListener implements Listener {
   public OrebfuscatorBlockListener() {
      super();
   }

   @EventHandler(
      priority = EventPriority.MONITOR
   )
   public void onBlockBreak(BlockBreakEvent event) {
      if (!event.isCancelled()) {
         BlockUpdate.Update(event.getBlock());
         BlockHitManager.breakBlock(event.getPlayer(), event.getBlock());
      }
   }

   @EventHandler(
      priority = EventPriority.MONITOR
   )
   public void onBlockDamage(BlockDamageEvent event) {
      if (!event.isCancelled() && OrebfuscatorConfig.UpdateOnDamage) {
         if (BlockUpdate.needsUpdate(event.getBlock())) {
            if (BlockHitManager.hitBlock(event.getPlayer(), event.getBlock())) {
               BlockUpdate.Update(event.getBlock());
            }
         }
      }
   }

   @EventHandler(
      priority = EventPriority.MONITOR
   )
   public void onBlockPhysics(BlockPhysicsEvent event) {
      if (!event.isCancelled()) {
         if (event.getBlock().getType() == Material.SAND || event.getBlock().getType() == Material.GRAVEL) {
            if (this.applyphysics(event.getBlock())) {
               BlockUpdate.Update(event.getBlock());
            }
         }
      }
   }

   @EventHandler(
      priority = EventPriority.MONITOR
   )
   public void onBlockPistonExtend(BlockPistonExtendEvent event) {
      if (!event.isCancelled()) {
         for(Block b : event.getBlocks()) {
            BlockUpdate.Update(b);
         }

      }
   }

   @EventHandler(
      priority = EventPriority.MONITOR
   )
   public void onBlockPistonRetract(BlockPistonRetractEvent event) {
      if (!event.isCancelled()) {
         BlockUpdate.Update(event.getBlock());
      }
   }

   private boolean applyphysics(Block block) {
      int blockID = block.getRelative(0, -1, 0).getTypeId();
      int air = Material.AIR.getId();
      int fire = Material.FIRE.getId();
      int water = Material.WATER.getId();
      int water2 = Material.STATIONARY_WATER.getId();
      int lava = Material.LAVA.getId();
      int lava2 = Material.STATIONARY_LAVA.getId();
      return blockID == air || blockID == fire || blockID == water || blockID == water2 || blockID == lava || blockID == lava2;
   }
}
