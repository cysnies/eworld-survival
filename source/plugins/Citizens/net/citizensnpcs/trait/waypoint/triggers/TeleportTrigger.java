package net.citizensnpcs.trait.waypoint.triggers;

import net.citizensnpcs.api.npc.NPC;
import net.citizensnpcs.api.persistence.Persist;
import org.bukkit.Location;
import org.bukkit.event.player.PlayerTeleportEvent.TeleportCause;

public class TeleportTrigger implements WaypointTrigger {
   @Persist(
      required = true
   )
   private Location location;

   public TeleportTrigger() {
      super();
   }

   public TeleportTrigger(Location location) {
      super();
      this.location = location;
   }

   public String description() {
      return String.format("Teleport to [%s, %d, %d, %d]", this.location.getWorld().getName(), this.location.getBlockX(), this.location.getBlockY(), this.location.getBlockZ());
   }

   public void onWaypointReached(NPC npc, Location waypoint) {
      if (this.location != null) {
         npc.teleport(waypoint, TeleportCause.PLUGIN);
      }

   }
}
