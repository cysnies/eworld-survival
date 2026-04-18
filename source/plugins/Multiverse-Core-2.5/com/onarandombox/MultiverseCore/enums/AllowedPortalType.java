package com.onarandombox.MultiverseCore.enums;

import org.bukkit.PortalType;

public enum AllowedPortalType {
   NONE(PortalType.CUSTOM),
   ALL(PortalType.CUSTOM),
   NETHER(PortalType.NETHER),
   END(PortalType.ENDER);

   private PortalType type;

   private AllowedPortalType(PortalType type) {
      this.type = type;
   }

   public PortalType getActualPortalType() {
      return this.type;
   }
}
