package net.citizensnpcs.trait.waypoint.triggers;

import net.citizensnpcs.api.CitizensAPI;
import net.citizensnpcs.api.npc.NPC;
import net.citizensnpcs.api.persistence.Persist;
import net.citizensnpcs.trait.waypoint.WaypointProvider;
import net.citizensnpcs.trait.waypoint.Waypoints;
import org.bukkit.Bukkit;
import org.bukkit.Location;

public class DelayTrigger implements WaypointTrigger {
   @Persist
   private int delay = 0;

   public DelayTrigger() {
      super();
   }

   public DelayTrigger(int delay) {
      super();
      this.delay = delay;
   }

   public String description() {
      return String.format("Delay for %d ticks", this.delay);
   }

   public int getDelay() {
      return this.delay;
   }

   public void onWaypointReached(NPC npc, Location waypoint) {
      if (this.delay > 0) {
         this.scheduleTask(((Waypoints)npc.getTrait(Waypoints.class)).getCurrentProvider());
      }

   }

   private void scheduleTask(final WaypointProvider provider) {
      provider.setPaused(true);
      Bukkit.getScheduler().scheduleSyncDelayedTask(CitizensAPI.getPlugin(), new Runnable() {
         public void run() {
            provider.setPaused(false);
         }
      }, (long)this.delay);
   }
}
