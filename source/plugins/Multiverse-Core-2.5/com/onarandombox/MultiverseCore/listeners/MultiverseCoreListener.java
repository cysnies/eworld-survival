package com.onarandombox.MultiverseCore.listeners;

import com.onarandombox.MultiverseCore.event.MVConfigReloadEvent;
import com.onarandombox.MultiverseCore.event.MVPlayerTouchedPortalEvent;
import com.onarandombox.MultiverseCore.event.MVRespawnEvent;
import com.onarandombox.MultiverseCore.event.MVTeleportEvent;
import com.onarandombox.MultiverseCore.event.MVVersionEvent;
import com.onarandombox.MultiverseCore.event.MVWorldDeleteEvent;
import com.onarandombox.MultiverseCore.event.MVWorldPropertyChangeEvent;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

public abstract class MultiverseCoreListener implements Listener {
   public MultiverseCoreListener() {
      super();
   }

   @EventHandler
   public void worldPropertyChange(MVWorldPropertyChangeEvent event) {
   }

   @EventHandler
   public void worldDelete(MVWorldDeleteEvent event) {
   }

   @EventHandler
   public void versionRequest(MVVersionEvent event) {
   }

   @EventHandler
   public void playerTeleport(MVTeleportEvent event) {
   }

   @EventHandler
   public void playerRespawn(MVRespawnEvent event) {
   }

   @EventHandler
   public void playerTouchedPortal(MVPlayerTouchedPortalEvent event) {
   }

   @EventHandler
   public void configReload(MVConfigReloadEvent event) {
   }
}
