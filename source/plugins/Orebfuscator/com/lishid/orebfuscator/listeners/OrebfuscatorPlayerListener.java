package com.lishid.orebfuscator.listeners;

import com.lishid.orebfuscator.Orebfuscator;
import com.lishid.orebfuscator.OrebfuscatorConfig;
import com.lishid.orebfuscator.hithack.BlockHitManager;
import com.lishid.orebfuscator.obfuscation.BlockUpdate;
import com.lishid.orebfuscator.obfuscation.ProximityHider;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.Event.Result;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerChangedWorldEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerQuitEvent;

public class OrebfuscatorPlayerListener implements Listener {
   public OrebfuscatorPlayerListener() {
      super();
   }

   @EventHandler(
      priority = EventPriority.NORMAL
   )
   public void onPlayerJoin(PlayerJoinEvent event) {
      Player player = event.getPlayer();
      if (OrebfuscatorConfig.LoginNotification) {
         if (OrebfuscatorConfig.playerBypassOp(player)) {
            Orebfuscator.message(player, "Orebfuscator bypassed because you are OP.");
         } else if (OrebfuscatorConfig.playerBypassPerms(player)) {
            Orebfuscator.message(player, "Orebfuscator bypassed because you have permission.");
         }
      }

   }

   @EventHandler(
      priority = EventPriority.NORMAL
   )
   public void onPlayerQuit(PlayerQuitEvent event) {
      BlockHitManager.clearHistory(event.getPlayer());
      if (OrebfuscatorConfig.UseProximityHider) {
         synchronized(ProximityHider.proximityHiderTracker) {
            ProximityHider.proximityHiderTracker.remove(event.getPlayer());
         }
      }

   }

   @EventHandler(
      priority = EventPriority.MONITOR
   )
   public void onPlayerInteract(PlayerInteractEvent event) {
      if (event.getAction() == Action.RIGHT_CLICK_BLOCK && event.useInteractedBlock() != Result.DENY) {
         if (event.getItem() != null && event.getItem().getType() != null && (event.getMaterial() == Material.DIRT || event.getMaterial() == Material.GRASS) && (event.getItem().getType() == Material.WOOD_HOE || event.getItem().getType() == Material.IRON_HOE || event.getItem().getType() == Material.GOLD_HOE || event.getItem().getType() == Material.DIAMOND_HOE)) {
            BlockUpdate.Update(event.getClickedBlock());
         }

      }
   }

   @EventHandler(
      priority = EventPriority.MONITOR
   )
   public void onPlayerChangeWorld(PlayerChangedWorldEvent event) {
      BlockHitManager.clearHistory(event.getPlayer());
      if (OrebfuscatorConfig.UseProximityHider) {
         synchronized(ProximityHider.proximityHiderTracker) {
            ProximityHider.proximityHiderTracker.remove(event.getPlayer());
         }
      }

   }

   @EventHandler(
      priority = EventPriority.MONITOR
   )
   public void onPlayerMove(PlayerMoveEvent event) {
      if (!event.isCancelled()) {
         if (OrebfuscatorConfig.UseProximityHider) {
            synchronized(ProximityHider.playersToCheck) {
               if (!ProximityHider.playersToCheck.containsKey(event.getPlayer())) {
                  ProximityHider.playersToCheck.put(event.getPlayer(), event.getFrom());
               }
            }
         }

      }
   }
}
