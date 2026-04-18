package net.citizensnpcs.trait.waypoint.triggers;

import net.citizensnpcs.api.npc.NPC;
import org.bukkit.Location;

public interface WaypointTrigger {
   String description();

   void onWaypointReached(NPC var1, Location var2);
}
