package net.citizensnpcs.trait.waypoint;

import com.google.common.collect.Lists;
import java.util.Collections;
import java.util.List;
import net.citizensnpcs.api.CitizensAPI;
import net.citizensnpcs.api.npc.NPC;
import net.citizensnpcs.api.persistence.Persist;
import net.citizensnpcs.api.persistence.PersistenceLoader;
import net.citizensnpcs.trait.waypoint.triggers.DelayTrigger;
import net.citizensnpcs.trait.waypoint.triggers.WaypointTrigger;
import net.citizensnpcs.trait.waypoint.triggers.WaypointTriggerRegistry;
import org.bukkit.Bukkit;
import org.bukkit.Location;

public class Waypoint {
   @Persist(
      required = true
   )
   private Location location;
   @Persist
   private List triggers;

   public Waypoint() {
      super();
   }

   public Waypoint(Location at) {
      super();
      this.location = at;
   }

   public void addTrigger(WaypointTrigger trigger) {
      if (this.triggers == null) {
         this.triggers = Lists.newArrayList();
      }

      this.triggers.add(trigger);
   }

   public Location getLocation() {
      return this.location;
   }

   public List getTriggers() {
      return this.triggers == null ? Collections.EMPTY_LIST : this.triggers;
   }

   public void onReach(NPC npc) {
      if (this.triggers != null) {
         this.runTriggers(npc, 0);
      }
   }

   private void runTriggers(final NPC npc, int start) {
      for(final int i = start; i < this.triggers.size(); ++i) {
         WaypointTrigger trigger = (WaypointTrigger)this.triggers.get(i);
         trigger.onWaypointReached(npc, this.location);
         if (trigger instanceof DelayTrigger) {
            int delay = ((DelayTrigger)trigger).getDelay();
            if (delay > 0) {
               Bukkit.getScheduler().scheduleSyncDelayedTask(CitizensAPI.getPlugin(), new Runnable() {
                  public void run() {
                     Waypoint.this.runTriggers(npc, i);
                  }
               }, (long)delay);
               break;
            }
         }
      }

   }

   static {
      PersistenceLoader.registerPersistDelegate(WaypointTrigger.class, WaypointTriggerRegistry.class);
   }
}
