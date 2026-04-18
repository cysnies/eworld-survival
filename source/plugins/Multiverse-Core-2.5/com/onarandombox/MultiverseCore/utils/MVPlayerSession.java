package com.onarandombox.MultiverseCore.utils;

import com.onarandombox.MultiverseCore.api.MultiverseCoreConfig;
import java.util.Date;
import org.bukkit.entity.Player;

public class MVPlayerSession {
   private Player player;
   private long teleportLast = 0L;
   private long messageLast = 0L;
   private MultiverseCoreConfig config;

   public MVPlayerSession(Player player, MultiverseCoreConfig config) {
      super();
      this.player = player;
      this.config = config;
   }

   public void teleport() {
      this.teleportLast = (new Date()).getTime();
   }

   public boolean getTeleportable() {
      long time = (new Date()).getTime();
      return time - this.teleportLast > (long)this.config.getTeleportCooldown();
   }
}
