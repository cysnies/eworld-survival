package com.lishid.orebfuscator.listeners;

import com.lishid.orebfuscator.obfuscation.BlockUpdate;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityExplodeEvent;

public class OrebfuscatorEntityListener implements Listener {
   public OrebfuscatorEntityListener() {
      super();
   }

   @EventHandler(
      priority = EventPriority.MONITOR
   )
   public void onEntityExplode(EntityExplodeEvent event) {
      if (!event.isCancelled()) {
         BlockUpdate.Update(event.blockList());
      }
   }
}
